package com.ritsu.ai.util

import android.content.Context
import com.ritsu.ai.data.RitsuDatabase
import com.ritsu.ai.data.model.LearningPattern
import com.ritsu.ai.data.model.UserPreference
import com.ritsu.ai.data.model.PatternType
import com.ritsu.ai.data.model.PreferenceCategory
import kotlinx.coroutines.*
import java.util.*

class LearningManager(private val context: Context) {
    
    private val database = RitsuDatabase.getInstance(context)
    private val preferenceManager = PreferenceManager(context)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Configuración de aprendizaje
    private val maxPatterns = 500
    private val minConfidence = 0.7f
    private val learningRate = 0.1f
    private val decayRate = 0.95f
    
    // Patrones de comportamiento comunes
    private val commonPatterns = mapOf(
        "saludo_matutino" to LearningPattern.createBehaviorPattern(
            "buenos días",
            "saludo_formal",
            "mañana"
        ),
        "saludo_tardío" to LearningPattern.createBehaviorPattern(
            "buenas tardes",
            "saludo_formal",
            "tarde"
        ),
        "saludo_nocturno" to LearningPattern.createBehaviorPattern(
            "buenas noches",
            "saludo_formal",
            "noche"
        ),
        "agradecimiento" to LearningPattern.createBehaviorPattern(
            "gracias",
            "respuesta_agradecimiento",
            "general"
        ),
        "despedida" to LearningPattern.createBehaviorPattern(
            "adiós",
            "respuesta_despedida",
            "general"
        ),
        "apertura_app" to LearningPattern.createCommandPattern(
            "abrir",
            "OPEN_APP",
            "aplicaciones"
        ),
        "cierre_app" to LearningPattern.createCommandPattern(
            "cerrar",
            "CLOSE_APP",
            "aplicaciones"
        ),
        "envío_mensaje" to LearningPattern.createCommandPattern(
            "enviar mensaje",
            "SEND_MESSAGE",
            "comunicación"
        ),
        "realizar_llamada" to LearningPattern.createCommandPattern(
            "llamar",
            "MAKE_CALL",
            "comunicación"
        ),
        "tomar_foto" to LearningPattern.createCommandPattern(
            "foto",
            "TAKE_PHOTO",
            "multimedia"
        ),
        "reproducir_música" to LearningPattern.createCommandPattern(
            "música",
            "PLAY_MUSIC",
            "entretenimiento"
        ),
        "configurar_recordatorio" to LearningPattern.createCommandPattern(
            "recordatorio",
            "SET_REMINDER",
            "productividad"
        )
    )
    
    // Preferencias de usuario comunes
    private val commonPreferences = mapOf(
        "lenguaje_formal" to UserPreference.createLanguagePreference(
            "lenguaje",
            "formal"
        ),
        "lenguaje_informal" to UserPreference.createLanguagePreference(
            "lenguaje",
            "informal"
        ),
        "respuestas_cortas" to UserPreference.createBehaviorPreference(
            "respuestas",
            "cortas"
        ),
        "respuestas_detalladas" to UserPreference.createBehaviorPreference(
            "respuestas",
            "detalladas"
        ),
        "avatar_visible" to UserPreference.createBehaviorPreference(
            "avatar",
            "visible"
        ),
        "avatar_oculto" to UserPreference.createBehaviorPreference(
            "avatar",
            "oculto"
        ),
        "voz_habilitada" to UserPreference.createBehaviorPreference(
            "voz",
            "habilitada"
        ),
        "voz_deshabilitada" to UserPreference.createBehaviorPreference(
            "voz",
            "deshabilitada"
        )
    )
    
    init {
        initialize()
    }
    
