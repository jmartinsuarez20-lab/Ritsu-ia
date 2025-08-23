package com.ritsu.ai.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class PreferenceManager(context: Context) {
    
    companion object {
        private const val PREF_AUTO_START_SERVICES = "auto_start_services"
        private const val PREF_OVERLAY_ENABLED = "overlay_enabled"
        private const val PREF_SPECIAL_MODE_UNLOCKED = "special_mode_unlocked"
        private const val PREF_CURRENT_CLOTHING = "current_clothing"
        private const val PREF_PREFER_FORMAL_LANGUAGE = "prefer_formal_language"
        private const val PREF_PREFER_SHORT_RESPONSES = "prefer_short_responses"
        private const val PREF_AVATAR_VISIBILITY = "avatar_visibility"
        private const val PREF_AVATAR_POSITION_X = "avatar_position_x"
        private const val PREF_AVATAR_POSITION_Y = "avatar_position_y"
        private const val PREF_AI_LEARNING_ENABLED = "ai_learning_enabled"
        private const val PREF_VOICE_ENABLED = "voice_enabled"
        private const val PREF_VOICE_SPEED = "voice_speed"
        private const val PREF_VOICE_PITCH = "voice_pitch"
        private const val PREF_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val PREF_CHAT_HISTORY_LIMIT = "chat_history_limit"
        private const val PREF_THEME_MODE = "theme_mode"
        private const val PREF_LANGUAGE = "language"
        private const val PREF_FIRST_RUN = "first_run"
        private const val PREF_TUTORIAL_COMPLETED = "tutorial_completed"
        private const val PREF_LAST_UPDATE_CHECK = "last_update_check"
        private const val PREF_APP_VERSION = "app_version"
    }
    
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    
    // Configuración de servicios
    fun shouldAutoStartServices(): Boolean {
        return sharedPreferences.getBoolean(PREF_AUTO_START_SERVICES, true)
    }
    
    fun setAutoStartServices(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(PREF_AUTO_START_SERVICES, enabled).apply()
    }
    
    fun isOverlayEnabled(): Boolean {
        return sharedPreferences.getBoolean(PREF_OVERLAY_ENABLED, true)
    }
    
    fun setOverlayEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(PREF_OVERLAY_ENABLED, enabled).apply()
    }
    
    // Modo especial
    fun isSpecialModeUnlocked(): Boolean {
        return sharedPreferences.getBoolean(PREF_SPECIAL_MODE_UNLOCKED, false)
    }
    
    fun setSpecialModeUnlocked(unlocked: Boolean) {
        sharedPreferences.edit().putBoolean(PREF_SPECIAL_MODE_UNLOCKED, unlocked).apply()
    }
    
    // Configuración del avatar
    fun getCurrentClothing(): String? {
        return sharedPreferences.getString(PREF_CURRENT_CLOTHING, null)
    }
    
    fun setCurrentClothing(clothingId: String) {
        sharedPreferences.edit().putString(PREF_CURRENT_CLOTHING, clothingId).apply()
    }
    
    fun isAvatarVisible(): Boolean {
        return sharedPreferences.getBoolean(PREF_AVATAR_VISIBILITY, true)
    }
    
    fun setAvatarVisible(visible: Boolean) {
        sharedPreferences.edit().putBoolean(PREF_AVATAR_VISIBILITY, visible).apply()
    }
    
    fun getAvatarPosition(): Pair<Float, Float> {
        val x = sharedPreferences.getFloat(PREF_AVATAR_POSITION_X, 0.5f)
        val y = sharedPreferences.getFloat(PREF_AVATAR_POSITION_Y, 0.5f)
        return Pair(x, y)
    }
    
    fun setAvatarPosition(x: Float, y: Float) {
        sharedPreferences.edit()
            .putFloat(PREF_AVATAR_POSITION_X, x)
            .putFloat(PREF_AVATAR_POSITION_Y, y)
            .apply()
    }
    
    // Configuración de IA
    fun isAILearningEnabled(): Boolean {
        return sharedPreferences.getBoolean(PREF_AI_LEARNING_ENABLED, true)
    }
    
    fun setAILearningEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(PREF_AI_LEARNING_ENABLED, enabled).apply()
    }
    
    fun preferFormalLanguage(): Boolean {
        return sharedPreferences.getBoolean(PREF_PREFER_FORMAL_LANGUAGE, false)
    }
    
    fun setPreferFormalLanguage(prefer: Boolean) {
        sharedPreferences.edit().putBoolean(PREF_PREFER_FORMAL_LANGUAGE, prefer).apply()
    }
    
    fun preferShortResponses(): Boolean {
        return sharedPreferences.getBoolean(PREF_PREFER_SHORT_RESPONSES, false)
    }
    
    fun setPreferShortResponses(prefer: Boolean) {
        sharedPreferences.edit().putBoolean(PREF_PREFER_SHORT_RESPONSES, prefer).apply()
    }
    
    // Configuración de voz
    fun isVoiceEnabled(): Boolean {
        return sharedPreferences.getBoolean(PREF_VOICE_ENABLED, true)
    }
    
    fun setVoiceEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(PREF_VOICE_ENABLED, enabled).apply()
    }
    
    fun getVoiceSpeed(): Float {
        return sharedPreferences.getFloat(PREF_VOICE_SPEED, 0.9f)
    }
    
    fun setVoiceSpeed(speed: Float) {
        sharedPreferences.edit().putFloat(PREF_VOICE_SPEED, speed).apply()
    }
    
    fun getVoicePitch(): Float {
        return sharedPreferences.getFloat(PREF_VOICE_PITCH, 1.1f)
    }
    
    fun setVoicePitch(pitch: Float) {
        sharedPreferences.edit().putFloat(PREF_VOICE_PITCH, pitch).apply()
    }
    
    // Configuración de notificaciones
    fun areNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(PREF_NOTIFICATIONS_ENABLED, true)
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(PREF_NOTIFICATIONS_ENABLED, enabled).apply()
    }
    
    // Configuración del chat
    fun getChatHistoryLimit(): Int {
        return sharedPreferences.getInt(PREF_CHAT_HISTORY_LIMIT, 100)
    }
    
    fun setChatHistoryLimit(limit: Int) {
        sharedPreferences.edit().putInt(PREF_CHAT_HISTORY_LIMIT, limit).apply()
    }
    
    // Configuración del tema
    fun getThemeMode(): String {
        return sharedPreferences.getString(PREF_THEME_MODE, "system") ?: "system"
    }
    
    fun setThemeMode(mode: String) {
        sharedPreferences.edit().putString(PREF_THEME_MODE, mode).apply()
    }
    
    // Configuración del idioma
    fun getLanguage(): String {
        return sharedPreferences.getString(PREF_LANGUAGE, "es") ?: "es"
    }
    
    fun setLanguage(language: String) {
        sharedPreferences.edit().putString(PREF_LANGUAGE, language).apply()
    }
    
    // Configuración de primera ejecución
    fun isFirstRun(): Boolean {
        return sharedPreferences.getBoolean(PREF_FIRST_RUN, true)
    }
    
    fun setFirstRun(firstRun: Boolean) {
        sharedPreferences.edit().putBoolean(PREF_FIRST_RUN, firstRun).apply()
    }
    
    fun isTutorialCompleted(): Boolean {
        return sharedPreferences.getBoolean(PREF_TUTORIAL_COMPLETED, false)
    }
    
    fun setTutorialCompleted(completed: Boolean) {
        sharedPreferences.edit().putBoolean(PREF_TUTORIAL_COMPLETED, completed).apply()
    }
    
    // Configuración de actualizaciones
    fun getLastUpdateCheck(): Long {
        return sharedPreferences.getLong(PREF_LAST_UPDATE_CHECK, 0L)
    }
    
    fun setLastUpdateCheck(timestamp: Long) {
        sharedPreferences.edit().putLong(PREF_LAST_UPDATE_CHECK, timestamp).apply()
    }
    
    fun getAppVersion(): String? {
        return sharedPreferences.getString(PREF_APP_VERSION, null)
    }
    
    fun setAppVersion(version: String) {
        sharedPreferences.edit().putString(PREF_APP_VERSION, version).apply()
    }
    
    // Métodos de utilidad
    fun clearAllPreferences() {
        sharedPreferences.edit().clear().apply()
    }
    
    fun clearAvatarPreferences() {
        sharedPreferences.edit()
            .remove(PREF_CURRENT_CLOTHING)
            .remove(PREF_AVATAR_VISIBILITY)
            .remove(PREF_AVATAR_POSITION_X)
            .remove(PREF_AVATAR_POSITION_Y)
            .apply()
    }
    
    fun clearAIPreferences() {
        sharedPreferences.edit()
            .remove(PREF_AI_LEARNING_ENABLED)
            .remove(PREF_PREFER_FORMAL_LANGUAGE)
            .remove(PREF_PREFER_SHORT_RESPONSES)
            .apply()
    }
    
    fun clearVoicePreferences() {
        sharedPreferences.edit()
            .remove(PREF_VOICE_ENABLED)
            .remove(PREF_VOICE_SPEED)
            .remove(PREF_VOICE_PITCH)
            .apply()
    }
    
    fun clearChatPreferences() {
        sharedPreferences.edit()
            .remove(PREF_CHAT_HISTORY_LIMIT)
            .apply()
    }
    
    fun clearThemePreferences() {
        sharedPreferences.edit()
            .remove(PREF_THEME_MODE)
            .apply()
    }
    
    fun clearLanguagePreferences() {
        sharedPreferences.edit()
            .remove(PREF_LANGUAGE)
            .apply()
    }
    
    fun clearTutorialPreferences() {
        sharedPreferences.edit()
            .remove(PREF_FIRST_RUN)
            .remove(PREF_TUTORIAL_COMPLETED)
            .apply()
    }
    
    fun clearUpdatePreferences() {
        sharedPreferences.edit()
            .remove(PREF_LAST_UPDATE_CHECK)
            .remove(PREF_APP_VERSION)
            .apply()
    }
    
    // Métodos para obtener todas las preferencias
    fun getAllPreferences(): Map<String, Any> {
        return mapOf(
            "auto_start_services" to shouldAutoStartServices(),
            "overlay_enabled" to isOverlayEnabled(),
            "special_mode_unlocked" to isSpecialModeUnlocked(),
            "current_clothing" to (getCurrentClothing() ?: ""),
            "prefer_formal_language" to preferFormalLanguage(),
            "prefer_short_responses" to preferShortResponses(),
            "avatar_visibility" to isAvatarVisible(),
            "avatar_position_x" to getAvatarPosition().first,
            "avatar_position_y" to getAvatarPosition().second,
            "ai_learning_enabled" to isAILearningEnabled(),
            "voice_enabled" to isVoiceEnabled(),
            "voice_speed" to getVoiceSpeed(),
            "voice_pitch" to getVoicePitch(),
            "notifications_enabled" to areNotificationsEnabled(),
            "chat_history_limit" to getChatHistoryLimit(),
            "theme_mode" to getThemeMode(),
            "language" to getLanguage(),
            "first_run" to isFirstRun(),
            "tutorial_completed" to isTutorialCompleted(),
            "last_update_check" to getLastUpdateCheck(),
            "app_version" to (getAppVersion() ?: "")
        )
    }
    
    fun setAllPreferences(preferences: Map<String, Any>) {
        val editor = sharedPreferences.edit()
        
        preferences.forEach { (key, value) ->
            when (value) {
                is Boolean -> editor.putBoolean(key, value)
                is String -> editor.putString(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Float -> editor.putFloat(key, value)
            }
        }
        
        editor.apply()
    }
    
    // Métodos para exportar/importar preferencias
    fun exportPreferences(): String {
        return getAllPreferences().toString()
    }
    
    fun importPreferences(preferencesString: String) {
        try {
            // Implementar parser de string a mapa
            // Por ahora solo un placeholder
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}