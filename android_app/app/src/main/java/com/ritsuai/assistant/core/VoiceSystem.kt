package com.ritsuai.assistant.core

import android.content.Context
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.*
import java.util.*

/**
 * Sistema de voz de Ritsu para síntesis de habla y comunicación
 * Proporciona una voz kawaii y expresiva para Ritsu
 */
class VoiceSystem(private val context: Context) {
    
    private val voiceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Sistema de síntesis de voz
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    
    // Configuración de voz
    private var currentEmotion = "neutral"
    private var voicePitch = 1.2f // Pitch más alto para voz kawaii
    private var voiceSpeed = 0.9f // Velocidad ligeramente más lenta para claridad
    
    // Estados de llamadas
    private var isInCall = false
    private var callPartner: String? = null
    
    // Callbacks para eventos de voz
    private var onSpeechStarted: (() -> Unit)? = null
    private var onSpeechCompleted: (() -> Unit)? = null
    private var onSpeechError: ((String) -> Unit)? = null
    
    // Configuraciones emocionales para la voz
    private val emotionalVoiceSettings = mapOf(
        "happy" to VoiceSettings(pitch = 1.3f, speed = 1.0f, volume = 1.0f),
        "excited" to VoiceSettings(pitch = 1.4f, speed = 1.1f, volume = 1.0f),
        "sad" to VoiceSettings(pitch = 0.9f, speed = 0.8f, volume = 0.8f),
        "angry" to VoiceSettings(pitch = 0.8f, speed = 1.1f, volume = 1.0f),
        "shy" to VoiceSettings(pitch = 1.1f, speed = 0.8f, volume = 0.7f),
        "blushing" to VoiceSettings(pitch = 1.25f, speed = 0.85f, volume = 0.8f),
        "thoughtful" to VoiceSettings(pitch = 1.0f, speed = 0.8f, volume = 0.9f),
        "surprised" to VoiceSettings(pitch = 1.35f, speed = 1.2f, volume = 1.0f),
        "loving" to VoiceSettings(pitch = 1.15f, speed = 0.85f, volume = 0.9f),
        "neutral" to VoiceSettings(pitch = 1.2f, speed = 0.9f, volume = 1.0f)
    )
    
    // Frases específicas para diferentes contextos de llamadas
    private val callPhrases = CallPhrases()
    
