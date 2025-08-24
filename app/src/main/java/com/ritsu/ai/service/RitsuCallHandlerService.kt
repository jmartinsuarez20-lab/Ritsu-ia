package com.ritsu.ai.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ritsu.ai.R
import com.ritsu.ai.util.AIManager
import com.ritsu.ai.util.PreferenceManager
import com.ritsu.ai.util.OPPOOptimizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class RitsuCallHandlerService : Service() {
    
    companion object {
        private const val TAG = "RitsuCallHandler"
        private const val NOTIFICATION_ID = 1004
        private const val CHANNEL_ID = "ritsu_call_handler_channel"
        private var instance: RitsuCallHandlerService? = null
        
        fun getInstance(): RitsuCallHandlerService? = instance
    }
    
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var aiManager: AIManager
    private lateinit var oppoOptimizer: OPPOOptimizer
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var audioManager: AudioManager
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    
    private var phoneStateListener: PhoneStateListener? = null
    private var isCallActive = AtomicBoolean(false)
    private var currentCallerNumber: String? = null
    private var currentCallerName: String? = null
    
    // Broadcast receiver para detectar llamadas entrantes
    private val callReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                TelephonyManager.ACTION_PHONE_STATE_CHANGED -> {
                    val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                    val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                    handlePhoneStateChange(state, number)
                }
                "android.intent.action.NEW_OUTGOING_CALL" -> {
                    val number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
                    handleOutgoingCall(number)
                }
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        preferenceManager = PreferenceManager(this)
        aiManager = AIManager(this)
        oppoOptimizer = OPPOOptimizer(this)
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        createNotificationChannel()
        setupPhoneStateListener()
        registerCallReceiver()
        
        Log.d(TAG, "Servicio de manejo de llamadas creado")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
        unregisterCallReceiver()
        removePhoneStateListener()
        Log.d(TAG, "Servicio de manejo de llamadas destruido")
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
                "Manejo de Llamadas",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Maneja llamadas entrantes automáticamente"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Manejo de Llamadas")
            .setContentText("Respondiendo automáticamente a llamadas")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
    
    private fun setupPhoneStateListener() {
        phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                when (state) {
                    TelephonyManager.CALL_STATE_RINGING -> {
                        Log.d(TAG, "Llamada entrante de: $phoneNumber")
                        handleIncomingCall(phoneNumber)
                    }
                    TelephonyManager.CALL_STATE_OFFHOOK -> {
                        Log.d(TAG, "Llamada contestada")
                        handleCallAnswered()
                    }
                    TelephonyManager.CALL_STATE_IDLE -> {
                        Log.d(TAG, "Llamada terminada")
                        handleCallEnded()
                    }
                }
            }
        }
        
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }
    
    private fun removePhoneStateListener() {
        phoneStateListener?.let {
            telephonyManager.listen(it, PhoneStateListener.LISTEN_NONE)
        }
    }
    
    private fun registerCallReceiver() {
        val filter = IntentFilter().apply {
            addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
            addAction("android.intent.action.NEW_OUTGOING_CALL")
        }
        registerReceiver(callReceiver, filter)
    }
    
    private fun unregisterCallReceiver() {
        try {
            unregisterReceiver(callReceiver)
        } catch (e: Exception) {
            Log.w(TAG, "Error al desregistrar receiver", e)
        }
    }
    
    private fun handlePhoneStateChange(state: String?, number: String?) {
        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                handleIncomingCall(number)
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                handleCallAnswered()
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                handleCallEnded()
            }
        }
    }
    
    private fun handleIncomingCall(phoneNumber: String?) {
        if (phoneNumber == null) return
        
        currentCallerNumber = phoneNumber
        currentCallerName = getContactName(phoneNumber)
        
        Log.d(TAG, "Llamada entrante de: $currentCallerName ($phoneNumber)")
        
        // Verificar si debemos responder automáticamente
        if (shouldAutoAnswer()) {
            serviceScope.launch {
                val delay = oppoOptimizer.getOptimalCallDelay()
                delay(delay.toLong()) // Usar delay optimizado para OPPO
                autoAnswerCall()
            }
        }
    }
    
    private fun handleCallAnswered() {
        if (isCallActive.get()) return
        
        isCallActive.set(true)
        Log.d(TAG, "Llamada contestada - iniciando respuesta automática")
        
        // Configurar audio optimizado para OPPO Reno 13 5G
        oppoOptimizer.applyCallOptimizations()
        
        // Generar y pronunciar respuesta automática
        serviceScope.launch {
            generateAndSpeakResponse()
        }
    }
    
    private fun handleCallEnded() {
        isCallActive.set(false)
        currentCallerNumber = null
        currentCallerName = null
        
        // Restaurar configuración de audio optimizada para OPPO
        oppoOptimizer.restoreAudioAfterCall()
        
        Log.d(TAG, "Llamada terminada")
    }
    
    private fun handleOutgoingCall(number: String?) {
        Log.d(TAG, "Llamada saliente a: $number")
    }
    
    private fun shouldAutoAnswer(): Boolean {
        // Verificar configuración del usuario
        return preferenceManager.getAutoAnswerEnabled() && 
               !preferenceManager.isInDoNotDisturbMode()
    }
    
    private fun autoAnswerCall() {
        try {
            // Para OPPO Reno 13 5G, usar el servicio de accesibilidad para contestar
            val accessibilityService = RitsuAccessibilityService.getInstance()
            if (accessibilityService != null) {
                // Buscar y hacer click en el botón de contestar
                accessibilityService.clickOnText("Contestar")
                delay(500)
                
                // Si no funciona, intentar con otros textos comunes
                if (!isCallActive.get()) {
                    accessibilityService.clickOnText("Answer")
                    delay(500)
                }
                
                if (!isCallActive.get()) {
                    accessibilityService.clickOnId("com.android.incallui:id/answer_button")
                    delay(500)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al contestar llamada automáticamente", e)
        }
    }
    

    
    private suspend fun generateAndSpeakResponse() {
        try {
            val callerName = currentCallerName ?: "desconocido"
            val callerNumber = currentCallerNumber ?: return
            
            // Generar respuesta contextual
            val response = generateContextualResponse(callerName, callerNumber)
            
            // Hablar la respuesta
            speakResponse(response)
            
            // Continuar la conversación si es necesario
            if (preferenceManager.getEnableConversationMode()) {
                startConversationMode()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al generar y pronunciar respuesta", e)
        }
    }
    
    private fun generateContextualResponse(callerName: String, callerNumber: String): String {
        val timeOfDay = getTimeOfDay()
        val isContact = isKnownContact(callerNumber)
        
        return when {
            isContact -> {
                when (timeOfDay) {
                    "morning" -> "¡Hola $callerName! Buenos días, ¿cómo estás?"
                    "afternoon" -> "¡Hola $callerName! Buenas tardes, ¿qué tal va tu día?"
                    "evening" -> "¡Hola $callerName! Buenas noches, ¿cómo va todo?"
                    else -> "¡Hola $callerName! ¿En qué puedo ayudarte?"
                }
            }
            else -> {
                when (timeOfDay) {
                    "morning" -> "Buenos días, soy el asistente de ${preferenceManager.getUserName()}. ¿En qué puedo ayudarle?"
                    "afternoon" -> "Buenas tardes, soy el asistente de ${preferenceManager.getUserName()}. ¿En qué puedo ayudarle?"
                    "evening" -> "Buenas noches, soy el asistente de ${preferenceManager.getUserName()}. ¿En qué puedo ayudarle?"
                    else -> "Hola, soy el asistente de ${preferenceManager.getUserName()}. ¿En qué puedo ayudarle?"
                }
            }
        }
    }
    
    private fun getTimeOfDay(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "morning"
            hour < 18 -> "afternoon"
            else -> "evening"
        }
    }
    
    private fun isKnownContact(phoneNumber: String): Boolean {
        // Implementar verificación de contactos
        return getContactName(phoneNumber) != phoneNumber
    }
    
    private fun getContactName(phoneNumber: String): String {
        // Implementar búsqueda en contactos
        // Por ahora retorna el número
        return phoneNumber
    }
    
    private fun speakResponse(response: String) {
        try {
            // Usar el AIManager optimizado para OPPO para pronunciar la respuesta
            if (OPPOOptimizer.isOPPOReno13()) {
                aiManager.speakDuringCall(response)
            } else {
                aiManager.speak(response)
            }
            Log.d(TAG, "Respuesta pronunciada: $response")
        } catch (e: Exception) {
            Log.e(TAG, "Error al pronunciar respuesta", e)
        }
    }
    
    private suspend fun startConversationMode() {
        try {
            // Modo conversación continua
            while (isCallActive.get()) {
                delay(5000) // Esperar 5 segundos entre interacciones
                
                if (isCallActive.get()) {
                    // Generar respuesta contextual basada en el contexto de la llamada
                    val contextualResponse = generateFollowUpResponse()
                    speakResponse(contextualResponse)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en modo conversación", e)
        }
    }
    
    private fun generateFollowUpResponse(): String {
        // Generar respuestas de seguimiento basadas en el contexto
        val responses = listOf(
            "Entiendo perfectamente, ¿hay algo más en lo que pueda ayudarte?",
            "Perfecto, ¿necesitas que tome alguna nota o recuerde algo importante?",
            "Excelente, ¿te parece bien si continuamos con eso?",
            "Muy bien, ¿hay algún detalle adicional que deba considerar?"
        )
        
        return responses.random()
    }
    
    // Métodos públicos para control externo
    fun enableAutoAnswer(enabled: Boolean) {
        preferenceManager.setAutoAnswerEnabled(enabled)
    }
    
    fun enableConversationMode(enabled: Boolean) {
        preferenceManager.setEnableConversationMode(enabled)
    }
    
    fun setUserName(name: String) {
        preferenceManager.setUserName(name)
    }
}