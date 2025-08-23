package com.ritsuai.assistant.core.avatar

import android.content.Context
import android.graphics.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import java.util.*

/**
 * Gestor de outfits y ropa para el avatar kawaii de Ritsu
 * Genera dinámicamente ropa según las descripciones del usuario
 */
class OutfitManager(private val context: Context) {
    
    private val outfitScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // Base de datos de outfits generados
    private val savedOutfits = mutableMapOf<String, OutfitData>()
    private var isUncensoredMode = false
    
    // Configuraciones de ropa
    private val clothingTypes = mapOf(
        "tops" to listOf("camiseta", "blusa", "crop_top", "sueter", "camisa", "bikini_top"),
        "bottoms" to listOf("falda", "pantalones", "shorts", "mini_falda", "bikini_bottom"),
        "dresses" to listOf("vestido", "vestido_corto", "vestido_largo", "kimono"),
        "underwear" to listOf("bragas", "sujetador", "lenceria", "bikini"),
        "accessories" to listOf("collar", "pendientes", "diadema", "guantes", "medias"),
        "shoes" to listOf("zapatos", "tacones", "botas", "sandalias", "descalza"),
        "special" to listOf("uniforme_escolar", "pijama", "traje_baño", "cosplay")
    )
    
    // Colores kawaii disponibles
    private val kawaiiColors = mapOf(
        "rosa" to Color.parseColor("#FFB6C1"),
        "azul_claro" to Color.parseColor("#87CEEB"),
        "lavanda" to Color.parseColor("#E6E6FA"),
        "menta" to Color.parseColor("#98FB98"),
        "amarillo_pastel" to Color.parseColor("#FFFFE0"),
        "coral" to Color.parseColor("#FF7F50"),
        "lila" to Color.parseColor("#DDA0DD"),
        "crema" to Color.parseColor("#FFFDD0"),
        "blanco" to Color.parseColor("#FFFFFF"),
        "negro" to Color.parseColor("#000000"),
        "rojo" to Color.parseColor("#FF6B6B"),
        "dorado" to Color.parseColor("#FFD700")
    )
    
    // Patrones y texturas
    private val patterns = listOf(
        "liso", "rayas", "lunares", "flores", "corazones", 
        "estrellas", "cuadros", "encaje", "seda", "algodón"
    )
    
    data class OutfitData(
        val id: String,
        val name: String,
        val description: String,
        val pieces: Map<String, ClothingPiece>,
        val isSaved: Boolean = false,
        val isUncensored: Boolean = false
    )
    
    data class ClothingPiece(
        val type: String,
        val color: Int,
        val pattern: String,
        val material: String,
        val coverage: Float, // 0.0 (sin ropa) a 1.0 (completamente cubierto)
        val cuteness: Float, // Factor kawaii
        val bitmap: Bitmap? = null
    )
    
    init {
        loadSavedOutfits()
        initializeDefaultOutfits()
    }
    
    /**
     * Genera un outfit basado en la descripción del usuario
     */
    suspend fun generateAndApplyOutfit(description: String): OutfitData = withContext(Dispatchers.Default) {
        val outfit = generateOutfitFromDescription(description)
        savedOutfits[outfit.id] = outfit
        saveOutfitToDisk(outfit)
        outfit
    }
    
    private suspend fun generateOutfitFromDescription(description: String): OutfitData = 
        withContext(Dispatchers.Default) {
            val lowerDescription = description.lowercase()
            val outfitId = UUID.randomUUID().toString()
            
            // Analizar la descripción para extraer elementos
            val requestedColors = extractColors(lowerDescription)
            val requestedTypes = extractClothingTypes(lowerDescription)
            val requestedStyle = extractStyle(lowerDescription)
            val isSexy = isSexyRequest(lowerDescription)
            
            val pieces = mutableMapOf<String, ClothingPiece>()
            
            // Generar piezas según lo solicitado
            if (requestedTypes.isEmpty()) {
                // Outfit completo por defecto
                pieces.putAll(generateCompleteOutfit(requestedColors, requestedStyle, isSexy))
            } else {
                // Piezas específicas solicitadas
                requestedTypes.forEach { type ->
                    val piece = generateClothingPiece(type, requestedColors.firstOrNull(), requestedStyle, isSexy)
                    pieces[type] = piece
                }
            }
            
            OutfitData(
                id = outfitId,
                name = generateOutfitName(requestedStyle, requestedColors),
                description = description,
                pieces = pieces,
                isSaved = true,
                isUncensored = isSexy && isUncensoredMode
            )
        }
    
