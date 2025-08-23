package com.ritsuai.assistant.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class WhatsAppAccessibilityService : AccessibilityService() {
    
    companion object {
        private const val WHATSAPP_PACKAGE = "com.whatsapp"
        private const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let { 
            handleWhatsAppEvent(it)
        }
    }
    
    private fun handleWhatsAppEvent(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString()
        
        if (packageName == WHATSAPP_PACKAGE || packageName == WHATSAPP_BUSINESS_PACKAGE) {
            when (event.eventType) {
                AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                    handleIncomingMessage(event)
                }
                
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                    if (isInChatWindow(event)) {
                        handleChatWindowChange(event)
                    }
                }
            }
        }
    }
    
    private fun handleIncomingMessage(event: AccessibilityEvent) {
        val text = event.text?.toString() ?: return
        
        Log.d("WhatsAppService", "Nuevo mensaje de WhatsApp: $text")
        
        // Extraer información del mensaje
        val messageInfo = parseWhatsAppMessage(text)
        
        if (messageInfo != null) {
            // Procesar con Ritsu IA
            processMessageWithRitsu(messageInfo)
        }
    }
    
    private fun handleChatWindowChange(event: AccessibilityEvent) {
        // Detectar cuando se abre una conversación de WhatsApp
        // para estar listo para responder automáticamente
        
        val rootNode = rootInActiveWindow ?: return
        
        // Buscar el campo de texto para escribir
        val messageField = findMessageInputField(rootNode)
        
        if (messageField != null) {
            Log.d("WhatsAppService", "Campo de mensaje encontrado, Ritsu lista para responder")
        }
    }
    
    private fun isInChatWindow(event: AccessibilityEvent): Boolean {
        // Verificar si estamos en una ventana de chat de WhatsApp
        val rootNode = rootInActiveWindow ?: return false
        
        return findNodeWithText(rootNode, "Escribe un mensaje") != null ||
               findNodeWithText(rootNode, "Type a message") != null
    }
    
    private fun parseWhatsAppMessage(text: String): MessageInfo? {
        // Parsear el texto de notificación de WhatsApp para extraer:
        // - Nombre del contacto
        // - Contenido del mensaje
        // - Timestamp
        
        val lines = text.split("\n")
        if (lines.size >= 2) {
            return MessageInfo(
                sender = lines[0],
                message = lines[1],
                timestamp = System.currentTimeMillis()
            )
        }
        
        return null
    }
    
    private fun processMessageWithRitsu(messageInfo: MessageInfo) {
        // Aquí integraríamos con RitsuAI para generar una respuesta
        // y luego enviarla automáticamente a través de WhatsApp
        
        Log.d("WhatsAppService", "Procesando mensaje de ${messageInfo.sender}: ${messageInfo.message}")
        
        // TODO: Integrar con RitsuAI para generar respuesta inteligente
        // val response = ritsuAI.processMessage(messageInfo.message, messageInfo.sender)
        // sendWhatsAppMessage(response)
    }
    
    private fun sendWhatsAppMessage(message: String) {
        val rootNode = rootInActiveWindow ?: return
        
        // Encontrar el campo de texto
        val messageField = findMessageInputField(rootNode)
        
        if (messageField != null && messageField.isEditable) {
            // Escribir el mensaje
            val arguments = android.os.Bundle()
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, message)
            messageField.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            
            // Buscar y presionar el botón de enviar
            val sendButton = findSendButton(rootNode)
            sendButton?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            
            Log.d("WhatsAppService", "Ritsu envió mensaje: $message")
        }
    }
    
    private fun findMessageInputField(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        // Buscar recursivamente el campo de entrada de texto
        if (node.isEditable && node.text != null) {
            return node
        }
        
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                val result = findMessageInputField(child)
                if (result != null) return result
            }
        }
        
        return null
    }
    
    private fun findSendButton(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        // Buscar el botón de enviar (generalmente tiene un ícono de avión o dice "Send")
        if (node.isClickable && 
            (node.contentDescription?.contains("Send") == true ||
             node.contentDescription?.contains("Enviar") == true)) {
            return node
        }
        
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                val result = findSendButton(child)
                if (result != null) return result
            }
        }
        
        return null
    }
    
    private fun findNodeWithText(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        if (node.text?.contains(text, ignoreCase = true) == true) {
            return node
        }
        
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                val result = findNodeWithText(child, text)
                if (result != null) return result
            }
        }
        
        return null
    }
    
    override fun onInterrupt() {
        Log.d("WhatsAppService", "Servicio de accesibilidad interrumpido")
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("WhatsAppService", "Servicio de WhatsApp conectado - Ritsu lista para mensajes")
    }
    
    data class MessageInfo(
        val sender: String,
        val message: String,
        val timestamp: Long
    )
}