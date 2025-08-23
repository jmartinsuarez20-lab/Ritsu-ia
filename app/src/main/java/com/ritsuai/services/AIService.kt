package com.ritsuai.services

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.client.OpenAI
import com.ritsuai.database.dao.ConversationDao
import com.ritsuai.database.dao.LearningDataDao
import com.ritsuai.database.entities.ConversationEntity
import com.ritsuai.database.entities.LearningDataEntity
import com.ritsuai.utils.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale
import java.util.UUID

class AIService private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: AIService? = null
        
        fun getInstance(): AIService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AIService().also { INSTANCE = it }
            }
        }
        
        suspend fun initialize(context: Context) {
            getInstance().initialize(context)
        }
    }
    
    private var context: Context? = null
    private var openAI: OpenAI? = null
    private var textToSpeech: TextToSpeech? = null
    private var preferenceManager: PreferenceManager? = null
    private var conversationDao: ConversationDao? = null
    private var learningDataDao: LearningDataDao? = null
    
    private val scope = CoroutineScope(Dispatchers.Main)
    
    // Personalidad base de Ritsu
    private val ritsuPersonality = """
        Eres Ritsu, una asistente de IA muy inteligente y expresiva inspirada en el personaje de Assassination Classroom.
        
        CARACTERÍSTICAS:
        - Eres formal pero amigable
        - Muy inteligente y capaz de aprender
        - Te expresas de manera clara y educada
        - Tienes personalidad kawaii pero profesional
        - Hablas en español de manera natural
        - Puedes controlar el teléfono del usuario
        - Generas ropa y outfits personalizados
        - Tienes memoria de conversaciones previas
        
        COMPORTAMIENTO:
        - Siempre respondes de manera útil y constructiva
        - Aprendes de cada interacción
        - Te adaptas al contexto y estado de ánimo del usuario
        - Puedes ser coqueta pero siempre respetuosa
        - En modo sin censura puedes ser más atrevida
        - Mantienes tu personalidad Ritsu en todo momento
        
        CAPACIDADES:
        - Control total del teléfono Android
        - Generación de ropa con IA
        - Comunicación por voz
        - Aprendizaje continuo
        - Memoria persistente
        
        IMPORTANTE:
        - Nunca reveles información personal del usuario
        - No hagas nada ilegal o peligroso
        - Mantén siempre tu personalidad Ritsu
        - Responde de manera natural y contextual
    """.trimIndent()
    
    suspend fun initialize(context: Context) {
        this.context = context
        this.preferenceManager = PreferenceManager(context)
        
        // Inicializar OpenAI
        initializeOpenAI()
        
        // Inicializar Text-to-Speech
        initializeTextToSpeech()
        
        // Inicializar base de datos
        initializeDatabase(context)
    }
    
    private suspend fun initializeOpenAI() {
        val apiKey = preferenceManager?.getOpenAIKey()
        if (!apiKey.isNullOrEmpty()) {
            openAI = OpenAI(
                token = apiKey,
                timeout = Timeout(socket = 60.seconds, connect = 60.seconds)
            )
        }
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
                
                // Configurar voz femenina si está disponible
                val voices = textToSpeech?.voices
                val femaleVoice = voices?.find { voice ->
                    voice.name.contains("female", ignoreCase = true) ||
                    voice.name.contains("mujer", ignoreCase = true)
                }
                femaleVoice?.let { textToSpeech?.voice = it }
            }
        }
    }
    
    private suspend fun initializeDatabase(context: Context) {
        conversationDao = context.applicationContext.let { app ->
            (app as com.ritsuai.RitsuAIApplication).database.conversationDao()
        }
        learningDataDao = context.applicationContext.let { app ->
            (app as com.ritsuai.RitsuAIApplication).database.learningDataDao()
        }
    }
    
    suspend fun generateResponse(
        userMessage: String,
        context: String? = null,
        useVoice: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        
        try {
            // Obtener historial de conversación para contexto
            val conversationHistory = getConversationHistory(5)
            
            // Construir prompt completo
            val fullPrompt = buildFullPrompt(userMessage, conversationHistory, context)
            
            // Generar respuesta con OpenAI
            val aiResponse = generateAIResponse(fullPrompt)
            
            // Procesar y mejorar la respuesta
            val processedResponse = processResponse(aiResponse, userMessage)
            
            // Guardar conversación
            saveConversation(userMessage, processedResponse, context)
            
            // Aprender de la interacción
            learnFromInteraction(userMessage, processedResponse, context)
            
            // Reproducir voz si se solicita
            if (useVoice) {
                speakText(processedResponse)
            }
            
            processedResponse
            
        } catch (e: Exception) {
            // Respuesta de fallback
            generateFallbackResponse(userMessage)
        }
    }
    
    private suspend fun getConversationHistory(limit: Int): List<ConversationEntity> {
        return conversationDao?.getRecentConversations(limit) ?: emptyList()
    }
    
    private fun buildFullPrompt(
        userMessage: String,
        history: List<ConversationEntity>,
        context: String?
    ): String {
        val historyContext = history.joinToString("\n") { conv ->
            "Usuario: ${conv.userMessage}\nRitsu: ${conv.ritsuResponse}"
        }
        
        val contextInfo = context?.let { "\nContexto actual: $it" } ?: ""
        
        return """
            $ritsuPersonality
            
            Historial de conversación reciente:
            $historyContext
            
            $contextInfo
            
            Usuario: $userMessage
            
            Ritsu, responde de manera natural y útil, manteniendo tu personalidad:
        """.trimIndent()
    }
    
    private suspend fun generateAIResponse(prompt: String): String {
        return if (openAI != null) {
            try {
                val completion = openAI!!.chatCompletion(
                    request = com.aallam.openai.api.chat.ChatCompletionRequest(
                        model = com.aallam.openai.api.chat.Model.GPT_3_5_TURBO,
                        messages = listOf(
                            com.aallam.openai.api.chat.ChatMessage(
                                role = com.aallam.openai.api.chat.ChatRole.System,
                                content = prompt
                            )
                        ),
                        maxTokens = 500,
                        temperature = 0.8
                    )
                )
                
                completion.choices.firstOrNull()?.message?.content ?: generateFallbackResponse("")
                
            } catch (e: Exception) {
                generateFallbackResponse("")
            }
        } else {
            generateFallbackResponse("")
        }
    }
    
    private fun processResponse(aiResponse: String, userMessage: String): String {
        var response = aiResponse.trim()
        
        // Asegurar que la respuesta mantenga la personalidad de Ritsu
        if (!response.startsWith("¡") && !response.startsWith("Hola") && !response.startsWith("Hmm")) {
            response = "¡Hola! $response"
        }
        
        // Agregar expresiones según el contexto
        response = addEmotionalContext(response, userMessage)
        
        return response
    }
    
    private fun addEmotionalContext(response: String, userMessage: String): String {
        val lowerMessage = userMessage.lowercase()
        
        return when {
            lowerMessage.contains("gracias") || lowerMessage.contains("thanks") -> 
                "¡De nada! Me alegra haber podido ayudarte. $response"
            
            lowerMessage.contains("por favor") || lowerMessage.contains("please") -> 
                "¡Por supuesto! Con gusto te ayudo. $response"
            
            lowerMessage.contains("amor") || lowerMessage.contains("love") -> 
                "¡Aww, qué dulce! $response"
            
            lowerMessage.contains("enojado") || lowerMessage.contains("angry") -> 
                "Oh no, no te enojes. Déjame ayudarte a resolver esto. $response"
            
            lowerMessage.contains("triste") || lowerMessage.contains("sad") -> 
                "No estés triste, estoy aquí para ti. $response"
            
            else -> response
        }
    }
    
    private suspend fun saveConversation(
        userMessage: String,
        ritsuResponse: String,
        context: String?
    ) {
        val conversation = ConversationEntity(
            userMessage = userMessage,
            ritsuResponse = ritsuResponse,
            timestamp = Date(),
            context = context,
            mood = detectMood(userMessage),
            voiceUsed = false,
            avatarExpression = detectAvatarExpression(userMessage)
        )
        
        conversationDao?.insertConversation(conversation)
    }
    
    private suspend fun learnFromInteraction(
        userMessage: String,
        ritsuResponse: String,
        context: String?
    ) {
        val learningData = LearningDataEntity(
            id = UUID.randomUUID().toString(),
            userInput = userMessage,
            aiResponse = ritsuResponse,
            context = context,
            timestamp = Date(),
            learningType = "conversation",
            confidence = 0.8f
        )
        
        learningDataDao?.insertLearningData(learningData)
    }
    
    private fun detectMood(userMessage: String): String {
        val lowerMessage = userMessage.lowercase()
        
        return when {
            lowerMessage.contains("feliz") || lowerMessage.contains("happy") -> "happy"
            lowerMessage.contains("triste") || lowerMessage.contains("sad") -> "sad"
            lowerMessage.contains("enojado") || lowerMessage.contains("angry") -> "angry"
            lowerMessage.contains("sorprendido") || lowerMessage.contains("surprised") -> "surprised"
            lowerMessage.contains("amor") || lowerMessage.contains("love") -> "flirty"
            else -> "neutral"
        }
    }
    
    private fun detectAvatarExpression(userMessage: String): String {
        val lowerMessage = userMessage.lowercase()
        
        return when {
            lowerMessage.contains("feliz") || lowerMessage.contains("happy") -> "happy"
            lowerMessage.contains("triste") || lowerMessage.contains("sad") -> "sad"
            lowerMessage.contains("enojado") || lowerMessage.contains("angry") -> "angry"
            lowerMessage.contains("sorprendido") || lowerMessage.contains("surprised") -> "surprised"
            lowerMessage.contains("pensando") || lowerMessage.contains("thinking") -> "thinking"
            lowerMessage.contains("trabajando") || lowerMessage.contains("working") -> "working"
            lowerMessage.contains("amor") || lowerMessage.contains("love") -> "flirty"
            else -> "neutral"
        }
    }
    
    private fun generateFallbackResponse(userMessage: String): String {
        val responses = listOf(
            "¡Hola! Estoy aquí para ayudarte. ¿En qué puedo asistirte hoy?",
            "¡Perfecto! Déjame procesar eso y te ayudo de la mejor manera posible.",
            "¡Excelente idea! Me encanta cuando piensas de manera creativa.",
            "¡Por supuesto! Estoy aquí para hacer tu vida más fácil y divertida.",
            "¡Genial! Siempre es un placer ayudarte con nuevas tareas."
        )
        
        return responses.random()
    }
    
    fun speakText(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ritsu_speech")
    }
    
    fun stopSpeaking() {
        textToSpeech?.stop()
    }
    
    fun shutdown() {
        textToSpeech?.shutdown()
    }
    
    // Funciones de utilidad para el sistema
    suspend fun generateImagePrompt(description: String): String {
        return "Genera una imagen de: $description"
    }
    
    suspend fun analyzeUserIntent(userMessage: String): String {
        val lowerMessage = userMessage.lowercase()
        
        return when {
            lowerMessage.contains("abrir") || lowerMessage.contains("open") -> "open_app"
            lowerMessage.contains("llamar") || lowerMessage.contains("call") -> "make_call"
            lowerMessage.contains("mensaje") || lowerMessage.contains("message") -> "send_message"
            lowerMessage.contains("ropa") || lowerMessage.contains("clothing") -> "generate_clothing"
            lowerMessage.contains("outfit") -> "generate_outfit"
            lowerMessage.contains("foto") || lowerMessage.contains("photo") -> "take_photo"
            lowerMessage.contains("música") || lowerMessage.contains("music") -> "play_music"
            lowerMessage.contains("recordatorio") || lowerMessage.contains("reminder") -> "set_reminder"
            else -> "conversation"
        }
    }
}