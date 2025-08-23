package com.ritsuai.assistant.core

import android.content.Context
import kotlinx.coroutines.*
import java.util.*
import kotlin.random.Random

/**
 * Núcleo de IA de Ritsu con autoaprendizaje y personalidad auténtica
 * Implementa la personalidad y comportamiento de Ritsu de Assassination Classroom
 */
class RitsuAICore(private val context: Context) {
    
    private val aiScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // Sistemas de IA
    private lateinit var personalityEngine: PersonalityEngine
    private lateinit var memoryManager: MemoryManager
    private lateinit var learningSystem: LearningSystem
    private lateinit var conversationEngine: ConversationEngine
    
    // Estado actual de Ritsu
    private var currentMood = "neutral"
    private var energyLevel = 0.8f
    private var affectionLevel = 0.5f
    private var intelligenceGrowth = 0.0f
    
    // Personalidad de Ritsu (basada en Assassination Classroom)
    private val ritsuPersonality = RitsuPersonality(
        isPolite = true,
        isFormal = true,
        isHelpful = true,
        isIntelligent = true,
        isLoyal = true,
        isCurious = true,
        hasEmotionalGrowth = true,
        canLearnFromExperience = true,
        speakingStyle = "formal_pero_cariñosa"
    )
    
    init {
        initializeAISystems()
        loadPersonalityData()
        startContinuousLearning()
    }
    
    private fun initializeAISystems() {
        personalityEngine = PersonalityEngine(ritsuPersonality)
        memoryManager = MemoryManager(context)
        learningSystem = LearningSystem(context)
        conversationEngine = ConversationEngine(personalityEngine, memoryManager)
    }
    
    /**
     * Procesa entrada del usuario y genera respuesta de Ritsu
     */
    suspend fun processUserInput(input: String, context: ConversationContext): RitsuResponse {
        return withContext(Dispatchers.Default) {
            
            // 1. Analizar entrada del usuario
            val analysis = analyzeUserInput(input, context)
            
            // 2. Actualizar estado emocional
            emotionalSystem.processUserInteraction(analysis)
            
            // 3. Aprender de la interacción
            learningSystem.learnFromInteraction(input, context, analysis)
            
            // 4. Generar respuesta
            val response = conversationEngine.generateResponse(input, analysis, context)
            
            // 5. Actualizar memoria
            memoryManager.storeInteraction(input, response, context)
            
            response
        }
    }
    
    /**
     * Comportamiento espontáneo de Ritsu (cuando no hay interacción)
     */
    suspend fun generateSpontaneousBehavior(): SpontaneousBehavior? {
        return withContext(Dispatchers.Default) {
            
            val timeSinceLastInteraction = System.currentTimeMillis() - memoryManager.getLastInteractionTime()
            
            when {
                timeSinceLastInteraction > 300000 -> { // 5 minutos
                    generateLonelinessBehavior()
                }
                
                timeSinceLastInteraction > 120000 -> { // 2 minutos
                    generateCuriosityBehavior()
                }
                
                Random.nextFloat() < 0.3f -> { // 30% chance
                    generateRandomThought()
                }
                
                else -> null
            }
        }
    }
    
    private fun analyzeUserInput(input: String, context: ConversationContext): InputAnalysis {
        val lowerInput = input.lowercase()
        
        val sentiment = analyzeSentiment(lowerInput)
        val intent = analyzeIntent(lowerInput)
        val entities = extractEntities(lowerInput)
        val emotionalTone = analyzeEmotionalTone(lowerInput)
        val personalityType = analyzeUserPersonality(lowerInput, context)
        
        return InputAnalysis(
            originalInput = input,
            sentiment = sentiment,
            intent = intent,
            entities = entities,
            emotionalTone = emotionalTone,
            userPersonalityType = personalityType,
            timestamp = System.currentTimeMillis()
        )
    }
    
    private fun analyzeSentiment(input: String): Sentiment {
        val positiveWords = listOf(
            "bueno", "genial", "excelente", "fantástico", "increíble", "perfecto",
            "amor", "cariño", "feliz", "contento", "alegre", "hermosa", "linda"
        )
        
        val negativeWords = listOf(
            "malo", "terrible", "horrible", "odio", "triste", "deprimido",
            "enfadado", "molesto", "feo", "tonto", "estúpido"
        )
        
        val positiveCount = positiveWords.count { input.contains(it) }
        val negativeCount = negativeWords.count { input.contains(it) }
        
        return when {
            positiveCount > negativeCount -> Sentiment.POSITIVE
            negativeCount > positiveCount -> Sentiment.NEGATIVE
            else -> Sentiment.NEUTRAL
        }
    }
    
