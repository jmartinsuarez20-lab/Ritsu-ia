package com.ritsuai.assistant.core

import android.content.Context
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*

/**
 * Sistema de aprendizaje avanzado de Ritsu
 * Implementa autoaprendizaje y mejora continua de respuestas
 */
class LearningSystem(private val context: Context) {
    
    private val learningScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // Base de conocimientos aprendida
    private val learnedKnowledge = mutableMapOf<String, KnowledgeItem>()
    private val learningPatterns = mutableListOf<LearningPattern>()
    private val behaviorAdaptations = mutableMapOf<String, BehaviorAdaptation>()
    
    // Métricas de aprendizaje
    private var totalInteractions = 0
    private var successfulResponses = 0
    private var learningAccuracy = 0.7f
    
    // Categorías de aprendizaje
    private val learningCategories = mapOf(
        "user_preferences" to UserPreferenceLearning(),
        "conversation_patterns" to ConversationPatternLearning(),
        "emotional_responses" to EmotionalResponseLearning(),
        "task_optimization" to TaskOptimizationLearning(),
        "relationship_dynamics" to RelationshipLearning()
    )
    
    init {
        loadLearnedKnowledge()
        startContinuousLearning()
    }
    
    /**
     * Aprende de una interacción específica
     */
    fun learnFromInteraction(
        userInput: String,
        context: RitsuAICore.ConversationContext,
        analysis: RitsuAICore.InputAnalysis
    ) {
        totalInteractions++
        
        learningScope.launch {
            // Analizar patrones en la interacción
            val patterns = extractLearningPatterns(userInput, context, analysis)
            
            // Aplicar aprendizaje por categorías
            learningCategories.forEach { (category, learningModule) ->
                learningModule.processInteraction(userInput, context, analysis, patterns)
            }
            
            // Almacenar patrones nuevos
            patterns.forEach { pattern ->
                if (isNewPattern(pattern)) {
                    learningPatterns.add(pattern)
                    processNewLearning(pattern)
                }
            }
            
            // Actualizar métricas
            updateLearningMetrics(analysis)
            
            // Guardar conocimiento
            saveLearnedKnowledge()
        }
    }
    
    /**
     * Procesamiento de aprendizaje en background
     */
    suspend fun processBackgroundLearning() {
        learningScope.launch {
            // Consolidar aprendizajes similares
            consolidateLearning()
            
            // Optimizar patrones de respuesta
            optimizeResponsePatterns()
            
            // Evolucionar comportamientos
            evolveBehaviors()
            
            // Limpiar conocimiento obsoleto
            cleanupObsoleteKnowledge()
            
            // Actualizar precisión de aprendizaje
            calculateLearningAccuracy()
        }
    }
    
