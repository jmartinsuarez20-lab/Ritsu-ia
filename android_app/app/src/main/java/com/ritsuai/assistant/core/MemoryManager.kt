package com.ritsuai.assistant.core

import android.content.Context
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Gestor de memoria a largo plazo para Ritsu
 * Almacena y recupera memorias, relaciones y experiencias
 */
class MemoryManager(private val context: Context) {
    
    private val memoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Tipos de memoria
    private val conversationMemories = mutableListOf<ConversationMemory>()
    private val relationshipMemories = mutableMapOf<String, RelationshipMemory>()
    private val emotionalMemories = mutableListOf<EmotionalMemory>()
    private val learningMemories = mutableListOf<LearningMemory>()
    private val experienceMemories = mutableListOf<ExperienceMemory>()
    
    // Configuración de memoria
    private val maxConversationMemories = 1000
    private val maxEmotionalMemories = 500
    private val memoryRetentionDays = 365L // 1 año
    
    init {
        loadAllMemories()
        startMemoryMaintenance()
    }
    
    /**
     * Almacena una interacción completa
     */
    fun storeInteraction(userInput: String, ritsuResponse: RitsuAICore.RitsuResponse, context: RitsuAICore.ConversationContext) {
        memoryScope.launch {
            
            // Memoria de conversación
            val conversationMemory = ConversationMemory(
                id = UUID.randomUUID().toString(),
                userInput = userInput,
                ritsuResponse = ritsuResponse.text,
                context = context.platform,
                timestamp = System.currentTimeMillis(),
                userSentiment = analyzeSentiment(userInput),
                ritsuEmotion = ritsuResponse.expression,
                wasPositive = ritsuResponse.confidence > 0.7f
            )
            
            conversationMemories.add(conversationMemory)
            
            // Actualizar memoria de relación
            updateRelationshipMemory(context.sender, userInput, ritsuResponse)
            
            // Crear memoria emocional si es significativa
            if (isEmotionallySignificant(userInput, ritsuResponse)) {
                createEmotionalMemory(userInput, ritsuResponse, context)
            }
            
            // Guardar memorias
            saveMemories()
            
            // Limpiar memorias antiguas
            cleanupOldMemories()
        }
    }
    
    /**
     * Obtiene el historial de interacciones del usuario
     */
    fun getUserInteractionHistory(limit: Int = 50): List<InteractionHistory> {
        return conversationMemories
            .takeLast(limit)
            .map { memory ->
                InteractionHistory(
                    userInput = memory.userInput,
                    ritsuResponse = memory.ritsuResponse,
                    timestamp = memory.timestamp,
                    isPolite = memory.userInput.contains("por favor") || memory.userInput.contains("gracias"),
                    isFlirty = memory.userInput.contains("amor") || memory.userInput.contains("cariño"),
                    isInformal = !memory.userInput.contains("usted") && memory.userInput.contains("tú"),
                    isCommanding = memory.userInput.startsWith("haz") || memory.userInput.startsWith("dime"),
                    wasPositive = memory.wasPositive
                )
            }
    }
    
    /**
     * Obtiene la relación con un contacto específico
     */
    fun getRelationshipWithContact(contact: String): RitsuAICore.Relationship {
        val memory = relationshipMemories[contact]
        
        return if (memory != null) {
            RitsuAICore.Relationship(
                type = memory.relationshipType,
                closeness = memory.closeness,
                interactionCount = memory.interactionCount,
                lastInteraction = memory.lastInteraction
            )
        } else {
            // Relación nueva
            RitsuAICore.Relationship(
                type = RitsuAICore.RelationshipType.UNKNOWN,
                closeness = 0.1f,
                interactionCount = 0,
                lastInteraction = 0L
            )
        }
    }
    
