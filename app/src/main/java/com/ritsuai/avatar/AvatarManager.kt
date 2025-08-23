package com.ritsuai.avatar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.core.content.ContextCompat
import com.ritsuai.R
import com.ritsuai.database.dao.AvatarOutfitDao
import com.ritsuai.database.entities.AvatarOutfitEntity
import com.ritsuai.utils.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class AvatarManager(private val context: Context) {
    
    companion object {
        const val AVATAR_SIZE = 200
        const val EXPRESSION_NEUTRAL = "neutral"
        const val EXPRESSION_HAPPY = "happy"
        const val EXPRESSION_SAD = "sad"
        const val EXPRESSION_ANGRY = "angry"
        const val EXPRESSION_SURPRISED = "surprised"
        const val EXPRESSION_THINKING = "thinking"
        const val EXPRESSION_WORKING = "working"
        const val EXPRESSION_FLIRTY = "flirty"
    }
    
    private val preferenceManager = PreferenceManager(context)
    private val scope = CoroutineScope(Dispatchers.Main)
    
    // Estado actual del avatar
    private var currentExpression = EXPRESSION_NEUTRAL
    private var currentOutfit: AvatarOutfitEntity? = null
    private var isUncensoredMode = false
    
    // Colores del avatar
    private val skinColor = ContextCompat.getColor(context, R.color.ritsu_skin)
    private val hairColor = ContextCompat.getColor(context, R.color.ritsu_hair)
    private val eyeColor = ContextCompat.getColor(context, R.color.ritsu_eyes)
    
    init {
        loadCurrentState()
    }
    
    private fun loadCurrentState() {
        isUncensoredMode = preferenceManager.isUncensoredModeEnabled()
        scope.launch {
            currentOutfit = loadDefaultOutfit()
        }
    }
    
    suspend fun generateAvatar(
        expression: String = currentExpression,
        outfit: AvatarOutfitEntity? = currentOutfit,
        size: Int = AVATAR_SIZE
    ): Bitmap = withContext(Dispatchers.Default) {
        
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        
        // Dibujar fondo transparente
        canvas.drawColor(android.graphics.Color.TRANSPARENT)
        
        // Dibujar cuerpo base
        drawBody(canvas, paint, size)
        
        // Dibujar ropa si hay outfit
        outfit?.let { drawOutfit(canvas, paint, it, size) }
        
        // Dibujar cara y expresión
        drawFace(canvas, paint, expression, size)
        
        // Dibujar cabello
        drawHair(canvas, paint, size)
        
        // Dibujar accesorios
        drawAccessories(canvas, paint, outfit, size)
        
        bitmap
    }
    
    private fun drawBody(canvas: Canvas, paint: Paint, size: Int) {
        val bodyRect = RectF(size * 0.3f, size * 0.4f, size * 0.7f, size * 0.9f)
        
        // Cuerpo principal
        paint.color = skinColor
        canvas.drawRoundRect(bodyRect, size * 0.1f, size * 0.1f, paint)
        
        // Brazos
        val leftArmRect = RectF(size * 0.2f, size * 0.45f, size * 0.3f, size * 0.7f)
        val rightArmRect = RectF(size * 0.7f, size * 0.45f, size * 0.8f, size * 0.7f)
        canvas.drawRoundRect(leftArmRect, size * 0.05f, size * 0.05f, paint)
        canvas.drawRoundRect(rightArmRect, size * 0.05f, size * 0.05f, paint)
        
        // Piernas
        val leftLegRect = RectF(size * 0.35f, size * 0.9f, size * 0.45f, size * 0.98f)
        val rightLegRect = RectF(size * 0.55f, size * 0.9f, size * 0.65f, size * 0.98f)
        canvas.drawRoundRect(leftLegRect, size * 0.05f, size * 0.05f, paint)
        canvas.drawRoundRect(rightLegRect, size * 0.05f, size * 0.05f, paint)
    }
    
    private fun drawFace(canvas: Canvas, paint: Paint, expression: String, size: Int) {
        // Cara base
        val faceRect = RectF(size * 0.35f, size * 0.2f, size * 0.65f, size * 0.4f)
        paint.color = skinColor
        canvas.drawOval(faceRect, paint)
        
        // Ojos
        drawEyes(canvas, paint, expression, size)
        
        // Boca según expresión
        drawMouth(canvas, paint, expression, size)
        
        // Mejillas
        if (expression == EXPRESSION_HAPPY || expression == EXPRESSION_FLIRTY) {
            drawCheeks(canvas, paint, size)
        }
    }
    
    private fun drawEyes(canvas: Canvas, paint: Paint, expression: String, size: Int) {
        paint.color = eyeColor
        
        when (expression) {
            EXPRESSION_HAPPY -> {
                // Ojos cerrados felices
                val leftEye = Path()
                leftEye.moveTo(size * 0.4f, size * 0.28f)
                leftEye.quadTo(size * 0.42f, size * 0.26f, size * 0.44f, size * 0.28f)
                canvas.drawPath(leftEye, paint)
                
                val rightEye = Path()
                rightEye.moveTo(size * 0.56f, size * 0.28f)
                rightEye.quadTo(size * 0.58f, size * 0.26f, size * 0.6f, size * 0.28f)
                canvas.drawPath(rightEye, paint)
            }
            EXPRESSION_SURPRISED -> {
                // Ojos sorprendidos (círculos)
                canvas.drawCircle(size * 0.42f, size * 0.28f, size * 0.02f, paint)
                canvas.drawCircle(size * 0.58f, size * 0.28f, size * 0.02f, paint)
            }
            EXPRESSION_ANGRY -> {
                // Ojos enojados (triángulos)
                val leftEye = Path()
                leftEye.moveTo(size * 0.4f, size * 0.26f)
                leftEye.lineTo(size * 0.44f, size * 0.28f)
                leftEye.lineTo(size * 0.4f, size * 0.3f)
                leftEye.close()
                canvas.drawPath(leftEye, paint)
                
                val rightEye = Path()
                rightEye.moveTo(size * 0.56f, size * 0.26f)
                rightEye.lineTo(size * 0.6f, size * 0.28f)
                rightEye.lineTo(size * 0.56f, size * 0.3f)
                rightEye.close()
                canvas.drawPath(rightEye, paint)
            }
            else -> {
                // Ojos normales (óvalos)
                canvas.drawOval(
                    RectF(size * 0.38f, size * 0.26f, size * 0.44f, size * 0.3f),
                    paint
                )
                canvas.drawOval(
                    RectF(size * 0.56f, size * 0.26f, size * 0.62f, size * 0.3f),
                    paint
                )
            }
        }
    }
    
    private fun drawMouth(canvas: Canvas, paint: Paint, expression: String, size: Int) {
        paint.color = ContextCompat.getColor(context, R.color.ritsu_mouth)
        
        when (expression) {
            EXPRESSION_HAPPY -> {
                // Boca feliz (arco hacia arriba)
                val mouth = Path()
                mouth.moveTo(size * 0.45f, size * 0.35f)
                mouth.quadTo(size * 0.5f, size * 0.38f, size * 0.55f, size * 0.35f)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = size * 0.01f
                canvas.drawPath(mouth, paint)
                paint.style = Paint.Style.FILL
            }
            EXPRESSION_SAD -> {
                // Boca triste (arco hacia abajo)
                val mouth = Path()
                mouth.moveTo(size * 0.45f, size * 0.38f)
                mouth.quadTo(size * 0.5f, size * 0.35f, size * 0.55f, size * 0.38f)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = size * 0.01f
                canvas.drawPath(mouth, paint)
                paint.style = Paint.Style.FILL
            }
            EXPRESSION_SURPRISED -> {
                // Boca sorprendida (círculo)
                canvas.drawCircle(size * 0.5f, size * 0.36f, size * 0.015f, paint)
            }
            EXPRESSION_FLIRTY -> {
                // Boca coqueta (pequeña sonrisa)
                val mouth = Path()
                mouth.moveTo(size * 0.47f, size * 0.35f)
                mouth.quadTo(size * 0.5f, size * 0.37f, size * 0.53f, size * 0.35f)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = size * 0.01f
                canvas.drawPath(mouth, paint)
                paint.style = Paint.Style.FILL
            }
            else -> {
                // Boca neutral (línea recta)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = size * 0.01f
                canvas.drawLine(
                    size * 0.45f, size * 0.35f,
                    size * 0.55f, size * 0.35f,
                    paint
                )
                paint.style = Paint.Style.FILL
            }
        }
    }
    
    private fun drawCheeks(canvas: Canvas, paint: Paint, size: Int) {
        paint.color = ContextCompat.getColor(context, R.color.ritsu_blush)
        canvas.drawCircle(size * 0.35f, size * 0.32f, size * 0.02f, paint)
        canvas.drawCircle(size * 0.65f, size * 0.32f, size * 0.02f, paint)
    }
    
    private fun drawHair(canvas: Canvas, paint: Paint, size: Int) {
        paint.color = hairColor
        
        // Cabello principal
        val hairPath = Path()
        hairPath.moveTo(size * 0.3f, size * 0.15f)
        hairPath.quadTo(size * 0.5f, size * 0.1f, size * 0.7f, size * 0.15f)
        hairPath.lineTo(size * 0.75f, size * 0.25f)
        hairPath.quadTo(size * 0.5f, size * 0.2f, size * 0.25f, size * 0.25f)
        hairPath.close()
        canvas.drawPath(hairPath, paint)
        
        // Mechas laterales
        val leftLock = Path()
        leftLock.moveTo(size * 0.25f, size * 0.25f)
        leftLock.quadTo(size * 0.2f, size * 0.3f, size * 0.22f, size * 0.4f)
        leftLock.quadTo(size * 0.25f, size * 0.35f, size * 0.28f, size * 0.3f)
        leftLock.close()
        canvas.drawPath(leftLock, paint)
        
        val rightLock = Path()
        rightLock.moveTo(size * 0.75f, size * 0.25f)
        rightLock.quadTo(size * 0.8f, size * 0.3f, size * 0.78f, size * 0.4f)
        rightLock.quadTo(size * 0.75f, size * 0.35f, size * 0.72f, size * 0.3f)
        rightLock.close()
        canvas.drawPath(rightLock, paint)
    }
    
    private fun drawOutfit(canvas: Canvas, paint: Paint, outfit: AvatarOutfitEntity, size: Int) {
        // Implementar dibujo de ropa según el outfit
        // Esto se implementará con el sistema de ropa generativa
    }
    
    private fun drawAccessories(canvas: Canvas, paint: Paint, outfit: AvatarOutfitEntity?, size: Int) {
        // Implementar accesorios según el outfit
    }
    
    suspend fun changeExpression(expression: String) {
        currentExpression = expression
        // Notificar cambio de expresión
    }
    
    suspend fun changeOutfit(outfit: AvatarOutfitEntity) {
        currentOutfit = outfit
        // Notificar cambio de outfit
    }
    
    fun toggleUncensoredMode() {
        isUncensoredMode = !isUncensoredMode
        preferenceManager.setUncensoredMode(isUncensoredMode)
    }
    
    private suspend fun loadDefaultOutfit(): AvatarOutfitEntity? {
        // Cargar outfit por defecto desde la base de datos
        return null // Implementar
    }
}