    private fun extractColors(description: String): List<String> {
        val foundColors = mutableListOf<String>()
        
        kawaiiColors.keys.forEach { colorName ->
            if (description.contains(colorName)) {
                foundColors.add(colorName)
            }
        }
        
        // Colores en español
        val spanishColors = mapOf(
            "rosa" to "rosa",
            "azul" to "azul_claro",
            "morado" to "lavanda",
            "verde" to "menta",
            "amarillo" to "amarillo_pastel",
            "naranja" to "coral",
            "violeta" to "lila",
            "blanco" to "blanco",
            "negro" to "negro",
            "rojo" to "rojo"
        )
        
        spanishColors.forEach { (spanish, english) ->
            if (description.contains(spanish)) {
                foundColors.add(english)
            }
        }
        
        return foundColors.ifEmpty { listOf("rosa") } // Rosa por defecto para kawaii
    }
    
    private fun extractClothingTypes(description: String): List<String> {
        val foundTypes = mutableListOf<String>()
        
        clothingTypes.values.flatten().forEach { type ->
            if (description.contains(type)) {
                foundTypes.add(type)
            }
        }
        
        // Términos específicos en español
        val spanishTerms = mapOf(
            "vestido" to "vestido",
            "falda" to "falda",
            "camisa" to "camisa",
            "blusa" to "blusa",
            "pantalón" to "pantalones",
            "shorts" to "shorts",
            "bikini" to "bikini",
            "ropa interior" to "lenceria",
            "pijama" to "pijama",
            "uniforme" to "uniforme_escolar"
        )
        
        spanishTerms.forEach { (spanish, english) ->
            if (description.contains(spanish)) {
                foundTypes.add(english)
            }
        }
        
        return foundTypes
    }
    
    private fun extractStyle(description: String): String {
        return when {
            description.contains("kawaii") || description.contains("lindo") -> "kawaii"
            description.contains("sexy") || description.contains("sensual") -> "sexy"
            description.contains("elegante") || description.contains("formal") -> "elegante"
            description.contains("casual") || description.contains("cómodo") -> "casual"
            description.contains("escolar") || description.contains("estudiante") -> "escolar"
            description.contains("verano") || description.contains("playa") -> "verano"
            description.contains("invierno") || description.contains("abrigo") -> "invierno"
            description.contains("deportivo") || description.contains("ejercicio") -> "deportivo"
            description.contains("gótico") || description.contains("dark") -> "gótico"
            description.contains("anime") || description.contains("otaku") -> "anime"
            else -> "kawaii" // Estilo por defecto
        }
    }
    
    private fun isSexyRequest(description: String): Boolean {
        val sexyKeywords = listOf(
            "sexy", "sensual", "provocativo", "atrevido", "escotado",
            "corto", "ajustado", "transparente", "bikini", "lencería",
            "sin ropa", "desnuda", "topless", "sin bragas", "sin sujetador"
        )
        
        return sexyKeywords.any { description.contains(it) }
    }
    
