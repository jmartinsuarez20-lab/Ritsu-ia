package com.ritsuai.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "ritsu_preferences",
        Context.MODE_PRIVATE
    )
    
    // Configuración de personalidad
    fun setRitsuPersonality(name: String, personality: String, voice: String, avatarStyle: String) {
        prefs.edit()
            .putString("ritsu_name", name)
            .putString("ritsu_personality", personality)
            .putString("ritsu_voice", voice)
            .putString("ritsu_avatar_style", avatarStyle)
            .apply()
    }
    
    fun getRitsuName(): String = prefs.getString("ritsu_name", "Ritsu") ?: "Ritsu"
    fun getRitsuPersonality(): String = prefs.getString("ritsu_personality", "") ?: ""
    fun getRitsuVoice(): String = prefs.getString("ritsu_voice", "female_spanish") ?: "female_spanish"
    fun getRitsuAvatarStyle(): String = prefs.getString("ritsu_avatar_style", "anime_kawaii") ?: "anime_kawaii"
    
    // Modo sin censura
    fun setUncensoredMode(enabled: Boolean) {
        prefs.edit().putBoolean("uncensored_mode", enabled).apply()
    }
    
    fun isUncensoredModeEnabled(): Boolean = prefs.getBoolean("uncensored_mode", false)
    
    // Permisos
    fun setBasicPermissionsGranted(granted: Boolean) {
        prefs.edit().putBoolean("basic_permissions_granted", granted).apply()
    }
    
    fun areBasicPermissionsGranted(): Boolean = prefs.getBoolean("basic_permissions_granted", false)
    
    fun setAccessibilityEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("accessibility_enabled", enabled).apply()
    }
    
    fun isAccessibilityEnabled(): Boolean = prefs.getBoolean("accessibility_enabled", false)
    
    fun setOverlayPermissionGranted(granted: Boolean) {
        prefs.edit().putBoolean("overlay_permission_granted", granted).apply()
    }
    
    fun isOverlayPermissionGranted(): Boolean = prefs.getBoolean("overlay_permission_granted", false)
    
    // OpenAI API
    fun getOpenAIKey(): String? = prefs.getString("openai_api_key", null)
    
    fun setOpenAIKey(key: String) {
        prefs.edit().putString("openai_api_key", key).apply()
    }
}