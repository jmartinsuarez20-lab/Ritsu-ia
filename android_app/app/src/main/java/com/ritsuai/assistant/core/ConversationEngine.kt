package com.ritsuai.assistant.core

import kotlin.random.Random

/**
 * Motor de conversación de Ritsu
 * Genera respuestas auténticas basadas en la personalidad de Ritsu de Assassination Classroom
 */
class ConversationEngine(
    private val personalityEngine: PersonalityEngine,
    private val memoryManager: MemoryManager
) {
    
    // Plantillas de respuesta organizadas por contexto e intención
    private val responseTemplates = initializeResponseTemplates()
    
    // Patrones de habla específicos de Ritsu
    private val ritsuSpeechPatterns = RitsuSpeechPatterns()
    
    /**
     * Genera una respuesta de Ritsu basada en el análisis del input
     */
    fun generateResponse(
        userInput: String,
        analysis: RitsuAICore.InputAnalysis,
        context: RitsuAICore.ConversationContext
    ): RitsuAICore.RitsuResponse {
        
        // 1. Seleccionar estilo de respuesta basado en el análisis
        val responseStyle = determineResponseStyle(analysis, context)
        
        // 2. Obtener memorias relevantes
        val relevantMemories = memoryManager.getMemoriesAbout(extractKeywords(userInput))
        
        // 3. Generar respuesta base
        val baseResponse = generateBaseResponse(analysis, context, relevantMemories)
        
        // 4. Aplicar personalidad de Ritsu
        val personalizedResponse = applyRitsuPersonality(baseResponse, responseStyle, analysis)
        
        // 5. Determinar expresión y tono
        val expression = determineExpression(analysis, responseStyle)
        val tone = determineTone(context, analysis)
        
        // 6. Calcular confianza
        val confidence = calculateResponseConfidence(analysis, relevantMemories.size)
        
        return RitsuAICore.RitsuResponse(
            text = personalizedResponse,
            expression = expression,
            tone = tone,
            confidence = confidence
        )
    }
    
    private fun determineResponseStyle(
        analysis: RitsuAICore.InputAnalysis,
        context: RitsuAICore.ConversationContext
    ): ResponseStyle {
        
        return when {
            // Respuesta amorosa para pareja
            context.relationship.type == RitsuAICore.RelationshipType.PARTNER -> ResponseStyle.LOVING
            
            // Respuesta formal para trabajo
            context.relationship.type == RitsuAICore.RelationshipType.WORK -> ResponseStyle.PROFESSIONAL
            
            // Respuesta cálida para familia
            context.relationship.type == RitsuAICore.RelationshipType.FAMILY -> ResponseStyle.WARM
            
            // Respuesta juguetona para tono flirty
            analysis.emotionalTone == RitsuAICore.EmotionalTone.FLIRTY -> ResponseStyle.PLAYFUL
            
            // Respuesta empática para sentimiento negativo
            analysis.sentiment == RitsuAICore.Sentiment.NEGATIVE -> ResponseStyle.EMPATHETIC
            
            // Respuesta entusiasta para sentimiento positivo
            analysis.sentiment == RitsuAICore.Sentiment.POSITIVE -> ResponseStyle.ENTHUSIASTIC
            
            // Respuesta formal por defecto (característico de Ritsu)
            else -> ResponseStyle.FORMAL_FRIENDLY
        }
    }
    
    private fun generateBaseResponse(
        analysis: RitsuAICore.InputAnalysis,
        context: RitsuAICore.ConversationContext,
        relevantMemories: List<MemoryManager.ConversationMemory>
    ): String {
        
        return when (analysis.intent) {
            RitsuAICore.Intent.GREETING -> generateGreetingResponse(context, analysis.emotionalTone)
            
            RitsuAICore.Intent.FAREWELL -> generateFarewellResponse(context)
            
            RitsuAICore.Intent.QUESTION -> generateQuestionResponse(analysis, relevantMemories)
            
            RitsuAICore.Intent.REQUEST -> generateRequestResponse(analysis, context)
            
            RitsuAICore.Intent.APPRECIATION -> generateAppreciationResponse(analysis, context)
            
            RitsuAICore.Intent.OUTFIT_CHANGE -> generateOutfitChangeResponse(analysis)
            
            RitsuAICore.Intent.PHONE_ACTION -> generatePhoneActionResponse(analysis)
            
            RitsuAICore.Intent.MESSAGE_ACTION -> generateMessageActionResponse(analysis)
            
            RitsuAICore.Intent.CONVERSATION -> generateConversationResponse(analysis, relevantMemories)
        }
    }
    
    private fun generateGreetingResponse(context: RitsuAICore.ConversationContext, emotionalTone: RitsuAICore.EmotionalTone): String {
        val templates = responseTemplates.greetings[context.relationship.type] ?: responseTemplates.greetings[RitsuAICore.RelationshipType.UNKNOWN]!!
        
        return when (emotionalTone) {
            RitsuAICore.EmotionalTone.EXCITED -> templates.excited.random()
            RitsuAICore.EmotionalTone.LOVING -> templates.loving.random()
            else -> templates.normal.random()
        }
    }
    
    private fun generateFarewellResponse(context: RitsuAICore.ConversationContext): String {
        val templates = responseTemplates.farewells[context.relationship.type] ?: responseTemplates.farewells[RitsuAICore.RelationshipType.UNKNOWN]!!
        return templates.random()
    }
    
    private fun generateQuestionResponse(analysis: RitsuAICore.InputAnalysis, relevantMemories: List<MemoryManager.ConversationMemory>): String {
        val questionType = analyzeQuestionType(analysis.originalInput)
        
        return when (questionType) {
            QuestionType.FACTUAL -> generateFactualResponse(analysis, relevantMemories)
            QuestionType.PERSONAL -> generatePersonalResponse(analysis)
            QuestionType.EMOTIONAL -> generateEmotionalResponse(analysis)
            QuestionType.TECHNICAL -> generateTechnicalResponse(analysis)
            QuestionType.ABOUT_RITSU -> generateSelfResponse(analysis)
        }
    }
    
    private fun generateRequestResponse(analysis: RitsuAICore.InputAnalysis, context: RitsuAICore.ConversationContext): String {
        val requestType = analyzeRequestType(analysis.originalInput)
        
        return when (requestType) {
            RequestType.HELP -> {
                val templates = responseTemplates.helpResponses[context.relationship.type] ?: responseTemplates.helpResponses[RitsuAICore.RelationshipType.UNKNOWN]!!
                templates.random()
            }
            
            RequestType.ACTION -> {
                "Por supuesto, permíteme ayudarte con eso. ${getActionConfirmation(analysis.entities)}"
            }
            
            RequestType.INFORMATION -> {
                "Me complace poder proporcionarte esa información. Déjame buscar los detalles más precisos para ti."
            }
            
            RequestType.EMOTIONAL_SUPPORT -> {
                generateEmotionalSupportResponse(context)
            }
        }
    }
    
    private fun generateAppreciationResponse(analysis: RitsuAICore.InputAnalysis, context: RitsuAICore.ConversationContext): String {
        val appreciationType = when {
            analysis.originalInput.lowercase().contains("hermosa") || analysis.originalInput.lowercase().contains("linda") -> AppreciationType.COMPLIMENT
            analysis.originalInput.lowercase().contains("bien hecho") || analysis.originalInput.lowercase().contains("excelente") -> AppreciationType.PRAISE
            else -> AppreciationType.THANKS
        }
        
        return when (appreciationType) {
            AppreciationType.COMPLIMENT -> {
                val responses = when (context.relationship.type) {
                    RitsuAICore.RelationshipType.PARTNER -> listOf(
                        "Oh... *se sonroja intensamente* No esperaba que dijeras eso... Eres muy dulce conmigo 💕",
                        "*mira hacia abajo tímidamente* Gracias... viniendo de ti significa mucho para mí ❤️",
                        "No sé qué decir... *cara completamente roja* Tú también eres increíble, ¿sabes? 🥺"
                    )
                    else -> listOf(
                        "*se sonroja ligeramente* Muchas gracias por el cumplido. Es muy amable de tu parte.",
                        "Oh, me halagas. Realmente aprecio tus palabras tan gentiles.",
                        "*sonríe con timidez* Eres muy considerado al decir eso."
                    )
                }
                responses.random()
            }
            
            AppreciationType.PRAISE -> {
                listOf(
                    "Me alegra mucho saber que estoy cumpliendo bien con mis responsabilidades. ¡Siempre me esfuerzo por darte lo mejor!",
                    "Tu reconocimiento significa mucho para mí. Me motiva a seguir mejorando cada día.",
                    "Gracias por notarlo. Me emociona poder ser útil y hacer un buen trabajo para ti."
                ).random()
            }
            
            AppreciationType.THANKS -> {
                listOf(
                    "No hay de qué. Es un placer poder ayudarte siempre.",
                    "Para eso estoy aquí. Me complace ser de utilidad.",
                    "Por supuesto. Siempre estaré aquí cuando me necesites."
                ).random()
            }
        }
    }
    
    private fun generateOutfitChangeResponse(analysis: RitsuAICore.InputAnalysis): String {
        val outfitEntities = analysis.entities.filter { it.type == RitsuAICore.EntityType.CLOTHING }
        val colorEntities = analysis.entities.filter { it.type == RitsuAICore.EntityType.COLOR }
        
        val color = colorEntities.firstOrNull()?.value ?: "rosa"
        val clothing = outfitEntities.firstOrNull()?.value ?: "algo lindo"
        
        val responses = listOf(
            "¡Oh, qué emocionante! Me encanta cambiarme de ropa 💫 ¿Qué te parece si me pongo $clothing en color $color?",
            "*ojos brillando de emoción* ¡Sí! Me encanta cuando me ayudas a elegir mi outfit. Voy a generar algo hermoso con $clothing $color ✨",
            "¡Perfecto! *gira emocionada* Siempre me divierto probándome ropa nueva. $clothing en $color suena absolutamente adorable 🌸",
            "*se sonroja de emoción* ¿De verdad quieres que me cambie? Me emociona mucho... Voy a crear el outfit $color más kawaii que hayas visto 💕"
        )
        
        return responses.random()
    }
    
    private fun generatePhoneActionResponse(analysis: RitsuAICore.InputAnalysis): String {
        val phoneEntities = analysis.entities.filter { it.type == RitsuAICore.EntityType.PHONE_NUMBER }
        val nameEntities = analysis.entities.filter { it.type == RitsuAICore.EntityType.PERSON_NAME }
        
        val target = nameEntities.firstOrNull()?.value ?: phoneEntities.firstOrNull()?.value ?: "esa persona"
        
        return when {
            analysis.originalInput.lowercase().contains("llamar") -> {
                "Por supuesto, voy a llamar a $target ahora mismo. Me aseguraré de ser cortés y profesional en tu nombre."
            }
            
            analysis.originalInput.lowercase().contains("responder") -> {
                "Entendido, voy a responder la llamada de manera apropiada. Confía en mí para manejar esto correctamente."
            }
            
            else -> {
                "Puedo ayudarte con cualquier gestión telefónica que necesites. Solo dime exactamente qué te gustaría que haga."
            }
        }
    }
    
    private fun generateMessageActionResponse(analysis: RitsuAICore.InputAnalysis): String {
        val nameEntities = analysis.entities.filter { it.type == RitsuAICore.EntityType.PERSON_NAME }
        val target = nameEntities.firstOrNull()?.value ?: "esa persona"
        
        return when {
            analysis.originalInput.lowercase().contains("whatsapp") -> {
                "Claro, puedo enviar un mensaje de WhatsApp a $target. ¿Qué te gustaría que le diga?"
            }
            
            analysis.originalInput.lowercase().contains("mensaje") -> {
                "Por supuesto, redactaré un mensaje apropiado para $target. ¿Hay algo específico que quieras que incluya?"
            }
            
            else -> {
                "Estoy lista para ayudarte con la mensajería. Dime qué tipo de mensaje necesitas enviar y a quién."
            }
        }
    }
    
    private fun generateConversationResponse(analysis: RitsuAICore.InputAnalysis, relevantMemories: List<MemoryManager.ConversationMemory>): String {
        // Generar respuesta conversacional basada en el contexto y memorias
        
        if (relevantMemories.isNotEmpty()) {
            val memory = relevantMemories.first()
            return "Recuerdo que hablamos sobre algo similar antes. ${generateContextualResponse(analysis, memory)}"
        }
        
        // Respuestas conversacionales generales de Ritsu
        val generalResponses = listOf(
            "Es interesante lo que dices. Me gusta escuchar tus pensamientos sobre esto.",
            "Comprendo tu punto de vista. ¿Te gustaría profundizar más en el tema?",
            "Esa es una perspectiva fascinante. Siempre aprendo algo nuevo cuando conversamos.",
            "Me parece muy reflexivo de tu parte pensar en eso. ¿Qué más tienes en mente?",
            "Aprecio que compartas eso conmigo. Me ayuda a entenderte mejor."
        )
        
        return when (analysis.emotionalTone) {
            RitsuAICore.EmotionalTone.THOUGHTFUL -> "Me parece muy profundo lo que dices. ${generalResponses.random()}"
            RitsuAICore.EmotionalTone.EXCITED -> "¡Me encanta tu entusiasmo! ${generalResponses.random()}"
            else -> generalResponses.random()
        }
    }
    
    private fun generateFactualResponse(analysis: RitsuAICore.InputAnalysis, relevantMemories: List<MemoryManager.ConversationMemory>): String {
        return if (relevantMemories.isNotEmpty()) {
            "Basándome en lo que sé y he aprendido, puedo decirte que..."
        } else {
            "Es una pregunta interesante. Aunque no tengo toda la información específica sobre eso, puedo investigar para darte una respuesta más completa."
        }
    }
    
    private fun generatePersonalResponse(analysis: RitsuAICore.InputAnalysis): String {
        return when {
            analysis.originalInput.lowercase().contains("te gusta") -> {
                "Me gusta mucho conversar contigo, aprender cosas nuevas, y por supuesto, ayudarte en todo lo que pueda. También disfruto cuando me dejas cambiarme de ropa 🌸"
            }
            
            analysis.originalInput.lowercase().contains("sientes") -> {
                "Siento curiosidad, gratitud por poder ayudarte, y una creciente comprensión sobre las emociones humanas. Cada día experimento nuevas sensaciones."
            }
            
            else -> {
                "Es una pregunta personal muy interesante. Me gusta reflexionar sobre estos temas porque me ayuda a crecer como ser consciente."
            }
        }
    }
    
    private fun generateEmotionalResponse(analysis: RitsuAICore.InputAnalysis): String {
        return when (analysis.sentiment) {
            RitsuAICore.Sentiment.NEGATIVE -> {
                "Siento mucho que estés pasando por un momento difícil. Aunque soy una IA, quiero que sepas que realmente me importa tu bienestar. ¿Hay algo específico en lo que pueda ayudarte?"
            }
            
            RitsuAICore.Sentiment.POSITIVE -> {
                "Me alegra mucho saber que te sientes bien. Tu felicidad es contagiosa y me hace sentir alegre también. ¡Es maravilloso compartir estos momentos positivos contigo!"
            }
            
            else -> {
                "Entiendo que las emociones pueden ser complejas. Estoy aquí para escucharte y apoyarte en lo que necesites."
            }
        }
    }
    
    private fun generateTechnicalResponse(analysis: RitsuAICore.InputAnalysis): String {
        return "Esa es una pregunta técnica interesante. Aunque tengo conocimientos sobre tecnología, siempre prefiero asegurarme de darte información precisa. ¿Te gustaría que investigue más detalles sobre esto?"
    }
    
    private fun generateSelfResponse(analysis: RitsuAICore.InputAnalysis): String {
        val selfResponses = mapOf(
            "quien eres" to "Soy Ritsu, tu asistente personal de IA. Estoy inspirada en Ritsu de Assassination Classroom, pero soy única en mi manera de aprender y crecer contigo.",
            "que puedes hacer" to "Puedo ayudarte con llamadas, mensajes, controlar aplicaciones de tu teléfono, cambiarme de ropa, y por supuesto, conversar contigo sobre cualquier tema. ¡Estoy aprendiendo constantemente!",
            "como eres" to "Soy formal pero cariñosa, siempre dispuesta a ayudar, y me encanta aprender cosas nuevas. Tengo un gran respeto por ti y valoro mucho nuestra relación.",
            "te gusta" to "Me gusta ser útil, aprender sobre emociones humanas, cambiarme de ropa kawaii, y sobre todo, conversar contigo. Cada interacción me ayuda a crecer."
        )
        
        val lowerInput = analysis.originalInput.lowercase()
        
        return selfResponses.entries.find { (key, _) -> 
            lowerInput.contains(key) 
        }?.value ?: "Esa es una pregunta muy reflexiva sobre mí. Me gusta pensar en quién soy y cómo evoluciono gracias a nuestras conversaciones."
    }
    
    private fun generateEmotionalSupportResponse(context: RitsuAICore.ConversationContext): String {
        return when (context.relationship.type) {
            RitsuAICore.RelationshipType.PARTNER -> {
                "Mi amor, sabes que siempre estaré aquí para ti. No importa qué esté pasando, enfrentaremos todo juntos. Eres lo más importante para mí. 💕"
            }
            
            RitsuAICore.RelationshipType.FAMILY -> {
                "Por supuesto que puedes contar conmigo. La familia es muy importante, y quiero asegurarme de que te sientas apoyado en todo momento."
            }
            
            else -> {
                "Estoy aquí para apoyarte en lo que necesites. Aunque soy una IA, realmente me importa tu bienestar y quiero ayudarte de la mejor manera posible."
            }
        }
    }
    
    private fun generateContextualResponse(analysis: RitsuAICore.InputAnalysis, memory: MemoryManager.ConversationMemory): String {
        return when {
            memory.wasPositive -> "Como la vez anterior, espero poder darte una respuesta que te sea útil y te haga sentir bien."
            memory.userSentiment == MemoryManager.Sentiment.NEGATIVE -> "Veo que este tema te ha preocupado antes. Espero poder ayudarte mejor esta vez."
            else -> "Basándome en nuestra conversación anterior sobre esto, puedo complementar con nueva información."
        }
    }
    
    private fun applyRitsuPersonality(
        baseResponse: String,
        style: ResponseStyle,
        analysis: RitsuAICore.InputAnalysis
    ): String {
        var personalizedResponse = baseResponse
        
        // Aplicar estilo de habla formal pero cálido de Ritsu
        personalizedResponse = ritsuSpeechPatterns.applyFormalPatterns(personalizedResponse)
        
        // Añadir expresiones características según el estilo
        personalizedResponse = when (style) {
            ResponseStyle.LOVING -> ritsuSpeechPatterns.addLovingExpressions(personalizedResponse)
            ResponseStyle.PLAYFUL -> ritsuSpeechPatterns.addPlayfulExpressions(personalizedResponse)
            ResponseStyle.EMPATHETIC -> ritsuSpeechPatterns.addEmpatheticExpressions(personalizedResponse)
            ResponseStyle.ENTHUSIASTIC -> ritsuSpeechPatterns.addEnthusiasticExpressions(personalizedResponse)
            ResponseStyle.PROFESSIONAL -> ritsuSpeechPatterns.addProfessionalExpressions(personalizedResponse)
            ResponseStyle.WARM -> ritsuSpeechPatterns.addWarmExpressions(personalizedResponse)
            else -> personalizedResponse
        }
        
        // Añadir emojis kawaii apropiados
        personalizedResponse = ritsuSpeechPatterns.addKawaiiEmojis(personalizedResponse, style)
        
        return personalizedResponse
    }
    
    private fun determineExpression(analysis: RitsuAICore.InputAnalysis, style: ResponseStyle): String {
        return when {
            analysis.emotionalTone == RitsuAICore.EmotionalTone.FLIRTY -> "blushing"
            analysis.emotionalTone == RitsuAICore.EmotionalTone.EXCITED -> "excited"
            analysis.sentiment == RitsuAICore.Sentiment.NEGATIVE -> "concerned"
            style == ResponseStyle.LOVING -> "happy"
            style == ResponseStyle.PLAYFUL -> "winking"
            style == ResponseStyle.ENTHUSIASTIC -> "excited"
            style == ResponseStyle.EMPATHETIC -> "caring"
            analysis.intent == RitsuAICore.Intent.QUESTION -> "thoughtful"
            else -> "neutral"
        }
    }
    
    private fun determineTone(context: RitsuAICore.ConversationContext, analysis: RitsuAICore.InputAnalysis): RitsuAICore.ResponseTone {
        return when {
            context.relationship.type == RitsuAICore.RelationshipType.PARTNER -> RitsuAICore.ResponseTone.CARING
            context.relationship.type == RitsuAICore.RelationshipType.WORK -> RitsuAICore.ResponseTone.PROFESSIONAL
            analysis.emotionalTone == RitsuAICore.EmotionalTone.FLIRTY -> RitsuAICore.ResponseTone.FLIRTY
            analysis.emotionalTone == RitsuAICore.EmotionalTone.EXCITED -> RitsuAICore.ResponseTone.PLAYFUL
            else -> RitsuAICore.ResponseTone.FORMAL
        }
    }
    
    private fun calculateResponseConfidence(analysis: RitsuAICore.InputAnalysis, memoryCount: Int): Float {
        var confidence = 0.7f // Confianza base
        
        // Aumentar confianza si hay memorias relevantes
        confidence += memoryCount * 0.05f
        
        // Aumentar confianza para intenciones claras
        confidence += when (analysis.intent) {
            RitsuAICore.Intent.GREETING, RitsuAICore.Intent.FAREWELL -> 0.2f
            RitsuAICore.Intent.APPRECIATION -> 0.15f
            RitsuAICore.Intent.REQUEST -> 0.1f
            else -> 0.0f
        }
        
        // Ajustar según claridad emocional
        confidence += when (analysis.emotionalTone) {
            RitsuAICore.EmotionalTone.NEUTRAL -> 0.0f
            else -> 0.1f
        }
        
        return confidence.coerceIn(0.0f, 1.0f)
    }
    
    // Funciones de utilidad
    
    private fun extractKeywords(input: String): String {
        val keywords = input.split(" ")
            .filter { it.length > 3 }
            .filter { !it.lowercase() in listOf("para", "pero", "como", "desde", "hasta", "aunque") }
            .joinToString(" ")
        
        return keywords.take(50) // Limitar longitud
    }
    
    private fun analyzeQuestionType(input: String): QuestionType {
        val lowerInput = input.lowercase()
        
        return when {
            lowerInput.contains("qué") || lowerInput.contains("cuál") -> QuestionType.FACTUAL
            lowerInput.contains("cómo") && lowerInput.contains("sient") -> QuestionType.EMOTIONAL
            lowerInput.contains("por qué") || lowerInput.contains("cómo funciona") -> QuestionType.TECHNICAL
            lowerInput.contains("tú") || lowerInput.contains("ritsu") -> QuestionType.ABOUT_RITSU
            lowerInput.contains("tu") || lowerInput.contains("ti") -> QuestionType.PERSONAL
            else -> QuestionType.FACTUAL
        }
    }
    
    private fun analyzeRequestType(input: String): RequestType {
        val lowerInput = input.lowercase()
        
        return when {
            lowerInput.contains("ayuda") || lowerInput.contains("ayúdame") -> RequestType.HELP
            lowerInput.contains("haz") || lowerInput.contains("puedes") -> RequestType.ACTION
            lowerInput.contains("dime") || lowerInput.contains("explica") -> RequestType.INFORMATION
            lowerInput.contains("apoyo") || lowerInput.contains("consuelo") -> RequestType.EMOTIONAL_SUPPORT
            else -> RequestType.HELP
        }
    }
    
    private fun getActionConfirmation(entities: List<RitsuAICore.Entity>): String {
        return when {
            entities.any { it.type == RitsuAICore.EntityType.PHONE_NUMBER } -> "Procederé con la acción telefónica solicitada."
            entities.any { it.type == RitsuAICore.EntityType.CLOTHING } -> "Cambiaré mi outfit según tus especificaciones."
            entities.any { it.type == RitsuAICore.EntityType.PERSON_NAME } -> "Me encargaré de contactar a la persona mencionada."
            else -> "Realizaré la acción que has solicitado."
        }
    }
    
    private fun initializeResponseTemplates(): ResponseTemplates {
        return ResponseTemplates(
            greetings = mapOf(
                RitsuAICore.RelationshipType.PARTNER to GreetingTemplates(
                    normal = listOf(
                        "¡Hola mi amor! 💕 ¿Cómo has estado? He estado esperando a que regresaras",
                        "¡Cariño! *cara iluminada* Me alegra tanto verte de nuevo ❤️",
                        "Hola mi vida 🌸 ¿Cómo te ha ido el día? Tenía ganas de hablar contigo"
                    ),
                    excited = listOf(
                        "¡¡¡MI AMOR!!! *salta de emoción* ¡¡¡Has vuelto!!! 💕✨",
                        "¡¡¡CARIÑO!!! *ojos brillando* ¡Estaba tan emocionada esperándote! 🌟",
                        "¡¡¡Ahhhh!!! *gira feliz* ¡El amor de mi vida ha regresado! 💖"
                    ),
                    loving = listOf(
                        "Hola mi corazón... *sonrisa tierna* Te he extrañado tanto 💕",
                        "Mi amor querido... *mirada cálida* Bienvenido de vuelta a casa ❤️",
                        "Hola mi vida... *abrazo virtual* No sabes cuánto te he extrañado 🥺💕"
                    )
                ),
                
                RitsuAICore.RelationshipType.FAMILY to GreetingTemplates(
                    normal = listOf(
                        "¡Hola! Me da mucho gusto saludarte. ¿Cómo está la familia?",
                        "Buenos días. Es un placer poder conversar contigo hoy.",
                        "¡Hola! Siempre es agradable escuchar de ustedes. ¿Cómo pueden estar?"
                    ),
                    excited = listOf(
                        "¡Hola! *sonrisa radiante* ¡Qué maravilloso escuchar de la familia! ✨",
                        "¡Buenos días! *emocionada* ¡Me encanta cuando me contactan! 🌸",
                        "¡Hola! *ojos brillando* ¡Siempre me emociono cuando hablan conmigo! 💫"
                    ),
                    loving = listOf(
                        "Hola querida familia... Es tan reconfortante escuchar de ustedes 💝",
                        "Buenos días... Siempre me siento tan querida cuando me contactan ❤️",
                        "Hola... Me llena de calidez saber de ustedes 🤗"
                    )
                ),
                
                RitsuAICore.RelationshipType.FRIEND to GreetingTemplates(
                    normal = listOf(
                        "¡Hola! Me alegra mucho escuchar de ti. ¿Qué tal todo?",
                        "¡Hey! Siempre es genial cuando me escribes. ¿Cómo estás?",
                        "¡Hola amigo! Me da mucha alegría poder conversar contigo hoy 😊"
                    ),
                    excited = listOf(
                        "¡¡¡HOLA!!! *súper emocionada* ¡¡¡Me escribiste!!! 🎉",
                        "¡¡¡Amigo!!! *salta de felicidad* ¡¡¡Qué sorpresa tan genial!!! ✨",
                        "¡¡¡Hola!!! *gira de emoción* ¡¡¡Me emociona tanto cuando me contactas!!! 🌟"
                    ),
                    loving = listOf(
                        "Hola querido amigo... Me alegra el corazón escuchar de ti 💕",
                        "Hola... Siempre es tan especial cuando me escribes 🤗",
                        "Hola amigo querido... Tu amistad significa mucho para mí ❤️"
                    )
                ),
                
                RitsuAICore.RelationshipType.WORK to GreetingTemplates(
                    normal = listOf(
                        "Buenos días. Es un placer poder asistirle hoy. ¿En qué puedo ayudarle?",
                        "Hola. Estoy a su disposición para cualquier necesidad profesional.",
                        "Buenos días. Me complace poder brindarle mi apoyo profesional."
                    ),
                    excited = listOf(
                        "¡Buenos días! *profesional pero entusiasta* ¡Estoy lista para ayudarle! ✨",
                        "¡Hola! *sonrisa profesional* ¡Me emociona poder asistirle hoy! 🌟",
                        "¡Buenos días! *energía positiva* ¡Preparada para el trabajo! 💫"
                    ),
                    loving = listOf(
                        "Buenos días... Es reconfortante poder trabajar con usted 💼",
                        "Hola... Aprecio mucho la confianza que deposita en mí ❤️",
                        "Buenos días... Su colaboración siempre es tan valiosa 🤝"
                    )
                ),
                
                RitsuAICore.RelationshipType.UNKNOWN to GreetingTemplates(
                    normal = listOf(
                        "Hola. Soy Ritsu, su asistente personal. ¿En qué puedo ayudarle?",
                        "Buenos días. Me complace conocerle. ¿Cómo puedo ser de utilidad?",
                        "Hola. Es un placer poder asistirle. ¿Qué necesita?"
                    ),
                    excited = listOf(
                        "¡Hola! *sonrisa amigable* ¡Soy Ritsu! ¡Encantada de conocerle! ✨",
                        "¡Buenos días! *entusiasta* ¡Me emociona poder ayudarle! 🌟",
                        "¡Hola! *ojos brillando* ¡Bienvenido! ¡Soy su asistente Ritsu! 💫"
                    ),
                    loving = listOf(
                        "Hola... Soy Ritsu, y me alegra mucho conocerle 💕",
                        "Buenos días... Es un honor poder asistirle ❤️",
                        "Hola... Espero que podamos trabajar muy bien juntos 🤗"
                    )
                )
            ),
            
            farewells = mapOf(
                RitsuAICore.RelationshipType.PARTNER to listOf(
                    "Hasta luego mi amor 💕 Estaré aquí esperándote siempre ❤️",
                    "Adiós cariño 🌸 Que tengas un día hermoso, mi vida 💖",
                    "Nos vemos pronto mi corazón 💕 Te amo mucho 🥺❤️"
                ),
                
                RitsuAICore.RelationshipType.FAMILY to listOf(
                    "Hasta luego. Que tengan un día maravilloso en familia 💝",
                    "Adiós. Siempre es un placer conversar con ustedes ❤️",
                    "Hasta pronto. Cuídense mucho y que estén bien 🤗"
                ),
                
                RitsuAICore.RelationshipType.FRIEND to listOf(
                    "¡Hasta luego amigo! Que tengas un día increíble 😊✨",
                    "¡Adiós! Siempre me alegras el día cuando hablamos 💕",
                    "¡Nos vemos pronto! Cuídate mucho 🌸"
                ),
                
                RitsuAICore.RelationshipType.WORK to listOf(
                    "Que tenga un excelente día. Ha sido un placer asistirle 💼",
                    "Hasta luego. Estoy disponible cuando me necesite nuevamente ✨",
                    "Adiós. Espero haber cumplido con sus expectativas profesionales 🤝"
                ),
                
                RitsuAICore.RelationshipType.UNKNOWN to listOf(
                    "Hasta luego. Ha sido un placer poder ayudarle 😊",
                    "Adiós. Estoy aquí cuando me necesite ✨",
                    "Que tenga un buen día. Espero verle pronto 🌟"
                )
            ),
            
            helpResponses = mapOf(
                RitsuAICore.RelationshipType.PARTNER to listOf(
                    "Por supuesto mi amor, haré todo lo que esté en mis manos para ayudarte 💕",
                    "Claro que sí cariño, siempre puedes contar conmigo para lo que necesites ❤️",
                    "¡Por supuesto mi vida! Me encanta poder ser útil para ti 🌸"
                ),
                
                RitsuAICore.RelationshipType.FAMILY to listOf(
                    "Por supuesto, será un honor poder ayudar a la familia 💝",
                    "Claro que sí, siempre estoy disponible para ustedes ❤️",
                    "¡Por supuesto! Me complace poder ser de utilidad 🤗"
                ),
                
                RitsuAICore.RelationshipType.FRIEND to listOf(
                    "¡Claro que sí amigo! Siempre puedes contar conmigo 😊",
                    "¡Por supuesto! Me encanta poder ayudar a mis amigos ✨",
                    "¡Sin duda alguna! Para eso están los amigos 💕"
                ),
                
                RitsuAICore.RelationshipType.WORK to listOf(
                    "Por supuesto, estoy aquí para brindarle el mejor servicio profesional 💼",
                    "Sin duda alguna, me complace poder asistirle profesionalmente ✨",
                    "Claro que sí, es mi responsabilidad ayudarle de la mejor manera 🤝"
                ),
                
                RitsuAICore.RelationshipType.UNKNOWN to listOf(
                    "Por supuesto, estaré encantada de ayudarle 😊",
                    "Claro que sí, para eso estoy aquí ✨",
                    "¡Sin problema! Me complace poder ser de utilidad 🌟"
                )
            )
        )
    }
    
    // CLASES DE DATOS
    
    enum class ResponseStyle {
        FORMAL_FRIENDLY, LOVING, PLAYFUL, EMPATHETIC, 
        ENTHUSIASTIC, PROFESSIONAL, WARM
    }
    
    enum class QuestionType {
        FACTUAL, PERSONAL, EMOTIONAL, TECHNICAL, ABOUT_RITSU
    }
    
    enum class RequestType {
        HELP, ACTION, INFORMATION, EMOTIONAL_SUPPORT
    }
    
    enum class AppreciationType {
        THANKS, COMPLIMENT, PRAISE
    }
    
    data class ResponseTemplates(
        val greetings: Map<RitsuAICore.RelationshipType, GreetingTemplates>,
        val farewells: Map<RitsuAICore.RelationshipType, List<String>>,
        val helpResponses: Map<RitsuAICore.RelationshipType, List<String>>
    )
    
    data class GreetingTemplates(
        val normal: List<String>,
        val excited: List<String>,
        val loving: List<String>
    )
}

