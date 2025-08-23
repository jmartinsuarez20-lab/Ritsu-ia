package com.ritsu.ai.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.ritsu.ai.data.RitsuDatabase
import com.ritsu.ai.data.model.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.util.*

class ClothingGenerator(private val context: Context) {
    
    private val database = RitsuDatabase.getInstance(context)
    private val preferenceManager = PreferenceManager(context)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Configuración de generación
    private val clothingStoragePath = "ritsu_clothing"
    private val maxClothingItems = 50
    
    // Palabras clave para categorías
    private val categoryKeywords = mapOf(
        ClothingCategory.TOP to listOf("camisa", "blusa", "polo", "camiseta", "jersey", "suéter", "chaqueta", "abrigo"),
        ClothingCategory.BOTTOM to listOf("pantalón", "falda", "shorts", "jeans", "leggings", "pantalones"),
        ClothingCategory.DRESS to listOf("vestido", "traje", "faldón", "túnica"),
        ClothingCategory.UNIFORM to listOf("uniforme", "escolar", "trabajo", "oficina", "formal"),
        ClothingCategory.SWIMWEAR to listOf("bañador", "traje de baño", "bikini", "baño", "playa", "piscina"),
        ClothingCategory.UNDERWEAR to listOf("ropa interior", "lencería", "intimo", "sujetador", "bra"),
        ClothingCategory.ACCESSORY to listOf("accesorio", "complemento", "adorno", "decoración"),
        ClothingCategory.SPECIAL to listOf("especial", "único", "exclusivo", "modo especial", "sin censura")
    )
    
    // Palabras clave para estilos
    private val styleKeywords = mapOf(
        ClothingStyle.CASUAL to listOf("casual", "informal", "cómodo", "relajado", "diario"),
        ClothingStyle.ELEGANT to listOf("elegante", "sofisticado", "refinado", "lujoso", "exclusivo"),
        ClothingStyle.SPORT to listOf("deportivo", "gimnasio", "ejercicio", "atlético", "fitness"),
        ClothingStyle.SCHOOL to listOf("escolar", "colegio", "universidad", "estudiante", "académico"),
        ClothingStyle.FORMAL to listOf("formal", "oficina", "trabajo", "profesional", "serio"),
        ClothingStyle.BEACH to listOf("playa", "verano", "vacaciones", "aire libre", "relajado"),
        ClothingStyle.WINTER to listOf("invierno", "frío", "abrigado", "caliente", "temporada"),
        ClothingStyle.SUMMER to listOf("verano", "calor", "fresco", "ligero", "estival"),
        ClothingStyle.SPECIAL to listOf("especial", "único", "exclusivo", "diferente", "particular")
    )
    
    // Palabras clave para colores
    private val colorKeywords = mapOf(
        ClothingColor.PINK to listOf("rosa", "pink", "rosado", "fucsia", "magenta"),
        ClothingColor.BLUE to listOf("azul", "blue", "celeste", "marino", "turquesa"),
        ClothingColor.GREEN to listOf("verde", "green", "esmeralda", "oliva", "menta"),
        ClothingColor.RED to listOf("rojo", "red", "carmesí", "escarlata", "bordeaux"),
        ClothingColor.YELLOW to listOf("amarillo", "yellow", "dorado", "oro", "limón"),
        ClothingColor.BLACK to listOf("negro", "black", "oscuro", "elegante"),
        ClothingColor.WHITE to listOf("blanco", "white", "claro", "puro", "limpio"),
        ClothingColor.GRAY to listOf("gris", "gray", "plata", "plateado", "neutro"),
        ClothingColor.PURPLE to listOf("morado", "purple", "violeta", "lila", "púrpura"),
        ClothingColor.ORANGE to listOf("naranja", "orange", "mandarina", "zapote"),
        ClothingColor.BROWN to listOf("marrón", "brown", "café", "chocolate", "caramelo"),
        ClothingColor.CYAN to listOf("cian", "cyan", "turquesa", "azul claro"),
        ClothingColor.MAGENTA to listOf("magenta", "fucsia", "rosa intenso"),
        ClothingColor.GOLD to listOf("dorado", "gold", "oro", "brillante"),
        ClothingColor.SILVER to listOf("plateado", "silver", "plata", "metálico")
    )
    
