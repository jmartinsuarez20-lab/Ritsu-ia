package com.ritsuai.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ritsuai.services.FloatingAvatarService

class ScreenReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "ScreenReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_ON -> {
                Log.d(TAG, "Pantalla encendida")
                handleScreenOn(context)
            }
            Intent.ACTION_SCREEN_OFF -> {
                Log.d(TAG, "Pantalla apagada")
                handleScreenOff(context)
            }
            Intent.ACTION_USER_PRESENT -> {
                Log.d(TAG, "Usuario presente")
                handleUserPresent(context)
            }
        }
    }
    
    private fun handleScreenOn(context: Context) {
        // Mostrar avatar cuando se enciende la pantalla
        val floatingService = FloatingAvatarService.getInstance()
        floatingService?.showAvatar()
    }
    
    private fun handleScreenOff(context: Context) {
        // Ocultar avatar cuando se apaga la pantalla
        val floatingService = FloatingAvatarService.getInstance()
        floatingService?.hideAvatar()
    }
    
    private fun handleUserPresent(context: Context) {
        // Usuario desbloqueó el dispositivo
        val floatingService = FloatingAvatarService.getInstance()
        floatingService?.showAvatar()
    }
}