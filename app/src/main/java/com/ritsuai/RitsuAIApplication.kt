package com.ritsuai

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.room.Room
import com.ritsuai.database.RitsuDatabase
import com.ritsuai.services.AIService
import com.ritsuai.services.FloatingAvatarService
import com.ritsuai.utils.PreferenceManager
import com.ritsuai.utils.AvatarManager
import com.ritsuai.utils.ClothingGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RitsuAIApplication : Application() {
    
    companion object {
        lateinit var instance: RitsuAIApplication
            private set
        
        const val CHANNEL_ID_AVATAR = "ritsu_avatar_channel"
        const val CHANNEL_ID_AI = "ritsu_ai_channel"
        const val CHANNEL_ID_SYSTEM = "ritsu_system_channel"
    }
    
    // Base de datos
    lateinit var database: RitsuDatabase
        private set
    
    // Managers
    lateinit var preferenceManager: PreferenceManager
        private set
    lateinit var avatarManager: AvatarManager
        private set
    lateinit var clothingGenerator: ClothingGenerator
        private set
    
    // Coroutine scope para la aplicación
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Inicializar managers
        initializeManagers()
        
        // Inicializar base de datos
        initializeDatabase()
        
        // Crear canales de notificación
        createNotificationChannels()
        
        // Inicializar servicios en background
        applicationScope.launch {
            initializeServices()
        }
        
        // Configurar Ritsu
        setupRitsu()
    }
    
    private fun initializeManagers() {
        preferenceManager = PreferenceManager(this)
        avatarManager = AvatarManager(this)
        clothingGenerator = ClothingGenerator(this)
    }
    
    private fun initializeDatabase() {
        database = Room.databaseBuilder(
            applicationContext,
            RitsuDatabase::class.java,
            "ritsu_database"
        ).build()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Canal para el avatar flotante
            val avatarChannel = NotificationChannel(
                CHANNEL_ID_AVATAR,
                "Ritsu Avatar",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificaciones del avatar de Ritsu"
                setShowBadge(false)
            }
            
            // Canal para la IA
            val aiChannel = NotificationChannel(
                CHANNEL_ID_AI,
                "Ritsu AI",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de la inteligencia artificial"
            }
            
            // Canal para el sistema
            val systemChannel = NotificationChannel(
                CHANNEL_ID_SYSTEM,
                "Ritsu Sistema",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones del sistema de Ritsu"
            }
            
            notificationManager.createNotificationChannels(
                listOf(avatarChannel, aiChannel, systemChannel)
            )
        }
    }
    
    private suspend fun initializeServices() {
        try {
            // Inicializar servicio de IA
            AIService.initialize(this)
            
            // Inicializar servicio de avatar flotante
            FloatingAvatarService.initialize(this)
            
        } catch (e: Exception) {
            // Log del error
            e.printStackTrace()
        }
    }
    
    private fun setupRitsu() {
        // Configurar personalidad base de Ritsu
        preferenceManager.setRitsuPersonality(
            name = "Ritsu",
            personality = "Soy Ritsu, tu asistente personal de IA. " +
                    "Soy formal pero amigable, muy inteligente y siempre estoy aquí para ayudarte. " +
                    "Puedo controlar tu teléfono, generar ropa, y aprender de nuestras interacciones.",
            voice = "female_spanish",
            avatarStyle = "anime_kawaii"
        )
        
        // Configurar modo sin censura (desbloqueado por defecto)
        preferenceManager.setUncensoredMode(true)
        
        // Configurar permisos básicos
        preferenceManager.setBasicPermissionsGranted(false)
        preferenceManager.setAccessibilityEnabled(false)
        preferenceManager.setOverlayPermissionGranted(false)
    }
    
    override fun onTerminate() {
        super.onTerminate()
        // Limpiar recursos
        database.close()
    }
}