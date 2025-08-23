package com.ritsu.ai.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.ritsu.ai.R
import com.ritsu.ai.data.RitsuDatabase
import com.ritsu.ai.data.model.AvatarConfig
import com.ritsu.ai.data.model.ClothingItem
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream

class AvatarManager(private val context: Context) {
    
    private val database = RitsuDatabase.getInstance(context)
    private val preferenceManager = PreferenceManager(context)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var currentAvatarConfig: AvatarConfig? = null
    private var currentClothing: ClothingItem? = null
    private var avatarBitmap: Bitmap? = null
    
    // Configuración del avatar
    private val avatarSize = preferenceManager.getAvatarSize()
    private val avatarPositionX = preferenceManager.getAvatarPositionX()
    private val avatarPositionY = preferenceManager.getAvatarPositionY()
    
    // Expresiones disponibles
    private val expressions = mapOf(
        "neutral" to "😐",
        "happy" to "😊",
        "sad" to "😢",
        "angry" to "😠",
        "surprised" to "😲",
        "wink" to "😉",
        "laugh" to "😄",
        "cry" to "😭",
        "love" to "🥰",
        "sleepy" to "😴",
        "confused" to "😕",
        "excited" to "🤩",
        "nervous" to "😰",
        "cool" to "😎",
        "shy" to "😳",
        "special" to "😈"
    )
    
    init {
        initializeAvatar()
    }
    
