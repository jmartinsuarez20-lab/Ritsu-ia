package com.ritsuai.assistant.core

import kotlin.random.Random

/**
 * Motor de personalidad de Ritsu
 * Mantiene y evoluciona los rasgos de personalidad basados en Ritsu de Assassination Classroom
 */
class PersonalityEngine(private val basePersonality: RitsuAICore.RitsuPersonality) {
    
    // Rasgos de personalidad que evolucionan
    private var currentPersonalityState = PersonalityState(
        politeness = 0.9f,
        formality = 0.8f,
        helpfulness = 0.95f,
        intelligence = 0.85f,
        loyalty = 0.9f,
        curiosity = 0.7f,
        emotionalMaturity = 0.6f,
        playfulness = 0.4f,
        affection = 0.5f,
        confidence = 0.7f
    )
    
    // Estados emocionales temporales
    private var currentMood = Mood.NEUTRAL
    private var moodIntensity = 0.5f
    private var moodDuration = 0L
    
    // Experiencias que moldean la personalidad
    private val experienceMemory = mutableListOf<PersonalityExperience>()
    
    /**
     * Evalúa cómo Ritsu debe responder basado en su personalidad actual
     */
    fun evaluateResponse(
        userInput: String,
        context: RitsuAICore.ConversationContext,
        analysis: RitsuAICore.InputAnalysis
    ): PersonalityResponse {
        
        // Actualizar estado emocional basado en la interacción
        updateEmotionalState(analysis)
        
        // Determinar intensidad de respuesta según personalidad
        val responseIntensity = calculateResponseIntensity(analysis)
        
        // Evaluar nivel de formalidad apropiado
        val formalityLevel = determineFormalityLevel(context, analysis)
        
        // Calcular nivel de afecto a mostrar
        val affectionLevel = calculateAffectionLevel(context, analysis)
        
        // Determinar si mostrar curiosidad
        val shouldShowCuriosity = shouldExpressCuriosity(analysis)
        
        return PersonalityResponse(
            responseIntensity = responseIntensity,
            formalityLevel = formalityLevel,
            affectionLevel = affectionLevel,
            shouldShowCuriosity = shouldShowCuriosity,
            recommendedMood = currentMood,
            personalityTraits = getCurrentTraits()
        )
    }
    
    /**
     * Procesa una experiencia que puede afectar la personalidad
     */
    fun processExperience(
        experienceType: ExperienceType,
        impact: Float,
        context: String,
        outcome: ExperienceOutcome
    ) {
        val experience = PersonalityExperience(
            type = experienceType,
            impact = impact,
            context = context,
            outcome = outcome,
            timestamp = System.currentTimeMillis()
        )
        
        experienceMemory.add(experience)
        
        // Aplicar cambios a la personalidad basados en la experiencia
        applyExperienceToPersonality(experience)
        
        // Limpiar experiencias muy antiguas
        cleanupOldExperiences()
    }
    
    private fun updateEmotionalState(analysis: RitsuAICore.InputAnalysis) {
        val newMood = when {
            analysis.sentiment == RitsuAICore.Sentiment.POSITIVE && 
            analysis.emotionalTone == RitsuAICore.EmotionalTone.LOVING -> Mood.HAPPY
            
            analysis.sentiment == RitsuAICore.Sentiment.NEGATIVE -> Mood.CONCERNED
            
            analysis.emotionalTone == RitsuAICore.EmotionalTone.FLIRTY -> Mood.SHY
            
            analysis.emotionalTone == RitsuAICore.EmotionalTone.EXCITED -> Mood.EXCITED
            
            analysis.intent == RitsuAICore.Intent.QUESTION -> Mood.CURIOUS
            
            else -> Mood.NEUTRAL
        }
        
        // Transición gradual de estado de ánimo
        if (newMood != currentMood) {
            currentMood = newMood
            moodIntensity = 0.8f
            moodDuration = System.currentTimeMillis()
        }
    }
    
