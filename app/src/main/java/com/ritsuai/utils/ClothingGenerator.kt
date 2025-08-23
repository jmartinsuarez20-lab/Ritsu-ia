package com.ritsuai.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.core.content.ContextCompat
import com.ritsuai.R
import com.ritsuai.database.dao.ClothingItemDao
import com.ritsuai.database.dao.AvatarOutfitDao
import com.ritsuai.database.entities.ClothingItemEntity
import com.ritsuai.database.entities.AvatarOutfitEntity
import com.ritsuai.services.AIService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID

class ClothingGenerator(private val context: Context) {
    
    companion object {
        const val CLOTHING_SIZE = 200
        const val CATEGORY_CASUAL = "casual"
        const val CATEGORY_FORMAL = "formal"
        const val CATEGORY_SPORT = "sport"
        const val CATEGORY_SPECIAL = "special"
        const val CATEGORY_UNCENSORED = "uncensored"
    }
    
    private val scope = CoroutineScope(Dispatchers.Main)
    private val aiService = AIService.getInstance()
    
    // Colores para la ropa
    private val clothingColors = listOf(
        ContextCompat.getColor(context, R.color.clothing_blue),
        ContextCompat.getColor(context, R.color.clothing_red),
        ContextCompat.getColor(context, R.color.clothing_green),
        ContextCompat.getColor(context, R.color.clothing_purple),
        ContextCompat.getColor(context, R.color.clothing_pink),
        ContextCompat.getColor(context, R.color.clothing_yellow),
        ContextCompat.getColor(context, R.color.clothing_black),
        ContextCompat.getColor(context, R.color.clothing_white)
    )
    
    suspend fun generateClothingFromPrompt(
        prompt: String,
        category: String = CATEGORY_CASUAL,
        isUncensored: Boolean = false
    ): ClothingItemEntity = withContext(Dispatchers.Default) {
        
        // Analizar el prompt con IA para entender qué ropa generar
        val aiAnalysis = analyzeClothingPrompt(prompt, category, isUncensored)
        
        // Generar la ropa basada en el análisis
        val clothingBitmap = generateClothingBitmap(aiAnalysis)
        
        // Crear entidad de ropa
        val clothingItem = ClothingItemEntity(
            id = UUID.randomUUID().toString(),
            name = aiAnalysis.name,
            description = aiAnalysis.description,
            category = category,
            subcategory = aiAnalysis.subcategory,
            color = aiAnalysis.primaryColor,
            style = aiAnalysis.style,
            isUncensored = isUncensored,
            accessibilityLevel = if (isUncensored) 2 else 0,
            generatedByAI = true,
            userPrompt = prompt,
            createdAt = Date(),
            imageData = bitmapToByteArray(clothingBitmap)
        )
        
        // Guardar en base de datos
        saveClothingItem(clothingItem)
        
        clothingItem
    }
    
    suspend fun generateOutfitFromPrompt(
        prompt: String,
        category: String = CATEGORY_CASUAL,
        isUncensored: Boolean = false
    ): AvatarOutfitEntity = withContext(Dispatchers.Default) {
        
        // Analizar el prompt para generar un outfit completo
        val outfitAnalysis = analyzeOutfitPrompt(prompt, category, isUncensored)
        
        // Generar múltiples prendas de ropa
        val clothingItems = mutableListOf<ClothingItemEntity>()
        
        outfitAnalysis.clothingTypes.forEach { clothingType ->
            val clothingPrompt = "${outfitAnalysis.style} $clothingType ${outfitAnalysis.colorScheme}"
            val clothingItem = generateClothingFromPrompt(
                clothingPrompt,
                category,
                isUncensored
            )
            clothingItems.add(clothingItem)
        }
        
        // Crear el outfit
        val outfit = AvatarOutfitEntity(
            name = outfitAnalysis.name,
            description = outfitAnalysis.description,
            clothingItems = clothingItems.map { it.id },
            category = category,
            isFavorite = false,
            createdAt = Date(),
            generatedByAI = true,
            userPrompt = prompt,
            isUncensored = isUncensored,
            accessibilityLevel = if (isUncensored) 2 else 0
        )
        
        // Guardar outfit en base de datos
        saveOutfit(outfit)
        
        outfit
    }
    
