package com.ritsuai.assistant.core.avatar

import android.content.Context
import kotlinx.coroutines.*

/**
 * Gestor de expresiones del avatar kawaii de Ritsu
 * Maneja las transiciones suaves entre expresiones y comportamientos emotivos
 */
class ExpressionManager(private val context: Context) {
    
    private val expressionScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var avatarRenderer: KawaiiAvatarRenderer
    
    // Estado actual de expresiones
    private var currentExpression = "neutral"
    private var expressionIntensity = 1.0f
    private var isTransitioning = false
    
    // Cola de expresiones para transiciones suaves
    private val expressionQueue = mutableListOf<ExpressionCommand>()
    
    data class ExpressionCommand(
        val expression: String,
        val intensity: Float = 1.0f,
        val duration: Long = 2000L,
        val transition: TransitionType = TransitionType.SMOOTH
    )
    
    enum class TransitionType {
        SMOOTH,     // Transición suave
        QUICK,      // Transición rápida
        BOUNCE,     // Con rebote
        FADE        // Desvanecimiento
    }
    
    // Patrones de expresión emocional
    private val emotionalPatterns = mapOf(
        "happiness" to listOf("happy", "excited", "blushing"),
        "sadness" to listOf("sad", "thoughtful", "sleepy"),
        "surprise" to listOf("surprised", "excited", "curious"),
        "shyness" to listOf("shy", "blushing", "looking_down"),
        "playfulness" to listOf("winking", "happy", "excited"),
        "concentration" to listOf("thoughtful", "looking_up", "neutral")
    )
    
    fun initialize(renderer: KawaiiAvatarRenderer) {
        avatarRenderer = renderer
        startExpressionProcessor()
    }
    
    /**
     * Muestra una expresión específica
     */
    fun showExpression(expression: String, intensity: Float = 1.0f, duration: Long = 2000L) {
        val command = ExpressionCommand(expression, intensity, duration)
        expressionQueue.add(command)
        
        if (!isTransitioning) {
            processNextExpression()
        }
    }
    
    /**
     * Muestra una secuencia de expresiones emocionales
     */
    fun showEmotionalSequence(emotionType: String, intensity: Float = 1.0f) {
        val patterns = emotionalPatterns[emotionType] ?: return
        
        patterns.forEach { expression ->
            val duration = when (expression) {
                "blushing" -> 3000L
                "excited" -> 1500L
                "thoughtful" -> 4000L
                else -> 2000L
            }
            
            showExpression(expression, intensity, duration)
        }
    }
    
    /**
     * Expresión reactiva basada en contexto
     */
    fun reactToContext(context: String, userInput: String = "") {
        when (context) {
            "user_praise" -> {
                showEmotionalSequence("happiness", 0.8f)
                showExpression("blushing", 1.0f, 3000L)
            }
            
            "user_touch" -> {
                showExpression("surprised", 0.7f, 1000L)
                showExpression("blushing", 0.9f, 2500L)
                showExpression("happy", 0.8f, 2000L)
            }
            
            "phone_call" -> {
                showExpression("surprised", 0.6f, 800L)
                showExpression("thoughtful", 0.7f, 1500L)
                showExpression("neutral", 0.5f, 1000L)
            }
            
            "whatsapp_message" -> {
                showExpression("curious", 0.6f, 1200L)
                showExpression("neutral", 0.5f, 1000L)
            }
            
            "outfit_change" -> {
                showExpression("excited", 1.0f, 2000L)
                showExpression("happy", 0.8f, 3000L)
            }
            
            "uncensored_mode" -> {
                showExpression("shy", 1.0f, 2000L)
                showExpression("blushing", 1.0f, 4000L)
            }
            
            "conversation_start" -> {
                showExpression("happy", 0.7f, 2000L)
                showExpression("excited", 0.6f, 1500L)
            }
            
            "user_compliment" -> {
                showExpression("surprised", 0.5f, 1000L)
                showExpression("blushing", 1.0f, 3000L)
                showExpression("shy", 0.8f, 2000L)
            }
            
            "idle_behavior" -> {
                performIdleBehavior()
            }
        }
    }
    
    /**
     * Comportamiento cuando Ritsu está en modo idle
     */
    private fun performIdleBehavior() {
        val idleBehaviors = listOf(
            { naturalBlinking() },
            { lookAroundCuriously() },
            { showMiniExpression() },
            { adjustPosture() }
        )
        
        idleBehaviors.random().invoke()
    }
    
    private fun naturalBlinking() {
        expressionScope.launch {
            repeat(3) {
                showExpression("blinking", 1.0f, 150L)
                delay(3000L + (2000L * Math.random()).toLong())
            }
        }
    }
    
    private fun lookAroundCuriously() {
        expressionScope.launch {
            showExpression("looking_left", 0.6f, 2000L)
            delay(2200L)
            showExpression("looking_right", 0.6f, 2000L)
            delay(2200L)
            showExpression("neutral", 0.5f, 1000L)
        }
    }
    
    private fun showMiniExpression() {
        val miniExpressions = listOf(
            "slight_smile",
            "thoughtful",
            "curious",
            "content"
        )
        
        val expression = miniExpressions.random()
        showExpression(expression, 0.4f, 3000L)
    }
    
    private fun adjustPosture() {
        // Simular pequeños ajustes de postura
        expressionScope.launch {
            showExpression("neutral", 0.8f, 1000L)
            delay(500L)
            showExpression("neutral", 1.0f, 1000L)
        }
    }
    
