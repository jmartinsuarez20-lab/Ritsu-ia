package com.ritsuai.assistant.core.ai

import com.ritsuai.assistant.core.ConversationEngine
import com.ritsuai.assistant.core.MemoryManager
import com.ritsuai.assistant.core.PersonalityEngine
import com.ritsuai.assistant.core.RitsuAICore
import kotlin.random.Random

class RuleBasedAIEngine(
    private val personalityEngine: PersonalityEngine,
    private val memoryManager: MemoryManager
) : AIEngine {

    private val conversationEngine = ConversationEngine(personalityEngine, memoryManager)

    override suspend fun process(
        userInput: String,
        context: RitsuAICore.ConversationContext
    ): AIResult {
        val analysis = analyzeUserInput(userInput, context)
        val response = conversationEngine.generateResponse(userInput, analysis, context)
        return AIResult(response, analysis)
    }

    private fun analyzeUserInput(input: String, context: RitsuAICore.ConversationContext): InputAnalysis {
        val lowerInput = input.lowercase()

        val sentiment = analyzeSentiment(lowerInput)
        val intent = analyzeIntent(lowerInput)
        val entities = extractEntities(lowerInput)
        val emotionalTone = analyzeEmotionalTone(lowerInput)
        val personalityType = analyzeUserPersonality(lowerInput, context)
        val relationship = memoryManager.getRelationshipWithContact(context.sender)
        val urgency = calculateUrgency(
            InputAnalysis(
                originalInput = input,
                sentiment = sentiment,
                intent = intent,
                entities = entities,
                emotionalTone = emotionalTone,
                userPersonalityType = personalityType,
                timestamp = System.currentTimeMillis(),
                urgency = 0.0f // temp value
            ),
            relationship
        )

        return InputAnalysis(
            originalInput = input,
            sentiment = sentiment,
            intent = intent,
            entities = entities,
            emotionalTone = emotionalTone,
            userPersonalityType = personalityType,
            timestamp = System.currentTimeMillis(),
            urgency = urgency
        )
    }

    private fun analyzeSentiment(input: String): RitsuAICore.Sentiment {
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
            positiveCount > negativeCount -> RitsuAICore.Sentiment.POSITIVE
            negativeCount > positiveCount -> RitsuAICore.Sentiment.NEGATIVE
            else -> RitsuAICore.Sentiment.NEUTRAL
        }
    }

    private fun analyzeIntent(input: String): RitsuAICore.Intent {
        return when {
            input.contains("quiero") || input.contains("necesito") -> RitsuAICore.Intent.REQUEST
            input.contains("?") || input.contains("qué") || input.contains("cómo") -> RitsuAICore.Intent.QUESTION
            input.contains("gracias") || input.contains("bien hecho") -> RitsuAICore.Intent.APPRECIATION
            input.contains("hola") || input.contains("buenos") -> RitsuAICore.Intent.GREETING
            input.contains("adiós") || input.contains("hasta") -> RitsuAICore.Intent.FAREWELL
            input.contains("cambiar") && input.contains("ropa") -> RitsuAICore.Intent.OUTFIT_CHANGE
            input.contains("llamar") || input.contains("teléfono") -> RitsuAICore.Intent.PHONE_ACTION
            input.contains("mensaje") || input.contains("whatsapp") -> RitsuAICore.Intent.MESSAGE_ACTION
            else -> RitsuAICore.Intent.CONVERSATION
        }
    }

    private fun extractEntities(input: String): List<RitsuAICore.Entity> {
        val entities = mutableListOf<RitsuAICore.Entity>()

        // Números de teléfono
        val phonePattern = Regex("""\b\d{9,}\b""")
        phonePattern.findAll(input).forEach {
            entities.add(RitsuAICore.Entity(RitsuAICore.EntityType.PHONE_NUMBER, it.value))
        }

        // Nombres (palabras con mayúscula)
        val namePattern = Regex("""\b[A-Z][a-z]+\b""")
        namePattern.findAll(input).forEach {
            entities.add(RitsuAICore.Entity(RitsuAICore.EntityType.PERSON_NAME, it.value))
        }

        // Colores
        val colors = listOf("rojo", "azul", "verde", "amarillo", "rosa", "negro", "blanco")
        colors.forEach { color ->
            if (input.contains(color)) {
                entities.add(RitsuAICore.Entity(RitsuAICore.EntityType.COLOR, color))
            }
        }

        // Ropa
        val clothes = listOf("vestido", "falda", "camisa", "pantalón", "bikini", "lencería")
        clothes.forEach { clothing ->
            if (input.contains(clothing)) {
                entities.add(RitsuAICore.Entity(RitsuAICore.EntityType.CLOTHING, clothing))
            }
        }

        return entities
    }

    private fun analyzeEmotionalTone(input: String): RitsuAICore.EmotionalTone {
        return when {
            input.contains("amor") || input.contains("te quiero") -> RitsuAICore.EmotionalTone.LOVING
            input.contains("sexy") || input.contains("sensual") -> RitsuAICore.EmotionalTone.FLIRTY
            input.contains("ayuda") || input.contains("por favor") -> RitsuAICore.EmotionalTone.NEEDY
            input.contains("!") && analyzeSentiment(input) == RitsuAICore.Sentiment.POSITIVE -> RitsuAICore.EmotionalTone.EXCITED
            input.contains("...") || input.contains("mm") -> RitsuAICore.EmotionalTone.THOUGHTFUL
            else -> RitsuAICore.EmotionalTone.NEUTRAL
        }
    }

    private fun analyzeUserPersonality(input: String, context: RitsuAICore.ConversationContext): RitsuAICore.UserPersonalityType {
        val history = memoryManager.getUserInteractionHistory(context.sender)

        if (history.isEmpty()) return RitsuAICore.UserPersonalityType.BALANCED

        return when {
            history.count { it.isPolite } > history.size * 0.7 -> RitsuAICore.UserPersonalityType.POLITE
            history.count { it.isFlirty } > history.size * 0.5 -> RitsuAICore.UserPersonalityType.FLIRTY
            history.count { it.isInformal } > history.size * 0.6 -> RitsuAICore.UserPersonalityType.CASUAL
            history.count { it.isCommanding } > history.size * 0.4 -> RitsuAICore.UserPersonalityType.COMMANDING
            else -> RitsuAICore.UserPersonalityType.BALANCED
        }
    }

    private fun calculateUrgency(analysis: InputAnalysis, relationship: RitsuAICore.Relationship): Float {
        var urgency = 0.0f

        urgency += when (relationship.type) {
            RitsuAICore.RelationshipType.PARTNER -> 0.8f
            RitsuAICore.RelationshipType.FAMILY -> 0.6f
            RitsuAICore.RelationshipType.FRIEND -> 0.4f
            RitsuAICore.RelationshipType.WORK -> 0.7f
            RitsuAICore.RelationshipType.UNKNOWN -> 0.1f
        }

        if (analysis.intent == RitsuAICore.Intent.REQUEST) urgency += 0.2f
        if (analysis.sentiment == RitsuAICore.Sentiment.NEGATIVE) urgency += 0.3f
        if (analysis.originalInput.contains("urgente") || analysis.originalInput.contains("importante")) {
            urgency += 0.4f
        }

        return urgency.coerceIn(0.0f, 1.0f)
    }

    override suspend fun handlePhoneCall(callerInfo: RitsuAICore.CallerInfo, relationship: RitsuAICore.Relationship): RitsuAICore.CallResponse {
        val greeting = when (relationship.type) {
            RitsuAICore.RelationshipType.PARTNER -> {
                "¡Hola cariño! Soy Ritsu. ${callerInfo.name ?: "Tu pareja"} está ocupado ahora mismo, pero quería que supieras que siempre habla de ti con mucho amor ❤️"
            }
            RitsuAICore.RelationshipType.FAMILY -> {
                "¡Hola! Soy Ritsu, la asistente personal. ${callerInfo.name ?: "Él"} está ocupado en este momento, pero puedo ayudarte o tomar un mensaje."
            }
            RitsuAICore.RelationshipType.FRIEND -> {
                "¡Hola ${callerInfo.name ?: "amigo"}! Hablas con Ritsu. Él está ocupado pero me ha hablado de ti. ¿Puedo ayudarte en algo?"
            }
            RitsuAICore.RelationshipType.WORK -> {
                "Buenos días. Habla Ritsu, asistente personal. En este momento está en una reunión importante. ¿Puedo tomar un mensaje o ayudarle con algo?"
            }
            RitsuAICore.RelationshipType.UNKNOWN -> {
                if (isSpamLikely(callerInfo)) {
                    "Esta llamada puede ser spam. ¿Desea que la rechace automáticamente?"
                } else {
                    "Hola, habla con Ritsu. ¿En qué puedo ayudarle?"
                }
            }
        }

        return RitsuAICore.CallResponse(
            shouldAnswer = relationship.type != RitsuAICore.RelationshipType.UNKNOWN || !isSpamLikely(callerInfo),
            greeting = greeting,
            tone = when (relationship.type) {
                RitsuAICore.RelationshipType.PARTNER -> RitsuAICore.CallTone.LOVING
                RitsuAICore.RelationshipType.FAMILY -> RitsuAICore.CallTone.WARM
                RitsuAICore.RelationshipType.FRIEND -> RitsuAICore.CallTone.FRIENDLY
                RitsuAICore.RelationshipType.WORK -> RitsuAICore.CallTone.PROFESSIONAL
                RitsuAICore.RelationshipType.UNKNOWN -> RitsuAICore.CallTone.POLITE
            }
        )
    }

    override suspend fun handleWhatsAppMessage(sender: String, message: String, relationship: RitsuAICore.Relationship): RitsuAICore.MessageResponse {
        val context = RitsuAICore.ConversationContext(
            platform = "whatsapp",
            sender = sender,
            relationship = relationship,
            timestamp = System.currentTimeMillis()
        )

        val result = process(message, context)

        return RitsuAICore.MessageResponse(
            shouldRespond = shouldRespondToMessage(relationship, result.analysis),
            response = result.response.text,
            urgency = result.analysis.urgency,
            suggestedActions = result.response.suggestedActions
        )
    }

    private fun shouldRespondToMessage(relationship: RitsuAICore.Relationship, analysis: InputAnalysis): Boolean {
        return when {
            relationship.type == RitsuAICore.RelationshipType.PARTNER -> true
            relationship.type == RitsuAICore.RelationshipType.FAMILY && analysis.intent == RitsuAICore.Intent.REQUEST -> true
            analysis.urgency > 0.7f -> true
            else -> false
        }
    }

    private fun isSpamLikely(callerInfo: RitsuAICore.CallerInfo): Boolean {
        // Detectar spam usando patrones
        return when {
            callerInfo.name == null && callerInfo.phoneNumber.length != 9 -> true
            callerInfo.phoneNumber.startsWith("900") -> true // Números premium
            callerInfo.phoneNumber.startsWith("901") -> true
            else -> false
        }
    }
}
