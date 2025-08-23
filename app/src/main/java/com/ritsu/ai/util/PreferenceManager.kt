package com.ritsu.ai.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ritsu.ai.data.model.UserPreference
import com.ritsu.ai.data.model.AvatarConfig
import com.ritsu.ai.data.model.ClothingItem
import com.ritsu.ai.data.model.LearningPattern

class PreferenceManager(context: Context) {
    
    companion object {
        private const val PREF_NAME = "ritsu_preferences"
        
        // Claves de configuración general
        private const val KEY_AUTO_START = "auto_start_enabled"
        private const val KEY_OVERLAY_ENABLED = "overlay_enabled"
        private const val KEY_FORMAL_LANGUAGE = "formal_language"
        private const val KEY_SHORT_RESPONSES = "short_responses"
        private const val KEY_FIRST_TIME_SETUP = "first_time_setup"
        
        // Claves del avatar
        private const val KEY_AVATAR_SIZE = "avatar_size"
        private const val KEY_AVATAR_POSITION_X = "avatar_position_x"
        private const val KEY_AVATAR_POSITION_Y = "avatar_position_y"
        private const val KEY_AVATAR_VISIBLE = "avatar_visible"
        private const val KEY_AVATAR_CONFIG = "avatar_config"
        
        // Claves de ropa
        private const val KEY_CURRENT_CLOTHING = "current_clothing"
        private const val KEY_SAVED_CLOTHING = "saved_clothing"
        private const val KEY_CLOTHING_HISTORY = "clothing_history"
        
        // Claves de aprendizaje
        private const val KEY_LEARNING_ENABLED = "learning_enabled"
        private const val KEY_LEARNED_PATTERNS = "learned_patterns"
        private const val KEY_USER_PREFERENCES = "user_preferences"
        
        // Claves de comunicación
        private const val KEY_VOICE_ENABLED = "voice_enabled"
        private const val KEY_VOICE_SPEED = "voice_speed"
        private const val KEY_VOICE_PITCH = "voice_pitch"
        
        // Claves de aplicaciones
        private const val KEY_FAVORITE_APPS = "favorite_apps"
        private const val KEY_APP_SHORTCUTS = "app_shortcuts"
        
        // Claves de modo especial
        private const val KEY_SPECIAL_MODE_ENABLED = "special_mode_enabled"
        private const val KEY_SPECIAL_MODE_LAST_USED = "special_mode_last_used"
    }
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    // Configuración general
    fun isAutoStartEnabled(): Boolean = sharedPreferences.getBoolean(KEY_AUTO_START, false)
    fun setAutoStartEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean(KEY_AUTO_START, enabled).apply()
    
    fun isOverlayEnabled(): Boolean = sharedPreferences.getBoolean(KEY_OVERLAY_ENABLED, false)
    fun setOverlayEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean(KEY_OVERLAY_ENABLED, enabled).apply()
    
    fun isFormalLanguage(): Boolean = sharedPreferences.getBoolean(KEY_FORMAL_LANGUAGE, true)
    fun setFormalLanguage(formal: Boolean) = sharedPreferences.edit().putBoolean(KEY_FORMAL_LANGUAGE, formal).apply()
    
    fun isShortResponses(): Boolean = sharedPreferences.getBoolean(KEY_SHORT_RESPONSES, false)
    fun setShortResponses(short: Boolean) = sharedPreferences.edit().putBoolean(KEY_SHORT_RESPONSES, short).apply()
    
    fun isFirstTimeSetup(): Boolean = sharedPreferences.getBoolean(KEY_FIRST_TIME_SETUP, true)
    fun setFirstTimeSetup(firstTime: Boolean) = sharedPreferences.edit().putBoolean(KEY_FIRST_TIME_SETUP, firstTime).apply()
    
    // Configuración del avatar
    fun getAvatarSize(): Int = sharedPreferences.getInt(KEY_AVATAR_SIZE, 200)
    fun setAvatarSize(size: Int) = sharedPreferences.edit().putInt(KEY_AVATAR_SIZE, size).apply()
    
