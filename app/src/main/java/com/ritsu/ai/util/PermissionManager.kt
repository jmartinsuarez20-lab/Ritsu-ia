package com.ritsu.ai.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

class PermissionManager(private val context: Context) {
    
    companion object {
        // Permisos básicos del sistema
        private val BASIC_PERMISSIONS = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.FOREGROUND_SERVICE
        )
        
        // Permisos para control del sistema
        private val SYSTEM_PERMISSIONS = arrayOf(
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.QUERY_ALL_PACKAGES,
            Manifest.permission.PACKAGE_USAGE_STATS,
            Manifest.permission.GET_TASKS
        )
        
        // Permisos para comunicación
        private val COMMUNICATION_PERMISSIONS = arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG
        )
        
        // Permisos para archivos y multimedia
        private val STORAGE_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        
        // Permisos para calendario
        private val CALENDAR_PERMISSIONS = arrayOf(
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR
        )
        
        // Permisos para notificaciones
        private val NOTIFICATION_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyArray()
        }
        
        // Permisos para cámara y micrófono
        private val MEDIA_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
        )
        
        // Permisos para ubicación
        private val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        
        // Permisos para contactos
        private val CONTACT_PERMISSIONS = arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS
        )
        
        // Permisos para Bluetooth
        private val BLUETOOTH_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }
        
        // Permisos para WiFi
        private val WIFI_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
        )
    }
    
    /**
     * Obtiene todos los permisos requeridos para que Ritsu funcione completamente
     */
    fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf<String>()
        
        permissions.addAll(BASIC_PERMISSIONS)
        permissions.addAll(SYSTEM_PERMISSIONS)
        permissions.addAll(COMMUNICATION_PERMISSIONS)
        permissions.addAll(STORAGE_PERMISSIONS)
        permissions.addAll(CALENDAR_PERMISSIONS)
        permissions.addAll(NOTIFICATION_PERMISSIONS)
        permissions.addAll(MEDIA_PERMISSIONS)
        permissions.addAll(LOCATION_PERMISSIONS)
        permissions.addAll(CONTACT_PERMISSIONS)
        permissions.addAll(BLUETOOTH_PERMISSIONS)
        permissions.addAll(WIFI_PERMISSIONS)
        
        return permissions.distinct()
    }
    
    /**
     * Obtiene permisos por categoría
     */
    fun getPermissionsByCategory(): Map<String, List<String>> {
        return mapOf(
            "Básicos" to BASIC_PERMISSIONS.toList(),
            "Sistema" to SYSTEM_PERMISSIONS.toList(),
            "Comunicación" to COMMUNICATION_PERMISSIONS.toList(),
            "Almacenamiento" to STORAGE_PERMISSIONS.toList(),
            "Calendario" to CALENDAR_PERMISSIONS.toList(),
            "Notificaciones" to NOTIFICATION_PERMISSIONS.toList(),
            "Multimedia" to MEDIA_PERMISSIONS.toList(),
            "Ubicación" to LOCATION_PERMISSIONS.toList(),
            "Contactos" to CONTACT_PERMISSIONS.toList(),
            "Bluetooth" to BLUETOOTH_PERMISSIONS.toList(),
            "WiFi" to WIFI_PERMISSIONS.toList()
        )
    }
    
    /**
     * Verifica si se han concedido los permisos básicos
     */
    fun hasBasicPermissions(): Boolean {
        return BASIC_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Verifica si se han concedido los permisos del sistema
     */
    fun hasSystemPermissions(): Boolean {
        return SYSTEM_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Verifica si se han concedido los permisos de comunicación
     */
    fun hasCommunicationPermissions(): Boolean {
        return COMMUNICATION_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Verifica si se han concedido los permisos de almacenamiento
     */
    fun hasStoragePermissions(): Boolean {
        return STORAGE_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Verifica si se han concedido los permisos de calendario
     */
    fun hasCalendarPermissions(): Boolean {
        return CALENDAR_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Verifica si se han concedido los permisos de notificaciones
     */
    fun hasNotificationPermissions(): Boolean {
        return NOTIFICATION_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Verifica si se han concedido los permisos de multimedia
     */
    fun hasMediaPermissions(): Boolean {
        return MEDIA_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Verifica si se han concedido los permisos de ubicación
     */
    fun hasLocationPermissions(): Boolean {
        return LOCATION_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Verifica si se han concedido los permisos de contactos
     */
    fun hasContactPermissions(): Boolean {
        return CONTACT_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Verifica si se han concedido los permisos de Bluetooth
     */
    fun hasBluetoothPermissions(): Boolean {
        return BLUETOOTH_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Verifica si se han concedido los permisos de WiFi
     */
    fun hasWifiPermissions(): Boolean {
        return WIFI_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Verifica si se han concedido todos los permisos requeridos
     */
    fun hasAllPermissions(): Boolean {
        return getRequiredPermissions().all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Obtiene el estado de todos los permisos
     */
    fun getPermissionsStatus(): Map<String, Boolean> {
        val permissions = getRequiredPermissions()
        return permissions.associateWith { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Obtiene el estado de permisos por categoría
     */
    fun getPermissionsStatusByCategory(): Map<String, Map<String, Boolean>> {
        val categories = getPermissionsByCategory()
        return categories.mapValues { (_, permissions) ->
            permissions.associateWith { permission ->
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            }
        }
    }
    
    /**
     * Obtiene permisos pendientes
     */
    fun getPendingPermissions(): List<String> {
        return getRequiredPermissions().filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Obtiene permisos pendientes por categoría
     */
    fun getPendingPermissionsByCategory(): Map<String, List<String>> {
        val categories = getPermissionsByCategory()
        return categories.mapValues { (_, permissions) ->
            permissions.filter { permission ->
                ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
            }
        }
    }
    
    /**
     * Verifica si un permiso específico ha sido concedido
     */
    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Obtiene el porcentaje de permisos concedidos
     */
    fun getPermissionsGrantedPercentage(): Float {
        val totalPermissions = getRequiredPermissions().size
        val grantedPermissions = getRequiredPermissions().count { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
        return if (totalPermissions > 0) (grantedPermissions.toFloat() / totalPermissions) * 100 else 0f
    }
    
    /**
     * Obtiene permisos críticos (necesarios para funcionamiento básico)
     */
    fun getCriticalPermissions(): List<String> {
        return listOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.QUERY_ALL_PACKAGES
        )
    }
    
    /**
     * Verifica si se han concedido los permisos críticos
     */
    fun hasCriticalPermissions(): Boolean {
        return getCriticalPermissions().all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Obtiene permisos opcionales (no críticos pero útiles)
     */
    fun getOptionalPermissions(): List<String> {
        val allPermissions = getRequiredPermissions().toMutableList()
        allPermissions.removeAll(getCriticalPermissions())
        return allPermissions
    }
    
    /**
     * Verifica si se han concedido los permisos opcionales
     */
    fun hasOptionalPermissions(): Boolean {
        return getOptionalPermissions().all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}