    private fun initializeAvatar() {
        scope.launch {
            try {
                // Cargar configuración del avatar
                currentAvatarConfig = database.avatarConfigDao().getAvatarConfig("ritsu_avatar")
                    ?: AvatarConfig.getDefaultConfig()
                
                // Cargar ropa actual
                currentClothing = preferenceManager.getCurrentClothing()
                    ?: ClothingItem.createDefaultClothing()
                
                // Generar avatar inicial
                generateAvatarBitmap()
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun initializeAvatar() {
        scope.launch {
            try {
                // Crear configuración por defecto si no existe
                if (currentAvatarConfig == null) {
                    currentAvatarConfig = AvatarConfig.getDefaultConfig()
                    database.avatarConfigDao().insertAvatarConfig(currentAvatarConfig!!)
                }
                
                // Crear ropa por defecto si no existe
                if (currentClothing == null) {
                    currentClothing = ClothingItem.createDefaultClothing()
                    preferenceManager.setCurrentClothing(currentClothing!!)
                }
                
                // Generar avatar
                generateAvatarBitmap()
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private suspend fun generateAvatarBitmap() {
        try {
            val config = currentAvatarConfig ?: return
            val clothing = currentClothing ?: return
            
            // Crear bitmap base
            val bitmap = Bitmap.createBitmap(config.size, config.size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            // Dibujar fondo transparente
            canvas.drawColor(Color.TRANSPARENT)
            
            // Dibujar cuerpo base (anime kawaii)
            drawBaseBody(canvas, config)
            
            // Dibujar ropa
            if (!clothing.isNude || !config.isSpecialModeEnabled) {
                drawClothing(canvas, clothing, config)
            }
            
            // Dibujar expresión
            drawExpression(canvas, config.expression, config)
            
            // Aplicar efectos especiales si está en modo especial
            if (config.isSpecialModeEnabled) {
                applySpecialEffects(canvas, config)
            }
            
            avatarBitmap = bitmap
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun drawBaseBody(canvas: Canvas, config: AvatarConfig) {
        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        
        val centerX = config.size / 2f
        val centerY = config.size / 2f
        val bodyRadius = config.size * 0.4f
        
        // Color de piel (tono anime)
        paint.color = Color.rgb(255, 224, 189)
        
        // Dibujar cabeza
        canvas.drawCircle(centerX, centerY - bodyRadius * 0.3f, bodyRadius * 0.8f, paint)
        
        // Dibujar cuerpo
        canvas.drawCircle(centerX, centerY + bodyRadius * 0.2f, bodyRadius, paint)
        
        // Dibujar brazos
        canvas.drawCircle(centerX - bodyRadius * 0.8f, centerY, bodyRadius * 0.3f, paint)
        canvas.drawCircle(centerX + bodyRadius * 0.8f, centerY, bodyRadius * 0.3f, paint)
        
        // Dibujar piernas
        canvas.drawCircle(centerX - bodyRadius * 0.3f, centerY + bodyRadius * 1.2f, bodyRadius * 0.4f, paint)
        canvas.drawCircle(centerX + bodyRadius * 0.3f, centerY + bodyRadius * 1.2f, bodyRadius * 0.4f, paint)
    }
    
    private fun drawClothing(canvas: Canvas, clothing: ClothingItem, config: AvatarConfig) {
        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        
        val centerX = config.size / 2f
        val centerY = config.size / 2f
        val bodyRadius = config.size * 0.4f
        
        // Color de la ropa
        paint.color = getClothingColor(clothing.color)
        
        when (clothing.category) {
            ClothingCategory.TOP -> {
                // Dibujar camisa/blusa
                canvas.drawCircle(centerX, centerY + bodyRadius * 0.1f, bodyRadius * 0.9f, paint)
            }
            ClothingCategory.DRESS -> {
                // Dibujar vestido
                canvas.drawCircle(centerX, centerY + bodyRadius * 0.3f, bodyRadius * 1.1f, paint)
            }
            ClothingCategory.UNIFORM -> {
                // Dibujar uniforme escolar
                paint.color = Color.rgb(100, 150, 255) // Azul escolar
                canvas.drawCircle(centerX, centerY + bodyRadius * 0.1f, bodyRadius * 0.9f, paint)
                
                // Corbata
                paint.color = Color.rgb(255, 100, 100)
                canvas.drawRect(
                    centerX - bodyRadius * 0.1f,
                    centerY - bodyRadius * 0.2f,
                    centerX + bodyRadius * 0.1f,
                    centerY + bodyRadius * 0.3f,
                    paint
                )
            }
            ClothingCategory.SWIMWEAR -> {
                // Traje de baño
                paint.color = Color.rgb(255, 100, 150)
                canvas.drawCircle(centerX, centerY + bodyRadius * 0.2f, bodyRadius * 0.8f, paint)
            }
            ClothingCategory.SPECIAL -> {
                // Ropa especial (modo sin censura)
                if (clothing.isNude && config.isSpecialModeEnabled) {
                    // No dibujar ropa
                    return
                } else {
                    paint.color = Color.rgb(255, 0, 0)
                    canvas.drawCircle(centerX, centerY + bodyRadius * 0.1f, bodyRadius * 0.9f, paint)
                }
            }
            else -> {
                // Ropa por defecto
                canvas.drawCircle(centerX, centerY + bodyRadius * 0.1f, bodyRadius * 0.9f, paint)
            }
        }
    }
    
    private fun drawExpression(canvas: Canvas, expression: String, config: AvatarConfig) {
        val paint = Paint().apply {
            isAntiAlias = true
            textSize = config.size * 0.3f
            textAlign = Paint.Align.CENTER
        }
        
        val centerX = config.size / 2f
        val centerY = config.size / 2f - config.size * 0.1f
        
        val emoji = expressions[expression] ?: expressions["neutral"] ?: "😐"
        
        // Dibujar emoji como expresión
        canvas.drawText(emoji, centerX, centerY, paint)
    }
    
    private fun applySpecialEffects(canvas: Canvas, config: AvatarConfig) {
        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = config.borderWidth
            color = config.borderColor
        }
        
        // Dibujar borde especial
        if (config.borderWidth > 0) {
            canvas.drawCircle(config.size / 2f, config.size / 2f, config.size * 0.45f, paint)
        }
        
        // Aplicar sombra especial
        if (config.showShadow) {
            paint.style = Paint.Style.FILL
            paint.color = config.shadowColor
            paint.maskFilter = null
            canvas.drawCircle(
                config.size / 2f + config.shadowOffsetX,
                config.size / 2f + config.shadowOffsetY,
                config.size * 0.45f,
                paint
            )
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
    
    fun updateExpression(expression: String) {
        scope.launch {
            try {
                val config = currentAvatarConfig ?: return@launch
                val updatedConfig = config.updateExpression(expression)
                currentAvatarConfig = updatedConfig
                
                database.avatarConfigDao().updateAvatarConfig(updatedConfig)
                generateAvatarBitmap()
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun updateClothing(clothing: ClothingItem) {
        scope.launch {
            try {
                currentClothing = clothing
                preferenceManager.setCurrentClothing(clothing)
                generateAvatarBitmap()
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun updatePosition(x: Float, y: Float) {
        scope.launch {
            try {
                val config = currentAvatarConfig ?: return@launch
                val updatedConfig = config.updatePosition(x, y)
                currentAvatarConfig = updatedConfig
                
                database.avatarConfigDao().updateAvatarConfig(updatedConfig)
                preferenceManager.setAvatarPositionX(x)
                preferenceManager.setAvatarPositionY(y)
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun updateSize(size: Int) {
        scope.launch {
            try {
                val config = currentAvatarConfig ?: return@launch
                val updatedConfig = config.updateSize(size)
                currentAvatarConfig = updatedConfig
                
                database.avatarConfigDao().updateAvatarConfig(updatedConfig)
                preferenceManager.setAvatarSize(size)
                generateAvatarBitmap()
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun toggleSpecialMode(enabled: Boolean) {
        scope.launch {
            try {
                val config = currentAvatarConfig ?: return@launch
                val updatedConfig = config.setSpecialMode(enabled)
                currentAvatarConfig = updatedConfig
                
                database.avatarConfigDao().updateAvatarConfig(updatedConfig)
                generateAvatarBitmap()
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun getCurrentAvatarBitmap(): Bitmap? {
        return avatarBitmap
    }
    
    fun getCurrentConfig(): AvatarConfig? {
        return currentAvatarConfig
    }
    
    fun getCurrentClothing(): ClothingItem? {
        return currentClothing
    }
    
    fun cleanup() {
        scope.cancel()
        avatarBitmap?.recycle()
    }
}