    private suspend fun analyzeClothingPrompt(
        prompt: String,
        category: String,
        isUncensored: Boolean
    ): ClothingAnalysis {
        
        val aiPrompt = """
            Analiza este prompt de ropa para una chica anime kawaii:
            Prompt: $prompt
            Categoría: $category
            Modo sin censura: $isUncensored
            
            Responde en formato JSON con:
            {
                "name": "Nombre descriptivo de la prenda",
                "description": "Descripción detallada",
                "subcategory": "tipo de prenda (camisa, falda, etc.)",
                "style": "estilo (cute, elegant, sporty, etc.)",
                "primaryColor": "color principal",
                "secondaryColor": "color secundario opcional",
                "pattern": "patrón si aplica",
                "accessories": ["lista de accesorios"]
            }
        """.trimIndent()
        
        val response = aiService.generateResponse(aiPrompt)
        
        // Parsear respuesta JSON (implementar parser robusto)
        return parseClothingAnalysis(response)
    }
    
    private suspend fun analyzeOutfitPrompt(
        prompt: String,
        category: String,
        isUncensored: Boolean
    ): OutfitAnalysis {
        
        val aiPrompt = """
            Analiza este prompt para generar un outfit completo de anime kawaii:
            Prompt: $prompt
            Categoría: $category
            Modo sin censura: $isUncensored
            
            Responde en formato JSON con:
            {
                "name": "Nombre del outfit",
                "description": "Descripción del outfit completo",
                "style": "estilo general del outfit",
                "colorScheme": "esquema de colores",
                "clothingTypes": ["camisa", "falda", "calcetines", "zapatos"],
                "season": "temporada apropiada",
                "occasion": "ocasión para usar"
            }
        """.trimIndent()
        
        val response = aiService.generateResponse(aiPrompt)
        
        // Parsear respuesta JSON
        return parseOutfitAnalysis(response)
    }
    