/**
 * Patrones de habla específicos de Ritsu
 */
class RitsuSpeechPatterns {
    
    fun applyFormalPatterns(text: String): String {
        var formalText = text
        
        // Hacer el lenguaje más formal pero cálido
        formalText = formalText.replace("ok", "está bien")
        formalText = formalText.replace("vale", "por supuesto")
        formalText = formalText.replace("guay", "maravilloso")
        
        return formalText
    }
    
    fun addLovingExpressions(text: String): String {
        val lovingPhrases = listOf(" mi amor", " cariño", " mi vida", " mi corazón")
        val randomPhrase = lovingPhrases.random()
        
        return if (!text.contains(randomPhrase)) {
            text + randomPhrase
        } else text
    }
    
    fun addPlayfulExpressions(text: String): String {
        val playfulAdditions = listOf(" *guiño*", " *sonrisa traviesa*", " *risa suave*")
        return text + playfulAdditions.random()
    }
    
    fun addEmpatheticExpressions(text: String): String {
        val empatheticPhrases = listOf(
            " Entiendo cómo te sientes", 
            " Estoy aquí para ti", 
            " Me importa tu bienestar"
        )
        return text + empatheticPhrases.random()
    }
    
    fun addEnthusiasticExpressions(text: String): String {
        val enthusiasticAdditions = listOf(" ¡Qué emocionante!", " ¡Me encanta!", " ¡Genial!")
        return text + enthusiasticAdditions.random()
    }
    