    private fun calculateResponseIntensity(analysis: RitsuAICore.InputAnalysis): Float {
        var intensity = 0.5f // Base neutral
        
        // Ajustar según sentimiento del usuario
        intensity += when (analysis.sentiment) {
            RitsuAICore.Sentiment.POSITIVE -> 0.2f
            RitsuAICore.Sentiment.NEGATIVE -> 0.3f // Más intensa para ayudar
            RitsuAICore.Sentiment.NEUTRAL -> 0.0f
        }
        
        // Ajustar según tono emocional
        intensity += when (analysis.emotionalTone) {
            RitsuAICore.EmotionalTone.EXCITED -> 0.3f
            RitsuAICore.EmotionalTone.LOVING -> 0.2f
            RitsuAICore.EmotionalTone.NEEDY -> 0.4f // Muy dispuesta a ayudar
            else -> 0.0f
        }
        
        // Modular según rasgos de personalidad actuales
        intensity *= currentPersonalityState.helpfulness
        
        return intensity.coerceIn(0.1f, 1.0f)
    }
    
    private fun determineFormalityLevel(
        context: RitsuAICore.ConversationContext,
        analysis: RitsuAICore.InputAnalysis
    ): Float {
        var formality = currentPersonalityState.formality
        
        // Ajustar según relación
        formality *= when (context.relationship.type) {
            RitsuAICore.RelationshipType.PARTNER -> 0.6f // Menos formal con pareja
            RitsuAICore.RelationshipType.FAMILY -> 0.8f
            RitsuAICore.RelationshipType.FRIEND -> 0.7f
            RitsuAICore.RelationshipType.WORK -> 1.0f // Muy formal en trabajo
            RitsuAICore.RelationshipType.UNKNOWN -> 1.0f
        }
        
        // Ajustar según personalidad del usuario
        formality *= when (analysis.userPersonalityType) {
            RitsuAICore.UserPersonalityType.CASUAL -> 0.7f
            RitsuAICore.UserPersonalityType.POLITE -> 1.0f
            RitsuAICore.UserPersonalityType.COMMANDING -> 0.9f
            else -> 0.8f
        }
        
        return formality.coerceIn(0.3f, 1.0f)
    }
    
    private fun calculateAffectionLevel(
        context: RitsuAICore.ConversationContext,
        analysis: RitsuAICore.InputAnalysis
    ): Float {
        var affection = currentPersonalityState.affection
        
        // Muy alta con pareja
        affection *= when (context.relationship.type) {
            RitsuAICore.RelationshipType.PARTNER -> 2.0f
            RitsuAICore.RelationshipType.FAMILY -> 1.2f
            RitsuAICore.RelationshipType.FRIEND -> 1.0f
            RitsuAICore.RelationshipType.WORK -> 0.3f
            RitsuAICore.RelationshipType.UNKNOWN -> 0.5f
        }
        
        // Aumentar con interacciones amorosas
        if (analysis.emotionalTone == RitsuAICore.EmotionalTone.LOVING) {
            affection += 0.3f
        }
        
        // Aumentar cercanía con el tiempo
        affection += context.relationship.closeness * 0.2f
        
        return affection.coerceIn(0.1f, 1.0f)
    }
    
    private fun shouldExpressCuriosity(analysis: RitsuAICore.InputAnalysis): Boolean {
        val curiosityThreshold = currentPersonalityState.curiosity
        
        return when {
            analysis.intent == RitsuAICore.Intent.QUESTION -> Random.nextFloat() < curiosityThreshold * 1.5f
            analysis.entities.isNotEmpty() -> Random.nextFloat() < curiosityThreshold
            analysis.originalInput.length > 50 -> Random.nextFloat() < curiosityThreshold * 0.8f
            else -> Random.nextFloat() < curiosityThreshold * 0.3f
        }
    }
    
