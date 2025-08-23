package com.ritsu.ai.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "user_preferences")
data class UserPreference(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val category: PreferenceCategory,
    val key: String,
    val value: String,
    val confidence: Float = 1.0f,
    val usageCount: Int = 1,
    val lastUsed: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun createAppPreference(appName: String, action: String): UserPreference {
            return UserPreference(
                category = PreferenceCategory.APPLICATION,
                key = appName.lowercase().trim(),
                value = action
            )
        }
        
        fun createLanguagePreference(preference: String, value: String): UserPreference {
            return UserPreference(
                category = PreferenceCategory.LANGUAGE,
                key = preference.lowercase().trim(),
                value = value
            )
        }
        
        fun createClothingPreference(style: String, preference: String): UserPreference {
            return UserPreference(
                category = PreferenceCategory.CLOTHING,
                key = style.lowercase().trim(),
                value = preference
            )
        }
        
        fun createBehaviorPreference(behavior: String, preference: String): UserPreference {
            return UserPreference(
                category = PreferenceCategory.BEHAVIOR,
                key = behavior.lowercase().trim(),
                value = preference
            )
        }
        
        fun createTimePreference(timeOfDay: String, preference: String): UserPreference {
            return UserPreference(
                category = PreferenceCategory.TIME,
                key = timeOfDay.lowercase().trim(),
                value = preference
            )
        }
        
        fun createGeneralPreference(key: String, value: String): UserPreference {
            return UserPreference(
                category = PreferenceCategory.GENERAL,
                key = key.lowercase().trim(),
                value = value
            )
        }
    }
    
    fun incrementUsage(): UserPreference {
        return copy(
            usageCount = usageCount + 1,
            lastUsed = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
    
    fun updateValue(newValue: String): UserPreference {
        return copy(
            value = newValue,
            lastUsed = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
    
    fun updateConfidence(newConfidence: Float): UserPreference {
        return copy(
            confidence = newConfidence.coerceIn(0f, 1f),
            updatedAt = System.currentTimeMillis()
        )
    }
    
    fun isReliable(): Boolean {
        return usageCount >= 3 && confidence >= 0.8f
    }
    
    fun getWeight(): Float {
        return (confidence * usageCount.toFloat()) / 100f
    }
    
    fun matchesKey(searchKey: String): Boolean {
        val normalizedSearchKey = searchKey.lowercase().trim()
        val normalizedKey = key.lowercase().trim()
        return normalizedKey.contains(normalizedSearchKey) || normalizedSearchKey.contains(normalizedKey)
    }
    
    fun getSimilarity(searchKey: String): Float {
        val normalizedSearchKey = searchKey.lowercase().trim()
        val normalizedKey = key.lowercase().trim()
        
        if (normalizedKey == normalizedSearchKey) return 1.0f
        if (normalizedKey.contains(normalizedSearchKey) || normalizedSearchKey.contains(normalizedKey)) return 0.8f
        
        // Calcular similitud basada en palabras comunes
        val searchWords = normalizedSearchKey.split(" ").toSet()
        val keyWords = normalizedKey.split(" ").toSet()
        val commonWords = searchWords.intersect(keyWords)
        
        return if (searchWords.isNotEmpty() && keyWords.isNotEmpty()) {
            commonWords.size.toFloat() / maxOf(searchWords.size, keyWords.size)
        } else {
            0f
        }
    }
}

enum class PreferenceCategory(val displayName: String) {
    APPLICATION("Aplicación"),
    LANGUAGE("Lenguaje"),
    CLOTHING("Ropa"),
    BEHAVIOR("Comportamiento"),
    TIME("Tiempo"),
    LOCATION("Ubicación"),
    WEATHER("Clima"),
    MOOD("Estado de Ánimo"),
    ACTIVITY("Actividad"),
    GENERAL("General")
}