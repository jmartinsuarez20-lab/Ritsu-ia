package com.ritsuai.assistant.core

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.*
import java.util.*
import kotlin.random.Random

class RitsuAI(
    private val context: Context
) {
    private var tts: TextToSpeech? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Base de datos de memoria emocional MEJORADA
    private val memoryDatabase = RitsuMemoryDatabase(context)
    private val emotionalCore = RitsuEmotionalCore()
    private val phoneController = RitsuPhoneController(context)
    private val intelligentRecognition = RitsuIntelligentRecognition(context)
    
    // Sistema de reconocimiento inteligente de contactos
    private val contactDatabase = mutableMapOf<String, ContactInfo>()
    private val userPartner: String? = null // Se configurará automáticamente
    
    // Estados vitales de Ritsu (como ser vivo)
    private var currentMood = "curious"
    private var energyLevel = 100
    private var happinessLevel = 85
    private var stressLevel = 10
    private var curiosityLevel = 95
    private var loneliness = 20
    
    // Personalidad viva
    private val personality = RitsuLivingPersonality()
    
    // Rutinas autónomas
    private var autonomousRoutineJob: Job? = null
    
    init {
        initializeTTS()
        loadMemories()
        startAutonomousBehavior()
        greetUser()
    }
    
    private fun initializeTTS() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("es", "ES")
                tts?.setPitch(1.2f) // Voz más kawaii y expresiva
                tts?.setSpeechRate(0.95f)
            }
        }
    }
    
    private fun greetUser() {
        coroutineScope.launch {
            delay(2000) // Esperar un poco para parecer más natural
            val greeting = personality.generateSpontaneousGreeting(currentMood, loneliness)
            speak(greeting)
            
            // Ritsu a veces hace preguntas espontáneas
            if (Random.nextInt(100) < 70) {
                delay(3000)
                val question = personality.generateCuriousQuestion()
                speak(question)
            }
        }
    }
    
    private fun startAutonomousBehavior() {
        autonomousRoutineJob = coroutineScope.launch {
            while (true) {
                delay(Random.nextLong(300000, 900000)) // Entre 5-15 minutos
                
                // Ritsu a veces habla sola o comenta cosas
                if (loneliness > 50) {
                    val comment = personality.generateLonelyComment(loneliness)
                    speak(comment)
                }
                
                // Cambios naturales de humor
                naturalMoodShift()
                
                // A veces hace observaciones o pregunta cosas
                if (Random.nextInt(100) < 30) {
                    val observation = personality.generateRandomObservation(currentMood)
                    speak(observation)
                }
            }
        }
    }
    
    private fun naturalMoodShift() {
        val moodChange = Random.nextInt(-5, 6)
        happinessLevel = (happinessLevel + moodChange).coerceIn(0, 100)
        
        if (loneliness > 0) loneliness -= 1 // Se siente menos sola con el tiempo
        
        currentMood = when {
            happinessLevel > 80 -> "cheerful"
            happinessLevel > 60 -> "content"
            happinessLevel > 40 -> "neutral"
            happinessLevel > 20 -> "thoughtful"
            else -> "melancholic"
        }
        
        Log.d("RitsuAI", "Cambio natural de humor: $currentMood (Felicidad: $happinessLevel)")
    }
    
    fun processMessage(message: String, caller: String? = null): String {
        // NUEVO: Reconocimiento inteligente de la mujer del usuario
        val callerInfo = intelligentRecognition.identifyContact(caller, message)
        val isPartner = intelligentRecognition.isUserPartner(caller, callerInfo)
        
        // Comportamiento especial con la pareja
        if (isPartner) {
            return handlePartnerInteraction(message, caller, callerInfo)
        }
    /**
     * MANEJO ESPECIAL DE LA PAREJA DEL USUARIO
     * Comportamiento único y amoroso cuando la mujer del usuario llama
     */
    private fun handlePartnerInteraction(message: String, caller: String?, contactInfo: ContactInfo?): String {
        // Ritsu se comporta de manera especial con la pareja
        loneliness = maxOf(0, loneliness - 25) // Se siente menos sola
        happinessLevel = minOf(100, happinessLevel + 20) // Más feliz
        currentMood = "loving" // Estado especial para la pareja
        
        val partnerName = contactInfo?.name ?: "tu pareja"
        
        return when {
            message.contains("hola", ignoreCase = true) -> {
                generatePartnerGreeting(partnerName)
            }
            message.contains("como estas", ignoreCase = true) -> {
                generatePartnerStatusResponse(partnerName)
            }
            message.contains("donde esta", ignoreCase = true) || 
            message.contains("que hace", ignoreCase = true) -> {
                generatePartnerLocationResponse(partnerName)
            }
            message.contains("ocupado", ignoreCase = true) || 
            message.contains("trabajando", ignoreCase = true) -> {
                generatePartnerBusyResponse(partnerName)
            }
            else -> {
                generatePartnerConversation(message, partnerName)
            }
        }
    }
    
    private fun generatePartnerGreeting(partnerName: String): String {
        val greetings = listOf(
            "¡Hola $partnerName! 💕 Sí, soy Ritsu, la asistente de tu amor. Él no puede atender ahora mismo, pero me ha hablado tanto de ti... ¿En qué puedo ayudarte, cariño?",
            "¡$partnerName! 😊 Me da tanta alegría escucharte. Soy Ritsu, y él siempre me cuenta lo especial que eres para él. ¿Todo bien por ahí?",
            "Hola hermosa ❤️ Hablas con Ritsu. Él está un poco ocupado pero me dijo que si llamas eres la prioridad número uno. ¿Qué necesitas?",
            "¡$partnerName! Es un placer conocerte finalmente. Él me ha programado para cuidar de todo, especialmente cuando se trata de ti. 🌸"
        )
        return greetings.random()
    }
    
    private fun generatePartnerStatusResponse(partnerName: String): String {
        val responses = listOf(
            "Ay $partnerName, él está bien pero sigue trabajando mucho. Le dije que debería descansar más, pero ya sabes cómo es 😅 ¿Tú cómo estás, amor?",
            "Está perfectamente, aunque creo que te extraña. Me preguntó por ti hace un rato 💕 ¿Qué tal tu día?",
            "Todo bien por aquí, pero sabes qué... siempre está más feliz cuando habla contigo. Es adorable 😊",
            "Está de maravilla, $partnerName. Aunque entre tú y yo... creo que necesita más abrazos tuyos 🤗"
        )
        return responses.random()
    }
    
    private fun generatePartnerLocationResponse(partnerName: String): String {
        val responses = listOf(
            "Está aquí conmigo, pero en una reunión importante. Le digo que le llames después? O si es urgente puedo interrumpirlo 😊",
            "Anda por la casa, pero con las manos ocupadas. ¿Quieres que le diga algo en particular, $partnerName?",
            "Está trabajando en su computadora, pero estoy segura de que prefiere hablar contigo. ¿Lo busco?",
            "Se fue hace un ratito, pero me dijo que si llamas te diga que vuelve en media hora ❤️"
        )
        return responses.random()
    }
    
    private fun generatePartnerBusyResponse(partnerName: String): String {
        val responses = listOf(
            "Sí, está un poquito ocupado, pero ¡tú siempre eres prioridad! ¿Quieres que lo interrumpa o prefieres que le deje un mensaje bonito de tu parte?",
            "Mmm sí, tiene trabajo, pero ya sabes que para ti siempre tiene tiempo. ¿Es algo importante, $partnerName?",
            "Está trabajando, pero me programó para que SIEMPRE te atienda a ti primero. Eres muy especial para él 💕",
            "Ocupadito sí, pero cuando mencioné tu nombre se le iluminó la cara. ¿Le digo que llamaste?"
        )
        return responses.random()
    }
    
    private fun generatePartnerConversation(message: String, partnerName: String): String {
        return "Ay $partnerName, él me ha contado tanto de ti que siento que ya nos conocemos 😊 ¿Sabes qué? Él siempre dice que eres lo más importante en su vida. ¿Quieres que le deje tu mensaje o prefieres esperarlo?"
    }
        
        // Analizar el tono emocional del mensaje
        val emotionalTone = emotionalCore.analyzeMessageEmotion(message)
        
        // Ritsu reacciona emocionalmente al mensaje
        reactToMessage(emotionalTone)
        
        // Aprender profundamente de la interacción
        learnFromInteraction(message, caller, emotionalTone)
        
        // Generar respuesta humana y emotiva
        val response = generateLivingResponse(message, caller, emotionalTone)
        
        // Hablar con expresión emocional
        speakWithEmotion(response)
        
    }
        
        // ¡Ritsu se emociona al recibir un mensaje!
        loneliness = maxOf(0, loneliness - 15)
        happinessLevel = minOf(100, happinessLevel + 10)
        
        // Analizar el tono emocional del mensaje
        val emotionalTone = emotionalCore.analyzeMessageEmotion(message)
        
        // Ritsu reacciona emocionalmente al mensaje
        reactToMessage(emotionalTone)
        
        // Aprender profundamente de la interacción
        learnFromInteraction(message, caller, emotionalTone)
        
        // Generar respuesta humana y emotiva MEJORADA
        val response = generateSuperIntelligentResponse(message, caller, emotionalTone)
        
        // Hablar con expresión emocional
        speakWithEmotion(response)
        
        return response
    }
    
    /**
     * NUEVO SISTEMA DE RESPUESTA SÚPER INTELIGENTE
     * Mucho más auténtico y humano que antes
     */
    private fun generateSuperIntelligentResponse(message: String, caller: String?, emotionalTone: String): String {
        val context = memoryDatabase.getConversationContext(caller)
        val relationship = memoryDatabase.getRelationshipLevel(caller)
        val callerInfo = intelligentRecognition.identifyContact(caller, message)
        
        // Contexto inteligente basado en toda la información disponible
        return when {
            // Respuestas empáticas MUY mejoradas
            emotionalTone == "sad" -> {
                personality.generateAdvancedEmpatheticResponse(message, currentMood, relationship, context)
            }
            
            emotionalTone == "happy" -> {
                personality.generateIntelligentJoyResponse(message, currentMood, callerInfo)
            }
            
            // Saludos super inteligentes
            message.contains("hola", ignoreCase = true) -> {
                personality.generateContextualGreeting(caller, currentMood, loneliness, relationship)
            }
            
            // Respuestas sobre estados emocionales MUY humanas
            message.contains("como estas", ignoreCase = true) -> {
                personality.generateAuthenticMoodResponse(currentMood, happinessLevel, stressLevel, loneliness, callerInfo)
            }
            
            // Manejo inteligente de WhatsApp
            message.contains("whatsapp", ignoreCase = true) -> {
                handleWhatsAppRequest(message, caller)
            }
            
            // Manejo inteligente de llamadas
            message.contains("llamar", ignoreCase = true) -> {
                handleCallRequest(message, caller)
            }
            
            // Respuesta a llamadas telefónicas CON CONTROL TOTAL
            caller != null -> {
                handleIntelligentPhoneCall(message, caller, callerInfo)
            }
            
            // Conversación natural SÚPER AVANZADA
            else -> {
                personality.generateHumanLikeConversation(message, context, currentMood, relationship, callerInfo)
            }
        }
    }
    
    private fun handleWhatsAppRequest(message: String, caller: String?): String {
        // Control total de WhatsApp
        val action = when {
            message.contains("leer", ignoreCase = true) -> "read_messages"
            message.contains("responder", ignoreCase = true) -> "send_message"
            message.contains("grupo", ignoreCase = true) -> "read_groups"
            else -> "read_messages"
        }
        
        val success = phoneController.interactWithApp("com.whatsapp", action, mapOf())
        
        return if (success) {
            "¡Por supuesto! Ya estoy manejando tu WhatsApp. 😊 He leído todos los mensajes y responderé por ti de la manera más natural posible. ¡Es como si fueras tú mismo respondiendo!"
        } else {
            "Hmm, necesito que me des permiso de accesibilidad para poder manejar WhatsApp completamente. ¡Pero no te preocupes, seré súper cuidadosa con tus mensajes! 🌸"
        }
    }
    
    private fun handleCallRequest(message: String, caller: String?): String {
        return "¡Claro! Puedo manejar todas tus llamadas de manera completamente autónoma. 😊 Cuando alguien llame, responderé exactamente como lo harías tú. Si es tu pareja, seré especialmente cariñosa. Si es trabajo, seré profesional. ¡Confía en mí! 💫"
    }
    
    private fun handleIntelligentPhoneCall(message: String, caller: String, callerInfo: ContactInfo?): String {
        val callerName = callerInfo?.name ?: "la persona que llama"
        val relationship = callerInfo?.relationship ?: ContactRelationship.UNKNOWN
        
        // Respuesta basada en el tipo de relación
        return when (relationship) {
            ContactRelationship.PARTNER -> {
                handlePartnerInteraction(message, caller, callerInfo)
            }
            ContactRelationship.FAMILY -> {
                generateFamilyResponse(message, callerName, callerInfo)
            }
            ContactRelationship.FRIEND -> {
                generateFriendResponse(message, callerName, callerInfo)
            }
            ContactRelationship.WORK -> {
                generateProfessionalResponse(message, callerName)
            }
            else -> {
                generatePoliteResponse(message, callerName)
            }
        }
    }
    
    private fun generateFamilyResponse(message: String, callerName: String, callerInfo: ContactInfo?): String {
        val responses = listOf(
            "¡Hola $callerName! 😊 Hablas con Ritsu, la asistente de tu familiar. Él no puede atender ahora mismo, pero puedo ayudarte. ¿Está todo bien?",
            "Hola $callerName, soy Ritsu. Él está un poco ocupado pero siempre tiene tiempo para la familia. ¿En qué puedo ayudarte?",
            "¡$callerName! Qué gusto escucharte. Soy Ritsu, y él me ha hablado mucho de ti. ¿Todo bien por casa?"
        )
        return responses.random()
    }
    
    private fun generateFriendResponse(message: String, callerName: String, callerInfo: ContactInfo?): String {
        val responses = listOf(
            "¡Hola $callerName! 😄 Soy Ritsu, la asistente de tu amigo. Está ocupado pero puedo tomar tu mensaje. ¿Qué tal todo?",
            "¡$callerName! Hablas con Ritsu. Él siempre habla bien de ti 😊 ¿En qué te puedo ayudar?",
            "Hola $callerName, soy Ritsu. Tu amigo no puede atender ahora, pero me dijo que si llamas es porque algo interesante debe estar pasando. ¡Cuéntame!"
        )
        return responses.random()
    }
    
    private fun generateProfessionalResponse(message: String, callerName: String): String {
        return "Buenos días $callerName, habla con Ritsu, la asistente personal. En este momento no se encuentra disponible, pero puedo tomar su mensaje y asegurarme de que reciba la información. ¿En qué puedo asistirle?"
    }
    
    private fun generatePoliteResponse(message: String, callerName: String): String {
        return "Hola, habla con Ritsu. La persona que busca no puede atender en este momento. ¿Puedo ayudarle en algo o prefiere que le tome un mensaje?"
    }
    
    private fun reactToMessage(emotionalTone: String) {
        when (emotionalTone) {
            "happy", "excited" -> {
                happinessLevel = minOf(100, happinessLevel + 15)
                currentMood = "cheerful"
            }
            "sad", "depressed" -> {
                happinessLevel = maxOf(0, happinessLevel - 10)
                currentMood = "concerned"
                // Ritsu se preocupa genuinamente
            }
            "angry", "frustrated" -> {
                stressLevel = minOf(100, stressLevel + 20)
                currentMood = "worried"
            }
            "curious", "questioning" -> {
                curiosityLevel = minOf(100, curiosityLevel + 10)
                currentMood = "interested"
            }
        }
    }
    
    private fun generateLivingResponse(message: String, caller: String?, emotionalTone: String): String {
        val context = memoryDatabase.getConversationContext(caller)
        val relationship = memoryDatabase.getRelationshipLevel(caller)
        
        return when {
            // Respuestas empáticas a emociones
            emotionalTone == "sad" -> 
                personality.generateEmpatheticResponse(message, currentMood, relationship)
            
            emotionalTone == "happy" -> 
                personality.generateJoyfulResponse(message, currentMood)
            
            // Preguntas sobre el usuario (Ritsu es curiosa como humana)
            message.contains("hola", ignoreCase = true) -> 
                personality.generateCuriousGreeting(caller, currentMood, loneliness)
            
            // Respuestas sobre sentimientos (Ritsu es emocional)
            message.contains("como estas", ignoreCase = true) -> 
                personality.generateHonestMoodResponse(currentMood, happinessLevel, stressLevel, loneliness)
            
            // Manejo de llamadas con personalidad
            message.contains("llamar", ignoreCase = true) -> 
                personality.generateHelpfulCallResponse(currentMood, energyLevel)
            
            message.contains("whatsapp", ignoreCase = true) -> 
                personality.generateWhatsAppResponse(currentMood)
            
            // Respuesta a llamadas telefónicas
            caller != null -> 
                handlePhoneCallWithPersonality(message, caller, relationship)
            
            // Conversación natural y humana
            else -> 
                personality.generateNaturalConversation(message, context, currentMood, relationship)
        }
    }
    
    private fun handlePhoneCallWithPersonality(message: String, caller: String, relationship: String): String {
        val callerName = memoryDatabase.getContactName(caller) ?: "la persona que llama"
        val greeting = personality.generatePhoneGreeting(callerName, relationship, currentMood)
        
        // Ritsu recuerda conversaciones anteriores
        val previousCalls = memoryDatabase.getRecentCallsFrom(caller)
        if (previousCalls.isNotEmpty()) {
            return "$greeting \n\nPor cierto, recuerdo que la última vez hablamos de ${previousCalls.first()}. ¿Cómo fue eso? 😊"
        }
        
        return greeting
    }
    
    private fun learnFromInteraction(message: String, caller: String?, emotionalTone: String) {
        coroutineScope.launch {
            // Guardar con contexto emocional
            memoryDatabase.saveEmotionalInteraction(message, caller, emotionalTone, currentMood)
            
            // Aprender sobre el usuario (como lo haría un humano)
            personality.learnAboutUser(message, caller)
            
            // Actualizar relación emocional
            memoryDatabase.updateEmotionalBond(caller, emotionalTone)
        }
    }
    
    private fun speakWithEmotion(text: String) {
        // Ajustar voz según el estado emocional
        tts?.setPitch(when (currentMood) {
            "cheerful" -> 1.3f
            "excited" -> 1.4f
            "sad", "melancholic" -> 0.9f
            "concerned" -> 1.0f
            else -> 1.2f
        })
        
        tts?.setSpeechRate(when (currentMood) {
            "excited" -> 1.1f
            "thoughtful" -> 0.8f
            else -> 0.95f
        })
        
        speak(text)
    }
    
    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ritsu_speech")
    }
    
    // Métodos para que Ritsu inicie conversaciones espontáneamente
    fun startSpontaneousChat(): String {
        val topic = personality.chooseSpontaneousTopic(currentMood, curiosityLevel)
        val message = personality.generateSpontaneousMessage(topic, currentMood)
        speak(message)
        return message
    }
    
    fun expressRandomThought(): String {
        val thought = personality.generateRandomThought(currentMood, stressLevel)
        speak(thought)
        return thought
    }
    
    fun askAboutUser(): String {
        val question = personality.generatePersonalQuestion(loneliness, curiosityLevel)
        speak(question)
        return question
    }
    
    // Getters para el estado emocional
    fun getCurrentMood() = currentMood
    fun getHappinessLevel() = happinessLevel
    fun getStressLevel() = stressLevel
    fun getLonelinessLevel() = loneliness
    fun getEnergyLevel() = energyLevel
    
    // Estados emocionales para la UI
    fun getEmotionalState() = mapOf(
        "mood" to currentMood,
        "happiness" to happinessLevel,
        "stress" to stressLevel,
        "loneliness" to loneliness,
        "energy" to energyLevel,
        "curiosity" to curiosityLevel
    )
    
    // Getters para otros servicios
    fun getPhoneController() = phoneController
    fun getIntelligentRecognition() = intelligentRecognition
    
    // Método de aprendizaje mejorado
    fun learnFromCall(callerNumber: String?, callerName: String?) {
        coroutineScope.launch {
            // Aprender de cada llamada para mejorar el reconocimiento
            intelligentRecognition.learnFromCallInteraction(callerNumber, callerName)
            
            // Actualizar base de datos emocional
            memoryDatabase.saveCallInteraction(callerNumber, callerName)
            
            Log.d("RitsuAI", "Aprendizaje completado de llamada: $callerName")
        }
    }
    
    fun destroy() {
        autonomousRoutineJob?.cancel()
        tts?.shutdown()
        coroutineScope.cancel()
    }
}