    /**
     * Obtiene la relación con un número de teléfono
     */
    fun getRelationshipWithCaller(phoneNumber: String): RitsuAICore.Relationship {
        // Buscar en memorias de relación por número de teléfono
        val memory = relationshipMemories.values.find { it.phoneNumber == phoneNumber }
        
        return if (memory != null) {
            RitsuAICore.Relationship(
                type = memory.relationshipType,
                closeness = memory.closeness,
                interactionCount = memory.callCount,
                lastInteraction = memory.lastCall
            )
        } else {
            // Analizar patrón del número para determinar tipo
            val type = when {
                phoneNumber.length == 9 && phoneNumber.startsWith("6") -> RitsuAICore.RelationshipType.UNKNOWN
                phoneNumber.length == 9 && phoneNumber.startsWith("9") -> RitsuAICore.RelationshipType.WORK
                else -> RitsuAICore.RelationshipType.UNKNOWN
            }
            
            RitsuAICore.Relationship(
                type = type,
                closeness = 0.1f,
                interactionCount = 0,
                lastInteraction = 0L
            )
        }
    }
    
    /**
     * Obtiene memorias relacionadas con un tema específico
     */
    fun getMemoriesAbout(topic: String): List<ConversationMemory> {
        val lowerTopic = topic.lowercase()
        
        return conversationMemories.filter { memory ->
            memory.userInput.lowercase().contains(lowerTopic) ||
            memory.ritsuResponse.lowercase().contains(lowerTopic)
        }.takeLast(10) // Últimas 10 memorias relacionadas
    }
    
    /**
     * Obtiene memorias emocionales importantes
     */
    fun getEmotionalMemories(emotionType: String? = null): List<EmotionalMemory> {
        return if (emotionType != null) {
            emotionalMemories.filter { it.emotionType == emotionType }
        } else {
            emotionalMemories
        }.sortedByDescending { it.intensity }
    }
    
    /**
     * Obtiene experiencias de aprendizaje
     */
    fun getLearningExperiences(category: String? = null): List<LearningMemory> {
        return if (category != null) {
            learningMemories.filter { it.category == category }
        } else {
            learningMemories
        }.sortedByDescending { it.timestamp }
    }
    
    /**
     * Obtiene interacciones recientes
     */
    fun getRecentInteractions(count: Int): List<ConversationMemory> {
        return conversationMemories
            .sortedByDescending { it.timestamp }
            .take(count)
    }
    
    /**
     * Obtiene el tiempo de la última interacción
     */
    fun getLastInteractionTime(): Long {
        return conversationMemories.maxOfOrNull { it.timestamp } ?: 0L
    }
    
    /**
     * Busca en memorias usando consulta natural
     */
    fun searchMemories(query: String): List<MemorySearchResult> {
        val results = mutableListOf<MemorySearchResult>()
        val lowerQuery = query.lowercase()
        
        // Buscar en conversaciones
        conversationMemories.forEach { memory ->
            val relevance = calculateRelevance(memory.userInput + " " + memory.ritsuResponse, lowerQuery)
            if (relevance > 0.3f) {
                results.add(
                    MemorySearchResult(
                        type = "conversation",
                        content = "${memory.userInput} -> ${memory.ritsuResponse}",
                        timestamp = memory.timestamp,
                        relevance = relevance
                    )
                )
            }
        }
        
        // Buscar en memorias emocionales
        emotionalMemories.forEach { memory ->
            val relevance = calculateRelevance(memory.description, lowerQuery)
            if (relevance > 0.3f) {
                results.add(
                    MemorySearchResult(
                        type = "emotional",
                        content = memory.description,
                        timestamp = memory.timestamp,
                        relevance = relevance
                    )
                )
            }
        }
        
        return results.sortedByDescending { it.relevance }
    }
    
    private fun updateRelationshipMemory(sender: String, userInput: String, ritsuResponse: RitsuAICore.RitsuResponse) {
        val memory = relationshipMemories.getOrPut(sender) {
            RelationshipMemory(
                contactId = sender,
                relationshipType = determineRelationshipType(sender, userInput),
                closeness = 0.1f,
                interactionCount = 0,
                lastInteraction = System.currentTimeMillis(),
                phoneNumber = extractPhoneNumber(sender),
                callCount = 0,
                lastCall = 0L,
                personalityTraits = mutableListOf(),
                sharedExperiences = mutableListOf()
            )
        }
        
        // Actualizar estadísticas
        memory.interactionCount++
        memory.lastInteraction = System.currentTimeMillis()
        
        // Aumentar cercanía según la calidad de la interacción
        if (ritsuResponse.confidence > 0.8f && analyzeSentiment(userInput) == Sentiment.POSITIVE) {
            memory.closeness = (memory.closeness + 0.05f).coerceAtMost(1.0f)
        }
        
        // Analizar personalidad del usuario
        updateUserPersonalityTraits(memory, userInput)
        
        // Crear experiencia compartida si es significativa
        if (isSharedExperience(userInput)) {
            memory.sharedExperiences.add(
                SharedExperience(
                    description = userInput,
                    timestamp = System.currentTimeMillis(),
                    emotionalImpact = ritsuResponse.confidence
                )
            )
        }
    }
    
