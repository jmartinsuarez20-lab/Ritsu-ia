package com.ritsu.ai.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ritsu.ai.service.RitsuAIService
import com.ritsu.ai.service.RitsuCommunicationService

class RitsuNotificationReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "RitsuNotificationReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d(TAG, "Sistema iniciado, iniciando servicios de Ritsu")
                startRitsuServices(context)
            }
            Intent.ACTION_PACKAGE_ADDED -> {
                val packageName = intent.data?.schemeSpecificPart
                Log.d(TAG, "Aplicación instalada: $packageName")
                handleAppInstalled(context, packageName)
            }
            Intent.ACTION_PACKAGE_REMOVED -> {
                val packageName = intent.data?.schemeSpecificPart
                Log.d(TAG, "Aplicación desinstalada: $packageName")
                handleAppUninstalled(context, packageName)
            }
            "android.intent.action.MY_PACKAGE_REPLACED" -> {
                Log.d(TAG, "Aplicación actualizada")
                startRitsuServices(context)
            }
            "android.intent.action.PACKAGE_REPLACED" -> {
                val packageName = intent.data?.schemeSpecificPart
                if (packageName == context.packageName) {
                    Log.d(TAG, "Ritsu actualizada")
                    startRitsuServices(context)
                }
            }
        }
    }
    
    private fun startRitsuServices(context: Context) {
        try {
            // Iniciar servicios de Ritsu
            val aiServiceIntent = Intent(context, RitsuAIService::class.java)
            val communicationServiceIntent = Intent(context, RitsuCommunicationService::class.java)
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(aiServiceIntent)
                context.startForegroundService(communicationServiceIntent)
            } else {
                context.startService(aiServiceIntent)
                context.startService(communicationServiceIntent)
            }
            
            Log.d(TAG, "Servicios de Ritsu iniciados")
        } catch (e: Exception) {
            Log.e(TAG, "Error al iniciar servicios de Ritsu", e)
        }
    }
    
    private fun handleAppInstalled(context: Context, packageName: String?) {
        packageName?.let {
            // Aprender sobre la nueva aplicación instalada
            val aiService = RitsuAIService.getInstance()
            aiService?.let { service ->
                service.processUserInput("Nueva aplicación instalada: $it")
            }
        }
    }
    
    private fun handleAppUninstalled(context: Context, packageName: String?) {
        packageName?.let {
            // Actualizar información sobre aplicación desinstalada
            val aiService = RitsuAIService.getInstance()
            aiService?.let { service ->
                service.processUserInput("Aplicación desinstalada: $it")
            }
        }
    }
}