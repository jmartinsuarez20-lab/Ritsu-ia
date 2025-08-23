package com.ritsuai.assistant.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.IBinder
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.app.NotificationCompat
import com.ritsuai.assistant.R
import com.ritsuai.assistant.core.avatar.KawaiiAvatarRenderer
import com.ritsuai.assistant.core.avatar.ExpressionManager
import com.ritsuai.assistant.core.avatar.OutfitManager
import kotlinx.coroutines.*

/**
 * Servicio principal del Avatar Kawaii de Ritsu
 * Maneja la aparición flotante del avatar de cuerpo completo en todas las pantallas
 */
class KawaiiAvatarService : Service() {
    
    private lateinit var windowManager: WindowManager
    private lateinit var avatarView: LinearLayout
    private lateinit var avatarImageView: ImageView
    private var avatarParams: WindowManager.LayoutParams? = null
    
    // Managers para funcionalidades del avatar
    private lateinit var avatarRenderer: KawaiiAvatarRenderer
    private lateinit var expressionManager: ExpressionManager
    private lateinit var outfitManager: OutfitManager
    
    // Coroutines para animaciones y comportamiento
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Estados del avatar
    private var isUncensoredMode = false
    private var currentEmotion = "neutral"
    private var isInteracting = false
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "ritsu_avatar_service"
        
        // Códigos de activación para modo sin censura
        private val UNCENSORED_ACTIVATION_CODES = listOf(
            "ritsu_kawaii_mode",
            "unlock_private_mode",
            "avatar_full_access"
        )
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar managers
        initializeAvatarSystem()
        
        // Crear canal de notificación
        createNotificationChannel()
        
        // Configurar avatar flotante
        setupFloatingAvatar()
        
        // Iniciar comportamientos del avatar
        startAvatarBehaviors()
    }
    
    private fun initializeAvatarSystem() {
        avatarRenderer = KawaiiAvatarRenderer(this)
        expressionManager = ExpressionManager(this)
        outfitManager = OutfitManager(this)
        
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }
    
    private fun setupFloatingAvatar() {
        // Crear vista del avatar
        avatarView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = ContextCompat.getDrawable(this@KawaiiAvatarService, R.drawable.avatar_background)
        }
        
        // ImageView para el avatar de cuerpo completo
        avatarImageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(300, 600) // Cuerpo completo
            scaleType = ImageView.ScaleType.FIT_CENTER
            setImageResource(R.drawable.ritsu_base_kawaii) // Imagen base kawaii
        }
        
        avatarView.addView(avatarImageView)
        
        // Configurar parámetros de ventana flotante
        avatarParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }
        
        // Agregar listeners táctiles para interacción
        setupTouchInteraction()
        
        // Mostrar avatar
        try {
            windowManager.addView(avatarView, avatarParams)
        } catch (e: Exception) {
            // Manejar casos donde no se tienen permisos de overlay
            handleOverlayPermissionError()
        }
    }
    
    private fun setupTouchInteraction() {
        avatarView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isMoving = false
            
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = avatarParams?.x ?: 0
                        initialY = avatarParams?.y ?: 0
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isMoving = false
                        
                        // Mostrar expresión de sorpresa
                        expressionManager.showExpression("surprised")
                        return true
                    }
                    
                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = event.rawX - initialTouchX
                        val deltaY = event.rawY - initialTouchY
                        
                        if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {
                            isMoving = true
                            avatarParams?.x = initialX + deltaX.toInt()
                            avatarParams?.y = initialY + deltaY.toInt()
                            windowManager.updateViewLayout(avatarView, avatarParams)
                        }
                        return true
                    }
                    
                    MotionEvent.ACTION_UP -> {
                        if (!isMoving) {
                            // Tap en el avatar - mostrar interacción
                            handleAvatarTap()
                        }
                        // Volver a expresión neutral
                        expressionManager.showExpression("neutral")
                        return true
                    }
                }
                return false
            }
        })
    }
    
    private fun handleAvatarTap() {
        isInteracting = true
        
        serviceScope.launch {
            // Ciclo de expresiones kawaii al tocar
            val expressions = listOf("happy", "blushing", "winking", "shy")
            
            for (expression in expressions) {
                expressionManager.showExpression(expression)
                delay(800)
            }
            
            // Volver a neutral
            expressionManager.showExpression("neutral")
            isInteracting = false
        }
    }
    
    private fun startAvatarBehaviors() {
        serviceScope.launch {
            while (isActive) {
                if (!isInteracting) {
                    // Comportamientos espontáneos del avatar
                    performRandomBehavior()
                }
                delay(15000) // Cada 15 segundos
            }
        }
    }
    
    private suspend fun performRandomBehavior() {
        val behaviors = listOf(
            { blinkRandomly() },
            { lookAround() },
            { showEmotionalState() },
            { adjustClothing() }
        )
        
        behaviors.random().invoke()
    }
    
    private suspend fun blinkRandomly() {
        expressionManager.showExpression("blinking")
        delay(150)
        expressionManager.showExpression("neutral")
    }
    
    private suspend fun lookAround() {
        val directions = listOf("looking_left", "looking_right", "looking_up")
        val direction = directions.random()
        
        expressionManager.showExpression(direction)
        delay(2000)
        expressionManager.showExpression("neutral")
    }
    
    private suspend fun showEmotionalState() {
        // Estados emocionales basados en contexto
        val emotions = when {
            System.currentTimeMillis() % 2 == 0L -> listOf("happy", "excited")
            else -> listOf("thoughtful", "curious")
        }
        
        expressionManager.showExpression(emotions.random())
        delay(3000)
        expressionManager.showExpression("neutral")
    }
    
    private suspend fun adjustClothing() {
        // Pequeños ajustes en la ropa como comportamiento natural
        avatarRenderer.adjustClothing()
        delay(1000)
    }
    
    /**
     * Función para activar modo sin censura
     */
    fun activateUncensoredMode(activationCode: String): Boolean {
        if (UNCENSORED_ACTIVATION_CODES.contains(activationCode)) {
            isUncensoredMode = true
            outfitManager.enableUncensoredMode()
            return true
        }
        return false
    }
    
    fun deactivateUncensoredMode() {
        isUncensoredMode = false
        outfitManager.disableUncensoredMode()
    }
    
    /**
     * Función para cambiar ropa del avatar
     */
    fun changeOutfit(outfitDescription: String) {
        serviceScope.launch {
            outfitManager.generateAndApplyOutfit(outfitDescription)
        }
    }
    
    /**
     * Función para expresar emociones específicas
     */
    fun expressEmotion(emotion: String, intensity: Float = 1.0f) {
        currentEmotion = emotion
        expressionManager.showExpression(emotion, intensity)
    }
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Ritsu Avatar Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Servicio del avatar kawaii de Ritsu"
            setShowBadge(false)
        }
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
    
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Ritsu Avatar Activo")
            .setContentText("Tu asistente kawaii está en pantalla ✨")
            .setSmallIcon(R.drawable.ic_ritsu_notification)
            .setOngoing(true)
            .build()
    }
    
    private fun handleOverlayPermissionError() {
        // Manejar caso donde no se tienen permisos de overlay
        // Aquí podrías mostrar una notificación pidiendo al usuario que active los permisos
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Limpiar vista del avatar
        try {
            windowManager.removeView(avatarView)
        } catch (e: Exception) {
            // Vista ya removida
        }
        
        // Cancelar corrutinas
        serviceScope.cancel()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}