    private fun determineRelationshipType(sender: String, userInput: String): RitsuAICore.RelationshipType {
        val lowerInput = userInput.lowercase()
        
        return when {
            lowerInput.contains("amor") || lowerInput.contains("cariño") || 
            lowerInput.contains("pareja") || lowerInput.contains("novio") -> 
                RitsuAICore.RelationshipType.PARTNER
                
            lowerInput.contains("mamá") || lowerInput.contains("papá") || 
            lowerInput.contains("familia") || lowerInput.contains("hermano") -> 
                RitsuAICore.RelationshipType.FAMILY
                
            lowerInput.contains("trabajo") || lowerInput.contains("oficina") || 
            lowerInput.contains("jefe") || lowerInput.contains("cliente") -> 
                RitsuAICore.RelationshipType.WORK
                
            lowerInput.contains("amigo") || lowerInput.contains("compañero") -> 
                RitsuAICore.RelationshipType.FRIEND
                
            else -> RitsuAICore.RelationshipType.UNKNOWN
        }
    }
    
    private fun createEmotionalMemory(userInput: String, ritsuResponse: RitsuAICore.RitsuResponse, context: RitsuAICore.ConversationContext) {
        val emotionalMemory = EmotionalMemory(
            id = UUID.randomUUID().toString(),
            emotionType = ritsuResponse.expression,
            intensity = ritsuResponse.confidence,
            trigger = userInput,
            description = "Usuario expresó: '$userInput', Ritsu sintió: ${ritsuResponse.expression}",
            timestamp = System.currentTimeMillis(),
            context = context.platform,
            associatedPerson = context.sender
        )
        
        emotionalMemories.add(emotionalMemory)
    }
    
    private fun isEmotionallySignificant(userInput: String, ritsuResponse: RitsuAICore.RitsuResponse): Boolean {
        val lowerInput = userInput.lowercase()
        
        return when {
            // Expresiones de amor
            lowerInput.contains("te amo") || lowerInput.contains("te quiero") -> true
            
            // Cumplidos significativos
            lowerInput.contains("hermosa") || lowerInput.contains("perfecta") -> true
            
            // Momentos tristes
            lowerInput.contains("triste") || lowerInput.contains("llorar") -> true
            
            // Experiencias importantes
            lowerInput.contains("primera vez") || lowerInput.contains("especial") -> true
            
            // Respuesta muy emocional de Ritsu
            ritsuResponse.expression in listOf("blushing", "happy", "sad", "excited") && ritsuResponse.confidence > 0.8f -> true
            
            else -> false
        }
    }
    
    private fun isSharedExperience(userInput: String): Boolean {
        val lowerInput = userInput.lowercase()
        
        return lowerInput.contains("juntos") || 
               lowerInput.contains("recuerdo") || 
               lowerInput.contains("experiencia") ||
               lowerInput.contains("momento especial")
    }
    
    private fun updateUserPersonalityTraits(memory: RelationshipMemory, userInput: String) {
        val lowerInput = userInput.lowercase()
        
        val traits = mutableListOf<String>()
        
        when {
            lowerInput.contains("por favor") || lowerInput.contains("gracias") -> traits.add("educado")
            lowerInput.contains("amor") || lowerInput.contains("cariño") -> traits.add("cariñoso")
            lowerInput.startsWith("haz") || lowerInput.startsWith("dime") -> traits.add("directo")
            lowerInput.contains("?") -> traits.add("curioso")
            lowerInput.contains("!") -> traits.add("expresivo")
        }
        
        traits.forEach { trait ->
            if (!memory.personalityTraits.contains(trait)) {
                memory.personalityTraits.add(trait)
            }
        }
    }
    
