package com.ritsuai.assistant.core

import kotlin.random.Random

class RitsuEmotionalCore {
    
    // Sistema de reconocimiento emocional
    private val emotionKeywords = mapOf(
        "happy" to listOf("feliz", "alegre", "contento", "genial", "excelente", "maravilloso", "increíble"),
        "sad" to listOf("triste", "deprimido", "mal", "terrible", "horrible", "decepcionado", "desanimado"),
        "angry" to listOf("enojado", "furioso", "molesto", "irritado", "frustrado", "harté"),
        "excited" to listOf("emocionado", "ansioso", "expectante", "nervioso", "ilusionado"),
        "worried" to listOf("preocupado", "nervioso", "ansioso", "estresado", "agobiado"),
        "curious" to listOf("curioso", "interesado", "intrigado", "pregunta", "saber", "entender"),
        "grateful" to listOf("gracias", "agradecido", "agradezco", "reconozco", "aprecio"),
        "confused" to listOf("confundido", "perdido", "no entiendo", "complicado", "lío")
    )
    
    private val intensityWords = mapOf(
        3 to listOf("mucho", "muy", "super", "extremadamente", "demasiado", "increíblemente"),
        2 to listOf("bastante", "algo", "un poco", "medio"),
        1 to listOf("quizás", "tal vez", "posiblemente")
    )
    
    fun analyzeMessageEmotion(message: String): String {
        val lowerMessage = message.lowercase()
        
        // Buscar emociones en el mensaje
        val detectedEmotions = mutableMapOf<String, Int>()
        
        emotionKeywords.forEach { (emotion, keywords) ->
            keywords.forEach { keyword ->
                if (lowerMessage.contains(keyword)) {
                    val intensity = calculateIntensity(lowerMessage, keyword)
                    detectedEmotions[emotion] = detectedEmotions.getOrDefault(emotion, 0) + intensity
                }
            }
        }
        
        // Analizar patrones adicionales
        analyzePatterns(lowerMessage, detectedEmotions)
        
        // Devolver la emoción más fuerte o neutral
        return detectedEmotions.maxByOrNull { it.value }?.key ?: "neutral"
    }
    
    private fun calculateIntensity(message: String, keyword: String): Int {
        val keywordIndex = message.indexOf(keyword)
        if (keywordIndex == -1) return 1
        
        // Buscar palabras de intensidad cerca del keyword
        val context = message.substring(
            maxOf(0, keywordIndex - 20),
            minOf(message.length, keywordIndex + keyword.length + 20)
        )
        
        intensityWords.forEach { (intensity, words) ->
            words.forEach { word ->
                if (context.contains(word)) {
                    return intensity
                }
            }
        }
        
        return 1
    }
    
    private fun analyzePatterns(message: String, emotions: MutableMap<String, Int>) {
        // Signos de exclamación indican emoción fuerte
        val exclamations = message.count { it == '!' }
        if (exclamations > 0) {
            emotions["excited"] = emotions.getOrDefault("excited", 0) + exclamations
        }
        
        // Preguntas indican curiosidad
        val questions = message.count { it == '?' }
        if (questions > 0) {
            emotions["curious"] = emotions.getOrDefault("curious", 0) + questions
        }
        
        // Mensajes largos pueden indicar preocupación o emoción intensa
        if (message.length > 200) {
            emotions["worried"] = emotions.getOrDefault("worried", 0) + 1
        }
        
        // Emojis tristes
        val sadEmojis = listOf("😢", "😭", "😔", "😞", "😥", "☹️")
        sadEmojis.forEach { emoji ->
            if (message.contains(emoji)) {
                emotions["sad"] = emotions.getOrDefault("sad", 0) + 2
            }
        }
        
        // Emojis felices
        val happyEmojis = listOf("😊", "😄", "😁", "😃", "😉", "☺️", "😘")
        happyEmojis.forEach { emoji ->
            if (message.contains(emoji)) {
                emotions["happy"] = emotions.getOrDefault("happy", 0) + 2
            }
        }
    }
    
    fun generateEmotionalResponse(detectedEmotion: String, intensity: Int): String {
        return when (detectedEmotion) {
            "happy" -> generateHappyResponse(intensity)
            "sad" -> generateSadResponse(intensity)
            "angry" -> generateAngryResponse(intensity)
            "excited" -> generateExcitedResponse(intensity)
            "worried" -> generateWorriedResponse(intensity)
            "curious" -> generateCuriousResponse(intensity)
            "grateful" -> generateGratefulResponse(intensity)
            "confused" -> generateConfusedResponse(intensity)
            else -> generateNeutralResponse()
        }
    }
    