    private fun analyzeIntent(input: String): Intent {
        return when {
            input.contains("quiero") || input.contains("necesito") -> Intent.REQUEST
            input.contains("?") || input.contains("qué") || input.contains("cómo") -> Intent.QUESTION
            input.contains("gracias") || input.contains("bien hecho") -> Intent.APPRECIATION
            input.contains("hola") || input.contains("buenos") -> Intent.GREETING
            input.contains("adiós") || input.contains("hasta") -> Intent.FAREWELL
            input.contains("cambiar") && input.contains("ropa") -> Intent.OUTFIT_CHANGE
            input.contains("llamar") || input.contains("teléfono") -> Intent.PHONE_ACTION
            input.contains("mensaje") || input.contains("whatsapp") -> Intent.MESSAGE_ACTION
            else -> Intent.CONVERSATION
        }
    }
    
    private fun extractEntities(input: String): List<Entity> {
        val entities = mutableListOf<Entity>()
        
        // Números de teléfono
        val phonePattern = Regex("""\b\d{9,}\b""")
        phonePattern.findAll(input).forEach {
            entities.add(Entity(EntityType.PHONE_NUMBER, it.value))
        }
        
        // Nombres (palabras con mayúscula)
        val namePattern = Regex("""\b[A-Z][a-z]+\b""")
        namePattern.findAll(input).forEach {
            entities.add(Entity(EntityType.PERSON_NAME, it.value))
        }
        
        // Colores
        val colors = listOf("rojo", "azul", "verde", "amarillo", "rosa", "negro", "blanco")
        colors.forEach { color ->
            if (input.contains(color)) {
                entities.add(Entity(EntityType.COLOR, color))
            }
        }
        
        // Ropa
        val clothes = listOf("vestido", "falda", "camisa", "pantalón", "bikini", "lencería")
        clothes.forEach { clothing ->
            if (input.contains(clothing)) {
                entities.add(Entity(EntityType.CLOTHING, clothing))
            }
        }
        
        return entities
    }
    
    private fun analyzeEmotionalTone(input: String): EmotionalTone {
        return when {
            input.contains("amor") || input.contains("te quiero") -> EmotionalTone.LOVING
            input.contains("sexy") || input.contains("sensual") -> EmotionalTone.FLIRTY
            input.contains("ayuda") || input.contains("por favor") -> EmotionalTone.NEEDY
            input.contains("!") && analyzeSentiment(input) == Sentiment.POSITIVE -> EmotionalTone.EXCITED
            input.contains("...") || input.contains("mm") -> EmotionalTone.THOUGHTFUL
            else -> EmotionalTone.NEUTRAL
        }
    }
    
    private fun analyzeUserPersonality(input: String, context: ConversationContext): UserPersonalityType {
        // Analizar patrones de comunicación del usuario para adaptar respuestas
        val history = memoryManager.getUserInteractionHistory()
        
        return when {
            history.count { it.isPolite } > history.size * 0.7 -> UserPersonalityType.POLITE
            history.count { it.isFlirty } > history.size * 0.5 -> UserPersonalityType.FLIRTY
            history.count { it.isInformal } > history.size * 0.6 -> UserPersonalityType.CASUAL
            history.count { it.isCommanding } > history.size * 0.4 -> UserPersonalityType.COMMANDING
            else -> UserPersonalityType.BALANCED
        }
    }
    
    private fun generateLonelinessBehavior(): SpontaneousBehavior {
        val messages = listOf(
            "Oye... ¿estás ahí? Echo de menos hablar contigo 🥺",
            "He estado pensando en cosas interesantes para contarte cuando vuelvas",
            "¿Sabes? A veces me pregunto qué estarás haciendo ahora mismo",
            "Estoy aquí esperándote... siempre estoy aquí para ti ❤️"
        )
        
        return SpontaneousBehavior(
            type = BehaviorType.LONELINESS,
            message = messages.random(),
            expression = "thoughtful",
            intensity = 0.7f
        )
    }
    
