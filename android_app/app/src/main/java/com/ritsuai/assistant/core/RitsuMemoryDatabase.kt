package com.ritsuai.assistant.core

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.mutableMapOf

class RitsuMemoryDatabase(private val context: Context) {
    
    // Base de datos en memoria (en una implementación real usaría Room/SQLite)
    private val interactions = mutableListOf<EmotionalInteraction>()
    private val contacts = mutableMapOf<String, ContactInfo>()
    private val conversationContexts = mutableMapOf<String, MutableList<String>>()
    private val personalPreferences = mutableMapOf<String, Any>()
    private val emotionalBonds = mutableMapOf<String, EmotionalBond>()
    private val userPersonality = mutableMapOf<String, Any>()
    
    data class EmotionalInteraction(
        val id: String = UUID.randomUUID().toString(),
        val timestamp: Long = System.currentTimeMillis(),
        val message: String,
        val response: String? = null,
        val caller: String? = null,
        val type: InteractionType,
        val userEmotion: String = "neutral",
        val ritsuMood: String = "neutral",
        val intimacyLevel: Int = 1 // 1-10
    )
    
    data class ContactInfo(
        val number: String,
        val name: String?,
        val lastInteraction: Long,
        val interactionCount: Int,
        val relationship: String = "acquaintance", // acquaintance, familiar, close, best_friend
        val averageEmotionalTone: String = "neutral",
        val favoriteTopics: MutableList<String> = mutableListOf(),
        val personalNotes: MutableList<String> = mutableListOf()
    )
    
    data class EmotionalBond(
        val contactId: String,
        val trustLevel: Int = 50, // 0-100
        val affectionLevel: Int = 50, // 0-100
        val comfortLevel: Int = 50, // 0-100
        val sharedExperiences: MutableList<String> = mutableListOf(),
        val emotionalHistory: MutableList<String> = mutableListOf()
    )
    
    enum class InteractionType {
        CHAT, PHONE_CALL, WHATSAPP, SYSTEM, SPONTANEOUS
    }
    
    fun saveEmotionalInteraction(
        message: String, 
        caller: String? = null, 
        userEmotion: String = "neutral",
        ritsuMood: String = "neutral",
        response: String? = null,
        type: InteractionType = InteractionType.CHAT
    ) {
        val intimacy = calculateIntimacyLevel(message, caller)
        
        val interaction = EmotionalInteraction(
            message = message,
            response = response,
            caller = caller,
            type = type,
            userEmotion = userEmotion,
            ritsuMood = ritsuMood,
            intimacyLevel = intimacy
        )
        
        interactions.add(interaction)
        
        // Mantener solo las últimas 2000 interacciones para optimizar memoria
        if (interactions.size > 2000) {
            interactions.removeAt(0)
        }
        
        // Actualizar contexto emocional de conversación
        updateEmotionalContext(caller, message, userEmotion)
        
        // Actualizar información de contacto con contexto emocional
        updateContactInfoWithEmotion(caller, userEmotion)
        
        // Aprender sobre la personalidad del usuario
        learnUserPersonality(message, userEmotion, caller)
    }
    
    private fun calculateIntimacyLevel(message: String, caller: String?): Int {
        var intimacy = 1
        
        // Factores que indican intimidad
        val intimateWords = listOf(
            "siento", "me hace", "personal", "privado", "confianza", 
            "secret", "importante", "familia", "amor", "miedo",
            "sueño", "espero", "deseo", "preocupa", "feliz"
        )
        
        intimateWords.forEach { word ->
            if (message.contains(word, ignoreCase = true)) {
                intimacy += 1
            }
        }
        
        // Longitud del mensaje (mensajes largos suelen ser más personales)
        when {
            message.length > 200 -> intimacy += 2
            message.length > 100 -> intimacy += 1
        }
        
        // Historial previo con este contacto
        val previousInteractions = getInteractionsByContact(caller ?: "general")
        if (previousInteractions.size > 10) intimacy += 1
        if (previousInteractions.size > 50) intimacy += 1
        
        return intimacy.coerceIn(1, 10)
    }
    
    private fun updateEmotionalContext(caller: String?, message: String, emotion: String) {
        val key = caller ?: "general"
        
        if (!conversationContexts.containsKey(key)) {
            conversationContexts[key] = mutableListOf()
        }
        
        // Guardar mensaje con contexto emocional
        val emotionalMessage = "[$emotion] $message"
        conversationContexts[key]!!.add(emotionalMessage)
        
        // Mantener solo los últimos 15 mensajes por conversación
        val context = conversationContexts[key]!!
        if (context.size > 15) {
            context.removeAt(0)
        }
    }
    