    fun getAvatarPositionX(): Float = sharedPreferences.getFloat(KEY_AVATAR_POSITION_X, 100f)
    fun setAvatarPositionX(x: Float) = sharedPreferences.edit().putFloat(KEY_AVATAR_POSITION_X, x).apply()
    
    fun getAvatarPositionY(): Float = sharedPreferences.getFloat(KEY_AVATAR_POSITION_Y, 100f)
    fun setAvatarPositionY(y: Float) = sharedPreferences.edit().putFloat(KEY_AVATAR_POSITION_Y, y).apply()
    
    fun isAvatarVisible(): Boolean = sharedPreferences.getBoolean(KEY_AVATAR_VISIBLE, true)
    fun setAvatarVisible(visible: Boolean) = sharedPreferences.edit().putBoolean(KEY_AVATAR_VISIBLE, visible).apply()
    
    fun getAvatarConfig(): AvatarConfig? {
        val json = sharedPreferences.getString(KEY_AVATAR_CONFIG, null)
        return if (json != null) gson.fromJson(json, AvatarConfig::class.java) else null
    }
    
    fun setAvatarConfig(config: AvatarConfig) {
        val json = gson.toJson(config)
        sharedPreferences.edit().putString(KEY_AVATAR_CONFIG, json).apply()
    }
    
    // Configuración de ropa
    fun getCurrentClothing(): ClothingItem? {
        val json = sharedPreferences.getString(KEY_CURRENT_CLOTHING, null)
        return if (json != null) gson.fromJson(json, ClothingItem::class.java) else null
    }
    
    fun setCurrentClothing(clothing: ClothingItem) {
        val json = gson.toJson(clothing)
        sharedPreferences.edit().putString(KEY_CURRENT_CLOTHING, json).apply()
    }
    