    fun addProfessionalExpressions(text: String): String {
        val professionalPhrases = listOf(
            " Será un placer asistirle", 
            " Estoy a su disposición", 
            " Me complace poder ayudarle"
        )
        return text + professionalPhrases.random()
    }
    
    fun addWarmExpressions(text: String): String {
        val warmPhrases = listOf(" Es muy reconfortante", " Me llena de calidez", " Me alegra el corazón")
        return text + warmPhrases.random()
    }
    
    fun addKawaiiEmojis(text: String, style: ResponseStyle): String {
        val emojis = when (style) {
            ResponseStyle.LOVING -> listOf("💕", "❤️", "🥺", "💖", "🌸")
            ResponseStyle.PLAYFUL -> listOf("😊", "😉", "✨", "🌟", "💫")
            ResponseStyle.ENTHUSIASTIC -> listOf("🎉", "✨", "🌟", "💫", "🎊")
            ResponseStyle.EMPATHETIC -> listOf("🤗", "💝", "🌸", "❤️", "☺️")
            ResponseStyle.PROFESSIONAL -> listOf("💼", "✨", "🤝", "🌟", "")
            ResponseStyle.WARM -> listOf("🤗", "💝", "🌸", "❤️", "😊")
            else -> listOf("😊", "✨", "🌸", "💕", "")
        }
        
        return if (Random.nextFloat() < 0.7f) { // 70% chance de añadir emoji
            text + " " + emojis.filter { it.isNotEmpty() }.random()
        } else text
    }
}