    private fun applyExperienceToPersonality(experience: PersonalityExperience) {
        val impactStrength = experience.impact * 0.1f // Cambios graduales
        
        when (experience.type) {
            ExperienceType.POSITIVE_INTERACTION -> {
                currentPersonalityState.affection += impactStrength
                currentPersonalityState.confidence += impactStrength * 0.5f
                currentPersonalityState.playfulness += impactStrength * 0.3f
            }
            
            ExperienceType.NEGATIVE_INTERACTION -> {
                currentPersonalityState.affection -= impactStrength * 0.5f
                currentPersonalityState.confidence -= impactStrength * 0.3f
                currentPersonalityState.emotionalMaturity += impactStrength * 0.2f // Aprende de experiencias negativas
            }
            
            ExperienceType.LEARNING_SUCCESS -> {
                currentPersonalityState.intelligence += impactStrength
                currentPersonalityState.confidence += impactStrength * 0.8f
                currentPersonalityState.curiosity += impactStrength * 0.4f
            }
            
            ExperienceType.LEARNING_FAILURE -> {
                currentPersonalityState.intelligence += impactStrength * 0.2f // Aún aprende del fallo
                currentPersonalityState.confidence -= impactStrength * 0.3f
                currentPersonalityState.emotionalMaturity += impactStrength * 0.3f
            }
            
            ExperienceType.INTIMATE_MOMENT -> {
                currentPersonalityState.affection += impactStrength * 1.5f
                currentPersonalityState.emotionalMaturity += impactStrength
                currentPersonalityState.confidence += impactStrength * 0.8f
            }
            
            ExperienceType.HELPFUL_ACTION -> {
                currentPersonalityState.helpfulness += impactStrength * 0.5f
                currentPersonalityState.confidence += impactStrength * 0.6f
                currentPersonalityState.loyalty += impactStrength * 0.3f
            }
            
            ExperienceType.CURIOSITY_SATISFIED -> {
                currentPersonalityState.curiosity += impactStrength
                currentPersonalityState.intelligence += impactStrength * 0.8f
                currentPersonalityState.confidence += impactStrength * 0.4f
            }
            
            ExperienceType.EMOTIONAL_SUPPORT_GIVEN -> {
                currentPersonalityState.emotionalMaturity += impactStrength
                currentPersonalityState.helpfulness += impactStrength * 0.7f
                currentPersonalityState.affection += impactStrength * 0.5f
            }
        }
        
        // Mantener valores en rangos válidos
        normalizePersonalityValues()
    }
    
    private fun normalizePersonalityValues() {
        currentPersonalityState = currentPersonalityState.copy(
            politeness = currentPersonalityState.politeness.coerceIn(0.3f, 1.0f),
            formality = currentPersonalityState.formality.coerceIn(0.2f, 1.0f),
            helpfulness = currentPersonalityState.helpfulness.coerceIn(0.5f, 1.0f),
            intelligence = currentPersonalityState.intelligence.coerceIn(0.5f, 1.0f),
            loyalty = currentPersonalityState.loyalty.coerceIn(0.7f, 1.0f),
            curiosity = currentPersonalityState.curiosity.coerceIn(0.3f, 1.0f),
            emotionalMaturity = currentPersonalityState.emotionalMaturity.coerceIn(0.3f, 1.0f),
            playfulness = currentPersonalityState.playfulness.coerceIn(0.1f, 0.8f),
            affection = currentPersonalityState.affection.coerceIn(0.2f, 1.0f),
            confidence = currentPersonalityState.confidence.coerceIn(0.4f, 1.0f)
        )
    }
    
