package com.ritsuai.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.ritsuai.utils.PreferenceManager

class RitsuAccessibilityService : AccessibilityService() {
    
    companion object {
        private const val TAG = "RitsuAccessibility"
    }
    
    private lateinit var preferenceManager: PreferenceManager
    
    override fun onCreate() {
        super.onCreate()
        preferenceManager = PreferenceManager(this)
        Log.d(TAG, "Servicio de accesibilidad creado")
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        
        serviceInfo = info
        preferenceManager.setAccessibilityEnabled(true)
        Log.d(TAG, "Servicio de accesibilidad conectado")
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                handleWindowStateChanged(event)
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                handleWindowContentChanged(event)
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                handleViewClicked(event)
            }
        }
    }
    
    private fun handleWindowStateChanged(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString()
        val className = event.className?.toString()
        
        Log.d(TAG, "Ventana cambiada: $packageName - $className")
        
        // Aquí puedes implementar lógica específica para diferentes apps
        when (packageName) {
            "com.whatsapp" -> handleWhatsAppEvent(event)
            "com.android.phone" -> handlePhoneEvent(event)
            "com.android.mms" -> handleSMSEvent(event)
        }
    }
    
    private fun handleWindowContentChanged(event: AccessibilityEvent) {
        // Manejar cambios en el contenido de la ventana
    }
    
    private fun handleViewClicked(event: AccessibilityEvent) {
        // Manejar clics en elementos
    }
    
    private fun handleWhatsAppEvent(event: AccessibilityEvent) {
        // Lógica específica para WhatsApp
        Log.d(TAG, "Evento de WhatsApp detectado")
    }
    
    private fun handlePhoneEvent(event: AccessibilityEvent) {
        // Lógica específica para llamadas
        Log.d(TAG, "Evento de teléfono detectado")
    }
    
    private fun handleSMSEvent(event: AccessibilityEvent) {
        // Lógica específica para SMS
        Log.d(TAG, "Evento de SMS detectado")
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "Servicio de accesibilidad interrumpido")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        preferenceManager.setAccessibilityEnabled(false)
        Log.d(TAG, "Servicio de accesibilidad destruido")
    }
    
    // Funciones para controlar otras aplicaciones
    fun openApp(packageName: String): Boolean {
        return try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error abriendo app: $packageName", e)
            false
        }
    }
    
    fun clickOnText(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        
        val nodes = rootNode.findAccessibilityNodeInfosByText(text)
        if (nodes.isNotEmpty()) {
            nodes[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
            return true
        }
        
        return false
    }
    
    fun typeText(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        
        // Buscar campo de texto activo
        val editTexts = rootNode.findAccessibilityNodeInfosByViewId("android:id/edit")
        if (editTexts.isNotEmpty()) {
            val editText = editTexts[0]
            editText.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            editText.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, text)
            return true
        }
        
        return false
    }
}