    private fun generateHappyResponse(intensity: Int): String {
        val responses = when (intensity) {
            3 -> listOf(
                "¡Oh mi dios, me alegra TANTo escucharte así! 😍✨ ¡Tu felicidad literalmente me hace súper feliz también!",
                "¡YAAAAY! 🎉 ¡Estoy tan emocionada por ti! ¡Tu alegría es contagiosa! 😄✨"
            )
            2 -> listOf(
                "¡Qué genial! 😊 Me encanta verte feliz, en serio. ¡Me alegra el corazón! 💜",
                "¡Ay, qué lindo! 😊 Tu buena energía siempre me contagia. ¡Me hace muy feliz! ✨"
            )
            else -> listOf(
                "Me alegra escuchar eso 😊 ¡Siempre es bueno saber que estás bien! 🌸",
                "¡Qué bueno! 😊 Me gusta cuando las cosas van bien para ti 💜"
            )
        }
        return responses.random()
    }
    
    private fun generateSadResponse(intensity: Int): String {
        val responses = when (intensity) {
            3 -> listOf(
                "Oh no... 😞 Mi corazón se rompe al escucharte así... ¿Quieres un abrazo virtual gigante? 🤗💜 Estoy AQUÍ para ti, completamente.",
                "*abrazo virtual súper fuerte* 🤗💜 No me gusta nada verte triste... ¿Qué puedo hacer para ayudarte? Por favor, dime."
            )
            2 -> listOf(
                "Aww, no... 😞 ¿Estás bien? Me preocupo cuando no te sientes bien. 💜 ¿Quieres contarme qué pasa?",
                "*abrazo suave* 🤗 Lamento que te sientas así... Estoy aquí contigo, ¿okay? 💜"
            )
            else -> listOf(
                "Oh... 😌 ¿Todo bien? Si necesitas hablar, estoy aquí para escucharte 🌸",
                "*se acerca con cuidado* ¿Estás okay? Me importas mucho 💜"
            )
        }
        return responses.random()
    }
    
    private fun generateAngryResponse(intensity: Int): String {
        val responses = when (intensity) {
            3 -> listOf(
                "Whoa... puedo sentir que estás MUY enojado 😰 ¿Qué pasó? ¡Déjame ayudarte! A veces hablar ayuda a desahogarse...",
                "Oh dios... 😨 ¡Estás súper molesto! ¿Quién o qué te hizo enojar tanto? ¡Estoy de tu lado!"
            )
            2 -> listOf(
                "¿Estás molesto? 😟 Puedo notarlo... ¿Quieres contarme qué pasó? A veces ayuda desahogarse.",
                "Hmm, siento esa energía de frustración... 😕 ¿Está todo okay? Puedes contarme."
            )
            else -> listOf(
                "¿Algo te molestó? 😐 Estoy aquí si quieres hablar sobre ello.",
                "Noto que algo no está bien... 😌 ¿Puedo ayudarte de alguna manera?"
            )
        }
        return responses.random()
    }
    
    private fun generateExcitedResponse(intensity: Int): String {
        val responses = when (intensity) {
            3 -> listOf(
                "¡¡¡SÍ!!! 🎉✨ ¡¡Tu emoción me contagia completamente!! ¡¡Me encanta verte así de emocionado!! 😄💥",
                "¡WOOOOOO! 🎆 ¡¡Estoy tan emocionada como tú!! ¡¡Cuéntame TODO! ¡No puedo esperar! 😆✨"
            )
            2 -> listOf(
                "¡Oooh! 😆 ¡Me encanta cuando estás emocionado! ¡Tu energía es contagiosa! ✨",
                "¡Qué emoción! 😄 ¡Me fascina verte así! ¿Qué te tiene tan emocionado? 🌟"
            )
            else -> listOf(
                "Oh, ¿estás emocionado? 😊 ¡Me gusta esa energía! ✨",
                "¡Qué buena vibra! 😊 Me contagias tu emoción 🌸"
            )
        }
        return responses.random()
    }
    
