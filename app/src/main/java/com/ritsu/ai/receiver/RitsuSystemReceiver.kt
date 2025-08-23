package com.ritsu.ai.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ritsu.ai.service.RitsuAIService
import com.ritsu.ai.service.RitsuFloatingService

class RitsuSystemReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "RitsuSystemReceiver"
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
            Intent.ACTION_TIME_TICK -> {
                Log.d(TAG, "Cambio de tiempo")
                handleTimeTick(context)
            }
            Intent.ACTION_TIMEZONE_CHANGED -> {
                Log.d(TAG, "Zona horaria cambiada")
                handleTimezoneChanged(context)
            }
            Intent.ACTION_LOCALE_CHANGED -> {
                Log.d(TAG, "Idioma cambiado")
                handleLocaleChanged(context)
            }
            Intent.ACTION_BATTERY_LOW -> {
                Log.d(TAG, "Batería baja")
                handleBatteryLow(context)
            }
            Intent.ACTION_BATTERY_OKAY -> {
                Log.d(TAG, "Batería OK")
                handleBatteryOkay(context)
            }
            Intent.ACTION_POWER_CONNECTED -> {
                Log.d(TAG, "Cargador conectado")
                handlePowerConnected(context)
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                Log.d(TAG, "Cargador desconectado")
                handlePowerDisconnected(context)
            }
            Intent.ACTION_HEADSET_PLUG -> {
                Log.d(TAG, "Auriculares conectados/desconectados")
                handleHeadsetPlug(context, intent)
            }
            Intent.ACTION_CAMERA_BUTTON -> {
                Log.d(TAG, "Botón de cámara presionado")
                handleCameraButton(context)
            }
            Intent.ACTION_MEDIA_BUTTON -> {
                Log.d(TAG, "Botón de medios presionado")
                handleMediaButton(context, intent)
            }
        }
    }
    
    private fun handleScreenOn(context: Context) {
        try {
            // Mostrar avatar cuando se enciende la pantalla
            val floatingService = RitsuFloatingService.getInstance()
            floatingService?.showAvatar()
            
            // Notificar al servicio de IA
            val aiService = RitsuAIService.getInstance()
            aiService?.processUserInput("Pantalla encendida")
        } catch (e: Exception) {
            Log.e(TAG, "Error al manejar pantalla encendida", e)
        }
    }
    
    private fun handleScreenOff(context: Context) {
        try {
            // Ocultar avatar cuando se apaga la pantalla
            val floatingService = RitsuFloatingService.getInstance()
            floatingService?.hideAvatar()
            
            // Notificar al servicio de IA
            val aiService = RitsuAIService.getInstance()
            aiService?.processUserInput("Pantalla apagada")
        } catch (e: Exception) {
            Log.e(TAG, "Error al manejar pantalla apagada", e)
        }
    }
    
    private fun handleUserPresent(context: Context) {
        try {
            // Usuario desbloqueó el dispositivo
            val aiService = RitsuAIService.getInstance()
            aiService?.speak("¡Hola! ¿En qué puedo ayudarte?")
        } catch (e: Exception) {
            Log.e(TAG, "Error al manejar usuario presente", e)
        }
    }
    
    private fun handleTimeTick(context: Context) {
        try {
            // Verificar recordatorios y tareas programadas
            val aiService = RitsuAIService.getInstance()
            aiService?.processUserInput("Verificar recordatorios")
        } catch (e: Exception) {
            Log.e(TAG, "Error al manejar cambio de tiempo", e)
        }
    }
    
    private fun handleTimezoneChanged(context: Context) {
        try {
            // Actualizar configuraciones de tiempo
            val aiService = RitsuAIService.getInstance()
            aiService?.processUserInput("Zona horaria actualizada")
        } catch (e: Exception) {
            Log.e(TAG, "Error al manejar cambio de zona horaria", e)
        }
    }
    
    private fun handleLocaleChanged(context: Context) {
        try {
            // Actualizar idioma de Ritsu
            val aiService = RitsuAIService.getInstance()
            aiService?.processUserInput("Idioma cambiado")
        } catch (e: Exception) {
            Log.e(TAG, "Error al manejar cambio de idioma", e)
        }
    }
    
    private fun handleBatteryLow(context: Context) {
        try {
            val aiService = RitsuAIService.getInstance()
            aiService?.speak("¡Atención! La batería está baja. Te recomiendo conectar el cargador.")
        } catch (e: Exception) {
            Log.e(TAG, "Error al manejar batería baja", e)
        }
    }
    
    private fun handleBatteryOkay(context: Context) {
        try {
            val aiService = RitsuAIService.getInstance()
            aiService?.processUserInput("Batería recuperada")
        } catch (e: Exception) {
            Log.e(TAG, "Error al manejar batería OK", e)
        }
    }
    
    private fun handlePowerConnected(context: Context) {
        try {
            val aiService = RitsuAIService.getInstance()
            aiService?.speak("Cargador conectado. La batería se está cargando.")
        } catch (e: Exception) {
            Log.e(TAG, "Error al manejar cargador conectado", e)
        }
    }
    
    private fun handlePowerDisconnected(context: Context) {
        try {
            val aiService = RitsuAIService.getInstance()
            aiService?.speak("Cargador desconectado. Te recomiendo monitorear el nivel de batería.")
        } catch (e: Exception) {
            Log.e(TAG, "Error al manejar cargador desconectado", e)
        }
    }
    
    private fun handleHeadsetPlug(context: Context, intent: Intent) {
        try {
            val state = intent.getIntExtra("state", -1)
            val aiService = RitsuAIService.getInstance()
            
            when (state) {
                0 -> aiService?.speak("Auriculares desconectados")
                1 -> aiService?.speak("Auriculares conectados")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al manejar auriculares", e)
        }
    }
    
    private fun handleCameraButton(context: Context) {
        try {
            val aiService = RitsuAIService.getInstance()
            aiService?.speak("Botón de cámara presionado. ¿Quieres que tome una foto?")
        } catch (e: Exception) {
            Log.e(TAG, "Error al manejar botón de cámara", e)
        }
    }
    
    private fun handleMediaButton(context: Context, intent: Intent) {
        try {
            val aiService = RitsuAIService.getInstance()
            aiService?.processUserInput("Botón de medios presionado")
        } catch (e: Exception) {
            Log.e(TAG, "Error al manejar botón de medios", e)
        }
    }
}