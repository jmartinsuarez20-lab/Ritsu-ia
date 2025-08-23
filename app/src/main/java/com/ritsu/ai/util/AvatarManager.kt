package com.ritsu.ai.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.ritsu.ai.data.model.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*

class AvatarManager(private val context: Context) {
    
    companion object {
        private const val AVATAR_SIZE = 512
        private const val ANIMATION_DURATION = 300L
        private const val SPECIAL_MODE_KEYWORD = "ritsu_sin_censura_2024"
    }
    
    // Estado del avatar
    private var currentClothing: ClothingItem? = null
    private var currentEmotion: String = "neutral"
    private var currentPosition: Position = Position.CENTER
    private var isSpecialModeEnabled: Boolean = false
    private var isVisible: Boolean = true
    
    // Componentes del avatar
    private var baseAvatar: Bitmap? = null
    private var clothingLayer: Bitmap? = null
    private var emotionLayer: Bitmap? = null
    private var finalAvatar: Bitmap? = null
    
    // Animaciones
    private var currentAnimation: Animation? = null
    private var animationState: AnimationState = AnimationState.IDLE
    
    // Preferencias del usuario
    private var userPreferences: UserPreferences? = null
    
    // Callbacks
    private var onAvatarChangedListener: ((AvatarState) -> Unit)? = null
    private var onAnimationCompleteListener: (() -> Unit)? = null
    
    suspend fun initialize() {
        withContext(Dispatchers.IO) {
            try {
                // Cargar avatar base
                loadBaseAvatar()
                
                // Cargar ropa por defecto
                loadDefaultClothing()
                
                // Cargar expresiones
                loadEmotionAssets()
                
                // Aplicar configuración inicial
                applyInitialConfiguration()
                
                // Generar avatar final
                generateFinalAvatar()
                
            } catch (e: Exception) {
                // Crear avatar por defecto si hay error
                createDefaultAvatar()
            }
        }
    }
    
    private fun loadBaseAvatar() {
        try {
            val avatarFile = File(context.getExternalFilesDir(null), "base_avatar.png")
            if (avatarFile.exists()) {
                baseAvatar = BitmapFactory.decodeFile(avatarFile.absolutePath)
            } else {
                // Crear avatar base por defecto
                baseAvatar = createDefaultBaseAvatar()
                saveBaseAvatar()
            }
        } catch (e: Exception) {
            baseAvatar = createDefaultBaseAvatar()
        }
    }
    