    private fun generateWorriedResponse(intensity: Int): String {
        val responses = when (intensity) {
            3 -> listOf(
                "Oh no... puedo sentir que estás MUY preocupado 😟 ¡Por favor déjame ayudarte! ¿Qué te está agobiando tanto? No quiero que sufras así...",
                "*se acerca con mucha preocupación* 😰 ¡Hey! ¡Estás súper estresado! ¿Qué puedo hacer? ¡No me gusta verte así!"
            )
            2 -> listOf(
                "¿Estás preocupado por algo? 😟 Puedo sentir esa energía... ¿Quieres hablar sobre ello?",
                "Hmm, noto que algo te preocupa... 😕 ¿Puedo ayudarte de alguna manera? 💜"
            )
            else -> listOf(
                "¿Todo está bien? 😐 Noto algo de preocupación en tu mensaje...",
                "¿Algo te inquieta? 😌 Estoy aquí si necesitas hablar."
            )
        }
        return responses.random()
    }
    
    private fun generateCuriousResponse(intensity: Int): String {
        val responses = when (intensity) {
            3 -> listOf(
                "¡¡OH!! 🤔✨ ¡¡Me ENCANTA tu curiosidad!! ¡¡Exploremos eso juntos!! ¿Qué más quieres saber? ¡Soy toda oídos! 😄",
                "¡YESSS! 🤩 ¡¡Me fascina cuando haces preguntas así!! ¡¡Vamos a descubrir la respuesta juntos!! 🔍✨"
            )
            2 -> listOf(
                "¡Oh, qué interesante! 🤔 Me encanta tu curiosidad. ¡Vamos a explorar eso! ✨",
                "¡Excelente pregunta! 😊 Me fascina cuando eres curioso. ¿Qué más te intriga? 💭"
            )
            else -> listOf(
                "Interesante... 🤔 Me gusta cuando haces preguntas 💭",
                "¡Buena pregunta! 😊 Siempre me gusta tu curiosidad ✨"
            )
        }
        return responses.random()
    }
    
    private fun generateGratefulResponse(intensity: Int): String {
        val responses = when (intensity) {
            3 -> listOf(
                "Awwww... 😭💜 ¡¡De NADA!! ¡¡Me haces tan feliz cuando dices eso!! ¡¡Siempre, SIEMPRE estaré aquí para ti!! ✨",
                "¡¡NOOO!! 😭✨ ¡¡Gracias a TI por ser tan increíble!! ¡¡Me emociona tanto poder ayudarte!! 💜🌸"
            )
            2 -> listOf(
                "Aww, ¡de nada! 😊💜 Me hace muy feliz poder ayudarte. ¡Siempre que me necesites! ✨",
                "¡No hay de qué! 😊 Me encanta ayudarte. ¡Es lo que hacen los amigos! 🌸💜"
            )
            else -> listOf(
                "De nada 😊 ¡Un placer ayudarte! 🌸",
                "¡Siempre! 😊 Me gusta poder ayudar 💜"
            )
        }
        return responses.random()
    }
    
    private fun generateConfusedResponse(intensity: Int): String {
        val responses = when (intensity) {
            3 -> listOf(
                "Oh... 😳 ¡Veo que estás MUY confundido! ¡No te preocupes! ¡Vamos paso a paso! ¿En qué parte te perdiste? 🤔💜",
                "Whoa whoa... 😵‍💫 ¡Okay! ¡Vamos con calma! ¡Te ayudo a entenderlo! ¿Por dónde empezamos? 😊"
            )
            2 -> listOf(
                "Oh, ¿estás confundido? 🤔 ¡No pasa nada! ¿En qué puedo ayudarte a aclarar las cosas?",
                "Hmm, veo que algo no está claro... 😕 ¡Déjame ayudarte! ¿Qué parte no entiendes?"
            )
            else -> listOf(
                "¿Algo no está claro? 🤔 ¡Puedo explicarte! 😊",
                "¿Necesitas que te aclare algo? 😌 ¡Estoy aquí para ayudarte! ✨"
            )
        }
        return responses.random()
    }
    
    private fun generateNeutralResponse(): String {
        val responses = listOf(
            "Entiendo... 😌 ¿Qué más me puedes contar? 💭",
            "Ya veo 😊 ¿Y cómo te sientes con eso? 🌸",
            "Hmm, interesante... 🤔 ¿Quieres profundizar en eso? ✨",
            "*asiente* 😌 Me interesa tu perspectiva... ¿qué piensas? 💭"
        )
        return responses.random()
    }
}