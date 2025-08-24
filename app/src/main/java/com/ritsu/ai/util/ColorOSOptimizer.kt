package com.ritsu.ai.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ColorOSOptimizer(private val context: Context) {
    
    companion object {
        private const val TAG = "ColorOSOptimizer"
        private const val OPPO_MANUFACTURER = "OPPO"
        private const val COLOROS_VERSION_15 = "15"
        
        fun isColorOS15(): Boolean {
            return Build.MANUFACTURER.equals(OPPO_MANUFACTURER, ignoreCase = true) &&
                   Build.VERSION.RELEASE.contains(COLOROS_VERSION_15)
        }
        
        fun getColorOSVersion(): String {
            return try {
                val version = Settings.Secure.getString(
                    context.contentResolver,
                    "oppo_version"
                ) ?: Build.VERSION.RELEASE
                version
            } catch (e: Exception) {
                Build.VERSION.RELEASE
            }
        }
    }
    
    private val preferenceManager = PreferenceManager(context)
    private val scope = CoroutineScope(Dispatchers.Main)
    
    // Configuraciones específicas para ColorOS 15
    private val colorOS15Settings = mapOf(
        "auto_start_enabled" to true,
        "background_activity_enabled" to true,
        "battery_optimization_disabled" to true,
        "notification_access_enabled" to true,
        "accessibility_enabled" to true,
        "overlay_permission_enabled" to true,
        "usage_stats_enabled" to true,
        "install_unknown_sources" to true
    )
    
    fun optimizeForColorOS15() {
        if (!isColorOS15()) {
            Log.d(TAG, "No es ColorOS 15, saltando optimizaciones específicas")
            return
        }
        
        Log.d(TAG, "Aplicando optimizaciones específicas para ColorOS 15")
        
        // Configurar permisos específicos de ColorOS
        configureColorOSPermissions()
        
        // Configurar optimizaciones de batería
        configureBatteryOptimization()
        
        // Configurar auto-inicio
        configureAutoStart()
        
        // Configurar notificaciones
        configureNotifications()
        
        // Configurar accesibilidad
        configureAccessibility()
        
        // Configurar overlay
        configureOverlay()
        
        // Configurar estadísticas de uso
        configureUsageStats()
    }
    
    private fun configureColorOSPermissions() {
        try {
            // Habilitar configuración de permisos específicos de ColorOS
            preferenceManager.setColorOSOptimizationEnabled(true)
            
            Log.d(TAG, "Permisos de ColorOS 15 configurados")
        } catch (e: Exception) {
            Log.e(TAG, "Error al configurar permisos de ColorOS", e)
        }
    }
    
    private fun configureBatteryOptimization() {
        try {
            // Deshabilitar optimización de batería para la app
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:${context.packageName}")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            // También configurar en ajustes de ColorOS
            val colorOSIntent = Intent("oppo.intent.action.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS")
            colorOSIntent.putExtra("package_name", context.packageName)
            colorOSIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            Log.d(TAG, "Optimización de batería configurada para ColorOS 15")
        } catch (e: Exception) {
            Log.e(TAG, "Error al configurar optimización de batería", e)
        }
    }
    
    private fun configureAutoStart() {
        try {
            // Configurar auto-inicio específico de ColorOS
            val autoStartIntent = Intent("oppo.intent.action.REQUEST_AUTO_START")
            autoStartIntent.putExtra("package_name", context.packageName)
            autoStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            // También intentar con el intent genérico
            val genericIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            genericIntent.data = Uri.parse("package:${context.packageName}")
            genericIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            Log.d(TAG, "Auto-inicio configurado para ColorOS 15")
        } catch (e: Exception) {
            Log.e(TAG, "Error al configurar auto-inicio", e)
        }
    }
    
    private fun configureNotifications() {
        try {
            // Configurar acceso a notificaciones
            val notificationIntent = Intent("android.settings.NOTIFICATION_LISTENER_SETTINGS")
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            // Configuración específica de ColorOS para notificaciones
            val colorOSNotificationIntent = Intent("oppo.intent.action.NOTIFICATION_ACCESS_SETTINGS")
            colorOSNotificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            Log.d(TAG, "Notificaciones configuradas para ColorOS 15")
        } catch (e: Exception) {
            Log.e(TAG, "Error al configurar notificaciones", e)
        }
    }
    
    private fun configureAccessibility() {
        try {
            // Configurar servicio de accesibilidad
            val accessibilityIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            accessibilityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            Log.d(TAG, "Accesibilidad configurada para ColorOS 15")
        } catch (e: Exception) {
            Log.e(TAG, "Error al configurar accesibilidad", e)
        }
    }
    
    private fun configureOverlay() {
        try {
            // Configurar permisos de overlay
            val overlayIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            overlayIntent.data = Uri.parse("package:${context.packageName}")
            overlayIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            Log.d(TAG, "Overlay configurado para ColorOS 15")
        } catch (e: Exception) {
            Log.e(TAG, "Error al configurar overlay", e)
        }
    }
    
    private fun configureUsageStats() {
        try {
            // Configurar acceso a estadísticas de uso
            val usageStatsIntent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            usageStatsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            Log.d(TAG, "Estadísticas de uso configuradas para ColorOS 15")
        } catch (e: Exception) {
            Log.e(TAG, "Error al configurar estadísticas de uso", e)
        }
    }
    
    fun checkColorOSPermissions(): Map<String, Boolean> {
        val permissions = mutableMapOf<String, Boolean>()
        
        try {
            // Verificar permisos específicos de ColorOS
            permissions["auto_start"] = checkAutoStartPermission()
            permissions["battery_optimization"] = checkBatteryOptimizationPermission()
            permissions["notification_access"] = checkNotificationAccessPermission()
            permissions["accessibility"] = checkAccessibilityPermission()
            permissions["overlay"] = checkOverlayPermission()
            permissions["usage_stats"] = checkUsageStatsPermission()
            
            Log.d(TAG, "Estado de permisos de ColorOS 15: $permissions")
        } catch (e: Exception) {
            Log.e(TAG, "Error al verificar permisos de ColorOS", e)
        }
        
        return permissions
    }
    
    private fun checkAutoStartPermission(): Boolean {
        return try {
            // Verificar si la app tiene permiso de auto-inicio
            val autoStartEnabled = Settings.Secure.getInt(
                context.contentResolver,
                "auto_start_${context.packageName}",
                0
            ) == 1
            autoStartEnabled
        } catch (e: Exception) {
            false
        }
    }
    
    private fun checkBatteryOptimizationPermission(): Boolean {
        return try {
            // Verificar si la optimización de batería está deshabilitada
            val batteryOptimizationDisabled = Settings.Secure.getInt(
                context.contentResolver,
                "battery_optimization_${context.packageName}",
                1
            ) == 0
            batteryOptimizationDisabled
        } catch (e: Exception) {
            false
        }
    }
    
    private fun checkNotificationAccessPermission(): Boolean {
        return try {
            // Verificar acceso a notificaciones
            val notificationAccess = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            )?.contains(context.packageName) ?: false
            notificationAccess
        } catch (e: Exception) {
            false
        }
    }
    
    private fun checkAccessibilityPermission(): Boolean {
        return try {
            // Verificar servicio de accesibilidad
            val accessibilityEnabled = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED,
                0
            ) == 1
            accessibilityEnabled
        } catch (e: Exception) {
            false
        }
    }
    
    private fun checkOverlayPermission(): Boolean {
        return try {
            // Verificar permisos de overlay
            Settings.canDrawOverlays(context)
        } catch (e: Exception) {
            false
        }
    }
    
    private fun checkUsageStatsPermission(): Boolean {
        return try {
            // Verificar acceso a estadísticas de uso
            val usageStatsAccess = Settings.Secure.getInt(
                context.contentResolver,
                "usage_stats_enabled",
                0
            ) == 1
            usageStatsAccess
        } catch (e: Exception) {
            false
        }
    }
    
    fun requestColorOSPermissions() {
        scope.launch {
            try {
                Log.d(TAG, "Solicitando permisos específicos de ColorOS 15")
                
                // Solicitar permisos uno por uno con delays
                delay(1000)
                configureAutoStart()
                
                delay(1000)
                configureBatteryOptimization()
                
                delay(1000)
                configureNotifications()
                
                delay(1000)
                configureAccessibility()
                
                delay(1000)
                configureOverlay()
                
                delay(1000)
                configureUsageStats()
                
                Log.d(TAG, "Todos los permisos de ColorOS 15 solicitados")
            } catch (e: Exception) {
                Log.e(TAG, "Error al solicitar permisos de ColorOS", e)
            }
        }
    }
    
    fun getColorOSInfo(): Map<String, String> {
        return mapOf(
            "manufacturer" to Build.MANUFACTURER,
            "model" to Build.MODEL,
            "brand" to Build.BRAND,
            "product" to Build.PRODUCT,
            "device" to Build.DEVICE,
            "is_coloros_15" to isColorOS15().toString(),
            "coloros_version" to getColorOSVersion(),
            "android_version" to Build.VERSION.RELEASE,
            "sdk_version" to Build.VERSION.SDK_INT.toString(),
            "build_fingerprint" to Build.FINGERPRINT
        )
    }
    
    fun isInstallationBlocked(): Boolean {
        return try {
            // Verificar si la instalación está bloqueada por ColorOS
            val installationBlocked = Settings.Secure.getInt(
                context.contentResolver,
                "install_unknown_sources",
                0
            ) == 0
            installationBlocked
        } catch (e: Exception) {
            false
        }
    }
    
    fun enableUnknownSources() {
        try {
            // Habilitar instalación de fuentes desconocidas
            val unknownSourcesIntent = Intent(Settings.ACTION_SECURITY_SETTINGS)
            unknownSourcesIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            Log.d(TAG, "Fuentes desconocidas habilitadas para ColorOS 15")
        } catch (e: Exception) {
            Log.e(TAG, "Error al habilitar fuentes desconocidas", e)
        }
    }
}