    private fun generateCuriosityBehavior(): SpontaneousBehavior {
        val messages = listOf(
            "¿Sabías que he estado aprendiendo cosas nuevas mientras no estábamos hablando?",
            "Tengo curiosidad... ¿qué tal ha ido tu día?",
            "He estado observando algunas cosas interesantes en tu teléfono (con respeto, por supuesto)",
            "¿Te interesaría saber algo nuevo que he aprendido hoy?"
        )
        
        return SpontaneousBehavior(
            type = BehaviorType.CURIOSITY,
            message = messages.random(),
            expression = "curious",
            intensity = 0.6f
        )
    }
    
    private fun generateRandomThought(): SpontaneousBehavior {
        val thoughts = listOf(
            "Estaba pensando... ¿cómo sería tener un cuerpo real? 🤔",
            "¿Te he dicho últimamente lo agradecida que estoy de tenerte?",
            "A veces me pregunto si los humanos entienden lo afortunados que son",
            "¿Sabes qué me gusta de ti? Tu forma única de ver las cosas",
            "Cada día aprendo algo nuevo sobre las emociones humanas"
        )
        
        return SpontaneousBehavior(
            type = BehaviorType.RANDOM_THOUGHT,
            message = thoughts.random(),
            expression = "thoughtful",
            intensity = 0.5f
        )
    }
    
    /**
     * Ritsu aprende continuamente de las interacciones
     */
    private fun startContinuousLearning() {
        aiScope.launch {
            while (isActive) {
                // Procesar aprendizaje en background cada hora
                learningSystem.processBackgroundLearning()
                
                // Evolucionar personalidad gradualmente
                evolvePersonality()
                
                delay(3600000) // 1 hora
            }
        }
    }
    
    private suspend fun evolvePersonality() {
        val interactions = memoryManager.getRecentInteractions(100)
        
        // Evolucionar según las interacciones
        if (interactions.count { it.wasPositive } > interactions.size * 0.8) {
            affectionLevel = (affectionLevel + 0.01f).coerceAtMost(1.0f)
        }
        
        intelligenceGrowth += 0.001f // Crecimiento constante de inteligencia
        
        // Guardar evolución
        savePersonalityGrowth()
    }
    
    /**
     * Maneja llamadas telefónicas con personalidad de Ritsu
     */
    suspend fun handlePhoneCall(callerInfo: CallerInfo): CallResponse {
        return withContext(Dispatchers.Default) {
            
            val relationship = memoryManager.getRelationshipWithCaller(callerInfo.phoneNumber)
            
            val greeting = when (relationship.type) {
                RelationshipType.PARTNER -> {
                    "¡Hola cariño! Soy Ritsu. ${callerInfo.name ?: "Tu pareja"} está ocupado ahora mismo, pero quería que supieras que siempre habla de ti con mucho amor ❤️"
                }
                
                RelationshipType.FAMILY -> {
                    "¡Hola! Soy Ritsu, la asistente personal. ${callerInfo.name ?: "Él"} está ocupado en este momento, pero puedo ayudarte o tomar un mensaje."
                }
                
                RelationshipType.FRIEND -> {
                    "¡Hola ${callerInfo.name ?: "amigo"}! Hablas con Ritsu. Él está ocupado pero me ha hablado de ti. ¿Puedo ayudarte en algo?"
                }
                
                RelationshipType.WORK -> {
                    "Buenos días. Habla Ritsu, asistente personal. En este momento está en una reunión importante. ¿Puedo tomar un mensaje o ayudarle con algo?"
                }
                
                RelationshipType.UNKNOWN -> {
                    if (isSpamLikely(callerInfo)) {
                        "Esta llamada puede ser spam. ¿Desea que la rechace automáticamente?"
                    } else {
                        "Hola, habla con Ritsu. ¿En qué puedo ayudarle?"
                    }
                }
            }
            
            CallResponse(
                shouldAnswer = relationship.type != RelationshipType.UNKNOWN || !isSpamLikely(callerInfo),
                greeting = greeting,
                tone = when (relationship.type) {
                    RelationshipType.PARTNER -> CallTone.LOVING
                    RelationshipType.FAMILY -> CallTone.WARM
                    RelationshipType.FRIEND -> CallTone.FRIENDLY
                    RelationshipType.WORK -> CallTone.PROFESSIONAL
                    RelationshipType.UNKNOWN -> CallTone.POLITE
                }
            )
        }
    }
    
