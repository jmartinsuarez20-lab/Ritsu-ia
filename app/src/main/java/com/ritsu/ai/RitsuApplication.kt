package com.ritsu.ai

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.room.Room
import com.ritsu.ai.data.database.RitsuDatabase
import com.ritsu.ai.service.RitsuAIService
import com.ritsu.ai.service.RitsuOverlayService
import com.ritsu.ai.util.PreferenceManager
import com.ritsu.ai.util.AIEngine
import com.ritsu.ai.util.AvatarManager
import com.ritsu.ai.util.ClothingGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RitsuApplication : Application() {
    
    companion object {
        lateinit var instance: RitsuApplication
            private set
        
        lateinit var database: RitsuDatabase
            private set
        
        lateinit var preferenceManager: PreferenceManager
            private set
        
        lateinit var aiEngine: AIEngine
            private set
        
        lateinit var avatarManager: AvatarManager
            private set
        
        lateinit var clothingGenerator: ClothingGenerator
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Inicializar componentes principales
        initializeComponents()
        
        // Crear canales de notificación
        createNotificationChannels()
        
        // Inicializar servicios en background
        CoroutineScope(Dispatchers.IO).launch {
            initializeServices()
        }
    }
    
    private fun initializeComponents() {
        // Base de datos
        database = Room.databaseBuilder(
            applicationContext,
            RitsuDatabase::class.java,
            "ritsu_database"
        ).build()
        
        // Gestor de preferencias
        preferenceManager = PreferenceManager(applicationContext)
        
        // Motor de IA
        aiEngine = AIEngine(applicationContext)
        
        // Gestor de avatar
        avatarManager = AvatarManager(applicationContext)
        
        // Generador de ropa
        clothingGenerator = ClothingGenerator(applicationContext)
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Canal principal para Ritsu
            val ritsuChannel = NotificationChannel(
                CHANNEL_RITSU_MAIN,
                "Ritsu Principal",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal principal para notificaciones de Ritsu"
                enableLights(true)
                enableVibration(true)
            }
            
            // Canal para servicios de IA
            val aiChannel = NotificationChannel(
                CHANNEL_AI_SERVICE,
                "Servicios de IA",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Canal para servicios de inteligencia artificial"
                enableLights(false)
                enableVibration(false)
            }
            
            // Canal para superposición
            val overlayChannel = NotificationChannel(
                CHANNEL_OVERLAY,
                "Superposición de Pantalla",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Canal para el servicio de superposición"
                enableLights(false)
                enableVibration(false)
            }
            
            notificationManager.createNotificationChannels(
                listOf(ritsuChannel, aiChannel, overlayChannel)
            )
        }
    }
    
    private suspend fun initializeServices() {
        try {
            // Inicializar motor de IA
            aiEngine.initialize()
            
            // Inicializar gestor de avatar
            avatarManager.initialize()
            
            // Inicializar generador de ropa
            clothingGenerator.initialize()
            
            // Verificar si los servicios deben iniciarse automáticamente
            if (preferenceManager.shouldAutoStartServices()) {
                startCoreServices()
            }
        } catch (e: Exception) {
            // Log del error
            e.printStackTrace()
        }
    }
    
    private fun startCoreServices() {
        // Iniciar servicio de IA
        RitsuAIService.startService(applicationContext)
        
        // Iniciar servicio de superposición si está habilitado
        if (preferenceManager.isOverlayEnabled()) {
            RitsuOverlayService.startService(applicationContext)
        }
    }
    
    override fun onTerminate() {
        super.onTerminate()
        
        // Limpiar recursos
        try {
            aiEngine.cleanup()
            avatarManager.cleanup()
            clothingGenerator.cleanup()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    companion object {
        const val CHANNEL_RITSU_MAIN = "ritsu_main"
        const val CHANNEL_AI_SERVICE = "ai_service"
        const val CHANNEL_OVERLAY = "overlay"
    }
}