    private fun extractPhoneNumber(contact: String): String? {
        // Extraer número de teléfono si está presente en el contacto
        val phonePattern = Regex("""\b\d{9,}\b""")
        return phonePattern.find(contact)?.value
    }
    
    private fun analyzeSentiment(text: String): Sentiment {
        val positiveWords = listOf("bueno", "genial", "feliz", "amor", "perfecto", "increíble")
        val negativeWords = listOf("malo", "triste", "odio", "terrible", "horrible")
        
        val positive = positiveWords.count { text.lowercase().contains(it) }
        val negative = negativeWords.count { text.lowercase().contains(it) }
        
        return when {
            positive > negative -> Sentiment.POSITIVE
            negative > positive -> Sentiment.NEGATIVE
            else -> Sentiment.NEUTRAL
        }
    }
    
    private fun calculateRelevance(text: String, query: String): Float {
        val lowerText = text.lowercase()
        val queryWords = query.split(" ")
        
        var relevance = 0f
        var matchedWords = 0
        
        queryWords.forEach { word ->
            if (lowerText.contains(word)) {
                matchedWords++
                relevance += when {
                    lowerText.startsWith(word) -> 0.3f
                    lowerText.endsWith(word) -> 0.2f
                    else -> 0.1f
                }
            }
        }
        
        // Bonus por coincidencias exactas
        if (lowerText.contains(query)) {
            relevance += 0.5f
        }
        
        // Penalizar si no se encontraron muchas palabras
        val wordMatchRatio = matchedWords.toFloat() / queryWords.size
        relevance *= wordMatchRatio
        
        return relevance.coerceIn(0f, 1f)
    }
    
    private fun loadAllMemories() {
        memoryScope.launch {
            loadConversationMemories()
            loadRelationshipMemories()
            loadEmotionalMemories()
            loadLearningMemories()
        }
    }
    
