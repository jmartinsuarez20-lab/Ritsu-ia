package com.ritsuai.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import com.ritsuai.R
import com.ritsuai.RitsuAIApplication
import com.ritsuai.avatar.AvatarManager
import com.ritsuai.database.entities.AvatarOutfitEntity
import com.ritsuai.ui.MainActivity
import com.ritsuai.utils.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class FloatingAvatarService : Service() {
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "ritsu_avatar_channel"
        
        @Volatile
        private var INSTANCE: FloatingAvatarService? = null
        
        fun getInstance(): FloatingAvatarService? = INSTANCE
        
        suspend fun initialize(context: Context) {
            // Inicializar el servicio si no está corriendo
            if (INSTANCE == null) {
                val intent = Intent(context, FloatingAvatarService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }
    }
    
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var avatarImageView: ImageView
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var avatarManager: AvatarManager
    
    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f
    
    private val scope = CoroutineScope(Dispatchers.Main)
    
    // Parámetros de la ventana flotante
    private val layoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        },
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        x = 100
        y = 200
    }
    
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        
        // Inicializar managers
        preferenceManager = PreferenceManager(this)
        avatarManager = AvatarManager(this)
        
        // Inicializar WindowManager
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        // Crear vista flotante
        createFloatingView()
        
        // Iniciar notificación foreground
        startForeground(NOTIFICATION_ID, createNotification())
        
        // Inicializar avatar
        scope.launch {
            initializeAvatar()
        }
    }
    
    private fun createFloatingView() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = inflater.inflate(R.layout.floating_avatar, null)
        
        // Configurar ImageView del avatar
        avatarImageView = floatingView.findViewById(R.id.avatar_image_view)
        
        // Configurar eventos táctiles
        setupTouchListener()
        
        // Agregar vista a WindowManager
        try {
            windowManager.addView(floatingView, layoutParams)
        } catch (e: Exception) {
            // Manejar error de permisos
            e.printStackTrace()
        }
    }
    
    private fun setupTouchListener() {
        avatarImageView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY
                    
                    layoutParams.x = initialX + deltaX.toInt()
                    layoutParams.y = initialY + deltaY.toInt()
                    
                    try {
                        windowManager.updateViewLayout(floatingView, layoutParams)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    true
                }
                
                MotionEvent.ACTION_UP -> {
                    val deltaX = abs(event.rawX - initialTouchX)
                    val deltaY = abs(event.rawY - initialTouchY)
                    
                    // Si el movimiento fue mínimo, considerar como tap
                    if (deltaX < 10 && deltaY < 10) {
                        onAvatarTap()
                    }
                    true
                }
                
                else -> false
            }
        }
    }
    
    private fun onAvatarTap() {
        // Abrir actividad principal o mostrar menú contextual
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }
    
    private suspend fun initializeAvatar() {
        try {
            // Generar avatar inicial
            val avatarBitmap = avatarManager.generateAvatar(
                expression = AvatarManager.EXPRESSION_NEUTRAL,
                size = AvatarManager.AVATAR_SIZE
            )
            
            // Mostrar avatar en el ImageView
            withContext(Dispatchers.Main) {
                avatarImageView.setImageBitmap(avatarBitmap)
            }
            
            // Iniciar animaciones y comportamientos
            startAvatarBehavior()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun startAvatarBehavior() {
        scope.launch {
            while (true) {
                try {
                    // Cambiar expresión periódicamente
                    changeAvatarExpression()
                    
                    // Mover avatar sutilmente
                    animateAvatarMovement()
                    
                    // Esperar antes de la siguiente acción
                    kotlinx.coroutines.delay(5000) // 5 segundos
                    
                } catch (e: Exception) {
                    e.printStackTrace()
                    break
                }
            }
        }
    }
    
    private suspend fun changeAvatarExpression() {
        val expressions = listOf(
            AvatarManager.EXPRESSION_NEUTRAL,
            AvatarManager.EXPRESSION_HAPPY,
            AvatarManager.EXPRESSION_THINKING,
            AvatarManager.EXPRESSION_WORKING
        )
        
        val randomExpression = expressions.random()
        val avatarBitmap = avatarManager.generateAvatar(
            expression = randomExpression,
            size = AvatarManager.AVATAR_SIZE
        )
        
        withContext(Dispatchers.Main) {
            avatarImageView.setImageBitmap(avatarBitmap)
        }
    }
    
    private suspend fun animateAvatarMovement() {
        // Movimiento sutil del avatar
        val currentX = layoutParams.x
        val currentY = layoutParams.y
        
        // Mover ligeramente en dirección aleatoria
        val deltaX = (-5..5).random()
        val deltaY = (-5..5).random()
        
        layoutParams.x = currentX + deltaX
        layoutParams.y = currentY + deltaY
        
        try {
            withContext(Dispatchers.Main) {
                windowManager.updateViewLayout(floatingView, layoutParams)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun changeAvatarOutfit(outfit: AvatarOutfitEntity) {
        try {
            val avatarBitmap = avatarManager.generateAvatar(
                expression = AvatarManager.EXPRESSION_HAPPY,
                outfit = outfit,
                size = AvatarManager.AVATAR_SIZE
            )
            
            withContext(Dispatchers.Main) {
                avatarImageView.setImageBitmap(avatarBitmap)
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun changeAvatarExpression(expression: String) {
        try {
            val avatarBitmap = avatarManager.generateAvatar(
                expression = expression,
                size = AvatarManager.AVATAR_SIZE
            )
            
            withContext(Dispatchers.Main) {
                avatarImageView.setImageBitmap(avatarBitmap)
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun showAvatar() {
        try {
            if (floatingView.parent == null) {
                windowManager.addView(floatingView, layoutParams)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun hideAvatar() {
        try {
            if (floatingView.parent != null) {
                windowManager.removeView(floatingView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun createNotification(): Notification {
        // Crear canal de notificación
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Ritsu Avatar",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Servicio del avatar flotante de Ritsu"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        
        // Crear intent para abrir la app
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Crear notificación
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Ritsu AI")
            .setContentText("Avatar flotante activo")
            .setSmallIcon(R.drawable.ic_ritsu_avatar)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // El servicio se reiniciará si se mata
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        INSTANCE = null
        
        try {
            if (floatingView.parent != null) {
                windowManager.removeView(floatingView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}