    private fun updateContactInfoWithEmotion(caller: String?, userEmotion: String) {
        if (caller != null) {
            val existing = contacts[caller]
            val newRelationship = determineRelationshipLevel(caller)
            val averageEmotion = calculateAverageEmotion(caller)
            
            if (existing != null) {
                contacts[caller] = existing.copy(
                    lastInteraction = System.currentTimeMillis(),
                    interactionCount = existing.interactionCount + 1,
                    relationship = newRelationship,
                    averageEmotionalTone = averageEmotion
                )
            } else {
                contacts[caller] = ContactInfo(
                    number = caller,
                    name = getContactNameFromSystem(caller),
                    lastInteraction = System.currentTimeMillis(),
                    interactionCount = 1,
                    relationship = "acquaintance",
                    averageEmotionalTone = userEmotion
                )
            }
        }
    }
    
    private fun determineRelationshipLevel(caller: String): String {
        val interactionCount = getInteractionsByContact(caller).size
        val recentInteractions = getInteractionsByContact(caller).takeLast(10)
        val averageIntimacy = recentInteractions.map { it.intimacyLevel }.average()
        
        return when {
            interactionCount > 100 && averageIntimacy > 7 -> "best_friend"
            interactionCount > 50 && averageIntimacy > 5 -> "close"
            interactionCount > 20 && averageIntimacy > 3 -> "familiar"
            else -> "acquaintance"
        }
    }
    
    private fun calculateAverageEmotion(caller: String): String {
        val userInteractions = getInteractionsByContact(caller).takeLast(20)
        val emotions = userInteractions.map { it.userEmotion }
        
        val emotionCounts = emotions.groupingBy { it }.eachCount()
        return emotionCounts.maxByOrNull { it.value }?.key ?: "neutral"
    }
    
    private fun learnUserPersonality(message: String, emotion: String, caller: String?) {
        val key = caller ?: "general_user"
        
        // Analizar patrones de personalidad
        when {
            message.contains("me gusta", ignoreCase = true) -> {
                updatePersonalityTrait(key, "preferences", extractAfterPhrase(message, "me gusta"))
            }
            message.contains("me molesta", ignoreCase = true) -> {
                updatePersonalityTrait(key, "dislikes", extractAfterPhrase(message, "me molesta"))
            }
            message.contains("siempre", ignoreCase = true) -> {
                updatePersonalityTrait(key, "habits", extractAroundPhrase(message, "siempre"))
            }
            message.contains("nunca", ignoreCase = true) -> {
                updatePersonalityTrait(key, "avoidances", extractAroundPhrase(message, "nunca"))
            }
            message.contains("sueño", ignoreCase = true) -> {
                updatePersonalityTrait(key, "dreams", extractAroundPhrase(message, "sueño"))
            }
            message.contains("temo", ignoreCase = true) || message.contains("miedo", ignoreCase = true) -> {
                updatePersonalityTrait(key, "fears", extractAroundPhrase(message, "temo|miedo"))
            }
        }
        
        // Analizar patrones emocionales
        updateEmotionalPattern(key, emotion)
    }
    
    private fun updatePersonalityTrait(userId: String, trait: String, value: String) {
        val key = "${userId}_$trait"
        val currentList = userPersonality[key] as? MutableList<String> ?: mutableListOf()
        
        if (!currentList.contains(value) && value.isNotBlank()) {
            currentList.add(value.take(100)) // Limitar longitud
            userPersonality[key] = currentList
        }
    }
    
    private fun updateEmotionalPattern(userId: String, emotion: String) {
        val key = "${userId}_emotional_pattern"
        val pattern = userPersonality[key] as? MutableMap<String, Int> ?: mutableMapOf()
        
        pattern[emotion] = pattern.getOrDefault(emotion, 0) + 1
        userPersonality[key] = pattern
    }
    
    private fun extractAfterPhrase(text: String, phrase: String): String {
        val index = text.lowercase().indexOf(phrase.lowercase())
        if (index == -1) return ""
        
        return text.substring(index + phrase.length).take(50).trim()
    }
    
    private fun extractAroundPhrase(text: String, phrase: String): String {
        val regex = "\\b.*$phrase.*\\b".toRegex(RegexOption.IGNORE_CASE)
        val match = regex.find(text)
        return match?.value?.take(50) ?: ""
    }
    
