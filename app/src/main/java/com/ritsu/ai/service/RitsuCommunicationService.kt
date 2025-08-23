package com.ritsu.ai.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ritsu.ai.R
import com.ritsu.ai.util.AIManager
import com.ritsu.ai.util.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RitsuCommunicationService : Service() {
    
    companion object {
        private const val TAG = "RitsuCommunication"
        private const val NOTIFICATION_ID = 1003
        private const val CHANNEL_ID = "ritsu_communication_channel"
        private var instance: RitsuCommunicationService? = null
        
        fun getInstance(): RitsuCommunicationService? = instance
    }
    
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var aiManager: AIManager
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        preferenceManager = PreferenceManager(this)
        aiManager = AIManager(this)
        
        createNotificationChannel()
        Log.d(TAG, "Servicio de comunicación creado")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.d(TAG, "Servicio de comunicación destruido")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Servicio de Comunicación",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Maneja WhatsApp, llamadas y mensajes"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Servicio de Comunicación")
            .setContentText("Maneja WhatsApp, llamadas y mensajes")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
    
    fun sendWhatsAppMessage(contactName: String, message: String): Boolean {
        return try {
            val accessibilityService = RitsuAccessibilityService.getInstance()
            if (accessibilityService != null) {
                // Abrir WhatsApp
                val success = accessibilityService.openApp("com.whatsapp")
                if (success) {
                    // Esperar a que WhatsApp se abra
                    serviceScope.launch {
                        kotlinx.coroutines.delay(2000)
                        
                        // Buscar el contacto
                        accessibilityService.clickOnText(contactName)
                        kotlinx.coroutines.delay(1000)
                        
                        // Hacer click en el campo de texto
                        accessibilityService.clickOnId("com.whatsapp:id/entry")
                        kotlinx.coroutines.delay(500)
                        
                        // Escribir el mensaje
                        accessibilityService.typeText(message)
                        kotlinx.coroutines.delay(500)
                        
                        // Enviar el mensaje
                        accessibilityService.clickOnId("com.whatsapp:id/send")
                        
                        Log.d(TAG, "Mensaje de WhatsApp enviado a $contactName")
                    }
                    true
                } else {
                    Log.w(TAG, "No se pudo abrir WhatsApp")
                    false
                }
            } else {
                Log.w(TAG, "Servicio de accesibilidad no disponible")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar mensaje de WhatsApp", e)
            false
        }
    }
    
    fun makeCall(contactName: String): Boolean {
        return try {
            val accessibilityService = RitsuAccessibilityService.getInstance()
            if (accessibilityService != null) {
                // Abrir la aplicación de teléfono
                val success = accessibilityService.openApp("com.android.dialer")
                if (success) {
                    serviceScope.launch {
                        kotlinx.coroutines.delay(2000)
                        
                        // Buscar el contacto
                        accessibilityService.clickOnText(contactName)
                        kotlinx.coroutines.delay(1000)
                        
                        // Hacer click en el botón de llamada
                        accessibilityService.clickOnId("com.android.dialer:id/call_button")
                        
                        Log.d(TAG, "Llamada iniciada a $contactName")
                    }
                    true
                } else {
                    Log.w(TAG, "No se pudo abrir la aplicación de teléfono")
                    false
                }
            } else {
                Log.w(TAG, "Servicio de accesibilidad no disponible")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al hacer llamada", e)
            false
        }
    }
    
    fun sendSMS(phoneNumber: String, message: String): Boolean {
        return try {
            val accessibilityService = RitsuAccessibilityService.getInstance()
            if (accessibilityService != null) {
                // Abrir la aplicación de mensajes
                val success = accessibilityService.openApp("com.android.mms")
                if (success) {
                    serviceScope.launch {
                        kotlinx.coroutines.delay(2000)
                        
                        // Hacer click en el botón de nuevo mensaje
                        accessibilityService.clickOnId("com.android.mms:id/fab")
                        kotlinx.coroutines.delay(1000)
                        
                        // Escribir el número de teléfono
                        accessibilityService.clickOnId("com.android.mms:id/recipients_editor")
                        accessibilityService.typeText(phoneNumber)
                        kotlinx.coroutines.delay(500)
                        
                        // Escribir el mensaje
                        accessibilityService.clickOnId("com.android.mms:id/embedded_text_editor")
                        accessibilityService.typeText(message)
                        kotlinx.coroutines.delay(500)
                        
                        // Enviar el mensaje
                        accessibilityService.clickOnId("com.android.mms:id/send_button")
                        
                        Log.d(TAG, "SMS enviado a $phoneNumber")
                    }
                    true
                } else {
                    Log.w(TAG, "No se pudo abrir la aplicación de mensajes")
                    false
                }
            } else {
                Log.w(TAG, "Servicio de accesibilidad no disponible")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar SMS", e)
            false
        }
    }
    
    fun readNotifications(): List<String> {
        return try {
            val accessibilityService = RitsuAccessibilityService.getInstance()
            if (accessibilityService != null) {
                val screenText = accessibilityService.getScreenText()
                // Procesar el texto para extraer notificaciones
                processNotificationsFromText(screenText)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al leer notificaciones", e)
            emptyList()
        }
    }
    
    private fun processNotificationsFromText(text: String): List<String> {
        // Implementación básica para extraer notificaciones del texto
        val notifications = mutableListOf<String>()
        val lines = text.split("\n")
        
        for (line in lines) {
            if (line.contains("notificación") || line.contains("mensaje") || line.contains("llamada")) {
                notifications.add(line.trim())
            }
        }
        
        return notifications
    }
    
    fun respondToNotification(notificationText: String, response: String): Boolean {
        return try {
            val accessibilityService = RitsuAccessibilityService.getInstance()
            if (accessibilityService != null) {
                // Buscar y hacer click en la notificación
                accessibilityService.clickOnText(notificationText)
                kotlinx.coroutines.delay(1000)
                
                // Escribir la respuesta
                accessibilityService.typeText(response)
                kotlinx.coroutines.delay(500)
                
                // Enviar la respuesta
                accessibilityService.clickOnText("Enviar")
                
                Log.d(TAG, "Respuesta enviada a notificación")
                true
            } else {
                Log.w(TAG, "Servicio de accesibilidad no disponible")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al responder a notificación", e)
            false
        }
    }
    
    fun getContactList(): List<String> {
        return try {
            // Implementación básica - en una aplicación real se usaría ContentResolver
            listOf("Juan", "María", "Pedro", "Ana", "Carlos")
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener lista de contactos", e)
            emptyList()
        }
    }
    
    fun searchContact(query: String): String? {
        return try {
            val contacts = getContactList()
            contacts.find { it.contains(query, ignoreCase = true) }
        } catch (e: Exception) {
            Log.e(TAG, "Error al buscar contacto", e)
            null
        }
    }
    
    fun speakDuringCall(text: String): Boolean {
        return try {
            // En una implementación real, esto usaría AudioManager para hablar durante la llamada
            aiManager.speak(text)
            Log.d(TAG, "Texto hablado durante la llamada: $text")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al hablar durante la llamada", e)
            false
        }
    }
}