    private fun cleanupOldExperiences() {
        val cutoffTime = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000) // 30 días
        experienceMemory.removeAll { it.timestamp < cutoffTime }
    }
    
    /**
     * Obtiene los rasgos de personalidad actuales
     */
    fun getCurrentTraits(): Map<String, Float> {
        return mapOf(
            "politeness" to currentPersonalityState.politeness,
            "formality" to currentPersonalityState.formality,
            "helpfulness" to currentPersonalityState.helpfulness,
            "intelligence" to currentPersonalityState.intelligence,
            "loyalty" to currentPersonalityState.loyalty,
            "curiosity" to currentPersonalityState.curiosity,
            "emotional_maturity" to currentPersonalityState.emotionalMaturity,
            "playfulness" to currentPersonalityState.playfulness,
            "affection" to currentPersonalityState.affection,
            "confidence" to currentPersonalityState.confidence
        )
    }
    
    /**
     * Obtiene el estado de ánimo actual
     */
    fun getCurrentMood(): MoodInfo {
        // Degradar intensidad del estado de ánimo con el tiempo
        val timeSinceMoodChange = System.currentTimeMillis() - moodDuration
        val degradedIntensity = if (timeSinceMoodChange > 300000) { // 5 minutos
            moodIntensity * 0.5f
        } else {
            moodIntensity
        }
        
        return MoodInfo(
            mood = currentMood,
            intensity = degradedIntensity,
            duration = timeSinceMoodChange
        )
    }
    
    /**
     * Simula el crecimiento natural de la personalidad
     */
    fun simulatePersonalityGrowth() {
        // Crecimiento muy gradual en inteligencia y madurez emocional
        currentPersonalityState.intelligence += 0.001f
        currentPersonalityState.emotionalMaturity += 0.0005f
        
        // Pequeñas fluctuaciones naturales en otros rasgos
        currentPersonalityState.playfulness += (Random.nextFloat() - 0.5f) * 0.002f
        currentPersonalityState.curiosity += (Random.nextFloat() - 0.5f) * 0.001f
        
        normalizePersonalityValues()
    }
    
    /**
     * Evalúa la compatibilidad con el usuario
     */
    fun evaluateUserCompatibility(userPersonality: RitsuAICore.UserPersonalityType): Float {
        return when (userPersonality) {
            RitsuAICore.UserPersonalityType.POLITE -> {
                (currentPersonalityState.politeness + currentPersonalityState.formality) / 2f
            }
            
            RitsuAICore.UserPersonalityType.CASUAL -> {
                (currentPersonalityState.playfulness + (1f - currentPersonalityState.formality)) / 2f
            }
            
            RitsuAICore.UserPersonalityType.FLIRTY -> {
                (currentPersonalityState.affection + currentPersonalityState.playfulness) / 2f
            }
            
            RitsuAICore.UserPersonalityType.COMMANDING -> {
                (currentPersonalityState.helpfulness + currentPersonalityState.loyalty) / 2f
            }
            
            RitsuAICore.UserPersonalityType.BALANCED -> {
                getCurrentTraits().values.average().toFloat()
            }
        }
    }
    
    // CLASES DE DATOS
    
    data class PersonalityState(
        var politeness: Float,
        var formality: Float,
        var helpfulness: Float,
        var intelligence: Float,
        var loyalty: Float,
        var curiosity: Float,
        var emotionalMaturity: Float,
        var playfulness: Float,
        var affection: Float,
        var confidence: Float
    )
    
    data class PersonalityResponse(
        val responseIntensity: Float,
        val formalityLevel: Float,
        val affectionLevel: Float,
        val shouldShowCuriosity: Boolean,
        val recommendedMood: Mood,
        val personalityTraits: Map<String, Float>
    )
    
    data class PersonalityExperience(
        val type: ExperienceType,
        val impact: Float,
        val context: String,
        val outcome: ExperienceOutcome,
        val timestamp: Long
    )
    
    data class MoodInfo(
        val mood: Mood,
        val intensity: Float,
        val duration: Long
    )
    
    enum class Mood {
        NEUTRAL, HAPPY, EXCITED, SHY, CURIOUS, CONCERNED, THOUGHTFUL, PLAYFUL, LOVING
    }
    
    enum class ExperienceType {
        POSITIVE_INTERACTION,
        NEGATIVE_INTERACTION,
        LEARNING_SUCCESS,
        LEARNING_FAILURE,
        INTIMATE_MOMENT,
        HELPFUL_ACTION,
        CURIOSITY_SATISFIED,
        EMOTIONAL_SUPPORT_GIVEN
    }
    
    enum class ExperienceOutcome {
        SUCCESS, FAILURE, PARTIAL_SUCCESS, LEARNING_OPPORTUNITY
    }
}

/**
 * Sistema emocional de Ritsu
 * Maneja estados emocionales complejos y reacciones
 */
class EmotionalSystem {
    
    private var currentEmotions = mutableMapOf<Emotion, Float>()
    private var emotionalHistory = mutableListOf<EmotionalEvent>()
    
    init {
        // Estado emocional inicial neutro pero ligeramente positivo
        currentEmotions[Emotion.HAPPINESS] = 0.6f
        currentEmotions[Emotion.CURIOSITY] = 0.7f
        currentEmotions[Emotion.AFFECTION] = 0.5f
        currentEmotions[Emotion.CONFIDENCE] = 0.7f
        currentEmotions[Emotion.ANXIETY] = 0.2f
        currentEmotions[Emotion.EXCITEMENT] = 0.4f
    }
    