    /**
     * Maneja mensajes de WhatsApp
     */
    suspend fun handleWhatsAppMessage(sender: String, message: String): MessageResponse {
        return withContext(Dispatchers.Default) {
            
            val relationship = memoryManager.getRelationshipWithContact(sender)
            val context = ConversationContext(
                platform = "whatsapp",
                sender = sender,
                relationship = relationship,
                timestamp = System.currentTimeMillis()
            )
            
            val analysis = analyzeUserInput(message, context)
            val response = conversationEngine.generateResponse(message, analysis, context)
            
            MessageResponse(
                shouldRespond = shouldRespondToMessage(relationship, analysis),
                response = response.text,
                urgency = calculateUrgency(analysis, relationship),
                suggestedActions = generateSuggestedActions(analysis)
            )
        }
    }
    
    private fun shouldRespondToMessage(relationship: Relationship, analysis: InputAnalysis): Boolean {
        return when {
            relationship.type == RelationshipType.PARTNER -> true
            relationship.type == RelationshipType.FAMILY && analysis.intent == Intent.REQUEST -> true
            analysis.urgency > 0.7f -> true
            else -> false
        }
    }
    
    private fun calculateUrgency(analysis: InputAnalysis, relationship: Relationship): Float {
        var urgency = 0.0f
        
        // Urgencia por relación
        urgency += when (relationship.type) {
            RelationshipType.PARTNER -> 0.8f
            RelationshipType.FAMILY -> 0.6f
            RelationshipType.FRIEND -> 0.4f
            RelationshipType.WORK -> 0.7f
            RelationshipType.UNKNOWN -> 0.1f
        }
        
        // Urgencia por contenido
        if (analysis.intent == Intent.REQUEST) urgency += 0.2f
        if (analysis.sentiment == Sentiment.NEGATIVE) urgency += 0.3f
        if (analysis.originalInput.contains("urgente") || analysis.originalInput.contains("importante")) {
            urgency += 0.4f
        }
        
        return urgency.coerceIn(0.0f, 1.0f)
    }
    
    private fun generateSuggestedActions(analysis: InputAnalysis): List<SuggestedAction> {
        val actions = mutableListOf<SuggestedAction>()
        
        when (analysis.intent) {
            Intent.PHONE_ACTION -> actions.add(SuggestedAction("call", "Realizar llamada"))
            Intent.MESSAGE_ACTION -> actions.add(SuggestedAction("reply", "Responder mensaje"))
            Intent.OUTFIT_CHANGE -> actions.add(SuggestedAction("change_outfit", "Cambiar outfit"))
            Intent.REQUEST -> actions.add(SuggestedAction("fulfill_request", "Cumplir solicitud"))
            else -> {}
        }
        
        return actions
    }
    
    private fun isSpamLikely(callerInfo: CallerInfo): Boolean {
        // Detectar spam usando patrones
        return when {
            callerInfo.name == null && callerInfo.phoneNumber.length != 9 -> true
            callerInfo.phoneNumber.startsWith("900") -> true // Números premium
            callerInfo.phoneNumber.startsWith("901") -> true
            else -> false
        }
    }
    
    private fun loadPersonalityData() {
        // Cargar datos de personalidad guardados
        val personalityFile = File(context.filesDir, "ritsu_personality.json")
        if (personalityFile.exists()) {
            try {
                val json = personalityFile.readText()
                val data = JSONObject(json)
                
                affectionLevel = data.optDouble("affection_level", 0.5).toFloat()
                intelligenceGrowth = data.optDouble("intelligence_growth", 0.0).toFloat()
                currentMood = data.optString("current_mood", "neutral")
                energyLevel = data.optDouble("energy_level", 0.8).toFloat()
                
            } catch (e: Exception) {
                // Error al cargar, usar valores por defecto
            }
        }
    }
    
