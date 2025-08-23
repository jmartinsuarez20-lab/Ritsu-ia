package com.ritsu.ai.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import com.ritsu.ai.R
import com.ritsu.ai.RitsuApplication
import com.ritsu.ai.util.AvatarManager
import com.ritsu.ai.util.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

class RitsuFloatingService : Service() {
    
    companion object {
        private const val TAG = "RitsuFloating"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "ritsu_floating_channel"
        private var instance: RitsuFloatingService? = null
        
        fun getInstance(): RitsuFloatingService? = instance
    }
    
    private lateinit var windowManager: WindowManager
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var avatarManager: AvatarManager
    private lateinit var avatarView: View
    private lateinit var avatarImageView: ImageView
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    private var animationJob: Job? = null
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        preferenceManager = PreferenceManager(this)
        avatarManager = AvatarManager(this)
        
        createNotificationChannel()
        setupAvatarView()
        Log.d(TAG, "Servicio de superposición creado")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
        removeAvatarView()
        Log.d(TAG, "Servicio de superposición destruido")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        showAvatar()
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.overlay_notification_title),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.overlay_notification_text)
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.overlay_notification_title))
            .setContentText(getString(R.string.overlay_notification_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
    
    private fun setupAvatarView() {
        avatarView = LayoutInflater.from(this).inflate(R.layout.floating_avatar, null)
        avatarImageView = avatarView.findViewById(R.id.avatar_image)
        
        // Configurar parámetros de la ventana
        val params = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.TOP or Gravity.START
            
            // Posición inicial
            x = preferenceManager.getAvatarX()
            y = preferenceManager.getAvatarY()
        }
        
        // Configurar eventos táctiles
        avatarView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY
                    
                    if (abs(deltaX) > 10 || abs(deltaY) > 10) {
                        isDragging = true
                        params.x = initialX + deltaX.toInt()
                        params.y = initialY + deltaY.toInt()
                        
                        try {
                            windowManager.updateViewLayout(avatarView, params)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error al actualizar posición del avatar", e)
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        // Click simple - mostrar menú o interactuar
                        handleAvatarClick()
                    } else {
                        // Guardar nueva posición
                        preferenceManager.setAvatarPosition(params.x, params.y)
                    }
                    isDragging = false
                    true
                }
                else -> false
            }
        }
        
        // Configurar eventos de doble click
        avatarView.setOnClickListener {
            if (!isDragging) {
                handleAvatarClick()
            }
        }
    }
    
    private fun showAvatar() {
        try {
            windowManager.addView(avatarView, getAvatarLayoutParams())
            updateAvatarAppearance()
            startIdleAnimation()
            Log.d(TAG, "Avatar mostrado")
        } catch (e: Exception) {
            Log.e(TAG, "Error al mostrar avatar", e)
        }
    }
    
    private fun removeAvatarView() {
        try {
            animationJob?.cancel()
            windowManager.removeView(avatarView)
            Log.d(TAG, "Avatar removido")
        } catch (e: Exception) {
            Log.e(TAG, "Error al remover avatar", e)
        }
    }
    
    private fun getAvatarLayoutParams(): WindowManager.LayoutParams {
        val config = preferenceManager.getAvatarConfig()
        
        return WindowManager.LayoutParams().apply {
            width = config.size
            height = config.size
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.TOP or Gravity.START
            x = config.x
            y = config.y
            alpha = config.opacity
        }
    }
    
    private fun updateAvatarAppearance() {
        serviceScope.launch {
            try {
                val bitmap = avatarManager.generateAvatarBitmap()
                avatarImageView.setImageBitmap(bitmap)
                
                // Aplicar escala y rotación
                val config = preferenceManager.getAvatarConfig()
                avatarImageView.scaleX = config.scale
                avatarImageView.scaleY = config.scale
                avatarImageView.rotation = config.rotation
                
                Log.d(TAG, "Apariencia del avatar actualizada")
            } catch (e: Exception) {
                Log.e(TAG, "Error al actualizar apariencia del avatar", e)
            }
        }
    }
    
    private fun startIdleAnimation() {
        animationJob?.cancel()
        animationJob = serviceScope.launch {
            while (true) {
                delay(5000) // Esperar 5 segundos
                
                // Animación de respiración sutil
                avatarImageView.animate()
                    .scaleX(1.05f)
                    .scaleY(1.05f)
                    .setDuration(1000)
                    .withEndAction {
                        avatarImageView.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(1000)
                            .start()
                    }
                    .start()
                
                delay(2000) // Esperar 2 segundos más
            }
        }
    }
    
    private fun handleAvatarClick() {
        // Cambiar expresión
        avatarManager.updateExpression()
        updateAvatarAppearance()
        
        // Mostrar mensaje de saludo
        serviceScope.launch {
            val aiManager = AIManager(this@RitsuFloatingService)
            aiManager.speak("¡Hola! ¿En qué puedo ayudarte?")
        }
    }
    
    fun updateAvatar() {
        updateAvatarAppearance()
    }
    
    fun hideAvatar() {
        try {
            avatarImageView.visibility = View.INVISIBLE
        } catch (e: Exception) {
            Log.e(TAG, "Error al ocultar avatar", e)
        }
    }
    
    fun showAvatar() {
        try {
            avatarImageView.visibility = View.VISIBLE
        } catch (e: Exception) {
            Log.e(TAG, "Error al mostrar avatar", e)
        }
    }
    
    fun changeExpression(expression: String) {
        avatarManager.setExpression(expression)
        updateAvatarAppearance()
    }
    
    fun changeClothing(clothingId: String) {
        avatarManager.setClothing(clothingId)
        updateAvatarAppearance()
    }
    
    fun toggleSpecialMode() {
        val isSpecialMode = preferenceManager.isSpecialModeEnabled()
        preferenceManager.setSpecialMode(!isSpecialMode)
        updateAvatarAppearance()
    }
}