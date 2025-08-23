package com.ritsu.ai.util

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*

class ClothingGenerator(private val context: Context) {
    
    companion object {
        private const val CLOTHING_SIZE = 512
        private const val CLOTHING_QUALITY = 100
        private const val MAX_GENERATED_ITEMS = 100
    }
    
    // Estilos de ropa disponibles
    private val clothingStyles = mapOf(
        "casual" to listOf("camiseta", "pantalón", "zapatillas", "chaqueta"),
        "elegante" to listOf("vestido", "tacones", "bolso", "joyas"),
        "deportivo" to listOf("chándal", "zapatillas deportivas", "gorra", "calcetines"),
        "escolar" to listOf("uniforme", "zapatos", "mochila", "corbata"),
        "formal" to listOf("traje", "camisa", "pantalón", "zapatos"),
        "playa" to listOf("bañador", "sombrero", "gafas", "toalla"),
        "invierno" to listOf("abrigo", "bufanda", "guantes", "botas"),
        "verano" to listOf("vestido ligero", "sandalias", "sombrero", "gafas de sol")
    )
    
    // Colores disponibles
    private val colorPalettes = mapOf(
        "pastel" to listOf(
            Color.rgb(255, 182, 193), // Rosa claro
            Color.rgb(173, 216, 230), // Azul claro
            Color.rgb(144, 238, 144), // Verde claro
            Color.rgb(255, 218, 185), // Melocotón
            Color.rgb(221, 160, 221)  // Lavanda
        ),
        "vibrante" to listOf(
            Color.rgb(255, 0, 0),     // Rojo
            Color.rgb(0, 255, 0),     // Verde
            Color.rgb(0, 0, 255),     // Azul
            Color.rgb(255, 255, 0),   // Amarillo
            Color.rgb(255, 0, 255)    // Magenta
        ),
        "neutro" to listOf(
            Color.rgb(128, 128, 128), // Gris
            Color.rgb(0, 0, 0),       // Negro
            Color.rgb(255, 255, 255), // Blanco
            Color.rgb(139, 69, 19),   // Marrón
            Color.rgb(105, 105, 105)  // Gris oscuro
        ),
        "tierra" to listOf(
            Color.rgb(160, 82, 45),   // Marrón silla
            Color.rgb(139, 69, 19),   // Marrón silla
            Color.rgb(205, 133, 63),  // Marrón peru
            Color.rgb(244, 164, 96),  // Marrón arena
            Color.rgb(210, 180, 140)  // Marrón trigo
        )
    )
    
    // Patrones disponibles
    private val patterns = listOf(
        "liso", "rayas", "cuadros", "flores", "puntos", "geométrico", "animal", "abstracto"
    )
    
    // Estado del generador
    private var generatedItems: MutableList<ClothingItem> = mutableListOf()
    private var userPreferences: MutableMap<String, Any> = mutableMapOf()
    