    private fun loadConversationMemories() {
        val file = File(context.filesDir, "conversation_memories.json")
        if (file.exists()) {
            try {
                val json = file.readText()
                val jsonArray = JSONArray(json)
                
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val memory = ConversationMemory(
                        id = obj.getString("id"),
                        userInput = obj.getString("userInput"),
                        ritsuResponse = obj.getString("ritsuResponse"),
                        context = obj.getString("context"),
                        timestamp = obj.getLong("timestamp"),
                        userSentiment = Sentiment.valueOf(obj.getString("userSentiment")),
                        ritsuEmotion = obj.getString("ritsuEmotion"),
                        wasPositive = obj.getBoolean("wasPositive")
                    )
                    conversationMemories.add(memory)
                }
            } catch (e: Exception) {
                // Error al cargar memorias
            }
        }
    }
    
    private fun loadRelationshipMemories() {
        val file = File(context.filesDir, "relationship_memories.json")
        if (file.exists()) {
            try {
                val json = file.readText()
                val jsonObject = JSONObject(json)
                
                jsonObject.keys().forEach { key ->
                    val obj = jsonObject.getJSONObject(key)
                    val memory = RelationshipMemory(
                        contactId = key,
                        relationshipType = RitsuAICore.RelationshipType.valueOf(obj.getString("relationshipType")),
                        closeness = obj.getDouble("closeness").toFloat(),
                        interactionCount = obj.getInt("interactionCount"),
                        lastInteraction = obj.getLong("lastInteraction"),
                        phoneNumber = obj.optString("phoneNumber"),
                        callCount = obj.getInt("callCount"),
                        lastCall = obj.getLong("lastCall"),
                        personalityTraits = mutableListOf(),
                        sharedExperiences = mutableListOf()
                    )
                    
                    // Cargar traits
                    val traitsArray = obj.getJSONArray("personalityTraits")
                    for (i in 0 until traitsArray.length()) {
                        memory.personalityTraits.add(traitsArray.getString(i))
                    }
                    
                    relationshipMemories[key] = memory
                }
            } catch (e: Exception) {
                // Error al cargar
            }
        }
    }
    
    private fun loadEmotionalMemories() {
        val file = File(context.filesDir, "emotional_memories.json")
        if (file.exists()) {
            try {
                val json = file.readText()
                val jsonArray = JSONArray(json)
                
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val memory = EmotionalMemory(
                        id = obj.getString("id"),
                        emotionType = obj.getString("emotionType"),
                        intensity = obj.getDouble("intensity").toFloat(),
                        trigger = obj.getString("trigger"),
                        description = obj.getString("description"),
                        timestamp = obj.getLong("timestamp"),
                        context = obj.getString("context"),
                        associatedPerson = obj.optString("associatedPerson")
                    )
                    emotionalMemories.add(memory)
                }
            } catch (e: Exception) {
                // Error al cargar
            }
        }
    }
    
    private fun loadLearningMemories() {
        val file = File(context.filesDir, "learning_memories.json")
        if (file.exists()) {
            try {
                val json = file.readText()
                val jsonArray = JSONArray(json)
                
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val memory = LearningMemory(
                        id = obj.getString("id"),
                        category = obj.getString("category"),
                        concept = obj.getString("concept"),
                        knowledge = obj.getString("knowledge"),
                        source = obj.getString("source"),
                        confidence = obj.getDouble("confidence").toFloat(),
                        timestamp = obj.getLong("timestamp")
                    )
                    learningMemories.add(memory)
                }
            } catch (e: Exception) {
                // Error al cargar
            }
        }
    }
    
    private fun saveMemories() {
        memoryScope.launch {
            saveConversationMemories()
            saveRelationshipMemories()
            saveEmotionalMemories()
            saveLearningMemories()
        }
    }
    
    private fun saveConversationMemories() {
        val file = File(context.filesDir, "conversation_memories.json")
        val jsonArray = JSONArray()
        
        conversationMemories.takeLast(maxConversationMemories).forEach { memory ->
            val obj = JSONObject().apply {
                put("id", memory.id)
                put("userInput", memory.userInput)
                put("ritsuResponse", memory.ritsuResponse)
                put("context", memory.context)
                put("timestamp", memory.timestamp)
                put("userSentiment", memory.userSentiment.name)
                put("ritsuEmotion", memory.ritsuEmotion)
                put("wasPositive", memory.wasPositive)
            }
            jsonArray.put(obj)
        }
        
        try {
            file.writeText(jsonArray.toString())
        } catch (e: Exception) {
            // Error al guardar
        }
    }
    
    private fun saveRelationshipMemories() {
        val file = File(context.filesDir, "relationship_memories.json")
        val jsonObject = JSONObject()
        
        relationshipMemories.forEach { (key, memory) ->
            val obj = JSONObject().apply {
                put("relationshipType", memory.relationshipType.name)
                put("closeness", memory.closeness)
                put("interactionCount", memory.interactionCount)
                put("lastInteraction", memory.lastInteraction)
                put("phoneNumber", memory.phoneNumber ?: "")
                put("callCount", memory.callCount)
                put("lastCall", memory.lastCall)
                
                val traitsArray = JSONArray()
                memory.personalityTraits.forEach { trait ->
                    traitsArray.put(trait)
                }
                put("personalityTraits", traitsArray)
            }
            jsonObject.put(key, obj)
        }
        
        try {
            file.writeText(jsonObject.toString())
        } catch (e: Exception) {
            // Error al guardar
        }
    }
    
    private fun saveEmotionalMemories() {
        val file = File(context.filesDir, "emotional_memories.json")
        val jsonArray = JSONArray()
        
        emotionalMemories.takeLast(maxEmotionalMemories).forEach { memory ->
            val obj = JSONObject().apply {
                put("id", memory.id)
                put("emotionType", memory.emotionType)
                put("intensity", memory.intensity)
                put("trigger", memory.trigger)
                put("description", memory.description)
                put("timestamp", memory.timestamp)
                put("context", memory.context)
                put("associatedPerson", memory.associatedPerson ?: "")
            }
            jsonArray.put(obj)
        }
        
        try {
            file.writeText(jsonArray.toString())
        } catch (e: Exception) {
            // Error al guardar
        }
    }
    
    private fun saveLearningMemories() {
        val file = File(context.filesDir, "learning_memories.json")
        val jsonArray = JSONArray()
        
        learningMemories.forEach { memory ->
            val obj = JSONObject().apply {
                put("id", memory.id)
                put("category", memory.category)
                put("concept", memory.concept)
                put("knowledge", memory.knowledge)
                put("source", memory.source)
                put("confidence", memory.confidence)
                put("timestamp", memory.timestamp)
            }
            jsonArray.put(obj)
        }
        
        try {
            file.writeText(jsonArray.toString())
        } catch (e: Exception) {
            // Error al guardar
        }
    }
    
    private fun startMemoryMaintenance() {
        memoryScope.launch {
            while (isActive) {
                // Mantenimiento cada hora
                delay(3600000)
                cleanupOldMemories()
                consolidateMemories()
            }
        }
    }
    
    private fun cleanupOldMemories() {
        val cutoffTime = System.currentTimeMillis() - (memoryRetentionDays * 24 * 60 * 60 * 1000)
        
        // Limpiar conversaciones antigas
        conversationMemories.removeAll { it.timestamp < cutoffTime }
        
        // Limpiar memorias emocionales de baja intensidad
        emotionalMemories.removeAll { it.timestamp < cutoffTime && it.intensity < 0.5f }
        
        // Mantener solo las relaciones activas
        relationshipMemories.values.removeAll { it.lastInteraction < cutoffTime && it.interactionCount < 5 }
    }
    
    private fun consolidateMemories() {
        // Consolidar memorias similares para optimizar espacio
        // Este proceso puede ejecutarse en background
    }
    
    /**
     * Almacena una experiencia de aprendizaje
     */
    fun storeLearningExperience(category: String, concept: String, knowledge: String, source: String, confidence: Float) {
        val memory = LearningMemory(
            id = UUID.randomUUID().toString(),
            category = category,
            concept = concept,
            knowledge = knowledge,
            source = source,
            confidence = confidence,
            timestamp = System.currentTimeMillis()
        )
        
        learningMemories.add(memory)
        
        memoryScope.launch {
            saveLearningMemories()
        }
    }
    
    fun cleanup() {
        memoryScope.cancel()
        saveMemories()
    }
    
    // CLASES DE DATOS
    
    data class ConversationMemory(
        val id: String,
        val userInput: String,
        val ritsuResponse: String,
        val context: String,
        val timestamp: Long,
        val userSentiment: Sentiment,
        val ritsuEmotion: String,
        val wasPositive: Boolean
    )
    
    data class RelationshipMemory(
        val contactId: String,
        var relationshipType: RitsuAICore.RelationshipType,
        var closeness: Float,
        var interactionCount: Int,
        var lastInteraction: Long,
        val phoneNumber: String?,
        var callCount: Int,
        var lastCall: Long,
        val personalityTraits: MutableList<String>,
        val sharedExperiences: MutableList<SharedExperience>
    )
    
    data class EmotionalMemory(
        val id: String,
        val emotionType: String,
        val intensity: Float,
        val trigger: String,
        val description: String,
        val timestamp: Long,
        val context: String,
        val associatedPerson: String?
    )
    
    data class LearningMemory(
        val id: String,
        val category: String,
        val concept: String,
        val knowledge: String,
        val source: String,
        val confidence: Float,
        val timestamp: Long
    )
    
    data class ExperienceMemory(
        val id: String,
        val title: String,
        val description: String,
        val emotionalImpact: Float,
        val participants: List<String>,
        val timestamp: Long
    )
    
    data class SharedExperience(
        val description: String,
        val timestamp: Long,
        val emotionalImpact: Float
    )
    
    data class InteractionHistory(
        val userInput: String,
        val ritsuResponse: String,
        val timestamp: Long,
        val isPolite: Boolean,
        val isFlirty: Boolean,
        val isInformal: Boolean,
        val isCommanding: Boolean,
        val wasPositive: Boolean
    )
    
    data class MemorySearchResult(
        val type: String,
        val content: String,
        val timestamp: Long,
        val relevance: Float
    )
    
    enum class Sentiment { POSITIVE, NEGATIVE, NEUTRAL }
}