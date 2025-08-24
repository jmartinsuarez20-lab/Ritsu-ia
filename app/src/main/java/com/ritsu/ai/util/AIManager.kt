package com.ritsu.ai.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import com.ritsu.ai.data.RitsuDatabase
import com.ritsu.ai.data.model.LearningPattern
import com.ritsu.ai.data.model.UserPreference
import com.ritsu.ai.data.model.PatternType
import com.ritsu.ai.data.model.PreferenceCategory
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.max

class AIManager(private val context: Context) {
    
    private val database = RitsuDatabase.getInstance(context)
    private val preferenceManager = PreferenceManager(context)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var textToSpeech: TextToSpeech? = null
    private var isTTSInitialized = false
    
    // Configuración de IA
    private val maxResponseLength = 500
    private val minConfidence = 0.7f
    private val learningRate = 0.1f
    
    // Patrones de respuesta predefinidos
    private val defaultResponses = mapOf(
        "saludo" to listOf(
            "¡Hola! Soy Ritsu, tu asistente personal. ¿En qué puedo ayudarte hoy?",
            "¡Hola! Estoy aquí para asistirte con cualquier tarea que necesites.",
            "¡Hola! Soy Ritsu, lista para ayudarte con tu dispositivo."
        ),
        "despedida" to listOf(
            "¡Hasta luego! Ha sido un placer ayudarte.",
            "¡Adiós! No dudes en llamarme si necesitas algo más.",
            "¡Hasta la próxima! Estaré aquí cuando me necesites."
        ),
        "agradecimiento" to listOf(
            "¡De nada! Es un placer poder ayudarte.",
            "No hay de qué. Estoy aquí para eso.",
            "¡Me alegra haber podido ayudarte!"
        ),
        "confusion" to listOf(
            "Lo siento, no entendí bien eso. ¿Podrías decirlo de otra manera?",
            "No estoy segura de lo que quieres decir. ¿Puedes ser más específico?",
            "Disculpa, no comprendí. ¿Podrías reformular tu solicitud?"
        ),
        "error" to listOf(
            "Lo siento, tuve un pequeño problema. ¿Podrías intentarlo de nuevo?",
            "Ocurrió un error inesperado. Intentemos de nuevo.",
            "Algo salió mal. ¿Podemos intentarlo otra vez?"
        ),
        "llamada_entrante" to listOf(
            "¡Hola! Soy el asistente de ${preferenceManager.getUserName()}. ¿En qué puedo ayudarle?",
            "Buenos días, soy el asistente personal. ¿Cómo puedo asistirle?",
            "Hola, soy Ritsu, el asistente de ${preferenceManager.getUserName()}. ¿En qué puedo ayudarle?"
        ),
        "llamada_contacto" to listOf(
            "¡Hola! Soy Ritsu, el asistente de ${preferenceManager.getUserName()}. ¿En qué puedo ayudarte?",
            "¡Hola! Estoy aquí para asistirte. ¿Qué necesitas?",
            "¡Hola! Soy el asistente personal. ¿Cómo puedo ayudarte?"
        ),
        "llamada_followup" to listOf(
            "Entiendo perfectamente. ¿Hay algo más en lo que pueda ayudarte?",
            "Perfecto. ¿Necesitas que tome alguna nota o recuerde algo importante?",
            "Excelente. ¿Te parece bien si continuamos con eso?",
            "Muy bien. ¿Hay algún detalle adicional que deba considerar?"
        )
    )
    
    // Comandos predefinidos
    private val defaultCommands = mapOf(
        "abrir" to "OPEN_APP",
        "cerrar" to "CLOSE_APP",
        "enviar" to "SEND_MESSAGE",
        "llamar" to "MAKE_CALL",
        "foto" to "TAKE_PHOTO",
        "música" to "PLAY_MUSIC",
        "calendario" to "OPEN_CALENDAR",
        "recordatorio" to "SET_REMINDER",
        "configuración" to "OPEN_SETTINGS",
        "volumen" to "ADJUST_VOLUME",
        "brillo" to "ADJUST_BRIGHTNESS",
        "wifi" to "TOGGLE_WIFI",
        "bluetooth" to "TOGGLE_BLUETOOTH",
        "modo avión" to "TOGGLE_AIRPLANE_MODE",
        "reiniciar" to "RESTART_DEVICE",
        "apagar" to "SHUTDOWN_DEVICE"
    )
    