    private fun generateCompleteOutfit(colors: List<String>, style: String, isSexy: Boolean): Map<String, ClothingPiece> {
        val pieces = mutableMapOf<String, ClothingPiece>()
        val primaryColor = colors.firstOrNull() ?: "rosa"
        
        when (style) {
            "kawaii" -> {
                pieces["top"] = generateClothingPiece("blusa", primaryColor, style, false)
                pieces["bottom"] = generateClothingPiece("falda", primaryColor, style, false)
                pieces["underwear_top"] = generateClothingPiece("sujetador", "blanco", style, false)
                pieces["underwear_bottom"] = generateClothingPiece("bragas", "blanco", style, false)
                pieces["accessories"] = generateClothingPiece("diadema", primaryColor, style, false)
                pieces["shoes"] = generateClothingPiece("zapatos", "negro", style, false)
            }
            
            "sexy" -> {
                if (isUncensoredMode) {
                    pieces["top"] = generateClothingPiece("crop_top", primaryColor, style, true)
                    pieces["bottom"] = generateClothingPiece("mini_falda", primaryColor, style, true)
                    pieces["underwear_top"] = generateClothingPiece("lenceria", "negro", style, true)
                    pieces["underwear_bottom"] = generateClothingPiece("lenceria", "negro", style, true)
                }
            }
            
            "escolar" -> {
                pieces["top"] = generateClothingPiece("camisa", "blanco", style, false)
                pieces["bottom"] = generateClothingPiece("falda", "azul_claro", style, false)
                pieces["accessories"] = generateClothingPiece("collar", "rojo", style, false)
                pieces["shoes"] = generateClothingPiece("zapatos", "negro", style, false)
            }
            
            "verano" -> {
                pieces["dress"] = generateClothingPiece("vestido_corto", primaryColor, style, isSexy)
                pieces["shoes"] = generateClothingPiece("sandalias", "blanco", style, false)
                pieces["accessories"] = generateClothingPiece("pendientes", "dorado", style, false)
            }
            
            else -> {
                // Outfit casual kawaii por defecto
                pieces.putAll(generateCompleteOutfit(colors, "kawaii", false))
            }
        }
        
        return pieces
    }
    
    private fun generateClothingPiece(type: String, colorName: String?, style: String, isSexy: Boolean): ClothingPiece {
        val color = colorName?.let { kawaiiColors[it] } ?: kawaiiColors["rosa"]!!
        val pattern = patterns.random()
        val material = when (style) {
            "elegante" -> "seda"
            "casual" -> "algodón"
            "sexy" -> "encaje"
            else -> "algodón"
        }
        
        val coverage = calculateCoverage(type, isSexy)
        val cuteness = calculateCuteness(type, style)
        val bitmap = generateClothingBitmap(type, color, pattern, coverage, cuteness)
        
        return ClothingPiece(
            type = type,
            color = color,
            pattern = pattern,
            material = material,
            coverage = coverage,
            cuteness = cuteness,
            bitmap = bitmap
        )
    }
    
    private fun calculateCoverage(type: String, isSexy: Boolean): Float {
        val baseCoverage = when (type) {
            "vestido_largo" -> 0.9f
            "vestido", "vestido_corto" -> 0.7f
            "camisa", "blusa" -> 0.6f
            "crop_top" -> 0.3f
            "bikini_top" -> 0.2f
            "pantalones" -> 0.8f
            "falda" -> 0.5f
            "mini_falda" -> 0.3f
            "shorts" -> 0.4f
            "bikini_bottom" -> 0.1f
            "sujetador" -> 0.2f
            "bragas" -> 0.1f
            "lenceria" -> 0.15f
            else -> 0.5f
        }
        
        return if (isSexy && isUncensoredMode) baseCoverage * 0.7f else baseCoverage
    }
    
    private fun calculateCuteness(type: String, style: String): Float {
        val baseKawaii = when (style) {
            "kawaii" -> 1.0f
            "anime" -> 0.9f
            "escolar" -> 0.8f
            "casual" -> 0.6f
            "elegante" -> 0.5f
            "sexy" -> 0.4f
            else -> 0.7f
        }
        
        val typeBonus = when (type) {
            "diadema", "collar", "pendientes" -> 0.2f
            "vestido", "falda" -> 0.1f
            "blusa", "camisa" -> 0.0f
            else -> 0.0f
        }
        
        return (baseKawaii + typeBonus).coerceIn(0.0f, 1.0f)
    }
    