    fun getSavedClothing(): List<ClothingItem> {
        val json = sharedPreferences.getString(KEY_SAVED_CLOTHING, "[]")
        val type = object : TypeToken<List<ClothingItem>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    fun setSavedClothing(clothing: List<ClothingItem>) {
        val json = gson.toJson(clothing)
        sharedPreferences.edit().putString(KEY_SAVED_CLOTHING, json).apply()
    }
    
    fun addSavedClothing(clothing: ClothingItem) {
        val current = getSavedClothing().toMutableList()
        current.add(clothing)
        setSavedClothing(current)
    }
    
    // Configuración de aprendizaje
    fun isLearningEnabled(): Boolean = sharedPreferences.getBoolean(KEY_LEARNING_ENABLED, true)
    fun setLearningEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean(KEY_LEARNING_ENABLED, enabled).apply()
    
    fun getLearnedPatterns(): List<LearningPattern> {
        val json = sharedPreferences.getString(KEY_LEARNED_PATTERNS, "[]")
        val type = object : TypeToken<List<LearningPattern>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    fun setLearnedPatterns(patterns: List<LearningPattern>) {
        val json = gson.toJson(patterns)
        sharedPreferences.edit().putString(KEY_LEARNED_PATTERNS, json).apply()
    }
    
    fun addLearnedPattern(pattern: LearningPattern) {
        val current = getLearnedPatterns().toMutableList()
        current.add(pattern)
        setLearnedPatterns(current)
    }
    
    fun getUserPreferences(): List<UserPreference> {
        val json = sharedPreferences.getString(KEY_USER_PREFERENCES, "[]")
        val type = object : TypeToken<List<UserPreference>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    fun setUserPreferences(preferences: List<UserPreference>) {
        val json = gson.toJson(preferences)
        sharedPreferences.edit().putString(KEY_USER_PREFERENCES, json).apply()
    }
    
    fun addUserPreference(preference: UserPreference) {
        val current = getUserPreferences().toMutableList()
        current.add(preference)
        setUserPreferences(current)
    }
    
    // Configuración de voz
    fun isVoiceEnabled(): Boolean = sharedPreferences.getBoolean(KEY_VOICE_ENABLED, true)
    fun setVoiceEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean(KEY_VOICE_ENABLED, enabled).apply()
    
    fun getVoiceSpeed(): Float = sharedPreferences.getFloat(KEY_VOICE_SPEED, 1.0f)
    fun setVoiceSpeed(speed: Float) = sharedPreferences.edit().putFloat(KEY_VOICE_SPEED, speed).apply()
    
    fun getVoicePitch(): Float = sharedPreferences.getFloat(KEY_VOICE_PITCH, 1.0f)
    fun setVoicePitch(pitch: Float) = sharedPreferences.edit().putFloat(KEY_VOICE_PITCH, pitch).apply()
    
    // Configuración de aplicaciones
    fun getFavoriteApps(): List<String> {
        val json = sharedPreferences.getString(KEY_FAVORITE_APPS, "[]")
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    fun setFavoriteApps(apps: List<String>) {
        val json = gson.toJson(apps)
        sharedPreferences.edit().putString(KEY_FAVORITE_APPS, json).apply()
    }
    
    fun addFavoriteApp(packageName: String) {
        val current = getFavoriteApps().toMutableList()
        if (!current.contains(packageName)) {
            current.add(packageName)
            setFavoriteApps(current)
        }
    }
    
    fun getAppShortcuts(): Map<String, String> {
        val json = sharedPreferences.getString(KEY_APP_SHORTCUTS, "{}")
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }
    
    fun setAppShortcuts(shortcuts: Map<String, String>) {
        val json = gson.toJson(shortcuts)
        sharedPreferences.edit().putString(KEY_APP_SHORTCUTS, json).apply()
    }
    
    // Configuración de modo especial
    fun isSpecialModeEnabled(): Boolean = sharedPreferences.getBoolean(KEY_SPECIAL_MODE_ENABLED, false)
    fun setSpecialModeEnabled(enabled: Boolean) = sharedPreferences.edit().putBoolean(KEY_SPECIAL_MODE_ENABLED, enabled).apply()
    
    fun getSpecialModeLastUsed(): Long = sharedPreferences.getLong(KEY_SPECIAL_MODE_LAST_USED, 0L)
    fun setSpecialModeLastUsed(timestamp: Long) = sharedPreferences.edit().putLong(KEY_SPECIAL_MODE_LAST_USED, timestamp).apply()
    
    // Métodos de utilidad
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean = sharedPreferences.getBoolean(key, defaultValue)
    fun putBoolean(key: String, value: Boolean) = sharedPreferences.edit().putBoolean(key, value).apply()
    
    fun getString(key: String, defaultValue: String? = null): String? = sharedPreferences.getString(key, defaultValue)
    fun putString(key: String, value: String) = sharedPreferences.edit().putString(key, value).apply()
    
    fun getInt(key: String, defaultValue: Int = 0): Int = sharedPreferences.getInt(key, defaultValue)
    fun putInt(key: String, value: Int) = sharedPreferences.edit().putInt(key, value).apply()
    
    fun getFloat(key: String, defaultValue: Float = 0f): Float = sharedPreferences.getFloat(key, defaultValue)
    fun putFloat(key: String, value: Float) = sharedPreferences.edit().putFloat(key, value).apply()
    
    fun getLong(key: String, defaultValue: Long = 0L): Long = sharedPreferences.getLong(key, defaultValue)
    fun putLong(key: String, value: Long) = sharedPreferences.edit().putLong(key, value).apply()
    
    // Guardar estado actual
    fun saveCurrentState() {
        // Guardar timestamp de último guardado
        putLong("last_save_timestamp", System.currentTimeMillis())
    }
    
    // Limpiar datos
    fun clearAllData() {
        sharedPreferences.edit().clear().apply()
    }
    
    fun clearLearningData() {
        setLearnedPatterns(emptyList())
        setUserPreferences(emptyList())
    }
    
    fun clearClothingData() {
        setSavedClothing(emptyList())
        sharedPreferences.edit().remove(KEY_CURRENT_CLOTHING).apply()
    }
}