    /**
     * Procesa la cola de expresiones
     */
    private fun startExpressionProcessor() {
        expressionScope.launch {
            while (isActive) {
                if (expressionQueue.isNotEmpty() && !isTransitioning) {
                    processNextExpression()
                }
                delay(100L) // Check every 100ms
            }
        }
    }
    
    private fun processNextExpression() {
        if (expressionQueue.isEmpty()) return
        
        val command = expressionQueue.removeAt(0)
        
        expressionScope.launch {
            isTransitioning = true
            
            try {
                // Aplicar transición según el tipo
                when (command.transition) {
                    TransitionType.SMOOTH -> applySmoothTransition(command)
                    TransitionType.QUICK -> applyQuickTransition(command)
                    TransitionType.BOUNCE -> applyBounceTransition(command)
                    TransitionType.FADE -> applyFadeTransition(command)
                }
                
                // Mantener la expresión por la duración especificada
                delay(command.duration)
                
            } finally {
                isTransitioning = false
                currentExpression = command.expression
                expressionIntensity = command.intensity
            }
        }
    }
    
    private suspend fun applySmoothTransition(command: ExpressionCommand) {
        val steps = 10
        val stepDuration = 200L
        
        for (i in 1..steps) {
            val progress = i.toFloat() / steps
            val blendedIntensity = expressionIntensity + (command.intensity - expressionIntensity) * progress
            
            avatarRenderer.changeExpression(command.expression, blendedIntensity)
            delay(stepDuration)
        }
    }
    
    private suspend fun applyQuickTransition(command: ExpressionCommand) {
        avatarRenderer.changeExpression(command.expression, command.intensity)
    }
    
    private suspend fun applyBounceTransition(command: ExpressionCommand) {
        // Transición con rebote
        avatarRenderer.changeExpression(command.expression, command.intensity * 1.2f)
        delay(100L)
        avatarRenderer.changeExpression(command.expression, command.intensity * 0.8f)
        delay(100L)
        avatarRenderer.changeExpression(command.expression, command.intensity)
    }
    
    private suspend fun applyFadeTransition(command: ExpressionCommand) {
        // Fade out current expression
        for (i in 10 downTo 1) {
            val fadeIntensity = expressionIntensity * (i.toFloat() / 10f)
            avatarRenderer.changeExpression(currentExpression, fadeIntensity)
            delay(50L)
        }
        
        // Fade in new expression
        for (i in 1..10) {
            val fadeIntensity = command.intensity * (i.toFloat() / 10f)
            avatarRenderer.changeExpression(command.expression, fadeIntensity)
            delay(50L)
        }
    }
    
    /**
     * Expresiones basadas en análisis de texto del usuario
     */
    fun analyzeUserTextAndReact(userText: String) {
        val lowerText = userText.lowercase()
        
        when {
            // Detectar cumplidos
            lowerText.contains("hermosa") || lowerText.contains("bonita") || 
            lowerText.contains("linda") || lowerText.contains("preciosa") -> {
                reactToContext("user_compliment")
            }
            
            // Detectar palabras de afecto
            lowerText.contains("te amo") || lowerText.contains("te quiero") -> {
                showExpression("surprised", 0.8f, 1000L)
                showExpression("blushing", 1.0f, 4000L)
                showExpression("shy", 0.9f, 3000L)
            }
            
            // Detectar preguntas sobre ropa
            lowerText.contains("ropa") || lowerText.contains("vestido") || 
            lowerText.contains("outfit") -> {
                showExpression("excited", 0.9f, 2000L)
                showExpression("happy", 0.8f, 2500L)
            }
            
            // Detectar tristeza del usuario
            lowerText.contains("triste") || lowerText.contains("mal") || 
            lowerText.contains("deprimido") -> {
                showExpression("concerned", 0.8f, 2000L)
                showExpression("sad", 0.6f, 3000L)
                showExpression("caring", 0.9f, 4000L)
            }
            
            // Detectar alegría del usuario
            lowerText.contains("feliz") || lowerText.contains("contento") || 
            lowerText.contains("bien") -> {
                showEmotionalSequence("happiness", 0.8f)
            }
            
            // Activación de modo sin censura
            lowerText.contains("ritsu_kawaii_mode") || 
            lowerText.contains("unlock_private_mode") -> {
                reactToContext("uncensored_mode")
            }
            
            else -> {
                // Reacción neutral pero interesada
                showExpression("thoughtful", 0.6f, 1500L)
                showExpression("neutral", 0.7f, 1000L)
            }
        }
    }
    
    /**
     * Expresiones específicas para diferentes momentos del día
     */
    fun showTimeBasedExpression() {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        
        when (hour) {
            in 6..9 -> {
                // Mañana - expresión despierta
                showExpression("sleepy", 0.8f, 2000L)
                showExpression("stretching", 0.7f, 1500L)
                showExpression("happy", 0.6f, 2000L)
            }
            
            in 10..17 -> {
                // Día - expresión energética
                showExpression("excited", 0.7f, 1500L)
                showExpression("happy", 0.8f, 2000L)
            }
            
            in 18..22 -> {
                // Tarde/Noche - expresión relajada
                showExpression("content", 0.7f, 2500L)
                showExpression("thoughtful", 0.6f, 2000L)
            }
            
            else -> {
                // Noche tardía - expresión somnolienta
                showExpression("sleepy", 0.9f, 3000L)
                showExpression("yawning", 0.8f, 2000L)
            }
        }
    }
    
    /**
     * Limpieza al destruir el manager
     */
    fun cleanup() {
        expressionScope.cancel()
        expressionQueue.clear()
    }
}