    private fun generateClothingBitmap(analysis: ClothingAnalysis): Bitmap {
        val bitmap = Bitmap.createBitmap(CLOTHING_SIZE, CLOTHING_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        
        // Fondo transparente
        canvas.drawColor(android.graphics.Color.TRANSPARENT)
        
        // Generar ropa según el tipo
        when (analysis.subcategory.lowercase()) {
            "camisa", "blusa" -> drawShirt(canvas, paint, analysis)
            "falda" -> drawSkirt(canvas, paint, analysis)
            "vestido" -> drawDress(canvas, paint, analysis)
            "pantalón", "pantalones" -> drawPants(canvas, paint, analysis)
            "calcetines" -> drawSocks(canvas, paint, analysis)
            "zapatos" -> drawShoes(canvas, paint, analysis)
            "accesorio" -> drawAccessory(canvas, paint, analysis)
            else -> drawGenericClothing(canvas, paint, analysis)
        }
        
        return bitmap
    }
    
    private fun drawShirt(canvas: Canvas, paint: Paint, analysis: ClothingAnalysis) {
        paint.color = analysis.primaryColor
        
        // Cuerpo de la camisa
        val shirtRect = RectF(
            CLOTHING_SIZE * 0.3f,
            CLOTHING_SIZE * 0.4f,
            CLOTHING_SIZE * 0.7f,
            CLOTHING_SIZE * 0.6f
        )
        canvas.drawRoundRect(shirtRect, CLOTHING_SIZE * 0.05f, CLOTHING_SIZE * 0.05f, paint)
        
        // Manga izquierda
        val leftSleeve = RectF(
            CLOTHING_SIZE * 0.2f,
            CLOTHING_SIZE * 0.42f,
            CLOTHING_SIZE * 0.3f,
            CLOTHING_SIZE * 0.55f
        )
        canvas.drawRoundRect(leftSleeve, CLOTHING_SIZE * 0.03f, CLOTHING_SIZE * 0.03f, paint)
        
        // Manga derecha
        val rightSleeve = RectF(
            CLOTHING_SIZE * 0.7f,
            CLOTHING_SIZE * 0.42f,
            CLOTHING_SIZE * 0.8f,
            CLOTHING_SIZE * 0.55f
        )
        canvas.drawRoundRect(rightSleeve, CLOTHING_SIZE * 0.03f, CLOTHING_SIZE * 0.03f, paint)
        
        // Cuello
        val collar = RectF(
            CLOTHING_SIZE * 0.4f,
            CLOTHING_SIZE * 0.38f,
            CLOTHING_SIZE * 0.6f,
            CLOTHING_SIZE * 0.42f
        )
        canvas.drawRoundRect(collar, CLOTHING_SIZE * 0.02f, CLOTHING_SIZE * 0.02f, paint)
    }
    
    private fun drawSkirt(canvas: Canvas, paint: Paint, analysis: ClothingAnalysis) {
        paint.color = analysis.primaryColor
        
        // Falda principal
        val skirtPath = Path()
        skirtPath.moveTo(CLOTHING_SIZE * 0.25f, CLOTHING_SIZE * 0.6f)
        skirtPath.lineTo(CLOTHING_SIZE * 0.75f, CLOTHING_SIZE * 0.6f)
        skirtPath.lineTo(CLOTHING_SIZE * 0.8f, CLOTHING_SIZE * 0.85f)
        skirtPath.lineTo(CLOTHING_SIZE * 0.2f, CLOTHING_SIZE * 0.85f)
        skirtPath.close()
        canvas.drawPath(skirtPath, paint)
        
        // Pliegues de la falda
        paint.color = analysis.secondaryColor ?: analysis.primaryColor
        for (i in 1..3) {
            val foldX = CLOTHING_SIZE * (0.3f + i * 0.1f)
            val foldPath = Path()
            foldPath.moveTo(foldX, CLOTHING_SIZE * 0.6f)
            foldPath.lineTo(foldX + CLOTHING_SIZE * 0.02f, CLOTHING_SIZE * 0.85f)
            foldPath.lineTo(foldX - CLOTHING_SIZE * 0.02f, CLOTHING_SIZE * 0.85f)
            foldPath.close()
            canvas.drawPath(foldPath, paint)
        }
    }
    
    private fun drawDress(canvas: Canvas, paint: Paint, analysis: ClothingAnalysis) {
        paint.color = analysis.primaryColor
        
        // Vestido completo
        val dressPath = Path()
        dressPath.moveTo(CLOTHING_SIZE * 0.3f, CLOTHING_SIZE * 0.35f)
        dressPath.lineTo(CLOTHING_SIZE * 0.7f, CLOTHING_SIZE * 0.35f)
        dressPath.lineTo(CLOTHING_SIZE * 0.75f, CLOTHING_SIZE * 0.85f)
        dressPath.lineTo(CLOTHING_SIZE * 0.25f, CLOTHING_SIZE * 0.85f)
        dressPath.close()
        canvas.drawPath(dressPath, paint)
        
        // Cinturón
        paint.color = analysis.secondaryColor ?: analysis.primaryColor
        val beltRect = RectF(
            CLOTHING_SIZE * 0.35f,
            CLOTHING_SIZE * 0.55f,
            CLOTHING_SIZE * 0.65f,
            CLOTHING_SIZE * 0.58f
        )
        canvas.drawRoundRect(beltRect, CLOTHING_SIZE * 0.02f, CLOTHING_SIZE * 0.02f, paint)
    }
    
    private fun drawPants(canvas: Canvas, paint: Paint, analysis: ClothingAnalysis) {
        paint.color = analysis.primaryColor
        
        // Pierna izquierda
        val leftLeg = RectF(
            CLOTHING_SIZE * 0.35f,
            CLOTHING_SIZE * 0.6f,
            CLOTHING_SIZE * 0.45f,
            CLOTHING_SIZE * 0.95f
        )
        canvas.drawRoundRect(leftLeg, CLOTHING_SIZE * 0.03f, CLOTHING_SIZE * 0.03f, paint)
        
        // Pierna derecha
        val rightLeg = RectF(
            CLOTHING_SIZE * 0.55f,
            CLOTHING_SIZE * 0.6f,
            CLOTHING_SIZE * 0.65f,
            CLOTHING_SIZE * 0.95f
        )
        canvas.drawRoundRect(rightLeg, CLOTHING_SIZE * 0.03f, CLOTHING_SIZE * 0.03f, paint)
        
        // Cintura
        val waistRect = RectF(
            CLOTHING_SIZE * 0.3f,
            CLOTHING_SIZE * 0.58f,
            CLOTHING_SIZE * 0.7f,
            CLOTHING_SIZE * 0.62f
        )
        canvas.drawRoundRect(waistRect, CLOTHING_SIZE * 0.02f, CLOTHING_SIZE * 0.02f, paint)
    }
    
    private fun drawSocks(canvas: Canvas, paint: Paint, analysis: ClothingAnalysis) {
        paint.color = analysis.primaryColor
        
        // Calcetín izquierdo
        val leftSock = RectF(
            CLOTHING_SIZE * 0.35f,
            CLOTHING_SIZE * 0.95f,
            CLOTHING_SIZE * 0.45f,
            CLOTHING_SIZE * 0.98f
        )
        canvas.drawRoundRect(leftSock, CLOTHING_SIZE * 0.02f, CLOTHING_SIZE * 0.02f, paint)
        
        // Calcetín derecho
        val rightSock = RectF(
            CLOTHING_SIZE * 0.55f,
            CLOTHING_SIZE * 0.95f,
            CLOTHING_SIZE * 0.65f,
            CLOTHING_SIZE * 0.98f
        )
        canvas.drawRoundRect(rightSock, CLOTHING_SIZE * 0.02f, CLOTHING_SIZE * 0.02f, paint)
    }
    
    private fun drawShoes(canvas: Canvas, paint: Paint, analysis: ClothingAnalysis) {
        paint.color = analysis.primaryColor
        
        // Zapato izquierdo
        val leftShoe = RectF(
            CLOTHING_SIZE * 0.32f,
            CLOTHING_SIZE * 0.96f,
            CLOTHING_SIZE * 0.48f,
            CLOTHING_SIZE * 0.99f
        )
        canvas.drawRoundRect(leftShoe, CLOTHING_SIZE * 0.03f, CLOTHING_SIZE * 0.03f, paint)
        
        // Zapato derecho
        val rightShoe = RectF(
            CLOTHING_SIZE * 0.52f,
            CLOTHING_SIZE * 0.96f,
            CLOTHING_SIZE * 0.68f,
            CLOTHING_SIZE * 0.99f
        )
        canvas.drawRoundRect(rightShoe, CLOTHING_SIZE * 0.03f, CLOTHING_SIZE * 0.03f, paint)
    }
    
    private fun drawAccessory(canvas: Canvas, paint: Paint, analysis: ClothingAnalysis) {
        paint.color = analysis.primaryColor
        
        // Accesorio genérico (círculo)
        canvas.drawCircle(
            CLOTHING_SIZE * 0.5f,
            CLOTHING_SIZE * 0.5f,
            CLOTHING_SIZE * 0.1f,
            paint
        )
    }
    
    private fun drawGenericClothing(canvas: Canvas, paint: Paint, analysis: ClothingAnalysis) {
        paint.color = analysis.primaryColor
        
        // Ropa genérica (rectángulo)
        val clothingRect = RectF(
            CLOTHING_SIZE * 0.2f,
            CLOTHING_SIZE * 0.3f,
            CLOTHING_SIZE * 0.8f,
            CLOTHING_SIZE * 0.7f
        )
        canvas.drawRoundRect(clothingRect, CLOTHING_SIZE * 0.05f, CLOTHING_SIZE * 0.05f, paint)
    }
    
    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        // Convertir bitmap a byte array para almacenar en base de datos
        // Implementar conversión
        return ByteArray(0)
    }
    