    suspend fun initialize() {
        withContext(Dispatchers.IO) {
            try {
                // Cargar items generados previamente
                loadGeneratedItems()
                
                // Cargar preferencias del usuario
                loadUserPreferences()
                
                // Crear directorio para ropa si no existe
                createClothingDirectory()
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun createClothingDirectory() {
        val clothingDir = File(context.getExternalFilesDir(null), "clothing")
        if (!clothingDir.exists()) {
            clothingDir.mkdirs()
        }
    }
    
    suspend fun generateClothing(description: String): ClothingItem {
        return withContext(Dispatchers.Default) {
            try {
                // Analizar descripción
                val analysis = analyzeClothingDescription(description)
                
                // Generar ropa según el análisis
                val clothing = generateClothingFromAnalysis(analysis)
                
                // Guardar item generado
                saveGeneratedItem(clothing)
                
                // Actualizar preferencias del usuario
                updateUserPreferences(analysis)
                
                clothing
                
            } catch (e: Exception) {
                // Generar item por defecto si hay error
                generateDefaultClothing()
            }
        }
    }
    
    private fun analyzeClothingDescription(description: String): ClothingAnalysis {
        val lowerDescription = description.lowercase()
        
        return ClothingAnalysis(
            style = detectStyle(lowerDescription),
            colors = detectColors(lowerDescription),
            pattern = detectPattern(lowerDescription),
            season = detectSeason(lowerDescription),
            occasion = detectOccasion(lowerDescription),
            mood = detectMood(lowerDescription),
            complexity = detectComplexity(lowerDescription)
        )
    }
    
    private fun detectStyle(description: String): String {
        return when {
            description.contains("casual") || description.contains("cómodo") -> "casual"
            description.contains("elegante") || description.contains("formal") -> "elegante"
            description.contains("deportivo") || description.contains("gimnasio") -> "deportivo"
            description.contains("escolar") || description.contains("uniforme") -> "escolar"
            description.contains("traje") || description.contains("oficina") -> "formal"
            description.contains("playa") || description.contains("verano") -> "playa"
            description.contains("invierno") || description.contains("frío") -> "invierno"
            description.contains("verano") || description.contains("calor") -> "verano"
            else -> "casual"
        }
    }
    
    private fun detectColors(description: String): List<Int> {
        val colors = mutableListOf<Int>()
        
        when {
            description.contains("rosa") || description.contains("pink") -> colors.add(Color.rgb(255, 182, 193))
            description.contains("azul") || description.contains("blue") -> colors.add(Color.rgb(173, 216, 230))
            description.contains("verde") || description.contains("green") -> colors.add(Color.rgb(144, 238, 144))
            description.contains("rojo") || description.contains("red") -> colors.add(Color.rgb(255, 0, 0))
            description.contains("amarillo") || description.contains("yellow") -> colors.add(Color.rgb(255, 255, 0))
            description.contains("negro") || description.contains("black") -> colors.add(Color.BLACK)
            description.contains("blanco") || description.contains("white") -> colors.add(Color.WHITE)
            description.contains("gris") || description.contains("gray") -> colors.add(Color.rgb(128, 128, 128))
            description.contains("morado") || description.contains("purple") -> colors.add(Color.rgb(128, 0, 128))
            description.contains("naranja") || description.contains("orange") -> colors.add(Color.rgb(255, 165, 0))
        }
        
        // Si no se especificaron colores, usar paleta por defecto
        if (colors.isEmpty()) {
            colors.addAll(colorPalettes["pastel"] ?: emptyList())
        }
        
        return colors
    }
    
    private fun detectPattern(description: String): String {
        return when {
            description.contains("rayas") || description.contains("stripes") -> "rayas"
            description.contains("cuadros") || description.contains("checkered") -> "cuadros"
            description.contains("flores") || description.contains("floral") -> "flores"
            description.contains("puntos") || description.contains("polka") -> "puntos"
            description.contains("geométrico") || description.contains("geometric") -> "geométrico"
            description.contains("animal") || description.contains("leopard") -> "animal"
            description.contains("abstracto") || description.contains("abstract") -> "abstracto"
            else -> "liso"
        }
    }
    
    private fun detectSeason(description: String): String {
        return when {
            description.contains("invierno") || description.contains("frío") -> "invierno"
            description.contains("verano") || description.contains("calor") -> "verano"
            description.contains("primavera") || description.contains("spring") -> "primavera"
            description.contains("otoño") || description.contains("fall") -> "otoño"
            else -> "todas"
        }
    }
    
    private fun detectOccasion(description: String): String {
        return when {
            description.contains("trabajo") || description.contains("oficina") -> "trabajo"
            description.contains("fiesta") || description.contains("celebración") -> "fiesta"
            description.contains("deporte") || description.contains("ejercicio") -> "deporte"
            description.contains("casa") || description.contains("hogar") -> "casual"
            description.contains("escuela") || description.contains("estudio") -> "escuela"
            description.contains("cita") || description.contains("romántico") -> "romántico"
            else -> "casual"
        }
    }
    
    private fun detectMood(description: String): String {
        return when {
            description.contains("alegre") || description.contains("feliz") -> "alegre"
            description.contains("triste") || description.contains("melancólico") -> "triste"
            description.contains("energético") || description.contains("activo") -> "energético"
            description.contains("tranquilo") || description.contains("calmado") -> "tranquilo"
            description.contains("elegante") || description.contains("sofisticado") -> "elegante"
            description.contains("divertido") || description.contains("juguetón") -> "divertido"
            else -> "neutral"
        }
    }
    
    private fun detectComplexity(description: String): Int {
        val wordCount = description.split(" ").size
        return when {
            wordCount > 10 -> 3
            wordCount > 5 -> 2
            else -> 1
        }
    }
    
    private fun generateClothingFromAnalysis(analysis: ClothingAnalysis): ClothingItem {
        val itemId = "clothing_${System.currentTimeMillis()}"
        val itemName = generateItemName(analysis)
        val itemDescription = generateItemDescription(analysis)
        
        // Generar imagen de la ropa
        val imagePath = generateClothingImage(analysis, itemId)
        
        return ClothingItem(
            id = itemId,
            name = itemName,
            description = itemDescription,
            imagePath = imagePath,
            category = analysis.style,
            isSpecial = analysis.complexity > 2
        )
    }
    
    private fun generateItemName(analysis: ClothingAnalysis): String {
        val styleNames = mapOf(
            "casual" to "Outfit Casual",
            "elegante" to "Conjunto Elegante",
            "deportivo" to "Equipo Deportivo",
            "escolar" to "Uniforme Escolar",
            "formal" to "Traje Formal",
            "playa" to "Conjunto de Playa",
            "invierno" to "Outfit de Invierno",
            "verano" to "Conjunto de Verano"
        )
        
        val baseName = styleNames[analysis.style] ?: "Outfit Personalizado"
        val patternSuffix = if (analysis.pattern != "liso") " ${analysis.pattern.capitalize()}" else ""
        val moodSuffix = if (analysis.mood != "neutral") " ${analysis.mood.capitalize()}" else ""
        
        return "$baseName$patternSuffix$moodSuffix"
    }
    
    private fun generateItemDescription(analysis: ClothingAnalysis): String {
        val descriptions = mutableListOf<String>()
        
        // Estilo
        descriptions.add("Estilo ${analysis.style}")
        
        // Colores
        if (analysis.colors.isNotEmpty()) {
            val colorNames = analysis.colors.map { getColorName(it) }
            descriptions.add("Colores: ${colorNames.joinToString(", ")}")
        }
        
        // Patrón
        if (analysis.pattern != "liso") {
            descriptions.add("Patrón: ${analysis.pattern}")
        }
        
        // Temporada
        if (analysis.season != "todas") {
            descriptions.add("Ideal para ${analysis.season}")
        }
        
        // Ocasión
        descriptions.add("Perfecto para ${analysis.occasion}")
        
        // Estado de ánimo
        if (analysis.mood != "neutral") {
            descriptions.add("Transmite ${analysis.mood}")
        }
        
        return descriptions.joinToString(". ")
    }
    
    private fun getColorName(color: Int): String {
        return when (color) {
            Color.rgb(255, 182, 193) -> "Rosa claro"
            Color.rgb(173, 216, 230) -> "Azul claro"
            Color.rgb(144, 238, 144) -> "Verde claro"
            Color.rgb(255, 218, 185) -> "Melocotón"
            Color.rgb(221, 160, 221) -> "Lavanda"
            Color.rgb(255, 0, 0) -> "Rojo"
            Color.rgb(0, 255, 0) -> "Verde"
            Color.rgb(0, 0, 255) -> "Azul"
            Color.rgb(255, 255, 0) -> "Amarillo"
            Color.rgb(255, 0, 255) -> "Magenta"
            Color.BLACK -> "Negro"
            Color.WHITE -> "Blanco"
            Color.rgb(128, 128, 128) -> "Gris"
            else -> "Color personalizado"
        }
    }
    
    private fun generateClothingImage(analysis: ClothingAnalysis, itemId: String): String {
        try {
            // Crear bitmap para la ropa
            val bitmap = Bitmap.createBitmap(CLOTHING_SIZE, CLOTHING_SIZE, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            // Fondo transparente
            canvas.drawColor(Color.TRANSPARENT)
            
            // Generar ropa según el análisis
            when (analysis.style) {
                "casual" -> drawCasualClothing(canvas, analysis)
                "elegante" -> drawElegantClothing(canvas, analysis)
                "deportivo" -> drawSportClothing(canvas, analysis)
                "escolar" -> drawSchoolClothing(canvas, analysis)
                "formal" -> drawFormalClothing(canvas, analysis)
                "playa" -> drawBeachClothing(canvas, analysis)
                "invierno" -> drawWinterClothing(canvas, analysis)
                "verano" -> drawSummerClothing(canvas, analysis)
                else -> drawCasualClothing(canvas, analysis)
            }
            
            // Aplicar patrón si es necesario
            if (analysis.pattern != "liso") {
                applyPattern(canvas, analysis.pattern, analysis.colors)
            }
            
            // Guardar imagen
            val imageFile = File(context.getExternalFilesDir(null), "clothing/$itemId.png")
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, CLOTHING_QUALITY, outputStream)
            outputStream.close()
            
            // Liberar bitmap
            bitmap.recycle()
            
            return imageFile.absolutePath
            
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }
    
    private fun drawCasualClothing(canvas: Canvas, analysis: ClothingAnalysis) {
        val paint = Paint().apply {
            color = analysis.colors.firstOrNull() ?: Color.rgb(255, 182, 193)
            isAntiAlias = true
        }
        
        // Camiseta
        val shirtRect = RectF(
            CLOTHING_SIZE * 0.25f,
            CLOTHING_SIZE * 0.3f,
            CLOTHING_SIZE * 0.75f,
            CLOTHING_SIZE * 0.6f
        )
        canvas.drawRect(shirtRect, paint)
        
        // Manga izquierda
        val leftSleeveRect = RectF(
            CLOTHING_SIZE * 0.15f,
            CLOTHING_SIZE * 0.35f,
            CLOTHING_SIZE * 0.25f,
            CLOTHING_SIZE * 0.55f
        )
        canvas.drawRect(leftSleeveRect, paint)
        
        // Manga derecha
        val rightSleeveRect = RectF(
            CLOTHING_SIZE * 0.75f,
            CLOTHING_SIZE * 0.35f,
            CLOTHING_SIZE * 0.85f,
            CLOTHING_SIZE * 0.55f
        )
        canvas.drawRect(rightSleeveRect, paint)
        
        // Pantalón
        val pantsPaint = Paint().apply {
            color = analysis.colors.getOrNull(1) ?: Color.rgb(173, 216, 230)
            isAntiAlias = true
        }
        
        val pantsRect = RectF(
            CLOTHING_SIZE * 0.3f,
            CLOTHING_SIZE * 0.6f,
            CLOTHING_SIZE * 0.7f,
            CLOTHING_SIZE * 0.9f
        )
        canvas.drawRect(pantsRect, pantsPaint)
    }
    
    private fun drawElegantClothing(canvas: Canvas, analysis: ClothingAnalysis) {
        val paint = Paint().apply {
            color = analysis.colors.firstOrNull() ?: Color.rgb(128, 0, 128)
            isAntiAlias = true
        }
        
        // Vestido elegante
        val dressPath = android.graphics.Path()
        dressPath.moveTo(CLOTHING_SIZE * 0.2f, CLOTHING_SIZE * 0.3f)
        dressPath.lineTo(CLOTHING_SIZE * 0.8f, CLOTHING_SIZE * 0.3f)
        dressPath.lineTo(CLOTHING_SIZE * 0.7f, CLOTHING_SIZE * 0.9f)
        dressPath.lineTo(CLOTHING_SIZE * 0.3f, CLOTHING_SIZE * 0.9f)
        dressPath.close()
        canvas.drawPath(dressPath, paint)
        
        // Cinturón
        val beltPaint = Paint().apply {
            color = analysis.colors.getOrNull(1) ?: Color.rgb(255, 215, 0)
            isAntiAlias = true
            strokeWidth = 10f
        }
        canvas.drawLine(
            CLOTHING_SIZE * 0.25f, CLOTHING_SIZE * 0.6f,
            CLOTHING_SIZE * 0.75f, CLOTHING_SIZE * 0.6f,
            beltPaint
        )
    }
    
    private fun drawSportClothing(canvas: Canvas, analysis: ClothingAnalysis) {
        val paint = Paint().apply {
            color = analysis.colors.firstOrNull() ?: Color.rgb(255, 0, 0)
            isAntiAlias = true
        }
        
        // Chándal
        val tracksuitRect = RectF(
            CLOTHING_SIZE * 0.2f,
            CLOTHING_SIZE * 0.3f,
            CLOTHING_SIZE * 0.8f,
            CLOTHING_SIZE * 0.9f
        )
        canvas.drawRect(tracksuitRect, paint)
        
        // Rayas deportivas
        val stripePaint = Paint().apply {
            color = analysis.colors.getOrNull(1) ?: Color.WHITE
            isAntiAlias = true
            strokeWidth = 8f
        }
        
        for (i in 0..3) {
            val y = CLOTHING_SIZE * (0.35f + i * 0.12f)
            canvas.drawLine(
                CLOTHING_SIZE * 0.25f, y,
                CLOTHING_SIZE * 0.75f, y,
                stripePaint
            )
        }
    }
    
    private fun drawSchoolClothing(canvas: Canvas, analysis: ClothingAnalysis) {
        val paint = Paint().apply {
            color = analysis.colors.firstOrNull() ?: Color.rgb(100, 149, 237)
            isAntiAlias = true
        }
        
        // Uniforme escolar
        val uniformRect = RectF(
            CLOTHING_SIZE * 0.2f,
            CLOTHING_SIZE * 0.3f,
            CLOTHING_SIZE * 0.8f,
            CLOTHING_SIZE * 0.9f
        )
        canvas.drawRect(uniformRect, paint)
        
        // Corbata
        val tiePaint = Paint().apply {
            color = analysis.colors.getOrNull(1) ?: Color.rgb(139, 69, 19)
            isAntiAlias = true
        }
        
        val tiePath = android.graphics.Path()
        tiePath.moveTo(CLOTHING_SIZE * 0.5f, CLOTHING_SIZE * 0.35f)
        tiePath.lineTo(CLOTHING_SIZE * 0.45f, CLOTHING_SIZE * 0.7f)
        tiePath.lineTo(CLOTHING_SIZE * 0.55f, CLOTHING_SIZE * 0.7f)
        tiePath.close()
        canvas.drawPath(tiePath, tiePaint)
    }
    
    private fun drawFormalClothing(canvas: Canvas, analysis: ClothingAnalysis) {
        val paint = Paint().apply {
            color = analysis.colors.firstOrNull() ?: Color.BLACK
            isAntiAlias = true
        }
        
        // Traje
        val suitRect = RectF(
            CLOTHING_SIZE * 0.2f,
            CLOTHING_SIZE * 0.3f,
            CLOTHING_SIZE * 0.8f,
            CLOTHING_SIZE * 0.9f
        )
        canvas.drawRect(suitRect, paint)
        
        // Camisa
        val shirtPaint = Paint().apply {
            color = analysis.colors.getOrNull(1) ?: Color.WHITE
            isAntiAlias = true
        }
        
        val shirtRect = RectF(
            CLOTHING_SIZE * 0.25f,
            CLOTHING_SIZE * 0.35f,
            CLOTHING_SIZE * 0.75f,
            CLOTHING_SIZE * 0.6f
        )
        canvas.drawRect(shirtRect, shirtPaint)
    }
    
    private fun drawBeachClothing(canvas: Canvas, analysis: ClothingAnalysis) {
        val paint = Paint().apply {
            color = analysis.colors.firstOrNull() ?: Color.rgb(255, 182, 193)
            isAntiAlias = true
        }
        
        // Bañador
        val swimsuitRect = RectF(
            CLOTHING_SIZE * 0.3f,
            CLOTHING_SIZE * 0.4f,
            CLOTHING_SIZE * 0.7f,
            CLOTHING_SIZE * 0.8f
        )
        canvas.drawRect(swimsuitRect, paint)
        
        // Sombrero de playa
        val hatPaint = Paint().apply {
            color = analysis.colors.getOrNull(1) ?: Color.rgb(255, 215, 0)
            isAntiAlias = true
        }
        
        val hatRect = RectF(
            CLOTHING_SIZE * 0.2f,
            CLOTHING_SIZE * 0.1f,
            CLOTHING_SIZE * 0.8f,
            CLOTHING_SIZE * 0.4f
        )
        canvas.drawOval(hatRect, hatPaint)
    }
    
    private fun drawWinterClothing(canvas: Canvas, analysis: ClothingAnalysis) {
        val paint = Paint().apply {
            color = analysis.colors.firstOrNull() ?: Color.rgb(70, 130, 180)
            isAntiAlias = true
        }
        
        // Abrigo
        val coatRect = RectF(
            CLOTHING_SIZE * 0.15f,
            CLOTHING_SIZE * 0.25f,
            CLOTHING_SIZE * 0.85f,
            CLOTHING_SIZE * 0.95f
        )
        canvas.drawRect(coatRect, paint)
        
        // Bufanda
        val scarfPaint = Paint().apply {
            color = analysis.colors.getOrNull(1) ?: Color.rgb(255, 0, 0)
            isAntiAlias = true
        }
        
        val scarfRect = RectF(
            CLOTHING_SIZE * 0.3f,
            CLOTHING_SIZE * 0.3f,
            CLOTHING_SIZE * 0.7f,
            CLOTHING_SIZE * 0.45f
        )
        canvas.drawRect(scarfRect, scarfPaint)
    }
    
    private fun drawSummerClothing(canvas: Canvas, analysis: ClothingAnalysis) {
        val paint = Paint().apply {
            color = analysis.colors.firstOrNull() ?: Color.rgb(255, 218, 185)
            isAntiAlias = true
        }
        
        // Vestido de verano
        val dressPath = android.graphics.Path()
        dressPath.moveTo(CLOTHING_SIZE * 0.2f, CLOTHING_SIZE * 0.3f)
        dressPath.lineTo(CLOTHING_SIZE * 0.8f, CLOTHING_SIZE * 0.3f)
        dressPath.lineTo(CLOTHING_SIZE * 0.6f, CLOTHING_SIZE * 0.9f)
        dressPath.lineTo(CLOTHING_SIZE * 0.4f, CLOTHING_SIZE * 0.9f)
        dressPath.close()
        canvas.drawPath(dressPath, paint)
        
        // Sombrero de verano
        val hatPaint = Paint().apply {
            color = analysis.colors.getOrNull(1) ?: Color.rgb(255, 255, 0)
            isAntiAlias = true
        }
        
        val hatRect = RectF(
            CLOTHING_SIZE * 0.25f,
            CLOTHING_SIZE * 0.15f,
            CLOTHING_SIZE * 0.75f,
            CLOTHING_SIZE * 0.35f
        )
        canvas.drawOval(hatRect, hatPaint)
    }
    
    private fun applyPattern(canvas: Canvas, pattern: String, colors: List<Int>) {
        when (pattern) {
            "rayas" -> drawStripes(canvas, colors)
            "cuadros" -> drawCheckered(canvas, colors)
            "flores" -> drawFlowers(canvas, colors)
            "puntos" -> drawPolkaDots(canvas, colors)
            "geométrico" -> drawGeometric(canvas, colors)
            "animal" -> drawAnimalPrint(canvas, colors)
            "abstracto" -> drawAbstract(canvas, colors)
        }
    }
    
    private fun drawStripes(canvas: Canvas, colors: List<Int>) {
        val paint = Paint().apply {
            color = colors.getOrNull(1) ?: Color.WHITE
            isAntiAlias = true
            strokeWidth = 8f
        }
        
        for (i in 0..10) {
            val x = CLOTHING_SIZE * (i * 0.1f)
            canvas.drawLine(x, 0f, x, CLOTHING_SIZE.toFloat(), paint)
        }
    }
    
    private fun drawCheckered(canvas: Canvas, colors: List<Int>) {
        val paint = Paint().apply {
            color = colors.getOrNull(1) ?: Color.WHITE
            isAntiAlias = true
        }
        
        val squareSize = CLOTHING_SIZE / 8f
        for (i in 0..7) {
            for (j in 0..7) {
                if ((i + j) % 2 == 0) {
                    val rect = RectF(
                        i * squareSize,
                        j * squareSize,
                        (i + 1) * squareSize,
                        (j + 1) * squareSize
                    )
                    canvas.drawRect(rect, paint)
                }
            }
        }
    }
    
    private fun drawFlowers(canvas: Canvas, colors: List<Int>) {
        val paint = Paint().apply {
            color = colors.getOrNull(1) ?: Color.rgb(255, 182, 193)
            isAntiAlias = true
        }
        
        // Dibujar flores simples
        for (i in 0..3) {
            for (j in 0..3) {
                val centerX = CLOTHING_SIZE * (0.2f + i * 0.2f)
                val centerY = CLOTHING_SIZE * (0.2f + j * 0.2f)
                val radius = 20f
                
                // Pétalos
                for (k in 0..7) {
                    val angle = k * Math.PI / 4
                    val petalX = centerX + (radius * Math.cos(angle)).toFloat()
                    val petalY = centerY + (radius * Math.sin(angle)).toFloat()
                    canvas.drawCircle(petalX, petalY, 8f, paint)
                }
                
                // Centro
                val centerPaint = Paint().apply {
                    color = colors.getOrNull(2) ?: Color.rgb(255, 255, 0)
                    isAntiAlias = true
                }
                canvas.drawCircle(centerX, centerY, 12f, centerPaint)
            }
        }
    }
    
    private fun drawPolkaDots(canvas: Canvas, colors: List<Int>) {
        val paint = Paint().apply {
            color = colors.getOrNull(1) ?: Color.WHITE
            isAntiAlias = true
        }
        
        for (i in 0..5) {
            for (j in 0..5) {
                val x = CLOTHING_SIZE * (0.1f + i * 0.15f)
                val y = CLOTHING_SIZE * (0.1f + j * 0.15f)
                canvas.drawCircle(x, y, 15f, paint)
            }
        }
    }
    
    private fun drawGeometric(canvas: Canvas, colors: List<Int>) {
        val paint = Paint().apply {
            color = colors.getOrNull(1) ?: Color.rgb(255, 0, 0)
            isAntiAlias = true
        }
        
        // Triángulos
        for (i in 0..2) {
            val path = android.graphics.Path()
            val startX = CLOTHING_SIZE * (0.1f + i * 0.3f)
            path.moveTo(startX, CLOTHING_SIZE * 0.2f)
            path.lineTo(startX + 50f, CLOTHING_SIZE * 0.2f)
            path.lineTo(startX + 25f, CLOTHING_SIZE * 0.4f)
            path.close()
            canvas.drawPath(path, paint)
        }
    }
    
    private fun drawAnimalPrint(canvas: Canvas, colors: List<Int>) {
        val paint = Paint().apply {
            color = colors.getOrNull(1) ?: Color.rgb(139, 69, 19)
            isAntiAlias = true
        }
        
        // Manchas de leopardo
        for (i in 0..8) {
            val x = CLOTHING_SIZE * (0.1f + Math.random() * 0.8f).toFloat()
            val y = CLOTHING_SIZE * (0.1f + Math.random() * 0.8f).toFloat()
            val radius = (10f + Math.random() * 20f).toFloat()
            canvas.drawCircle(x, y, radius, paint)
        }
    }
    
    private fun drawAbstract(canvas: Canvas, colors: List<Int>) {
        val paint = Paint().apply {
            color = colors.getOrNull(1) ?: Color.rgb(128, 0, 128)
            isAntiAlias = true
            strokeWidth = 5f
        }
        
        // Líneas abstractas
        for (i in 0..5) {
            val startX = CLOTHING_SIZE * Math.random().toFloat()
            val startY = CLOTHING_SIZE * Math.random().toFloat()
            val endX = CLOTHING_SIZE * Math.random().toFloat()
            val endY = CLOTHING_SIZE * Math.random().toFloat()
            
            canvas.drawLine(startX, startY, endX, endY, paint)
        }
    }
    
    private fun saveGeneratedItem(item: ClothingItem) {
        generatedItems.add(item)
        
        // Mantener solo los últimos items
        if (generatedItems.size > MAX_GENERATED_ITEMS) {
            generatedItems.removeAt(0)
        }
        
        // Guardar en archivo
        saveGeneratedItemsToFile()
    }
    
    private fun saveGeneratedItemsToFile() {
        try {
            val itemsFile = File(context.getExternalFilesDir(null), "generated_clothing.json")
            // Implementar guardado en JSON
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun loadGeneratedItems() {
        try {
            val itemsFile = File(context.getExternalFilesDir(null), "generated_clothing.json")
            if (itemsFile.exists()) {
                // Cargar items desde JSON
                // Implementar parser JSON
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun updateUserPreferences(analysis: ClothingAnalysis) {
        // Actualizar preferencias basadas en el análisis
        userPreferences["preferred_style"] = analysis.style
        userPreferences["preferred_colors"] = analysis.colors
        userPreferences["preferred_pattern"] = analysis.pattern
        
        // Guardar preferencias
        saveUserPreferences()
    }
    
    private fun loadUserPreferences() {
        try {
            val prefsFile = File(context.getExternalFilesDir(null), "clothing_preferences.json")
            if (prefsFile.exists()) {
                // Cargar preferencias desde JSON
                // Implementar parser JSON
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun saveUserPreferences() {
        try {
            val prefsFile = File(context.getExternalFilesDir(null), "clothing_preferences.json")
            // Implementar guardado en JSON
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun generateDefaultClothing(): ClothingItem {
        return ClothingItem(
            id = "default_clothing",
            name = "Outfit Básico",
            description = "Un outfit simple y cómodo para uso diario",
            imagePath = "",
            category = "casual",
            isSpecial = false
        )
    }
    
    fun getGeneratedItems(): List<ClothingItem> {
        return generatedItems.toList()
    }
    
    fun cleanup() {
        // Limpiar recursos si es necesario
        generatedItems.clear()
    }
    
    // Clases de datos
    data class ClothingAnalysis(
        val style: String,
        val colors: List<Int>,
        val pattern: String,
        val season: String,
        val occasion: String,
        val mood: String,
        val complexity: Int
    )
    
    data class ClothingItem(
        val id: String,
        val name: String,
        val description: String,
        val imagePath: String,
        val category: String,
        val isSpecial: Boolean = false
    )
}