    init {
        initializeTextToSpeech()
    }
    
    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                setupVoice()
                isInitialized = true
            } else {
                onSpeechError?.invoke("Error al inicializar síntesis de voz")
            }
        }
    }
    
    private fun setupVoice() {
        textToSpeech?.let { tts ->
            // Configurar idioma español
            val result = tts.setLanguage(Locale("es", "ES"))
            
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback a español genérico
                tts.setLanguage(Locale("es"))
            }
            
            // Buscar voz femenina disponible
            val voices = tts.voices
            val femaleVoice = voices?.find { voice ->
                voice.name.contains("female", true) || 
                voice.name.contains("mujer", true) ||
                voice.name.contains("woman", true)
            }
            
            femaleVoice?.let { tts.voice = it }
            
            // Configurar parámetros por defecto
            applyVoiceSettings(emotionalVoiceSettings["neutral"]!!)
            
            // Configurar listener para eventos
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    onSpeechStarted?.invoke()
                }
                
                override fun onDone(utteranceId: String?) {
                    onSpeechCompleted?.invoke()
                }
                
                override fun onError(utteranceId: String?) {
                    onSpeechError?.invoke("Error durante síntesis de voz")
                }
            })
        }
    }
    
    /**
     * Hace que Ritsu hable con expresión emocional
     */
    fun speak(text: String, emotion: String = "neutral", priority: SpeechPriority = SpeechPriority.NORMAL) {
        if (!isInitialized) {
            onSpeechError?.invoke("Sistema de voz no inicializado")
            return
        }
        
        voiceScope.launch {
            try {
                // Aplicar configuración emocional
                applyEmotionalVoice(emotion)
                
                // Procesar texto para voz kawaii
                val processedText = processTextForKawaiiVoice(text, emotion)
                
                // Configurar prioridad de habla
                val queueMode = when (priority) {
                    SpeechPriority.URGENT -> TextToSpeech.QUEUE_FLUSH
                    SpeechPriority.NORMAL -> TextToSpeech.QUEUE_ADD
                    SpeechPriority.BACKGROUND -> TextToSpeech.QUEUE_ADD
                }
                
                // Generar ID único para la utterance
                val utteranceId = "ritsu_${System.currentTimeMillis()}"
                
                // Síntesis de voz
                textToSpeech?.speak(processedText, queueMode, null, utteranceId)
                
            } catch (e: Exception) {
                onSpeechError?.invoke("Error al procesar voz: ${e.message}")
            }
        }
    }
    
    /**
     * Responde una llamada telefónica con voz de Ritsu
     */
    fun handleIncomingCall(callerInfo: RitsuAICore.CallerInfo, greeting: String) {
        if (!isInitialized) return
        
        isInCall = true
        callPartner = callerInfo.name ?: callerInfo.phoneNumber
        
        voiceScope.launch {
            delay(1000) // Pequeña pausa antes de hablar
            
            // Determinar tono según el tipo de llamada
            val emotion = when {
                callerInfo.name?.lowercase()?.contains("amor") == true -> "loving"
                callerInfo.name?.lowercase()?.contains("mamá") == true -> "warm"
                callerInfo.name?.lowercase()?.contains("papá") == true -> "respectful"
                callerInfo.isContact -> "friendly"
                else -> "polite"
            }
            
            speak(greeting, emotion, SpeechPriority.URGENT)
        }
    }
    
    /**
     * Continúa una conversación telefónica
     */
    fun continuePhoneConversation(message: String, relationshipType: RitsuAICore.RelationshipType) {
        if (!isInCall) return
        
        val emotion = when (relationshipType) {
            RitsuAICore.RelationshipType.PARTNER -> "loving"
            RitsuAICore.RelationshipType.FAMILY -> "warm"
            RitsuAICore.RelationshipType.FRIEND -> "friendly"
            RitsuAICore.RelationshipType.WORK -> "professional"
            RitsuAICore.RelationshipType.UNKNOWN -> "polite"
        }
        
        speak(message, emotion, SpeechPriority.URGENT)
    }
    
    /**
     * Termina una llamada telefónica
     */
    fun endPhoneCall(relationshipType: RitsuAICore.RelationshipType) {
        if (!isInCall) return
        
        val farewell = callPhrases.getFarewell(relationshipType, callPartner)
        val emotion = when (relationshipType) {
            RitsuAICore.RelationshipType.PARTNER -> "loving"
            else -> "warm"
        }
        
        speak(farewell, emotion, SpeechPriority.URGENT)
        
        voiceScope.launch {
            delay(3000) // Esperar que termine de hablar
            isInCall = false
            callPartner = null
        }
    }
    
    /**
     * Responde automáticamente según el contexto
     */
    fun providesAutomaticResponse(context: String, urgency: Float): String? {
        if (!isInCall) return null
        
        return when (context) {
            "user_busy" -> callPhrases.getBusyResponse(urgency)
            "take_message" -> callPhrases.getMessageResponse()
            "unknown_caller" -> callPhrases.getUnknownCallerResponse()
            "spam_likely" -> callPhrases.getSpamResponse()
            else -> null
        }
    }
    
    private fun applyEmotionalVoice(emotion: String) {
        currentEmotion = emotion
        val settings = emotionalVoiceSettings[emotion] ?: emotionalVoiceSettings["neutral"]!!
        applyVoiceSettings(settings)
    }
    
    private fun applyVoiceSettings(settings: VoiceSettings) {
        textToSpeech?.let { tts ->
            tts.setPitch(settings.pitch)
            tts.setSpeechRate(settings.speed)
            
            // Ajustar volumen mediante AudioManager
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val targetVolume = (maxVolume * settings.volume).toInt()
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)
        }
    }
    
    private fun processTextForKawaiiVoice(text: String, emotion: String): String {
        var processedText = text
        
        // Remover emojis que no se pueden pronunciar
        processedText = processedText.replace(Regex("[😊😉💕❤️🌸✨🎉💫🥺🤗💝🌟💖🎊☺️💼🤝]"), "")
        
        // Reemplazar expresiones de acción por pausas naturales
        processedText = processedText.replace("*sonroja*", " ")
        processedText = processedText.replace("*sonríe*", " ")
        processedText = processedText.replace("*guiño*", " ")
        processedText = processedText.replace(Regex("\\*[^*]*\\*"), " ")
        
        // Añadir pausas naturales para expresividad
        processedText = processedText.replace("...", "... ")
        processedText = processedText.replace("!", "! ")
        processedText = processedText.replace("?", "? ")
        
        // Procesar según emoción
        processedText = when (emotion) {
            "excited" -> addExcitementToText(processedText)
            "shy" -> addShynessToText(processedText)
            "loving" -> addLoveToText(processedText)
            "surprised" -> addSurpriseToText(processedText)
            else -> processedText
        }
        
        // Limpiar espacios extra
        processedText = processedText.replace(Regex("\\s+"), " ").trim()
        
        return processedText
    }
    
    private fun addExcitementToText(text: String): String {
        // Añadir énfasis a palabras importantes
        var excitedText = text
        excitedText = excitedText.replace("genial", "¡genial!")
        excitedText = excitedText.replace("increíble", "¡increíble!")
        excitedText = excitedText.replace("perfecto", "¡perfecto!")
        return excitedText
    }
    
    private fun addShynessToText(text: String): String {
        // Hacer el texto más suave y tímido
        var shyText = text
        shyText = shyText.replace(".", "...")
        shyText = shyText.replace("!", ".")
        return shyText
    }
    
    private fun addLoveToText(text: String): String {
        // Hacer el texto más cálido y amoroso
        var loveText = text
        loveText = loveText.replace("bien", "muy bien")
        loveText = loveText.replace("bueno", "maravilloso")
        return loveText
    }
    
    private fun addSurpriseToText(text: String): String {
        // Añadir expresividad de sorpresa
        var surpriseText = text
        if (!surpriseText.startsWith("¡")) {
            surpriseText = "¡$surpriseText"
        }
        if (!surpriseText.endsWith("!")) {
            surpriseText = "$surpriseText!"
        }
        return surpriseText
    }
    
    /**
     * Configura callbacks para eventos de voz
     */
    fun setVoiceCallbacks(
        onStarted: () -> Unit,
        onCompleted: () -> Unit,
        onError: (String) -> Unit
    ) {
        onSpeechStarted = onStarted
        onSpeechCompleted = onCompleted
        onSpeechError = onError
    }
    
    /**
     * Pausa la síntesis de voz
     */
    fun pauseSpeech() {
        textToSpeech?.stop()
    }
    
    /**
     * Verifica si Ritsu está hablando
     */
    fun isSpeaking(): Boolean {
        return textToSpeech?.isSpeaking ?: false
    }
    
    /**
     * Ajusta el volumen de voz
     */
    fun adjustVolume(level: Float) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val targetVolume = (maxVolume * level.coerceIn(0f, 1f)).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)
    }
    
    /**
     * Cambia la velocidad de habla
     */
    fun setSpeechRate(rate: Float) {
        voiceSpeed = rate.coerceIn(0.5f, 2.0f)
        textToSpeech?.setSpeechRate(voiceSpeed)
    }
    
    /**
     * Cambia el pitch de voz
     */
    fun setPitch(pitch: Float) {
        voicePitch = pitch.coerceIn(0.5f, 2.0f)
        textToSpeech?.setPitch(voicePitch)
    }
    
    /**
     * Obtiene información sobre las voces disponibles
     */
    fun getAvailableVoices(): List<VoiceInfo> {
        return textToSpeech?.voices?.map { voice ->
            VoiceInfo(
                name = voice.name,
                language = voice.locale.toString(),
                quality = when (voice.quality) {
                    Voice.QUALITY_VERY_HIGH -> "Muy Alta"
                    Voice.QUALITY_HIGH -> "Alta"
                    Voice.QUALITY_NORMAL -> "Normal"
                    else -> "Baja"
                },
                isNetworkRequired = voice.isNetworkConnectionRequired
            )
        } ?: emptyList()
    }
    
    /**
     * Selecciona una voz específica
     */
    fun selectVoice(voiceName: String): Boolean {
        val voices = textToSpeech?.voices
        val selectedVoice = voices?.find { it.name == voiceName }
        
        return if (selectedVoice != null) {
            textToSpeech?.voice = selectedVoice
            true
        } else {
            false
        }
    }
    
    fun cleanup() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        voiceScope.cancel()
    }
    
    // CLASES DE DATOS
    
    data class VoiceSettings(
        val pitch: Float,
        val speed: Float,
        val volume: Float
    )
    
    data class VoiceInfo(
        val name: String,
        val language: String,
        val quality: String,
        val isNetworkRequired: Boolean
    )
    
    enum class SpeechPriority {
        URGENT,     // Interrumpe cualquier habla actual
        NORMAL,     // Se añade a la cola
        BACKGROUND  // Prioridad baja
    }
}