    private fun extractLearningPatterns(
        userInput: String,
        context: RitsuAICore.ConversationContext,
        analysis: RitsuAICore.InputAnalysis
    ): List<LearningPattern> {
        val patterns = mutableListOf<LearningPattern>()
        
        // Patrón de preferencia temporal
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        patterns.add(
            LearningPattern(
                type = PatternType.TEMPORAL_PREFERENCE,
                trigger = "hour_$hour",
                context = userInput,
                outcome = analysis.sentiment.toString(),
                confidence = 0.6f,
                timestamp = System.currentTimeMillis()
            )
        )
        
        // Patrón de comunicación
        if (userInput.length > 20) {
            val communicationStyle = when {
                userInput.contains("por favor") -> "polite"
                userInput.startsWith("haz") -> "direct"
                userInput.contains("?") -> "inquisitive"
                else -> "casual"
            }
            
            patterns.add(
                LearningPattern(
                    type = PatternType.COMMUNICATION_STYLE,
                    trigger = communicationStyle,
                    context = context.relationship.type.toString(),
                    outcome = "preferred",
                    confidence = 0.8f,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        
        // Patrón emocional
        if (analysis.emotionalTone != RitsuAICore.EmotionalTone.NEUTRAL) {
            patterns.add(
                LearningPattern(
                    type = PatternType.EMOTIONAL_TRIGGER,
                    trigger = analysis.emotionalTone.toString(),
                    context = analysis.originalInput.take(50),
                    outcome = "emotion_detected",
                    confidence = 0.9f,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        
        // Patrón de tema
        val keywords = extractTopicKeywords(userInput)
        if (keywords.isNotEmpty()) {
            patterns.add(
                LearningPattern(
                    type = PatternType.TOPIC_INTEREST,
                    trigger = keywords.joinToString(","),
                    context = context.platform,
                    outcome = "topic_discussed",
                    confidence = 0.7f,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        
        return patterns
    }
    
    private fun extractTopicKeywords(input: String): List<String> {
        val stopWords = setOf("el", "la", "de", "que", "y", "a", "en", "un", "es", "se", "no", "te", "lo", "le", "da", "su", "por", "son", "con", "para", "pero", "como", "las", "me", "una", "si", "ya", "todo", "esta", "muy", "bien", "puede", "hacer", "más", "aquí", "hasta")
        
        return input.lowercase()
            .split(Regex("[^a-záéíóúñü]+"))
            .filter { word -> 
                word.length > 3 && 
                !stopWords.contains(word) &&
                word.matches(Regex("[a-záéíóúñü]+"))
            }
            .take(5) // Top 5 keywords
    }
    
    private fun isNewPattern(pattern: LearningPattern): Boolean {
        return learningPatterns.none { existingPattern ->
            existingPattern.type == pattern.type &&
            existingPattern.trigger == pattern.trigger &&
            existingPattern.context == pattern.context
        }
    }
    
    private fun processNewLearning(pattern: LearningPattern) {
        when (pattern.type) {
            PatternType.USER_PREFERENCE -> {
                learnUserPreference(pattern)
            }
            
            PatternType.COMMUNICATION_STYLE -> {
                adaptCommunicationStyle(pattern)
            }
            
            PatternType.EMOTIONAL_TRIGGER -> {
                improveEmotionalResponse(pattern)
            }
            
            PatternType.TOPIC_INTEREST -> {
                enhanceTopicKnowledge(pattern)
            }
            
            PatternType.TEMPORAL_PREFERENCE -> {
                learnTemporalBehavior(pattern)
            }
            
            PatternType.RELATIONSHIP_DYNAMIC -> {
                refineRelationshipUnderstanding(pattern)
            }
        }
    }
    
    private fun learnUserPreference(pattern: LearningPattern) {
        val preferenceKey = "user_pref_${pattern.trigger}"
        val existing = learnedKnowledge[preferenceKey]
        
        if (existing != null) {
            // Reforzar conocimiento existente
            existing.confidence = (existing.confidence + pattern.confidence) / 2f
            existing.reinforcements++
        } else {
            // Nuevo conocimiento
            learnedKnowledge[preferenceKey] = KnowledgeItem(
                category = "user_preferences",
                key = preferenceKey,
                value = pattern.context,
                confidence = pattern.confidence,
                timestamp = pattern.timestamp,
                reinforcements = 1
            )
        }
    }
    
    private fun adaptCommunicationStyle(pattern: LearningPattern) {
        val adaptationKey = "comm_style_${pattern.context}"
        val existing = behaviorAdaptations[adaptationKey]
        
        if (existing != null) {
            existing.effectivenessScore += 0.1f
            existing.usageCount++
        } else {
            behaviorAdaptations[adaptationKey] = BehaviorAdaptation(
                context = pattern.context,
                adaptation = pattern.trigger,
                effectivenessScore = pattern.confidence,
                usageCount = 1,
                lastUsed = pattern.timestamp
            )
        }
    }
    
    private fun improveEmotionalResponse(pattern: LearningPattern) {
        val emotionKey = "emotion_${pattern.trigger}"
        val knowledge = learnedKnowledge.getOrPut(emotionKey) {
            KnowledgeItem(
                category = "emotional_responses",
                key = emotionKey,
                value = pattern.context,
                confidence = pattern.confidence,
                timestamp = pattern.timestamp,
                reinforcements = 0
            )
        }
        
        knowledge.reinforcements++
        knowledge.confidence = (knowledge.confidence + pattern.confidence) / 2f
    }
    
    private fun enhanceTopicKnowledge(pattern: LearningPattern) {
        val topics = pattern.trigger.split(",")
        
        topics.forEach { topic ->
            val topicKey = "topic_$topic"
            val existing = learnedKnowledge[topicKey]
            
            if (existing != null) {
                existing.reinforcements++
                existing.confidence = minOf(existing.confidence + 0.05f, 1.0f)
            } else {
                learnedKnowledge[topicKey] = KnowledgeItem(
                    category = "topics",
                    key = topicKey,
                    value = pattern.context,
                    confidence = pattern.confidence,
                    timestamp = pattern.timestamp,
                    reinforcements = 1
                )
            }
        }
    }
    
    private fun learnTemporalBehavior(pattern: LearningPattern) {
        val timeKey = "temporal_${pattern.trigger}"
        val existing = learnedKnowledge[timeKey]
        
        if (existing != null) {
            // Actualizar comportamiento temporal
            existing.value = "${existing.value},${pattern.outcome}"
            existing.reinforcements++
        } else {
            learnedKnowledge[timeKey] = KnowledgeItem(
                category = "temporal_behavior",
                key = timeKey,
                value = pattern.outcome,
                confidence = pattern.confidence,
                timestamp = pattern.timestamp,
                reinforcements = 1
            )
        }
    }
    
    private fun refineRelationshipUnderstanding(pattern: LearningPattern) {
        val relationKey = "relationship_${pattern.context}"
        val existing = learnedKnowledge[relationKey]
        
        if (existing != null) {
            existing.value = pattern.trigger // Actualizar comprensión
            existing.confidence = (existing.confidence + pattern.confidence) / 2f
            existing.reinforcements++
        } else {
            learnedKnowledge[relationKey] = KnowledgeItem(
                category = "relationships",
                key = relationKey,
                value = pattern.trigger,
                confidence = pattern.confidence,
                timestamp = pattern.timestamp,
                reinforcements = 1
            )
        }
    }
    
    private fun updateLearningMetrics(analysis: RitsuAICore.InputAnalysis) {
        // Considerar respuesta exitosa si el sentimiento es positivo o neutral
        if (analysis.sentiment != RitsuAICore.Sentiment.NEGATIVE) {
            successfulResponses++
        }
        
        // Calcular accuracy general
        if (totalInteractions > 0) {
            learningAccuracy = successfulResponses.toFloat() / totalInteractions.toFloat()
        }
    }
    
    private fun consolidateLearning() {
        // Consolidar conocimientos similares
        val groupedKnowledge = learnedKnowledge.values.groupBy { "${it.category}_${it.key.take(10)}" }
        
        groupedKnowledge.forEach { (_, items) ->
            if (items.size > 1) {
                // Combinar conocimientos similares
                val consolidated = consolidateKnowledgeItems(items)
                
                // Remover items originales y agregar consolidado
                items.forEach { learnedKnowledge.remove(it.key) }
                learnedKnowledge[consolidated.key] = consolidated
            }
        }
    }
    
    private fun consolidateKnowledgeItems(items: List<KnowledgeItem>): KnowledgeItem {
        val first = items.first()
        val avgConfidence = items.map { it.confidence }.average().toFloat()
        val totalReinforcements = items.sumOf { it.reinforcements }
        val combinedValue = items.joinToString("|") { it.value }
        
        return KnowledgeItem(
            category = first.category,
            key = first.key + "_consolidated",
            value = combinedValue,
            confidence = avgConfidence,
            timestamp = System.currentTimeMillis(),
            reinforcements = totalReinforcements
        )
    }
    
    private fun optimizeResponsePatterns() {
        // Identificar patrones de respuesta más efectivos
        val effectivePatterns = learningPatterns.filter { it.confidence > 0.8f }
        
        effectivePatterns.groupBy { it.type }.forEach { (type, patterns) ->
            val bestPattern = patterns.maxByOrNull { it.confidence }
            bestPattern?.let { pattern ->
                // Promover este patrón como preferido
                val optimizationKey = "optimized_${type}_${pattern.trigger}"
                learnedKnowledge[optimizationKey] = KnowledgeItem(
                    category = "optimizations",
                    key = optimizationKey,
                    value = pattern.context,
                    confidence = pattern.confidence + 0.1f,
                    timestamp = System.currentTimeMillis(),
                    reinforcements = 1
                )
            }
        }
    }
    
    private fun evolveBehaviors() {
        behaviorAdaptations.values.forEach { adaptation ->
            // Evolucionar comportamientos basados en efectividad
            if (adaptation.effectivenessScore > 0.8f && adaptation.usageCount > 5) {
                adaptation.effectivenessScore = minOf(adaptation.effectivenessScore + 0.02f, 1.0f)
            } else if (adaptation.effectivenessScore < 0.3f) {
                adaptation.effectivenessScore = maxOf(adaptation.effectivenessScore - 0.01f, 0.1f)
            }
        }
    }
    
    private fun cleanupObsoleteKnowledge() {
        val cutoffTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000) // 7 días
        
        // Remover conocimiento muy antiguo con baja confianza
        val toRemove = learnedKnowledge.filter { (_, knowledge) ->
            knowledge.timestamp < cutoffTime && 
            knowledge.confidence < 0.3f && 
            knowledge.reinforcements < 2
        }
        
        toRemove.forEach { (key, _) ->
            learnedKnowledge.remove(key)
        }
        
        // Limpiar patrones antiguos
        learningPatterns.removeAll { it.timestamp < cutoffTime }
    }
    
    private fun calculateLearningAccuracy() {
        if (totalInteractions > 100) {
            // Recalcular accuracy basada en interacciones recientes
            val recentPatterns = learningPatterns.takeLast(50)
            val successfulPatterns = recentPatterns.count { it.confidence > 0.7f }
            
            if (recentPatterns.isNotEmpty()) {
                learningAccuracy = successfulPatterns.toFloat() / recentPatterns.size.toFloat()
            }
        }
    }
    
    /**
     * Obtiene conocimiento aprendido sobre un tema específico
     */
    fun getLearnedKnowledge(category: String): List<KnowledgeItem> {
        return learnedKnowledge.values.filter { it.category == category }
    }
    
    /**
     * Obtiene adaptaciones de comportamiento
     */
    fun getBehaviorAdaptations(context: String): BehaviorAdaptation? {
        return behaviorAdaptations.values.find { it.context == context && it.effectivenessScore > 0.6f }
    }
    
    /**
     * Obtiene estadísticas de aprendizaje
     */
    fun getLearningStats(): LearningStats {
        return LearningStats(
            totalInteractions = totalInteractions,
            successfulResponses = successfulResponses,
            learningAccuracy = learningAccuracy,
            knowledgeItems = learnedKnowledge.size,
            learningPatterns = learningPatterns.size,
            behaviorAdaptations = behaviorAdaptations.size
        )
    }
    
    /**
     * Sugiere mejoras basadas en aprendizaje
     */
    fun suggestImprovements(): List<ImprovementSuggestion> {
        val suggestions = mutableListOf<ImprovementSuggestion>()
        
        // Analizar áreas con baja efectividad
        val lowEffectivenessBehaviors = behaviorAdaptations.values.filter { it.effectivenessScore < 0.5f }
        
        lowEffectivenessBehaviors.forEach { behavior ->
            suggestions.add(
                ImprovementSuggestion(
                    area = "communication",
                    description = "Mejorar respuestas en contexto: ${behavior.context}",
                    priority = ImprovementPriority.MEDIUM,
                    confidence = 0.8f
                )
            )
        }
        
        // Sugerir nuevas áreas de aprendizaje
        if (learnedKnowledge.count { it.value.category == "topics" } < 10) {
            suggestions.add(
                ImprovementSuggestion(
                    area = "knowledge",
                    description = "Expandir conocimiento sobre temas de interés del usuario",
                    priority = ImprovementPriority.HIGH,
                    confidence = 0.9f
                )
            )
        }
        
        return suggestions
    }
    
    private fun loadLearnedKnowledge() {
        learningScope.launch {
            try {
                val knowledgeFile = File(context.filesDir, "learned_knowledge.json")
                if (knowledgeFile.exists()) {
                    val jsonContent = knowledgeFile.readText()
                    val jsonArray = JSONArray(jsonContent)
                    
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val knowledge = KnowledgeItem(
                            category = obj.getString("category"),
                            key = obj.getString("key"),
                            value = obj.getString("value"),
                            confidence = obj.getDouble("confidence").toFloat(),
                            timestamp = obj.getLong("timestamp"),
                            reinforcements = obj.getInt("reinforcements")
                        )
                        learnedKnowledge[knowledge.key] = knowledge
                    }
                }
                
                // Cargar adaptaciones de comportamiento
                val behaviorFile = File(context.filesDir, "behavior_adaptations.json")
                if (behaviorFile.exists()) {
                    val jsonContent = behaviorFile.readText()
                    val jsonObject = JSONObject(jsonContent)
                    
                    jsonObject.keys().forEach { key ->
                        val obj = jsonObject.getJSONObject(key)
                        val adaptation = BehaviorAdaptation(
                            context = obj.getString("context"),
                            adaptation = obj.getString("adaptation"),
                            effectivenessScore = obj.getDouble("effectivenessScore").toFloat(),
                            usageCount = obj.getInt("usageCount"),
                            lastUsed = obj.getLong("lastUsed")
                        )
                        behaviorAdaptations[key] = adaptation
                    }
                }
                
            } catch (e: Exception) {
                // Error al cargar conocimiento
            }
        }
    }
    
    private fun saveLearnedKnowledge() {
        learningScope.launch {
            try {
                // Guardar conocimiento aprendido
                val knowledgeFile = File(context.filesDir, "learned_knowledge.json")
                val jsonArray = JSONArray()
                
                learnedKnowledge.values.forEach { knowledge ->
                    val obj = JSONObject().apply {
                        put("category", knowledge.category)
                        put("key", knowledge.key)
                        put("value", knowledge.value)
                        put("confidence", knowledge.confidence)
                        put("timestamp", knowledge.timestamp)
                        put("reinforcements", knowledge.reinforcements)
                    }
                    jsonArray.put(obj)
                }
                
                knowledgeFile.writeText(jsonArray.toString())
                
                // Guardar adaptaciones de comportamiento
                val behaviorFile = File(context.filesDir, "behavior_adaptations.json")
                val behaviorJson = JSONObject()
                
                behaviorAdaptations.forEach { (key, adaptation) ->
                    val obj = JSONObject().apply {
                        put("context", adaptation.context)
                        put("adaptation", adaptation.adaptation)
                        put("effectivenessScore", adaptation.effectivenessScore)
                        put("usageCount", adaptation.usageCount)
                        put("lastUsed", adaptation.lastUsed)
                    }
                    behaviorJson.put(key, obj)
                }
                
                behaviorFile.writeText(behaviorJson.toString())
                
            } catch (e: Exception) {
                // Error al guardar
            }
        }
    }
    
    private fun startContinuousLearning() {
        learningScope.launch {
            while (isActive) {
                delay(3600000) // Cada hora
                processBackgroundLearning()
            }
        }
    }
    
    fun cleanup() {
        saveLearnedKnowledge()
        learningScope.cancel()
    }
    
    // CLASES DE DATOS
    
    data class KnowledgeItem(
        val category: String,
        val key: String,
        var value: String,
        var confidence: Float,
        val timestamp: Long,
        var reinforcements: Int
    )
    
    data class LearningPattern(
        val type: PatternType,
        val trigger: String,
        val context: String,
        val outcome: String,
        val confidence: Float,
        val timestamp: Long
    )
    
    data class BehaviorAdaptation(
        val context: String,
        val adaptation: String,
        var effectivenessScore: Float,
        var usageCount: Int,
        var lastUsed: Long
    )
    
    data class LearningStats(
        val totalInteractions: Int,
        val successfulResponses: Int,
        val learningAccuracy: Float,
        val knowledgeItems: Int,
        val learningPatterns: Int,
        val behaviorAdaptations: Int
    )
    
    data class ImprovementSuggestion(
        val area: String,
        val description: String,
        val priority: ImprovementPriority,
        val confidence: Float
    )
    
    enum class PatternType {
        USER_PREFERENCE,
        COMMUNICATION_STYLE,
        EMOTIONAL_TRIGGER,
        TOPIC_INTEREST,
        TEMPORAL_PREFERENCE,
        RELATIONSHIP_DYNAMIC
    }
    
    enum class ImprovementPriority {
        HIGH, MEDIUM, LOW
    }
}

// MÓDULOS DE APRENDIZAJE ESPECIALIZADOS

/**
 * Módulo base para aprendizaje especializado
 */
abstract class SpecializedLearningModule {
    abstract fun processInteraction(
        userInput: String,
        context: RitsuAICore.ConversationContext,
        analysis: RitsuAICore.InputAnalysis,
        patterns: List<LearningSystem.LearningPattern>
    )
}

/**
 * Aprendizaje de preferencias del usuario
 */
class UserPreferenceLearning : SpecializedLearningModule() {
    override fun processInteraction(
        userInput: String,
        context: RitsuAICore.ConversationContext,
        analysis: RitsuAICore.InputAnalysis,
        patterns: List<LearningSystem.LearningPattern>
    ) {
        // Implementar aprendizaje específico de preferencias
    }
}

/**
 * Aprendizaje de patrones de conversación
 */
class ConversationPatternLearning : SpecializedLearningModule() {
    override fun processInteraction(
        userInput: String,
        context: RitsuAICore.ConversationContext,
        analysis: RitsuAICore.InputAnalysis,
        patterns: List<LearningSystem.LearningPattern>
    ) {
        // Implementar aprendizaje de patrones conversacionales
    }
}

/**
 * Aprendizaje de respuestas emocionales
 */
class EmotionalResponseLearning : SpecializedLearningModule() {
    override fun processInteraction(
        userInput: String,
        context: RitsuAICore.ConversationContext,
        analysis: RitsuAICore.InputAnalysis,
        patterns: List<LearningSystem.LearningPattern>
    ) {
        // Implementar mejora de respuestas emocionales
    }
}

/**
 * Optimización de tareas
 */
class TaskOptimizationLearning : SpecializedLearningModule() {
    override fun processInteraction(
        userInput: String,
        context: RitsuAICore.ConversationContext,
        analysis: RitsuAICore.InputAnalysis,
        patterns: List<LearningSystem.LearningPattern>
    ) {
        // Implementar optimización de tareas
    }
}

/**
 * Aprendizaje de dinámicas relacionales
 */
class RelationshipLearning : SpecializedLearningModule() {
    override fun processInteraction(
        userInput: String,
        context: RitsuAICore.ConversationContext,
        analysis: RitsuAICore.InputAnalysis,
        patterns: List<LearningSystem.LearningPattern>
    ) {
        // Implementar comprensión de relaciones
    }
}