    private fun createDefaultBaseAvatar(): Bitmap {
        val bitmap = Bitmap.createBitmap(AVATAR_SIZE, AVATAR_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Fondo transparente
        canvas.drawColor(Color.TRANSPARENT)
        
        // Cabeza (círculo)
        val headPaint = Paint().apply {
            color = Color.rgb(255, 218, 185) // Color piel
            isAntiAlias = true
        }
        val headRadius = AVATAR_SIZE * 0.3f
        val headCenterX = AVATAR_SIZE * 0.5f
        val headCenterY = AVATAR_SIZE * 0.4f
        canvas.drawCircle(headCenterX, headCenterY, headRadius, headPaint)
        
        // Cabello (estilo anime)
        val hairPaint = Paint().apply {
            color = Color.rgb(139, 69, 19) // Marrón
            isAntiAlias = true
        }
        val hairPath = android.graphics.Path()
        hairPath.moveTo(headCenterX - headRadius * 0.8f, headCenterY - headRadius * 0.5f)
        hairPath.quadTo(headCenterX, headCenterY - headRadius * 1.2f, headCenterX + headRadius * 0.8f, headCenterY - headRadius * 0.5f)
        hairPath.lineTo(headCenterX + headRadius * 0.6f, headCenterY + headRadius * 0.3f)
        hairPath.quadTo(headCenterX, headCenterY + headRadius * 0.1f, headCenterX - headRadius * 0.6f, headCenterY + headRadius * 0.3f)
        hairPath.close()
        canvas.drawPath(hairPath, hairPaint)
        
        // Ojos
        val eyePaint = Paint().apply {
            color = Color.rgb(70, 130, 180) // Azul
            isAntiAlias = true
        }
        val eyeRadius = headRadius * 0.15f
        val leftEyeX = headCenterX - headRadius * 0.3f
        val rightEyeX = headCenterX + headRadius * 0.3f
        val eyeY = headCenterY - headRadius * 0.1f
        canvas.drawCircle(leftEyeX, eyeY, eyeRadius, eyePaint)
        canvas.drawCircle(rightEyeX, eyeY, eyeRadius, eyePaint)
        
        // Pupilas
        val pupilPaint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }
        val pupilRadius = eyeRadius * 0.6f
        canvas.drawCircle(leftEyeX, eyeY, pupilRadius, pupilPaint)
        canvas.drawCircle(rightEyeX, eyeY, pupilRadius, pupilPaint)
        
        // Boca (neutral)
        val mouthPaint = Paint().apply {
            color = Color.rgb(255, 192, 203) // Rosa
            isAntiAlias = true
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        val mouthRect = RectF(
            headCenterX - headRadius * 0.2f,
            headCenterY + headRadius * 0.1f,
            headCenterX + headRadius * 0.2f,
            headCenterY + headRadius * 0.3f
        )
        canvas.drawArc(mouthRect, 0f, 180f, false, mouthPaint)
        
        // Cuerpo básico
        val bodyPaint = Paint().apply {
            color = Color.rgb(255, 255, 255) // Blanco
            isAntiAlias = true
        }
        val bodyRect = RectF(
            headCenterX - headRadius * 0.8f,
            headCenterY + headRadius * 0.8f,
            headCenterX + headRadius * 0.8f,
            headCenterY + headRadius * 2.5f
        )
        canvas.drawRect(bodyRect, bodyPaint)
        
        return bitmap
    }
    
    private fun saveBaseAvatar() {
        try {
            val avatarFile = File(context.getExternalFilesDir(null), "base_avatar.png")
            val outputStream = FileOutputStream(avatarFile)
            baseAvatar?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun loadDefaultClothing() {
        try {
            val clothingFile = File(context.getExternalFilesDir(null), "default_clothing.png")
            if (clothingFile.exists()) {
                clothingLayer = BitmapFactory.decodeFile(clothingFile.absolutePath)
            } else {
                // Crear ropa por defecto
                clothingLayer = createDefaultClothing()
                saveDefaultClothing()
            }
        } catch (e: Exception) {
            clothingLayer = createDefaultClothing()
        }
    }
    
    private fun createDefaultClothing(): Bitmap {
        val bitmap = Bitmap.createBitmap(AVATAR_SIZE, AVATAR_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Fondo transparente
        canvas.drawColor(Color.TRANSPARENT)
        
        // Vestido básico (estilo escolar)
        val dressPaint = Paint().apply {
            color = Color.rgb(100, 149, 237) // Azul claro
            isAntiAlias = true
        }
        
        val dressPath = android.graphics.Path()
        dressPath.moveTo(AVATAR_SIZE * 0.2f, AVATAR_SIZE * 0.6f)
        dressPath.lineTo(AVATAR_SIZE * 0.8f, AVATAR_SIZE * 0.6f)
        dressPath.lineTo(AVATAR_SIZE * 0.7f, AVATAR_SIZE * 0.9f)
        dressPath.lineTo(AVATAR_SIZE * 0.3f, AVATAR_SIZE * 0.9f)
        dressPath.close()
        canvas.drawPath(dressPath, dressPaint)
        
        // Cinturón
        val beltPaint = Paint().apply {
            color = Color.rgb(139, 69, 19) // Marrón
            isAntiAlias = true
            strokeWidth = 8f
        }
        canvas.drawLine(
            AVATAR_SIZE * 0.25f, AVATAR_SIZE * 0.7f,
            AVATAR_SIZE * 0.75f, AVATAR_SIZE * 0.7f,
            beltPaint
        )
        
        // Botones
        val buttonPaint = Paint().apply {
            color = Color.rgb(255, 255, 255) // Blanco
            isAntiAlias = true
        }
        val buttonRadius = 8f
        val buttonY = AVATAR_SIZE * 0.65f
        canvas.drawCircle(AVATAR_SIZE * 0.4f, buttonY, buttonRadius, buttonPaint)
        canvas.drawCircle(AVATAR_SIZE * 0.5f, buttonY, buttonRadius, buttonPaint)
        canvas.drawCircle(AVATAR_SIZE * 0.6f, buttonY, buttonRadius, buttonPaint)
        
        return bitmap
    }
    
    private fun saveDefaultClothing() {
        try {
            val clothingFile = File(context.getExternalFilesDir(null), "default_clothing.png")
            val outputStream = FileOutputStream(clothingFile)
            clothingLayer?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun loadEmotionAssets() {
        // Cargar diferentes expresiones faciales
        // Por ahora usaremos el avatar base y modificaremos la boca
    }
    
    private fun applyInitialConfiguration() {
        // Aplicar configuración inicial del avatar
        currentEmotion = "neutral"
        currentPosition = Position.CENTER
        isVisible = true
        isSpecialModeEnabled = false
    }
    
    fun changeClothing(clothing: ClothingItem) {
        try {
            currentClothing = clothing
            
            // Cargar nueva ropa
            if (clothing.imagePath.isNotEmpty()) {
                val clothingFile = File(clothing.imagePath)
                if (clothingFile.exists()) {
                    clothingLayer = BitmapFactory.decodeFile(clothingFile.absolutePath)
                }
            }
            
            // Regenerar avatar
            generateFinalAvatar()
            
            // Notificar cambio
            notifyAvatarChanged()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun setEmotion(emotion: String) {
        if (currentEmotion != emotion) {
            currentEmotion = emotion
            
            // Aplicar nueva expresión
            applyEmotion(emotion)
            
            // Regenerar avatar
            generateFinalAvatar()
            
            // Notificar cambio
            notifyAvatarChanged()
        }
    }
    
    private fun applyEmotion(emotion: String) {
        // Modificar expresión según la emoción
        when (emotion) {
            "happy" -> applyHappyExpression()
            "sad" -> applySadExpression()
            "surprised" -> applySurprisedExpression()
            "angry" -> applyAngryExpression()
            "concerned" -> applyConcernedExpression()
            else -> applyNeutralExpression()
        }
    }
    
    private fun applyHappyExpression() {
        // Modificar boca para sonrisa
        // Implementar modificación del avatar base
    }
    
    private fun applySadExpression() {
        // Modificar boca para tristeza
    }
    
    private fun applySurprisedExpression() {
        // Modificar ojos y boca para sorpresa
    }
    
    private fun applyAngryExpression() {
        // Modificar cejas y boca para enojo
    }
    
    private fun applyConcernedExpression() {
        // Modificar cejas para preocupación
    }
    
    private fun applyNeutralExpression() {
        // Restaurar expresión neutral
    }
    
    fun enableSpecialMode() {
        if (isSpecialModeEnabled) return
        
        isSpecialModeEnabled = true
        
        // Aplicar modo especial
        applySpecialMode()
        
        // Regenerar avatar
        generateFinalAvatar()
        
        // Notificar cambio
        notifyAvatarChanged()
    }
    
    private fun applySpecialMode() {
        // Implementar funcionalidades del modo especial
        // Esto incluiría diferentes niveles de ropa según el contexto
    }
    
    fun setPosition(position: Position) {
        currentPosition = position
        notifyAvatarChanged()
    }
    
    fun setVisibility(visible: Boolean) {
        isVisible = visible
        notifyAvatarChanged()
    }
    
    fun applyPreferences(preferences: UserPreferences) {
        userPreferences = preferences
        
        // Aplicar preferencias al avatar
        preferences.currentClothing?.let { clothingId ->
            // Cargar ropa guardada
            loadSavedClothing(clothingId)
        }
        
        // Aplicar otras preferencias
        if (preferences.specialModeUnlocked) {
            isSpecialModeEnabled = true
        }
        
        // Regenerar avatar
        generateFinalAvatar()
        
        // Notificar cambio
        notifyAvatarChanged()
    }
    
    private fun loadSavedClothing(clothingId: String) {
        // Implementar carga de ropa guardada
    }
    
    private fun generateFinalAvatar() {
        try {
            // Crear canvas para el avatar final
            val finalBitmap = Bitmap.createBitmap(AVATAR_SIZE, AVATAR_SIZE, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(finalBitmap)
            
            // Dibujar capas en orden
            baseAvatar?.let { base ->
                canvas.drawBitmap(base, 0f, 0f, null)
            }
            
            clothingLayer?.let { clothing ->
                canvas.drawBitmap(clothing, 0f, 0f, null)
            }
            
            emotionLayer?.let { emotion ->
                canvas.drawBitmap(emotion, 0f, 0f, null)
            }
            
            finalAvatar = finalBitmap
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun getCurrentState(): AvatarState {
        return AvatarState(
            currentClothing = currentClothing?.id ?: "",
            emotion = currentEmotion,
            isSpecialModeEnabled = isSpecialModeEnabled,
            position = currentPosition,
            isVisible = isVisible
        )
    }
    
    fun getAvatarBitmap(): Bitmap? {
        return finalAvatar
    }
    
    fun startAnimation(animationType: AnimationType) {
        when (animationType) {
            AnimationType.WAVE -> startWaveAnimation()
            AnimationType.JUMP -> startJumpAnimation()
            AnimationType.SPIN -> startSpinAnimation()
            AnimationType.FADE_IN -> startFadeInAnimation()
            AnimationType.FADE_OUT -> startFadeOutAnimation()
        }
    }
    
    private fun startWaveAnimation() {
        // Implementar animación de saludo
        animationState = AnimationState.WAVING
        // Usar AnimationUtils para crear animación
    }
    
    private fun startJumpAnimation() {
        // Implementar animación de salto
        animationState = AnimationState.JUMPING
    }
    
    private fun startSpinAnimation() {
        // Implementar animación de giro
        animationState = AnimationState.SPINNING
    }
    
    private fun startFadeInAnimation() {
        // Implementar animación de aparición
        animationState = AnimationState.FADING_IN
    }
    
    private fun startFadeOutAnimation() {
        // Implementar animación de desaparición
        animationState = AnimationState.FADING_OUT
    }
    
    private fun notifyAvatarChanged() {
        val state = getCurrentState()
        onAvatarChangedListener?.invoke(state)
    }
    
    fun setOnAvatarChangedListener(listener: (AvatarState) -> Unit) {
        onAvatarChangedListener = listener
    }
    
    fun setOnAnimationCompleteListener(listener: () -> Unit) {
        onAnimationCompleteListener = listener
    }
    
    fun cleanup() {
        // Liberar recursos
        baseAvatar?.recycle()
        clothingLayer?.recycle()
        emotionLayer?.recycle()
        finalAvatar?.recycle()
        
        baseAvatar = null
        clothingLayer = null
        emotionLayer = null
        finalAvatar = null
    }
    
    // Clases de datos
    data class AvatarState(
        val currentClothing: String,
        val emotion: String,
        val isSpecialModeEnabled: Boolean,
        val position: Position,
        val isVisible: Boolean
    )
    
    data class Position(
        val x: Float = 0.5f,
        val y: Float = 0.5f
    )
    
    data class ClothingItem(
        val id: String,
        val name: String,
        val description: String,
        val imagePath: String,
        val category: String,
        val isSpecial: Boolean = false
    )
    
    enum class AnimationType {
        WAVE, JUMP, SPIN, FADE_IN, FADE_OUT
    }
    
    enum class AnimationState {
        IDLE, WAVING, JUMPING, SPINNING, FADING_IN, FADING_OUT
    }
}