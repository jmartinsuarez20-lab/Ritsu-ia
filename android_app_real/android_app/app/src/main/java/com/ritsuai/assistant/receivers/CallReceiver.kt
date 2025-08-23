package com.ritsuai.assistant.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log

class CallReceiver : BroadcastReceiver() {
    
    companion object {
        private var lastState = TelephonyManager.CALL_STATE_IDLE
        private var callStartTime: Long = 0
        private var isIncoming = false
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.PHONE_STATE") {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            
            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    isIncoming = true
                    callStartTime = System.currentTimeMillis()
                    Log.d("CallReceiver", "Llamada entrante de: $incomingNumber")
                    
                    // Notificar a Ritsu sobre la llamada entrante
                    notifyRitsuIncomingCall(context, incomingNumber)
                }
                
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                        isIncoming = true
                        callStartTime = System.currentTimeMillis()
                        Log.d("CallReceiver", "Llamada respondida")
                    } else {
                        isIncoming = false
                        Log.d("CallReceiver", "Llamada saliente")
                    }
                }
                
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    if (lastState == TelephonyManager.CALL_STATE_OFFHOOK) {
                        val callDuration = System.currentTimeMillis() - callStartTime
                        Log.d("CallReceiver", "Llamada terminada. Duración: ${callDuration}ms")
                        
                        // Notificar a Ritsu sobre el fin de la llamada
                        notifyRitsuCallEnded(context, incomingNumber, callDuration)
                    }
                }
            }
            
            lastState = when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> TelephonyManager.CALL_STATE_RINGING
                TelephonyManager.EXTRA_STATE_OFFHOOK -> TelephonyManager.CALL_STATE_OFFHOOK
                else -> TelephonyManager.CALL_STATE_IDLE
            }
        }
    }
    
    private fun notifyRitsuIncomingCall(context: Context, number: String?) {
        // Enviar broadcast interno para que Ritsu se prepare
        val ritsuIntent = Intent("com.ritsuai.INCOMING_CALL")
        ritsuIntent.putExtra("caller_number", number)
        ritsuIntent.putExtra("timestamp", System.currentTimeMillis())
        context.sendBroadcast(ritsuIntent)
    }
    
    private fun notifyRitsuCallEnded(context: Context, number: String?, duration: Long) {
        // Enviar broadcast interno para que Ritsu registre la llamada
        val ritsuIntent = Intent("com.ritsuai.CALL_ENDED")
        ritsuIntent.putExtra("caller_number", number)
        ritsuIntent.putExtra("duration", duration)
        ritsuIntent.putExtra("was_incoming", isIncoming)
        context.sendBroadcast(ritsuIntent)
    }
}