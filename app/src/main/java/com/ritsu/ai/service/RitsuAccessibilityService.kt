package com.ritsu.ai.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.ritsu.ai.RitsuApplication
import com.ritsu.ai.util.AIManager
import com.ritsu.ai.util.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RitsuAccessibilityService : AccessibilityService() {
    
    companion object {
        private const val TAG = "RitsuAccessibility"
        private var instance: RitsuAccessibilityService? = null
        
        fun getInstance(): RitsuAccessibilityService? = instance
    }
    
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var aiManager: AIManager
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        preferenceManager = PreferenceManager(this)
        aiManager = AIManager(this)
        Log.d(TAG, "Servicio de accesibilidad creado")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.d(TAG, "Servicio de accesibilidad destruido")
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
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                handleViewTextChanged(event)
            }
        }
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "Servicio interrumpido")
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY
            notificationTimeout = 100
        }
        serviceInfo = info
        Log.d(TAG, "Servicio de accesibilidad conectado")
    }
    
    private fun handleWindowStateChanged(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        val className = event.className?.toString() ?: return
        
        Log.d(TAG, "Ventana cambiada: $packageName/$className")
        
        // Aprender sobre el uso de aplicaciones
        serviceScope.launch {
            aiManager.learnFromAppUsage(packageName, className)
        }
    }
    
    private fun handleWindowContentChanged(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        
        // Procesar cambios de contenido para detectar patrones
        serviceScope.launch {
            aiManager.processContentChange(packageName, event)
        }
    }
    
    private fun handleViewClicked(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        val className = event.className?.toString() ?: return
        
        Log.d(TAG, "Vista clickeada: $packageName/$className")
        
        // Aprender sobre interacciones del usuario
        serviceScope.launch {
            aiManager.learnFromUserInteraction(packageName, className, "click")
        }
    }
    
    private fun handleViewTextChanged(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        val text = event.text?.joinToString(" ") ?: return
        
        Log.d(TAG, "Texto cambiado en $packageName: $text")
        
        // Procesar texto para comandos de voz
        serviceScope.launch {
            aiManager.processTextInput(packageName, text)
        }
    }
    
    // Métodos para controlar aplicaciones
    fun openApp(packageName: String): Boolean {
        return try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                Log.d(TAG, "Aplicación abierta: $packageName")
                true
            } else {
                Log.w(TAG, "No se pudo abrir la aplicación: $packageName")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al abrir aplicación: $packageName", e)
            false
        }
    }
    
    fun clickOnText(text: String): Boolean {
        return try {
            val rootNode = rootInActiveWindow ?: return false
            val nodes = rootNode.findAccessibilityNodeInfosByText(text)
            
            if (nodes.isNotEmpty()) {
                nodes.first().performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d(TAG, "Click en texto: $text")
                true
            } else {
                Log.w(TAG, "No se encontró el texto: $text")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al hacer click en texto: $text", e)
            false
        }
    }
    
    fun clickOnId(resourceId: String): Boolean {
        return try {
            val rootNode = rootInActiveWindow ?: return false
            val nodes = rootNode.findAccessibilityNodeInfosByViewId(resourceId)
            
            if (nodes.isNotEmpty()) {
                nodes.first().performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d(TAG, "Click en ID: $resourceId")
                true
            } else {
                Log.w(TAG, "No se encontró el ID: $resourceId")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al hacer click en ID: $resourceId", e)
            false
        }
    }
    
    fun typeText(text: String): Boolean {
        return try {
            val rootNode = rootInActiveWindow ?: return false
            val focusedNode = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            
            if (focusedNode != null) {
                val arguments = Bundle()
                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
                focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                Log.d(TAG, "Texto escrito: $text")
                true
            } else {
                Log.w(TAG, "No se encontró un campo de texto enfocado")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al escribir texto: $text", e)
            false
        }
    }
    
    fun scroll(direction: Int): Boolean {
        return try {
            val rootNode = rootInActiveWindow ?: return false
            val scrollableNode = findScrollableNode(rootNode)
            
            if (scrollableNode != null) {
                scrollableNode.performAction(direction)
                Log.d(TAG, "Scroll realizado: $direction")
                true
            } else {
                Log.w(TAG, "No se encontró un elemento scrolleable")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al hacer scroll: $direction", e)
            false
        }
    }
    
    private fun findScrollableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isScrollable) {
            return node
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val scrollable = findScrollableNode(child)
            if (scrollable != null) {
                return scrollable
            }
        }
        
        return null
    }
    
    fun getCurrentAppInfo(): Pair<String, String>? {
        return try {
            val rootNode = rootInActiveWindow ?: return null
            val packageName = rootNode.packageName?.toString() ?: return null
            val className = rootNode.className?.toString() ?: return null
            Pair(packageName, className)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener información de la aplicación actual", e)
            null
        }
    }
    
    fun getScreenText(): String {
        return try {
            val rootNode = rootInActiveWindow ?: return ""
            extractTextFromNode(rootNode)
        } catch (e: Exception) {
            Log.e(TAG, "Error al extraer texto de la pantalla", e)
            ""
        }
    }
    
    private fun extractTextFromNode(node: AccessibilityNodeInfo): String {
        val text = StringBuilder()
        
        // Agregar texto del nodo actual
        node.text?.let { text.append(it).append(" ") }
        node.contentDescription?.let { text.append(it).append(" ") }
        
        // Recursivamente procesar nodos hijos
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            text.append(extractTextFromNode(child))
        }
        
        return text.toString().trim()
    }
}