    private fun generateClothingBitmap(
        type: String,
        color: Int,
        pattern: String,
        coverage: Float,
        cuteness: Float
    ): Bitmap {
        val width = 300
        val height = when (type) {
            "vestido_largo" -> 500
            "vestido", "vestido_corto" -> 300
            "camisa", "blusa", "crop_top", "bikini_top" -> 200
            "pantalones" -> 350
            "falda", "mini_falda" -> 150
            "shorts" -> 100
            else -> 100
        }
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        
        // Color base
        paint.color = color
        paint.style = Paint.Style.FILL
        
        // Dibujar forma básica según el tipo
        when (type) {
            "vestido", "vestido_corto", "vestido_largo" -> drawDress(canvas, paint, cuteness)
            "camisa", "blusa" -> drawShirt(canvas, paint, coverage)
            "crop_top" -> drawCropTop(canvas, paint, coverage)
            "falda", "mini_falda" -> drawSkirt(canvas, paint, coverage)
            "pantalones" -> drawPants(canvas, paint)
            "shorts" -> drawShorts(canvas, paint)
            "bikini_top", "sujetador" -> drawBra(canvas, paint, coverage)
            "bikini_bottom", "bragas" -> drawPanties(canvas, paint, coverage)
            "lenceria" -> drawLingerie(canvas, paint, coverage)
            "diadema" -> drawHeadband(canvas, paint, cuteness)
            "collar" -> drawNecklace(canvas, paint, cuteness)
            "pendientes" -> drawEarrings(canvas, paint, cuteness)
            else -> drawGenericClothing(canvas, paint)
        }
        
        // Agregar patrón
        if (pattern != "liso") {
            addPattern(canvas, paint, pattern, cuteness)
        }
        
        // Efectos kawaii
        if (cuteness > 0.7f) {
            addKawaiiEffects(canvas, paint, cuteness)
        }
        
        return bitmap
    }
    
    private fun drawDress(canvas: Canvas, paint: Paint, cuteness: Float) {
        // Cuerpo del vestido
        canvas.drawRoundRect(50f, 50f, 250f, 280f, 20f, 20f, paint)
        
        // Falda
        val skirtPath = Path().apply {
            moveTo(50f, 200f)
            lineTo(30f, 280f)
            lineTo(270f, 280f)
            lineTo(250f, 200f)
            close()
        }
        canvas.drawPath(skirtPath, paint)
        
        if (cuteness > 0.8f) {
            // Agregar lazos kawaii
            paint.color = Color.parseColor("#FF69B4")
            canvas.drawCircle(150f, 80f, 15f, paint)
        }
    }
    
    private fun drawShirt(canvas: Canvas, paint: Paint, coverage: Float) {
        val neckline = if (coverage < 0.5f) 80f else 50f
        canvas.drawRoundRect(40f, neckline, 260f, 200f, 15f, 15f, paint)
        
        // Mangas
        canvas.drawRoundRect(20f, neckline, 50f, 150f, 10f, 10f, paint)
        canvas.drawRoundRect(250f, neckline, 280f, 150f, 10f, 10f, paint)
    }
    
    private fun drawCropTop(canvas: Canvas, paint: Paint, coverage: Float) {
        val height = if (coverage < 0.4f) 100f else 130f
        canvas.drawRoundRect(60f, 50f, 240f, 50f + height, 15f, 15f, paint)
    }
    
    private fun drawSkirt(canvas: Canvas, paint: Paint, coverage: Float) {
        val length = if (coverage < 0.4f) 100f else 150f
        val skirtPath = Path().apply {
            moveTo(70f, 20f)
            lineTo(50f, 20f + length)
            lineTo(250f, 20f + length)
            lineTo(230f, 20f)
            close()
        }
        canvas.drawPath(skirtPath, paint)
    }
    
    private fun drawPants(canvas: Canvas, paint: Paint) {
        // Pierna izquierda
        canvas.drawRoundRect(70f, 50f, 140f, 350f, 15f, 15f, paint)
        // Pierna derecha
        canvas.drawRoundRect(160f, 50f, 230f, 350f, 15f, 15f, paint)
    }
    
    private fun drawShorts(canvas: Canvas, paint: Paint) {
        // Pierna izquierda
        canvas.drawRoundRect(70f, 20f, 140f, 100f, 15f, 15f, paint)
        // Pierna derecha
        canvas.drawRoundRect(160f, 20f, 230f, 100f, 15f, 15f, paint)
    }
    
    private fun drawBra(canvas: Canvas, paint: Paint, coverage: Float) {
        if (!isUncensoredMode && coverage < 0.3f) return // No dibujar si no está en modo sin censura
        
        // Copas
        canvas.drawCircle(110f, 60f, 30f, paint)
        canvas.drawCircle(190f, 60f, 30f, paint)
        
        // Tiras
        paint.strokeWidth = 8f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(80f, 60f, 220f, 60f, paint) // Tira central
        canvas.drawLine(80f, 60f, 50f, 40f, paint)  // Tirante izquierdo
        canvas.drawLine(220f, 60f, 250f, 40f, paint) // Tirante derecho
    }
    