    // Palabras clave para patrones
    private val patternKeywords = mapOf(
        ClothingPattern.PLAIN to listOf("liso", "plain", "sólido", "uniforme", "sin patrón"),
        ClothingPattern.STRIPES to listOf("rayas", "stripes", "rayado", "franjas", "líneas"),
        ClothingPattern.CHECKERED to listOf("cuadros", "checkered", "escocés", "tartán", "ajedrez"),
        ClothingPattern.FLORAL to listOf("flores", "floral", "floreado", "rosas", "pétalos"),
        ClothingPattern.POLKA to listOf("puntos", "polka", "punteado", "lunares", "manchas"),
        ClothingPattern.GEOMETRIC to listOf("geométrico", "geometric", "formas", "figuras", "diseño"),
        ClothingPattern.ANIMAL to listOf("animal", "leopardo", "cebra", "tigre", "serpiente"),
        ClothingPattern.ABSTRACT to listOf("abstracto", "abstract", "artístico", "creativo", "moderno"),
        ClothingPattern.PLAID to listOf("escocés", "plaid", "tartán", "cuadros escoceses"),
        ClothingPattern.DOT to listOf("puntos", "dot", "lunares", "punteado"),
        ClothingPattern.WAVE to listOf("ondas", "wave", "ondulado", "curvas"),
        ClothingPattern.STAR to listOf("estrellas", "star", "brillante", "espacial"),
        ClothingPattern.HEART to listOf("corazones", "heart", "amor", "romántico"),
        ClothingPattern.NONE to listOf("sin patrón", "none", "liso", "sólido")
    )
    
    init {
        initialize()
    }
    
