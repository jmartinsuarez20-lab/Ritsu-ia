package com.ritsu.ai.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ritsu.ai.R
import com.ritsu.ai.util.AIManager
import com.ritsu.ai.util.LearningManager
import com.ritsu.ai.util.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RitsuAIService : Service() {
    
    companion object {
        private const val TAG = "RitsuAI"
        private const val NOTIFICATION_ID = 1002
        private const val CHANNEL_ID = "ritsu_ai_channel"
        private var instance: RitsuAIService? = null
        
        fun getInstance(): RitsuAIService? = instance
    }
    
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var aiManager: AIManager
    private lateinit var learningManager: LearningManager
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var learningJob: Job? = null
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        preferenceManager = PreferenceManager(this)
        aiManager = AIManager(this)
        learningManager = LearningManager(this)
        
        createNotificationChannel()
        startLearningProcess()
        Log.d(TAG, "Servicio de IA creado")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
        learningJob?.cancel()
        Log.d(TAG, "Servicio de IA destruido")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.ai_service_notification_title),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.ai_service_notification_text)
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.ai_service_notification_title))
            .setContentText(getString(R.string.ai_service_notification_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
    
    private fun startLearningProcess() {
        learningJob = serviceScope.launch {
            while (true) {
                try {
                    // Procesar aprendizaje continuo
                    learningManager.processContinuousLearning()
                    
                    // Limpiar patrones antiguos
                    learningManager.cleanupOldPatterns()
                    
                    // Actualizar estadísticas de aprendizaje
                    updateLearningStatistics()
                    
                    delay(300000) // Esperar 5 minutos
                } catch (e: Exception) {
                    Log.e(TAG, "Error en el proceso de aprendizaje", e)
                    delay(60000) // Esperar 1 minuto en caso de error
                }
            }
        }
    }
    
    private fun updateLearningStatistics() {
        serviceScope.launch {
            try {
                val stats = learningManager.getLearningStatistics()
                Log.d(TAG, "Estadísticas de aprendizaje: $stats")
                
                // Guardar estadísticas en preferencias
                preferenceManager.setLearningStats(stats)
            } catch (e: Exception) {
                Log.e(TAG, "Error al actualizar estadísticas", e)
            }
        }
    }
    
    fun processUserInput(input: String): String {
        return try {
            val response = aiManager.processInput(input)
            
            // Aprender de la interacción
            serviceScope.launch {
                learningManager.learnFromInteraction(input, response)
            }
            
            response
        } catch (e: Exception) {
            Log.e(TAG, "Error al procesar entrada del usuario", e)
            "Lo siento, tuve un problema procesando tu solicitud. ¿Podrías intentarlo de nuevo?"
        }
    }
    
    fun speak(text: String) {
        try {
            aiManager.speak(text)
        } catch (e: Exception) {
            Log.e(TAG, "Error al hablar", e)
        }
    }
    
    fun generateClothing(description: String): String {
        return try {
            val clothingGenerator = com.ritsu.ai.util.ClothingGenerator(this)
            val clothing = clothingGenerator.generateClothing(description)
            
            // Aprender sobre preferencias de ropa
            serviceScope.launch {
                learningManager.learnUserPreferences("clothing", description, clothing.name)
            }
            
            "He generado una nueva prenda: ${clothing.name}. ${clothing.description}"
        } catch (e: Exception) {
            Log.e(TAG, "Error al generar ropa", e)
            "Lo siento, tuve un problema generando la ropa. ¿Podrías intentarlo de nuevo?"
        }
    }
    
    fun getLearningProgress(): Map<String, Any> {
        return try {
            learningManager.getLearningStatistics()
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener progreso de aprendizaje", e)
            emptyMap()
        }
    }
    
    fun resetLearning() {
        try {
            learningManager.resetLearning()
            Log.d(TAG, "Aprendizaje reiniciado")
        } catch (e: Exception) {
            Log.e(TAG, "Error al reiniciar aprendizaje", e)
        }
    }
    
    fun exportLearningData(): String {
        return try {
            learningManager.exportLearningData()
        } catch (e: Exception) {
            Log.e(TAG, "Error al exportar datos de aprendizaje", e)
            "Error al exportar datos"
        }
    }
    
    fun importLearningData(data: String): Boolean {
        return try {
            learningManager.importLearningData(data)
            Log.d(TAG, "Datos de aprendizaje importados")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al importar datos de aprendizaje", e)
            false
        }
    }
}