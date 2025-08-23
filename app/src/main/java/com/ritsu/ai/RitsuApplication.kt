package com.ritsu.ai

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.room.Room
import com.ritsu.ai.data.RitsuDatabase
import com.ritsu.ai.service.RitsuAIService
import com.ritsu.ai.service.RitsuFloatingService
import com.ritsu.ai.util.PreferenceManager
import com.ritsu.ai.util.AvatarManager
import com.ritsu.ai.util.AIManager
import com.ritsu.ai.util.ClothingGenerator
import com.ritsu.ai.util.LearningManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RitsuApplication : Application() {
    
    companion object {
        lateinit var instance: RitsuApplication
            private set
        
        // Constantes de la aplicación
        const val CHANNEL_ID_FLOATING = "ritsu_floating_channel"
        const val CHANNEL_ID_AI = "ritsu_ai_channel"
        const val CHANNEL_ID_COMMUNICATION = "ritsu_communication_channel"
        const val CHANNEL_ID_SYSTEM = "ritsu_system_channel"
        
        // Configuración del avatar
        const val AVATAR_DEFAULT_SIZE = 200
        const val AVATAR_MIN_SIZE = 100
        const val AVATAR_MAX_SIZE = 400
        
        // Configuración de IA
        const val AI_RESPONSE_TIMEOUT = 30000L // 30 segundos
        const val AI_LEARNING_RATE = 0.1f
        const val AI_MAX_MEMORY = 1000
        
        // Configuración de ropa
        const val CLOTHING_STORAGE_PATH = "ritsu_clothing"
        const val CLOTHING_MAX_ITEMS = 50
        
        // Modo especial
        const val SPECIAL_MODE_PASSWORD = "262456"
        const val SPECIAL_MODE_ENABLED_KEY = "special_mode_enabled"
        
        // Configuración de aprendizaje
        const val LEARNING_DATA_PATH = "ritsu_learning"
        const val LEARNING_MAX_PATTERNS = 500
    }
    
    // Managers principales
    lateinit var preferenceManager: PreferenceManager
    lateinit var avatarManager: AvatarManager
    lateinit var aiManager: AIManager
    lateinit var clothingGenerator: ClothingGenerator
    lateinit var learningManager: LearningManager
    
    // Base de datos
    lateinit var database: RitsuDatabase
    
    // Coroutine scope para operaciones en segundo plano
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Inicializar managers
        initializeManagers()
        
        // Inicializar base de datos
        initializeDatabase()
        
        // Crear canales de notificación
        createNotificationChannels()
        
        // Inicializar servicios en segundo plano
        initializeBackgroundServices()
        
        // Configurar aprendizaje automático
        setupLearning()
    }
    
    private fun initializeManagers() {
        preferenceManager = PreferenceManager(this)
        avatarManager = AvatarManager(this)
        aiManager = AIManager(this)
        clothingGenerator = ClothingGenerator(this)
        learningManager = LearningManager(this)
    }
    
    private fun initializeDatabase() {
        database = Room.databaseBuilder(
            applicationContext,
            RitsuDatabase::class.java,
            "ritsu_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Canal para el avatar flotante
            val floatingChannel = NotificationChannel(
                CHANNEL_ID_FLOATING,
                "Ritsu Avatar",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificaciones del avatar flotante de Ritsu"
                setShowBadge(false)
            }
            
            // Canal para servicios de IA
            val aiChannel = NotificationChannel(
                CHANNEL_ID_AI,
                "Ritsu IA",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de los servicios de inteligencia artificial"
            }
            
            // Canal para comunicación
            val communicationChannel = NotificationChannel(
                CHANNEL_ID_COMMUNICATION,
                "Ritsu Comunicación",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de llamadas, mensajes y comunicación"
            }
            
            // Canal para sistema
            val systemChannel = NotificationChannel(
                CHANNEL_ID_SYSTEM,
                "Ritsu Sistema",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificaciones del sistema de Ritsu"
            }
            
            notificationManager.createNotificationChannels(
                listOf(floatingChannel, aiChannel, communicationChannel, systemChannel)
            )
        }
    }
    
    private fun initializeBackgroundServices() {
        applicationScope.launch {
            // Inicializar servicios en segundo plano
            try {
                // Verificar si Ritsu debe iniciarse automáticamente
                if (preferenceManager.isAutoStartEnabled()) {
                    startRitsuServices()
                }
            } catch (e: Exception) {
                // Log del error
                e.printStackTrace()
            }
        }
    }
    
    private fun setupLearning() {
        applicationScope.launch {
            try {
                // Inicializar sistema de aprendizaje
                learningManager.initialize()
                
                // Cargar patrones aprendidos
                learningManager.loadLearnedPatterns()
                
                // Configurar IA con patrones aprendidos
                aiManager.updateWithLearnedPatterns(learningManager.getLearnedPatterns())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun startRitsuServices() {
        applicationScope.launch {
            try {
                // Iniciar servicio de IA
                RitsuAIService.startService(this@RitsuApplication)
                
                // Iniciar servicio flotante si está habilitado
                if (preferenceManager.isOverlayEnabled()) {
                    RitsuFloatingService.startService(this@RitsuApplication)
                }
                
                // Configurar avatar
                avatarManager.initializeAvatar()
                
                // Inicializar generador de ropa
                clothingGenerator.initialize()
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun stopRitsuServices() {
        applicationScope.launch {
            try {
                // Detener servicios
                RitsuAIService.stopService(this@RitsuApplication)
                RitsuFloatingService.stopService(this@RitsuApplication)
                
                // Guardar estado actual
                preferenceManager.saveCurrentState()
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun isSpecialModeEnabled(): Boolean {
        return preferenceManager.getBoolean(SPECIAL_MODE_ENABLED_KEY, false)
    }
    
    fun enableSpecialMode(password: String): Boolean {
        return if (password == SPECIAL_MODE_PASSWORD) {
            preferenceManager.putBoolean(SPECIAL_MODE_ENABLED_KEY, true)
            true
        } else {
            false
        }
    }
    
    fun disableSpecialMode() {
        preferenceManager.putBoolean(SPECIAL_MODE_ENABLED_KEY, false)
    }
    
    override fun onTerminate() {
        super.onTerminate()
        // Guardar estado antes de terminar
        stopRitsuServices()
    }
}