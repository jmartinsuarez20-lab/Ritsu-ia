package com.ritsuai.assistant.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.ritsuai.assistant.R
import com.ritsuai.assistant.core.RitsuAI
import kotlinx.coroutines.*

class RitsuAIService : Service() {
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "ritsu_ai_service"
    }
    
    private lateinit var ritsuAI: RitsuAI
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    override fun onCreate() {
        super.onCreate()
        
        createNotificationChannel()
        ritsuAI = RitsuAI(this)
        
        startForeground(NOTIFICATION_ID, createNotification())
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Ritsu siempre está activa y lista para ayudar
        return START_STICKY // Se reinicia automáticamente si el sistema la mata
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return RitsuBinder()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Ritsu AI Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Servicio principal de Ritsu AI"
                setShowBadge(false)
            }
            
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Ritsu AI Activa 🌸")
            .setContentText("Lista para ayudarte con llamadas y mensajes")
            .setSmallIcon(R.drawable.ic_ritsu_notification)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }
    
    fun getRitsuAI(): RitsuAI {
        return ritsuAI
    }
    
    override fun onDestroy() {
        super.onDestroy()
        ritsuAI.destroy()
        serviceScope.cancel()
    }
    
    inner class RitsuBinder : android.os.Binder() {
        fun getService(): RitsuAIService = this@RitsuAIService
    }
}