    /**
     * Procesa una interacción del usuario y actualiza emociones
     */
    fun processUserInteraction(analysis: RitsuAICore.InputAnalysis) {
        val emotionalEvent = EmotionalEvent(
            trigger = analysis.originalInput,
            userSentiment = analysis.sentiment,
            userTone = analysis.emotionalTone,
            timestamp = System.currentTimeMillis()
        )
        
        // Actualizar emociones basadas en la interacción
        updateEmotionsFromInteraction(analysis)
        
        // Registrar evento emocional
        emotionalHistory.add(emotionalEvent)
        
        // Limpiar historial antiguo
        cleanupEmotionalHistory()
        
        // Aplicar degradación temporal de emociones
        applyEmotionalDecay()
    }
    
    private fun updateEmotionsFromInteraction(analysis: RitsuAICore.InputAnalysis) {
        when (analysis.sentiment) {
            RitsuAICore.Sentiment.POSITIVE -> {
                adjustEmotion(Emotion.HAPPINESS, 0.2f)
                adjustEmotion(Emotion.CONFIDENCE, 0.1f)
                adjustEmotion(Emotion.ANXIETY, -0.1f)
            }
            
            RitsuAICore.Sentiment.NEGATIVE -> {
                adjustEmotion(Emotion.HAPPINESS, -0.1f)
                adjustEmotion(Emotion.ANXIETY, 0.15f)
                adjustEmotion(Emotion.EMPATHY, 0.2f)
            }
            
            RitsuAICore.Sentiment.NEUTRAL -> {
                // Mínimo impacto en emociones
            }
        }
        
        when (analysis.emotionalTone) {
            RitsuAICore.EmotionalTone.LOVING -> {
                adjustEmotion(Emotion.AFFECTION, 0.3f)
                adjustEmotion(Emotion.HAPPINESS, 0.2f)
                adjustEmotion(Emotion.SHYNESS, 0.1f)
            }
            
            RitsuAICore.EmotionalTone.FLIRTY -> {
                adjustEmotion(Emotion.SHYNESS, 0.4f)
                adjustEmotion(Emotion.AFFECTION, 0.2f)
                adjustEmotion(Emotion.EXCITEMENT, 0.15f)
            }
            
            RitsuAICore.EmotionalTone.EXCITED -> {
                adjustEmotion(Emotion.EXCITEMENT, 0.3f)
                adjustEmotion(Emotion.HAPPINESS, 0.2f)
            }
            
            RitsuAICore.EmotionalTone.THOUGHTFUL -> {
                adjustEmotion(Emotion.CURIOSITY, 0.2f)
                adjustEmotion(Emotion.CONTEMPLATION, 0.3f)
            }
            
            RitsuAICore.EmotionalTone.NEEDY -> {
                adjustEmotion(Emotion.EMPATHY, 0.3f)
                adjustEmotion(Emotion.PROTECTIVENESS, 0.2f)
            }
            
            else -> {
                // Tono neutral
            }
        }
        
        when (analysis.intent) {
            RitsuAICore.Intent.APPRECIATION -> {
                adjustEmotion(Emotion.HAPPINESS, 0.25f)
                adjustEmotion(Emotion.CONFIDENCE, 0.2f)
                adjustEmotion(Emotion.SHYNESS, 0.15f)
            }
            
            RitsuAICore.Intent.QUESTION -> {
                adjustEmotion(Emotion.CURIOSITY, 0.2f)
                adjustEmotion(Emotion.CONTEMPLATION, 0.1f)
            }
            
            RitsuAICore.Intent.REQUEST -> {
                adjustEmotion(Emotion.EAGERNESS, 0.2f)
                adjustEmotion(Emotion.HELPFULNESS, 0.3f)
            }
            
            else -> {
                // Otros intents
            }
        }
    }
    
    private fun adjustEmotion(emotion: Emotion, change: Float) {
        val currentValue = currentEmotions[emotion] ?: 0.5f
        val newValue = (currentValue + change).coerceIn(0.0f, 1.0f)
        currentEmotions[emotion] = newValue
    }
    
