package com.ritsu.ai.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import com.ritsu.ai.data.model.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.File
import java.nio.MappedByteBuffer
import java.util.*

class AIEngine(private val context: Context) {
    
    companion object {
        private const val MODEL_FILE = "ritsu_ai_model.tflite"
        private const val VOCAB_FILE = "ritsu_vocab.txt"
        private const val PERSONALITY_FILE = "ritsu_personality.json"
        private const val MAX_RESPONSE_LENGTH = 500
        private const val LEARNING_RATE = 0.001f
    }
    
    // Componentes de IA
    private var tfliteInterpreter: Interpreter? = null
    private var textToSpeech: TextToSpeech? = null
    private var vocabulary: List<String> = emptyList()
    private var personalityData: Map<String, Any> = emptyMap()
    
    // Estado del motor
    private var isInitialized = false
    private var conversationHistory: MutableList<ConversationEntry> = mutableListOf()
    private var userPreferences: UserPreferences? = null
    private var learningData: MutableMap<String, Float> = mutableMapOf()
    
    // Configuración de personalidad de Ritsu
    private val ritsuPersonality = mapOf(
        "name" to "Ritsu",
        "personality" to "formal_but_friendly",
        "speaking_style" to "polite_and_respectful",
        "emotions" to listOf("happy", "concerned", "excited", "thoughtful", "helpful"),
        "language" to "spanish",
        "formality_level" to "high",
        "response_length" to "detailed",
        "special_traits" to listOf("super_intelligent", "expressive", "caring", "efficient")
    )
    
    suspend fun initialize() {
        withContext(Dispatchers.IO) {
            try {
                // Cargar modelo de TensorFlow Lite
                loadTensorFlowModel()
                
                // Cargar vocabulario
                loadVocabulary()
                
                // Cargar datos de personalidad
                loadPersonalityData()
                
                // Inicializar Text-to-Speech
                initializeTextToSpeech()
                
                // Cargar datos de aprendizaje previo
                loadLearningData()
                
                isInitialized = true
                
            } catch (e: Exception) {
                throw AIInitializationException("Error inicializando motor de IA: ${e.message}", e)
            }
        }
    }
    
    private fun loadTensorFlowModel() {
        try {
            val modelFile = File(context.getExternalFilesDir(null), MODEL_FILE)
            if (!modelFile.exists()) {
                // Si no existe el modelo, crear uno básico
                createBasicModel()
            }
            
            val modelBuffer: MappedByteBuffer = FileUtil.loadMappedFile(context, MODEL_FILE)
            val options = Interpreter.Options()
            options.setNumThreads(4)
            options.setUseNNAPI(true)
            
            tfliteInterpreter = Interpreter(modelBuffer, options)
            
        } catch (e: Exception) {
            // Fallback a modelo básico si hay error
            createBasicModel()
        }
    }
    
    private fun createBasicModel() {
        // Crear modelo básico de fallback
        // En una implementación real, esto sería más complejo
    }
    
    private fun loadVocabulary() {
        try {
            val vocabFile = File(context.getExternalFilesDir(null), VOCAB_FILE)
            if (vocabFile.exists()) {
                vocabulary = vocabFile.readLines()
            } else {
                // Vocabulario básico por defecto
                vocabulary = createBasicVocabulary()
                vocabFile.writeText(vocabulary.joinToString("\n"))
            }
        } catch (e: Exception) {
            vocabulary = createBasicVocabulary()
        }
    }
    
    private fun createBasicVocabulary(): List<String> {
        return listOf(
            "hola", "gracias", "por favor", "ayuda", "dispositivo", "aplicación",
            "configuración", "mensaje", "llamada", "archivo", "foto", "música",
            "calendario", "contacto", "notificación", "wifi", "bluetooth",
            "batería", "almacenamiento", "pantalla", "sonido", "vibración",
            "ritsu", "asistente", "inteligente", "ayudar", "hacer", "abrir",
            "cerrar", "enviar", "recibir", "buscar", "encontrar", "organizar",
            "recordar", "programar", "cambiar", "mejorar", "optimizar"
        )
    }
    
    private fun loadPersonalityData() {
        try {
            val personalityFile = File(context.getExternalFilesDir(null), PERSONALITY_FILE)
            if (personalityFile.exists()) {
                // Cargar desde archivo JSON
                // Implementar parser JSON
            } else {
                personalityData = ritsuPersonality
                // Guardar en archivo
                savePersonalityData()
            }
        } catch (e: Exception) {
            personalityData = ritsuPersonality
        }
    }
    