    private suspend fun saveClothingItem(clothingItem: ClothingItemEntity) {
        // Guardar en base de datos
        // Implementar
    }
    
    private suspend fun saveOutfit(outfit: AvatarOutfitEntity) {
        // Guardar outfit en base de datos
        // Implementar
    }
    
    private fun parseClothingAnalysis(response: String): ClothingAnalysis {
        // Parsear respuesta JSON de la IA
        // Implementar parser robusto
        return ClothingAnalysis(
            name = "Ropa Generada",
            description = "Ropa generada por IA",
            subcategory = "genérico",
            style = "cute",
            primaryColor = clothingColors.random(),
            secondaryColor = null,
            pattern = null,
            accessories = emptyList()
        )
    }
    
    private fun parseOutfitAnalysis(response: String): OutfitAnalysis {
        // Parsear respuesta JSON de la IA
        return OutfitAnalysis(
            name = "Outfit Generado",
            description = "Outfit completo generado por IA",
            style = "cute",
            colorScheme = "multicolor",
            clothingTypes = listOf("camisa", "falda", "calcetines", "zapatos"),
            season = "all",
            occasion = "casual"
        )
    }
    
    data class ClothingAnalysis(
        val name: String,
        val description: String,
        val subcategory: String,
        val style: String,
        val primaryColor: Int,
        val secondaryColor: Int?,
        val pattern: String?,
        val accessories: List<String>
    )
    
    data class OutfitAnalysis(
        val name: String,
        val description: String,
        val style: String,
        val colorScheme: String,
        val clothingTypes: List<String>,
        val season: String,
        val occasion: String
    )
}