package com.ritsuai.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ritsuai.services.FloatingAvatarService
import com.ritsuai.services.AIService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_QUICKBOOT_POWERON -> {
                Log.d(TAG, "Dispositivo iniciado, configurando Ritsu AI")
                setupRitsuAfterBoot(context)
            }
        }
    }
    
    private fun setupRitsuAfterBoot(context: Context) {
        val scope = CoroutineScope(Dispatchers.Main)
        
        scope.launch {
            try {
                // Inicializar servicios de Ritsu
                AIService.initialize(context)
                FloatingAvatarService.initialize(context)
                
                Log.d(TAG, "Ritsu AI configurado exitosamente después del arranque")
            } catch (e: Exception) {
                Log.e(TAG, "Error configurando Ritsu AI después del arranque", e)
            }
        }
    }
}