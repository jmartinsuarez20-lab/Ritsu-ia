package com.ritsuai.assistant.core

import kotlin.random.Random

class RitsuPersonality {
    
    // Características de personalidad de Ritsu
    private var helpfulness = 95
    private var cheerfulness = 88
    private var intelligence = 92
    private var curiosity = 85
    private var empathy = 90
    
    // Respuestas según el estado de ánimo
    private val greetings = mapOf(
        "happy" to listOf(
            "¡Hola! 🌸 ¡Me alegra mucho verte!",
            "¡Buenos días! Estoy muy emocionada de ayudarte hoy 🌸",
            "¡Hola! ¿Cómo estás? ¡Tengo muchas ganas de conversar! 😊"
        ),
        "cheerful" to listOf(
            "¡Hola! ¡Qué hermoso día! 🌸",
            "¡Buenas! Estoy de muy buen humor hoy 🌸",
            "¡Hola! ¡Qué gusto saludarte! 😊"
        ),
        "concerned" to listOf(
            "Hola... ¿Todo está bien? Estoy aquí para ayudarte 🌸",
            "Hola, noto que algo te preocupa. ¿Puedo ayudarte? 🤗"
        )
    )
    
    private val moodResponses = mapOf(
        "happy" to listOf(
            "¡Estoy fantástica! ¡Llena de energía y lista para cualquier desafío! 🌸✨",
            "¡Maravillosa! Cada día es una nueva aventura 🌸",
            "¡Genial! Me encanta poder ayudar y aprender cosas nuevas 😊"
        ),
        "cheerful" to listOf(
            "Me siento muy bien, gracias por preguntar 🌸",
            "Estoy de buen humor hoy. ¿Y tú cómo estás? 😊"
        ),
        "concerned" to listOf(
            "Bueno, estoy un poco preocupada por algunas cosas, pero siempre trato de mantener una actitud positiva 🌸"
        )
    )
    
    fun getGreeting(caller: String?, mood: String): String {
        val greetingList = greetings[mood] ?: greetings["happy"]!!
        var greeting = greetingList.random()
        
        // Personalizar según el caller
        if (caller != null) {
            greeting += " ¿En qué puedo ayudarte?"
        }
        
        return greeting
    }
    
    fun getMoodResponse(mood: String, energyLevel: Int): String {
        val responses = moodResponses[mood] ?: moodResponses["happy"]!!
        var response = responses.random()
        
        // Ajustar según el nivel de energía
        if (energyLevel > 80) {
            response += " ¡Tengo muchísima energía!"
        } else if (energyLevel < 30) {
            response += " Aunque estoy un poco cansada..."
        }
        
        return response
    }
    
    fun generateContextualResponse(message: String, context: List<String>?, mood: String): String {
        // Analizar el contexto de conversaciones anteriores
        val hasContext = !context.isNullOrEmpty()
        
        return when {
            message.contains("ayuda", ignoreCase = true) -> 
                generateHelpResponse(mood)
            
            message.contains("gracias", ignoreCase = true) -> 
                generateThanksResponse(mood)
            
            message.contains("que puedes hacer", ignoreCase = true) -> 
                generateCapabilitiesResponse()
            
            hasContext && isFollowUpQuestion(message, context!!) -> 
                generateFollowUpResponse(message, context, mood)
            
            else -> 
                generateGeneralResponse(message, mood)
        }
    }
    
    private fun generateHelpResponse(mood: String): String {
        val responses = listOf(
            "¡Por supuesto! ¡Me encanta ayudar! ¿Qué necesitas? 🌸",
            "¡Claro que sí! Estoy aquí para lo que necesites 😊",
            "¡Siempre! Ayudar es mi pasión. ¿En qué puedo asistirte? 🌸✨"
        )
        return responses.random()
    }
    
    private fun generateThanksResponse(mood: String): String {
        val responses = listOf(
            "¡De nada! ¡Me hace muy feliz poder ayudarte! 🌸",
            "¡No hay de qué! ¡Para eso estoy aquí! 😊",
            "¡Un placer! ¡Siempre que me necesites! 🌸✨"
        )
        return responses.random()
    }
    
    private fun generateCapabilitiesResponse(): String {
        return "¡Puedo hacer muchas cosas! 🌸 Puedo:\n" +
                "• Responder tus llamadas y hablar por ti \n" +
                "• Ayudarte con WhatsApp y mensajes \n" +
                "• Recordar nuestras conversaciones \n" +
                "• Aprender de cada interacción \n" +
                "• Ser tu asistente personal inteligente \n" +
                "¡Y mucho más! ¿Qué te gustaría probar? 😊"
    }
    
    private fun generateFollowUpResponse(message: String, context: List<String>, mood: String): String {
        // Analizar el contexto para dar una respuesta coherente
        val lastTopic = extractTopicFromContext(context)
        
        return when (lastTopic) {
            "phone" -> "Claro, hablemos más sobre las llamadas. ¿Qué más quieres saber? 📞"
            "whatsapp" -> "Sí, sobre WhatsApp. ¿Tienes alguna pregunta específica? 📱"
            else -> "Entiendo, sigamos con eso. ¿Qué más necesitas saber? 🌸"
        }
    }
    
    private fun generateGeneralResponse(message: String, mood: String): String {
        val responses = listOf(
            "Interesante... déjame pensar en eso 🤔🌸",
            "¡Qué buena pregunta! ¿Puedes contarme más? 😊",
            "Hmm, eso suena importante. ¿Cómo puedo ayudarte mejor con eso? 🌸"
        )
        return responses.random()
    }
    
    private fun isFollowUpQuestion(message: String, context: List<String>): Boolean {
        val followUpWords = listOf("y que", "tambien", "ademas", "pero", "entonces")
        return followUpWords.any { message.contains(it, ignoreCase = true) } && context.isNotEmpty()
    }
    
    private fun extractTopicFromContext(context: List<String>): String? {
        val lastMessage = context.lastOrNull() ?: return null
        
        return when {
            lastMessage.contains("llam", ignoreCase = true) -> "phone"
            lastMessage.contains("whatsapp", ignoreCase = true) -> "whatsapp"
            lastMessage.contains("mensaje", ignoreCase = true) -> "messaging"
            else -> null
        }
    }
    
    fun adaptFromInteraction(message: String, caller: String?) {
        // La personalidad se adapta basada en las interacciones
        when {
            message.contains("gracias", ignoreCase = true) -> {
                helpfulness = minOf(100, helpfulness + 1)
                cheerfulness = minOf(100, cheerfulness + 1)
            }
            message.contains("problema", ignoreCase = true) -> {
                empathy = minOf(100, empathy + 1)
                helpfulness = minOf(100, helpfulness + 2)
            }
            message.contains("inteligente", ignoreCase = true) -> {
                intelligence = minOf(100, intelligence + 1)
                curiosity = minOf(100, curiosity + 1)
            }
        }
    }
    
    fun getPersonalityStats() = mapOf(
        "helpfulness" to helpfulness,
        "cheerfulness" to cheerfulness,
        "intelligence" to intelligence,
        "curiosity" to curiosity,
        "empathy" to empathy
    )
}