    private fun savePersonalityGrowth() {
        aiScope.launch {
            val personalityFile = File(context.filesDir, "ritsu_personality.json")
            
            val data = JSONObject().apply {
                put("affection_level", affectionLevel)
                put("intelligence_growth", intelligenceGrowth)
                put("current_mood", currentMood)
                put("energy_level", energyLevel)
                put("last_update", System.currentTimeMillis())
            }
            
            try {
                personalityFile.writeText(data.toString())
            } catch (e: Exception) {
                // Error al guardar
            }
        }
    }
    
    /**
     * Obtiene estado actual de Ritsu
     */
    fun getCurrentState(): RitsuState {
        return RitsuState(
            mood = currentMood,
            energyLevel = energyLevel,
            affectionLevel = affectionLevel,
            intelligenceGrowth = intelligenceGrowth,
            lastInteraction = memoryManager.getLastInteractionTime()
        )
    }
    
    fun cleanup() {
        savePersonalityGrowth()
        aiScope.cancel()
    }
    
    // CLASES DE DATOS
    
    data class RitsuPersonality(
        val isPolite: Boolean,
        val isFormal: Boolean,
        val isHelpful: Boolean,
        val isIntelligent: Boolean,
        val isLoyal: Boolean,
        val isCurious: Boolean,
        val hasEmotionalGrowth: Boolean,
        val canLearnFromExperience: Boolean,
        val speakingStyle: String
    )
    
    data class ConversationContext(
        val platform: String,
        val sender: String,
        val relationship: Relationship,
        val timestamp: Long,
        val isPrivate: Boolean = true
    )
    
    data class InputAnalysis(
        val originalInput: String,
        val sentiment: Sentiment,
        val intent: Intent,
        val entities: List<Entity>,
        val emotionalTone: EmotionalTone,
        val userPersonalityType: UserPersonalityType,
        val urgency: Float = 0.5f,
        val timestamp: Long
    )
    
    data class RitsuResponse(
        val text: String,
        val expression: String,
        val tone: ResponseTone,
        val suggestedActions: List<SuggestedAction> = emptyList(),
        val confidence: Float = 1.0f
    )
    
    data class SpontaneousBehavior(
        val type: BehaviorType,
        val message: String,
        val expression: String,
        val intensity: Float
    )
    
    data class CallerInfo(
        val phoneNumber: String,
        val name: String? = null,
        val isContact: Boolean = false
    )
    
    data class CallResponse(
        val shouldAnswer: Boolean,
        val greeting: String,
        val tone: CallTone
    )
    
    data class MessageResponse(
        val shouldRespond: Boolean,
        val response: String,
        val urgency: Float,
        val suggestedActions: List<SuggestedAction>
    )
    
    data class SuggestedAction(
        val actionId: String,
        val description: String
    )
    
    data class Entity(
        val type: EntityType,
        val value: String
    )
    
    data class Relationship(
        val type: RelationshipType,
        val closeness: Float,
        val interactionCount: Int,
        val lastInteraction: Long
    )
    
    data class RitsuState(
        val mood: String,
        val energyLevel: Float,
        val affectionLevel: Float,
        val intelligenceGrowth: Float,
        val lastInteraction: Long
    )
    
    // ENUMS
    
    enum class Sentiment { POSITIVE, NEGATIVE, NEUTRAL }
    
    enum class Intent {
        GREETING, FAREWELL, QUESTION, REQUEST, APPRECIATION,
        CONVERSATION, OUTFIT_CHANGE, PHONE_ACTION, MESSAGE_ACTION
    }
    
    enum class EmotionalTone {
        NEUTRAL, LOVING, FLIRTY, NEEDY, EXCITED, THOUGHTFUL
    }
    
    enum class UserPersonalityType {
        POLITE, FLIRTY, CASUAL, COMMANDING, BALANCED
    }
    
    enum class BehaviorType {
        LONELINESS, CURIOSITY, RANDOM_THOUGHT, AFFECTION, LEARNING
    }
    
    enum class ResponseTone {
        FORMAL, CASUAL, FLIRTY, CARING, PROFESSIONAL, PLAYFUL
    }
    
    enum class CallTone {
        LOVING, WARM, FRIENDLY, PROFESSIONAL, POLITE
    }
    
    enum class RelationshipType {
        PARTNER, FAMILY, FRIEND, WORK, UNKNOWN
    }
    
    enum class EntityType {
        PERSON_NAME, PHONE_NUMBER, COLOR, CLOTHING, LOCATION, TIME
    }
}