package com.ritsuai.assistant

import android.app.Application
import android.content.Intent
import com.ritsuai.assistant.services.RitsuAIService

class RitsuApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar la IA Ritsu al arrancar la aplicación
        startService(Intent(this, RitsuAIService::class.java))
    }
}