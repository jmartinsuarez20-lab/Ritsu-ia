package com.ritsuai.assistant.core.ai

import com.ritsuai.assistant.core.RitsuAICore

/**
 * Data class to hold the result of the AI processing.
 */
data class AIResult(
    val response: RitsuAICore.RitsuResponse,
    val analysis: InputAnalysis
)

/**
 * Interface for the AI engine.
 * This allows swapping different AI implementations (e.g., rule-based, LLM-based).
 */
interface AIEngine {
    suspend fun process(
        userInput: String,
        context: RitsuAICore.ConversationContext
    ): AIResult

    suspend fun handlePhoneCall(callerInfo: RitsuAICore.CallerInfo, relationship: RitsuAICore.Relationship): RitsuAICore.CallResponse

    suspend fun handleWhatsAppMessage(sender: String, message: String, relationship: RitsuAICore.Relationship): RitsuAICore.MessageResponse
}

/**
 * Data class representing the analysis of the user's input.
 */
data class InputAnalysis(
    val originalInput: String,
    val sentiment: RitsuAICore.Sentiment,
    val intent: RitsuAICore.Intent,
    val entities: List<RitsuAICore.Entity>,
    val emotionalTone: RitsuAICore.EmotionalTone,
    val userPersonalityType: RitsuAICore.UserPersonalityType,
    val urgency: Float,
    val timestamp: Long
)