    private fun initialize() {
        scope.launch {
            try {
                // Cargar patrones comunes si no existen
                loadCommonPatterns()
                
                // Cargar preferencias comunes si no existen
                loadCommonPreferences()
                
                // Limpiar patrones antiguos
                cleanupOldPatterns()
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private suspend fun loadCommonPatterns() {
        try {
            val existingPatterns = database.learningPatternDao().getAllLearningPatterns()
            
            commonPatterns.forEach { (key, pattern) ->
                val exists = existingPatterns.any { it.input == pattern.input && it.output == pattern.output }
                if (!exists) {
                    database.learningPatternDao().insertLearningPattern(pattern)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private suspend fun loadCommonPreferences() {
        try {
            val existingPreferences = database.userPreferenceDao().getAllUserPreferences()
            
            commonPreferences.forEach { (key, preference) ->
                val exists = existingPreferences.any { it.key == preference.key && it.value == preference.value }
                if (!exists) {
                    database.userPreferenceDao().insertUserPreference(preference)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private suspend fun cleanupOldPatterns() {
        try {
            val allPatterns = database.learningPatternDao().getAllLearningPatterns()
            
            if (allPatterns.size > maxPatterns) {
                // Eliminar patrones más antiguos y menos confiables
                val sortedPatterns = allPatterns.sortedWith(
                    compareBy<LearningPattern> { it.confidence }
                        .thenBy { it.usageCount }
                        .thenBy { it.lastUsed }
                )
                
                val patternsToDelete = sortedPatterns.take(allPatterns.size - maxPatterns)
                patternsToDelete.forEach { pattern ->
                    database.learningPatternDao().deleteLearningPattern(pattern)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun learnFromInteraction(input: String, response: String, success: Boolean, context: String? = null) {
        try {
            // Crear patrón de aprendizaje
            val pattern = LearningPattern(
                patternType = PatternType.INTERACTION,
                input = input.lowercase().trim(),
                output = response,
                context = context,
                confidence = if (success) 1.0f else 0.5f,
                usageCount = 1,
                successRate = if (success) 1.0f else 0.0f
            )
            
            // Buscar patrón existente
            val existingPatterns = database.learningPatternDao().searchLearningPatterns(input)
            val similarPattern = existingPatterns.find { it.getSimilarity(input) > 0.8f }
            
            if (similarPattern != null) {
                // Actualizar patrón existente
                val updatedPattern = similarPattern.incrementUsage(success)
                database.learningPatternDao().updateLearningPattern(updatedPattern)
            } else {
                // Crear nuevo patrón
                database.learningPatternDao().insertLearningPattern(pattern)
            }
            
            // Aprender preferencias del usuario
            learnUserPreferences(input, response, success, context)
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private suspend fun learnUserPreferences(input: String, response: String, success: Boolean, context: String?) {
        try {
            val normalizedInput = input.lowercase().trim()
            
            // Detectar preferencias de lenguaje
            if (normalizedInput.contains("formal") || response.contains("usted")) {
                val preference = UserPreference.createLanguagePreference("lenguaje", "formal")
                addOrUpdatePreference(preference)
            } else if (normalizedInput.contains("informal") || response.contains("tú")) {
                val preference = UserPreference.createLanguagePreference("lenguaje", "informal")
                addOrUpdatePreference(preference)
            }
            
            // Detectar preferencias de respuesta
            if (response.length < 50) {
                val preference = UserPreference.createBehaviorPreference("respuestas", "cortas")
                addOrUpdatePreference(preference)
            } else if (response.length > 100) {
                val preference = UserPreference.createBehaviorPreference("respuestas", "detalladas")
                addOrUpdatePreference(preference)
            }
            
            // Detectar preferencias de aplicaciones
            val appKeywords = listOf("whatsapp", "instagram", "facebook", "youtube", "gmail", "chrome")
            appKeywords.forEach { app ->
                if (normalizedInput.contains(app)) {
                    val preference = UserPreference.createAppPreference(app, "favorita")
                    addOrUpdatePreference(preference)
                }
            }
            
            // Detectar preferencias de tiempo
            val timeOfDay = getTimeOfDay()
            if (timeOfDay != null) {
                val preference = UserPreference.createTimePreference(timeOfDay, "activo")
                addOrUpdatePreference(preference)
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private suspend fun addOrUpdatePreference(preference: UserPreference) {
        try {
            val existingPreferences = database.userPreferenceDao().getUserPreferencesByKey(preference.key)
            val similarPreference = existingPreferences.find { it.value == preference.value }
            
            if (similarPreference != null) {
                // Actualizar preferencia existente
                val updatedPreference = similarPreference.incrementUsage()
                database.userPreferenceDao().updateUserPreference(updatedPreference)
            } else {
                // Crear nueva preferencia
                database.userPreferenceDao().insertUserPreference(preference)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun getTimeOfDay(): String? {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        return when {
            hour in 6..11 -> "mañana"
            hour in 12..17 -> "tarde"
            hour in 18..23 -> "noche"
            hour in 0..5 -> "madrugada"
            else -> null
        }
    }
    
    suspend fun getLearnedPatterns(): List<LearningPattern> {
        return try {
            database.learningPatternDao().getReliableLearningPatterns()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getUserPreferences(): List<UserPreference> {
        return try {
            database.userPreferenceDao().getReliableUserPreferences()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getPatternsByType(type: PatternType): List<LearningPattern> {
        return try {
            database.learningPatternDao().getLearningPatternsByType(type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getPreferencesByCategory(category: PreferenceCategory): List<UserPreference> {
        return try {
            database.userPreferenceDao().getUserPreferencesByCategory(category)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun searchPatterns(query: String): List<LearningPattern> {
        return try {
            database.learningPatternDao().searchLearningPatterns(query)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun searchPreferences(query: String): List<UserPreference> {
        return try {
            database.userPreferenceDao().searchUserPreferences(query)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getMostUsedPatterns(limit: Int = 10): List<LearningPattern> {
        return try {
            database.learningPatternDao().getMostUsedLearningPatterns(limit)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getMostUsedPreferences(limit: Int = 10): List<UserPreference> {
        return try {
            database.userPreferenceDao().getMostUsedUserPreferences(limit)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getRecentlyUsedPatterns(hours: Int = 24): List<LearningPattern> {
        return try {
            val timestamp = System.currentTimeMillis() - (hours * 60 * 60 * 1000L)
            database.learningPatternDao().getRecentlyUsedLearningPatterns(timestamp)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getRecentlyUsedPreferences(hours: Int = 24): List<UserPreference> {
        return try {
            val timestamp = System.currentTimeMillis() - (hours * 60 * 60 * 1000L)
            database.userPreferenceDao().getRecentlyUsedUserPreferences(timestamp)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun updatePatternConfidence(patternId: String, newConfidence: Float) {
        try {
            database.learningPatternDao().updateConfidence(patternId, newConfidence)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun updatePreferenceConfidence(preferenceId: String, newConfidence: Float) {
        try {
            database.userPreferenceDao().updateConfidence(preferenceId, newConfidence)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun deletePattern(patternId: String) {
        try {
            database.learningPatternDao().deleteLearningPatternById(patternId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun deletePreference(preferenceId: String) {
        try {
            database.userPreferenceDao().deleteUserPreferenceById(preferenceId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun clearAllLearningData() {
        try {
            database.learningPatternDao().deleteAllLearningPatterns()
            database.userPreferenceDao().deleteAllUserPreferences()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun getLearningStatistics(): LearningStatistics {
        return try {
            val totalPatterns = database.learningPatternDao().getLearningPatternCount()
            val reliablePatterns = database.learningPatternDao().getReliableLearningPatternCount()
            val avgConfidence = database.learningPatternDao().getAverageConfidence() ?: 0f
            val avgSuccessRate = database.learningPatternDao().getAverageSuccessRate() ?: 0f
            val avgUsageCount = database.learningPatternDao().getAverageUsageCount() ?: 0f
            
            val totalPreferences = database.userPreferenceDao().getUserPreferenceCount()
            val reliablePreferences = database.userPreferenceDao().getReliableUserPreferenceCount()
            val avgPreferenceConfidence = database.userPreferenceDao().getAverageConfidence() ?: 0f
            val avgPreferenceUsage = database.userPreferenceDao().getAverageUsageCount() ?: 0f
            
            LearningStatistics(
                totalPatterns = totalPatterns,
                reliablePatterns = reliablePatterns,
                averagePatternConfidence = avgConfidence,
                averagePatternSuccessRate = avgSuccessRate,
                averagePatternUsageCount = avgUsageCount,
                totalPreferences = totalPreferences,
                reliablePreferences = reliablePreferences,
                averagePreferenceConfidence = avgPreferenceConfidence,
                averagePreferenceUsageCount = avgPreferenceUsage
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LearningStatistics()
        }
    }
    
    fun cleanup() {
        scope.cancel()
    }
    
    data class LearningStatistics(
        val totalPatterns: Int = 0,
        val reliablePatterns: Int = 0,
        val averagePatternConfidence: Float = 0f,
        val averagePatternSuccessRate: Float = 0f,
        val averagePatternUsageCount: Float = 0f,
        val totalPreferences: Int = 0,
        val reliablePreferences: Int = 0,
        val averagePreferenceConfidence: Float = 0f,
        val averagePreferenceUsageCount: Float = 0f
    )
}