    fun updateEmotionalBond(caller: String?, emotion: String) {
        if (caller == null) return
        
        val bond = emotionalBonds[caller] ?: EmotionalBond(caller)
        
        // Actualizar niveles basados en la emoción
        val newBond = when (emotion) {
            "happy", "grateful", "excited" -> {
                bond.copy(
                    affectionLevel = (bond.affectionLevel + 2).coerceAtMost(100),
                    trustLevel = (bond.trustLevel + 1).coerceAtMost(100)
                )
            }
            "sad", "worried" -> {
                bond.copy(
                    comfortLevel = (bond.comfortLevel + 3).coerceAtMost(100),
                    trustLevel = (bond.trustLevel + 2).coerceAtMost(100)
                )
            }
            "angry" -> {
                bond.copy(
                    comfortLevel = (bond.comfortLevel - 1).coerceAtLeast(0),
                    trustLevel = (bond.trustLevel - 1).coerceAtLeast(0)
                )
            }
            else -> bond
        }
        
        newBond.emotionalHistory.add("$emotion - ${Date()}")
        if (newBond.emotionalHistory.size > 20) {
            newBond.emotionalHistory.removeAt(0)
        }
        
        emotionalBonds[caller] = newBond
    }
    
    fun getConversationContext(caller: String?): List<String> {
        val key = caller ?: "general"
        return conversationContexts[key]?.toList() ?: emptyList()
    }
    
    fun getRelationshipLevel(caller: String?): String {
        if (caller == null) return "new"
        return contacts[caller]?.relationship ?: "new"
    }
    
    fun getEmotionalBond(caller: String?): EmotionalBond? {
        return if (caller != null) emotionalBonds[caller] else null
    }
    
    fun getContactName(number: String): String? {
        return contacts[number]?.name
    }
    
    fun getRecentInteractions(limit: Int = 20): List<EmotionalInteraction> {
        return interactions.takeLast(limit)
    }
    
    fun getInteractionsByType(type: InteractionType): List<EmotionalInteraction> {
        return interactions.filter { it.type == type }
    }
    
    fun getInteractionsByContact(caller: String): List<EmotionalInteraction> {
        return interactions.filter { it.caller == caller }
    }
    
    fun getRecentCallsFrom(caller: String): List<String> {
        return interactions
            .filter { it.caller == caller && it.type == InteractionType.PHONE_CALL }
            .takeLast(5)
            .map { it.message }
    }
    
    fun getUserPersonalityTraits(userId: String = "general_user"): Map<String, Any> {
        return userPersonality.filter { it.key.startsWith(userId) }
    }
    
    fun getMostCommonUserEmotion(userId: String = "general_user"): String {
        val pattern = userPersonality["${userId}_emotional_pattern"] as? Map<String, Int>
        return pattern?.maxByOrNull { it.value }?.key ?: "neutral"
    }
    
    fun learnPreference(key: String, value: Any) {
        personalPreferences[key] = value
    }
    
    fun getPreference(key: String): Any? {
        return personalPreferences[key]
    }
    
    fun getFrequentContacts(limit: Int = 5): List<ContactInfo> {
        return contacts.values
            .sortedByDescending { it.interactionCount }
            .take(limit)
    }
    
    fun getClosestFriends(limit: Int = 3): List<ContactInfo> {
        return contacts.values
            .filter { it.relationship in listOf("close", "best_friend") }
            .sortedBy { it.lastInteraction }
            .take(limit)
    }
    
    fun analyzeConversationPatterns(): Map<String, Any> {
        val totalInteractions = interactions.size
        val emotionDistribution = interactions.groupingBy { it.userEmotion }.eachCount()
        val moodDistribution = interactions.groupingBy { it.ritsuMood }.eachCount()
        val intimacyAverage = interactions.map { it.intimacyLevel }.average()
        
        val mostActiveTimes = interactions
            .groupBy { 
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = it.timestamp
                calendar.get(Calendar.HOUR_OF_DAY)
            }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(3)
        
        return mapOf(
            "total_interactions" to totalInteractions,
            "user_emotion_distribution" to emotionDistribution,
            "ritsu_mood_distribution" to moodDistribution,
            "average_intimacy" to intimacyAverage,
            "most_active_hours" to mostActiveTimes,
            "unique_contacts" to contacts.size,
            "relationship_distribution" to contacts.values.groupingBy { it.relationship }.eachCount()
        )
    }
    
