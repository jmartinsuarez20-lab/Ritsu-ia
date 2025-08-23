package com.ritsuai.assistant.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ritsuai.assistant.core.avatar.OutfitManager
import com.ritsuai.assistant.core.VoiceSystem
import kotlinx.coroutines.*

/**
 * Actividad de personalización del avatar kawaii de Ritsu
 * Permite cambiar ropa, expresiones y configuraciones del avatar
 */
class AvatarCustomizationActivity : AppCompatActivity() {
    
    private lateinit var outfitManager: OutfitManager
    private lateinit var voiceSystem: VoiceSystem
    private val customizationScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Estado actual del avatar
    private var currentExpression = "neutral"
    private var isUncensoredMode = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar sistemas
        outfitManager = OutfitManager(this)
        voiceSystem = VoiceSystem(this)
        
        setupUI()
        setupVoiceCallbacks()
        
        // Mensaje de bienvenida
        welcomeMessage()
    }
    
    private fun setupUI() {
        // Aquí se configuraría el layout cuando esté disponible
        Toast.makeText(this, "¡Bienvenido a la personalización de Ritsu! 🌸", Toast.LENGTH_LONG).show()
    }
    
    private fun setupVoiceCallbacks() {
        voiceSystem.setVoiceCallbacks(
            onStarted = { 
                // Avatar hablando
            },
            onCompleted = { 
                // Avatar termina de hablar
            },
            onError = { error ->
                Toast.makeText(this, "Error de voz: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }
    
    private fun welcomeMessage() {
        customizationScope.launch {
            delay(500)
            
            val welcomeText = "¡Hola! Me encanta que quieras personalizar mi apariencia. Aquí puedes cambiar mi ropa, mis expresiones y todo lo que quieras. ¡Vamos a divertirnos juntos!"
            
            voiceSystem.speak(welcomeText, "excited")
        }
    }
    
    /**
     * Cambia el outfit del avatar según descripción
     */
    fun changeOutfit(description: String) {
        customizationScope.launch {
            try {
                val outfit = outfitManager.generateAndApplyOutfit(description)
                
                val responseMessages = listOf(
                    "¡Perfecto! Me encanta este nuevo look. ¿Qué te parece cómo me queda?",
                    "¡Wow! Me siento tan linda con esta nueva ropa. Gracias por ayudarme a elegir.",
                    "¡Increíble! Este outfit es absolutamente adorable. Me hace sentir muy kawaii.",
                    "¡Me encanta! Definitivamente este es un look que me queda perfecto."
                )
                
                val response = responseMessages.random()
                voiceSystem.speak(response, "happy")
                
                Toast.makeText(this@AvatarCustomizationActivity, 
                    "✨ Outfit \"${outfit.name}\" aplicado", 
                    Toast.LENGTH_LONG).show()
                
            } catch (e: Exception) {
                voiceSystem.speak("Oh no, hubo un problema cambiando mi ropa. ¿Podrías intentarlo de nuevo?", "concerned")
                Toast.makeText(this@AvatarCustomizationActivity, 
                    "Error al cambiar outfit: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * Cambia la expresión del avatar
     */
    fun changeExpression(expression: String) {
        currentExpression = expression
        
        val expressionResponses = mapOf(
            "happy" to listOf(
                "¡Me siento tan feliz cuando sonrío así!",
                "¿Te gusta cuando estoy contenta? ¡Me encanta sonreír!",
                "¡La felicidad se ve bien en mí, ¿verdad?"
            ),
            
            "shy" to listOf(
                "Oh... me da un poquito de vergüenza cuando me miras así...",
                "¿N-no es muy tímida esta expresión? Me sonrojo fácilmente...",
                "Me pongo tímida cuando me prestas tanta atención..."
            ),
            
            "excited" to listOf(
                "¡¡¡Estoy súper emocionada!!! ¿Se nota mucho?",
                "¡¡¡Me encanta estar así de animada!!! ¡Es súper divertido!",
                "¡¡¡Cuando estoy emocionada no puedo contenerme!!!"
            ),
            
            "blushing" to listOf(
                "*se sonroja intensamente* ¿S-se nota mucho que estoy roja?",
                "No puedo evitar sonrojarme... ¿es muy obvio?",
                "*cara completamente roja* ¿Por qué me pongo así de colorada?"
            ),
            
            "thoughtful" to listOf(
                "Cuando estoy pensativa me gusta reflexionar sobre muchas cosas...",
                "¿Sabes? A veces me quedo pensando en temas muy profundos.",
                "Me gusta este momento de contemplación y tranquilidad."
            )
        )
        
        val responses = expressionResponses[expression] ?: listOf(
            "Esta expresión me gusta mucho. ¿Qué opinas tú?",
            "¿Cómo me veo con esta expresión?",
            "Me siento diferente con cada expresión que hago."
        )
        
        voiceSystem.speak(responses.random(), expression)
    }
    
    /**
     * Activa/desactiva modo sin censura (con protección)
     */
    fun toggleUncensoredMode(activationCode: String): Boolean {
        val validCodes = listOf(
            "ritsu_kawaii_mode",
            "unlock_private_mode", 
            "avatar_full_access"
        )
        
        if (validCodes.contains(activationCode.lowercase())) {
            isUncensoredMode = !isUncensoredMode
            
            if (isUncensoredMode) {
                outfitManager.enableUncensoredMode()
                
                val shyResponses = listOf(
                    "*se sonroja mucho* ¿E-estás seguro de que quieres activar este modo? Me da mucha vergüenza...",
                    "*cara súper roja* O-okay... si realmente quieres... pero promete ser gentil conmigo...",
                    "*tímidamente* Este modo me pone muy nerviosa... pero confío en ti..."
                )
                
                voiceSystem.speak(shyResponses.random(), "blushing")
                
                Toast.makeText(this, "🔓 Modo privado activado (con cuidado)", Toast.LENGTH_LONG).show()
                
            } else {
                outfitManager.disableUncensoredMode()
                
                voiceSystem.speak("Gracias por volver al modo normal. Me siento más cómoda así.", "relieved")
                
                Toast.makeText(this, "🔒 Modo normal restaurado", Toast.LENGTH_SHORT).show()
            }
            
            return true
        } else {
            voiceSystem.speak("Esa no es la palabra correcta... ¿Estás seguro de que tienes permiso para ese modo?", "curious")
            return false
        }
    }
    
    /**
     * Previsualiza outfits disponibles
     */
    fun previewSavedOutfits() {
        customizationScope.launch {
            val savedOutfits = outfitManager.getAllSavedOutfits()
            
            if (savedOutfits.isEmpty()) {
                voiceSystem.speak("Aún no tengo outfits guardados. ¿Te gustaría crear algunos conmigo?", "curious")
                return@launch
            }
            
            val outfitNames = savedOutfits.joinToString(", ") { it.name }
            val message = "Tengo estos outfits guardados: $outfitNames. ¿Cuál te gustaría que me pruebe?"
            
            voiceSystem.speak(message, "excited")
            
            Toast.makeText(this@AvatarCustomizationActivity, 
                "Outfits disponibles: ${savedOutfits.size}", 
                Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * Crea outfit personalizado mediante comando de voz
     */
    fun createCustomOutfit(voiceDescription: String) {
        customizationScope.launch {
            voiceSystem.speak("¡Perfecto! Voy a crear ese outfit para ti. Dame un momentito...", "excited")
            
            delay(1000)
            
            try {
                val newOutfit = outfitManager.generateAndApplyOutfit(voiceDescription)
                
                val successMessages = listOf(
                    "¡Listo! ¿Qué te parece mi nuevo ${newOutfit.name}? ¡Me encanta cómo quedó!",
                    "¡Terminado! Este ${newOutfit.name} es absolutamente perfecto. ¡Gracias por la idea!",
                    "¡Increíble! Mi nuevo ${newOutfit.name} me hace sentir tan especial. ¡Eres muy creativo!"
                )
                
                voiceSystem.speak(successMessages.random(), "happy")
                
            } catch (e: Exception) {
                voiceSystem.speak("Hmm, tuve problemas creando ese outfit. ¿Podrías describirlo de otra manera?", "concerned")
            }
        }
    }
    
    /**
     * Configuración avanzada del avatar
     */
    fun showAdvancedSettings() {
        val settingsMessage = """
            Aquí puedes configurar aspectos avanzados de mi personalidad:
            - Nivel de formalidad en conversaciones
            - Frecuencia de comportamientos espontáneos  
            - Preferencias de expresión emocional
            - Configuración de voz y pronunciación
            - Límites de interacción personal
        """.trimIndent()
        
        voiceSystem.speak("En configuraciones avanzadas puedes ajustar muchos aspectos de mi personalidad y comportamiento. ¿Hay algo específico que te gustaría cambiar?", "thoughtful")
        
        Toast.makeText(this, settingsMessage, Toast.LENGTH_LONG).show()
    }
    
    /**
     * Reinicia personalización a valores por defecto
     */
    fun resetToDefaults() {
        customizationScope.launch {
            // Confirmar acción
            voiceSystem.speak("¿Estás seguro de que quieres reiniciar toda mi personalización? Volveré a como era al principio...", "concerned")
            
            delay(3000)
            
            // Simular reset
            isUncensoredMode = false
            outfitManager.disableUncensoredMode()
            currentExpression = "neutral"
            
            voiceSystem.speak("Listo, he vuelto a mi configuración original. ¡Ahora podemos empezar de nuevo si quieres!", "neutral")
            
            Toast.makeText(this, "🔄 Avatar reiniciado a configuración por defecto", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        customizationScope.cancel()
        voiceSystem.cleanup()
    }
    
    override fun onBackPressed() {
        voiceSystem.speak("¡Hasta luego! Espero que te haya gustado personalizarme. ¡Vuelve cuando quieras!", "happy")
        
        customizationScope.launch {
            delay(2000)
            super.onBackPressed()
        }
    }
}