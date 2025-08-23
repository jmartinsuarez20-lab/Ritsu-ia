package com.ritsuai.assistant.core.avatar

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.ritsuai.assistant.R
import kotlinx.coroutines.*

/**
 * Renderizador del Avatar Kawaii de Ritsu
 * Maneja la generación y renderizado del avatar de cuerpo completo con expresiones dinámicas
 */
class KawaiiAvatarRenderer(private val context: Context) {
    
    private val renderScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // Capas del avatar (de atrás hacia adelante)
    private var baseBodyLayer: Bitmap? = null
    private var underwearLayer: Bitmap? = null
    private var clothingLayer: Bitmap? = null
    private var accessoriesLayer: Bitmap? = null
    private var faceLayer: Bitmap? = null
    private var expressionLayer: Bitmap? = null
    private var hairLayer: Bitmap? = null
    private var effectsLayer: Bitmap? = null
    
    // Estados del avatar
    private var currentOutfit = "school_uniform"
    private var currentExpression = "neutral"
    private var isUncensoredMode = false
    
    // Configuraciones kawaii
    private val kawaiiColors = mapOf(
        "skin" to Color.parseColor("#FFDBCC"),
        "blush" to Color.parseColor("#FF9999"),
        "eyes" to Color.parseColor("#6B4E3D"),
        "hair" to Color.parseColor("#8B4513")
    )
    
    init {
        initializeBaseLayers()
    }
    
    private fun initializeBaseLayers() {
        renderScope.launch {
            // Cargar capas base del avatar
            loadBaseLayers()
        }
    }
    
    private suspend fun loadBaseLayers() = withContext(Dispatchers.IO) {
        try {
            // Cargar sprite base del cuerpo kawaii
            baseBodyLayer = loadBitmapFromResource(R.drawable.ritsu_base_body_kawaii)
            
            // Cargar ropa interior por defecto (kawaii)
            underwearLayer = loadBitmapFromResource(R.drawable.ritsu_underwear_kawaii)
            
            // Cargar uniforme escolar por defecto
            clothingLayer = loadBitmapFromResource(R.drawable.ritsu_school_uniform)
            
            // Cargar cara base
            faceLayer = loadBitmapFromResource(R.drawable.ritsu_face_base)
            
            // Cargar cabello
            hairLayer = loadBitmapFromResource(R.drawable.ritsu_hair_kawaii)
            
        } catch (e: Exception) {
            // En caso de error, generar sprites por código
            generateDefaultSprites()
        }
    }
    