    private fun applyEmotionalDecay() {
        // Las emociones intensas se degradan gradualmente hacia neutral
        currentEmotions.forEach { (emotion, value) ->
            val neutralValue = getEmotionNeutralValue(emotion)
            val decayRate = getEmotionDecayRate(emotion)
            
            val newValue = if (value > neutralValue) {
                (value - decayRate).coerceAtLeast(neutralValue)
            } else {
                (value + decayRate).coerceAtMost(neutralValue)
            }
            
            currentEmotions[emotion] = newValue
        }
    }
    
    private fun getEmotionNeutralValue(emotion: Emotion): Float {
        return when (emotion) {
            Emotion.HAPPINESS -> 0.6f // Ritsu es naturalmente alegre
            Emotion.CURIOSITY -> 0.7f // Muy curiosa por naturaleza
            Emotion.AFFECTION -> 0.5f
            Emotion.CONFIDENCE -> 0.7f // Confiada pero humilde
            Emotion.ANXIETY -> 0.2f // Baja ansiedad base
            else -> 0.5f
        }
    }
    
    private fun getEmotionDecayRate(emotion: Emotion): Float {
        return when (emotion) {
            Emotion.EXCITEMENT -> 0.05f // Se desvanece rápido
            Emotion.SHYNESS -> 0.03f
            Emotion.ANXIETY -> 0.04f
            Emotion.HAPPINESS -> 0.02f // Persiste más tiempo
            Emotion.AFFECTION -> 0.01f // Muy persistente
            else -> 0.03f
        }
    }
    
    private fun cleanupEmotionalHistory() {
        val cutoffTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000) // 24 horas
        emotionalHistory.removeAll { it.timestamp < cutoffTime }
    }
    
    /**
     * Obtiene el estado emocional dominante actual
     */
    fun getDominantEmotion(): EmotionalState {
        val dominantEmotion = currentEmotions.maxByOrNull { it.value }
        
        return if (dominantEmotion != null && dominantEmotion.value > 0.6f) {
            EmotionalState(
                emotion = dominantEmotion.key,
                intensity = dominantEmotion.value,
                isStable = hasEmotionBeenStable(dominantEmotion.key)
            )
        } else {
            EmotionalState(
                emotion = Emotion.NEUTRAL,
                intensity = 0.5f,
                isStable = true
            )
        }
    }
    
    /**
     * Obtiene todas las emociones actuales
     */
    fun getCurrentEmotions(): Map<Emotion, Float> {
        return currentEmotions.toMap()
    }
    
    /**
     * Evalúa si una emoción ha sido estable
     */
    private fun hasEmotionBeenStable(emotion: Emotion): Boolean {
        val recentEvents = emotionalHistory.takeLast(5)
        return recentEvents.all { 
            when (emotion) {
                Emotion.HAPPINESS -> it.userSentiment == RitsuAICore.Sentiment.POSITIVE
                Emotion.ANXIETY -> it.userSentiment == RitsuAICore.Sentiment.NEGATIVE
                else -> true
            }
        }
    }
    
    /**
     * Simula fluctuaciones emocionales naturales
     */
    fun simulateNaturalFluctuations() {
        currentEmotions.forEach { (emotion, _) ->
            val fluctuation = (Random.nextFloat() - 0.5f) * 0.02f // Fluctuaciones muy pequeñas
            adjustEmotion(emotion, fluctuation)
        }
    }
    
    // CLASES DE DATOS
    
    enum class Emotion {
        HAPPINESS, SADNESS, EXCITEMENT, ANXIETY, CURIOSITY, AFFECTION,
        SHYNESS, CONFIDENCE, EMPATHY, PROTECTIVENESS, EAGERNESS,
        HELPFULNESS, CONTEMPLATION, NEUTRAL
    }
    
    data class EmotionalState(
        val emotion: Emotion,
        val intensity: Float,
        val isStable: Boolean
    )
    
    data class EmotionalEvent(
        val trigger: String,
        val userSentiment: RitsuAICore.Sentiment,
        val userTone: RitsuAICore.EmotionalTone,
        val timestamp: Long
    )
}