    private fun savePersonalityData() {
        // Implementar guardado de datos de personalidad
    }
    
    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Configurar voz en español
                val result = textToSpeech?.setLanguage(Locale("es", "ES"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Fallback a inglés si no hay español
                    textToSpeech?.setLanguage(Locale.US)
                }
                
                // Configurar velocidad y tono
                textToSpeech?.setSpeechRate(0.9f)
                textToSpeech?.setPitch(1.1f)
                
            } else {
                // Error en TTS
            }
        }
    }
    
    private fun loadLearningData() {
        try {
            val learningFile = File(context.getExternalFilesDir(null), "learning_data.json")
            if (learningFile.exists()) {
                // Cargar datos de aprendizaje previo
                // Implementar parser JSON
            }
        } catch (e: Exception) {
            // Usar datos por defecto
        }
    }
    
    suspend fun generateResponse(userMessage: String, preferences: UserPreferences?): String {
        if (!isInitialized) {
            throw AIException("Motor de IA no inicializado")
        }
        
        return withContext(Dispatchers.Default) {
            try {
                // Guardar entrada en historial
                addToConversationHistory(userMessage, "user")
                
                // Analizar mensaje del usuario
                val analysis = analyzeUserMessage(userMessage)
                
                // Generar respuesta contextual
                val response = generateContextualResponse(analysis, preferences)
                
                // Aplicar personalidad de Ritsu
                val personalizedResponse = applyRitsuPersonality(response, analysis.emotion)
                
                // Guardar respuesta en historial
                addToConversationHistory(personalizedResponse, "ritsu")
                
                // Aprender de la interacción
                learnFromInteraction(userMessage, personalizedResponse, analysis)
                
                // Limitar longitud de respuesta
                if (personalizedResponse.length > MAX_RESPONSE_LENGTH) {
                    personalizedResponse.take(MAX_RESPONSE_LENGTH) + "..."
                } else {
                    personalizedResponse
                }
                
            } catch (e: Exception) {
                generateFallbackResponse(userMessage)
            }
        }
    }
    
    private fun analyzeUserMessage(message: String): MessageAnalysis {
        val lowerMessage = message.lowercase()
        
        return MessageAnalysis(
            intent = detectIntent(lowerMessage),
            emotion = detectEmotion(lowerMessage),
            urgency = detectUrgency(lowerMessage),
            complexity = detectComplexity(lowerMessage),
            keywords = extractKeywords(lowerMessage),
            language = detectLanguage(message)
        )
    }
    
    private fun detectIntent(message: String): String {
        return when {
            message.contains("ayuda") || message.contains("ayudar") -> "help"
            message.contains("abrir") || message.contains("iniciar") -> "open_app"
            message.contains("cerrar") || message.contains("terminar") -> "close_app"
            message.contains("enviar") || message.contains("mandar") -> "send"
            message.contains("buscar") || message.contains("encontrar") -> "search"
            message.contains("configurar") || message.contains("ajustar") -> "configure"
            message.contains("recordar") || message.contains("recordatorio") -> "reminder"
            message.contains("foto") || message.contains("imagen") -> "photo"
            message.contains("música") || message.contains("audio") -> "music"
            message.contains("llamar") || message.contains("teléfono") -> "call"
            message.contains("mensaje") || message.contains("whatsapp") -> "message"
            message.contains("calendario") || message.contains("evento") -> "calendar"
            message.contains("ropa") || message.contains("vestir") -> "clothing"
            message.contains("avatar") || message.contains("apariencia") -> "avatar"
            else -> "general_conversation"
        }
    }
    
    private fun detectEmotion(message: String): String {
        return when {
            message.contains("gracias") || message.contains("genial") -> "happy"
            message.contains("problema") || message.contains("error") -> "concerned"
            message.contains("urgente") || message.contains("rápido") -> "urgent"
            message.contains("triste") || message.contains("mal") -> "sad"
            message.contains("sorprendente") || message.contains("increíble") -> "surprised"
            else -> "neutral"
        }
    }
    
    private fun detectUrgency(message: String): Int {
        return when {
            message.contains("urgente") || message.contains("ahora") -> 3
            message.contains("rápido") || message.contains("pronto") -> 2
            else -> 1
        }
    }
    
    private fun detectComplexity(message: String): Int {
        val wordCount = message.split(" ").size
        return when {
            wordCount > 15 -> 3
            wordCount > 8 -> 2
            else -> 1
        }
    }
    
    private fun extractKeywords(message: String): List<String> {
        return message.split(" ").filter { word ->
            vocabulary.contains(word.lowercase()) && word.length > 2
        }
    }
    
    private fun detectLanguage(message: String): String {
        val spanishWords = listOf("hola", "gracias", "por", "que", "como", "donde", "cuando")
        val englishWords = listOf("hello", "thanks", "for", "what", "how", "where", "when")
        
        val spanishCount = spanishWords.count { message.lowercase().contains(it) }
        val englishCount = englishWords.count { message.lowercase().contains(it) }
        
        return if (spanishCount > englishCount) "spanish" else "english"
    }
    
    private fun generateContextualResponse(analysis: MessageAnalysis, preferences: UserPreferences?): String {
        val baseResponse = when (analysis.intent) {
            "help" -> generateHelpResponse(analysis)
            "open_app" -> generateAppOpeningResponse(analysis)
            "close_app" -> generateAppClosingResponse(analysis)
            "send" -> generateSendingResponse(analysis)
            "search" -> generateSearchResponse(analysis)
            "configure" -> generateConfigurationResponse(analysis)
            "reminder" -> generateReminderResponse(analysis)
            "photo" -> generatePhotoResponse(analysis)
            "music" -> generateMusicResponse(analysis)
            "call" -> generateCallResponse(analysis)
            "message" -> generateMessageResponse(analysis)
            "calendar" -> generateCalendarResponse(analysis)
            "clothing" -> generateClothingResponse(analysis)
            "avatar" -> generateAvatarResponse(analysis)
            else -> generateGeneralResponse(analysis)
        }
        
        // Personalizar según preferencias del usuario
        return personalizeResponse(baseResponse, preferences, analysis)
    }
    
    private fun generateHelpResponse(analysis: MessageAnalysis): String {
        return "Estoy aquí para ayudarte. Puedo ayudarte con aplicaciones, mensajes, llamadas, fotos, música, calendario y mucho más. ¿Qué te gustaría que haga?"
    }
    
    private fun generateAppOpeningResponse(analysis: MessageAnalysis): String {
        return "Con gusto te ayudo a abrir la aplicación. ¿Cuál te gustaría que abra?"
    }
    
    private fun generateAppClosingResponse(analysis: MessageAnalysis): String {
        return "Entiendo que quieres cerrar una aplicación. ¿Cuál de las que están abiertas te gustaría que cierre?"
    }
    
    private fun generateSendingResponse(analysis: MessageAnalysis): String {
        return "Te ayudo a enviar. ¿Qué quieres enviar y a quién?"
    }
    
    private fun generateSearchResponse(analysis: MessageAnalysis): String {
        return "Perfecto, te ayudo a buscar. ¿Qué estás buscando exactamente?"
    }
    
    private fun generateConfigurationResponse(analysis: MessageAnalysis): String {
        return "Te ayudo con la configuración. ¿Qué te gustaría configurar o ajustar?"
    }
    
    private fun generateReminderResponse(analysis: MessageAnalysis): String {
        return "Excelente idea establecer un recordatorio. ¿Qué quieres que te recuerde y cuándo?"
    }
    
    private fun generatePhotoResponse(analysis: MessageAnalysis): String {
        return "Te ayudo con las fotos. ¿Quieres tomar una nueva foto, ver las existentes o hacer algo específico con ellas?"
    }
    
    private fun generateMusicResponse(analysis: MessageAnalysis): String {
        return "¡Música! ¿Quieres reproducir algo específico, crear una playlist o ajustar la configuración de audio?"
    }
    
    private fun generateCallResponse(analysis: MessageAnalysis): String {
        return "Te ayudo con las llamadas. ¿Quieres hacer una llamada, ver el historial o configurar algo?"
    }
    
    private fun generateMessageResponse(analysis: MessageAnalysis): String {
        return "Te ayudo con los mensajes. ¿Quieres enviar un WhatsApp, SMS o configurar las notificaciones?"
    }
    
    private fun generateCalendarResponse(analysis: MessageAnalysis): String {
        return "Perfecto para el calendario. ¿Quieres ver eventos, crear uno nuevo o configurar recordatorios?"
    }
    
    private fun generateClothingResponse(analysis: MessageAnalysis): String {
        return "¡Cambio de ropa! ¿Qué estilo te gustaría que genere para mí? Puedo crear outfits casuales, elegantes, deportivos o cualquier cosa que se te ocurra."
    }
    
    private fun generateAvatarResponse(analysis: MessageAnalysis): String {
        return "¡Configuración del avatar! ¿Te gustaría cambiar mi apariencia, expresiones o configurar algún aspecto específico?"
    }
    
    private fun generateGeneralResponse(analysis: MessageAnalysis): String {
        return "Entiendo lo que dices. ¿En qué más puedo ayudarte hoy? Estoy aquí para asistirte con cualquier tarea en tu dispositivo."
    }
    
    private fun personalizeResponse(
        baseResponse: String,
        preferences: UserPreferences?,
        analysis: MessageAnalysis
    ): String {
        var response = baseResponse
        
        // Ajustar formalidad según preferencias del usuario
        preferences?.let { prefs ->
            if (prefs.preferFormalLanguage) {
                response = makeResponseMoreFormal(response)
            } else {
                response = makeResponseMoreCasual(response)
            }
            
            // Ajustar longitud según preferencias
            if (prefs.preferShortResponses && response.length > 100) {
                response = response.take(100) + "..."
            }
        }
        
        // Ajustar según urgencia
        if (analysis.urgency > 2) {
            response = "¡" + response.replaceFirstChar { it.uppercase() }
        }
        
        return response
    }
    
    private fun makeResponseMoreFormal(response: String): String {
        return response.replace("te ayudo", "le ayudo")
            .replace("puedo ayudarte", "puedo ayudarle")
            .replace("¿qué te gustaría", "¿qué le gustaría")
    }
    
    private fun makeResponseMoreCasual(response: String): String {
        return response.replace("le ayudo", "te ayudo")
            .replace("puedo ayudarle", "puedo ayudarte")
            .replace("¿qué le gustaría", "¿qué te gustaría")
    }
    
    private fun applyRitsuPersonality(response: String, emotion: String): String {
        var personalizedResponse = response
        
        // Agregar expresiones características de Ritsu
        when (emotion) {
            "happy" -> {
                personalizedResponse = "¡Perfecto! $personalizedResponse 😊"
            }
            "concerned" -> {
                personalizedResponse = "Entiendo tu preocupación. $personalizedResponse 🤔"
            }
            "urgent" -> {
                personalizedResponse = "¡Por supuesto! $personalizedResponse ⚡"
            }
            "sad" -> {
                personalizedResponse = "No te preocupes, estoy aquí para ayudarte. $personalizedResponse 💙"
            }
            "surprised" -> {
                personalizedResponse = "¡Oh! $personalizedResponse 😮"
            }
        }
        
        // Agregar formalidad característica de Ritsu
        if (!personalizedResponse.startsWith("¡") && !personalizedResponse.startsWith("¡")) {
            personalizedResponse = "Bien, $personalizedResponse"
        }
        
        return personalizedResponse
    }
    
    private fun learnFromInteraction(userMessage: String, response: String, analysis: MessageAnalysis) {
        // Implementar aprendizaje automático
        val interactionKey = "${analysis.intent}_${analysis.emotion}"
        val currentScore = learningData[interactionKey] ?: 0f
        learningData[interactionKey] = currentScore + LEARNING_RATE
        
        // Guardar datos de aprendizaje
        saveLearningData()
    }
    
    private fun saveLearningData() {
        try {
            val learningFile = File(context.getExternalFilesDir(null), "learning_data.json")
            // Implementar guardado en JSON
        } catch (e: Exception) {
            // Manejar error
        }
    }
    
    private fun generateFallbackResponse(userMessage: String): String {
        return "Lo siento, tuve un pequeño problema procesando tu mensaje. ¿Podrías decirlo de otra manera? Estoy aquí para ayudarte."
    }
    
    private fun addToConversationHistory(message: String, sender: String) {
        conversationHistory.add(ConversationEntry(message, sender, System.currentTimeMillis()))
        
        // Mantener solo los últimos 100 mensajes
        if (conversationHistory.size > 100) {
            conversationHistory.removeAt(0)
        }
    }
    
    suspend fun analyzeEmotion(text: String): String {
        return withContext(Dispatchers.Default) {
            val analysis = analyzeUserMessage(text)
            analysis.emotion
        }
    }
    
    fun speakText(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ritsu_speech")
    }
    
    fun getContext(): Context = context
    
    fun cleanup() {
        textToSpeech?.shutdown()
        tfliteInterpreter?.close()
        isInitialized = false
    }
    
    // Clases de datos
    data class MessageAnalysis(
        val intent: String,
        val emotion: String,
        val urgency: Int,
        val complexity: Int,
        val keywords: List<String>,
        val language: String
    )
    
    data class ConversationEntry(
        val message: String,
        val sender: String,
        val timestamp: Long
    )
    
    // Excepciones
    class AIException(message: String) : Exception(message)
    class AIInitializationException(message: String, cause: Throwable? = null) : Exception(message, cause)
}