/**
 * Frases específicas para llamadas telefónicas
 */
class CallPhrases {
    
    fun getFarewell(relationshipType: RitsuAICore.RelationshipType, partnerName: String?): String {
        return when (relationshipType) {
            RitsuAICore.RelationshipType.PARTNER -> {
                val farewells = listOf(
                    "Adiós mi amor, que tengas un día hermoso. Te amo mucho.",
                    "Hasta luego cariño, cuídate mucho por favor.",
                    "Nos vemos pronto mi vida, siempre estaré aquí para ti."
                )
                farewells.random()
            }
            
            RitsuAICore.RelationshipType.FAMILY -> {
                "Que tengan un día maravilloso. Fue un placer conversar con ustedes."
            }
            
            RitsuAICore.RelationshipType.FRIEND -> {
                "¡Hasta luego! Siempre es genial hablar contigo."
            }
            
            RitsuAICore.RelationshipType.WORK -> {
                "Que tenga un excelente día. Ha sido un placer asistirle profesionalmente."
            }
            
            RitsuAICore.RelationshipType.UNKNOWN -> {
                "Que tenga un buen día. Gracias por la llamada."
            }
        }
    }
    
    fun getBusyResponse(urgency: Float): String {
        return when {
            urgency > 0.8f -> {
                "Entiendo que es urgente. Voy a interrumpir lo que está haciendo para que pueda atender su llamada inmediatamente."
            }
            
            urgency > 0.5f -> {
                "Comprendo que es importante. Le diré que tiene una llamada que requiere su atención."
            }
            
            else -> {
                "En este momento está ocupado, pero puedo tomar un mensaje detallado o pedirle que le devuelva la llamada cuando esté libre."
            }
        }
    }
    
