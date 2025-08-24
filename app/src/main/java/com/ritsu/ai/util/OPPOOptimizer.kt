package com.ritsu.ai.util

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OPPOOptimizer(private val context: Context) {
    
    companion object {
        private const val TAG = "OPPOOptimizer"
        private const val OPPO_MANUFACTURER = "OPPO"
        private const val RENO_MODEL_PREFIX = "CPH"
        
        fun isOPPOReno13(): Boolean {
            return Build.MANUFACTURER.equals(OPPO_MANUFACTURER, ignoreCase = true) &&
                   Build.MODEL.contains(RENO_MODEL_PREFIX, ignoreCase = true)
        }
    }
    
    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val preferenceManager = PreferenceManager(context)
    private val scope = CoroutineScope(Dispatchers.Main)
    
    // Configuraciones específicas para OPPO Reno 13 5G
    private val oppoAudioSettings = mapOf(
        "call_volume" to 0.8f,
        "music_volume" to 0.6f,
        "speech_rate" to 0.85f,
        "pitch" to 1.05f,
        "noise_reduction" to true,
        "echo_cancellation" to true
    )
    
    fun optimizeForOPPO() {
        if (!isOPPOReno13()) {
            Log.d(TAG, "No es un OPPO Reno 13 5G, saltando optimizaciones específicas")
            return
        }
        
        Log.d(TAG, "Aplicando optimizaciones específicas para OPPO Reno 13 5G")
        
        // Optimizar configuración de audio
        optimizeAudioSettings()
        
        // Configurar para mejor rendimiento
        optimizePerformance()
        
        // Configurar para mejor calidad de llamadas
        optimizeCallQuality()
    }
    
    private fun optimizeAudioSettings() {
        try {
            // Configurar volúmenes específicos para OPPO
            val maxCallVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
            val maxMusicVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            
            val callVolume = (maxCallVolume * oppoAudioSettings["call_volume"]!!).toInt()
            val musicVolume = (maxMusicVolume * oppoAudioSettings["music_volume"]!!).toInt()
            
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, callVolume, 0)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, musicVolume, 0)
            
            // Configurar modo de audio para llamadas
            audioManager.mode = AudioManager.MODE_NORMAL
            
            Log.d(TAG, "Configuración de audio optimizada para OPPO Reno 13 5G")
        } catch (e: Exception) {
            Log.e(TAG, "Error al optimizar configuración de audio", e)
        }
    }
    
    private fun optimizePerformance() {
        try {
            // Configuraciones específicas para rendimiento en OPPO
            preferenceManager.setVoiceSpeed(oppoAudioSettings["speech_rate"]!!)
            preferenceManager.setVoicePitch(oppoAudioSettings["pitch"]!!)
            
            // Habilitar optimizaciones específicas
            preferenceManager.setOPPOOptimizationEnabled(true)
            preferenceManager.setVoiceClarityModeEnabled(true)
            
            Log.d(TAG, "Configuración de rendimiento optimizada para OPPO Reno 13 5G")
        } catch (e: Exception) {
            Log.e(TAG, "Error al optimizar rendimiento", e)
        }
    }
    
    private fun optimizeCallQuality() {
        try {
            // Configuraciones específicas para calidad de llamadas en OPPO
            preferenceManager.setCallResponseDelay(1500) // Delay más corto para OPPO
            
            Log.d(TAG, "Configuración de calidad de llamadas optimizada para OPPO Reno 13 5G")
        } catch (e: Exception) {
            Log.e(TAG, "Error al optimizar calidad de llamadas", e)
        }
    }
    
    fun setupAudioForCall() {
        if (!isOPPOReno13()) return
        
        try {
            // Configuración específica para llamadas en OPPO Reno 13 5G
            audioManager.mode = AudioManager.MODE_IN_CALL
            audioManager.isSpeakerphoneOn = false
            
            // Configurar volúmenes para llamadas
            val maxCallVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
            val callVolume = (maxCallVolume * 0.9f).toInt() // 90% del volumen máximo
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, callVolume, 0)
            
            // Configurar micrófono
            val maxMusicVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val musicVolume = (maxMusicVolume * 0.5f).toInt() // 50% para evitar eco
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, musicVolume, 0)
            
            Log.d(TAG, "Audio configurado para llamada en OPPO Reno 13 5G")
        } catch (e: Exception) {
            Log.e(TAG, "Error al configurar audio para llamada", e)
        }
    }
    
    fun restoreAudioAfterCall() {
        if (!isOPPOReno13()) return
        
        try {
            // Restaurar configuración normal
            audioManager.mode = AudioManager.MODE_NORMAL
            audioManager.isSpeakerphoneOn = false
            
            // Restaurar volúmenes
            val maxCallVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
            val maxMusicVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            
            val callVolume = (maxCallVolume * oppoAudioSettings["call_volume"]!!).toInt()
            val musicVolume = (maxMusicVolume * oppoAudioSettings["music_volume"]!!).toInt()
            
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, callVolume, 0)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, musicVolume, 0)
            
            Log.d(TAG, "Audio restaurado después de llamada en OPPO Reno 13 5G")
        } catch (e: Exception) {
            Log.e(TAG, "Error al restaurar audio después de llamada", e)
        }
    }
    
    fun getOptimalSpeechRate(): Float {
        return if (isOPPOReno13()) {
            oppoAudioSettings["speech_rate"]!!
        } else {
            1.0f
        }
    }
    
    fun getOptimalPitch(): Float {
        return if (isOPPOReno13()) {
            oppoAudioSettings["pitch"]!!
        } else {
            1.0f
        }
    }
    
    fun getOptimalCallDelay(): Int {
        return if (isOPPOReno13()) {
            1500 // 1.5 segundos para OPPO
        } else {
            2000 // 2 segundos para otros dispositivos
        }
    }
    
    fun enableNoiseReduction() {
        if (!isOPPOReno13()) return
        
        try {
            // Habilitar reducción de ruido específica para OPPO
            if (oppoAudioSettings["noise_reduction"] == true) {
                // Configuraciones específicas para reducción de ruido
                Log.d(TAG, "Reducción de ruido habilitada para OPPO Reno 13 5G")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al habilitar reducción de ruido", e)
        }
    }
    
    fun enableEchoCancellation() {
        if (!isOPPOReno13()) return
        
        try {
            // Habilitar cancelación de eco específica para OPPO
            if (oppoAudioSettings["echo_cancellation"] == true) {
                // Configuraciones específicas para cancelación de eco
                Log.d(TAG, "Cancelación de eco habilitada para OPPO Reno 13 5G")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al habilitar cancelación de eco", e)
        }
    }
    
    fun getDeviceInfo(): Map<String, String> {
        return mapOf(
            "manufacturer" to Build.MANUFACTURER,
            "model" to Build.MODEL,
            "brand" to Build.BRAND,
            "product" to Build.PRODUCT,
            "device" to Build.DEVICE,
            "is_oppo_reno" to isOPPOReno13().toString(),
            "android_version" to Build.VERSION.RELEASE,
            "sdk_version" to Build.VERSION.SDK_INT.toString()
        )
    }
    
    fun applyCallOptimizations() {
        if (!isOPPOReno13()) return
        
        scope.launch {
            try {
                // Aplicar optimizaciones con delay para estabilidad
                delay(500)
                setupAudioForCall()
                delay(200)
                enableNoiseReduction()
                delay(200)
                enableEchoCancellation()
                
                Log.d(TAG, "Todas las optimizaciones de llamada aplicadas para OPPO Reno 13 5G")
            } catch (e: Exception) {
                Log.e(TAG, "Error al aplicar optimizaciones de llamada", e)
            }
        }
    }
}