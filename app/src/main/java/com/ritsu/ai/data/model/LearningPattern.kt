package com.ritsu.ai.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "learning_patterns")
data class LearningPattern(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val patternType: PatternType,
    val input: String,
    val output: String,
    val context: String? = null,
    val confidence: Float = 1.0f,
    val usageCount: Int = 1,
    val successRate: Float = 1.0f,
    val lastUsed: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun createCommandPattern(command: String, action: String, context: String? = null): LearningPattern {
            return LearningPattern(
                patternType = PatternType.COMMAND,
                input = command.lowercase().trim(),
                output = action,
                context = context
            )
        }
        
        fun createResponsePattern(question: String, response: String, context: String? = null): LearningPattern {
            return LearningPattern(
                patternType = PatternType.RESPONSE,
                input = question.lowercase().trim(),
                output = response,
                context = context
            )
        }
        
        fun createPreferencePattern(preference: String, value: String, context: String? = null): LearningPattern {
            return LearningPattern(
                patternType = PatternType.PREFERENCE,
                input = preference.lowercase().trim(),
                output = value,
                context = context
            )
        }
        
        fun createBehaviorPattern(behavior: String, response: String, context: String? = null): LearningPattern {
            return LearningPattern(
                patternType = PatternType.BEHAVIOR,
                input = behavior.lowercase().trim(),
                output = response,
                context = context
            )
        }
        
        fun fromContentValues(values: android.content.ContentValues): LearningPattern {
            return LearningPattern(
                id = values.getAsString("id") ?: UUID.randomUUID().toString(),
                patternType = PatternType.valueOf(values.getAsString("patternType") ?: "COMMAND"),
                input = values.getAsString("input") ?: "",
                output = values.getAsString("output") ?: "",
                context = values.getAsString("context"),
                confidence = values.getAsFloat("confidence") ?: 1.0f,
                usageCount = values.getAsInteger("usageCount") ?: 1,
                successRate = values.getAsFloat("successRate") ?: 1.0f,
                lastUsed = values.getAsLong("lastUsed") ?: System.currentTimeMillis(),
                createdAt = values.getAsLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = values.getAsLong("updatedAt") ?: System.currentTimeMillis()
            )
        }
    }
    
    fun incrementUsage(success: Boolean = true): LearningPattern {
        val newUsageCount = usageCount + 1
        val newSuccessRate = if (success) {
            ((successRate * usageCount) + 1) / newUsageCount
        } else {
            (successRate * usageCount) / newUsageCount
        }
        
        return copy(
            usageCount = newUsageCount,
            successRate = newSuccessRate,
            lastUsed = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
    
    fun updateConfidence(newConfidence: Float): LearningPattern {
        return copy(
            confidence = newConfidence.coerceIn(0f, 1f),
            updatedAt = System.currentTimeMillis()
        )
    }
    
    fun isReliable(): Boolean {
        return usageCount >= 3 && successRate >= 0.7f && confidence >= 0.8f
    }
    
    fun getWeight(): Float {
        return (confidence * successRate * usageCount.toFloat()) / 100f
    }
    
    fun matchesInput(input: String): Boolean {
        val normalizedInput = input.lowercase().trim()
        return this.input.contains(normalizedInput) || normalizedInput.contains(this.input)
    }
    
    fun getSimilarity(input: String): Float {
        val normalizedInput = input.lowercase().trim()
        val patternInput = this.input.lowercase().trim()
        
        if (patternInput == normalizedInput) return 1.0f
        if (patternInput.contains(normalizedInput) || normalizedInput.contains(patternInput)) return 0.8f
        
        // Calcular similitud basada en palabras comunes
        val inputWords = normalizedInput.split(" ").toSet()
        val patternWords = patternInput.split(" ").toSet()
        val commonWords = inputWords.intersect(patternWords)
        
        return if (inputWords.isNotEmpty() && patternWords.isNotEmpty()) {
            commonWords.size.toFloat() / maxOf(inputWords.size, patternWords.size)
        } else {
            0f
        }
    }
}

enum class PatternType(val displayName: String) {
    COMMAND("Comando"),
    RESPONSE("Respuesta"),
    PREFERENCE("Preferencia"),
    BEHAVIOR("Comportamiento"),
    INTERACTION("Interacción"),
    CONTEXT("Contexto")
}