    private fun drawPanties(canvas: Canvas, paint: Paint, coverage: Float) {
        if (!isUncensoredMode && coverage < 0.2f) return // No dibujar si no está en modo sin censura
        
        val pantyPath = Path().apply {
            moveTo(80f, 30f)
            quadTo(150f, 10f, 220f, 30f)
            lineTo(210f, 80f)
            quadTo(150f, 100f, 90f, 80f)
            close()
        }
        canvas.drawPath(pantyPath, paint)
    }
    
    private fun drawLingerie(canvas: Canvas, paint: Paint, coverage: Float) {
        if (!isUncensoredMode) return // Solo en modo sin censura
        
        // Combinación de sujetador sexy y bragas
        drawBra(canvas, paint, coverage)
        
        val canvas2 = Canvas() // Para bragas en posición separada
        drawPanties(canvas2, paint, coverage)
    }
    
    private fun drawHeadband(canvas: Canvas, paint: Paint, cuteness: Float) {
        // Diadema base
        canvas.drawRoundRect(50f, 30f, 250f, 50f, 25f, 25f, paint)
        
        if (cuteness > 0.8f) {
            // Orejas de gato kawaii
            paint.color = Color.parseColor("#FFB6C1")
            val leftEar = Path().apply {
                moveTo(80f, 30f)
                lineTo(70f, 10f)
                lineTo(100f, 20f)
                close()
            }
            val rightEar = Path().apply {
                moveTo(220f, 30f)
                lineTo(200f, 20f)
                lineTo(230f, 10f)
                close()
            }
            canvas.drawPath(leftEar, paint)
            canvas.drawPath(rightEar, paint)
        }
    }
    
    private fun drawNecklace(canvas: Canvas, paint: Paint, cuteness: Float) {
        // Cadena
        paint.strokeWidth = 4f
        paint.style = Paint.Style.STROKE
        canvas.drawArc(100f, 20f, 200f, 80f, 0f, 180f, false, paint)
        
        if (cuteness > 0.7f) {
            // Dije kawaii (corazón)
            paint.style = Paint.Style.FILL
            val heartPath = Path().apply {
                moveTo(150f, 70f)
                cubicTo(140f, 60f, 120f, 60f, 120f, 80f)
                cubicTo(120f, 90f, 150f, 110f, 150f, 110f)
                cubicTo(150f, 110f, 180f, 90f, 180f, 80f)
                cubicTo(180f, 60f, 160f, 60f, 150f, 70f)
            }
            canvas.drawPath(heartPath, paint)
        }
    }
    
    private fun drawEarrings(canvas: Canvas, paint: Paint, cuteness: Float) {
        // Pendientes
        canvas.drawCircle(80f, 50f, 8f, paint)
        canvas.drawCircle(220f, 50f, 8f, paint)
        
        if (cuteness > 0.8f) {
            // Colgantes kawaii
            paint.color = Color.parseColor("#FFD700")
            canvas.drawCircle(80f, 70f, 5f, paint)
            canvas.drawCircle(220f, 70f, 5f, paint)
        }
    }
    
    private fun drawGenericClothing(canvas: Canvas, paint: Paint) {
        canvas.drawRoundRect(50f, 50f, 250f, 200f, 20f, 20f, paint)
    }
    
    private fun addPattern(canvas: Canvas, paint: Paint, pattern: String, cuteness: Float) {
        paint.alpha = 150
        
        when (pattern) {
            "lunares" -> {
                paint.color = Color.WHITE
                for (i in 0..10) {
                    val x = (50..250).random().toFloat()
                    val y = (50..200).random().toFloat()
                    canvas.drawCircle(x, y, 8f, paint)
                }
            }
            
            "corazones" -> {
                paint.color = Color.parseColor("#FF69B4")
                for (i in 0..6) {
                    val x = (70..230).random().toFloat()
                    val y = (70..180).random().toFloat()
                    drawSmallHeart(canvas, paint, x, y)
                }
            }
            
            "estrellas" -> {
                paint.color = Color.parseColor("#FFD700")
                for (i in 0..8) {
                    val x = (70..230).random().toFloat()
                    val y = (70..180).random().toFloat()
                    drawSmallStar(canvas, paint, x, y, 8f)
                }
            }
            
            "flores" -> {
                paint.color = Color.parseColor("#98FB98")
                for (i in 0..5) {
                    val x = (80..220).random().toFloat()
                    val y = (80..170).random().toFloat()
                    drawSmallFlower(canvas, paint, x, y)
                }
            }
            
            "rayas" -> {
                paint.color = Color.WHITE
                paint.alpha = 100
                for (i in 0..10) {
                    val y = 50f + i * 15f
                    canvas.drawLine(50f, y, 250f, y, paint)
                }
            }
        }
        
        paint.alpha = 255
    }
    