    private fun initialize() {
        scope.launch {
            try {
                // Crear directorio de almacenamiento si no existe
                createStorageDirectory()
                
                // Limpiar ropa antigua si es necesario
                cleanupOldClothing()
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun createStorageDirectory() {
        val storageDir = File(context.getExternalFilesDir(null), clothingStoragePath)
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
    }
    
    private fun cleanupOldClothing() {
        scope.launch {
            try {
                val allClothing = database.clothingItemDao().getAllClothingItems()
                if (allClothing.size > maxClothingItems) {
                    // Eliminar ropa más antigua
                    val sortedClothing = allClothing.sortedBy { it.lastUsed }
                    val itemsToDelete = sortedClothing.take(allClothing.size - maxClothingItems)
                    
                    itemsToDelete.forEach { clothing ->
                        // Eliminar archivo de imagen si existe
                        clothing.imagePath?.let { path ->
                            val imageFile = File(path)
                            if (imageFile.exists()) {
                                imageFile.delete()
                            }
                        }
                        
                        // Eliminar de la base de datos
                        database.clothingItemDao().deleteClothingItem(clothing)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    suspend fun generateClothing(description: String): ClothingItem {
        val normalizedDescription = description.lowercase().trim()
        
        // Analizar descripción para extraer características
        val category = extractCategory(normalizedDescription)
        val style = extractStyle(normalizedDescription)
        val color = extractColor(normalizedDescription)
        val pattern = extractPattern(normalizedDescription)
        val season = extractSeason(normalizedDescription)
        val formality = extractFormality(normalizedDescription)
        
        // Verificar si es modo especial
        val isSpecialMode = normalizedDescription.contains("especial") || 
                           normalizedDescription.contains("sin censura") ||
                           normalizedDescription.contains("desnuda") ||
                           normalizedDescription.contains("nuda")
        
        val isNude = isSpecialMode && (normalizedDescription.contains("desnuda") || 
                                      normalizedDescription.contains("nuda") ||
                                      normalizedDescription.contains("sin ropa"))
        
        // Crear nombre y descripción
        val name = generateClothingName(category, style, color, pattern)
        val fullDescription = generateClothingDescription(category, style, color, pattern, season, formality)
        
        // Crear item de ropa
        val clothingItem = ClothingItem(
            name = name,
            description = fullDescription,
            category = category,
            style = style,
            color = color,
            pattern = pattern,
            season = season,
            formality = formality,
            isGenerated = true,
            generationPrompt = description,
            isSpecialMode = isSpecialMode,
            isNude = isNude
        )
        
        // Generar imagen si no es modo especial o si está habilitado
        if (!isSpecialMode || preferenceManager.isSpecialModeEnabled()) {
            val imagePath = generateClothingImage(clothingItem)
            clothingItem.updateImagePath(imagePath)
        }
        
        // Guardar en base de datos
        database.clothingItemDao().insertClothingItem(clothingItem)
        
        // Agregar a preferencias guardadas
        preferenceManager.addSavedClothing(clothingItem)
        
        return clothingItem
    }
    
    private fun extractCategory(description: String): ClothingCategory {
        for ((category, keywords) in categoryKeywords) {
            if (keywords.any { description.contains(it) }) {
                return category
            }
        }
        return ClothingCategory.TOP // Por defecto
    }
    
    private fun extractStyle(description: String): ClothingStyle {
        for ((style, keywords) in styleKeywords) {
            if (keywords.any { description.contains(it) }) {
                return style
            }
        }
        return ClothingStyle.CASUAL // Por defecto
    }
    
    private fun extractColor(description: String): ClothingColor {
        for ((color, keywords) in colorKeywords) {
            if (keywords.any { description.contains(it) }) {
                return color
            }
        }
        return ClothingColor.BLUE // Por defecto
    }
    
    private fun extractPattern(description: String): ClothingPattern {
        for ((pattern, keywords) in patternKeywords) {
            if (keywords.any { description.contains(it) }) {
                return pattern
            }
        }
        return ClothingPattern.PLAIN // Por defecto
    }
    
    private fun extractSeason(description: String): ClothingSeason {
        return when {
            description.contains("invierno") || description.contains("frío") -> ClothingSeason.WINTER
            description.contains("verano") || description.contains("calor") -> ClothingSeason.SUMMER
            description.contains("primavera") -> ClothingSeason.SPRING
            description.contains("otoño") -> ClothingSeason.AUTUMN
            else -> ClothingSeason.ALL
        }
    }
    
    private fun extractFormality(description: String): ClothingFormality {
        return when {
            description.contains("formal") || description.contains("oficina") -> ClothingFormality.FORMAL
            description.contains("muy formal") || description.contains("elegante") -> ClothingFormality.VERY_FORMAL
            description.contains("semi formal") -> ClothingFormality.SEMI_FORMAL
            description.contains("especial") -> ClothingFormality.SPECIAL
            else -> ClothingFormality.CASUAL
        }
    }
    
    private fun generateClothingName(category: ClothingCategory, style: ClothingStyle, color: ClothingColor, pattern: ClothingPattern): String {
        val parts = mutableListOf<String>()
        
        // Agregar patrón si no es liso
        if (pattern != ClothingPattern.PLAIN) {
            parts.add(pattern.displayName)
        }
        
        // Agregar color
        parts.add(color.displayName)
        
        // Agregar estilo
        parts.add(style.displayName)
        
        // Agregar categoría
        parts.add(category.displayName)
        
        return parts.joinToString(" ")
    }
    
    private fun generateClothingDescription(category: ClothingCategory, style: ClothingStyle, color: ClothingColor, pattern: ClothingPattern, season: ClothingSeason, formality: ClothingFormality): String {
        val parts = mutableListOf<String>()
        
        parts.add("Una ${category.displayName.lowercase()}")
        parts.add("de estilo ${style.displayName.lowercase()}")
        parts.add("en color ${color.displayName.lowercase()}")
        
        if (pattern != ClothingPattern.PLAIN) {
            parts.add("con patrón ${pattern.displayName.lowercase()}")
        }
        
        parts.add("perfecta para ${season.displayName.lowercase()}")
        parts.add("y ocasiones ${formality.displayName.lowercase()}s")
        
        return parts.joinToString(", ")
    }
    
    private fun generateClothingImage(clothingItem: ClothingItem): String {
        try {
            val imageSize = 512
            val bitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            // Fondo transparente
            canvas.drawColor(Color.TRANSPARENT)
            
            // Dibujar ropa según categoría
            drawClothingOnCanvas(canvas, clothingItem, imageSize)
            
            // Guardar imagen
            val fileName = "clothing_${clothingItem.id}_${System.currentTimeMillis()}.png"
            val imageFile = File(context.getExternalFilesDir(clothingStoragePath), fileName)
            
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
            
            bitmap.recycle()
            
            return imageFile.absolutePath
            
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }
    
    private fun drawClothingOnCanvas(canvas: Canvas, clothingItem: ClothingItem, size: Int) {
        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        
        val centerX = size / 2f
        val centerY = size / 2f
        val clothingRadius = size * 0.4f
        
        // Color de la ropa
        paint.color = getClothingColor(clothingItem.color)
        
        when (clothingItem.category) {
            ClothingCategory.TOP -> {
                // Dibujar camisa/blusa
                canvas.drawCircle(centerX, centerY, clothingRadius, paint)
                
                // Agregar detalles según el patrón
                drawPattern(canvas, clothingItem.pattern, centerX, centerY, clothingRadius, paint)
            }
            
            ClothingCategory.DRESS -> {
                // Dibujar vestido (más largo)
                canvas.drawCircle(centerX, centerY + clothingRadius * 0.2f, clothingRadius * 1.2f, paint)
                drawPattern(canvas, clothingItem.pattern, centerX, centerY, clothingRadius, paint)
            }
            
            ClothingCategory.UNIFORM -> {
                // Dibujar uniforme escolar
                paint.color = Color.rgb(100, 150, 255)
                canvas.drawCircle(centerX, centerY, clothingRadius, paint)
                
                // Corbata
                paint.color = Color.rgb(255, 100, 100)
                canvas.drawRect(
                    centerX - clothingRadius * 0.1f,
                    centerY - clothingRadius * 0.2f,
                    centerX + clothingRadius * 0.1f,
                    centerY + clothingRadius * 0.3f,
                    paint
                )
            }
            
            ClothingCategory.SWIMWEAR -> {
                // Traje de baño
                paint.color = Color.rgb(255, 100, 150)
                canvas.drawCircle(centerX, centerY, clothingRadius * 0.8f, paint)
            }
            
            ClothingCategory.SPECIAL -> {
                if (clothingItem.isNude) {
                    // No dibujar ropa para modo especial
                    return
                } else {
                    paint.color = Color.rgb(255, 0, 0)
                    canvas.drawCircle(centerX, centerY, clothingRadius, paint)
                }
            }
            
            else -> {
                // Ropa por defecto
                canvas.drawCircle(centerX, centerY, clothingRadius, paint)
                drawPattern(canvas, clothingItem.pattern, centerX, centerY, clothingRadius, paint)
            }
        }
    }
    
    private fun drawPattern(canvas: Canvas, pattern: ClothingPattern, centerX: Float, centerY: Float, radius: Float, paint: Paint) {
        when (pattern) {
            ClothingPattern.STRIPES -> {
                paint.color = Color.rgb(200, 200, 200)
                for (i in 0..10) {
                    val x = centerX - radius + (i * radius * 0.2f)
                    canvas.drawRect(x, centerY - radius, x + radius * 0.1f, centerY + radius, paint)
                }
            }
            
            ClothingPattern.CHECKERED -> {
                paint.color = Color.rgb(200, 200, 200)
                val squareSize = radius * 0.2f
                for (i in -2..2) {
                    for (j in -2..2) {
                        if ((i + j) % 2 == 0) {
                            val x = centerX + i * squareSize
                            val y = centerY + j * squareSize
                            canvas.drawRect(x, y, x + squareSize, y + squareSize, paint)
                        }
                    }
                }
            }
            
            ClothingPattern.POLKA -> {
                paint.color = Color.rgb(255, 255, 255)
                for (i in 0..8) {
                    val angle = i * 45f * Math.PI / 180f
                    val x = centerX + (radius * 0.6f * Math.cos(angle)).toFloat()
                    val y = centerY + (radius * 0.6f * Math.sin(angle)).toFloat()
                    canvas.drawCircle(x, y, radius * 0.1f, paint)
                }
            }
            
            else -> {
                // Patrón liso, no agregar nada
            }
        }
    }
    
    private fun getClothingColor(color: ClothingColor): Int {
        return when (color) {
            ClothingColor.PINK -> Color.rgb(255, 192, 203)
            ClothingColor.BLUE -> Color.rgb(100, 150, 255)
            ClothingColor.GREEN -> Color.rgb(100, 255, 100)
            ClothingColor.RED -> Color.rgb(255, 100, 100)
            ClothingColor.YELLOW -> Color.rgb(255, 255, 100)
            ClothingColor.BLACK -> Color.rgb(50, 50, 50)
            ClothingColor.WHITE -> Color.rgb(255, 255, 255)
            ClothingColor.GRAY -> Color.rgb(150, 150, 150)
            ClothingColor.PURPLE -> Color.rgb(200, 100, 255)
            ClothingColor.ORANGE -> Color.rgb(255, 165, 0)
            ClothingColor.BROWN -> Color.rgb(165, 42, 42)
            ClothingColor.CYAN -> Color.rgb(0, 255, 255)
            ClothingColor.MAGENTA -> Color.rgb(255, 0, 255)
            ClothingColor.GOLD -> Color.rgb(255, 215, 0)
            ClothingColor.SILVER -> Color.rgb(192, 192, 192)
        }
    }
    
    fun cleanup() {
        scope.cancel()
    }
}