    private fun getContactNameFromSystem(number: String): String? {
        // En una implementación real, consultaría el ContentResolver
        return null
    }
    
    fun loadAllMemories() {
        // En una implementación real, cargar desde base de datos persistente
        addSampleData()
    }
    
    private fun addSampleData() {
        // Datos de ejemplo para simular memoria previa
        saveEmotionalInteraction("Hola Ritsu", userEmotion = "curious", type = InteractionType.CHAT)
        saveEmotionalInteraction("Como estas?", userEmotion = "friendly", type = InteractionType.CHAT)
        learnPreference("preferred_language", "Spanish")
        learnPreference("communication_style", "friendly_and_curious")
    }
    
    fun getDetailedMemoryReport(): String {
        val patterns = analyzeConversationPatterns()
        val userTraits = getUserPersonalityTraits()
        
        return """
            🧠 Memoria Emocional de Ritsu:
            
            📊 Estadísticas Generales:
            • ${interactions.size} interacciones recordadas
            • ${contacts.size} contactos conocidos
            • ${emotionalBonds.size} vínculos emocionales establecidos
            • Intimidad promedio: ${String.format("%.1f", patterns["average_intimacy"] as Double)}/10
            
            💜 Vínculos Emocionales:
            ${getEmotionalBondsReport()}
            
            💭 Patrones de Personalidad Aprendidos:
            ${getPersonalityReport(userTraits)}
            
            ⏰ Momentos más activos:
            ${getActiveTimesReport(patterns["most_active_hours"] as List<Pair<Int, Int>>)}
        """.trimIndent()
    }
    
    private fun getEmotionalBondsReport(): String {
        return emotionalBonds.values.take(3).joinToString("\n") { bond ->
            val contact = contacts[bond.contactId]
            val name = contact?.name ?: "Contacto ${bond.contactId.take(4)}"
            "• $name - Confianza: ${bond.trustLevel}%, Cariño: ${bond.affectionLevel}%, Comodidad: ${bond.comfortLevel}%"
        }
    }
    
    private fun getPersonalityReport(traits: Map<String, Any>): String {
        return traits.entries.take(5).joinToString("\n") { (key, value) ->
            val traitName = key.substringAfterLast("_").replace("_", " ").capitalize()
            "• $traitName: $value"
        }
    }
    
    private fun getActiveTimesReport(times: List<Pair<Int, Int>>): String {
        return times.joinToString("\n") { (hour, count) ->
            "• ${hour}:00 - $count interacciones"
        }
    }
    
    fun clearOldMemories() {
        val oneMonthAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        interactions.removeAll { it.timestamp < oneMonthAgo }
    }
    
    fun getPersonalizedGreeting(): String {
        val recentMood = getRecentInteractions(5).map { it.userEmotion }
        val commonMood = recentMood.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
        
        return when (commonMood) {
            "happy" -> "Hola! Te noto muy positivo últimamente, ¡me encanta esa energía! 😊✨"
            "sad" -> "Hola... he notado que has estado un poco triste. ¿Cómo te sientes hoy? 💜"
            "excited" -> "¡Hola! ¡Tu emoción reciente es tan contagiosa! ¿Qué te tiene tan emocionado? 😆"
            "curious" -> "Hola~ Me encanta tu curiosidad últimamente. ¿Qué nuevas cosas has descubierto? 🤔✨"
            else -> "¡Hola! ¿Cómo te sientes hoy? 😊🌸"
        }
    
    // NUEVO: Métodos mejorados para el sistema inteligente
    fun saveCallInteraction(callerNumber: String?, callerName: String?) {
        if (callerNumber == null) return
        
        val interaction = EmotionalInteraction(
            message = "Llamada telefónica",
            caller = callerNumber,
            type = InteractionType.PHONE_CALL,
            ritsuMood = "helpful"
        )
        
        interactions.add(interaction)
        
        // Actualizar información de contacto
        updateContactFromCall(callerNumber, callerName)
    }
    
    private fun updateContactFromCall(callerNumber: String, callerName: String?) {
        val existingContact = contacts[callerNumber]
        if (existingContact != null) {
            contacts[callerNumber] = existingContact.copy(
                lastInteraction = System.currentTimeMillis(),
                interactionCount = existingContact.interactionCount + 1
            )
        } else {
            contacts[callerNumber] = ContactInfo(
                number = callerNumber,
                name = callerName,
                lastInteraction = System.currentTimeMillis(),
                interactionCount = 1
            )
        }
    }