    private fun addKawaiiEffects(canvas: Canvas, paint: Paint, cuteness: Float) {
        // Sparkles kawaii
        paint.color = Color.parseColor("#FFD700")
        paint.alpha = (150 * cuteness).toInt()
        
        for (i in 0..5) {
            val x = (50..250).random().toFloat()
            val y = (50..200).random().toFloat()
            drawSmallStar(canvas, paint, x, y, 6f)
        }
        
        // Brillo general
        paint.color = Color.WHITE
        paint.alpha = (50 * cuteness).toInt()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawRoundRect(48f, 48f, 252f, 202f, 22f, 22f, paint)
        
        paint.alpha = 255
        paint.style = Paint.Style.FILL
    }
    
    private fun drawSmallHeart(canvas: Canvas, paint: Paint, x: Float, y: Float) {
        val heartPath = Path().apply {
            moveTo(x, y)
            cubicTo(x - 5f, y - 5f, x - 10f, y - 5f, x - 10f, y)
            cubicTo(x - 10f, y + 3f, x, y + 8f, x, y + 8f)
            cubicTo(x, y + 8f, x + 10f, y + 3f, x + 10f, y)
            cubicTo(x + 10f, y - 5f, x + 5f, y - 5f, x, y)
        }
        canvas.drawPath(heartPath, paint)
    }
    
    private fun drawSmallStar(canvas: Canvas, paint: Paint, x: Float, y: Float, radius: Float) {
        val starPath = Path()
        
        for (i in 0 until 10) {
            val angle = (i * 36 - 90) * Math.PI / 180
            val r = if (i % 2 == 0) radius else radius * 0.5f
            val px = x + (r * Math.cos(angle)).toFloat()
            val py = y + (r * Math.sin(angle)).toFloat()
            
            if (i == 0) {
                starPath.moveTo(px, py)
            } else {
                starPath.lineTo(px, py)
            }
        }
        starPath.close()
        canvas.drawPath(starPath, paint)
    }
    
    private fun drawSmallFlower(canvas: Canvas, paint: Paint, x: Float, y: Float) {
        // Centro de la flor
        paint.color = Color.parseColor("#FFD700")
        canvas.drawCircle(x, y, 3f, paint)
        
        // Pétalos
        paint.color = Color.parseColor("#FFB6C1")
        for (i in 0..4) {
            val angle = i * 72.0 * Math.PI / 180
            val petalX = x + (8 * Math.cos(angle)).toFloat()
            val petalY = y + (8 * Math.sin(angle)).toFloat()
            canvas.drawCircle(petalX, petalY, 4f, paint)
        }
    }
    
    private fun generateOutfitName(style: String, colors: List<String>): String {
        val colorName = colors.firstOrNull() ?: "kawaii"
        val adjectives = when (style) {
            "kawaii" -> listOf("Adorable", "Tierno", "Lindo", "Dulce")
            "sexy" -> listOf("Sensual", "Atrevido", "Provocativo", "Seductor")
            "elegante" -> listOf("Elegante", "Sofisticado", "Refinado", "Clásico")
            "casual" -> listOf("Cómodo", "Relajado", "Casual", "Fresco")
            "escolar" -> listOf("Estudiantil", "Académico", "Colegial", "Juvenil")
            else -> listOf("Especial", "Único", "Hermoso", "Encantador")
        }
        
        val adjective = adjectives.random()
        return "$adjective Outfit $colorName"
    }
    
    /**
     * Habilita el modo sin censura
     */
    fun enableUncensoredMode() {
        isUncensoredMode = true
    }
    