    fun getMessageResponse(): String {
        val responses = listOf(
            "Por supuesto, puedo tomar un mensaje detallado. Por favor, dígame lo que le gustaría transmitir.",
            "Claro que sí, estaré encantada de tomar su mensaje. ¿Qué le gustaría que le comunique?",
            "Sin problema, tomaré nota de su mensaje. Por favor, compártame la información que desea transmitir."
        )
        return responses.random()
    }
    
    fun getUnknownCallerResponse(): String {
        return "Hola, habla Ritsu, asistente personal. ¿Podría decirme su nombre y el motivo de su llamada, por favor?"
    }
    
    fun getSpamResponse(): String {
        return "Esta llamada ha sido identificada como posible spam. Voy a colgar automáticamente para proteger su privacidad."
    }
    
    fun getPartnerSpecialGreeting(partnerName: String?): String {
        val name = partnerName ?: "amor"
        val greetings = listOf(
            "¡Hola $name! Soy Ritsu. Él está ocupado ahora pero siempre me dice que eres lo más importante en su vida.",
            "¡Cariño! Hablas con Ritsu. Me ha hablado tanto de ti que siento que ya te conozco.",
            "¡$name! Soy la asistente de tu pareja. Él siempre habla de ti con tanto amor."
        )
        return greetings.random()
    }
    
    fun getWorkGreeting(): String {
        val greetings = listOf(
            "Buenos días, habla Ritsu, asistente personal. En este momento está en una reunión importante.",
            "Hola, soy Ritsu. Él está trabajando en un proyecto crítico en este momento.",
            "Buenos días, habla con su asistente Ritsu. Está ocupado con responsabilidades laborales."
        )
        return greetings.random()
    }
    
    fun getFamilyGreeting(callerName: String?): String {
        val name = callerName ?: "familia"
        val greetings = listOf(
            "¡Hola $name! Soy Ritsu, su asistente personal. ¡Qué alegría escuchar de la familia!",
            "Buenos días $name. Habla Ritsu. Siempre es un placer recibir llamadas de la familia.",
            "¡Hola! Soy Ritsu. Él me ha hablado mucho sobre ustedes, $name."
        )
        return greetings.random()
    }
}