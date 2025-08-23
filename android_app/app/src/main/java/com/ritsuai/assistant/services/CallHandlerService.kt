package com.ritsuai.assistant.services

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.telecom.Call
import android.telecom.InCallService
import android.util.Log
import com.ritsuai.assistant.core.RitsuAI
import com.ritsuai.assistant.core.CallAction

class CallHandlerService : InCallService() {
    
    private var ritsuAI: RitsuAI? = null
    private var ritsuService: RitsuAIService? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as RitsuAIService.RitsuBinder
            ritsuService = binder.getService()
            ritsuAI = ritsuService?.getRitsuAI()
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            ritsuService = null
            ritsuAI = null
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Conectar con el servicio de IA
        val intent = Intent(this, RitsuAIService::class.java)
        bindService(intent, serviceConnection, BIND_AUTO_CREATE)
    }
    
    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        
        Log.d("CallHandler", "Nueva llamada entrante - Ritsu tomará control total")
        
        // Obtener información del caller
        val callerNumber = call.details.handle?.schemeSpecificPart
        val callerName = getCallerName(callerNumber)
        
        // Configurar callback para manejar el estado de la llamada
        call.registerCallback(object : Call.Callback() {
            override fun onStateChanged(call: Call, state: Int) {
                handleIntelligentCallState(call, state, callerNumber, callerName)
            }
        })
        
        // NUEVO: Análisis inteligente de la llamada
        if (call.state == Call.STATE_RINGING) {
            handleIncomingCallIntelligently(call, callerNumber, callerName)
        }
    }
    
    /**
     * MANEJO INTELIGENTE DE LLAMADAS ENTRANTES
     * Ritsu decide automáticamente cómo manejar cada llamada
     */
    private fun handleIncomingCallIntelligently(call: Call, callerNumber: String?, callerName: String?) {
        ritsuAI?.let { ai ->
            // Obtener el controlador de teléfono para análisis
            val phoneController = ai.getPhoneController()
            val callAction = phoneController?.handleIncomingCall(
                callerNumber ?: "unknown", 
                callerName
            ) ?: CallAction.ANSWER_PROFESSIONALLY
            
            // Ejecutar acción basada en el análisis inteligente
            when (callAction) {
                CallAction.ANSWER_IMMEDIATELY -> {
                    Log.d("CallHandler", "Llamada de pareja - Respondiendo inmediatamente")
                    answerCallImmediate(call, callerNumber, callerName)
                }
                CallAction.ANSWER_WITH_GREETING -> {
                    Log.d("CallHandler", "Contacto importante - Respuesta con saludo especial")
                    answerCallWithGreeting(call, callerNumber, callerName)
                }
                CallAction.ANSWER_PROFESSIONALLY -> {
                    Log.d("CallHandler", "Llamada profesional - Respuesta formal")
                    answerCallProfessionally(call, callerNumber, callerName)
                }
                CallAction.DECLINE_POLITELY -> {
                    Log.d("CallHandler", "Spam detectado - Rechazando educadamente")
                    declineCallPolitely(call, callerNumber)
                }
                CallAction.SEND_TO_VOICEMAIL -> {
                    Log.d("CallHandler", "Enviando a buzón de voz")
                    sendToVoicemail(call)
                }
            }
        }
    }
    
    private fun handleIntelligentCallState(call: Call, state: Int, callerNumber: String?, callerName: String?) {
        when (state) {
            Call.STATE_ACTIVE -> {
                Log.d("CallHandler", "Llamada activa - Ritsu iniciando conversación inteligente")
                startIntelligentConversation(call, callerNumber, callerName)
            }
            Call.STATE_DISCONNECTED -> {
                Log.d("CallHandler", "Llamada terminada - Ritsu procesando información")
                endIntelligentConversation(callerNumber, callerName)
            }
        }
    }
    
    private fun answerCallImmediate(call: Call, callerNumber: String?, callerName: String?) {
        call.answer(android.telecom.VideoProfile.STATE_AUDIO_ONLY)
        Log.d("CallHandler", "Ritsu respondió inmediatamente a: $callerName")
    }
    
    private fun answerCallWithGreeting(call: Call, callerNumber: String?, callerName: String?) {
        call.answer(android.telecom.VideoProfile.STATE_AUDIO_ONLY)
        // Delay pequeño para saludo especial
        Thread.sleep(500)
        Log.d("CallHandler", "Ritsu respondió con saludo especial a: $callerName")
    }
    
    private fun answerCallProfessionally(call: Call, callerNumber: String?, callerName: String?) {
        call.answer(android.telecom.VideoProfile.STATE_AUDIO_ONLY)
        Log.d("CallHandler", "Ritsu respondió profesionalmente a: $callerName")
    }
    
    private fun declineCallPolitely(call: Call, callerNumber: String?) {
        call.reject(false, "")
        Log.d("CallHandler", "Ritsu rechazó educadamente llamada de: $callerNumber")
    }
    
    private fun sendToVoicemail(call: Call) {
        call.reject(false, "")
        Log.d("CallHandler", "Llamada enviada a buzón de voz")
    }
    
    private fun startIntelligentConversation(call: Call, callerNumber: String?, callerName: String?) {
        ritsuAI?.let { ai ->
            // Saludo inicial inteligente basado en el tipo de contacto
            val greeting = ai.processMessage("Nueva llamada", callerNumber)
            
            Log.d("CallHandler", "Ritsu inició conversación: $greeting")
            
            // TODO: Implementar reconocimiento de voz en tiempo real
            // TODO: Implementar respuesta de voz inteligente
            // TODO: Implementar control de flujo de conversación
        }
    }
    
    private fun endIntelligentConversation(callerNumber: String?, callerName: String?) {
        ritsuAI?.let { ai ->
            // Despedida inteligente y aprendizaje de la conversación
            val farewell = ai.processMessage("Terminar llamada", callerNumber)
            Log.d("CallHandler", "Ritsu terminó conversación: $farewell")
            
            // Guardar información de la llamada para aprendizaje
            ai.learnFromCall(callerNumber, callerName)
        }
    }
    
    private fun getCallerName(phoneNumber: String?): String? {
        // TODO: Implementar obtención de nombre desde contactos
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }
}