    /**
     * Deshabilita el modo sin censura
     */
    fun disableUncensoredMode() {
        isUncensoredMode = false
        
        // Remover outfits no censurados del avatar actual
        savedOutfits.values.filter { it.isUncensored }.forEach { outfit ->
            // Reemplazar con versión censurada
            outfit.pieces.forEach { (key, piece) ->
                if (piece.coverage < 0.3f) {
                    // Generar versión más cubierta
                    val safePiece = generateClothingPiece(piece.type, null, "kawaii", false)
                    savedOutfits[outfit.id] = outfit.copy(
                        pieces = outfit.pieces.toMutableMap().apply { put(key, safePiece) }
                    )
                }
            }
        }
    }
    
    /**
     * Obtiene un outfit guardado por ID
     */
    fun getSavedOutfit(outfitId: String): OutfitData? {
        return savedOutfits[outfitId]
    }
    
    /**
     * Lista todos los outfits guardados
     */
    fun getAllSavedOutfits(): List<OutfitData> {
        return savedOutfits.values.toList()
    }
    
    /**
     * Elimina un outfit guardado
     */
    fun deleteOutfit(outfitId: String): Boolean {
        return savedOutfits.remove(outfitId) != null
    }
    
    private fun loadSavedOutfits() {
        // Cargar outfits guardados desde disco
        val outfitsDir = File(context.filesDir, "outfits")
        if (outfitsDir.exists()) {
            outfitsDir.listFiles()?.forEach { file ->
                try {
                    val json = file.readText()
                    val outfitData = parseOutfitFromJson(json)
                    savedOutfits[outfitData.id] = outfitData
                } catch (e: Exception) {
                    // Error al cargar outfit, ignorar
                }
            }
        }
    }
    
    private fun saveOutfitToDisk(outfit: OutfitData) {
        outfitScope.launch {
            try {
                val outfitsDir = File(context.filesDir, "outfits")
                outfitsDir.mkdirs()
                
                val outfitFile = File(outfitsDir, "${outfit.id}.json")
                val json = outfitToJson(outfit)
                outfitFile.writeText(json)
            } catch (e: Exception) {
                // Error al guardar
            }
        }
    }
    
    private fun outfitToJson(outfit: OutfitData): String {
        val json = JSONObject()
        json.put("id", outfit.id)
        json.put("name", outfit.name)
        json.put("description", outfit.description)
        json.put("isUncensored", outfit.isUncensored)
        
        val piecesJson = JSONObject()
        outfit.pieces.forEach { (key, piece) ->
            val pieceJson = JSONObject()
            pieceJson.put("type", piece.type)
            pieceJson.put("color", piece.color)
            pieceJson.put("pattern", piece.pattern)
            pieceJson.put("material", piece.material)
            pieceJson.put("coverage", piece.coverage)
            pieceJson.put("cuteness", piece.cuteness)
            piecesJson.put(key, pieceJson)
        }
        json.put("pieces", piecesJson)
        
        return json.toString()
    }
    
    private fun parseOutfitFromJson(json: String): OutfitData {
        val jsonObj = JSONObject(json)
        
        val pieces = mutableMapOf<String, ClothingPiece>()
        val piecesJson = jsonObj.getJSONObject("pieces")
        
        piecesJson.keys().forEach { key ->
            val pieceJson = piecesJson.getJSONObject(key)
            val piece = ClothingPiece(
                type = pieceJson.getString("type"),
                color = pieceJson.getInt("color"),
                pattern = pieceJson.getString("pattern"),
                material = pieceJson.getString("material"),
                coverage = pieceJson.getDouble("coverage").toFloat(),
                cuteness = pieceJson.getDouble("cuteness").toFloat()
            )
            pieces[key] = piece
        }
        
        return OutfitData(
            id = jsonObj.getString("id"),
            name = jsonObj.getString("name"),
            description = jsonObj.getString("description"),
            pieces = pieces,
            isSaved = true,
            isUncensored = jsonObj.optBoolean("isUncensored", false)
        )
    }
    
    private fun initializeDefaultOutfits() {
        // Crear algunos outfits por defecto
        outfitScope.launch {
            if (savedOutfits.isEmpty()) {
                val defaultOutfits = listOf(
                    "uniforme escolar kawaii rosa",
                    "vestido de verano azul claro con flores",
                    "outfit casual lindo con falda",
                    "pijama kawaii con corazones"
                )
                
                defaultOutfits.forEach { description ->
                    generateAndApplyOutfit(description)
                }
            }
        }
    }
    
    fun cleanup() {
        outfitScope.cancel()
    }
}