    init {
        initializeTTS()
        loadDefaultPatterns()
    }
    
    private fun initializeTTS() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale("es", "ES"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Fallback a inglés si el español no está disponible
                    textToSpeech?.setLanguage(Locale.US)
                }
                
                // Configurar voz
                val voices = textToSpeech?.voices
                val femaleVoice = voices?.find { it.name.contains("female", ignoreCase = true) }
                if (femaleVoice != null) {
                    textToSpeech?.voice = femaleVoice
                }
                
                // Configurar velocidad y pitch
                textToSpeech?.setSpeechRate(preferenceManager.getVoiceSpeed())
                textToSpeech?.setPitch(preferenceManager.getVoicePitch())
                
                isTTSInitialized = true
            }
        }
    }
    
    private fun loadDefaultPatterns() {
        scope.launch {
            try {
                // Cargar patrones de respuesta predefinidos
                defaultResponses.forEach { (key, responses) ->
                    responses.forEach { response ->
                        val pattern = LearningPattern.createResponsePattern(
                            key,
                            response,
                            "default"
                        )
                        database.learningPatternDao().insertLearningPattern(pattern)
                    }
                }
                
                // Cargar comandos predefinidos
                defaultCommands.forEach { (command, action) ->
                    val pattern = LearningPattern.createCommandPattern(
                        command,
                        action,
                        "default"
                    )
                    database.learningPatternDao().insertLearningPattern(pattern)
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    suspend fun processInput(input: String, context: String? = null): AIResponse {
        val normalizedInput = input.lowercase().trim()
        
        // Buscar patrones de aprendizaje existentes
        val learnedPatterns = database.learningPatternDao().searchLearningPatterns(normalizedInput)
        val bestPattern = findBestPattern(learnedPatterns, normalizedInput)
        
        if (bestPattern != null && bestPattern.confidence >= minConfidence) {
            // Usar patrón aprendido
            database.learningPatternDao().incrementUsage(bestPattern.id)
            return AIResponse(
                text = bestPattern.output,
                action = extractAction(bestPattern.output),
                confidence = bestPattern.confidence,
                patternId = bestPattern.id,
                isLearned = true
            )
        }
        
        // Buscar en comandos predefinidos
        val command = findMatchingCommand(normalizedInput)
        if (command != null) {
            val response = generateCommandResponse(command, normalizedInput)
            learnPattern(normalizedInput, response, PatternType.COMMAND, context)
            return AIResponse(
                text = response,
                action = command,
                confidence = 0.9f,
                patternId = null,
                isLearned = false
            )
        }
        
        // Buscar en respuestas predefinidas
        val defaultResponse = findDefaultResponse(normalizedInput)
        if (defaultResponse != null) {
            learnPattern(normalizedInput, defaultResponse, PatternType.RESPONSE, context)
            return AIResponse(
                text = defaultResponse,
                action = null,
                confidence = 0.8f,
                patternId = null,
                isLearned = false
            )
        }
        
        // Generar respuesta contextual
        val contextualResponse = generateContextualResponse(normalizedInput, context)
        learnPattern(normalizedInput, contextualResponse, PatternType.RESPONSE, context)
        return AIResponse(
            text = contextualResponse,
            action = null,
            confidence = 0.6f,
            patternId = null,
            isLearned = false
        )
    }
    
    private fun findBestPattern(patterns: List<LearningPattern>, input: String): LearningPattern? {
        return patterns.maxByOrNull { pattern ->
            val similarity = pattern.getSimilarity(input)
            val weight = pattern.getWeight()
            similarity * weight
        }
    }
    
    private fun findMatchingCommand(input: String): String? {
        return defaultCommands.entries.find { (command, _) ->
            input.contains(command, ignoreCase = true)
        }?.value
    }
    
    private fun findDefaultResponse(input: String): String? {
        val responses = when {
            input.contains("hola", ignoreCase = true) || input.contains("buenos días", ignoreCase = true) || 
            input.contains("buenas tardes", ignoreCase = true) || input.contains("buenas noches", ignoreCase = true) ->
                defaultResponses["saludo"]
            
            input.contains("adiós", ignoreCase = true) || input.contains("hasta luego", ignoreCase = true) ||
            input.contains("chao", ignoreCase = true) || input.contains("nos vemos", ignoreCase = true) ->
                defaultResponses["despedida"]
            
            input.contains("gracias", ignoreCase = true) || input.contains("gracias", ignoreCase = true) ->
                defaultResponses["agradecimiento"]
            
            else -> null
        }
        
        return responses?.random()
    }
    
    private fun generateCommandResponse(command: String, input: String): String {
        val appName = extractAppName(input)
        return when (command) {
            "OPEN_APP" -> "Voy a abrir $appName para ti."
            "CLOSE_APP" -> "Cerraré $appName ahora."
            "SEND_MESSAGE" -> "Preparando el envío de mensaje..."
            "MAKE_CALL" -> "Iniciando la llamada..."
            "TAKE_PHOTO" -> "Abriendo la cámara para tomar una foto."
            "PLAY_MUSIC" -> "Reproduciendo música para ti."
            "OPEN_CALENDAR" -> "Abriendo tu calendario."
            "SET_REMINDER" -> "Configurando el recordatorio."
            "OPEN_SETTINGS" -> "Abriendo la configuración del dispositivo."
            "ADJUST_VOLUME" -> "Ajustando el volumen."
            "ADJUST_BRIGHTNESS" -> "Ajustando el brillo de la pantalla."
            "TOGGLE_WIFI" -> "Cambiando el estado del WiFi."
            "TOGGLE_BLUETOOTH" -> "Cambiando el estado del Bluetooth."
            "TOGGLE_AIRPLANE_MODE" -> "Cambiando el modo avión."
            "RESTART_DEVICE" -> "Reiniciando el dispositivo."
            "SHUTDOWN_DEVICE" -> "Apagando el dispositivo."
            else -> "Ejecutando el comando solicitado."
        }
    }
    
    private fun extractAppName(input: String): String {
        val appKeywords = listOf("whatsapp", "instagram", "facebook", "twitter", "youtube", "gmail", "chrome", "fotos", "cámara", "música", "calendario", "configuración")
        
        return appKeywords.find { input.contains(it, ignoreCase = true) } ?: "la aplicación"
    }
    
    private fun extractAction(response: String): String? {
        return when {
            response.contains("abrir", ignoreCase = true) -> "OPEN_APP"
            response.contains("cerrar", ignoreCase = true) -> "CLOSE_APP"
            response.contains("enviar", ignoreCase = true) -> "SEND_MESSAGE"
            response.contains("llamar", ignoreCase = true) -> "MAKE_CALL"
            response.contains("foto", ignoreCase = true) -> "TAKE_PHOTO"
            response.contains("música", ignoreCase = true) -> "PLAY_MUSIC"
            response.contains("calendario", ignoreCase = true) -> "OPEN_CALENDAR"
            response.contains("recordatorio", ignoreCase = true) -> "SET_REMINDER"
            else -> null
        }
    }
    
    private fun generateContextualResponse(input: String, context: String?): String {
        val responses = listOf(
            "Entiendo lo que dices. Déjame procesar esa información.",
            "Interesante. ¿Podrías ser más específico sobre lo que necesitas?",
            "Estoy analizando tu solicitud. Un momento por favor.",
            "Comprendo. ¿Hay algo más que pueda hacer por ti?",
            "Gracias por la información. ¿En qué más puedo ayudarte?"
        )
        
        return responses.random()
    }
    
    private suspend fun learnPattern(input: String, output: String, type: PatternType, context: String?) {
        try {
            val pattern = LearningPattern(
                patternType = type,
                input = input,
                output = output,
                context = context
            )
            database.learningPatternDao().insertLearningPattern(pattern)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun speak(text: String) {
        if (isTTSInitialized && preferenceManager.isVoiceEnabled()) {
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }
    
    fun stopSpeaking() {
        textToSpeech?.stop()
    }
    
    fun updateWithLearnedPatterns(patterns: List<LearningPattern>) {
        scope.launch {
            try {
                patterns.forEach { pattern ->
                    database.learningPatternDao().insertLearningPattern(pattern)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun cleanup() {
        textToSpeech?.shutdown()
        scope.cancel()
    }
    
    // Métodos específicos para manejo de llamadas
    fun generateCallResponse(callerName: String, callerNumber: String, isKnownContact: Boolean): String {
        val timeOfDay = getTimeOfDay()
        val userName = preferenceManager.getUserName()
        
        return when {
            isKnownContact -> {
                when (timeOfDay) {
                    "morning" -> "¡Hola $callerName! Buenos días, soy Ritsu, el asistente de $userName. ¿Cómo estás?"
                    "afternoon" -> "¡Hola $callerName! Buenas tardes, soy Ritsu, el asistente de $userName. ¿Qué tal va tu día?"
                    "evening" -> "¡Hola $callerName! Buenas noches, soy Ritsu, el asistente de $userName. ¿Cómo va todo?"
                    else -> "¡Hola $callerName! Soy Ritsu, el asistente de $userName. ¿En qué puedo ayudarte?"
                }
            }
            else -> {
                when (timeOfDay) {
                    "morning" -> "Buenos días, soy Ritsu, el asistente de $userName. ¿En qué puedo ayudarle?"
                    "afternoon" -> "Buenas tardes, soy Ritsu, el asistente de $userName. ¿En qué puedo ayudarle?"
                    "evening" -> "Buenas noches, soy Ritsu, el asistente de $userName. ¿En qué puedo ayudarle?"
                    else -> "Hola, soy Ritsu, el asistente de $userName. ¿En qué puedo ayudarle?"
                }
            }
        }
    }
    
    fun generateFollowUpResponse(context: String? = null): String {
        val responses = listOf(
            "Entiendo perfectamente. ¿Hay algo más en lo que pueda ayudarte?",
            "Perfecto. ¿Necesitas que tome alguna nota o recuerde algo importante?",
            "Excelente. ¿Te parece bien si continuamos con eso?",
            "Muy bien. ¿Hay algún detalle adicional que deba considerar?",
            "Comprendo. ¿Hay alguna otra cosa que necesites?",
            "Perfecto. ¿Quieres que proceda con eso?"
        )
        
        return responses.random()
    }
    
    private fun getTimeOfDay(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "morning"
            hour < 18 -> "afternoon"
            else -> "evening"
        }
    }
    
    fun speakWithClarity(text: String) {
        if (isTTSInitialized && preferenceManager.isVoiceEnabled()) {
            // Configurar para mejor claridad en llamadas
            if (preferenceManager.isVoiceClarityModeEnabled()) {
                textToSpeech?.setSpeechRate(0.9f) // Hablar más lento para mayor claridad
                textToSpeech?.setPitch(1.1f) // Pitch ligeramente más alto para mejor audibilidad
            }
            
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            
            // Restaurar configuración normal después de un delay
            if (preferenceManager.isVoiceClarityModeEnabled()) {
                scope.launch {
                    delay(5000) // Esperar 5 segundos
                    textToSpeech?.setSpeechRate(preferenceManager.getVoiceSpeed())
                    textToSpeech?.setPitch(preferenceManager.getVoicePitch())
                }
            }
        }
    }
    
    fun speakDuringCall(text: String) {
        if (isTTSInitialized && preferenceManager.isVoiceEnabled()) {
            // Configuración específica para llamadas
            textToSpeech?.setSpeechRate(0.85f) // Hablar más lento durante llamadas
            textToSpeech?.setPitch(1.05f) // Pitch ligeramente más alto
            
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            
            // Restaurar configuración después de la llamada
            scope.launch {
                delay(3000)
                textToSpeech?.setSpeechRate(preferenceManager.getVoiceSpeed())
                textToSpeech?.setPitch(preferenceManager.getVoicePitch())
            }
        }
    }
    
    data class AIResponse(
        val text: String,
        val action: String?,
        val confidence: Float,
        val patternId: String?,
        val isLearned: Boolean
    )
}