    private fun loadBitmapFromResource(resourceId: Int): Bitmap? {
        return try {
            val drawable = ContextCompat.getDrawable(context, resourceId)
            drawable?.let { drawableToBitmap(it) }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
    
    /**
     * Genera el avatar completo combinando todas las capas
     */
    suspend fun renderCompleteAvatar(): Bitmap = withContext(Dispatchers.Default) {
        val avatarWidth = 300
        val avatarHeight = 600
        
        val finalBitmap = Bitmap.createBitmap(avatarWidth, avatarHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(finalBitmap)
        
        // Dibujar capas en orden (de atrás hacia adelante)
        baseBodyLayer?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        
        if (!isUncensoredMode) {
            underwearLayer?.let { canvas.drawBitmap(it, 0f, 0f, null) }
            clothingLayer?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        }
        
        accessoriesLayer?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        faceLayer?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        expressionLayer?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        hairLayer?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        effectsLayer?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        
        // Agregar efectos kawaii
        addKawaiiEffects(canvas)
        
        finalBitmap
    }
    
    /**
     * Cambia la expresión facial del avatar
     */
    suspend fun changeExpression(expression: String, intensity: Float = 1.0f) {
        currentExpression = expression
        expressionLayer = generateExpressionLayer(expression, intensity)
    }
    
    private suspend fun generateExpressionLayer(expression: String, intensity: Float): Bitmap = 
        withContext(Dispatchers.Default) {
            val bitmap = Bitmap.createBitmap(300, 200, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            
            when (expression) {
                "happy" -> drawHappyExpression(canvas, paint, intensity)
                "blushing" -> drawBlushingExpression(canvas, paint, intensity)
                "surprised" -> drawSurprisedExpression(canvas, paint, intensity)
                "winking" -> drawWinkingExpression(canvas, paint, intensity)
                "shy" -> drawShyExpression(canvas, paint, intensity)
                "excited" -> drawExcitedExpression(canvas, paint, intensity)
                "thoughtful" -> drawThoughtfulExpression(canvas, paint, intensity)
                "sleepy" -> drawSleepyExpression(canvas, paint, intensity)
                "angry" -> drawAngryExpression(canvas, paint, intensity)
                "sad" -> drawSadExpression(canvas, paint, intensity)
                "looking_left" -> drawLookingDirection(canvas, paint, "left")
                "looking_right" -> drawLookingDirection(canvas, paint, "right")
                "looking_up" -> drawLookingDirection(canvas, paint, "up")
                "blinking" -> drawBlinkingExpression(canvas, paint)
                else -> drawNeutralExpression(canvas, paint)
            }
            
            bitmap
        }
    
    private fun drawHappyExpression(canvas: Canvas, paint: Paint, intensity: Float) {
        // Ojos felices (curva hacia arriba)
        paint.color = kawaiiColors["eyes"]!!
        paint.strokeWidth = 4f
        paint.style = Paint.Style.STROKE
        
        // Ojo izquierdo (sonriendo)
        val leftEyePath = Path().apply {
            moveTo(80f, 120f)
            quadTo(110f, 100f + (20f * intensity), 140f, 120f)
        }
        canvas.drawPath(leftEyePath, paint)
        
        // Ojo derecho (sonriendo)
        val rightEyePath = Path().apply {
            moveTo(160f, 120f)
            quadTo(190f, 100f + (20f * intensity), 220f, 120f)
        }
        canvas.drawPath(rightEyePath, paint)
        
        // Boca sonriente
        paint.color = Color.parseColor("#FF6B6B")
        val mouthPath = Path().apply {
            moveTo(120f, 160f)
            quadTo(150f, 180f + (10f * intensity), 180f, 160f)
        }
        canvas.drawPath(mouthPath, paint)
        
        // Sonrojo kawaii
        paint.color = kawaiiColors["blush"]!!
        paint.alpha = (100 * intensity).toInt()
        paint.style = Paint.Style.FILL
        canvas.drawCircle(60f, 140f, 15f, paint)
        canvas.drawCircle(240f, 140f, 15f, paint)
    }
    
    private fun drawBlushingExpression(canvas: Canvas, paint: Paint, intensity: Float) {
        // Ojos normales pero con brillo
        drawNeutralEyes(canvas, paint)
        
        // Sonrojo intenso
        paint.color = kawaiiColors["blush"]!!
        paint.alpha = (150 * intensity).toInt()
        paint.style = Paint.Style.FILL
        canvas.drawCircle(60f, 140f, 20f, paint)
        canvas.drawCircle(240f, 140f, 20f, paint)
        
        // Boca pequeña y tímida
        paint.color = Color.parseColor("#FF8888")
        paint.alpha = 255
        canvas.drawCircle(150f, 165f, 8f, paint)
    }
    
    private fun drawSurprisedExpression(canvas: Canvas, paint: Paint, intensity: Float) {
        // Ojos muy abiertos
        paint.color = kawaiiColors["eyes"]!!
        paint.style = Paint.Style.FILL
        canvas.drawCircle(110f, 120f, 20f + (10f * intensity), paint)
        canvas.drawCircle(190f, 120f, 20f + (10f * intensity), paint)
        
        // Pupilas grandes
        paint.color = Color.BLACK
        canvas.drawCircle(110f, 120f, 15f, paint)
        canvas.drawCircle(190f, 120f, 15f, paint)
        
        // Brillos en los ojos
        paint.color = Color.WHITE
        canvas.drawCircle(115f, 115f, 5f, paint)
        canvas.drawCircle(195f, 115f, 5f, paint)
        
        // Boca abierta (O)
        paint.color = Color.parseColor("#FF6B6B")
        canvas.drawCircle(150f, 165f, 12f + (5f * intensity), paint)
    }
    
    private fun drawWinkingExpression(canvas: Canvas, paint: Paint, intensity: Float) {
        // Ojo izquierdo guiñando
        paint.color = kawaiiColors["eyes"]!!
        paint.strokeWidth = 4f
        paint.style = Paint.Style.STROKE
        
        val winkPath = Path().apply {
            moveTo(80f, 120f)
            quadTo(110f, 100f, 140f, 120f)
        }
        canvas.drawPath(winkPath, paint)
        
        // Ojo derecho normal
        paint.style = Paint.Style.FILL
        canvas.drawCircle(190f, 120f, 15f, paint)
        paint.color = Color.BLACK
        canvas.drawCircle(190f, 120f, 10f, paint)
        
        // Boca sonriente
        paint.color = Color.parseColor("#FF6B6B")
        paint.style = Paint.Style.STROKE
        val mouthPath = Path().apply {
            moveTo(130f, 160f)
            quadTo(150f, 175f, 170f, 160f)
        }
        canvas.drawPath(mouthPath, paint)
    }
    
    private fun drawShyExpression(canvas: Canvas, paint: Paint, intensity: Float) {
        // Ojos mirando hacia abajo
        paint.color = kawaiiColors["eyes"]!!
        paint.style = Paint.Style.FILL
        canvas.drawCircle(110f, 125f, 12f, paint)
        canvas.drawCircle(190f, 125f, 12f, paint)
        
        // Pupilas hacia abajo
        paint.color = Color.BLACK
        canvas.drawCircle(110f, 130f, 8f, paint)
        canvas.drawCircle(190f, 130f, 8f, paint)
        
        // Sonrojo suave
        paint.color = kawaiiColors["blush"]!!
        paint.alpha = (80 * intensity).toInt()
        canvas.drawCircle(70f, 140f, 18f, paint)
        canvas.drawCircle(230f, 140f, 18f, paint)
        
        // Boca pequeña
        paint.color = Color.parseColor("#FF9999")
        paint.alpha = 255
        canvas.drawCircle(150f, 170f, 6f, paint)
    }
    
    private fun drawNeutralEyes(canvas: Canvas, paint: Paint) {
        // Ojos normales
        paint.color = kawaiiColors["eyes"]!!
        paint.style = Paint.Style.FILL
        canvas.drawCircle(110f, 120f, 15f, paint)
        canvas.drawCircle(190f, 120f, 15f, paint)
        
        // Pupilas
        paint.color = Color.BLACK
        canvas.drawCircle(110f, 120f, 10f, paint)
        canvas.drawCircle(190f, 120f, 10f, paint)
        
        // Brillos
        paint.color = Color.WHITE
        canvas.drawCircle(113f, 117f, 3f, paint)
        canvas.drawCircle(193f, 117f, 3f, paint)
    }
    
    private fun drawNeutralExpression(canvas: Canvas, paint: Paint) {
        drawNeutralEyes(canvas, paint)
        
        // Boca neutral
        paint.color = Color.parseColor("#FFAAAA")
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(140f, 165f, 160f, 175f, 5f, 5f, paint)
    }
    
    // Implementar más expresiones...
    private fun drawExcitedExpression(canvas: Canvas, paint: Paint, intensity: Float) {
        // Ojos brillantes con estrellas
        drawNeutralEyes(canvas, paint)
        
        // Estrellas en los ojos
        paint.color = Color.YELLOW
        drawStar(canvas, paint, 110f, 120f, 8f)
        drawStar(canvas, paint, 190f, 120f, 8f)
        
        // Boca muy sonriente
        paint.color = Color.parseColor("#FF4444")
        paint.strokeWidth = 5f
        paint.style = Paint.Style.STROKE
        val excitedMouthPath = Path().apply {
            moveTo(110f, 155f)
            quadTo(150f, 185f + (15f * intensity), 190f, 155f)
        }
        canvas.drawPath(excitedMouthPath, paint)
    }
    
    private fun drawThoughtfulExpression(canvas: Canvas, paint: Paint, intensity: Float) {
        // Ojos mirando hacia arriba y a la derecha
        paint.color = kawaiiColors["eyes"]!!
        paint.style = Paint.Style.FILL
        canvas.drawCircle(110f, 120f, 15f, paint)
        canvas.drawCircle(190f, 120f, 15f, paint)
        
        // Pupilas hacia arriba-derecha
        paint.color = Color.BLACK
        canvas.drawCircle(115f, 115f, 10f, paint)
        canvas.drawCircle(195f, 115f, 10f, paint)
        
        // Boca pensativa (línea pequeña)
        paint.color = Color.parseColor("#FFAAAA")
        paint.strokeWidth = 3f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(145f, 168f, 155f, 168f, paint)
    }
    
    private fun drawSleepyExpression(canvas: Canvas, paint: Paint, intensity: Float) {
        // Ojos semi-cerrados
        paint.color = kawaiiColors["eyes"]!!
        paint.strokeWidth = 6f
        paint.style = Paint.Style.STROKE
        
        canvas.drawLine(95f, 120f, 125f, 120f, paint)
        canvas.drawLine(175f, 120f, 205f, 120f, paint)
        
        // Burbuja de sueño
        paint.color = Color.parseColor("#E6F3FF")
        paint.style = Paint.Style.FILL
        canvas.drawCircle(220f, 80f, 15f, paint)
        paint.color = Color.parseColor("#CCE6FF")
        canvas.drawCircle(235f, 60f, 10f, paint)
        canvas.drawCircle(245f, 45f, 6f, paint)
    }
    
    private fun drawAngryExpression(canvas: Canvas, paint: Paint, intensity: Float) {
        // Cejas fruncidas
        paint.color = kawaiiColors["hair"]!!
        paint.strokeWidth = 8f
        paint.style = Paint.Style.STROKE
        
        // Ceja izquierda
        canvas.drawLine(85f, 100f, 125f, 110f, paint)
        // Ceja derecha
        canvas.drawLine(175f, 110f, 215f, 100f, paint)
        
        // Ojos entrecerrados
        drawNeutralEyes(canvas, paint)
        
        // Boca molesta
        paint.color = Color.parseColor("#FF6666")
        val angryMouthPath = Path().apply {
            moveTo(130f, 170f)
            quadTo(150f, 160f, 170f, 170f)
        }
        canvas.drawPath(angryMouthPath, paint)
        
        // Símbolo de enojo (cruz)
        paint.color = Color.RED
        paint.strokeWidth = 4f
        canvas.drawLine(35f, 90f, 50f, 105f, paint)
        canvas.drawLine(50f, 90f, 35f, 105f, paint)
    }
    
    private fun drawSadExpression(canvas: Canvas, paint: Paint, intensity: Float) {
        // Ojos tristes
        paint.color = kawaiiColors["eyes"]!!
        paint.style = Paint.Style.FILL
        canvas.drawCircle(110f, 125f, 15f, paint)
        canvas.drawCircle(190f, 125f, 15f, paint)
        
        // Pupilas grandes
        paint.color = Color.BLACK
        canvas.drawCircle(110f, 125f, 12f, paint)
        canvas.drawCircle(190f, 125f, 12f, paint)
        
        // Lágrimas kawaii
        paint.color = Color.parseColor("#87CEEB")
        paint.style = Paint.Style.FILL
        // Lágrima izquierda
        val leftTearPath = Path().apply {
            moveTo(105f, 140f)
            lineTo(100f, 160f)
            lineTo(110f, 155f)
            close()
        }
        canvas.drawPath(leftTearPath, paint)
        
        // Lágrima derecha
        val rightTearPath = Path().apply {
            moveTo(195f, 140f)
            lineTo(200f, 160f)
            lineTo(190f, 155f)
            close()
        }
        canvas.drawPath(rightTearPath, paint)
        
        // Boca triste
        paint.color = Color.parseColor("#FF8888")
        paint.strokeWidth = 4f
        paint.style = Paint.Style.STROKE
        val sadMouthPath = Path().apply {
            moveTo(130f, 175f)
            quadTo(150f, 165f, 170f, 175f)
        }
        canvas.drawPath(sadMouthPath, paint)
    }
    
    private fun drawLookingDirection(canvas: Canvas, paint: Paint, direction: String) {
        // Ojos normales
        paint.color = kawaiiColors["eyes"]!!
        paint.style = Paint.Style.FILL
        canvas.drawCircle(110f, 120f, 15f, paint)
        canvas.drawCircle(190f, 120f, 15f, paint)
        
        // Pupilas en dirección específica
        paint.color = Color.BLACK
        when (direction) {
            "left" -> {
                canvas.drawCircle(105f, 120f, 10f, paint)
                canvas.drawCircle(185f, 120f, 10f, paint)
            }
            "right" -> {
                canvas.drawCircle(115f, 120f, 10f, paint)
                canvas.drawCircle(195f, 120f, 10f, paint)
            }
            "up" -> {
                canvas.drawCircle(110f, 115f, 10f, paint)
                canvas.drawCircle(190f, 115f, 10f, paint)
            }
        }
        
        // Boca neutral
        paint.color = Color.parseColor("#FFAAAA")
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(140f, 165f, 160f, 175f, 5f, 5f, paint)
    }
    
    private fun drawBlinkingExpression(canvas: Canvas, paint: Paint) {
        // Ojos cerrados (líneas horizontales)
        paint.color = kawaiiColors["eyes"]!!
        paint.strokeWidth = 4f
        paint.style = Paint.Style.STROKE
        
        canvas.drawLine(95f, 120f, 125f, 120f, paint)
        canvas.drawLine(175f, 120f, 205f, 120f, paint)
        
        // Boca neutral
        paint.color = Color.parseColor("#FFAAAA")
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(140f, 165f, 160f, 175f, 5f, 5f, paint)
    }
    
    private fun drawStar(canvas: Canvas, paint: Paint, centerX: Float, centerY: Float, radius: Float) {
        val starPath = Path()
        
        for (i in 0 until 10) {
            val angle = (i * 36 - 90) * Math.PI / 180
            val r = if (i % 2 == 0) radius else radius * 0.5f
            val x = centerX + (r * Math.cos(angle)).toFloat()
            val y = centerY + (r * Math.sin(angle)).toFloat()
            
            if (i == 0) {
                starPath.moveTo(x, y)
            } else {
                starPath.lineTo(x, y)
            }
        }
        starPath.close()
        canvas.drawPath(starPath, paint)
    }
    
    private fun addKawaiiEffects(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        
        // Sparkles kawaii aleatorios
        paint.color = Color.parseColor("#FFD700")
        paint.alpha = 150
        
        // Pequeñas estrellitas
        for (i in 0..5) {
            val x = (50..250).random().toFloat()
            val y = (50..550).random().toFloat()
            val size = (3..8).random().toFloat()
            drawStar(canvas, paint, x, y, size)
        }
        
        // Brillo sutil alrededor del avatar
        paint.color = Color.parseColor("#FFE4E1")
        paint.alpha = 30
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawRoundRect(5f, 5f, 295f, 595f, 20f, 20f, paint)
    }
    
    /**
     * Cambia la ropa del avatar
     */
    suspend fun changeClothing(clothingType: String) {
        currentOutfit = clothingType
        clothingLayer = generateClothingLayer(clothingType)
    }
    
    private suspend fun generateClothingLayer(clothingType: String): Bitmap = 
        withContext(Dispatchers.Default) {
            // Aquí se generaría la ropa según el tipo
            // Por simplicidad, retornamos una capa básica
            Bitmap.createBitmap(300, 600, Bitmap.Config.ARGB_8888)
        }
    
    /**
     * Pequeños ajustes en la ropa como comportamiento natural
     */
    suspend fun adjustClothing() {
        // Simular pequeños ajustes en la ropa
        clothingLayer?.let { originalClothing ->
            val adjustedClothing = originalClothing.copy(Bitmap.Config.ARGB_8888, true)
            // Aquí se harían pequeños ajustes visuales
            clothingLayer = adjustedClothing
        }
    }
    
    /**
     * Habilita/deshabilita modo sin censura
     */
    fun setUncensoredMode(enabled: Boolean) {
        isUncensoredMode = enabled
    }
    
    private fun generateDefaultSprites() {
        // Generar sprites por defecto si no se encuentran los recursos
        val defaultWidth = 300
        val defaultHeight = 600
        
        baseBodyLayer = Bitmap.createBitmap(defaultWidth, defaultHeight, Bitmap.Config.ARGB_8888)
        // ... generar sprites básicos por código
    }
}