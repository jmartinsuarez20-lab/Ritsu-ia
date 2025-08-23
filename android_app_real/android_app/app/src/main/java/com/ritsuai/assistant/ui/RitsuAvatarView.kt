package com.ritsuai.assistant.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.ritsuai.assistant.R

class RitsuAvatarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    
    private var isActive = false
    private var currentExpression = "neutral"
    private var currentAnimator: AnimatorSet? = null
    
    // Mapeo de expresiones a imágenes
    private val expressionImages = mapOf(
        "neutral" to R.drawable.ritsu_avatar_neutral,
        "happy" to R.drawable.ritsu_avatar_happy,
        "talking" to R.drawable.ritsu_avatar_talking,
        "surprised" to R.drawable.ritsu_avatar_surprised,
        "thinking" to R.drawable.ritsu_avatar_thinking
    )
    
    init {
        setupInitialState()
    }
    
    private fun setupInitialState() {
        scaleType = ImageView.ScaleType.CENTER_CROP
        showExpression("neutral")
        
        // Animación de respiración sutil cuando está inactiva
        startBreathingAnimation()
    }
    
    fun setRitsuActive(active: Boolean) {
        isActive = active
        
        if (active) {
            showExpression("happy")
            startActiveAnimation()
        } else {
            showExpression("neutral")
            startBreathingAnimation()
        }
    }
    
    fun showExpression(expression: String) {
        if (currentExpression == expression) return
        
        currentExpression = expression
        val imageRes = expressionImages[expression] ?: R.drawable.ritsu_avatar_neutral
        
        // Transición suave entre expresiones
        animate()
            .alpha(0f)
            .setDuration(150)
            .withEndAction {
                setImageResource(imageRes)
                animate()
                    .alpha(1f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }
    
    private fun startBreathingAnimation() {
        currentAnimator?.cancel()
        
        val scaleUp = ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.02f)
        val scaleUpY = ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.02f)
        val scaleDown = ObjectAnimator.ofFloat(this, "scaleX", 1.02f, 1f)
        val scaleDownY = ObjectAnimator.ofFloat(this, "scaleY", 1.02f, 1f)
        
        scaleUp.duration = 2000
        scaleUpY.duration = 2000
        scaleDown.duration = 2000
        scaleDownY.duration = 2000
        
        currentAnimator = AnimatorSet().apply {
            play(scaleUp).with(scaleUpY)
            play(scaleDown).with(scaleDownY).after(scaleUp)
            
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator?) {
                    if (!isActive) {
                        startBreathingAnimation() // Repetir la animación
                    }
                }
            })
            
            start()
        }
    }
    
    private fun startActiveAnimation() {
        currentAnimator?.cancel()
        
        // Animación más dinámica cuando está activa
        val bounce = ObjectAnimator.ofFloat(this, "translationY", 0f, -10f, 0f)
        bounce.duration = 800
        bounce.repeatCount = ObjectAnimator.INFINITE
        bounce.repeatMode = ObjectAnimator.RESTART
        
        currentAnimator = AnimatorSet().apply {
            play(bounce)
            start()
        }
    }
    
    fun startTalkingAnimation() {
        showExpression("talking")
        
        // Animación de hablar (pequeños movimientos)
        currentAnimator?.cancel()
        
        val talkMove = ObjectAnimator.ofFloat(this, "translationX", -2f, 2f, -2f)
        talkMove.duration = 300
        talkMove.repeatCount = ObjectAnimator.INFINITE
        talkMove.repeatMode = ObjectAnimator.REVERSE
        
        currentAnimator = AnimatorSet().apply {
            play(talkMove)
            start()
        }
    }
    
    fun stopTalkingAnimation() {
        currentAnimator?.cancel()
        translationX = 0f
        
        if (isActive) {
            showExpression("happy")
            startActiveAnimation()
        } else {
            showExpression("neutral")
            startBreathingAnimation()
        }
    }
    
    fun expressEmotion(emotion: String) {
        when (emotion) {
            "surprise" -> {
                showExpression("surprised")
                // Volver a la expresión normal después de un momento
                postDelayed({
                    showExpression(if (isActive) "happy" else "neutral")
                }, 2000)
            }
            "thinking" -> {
                showExpression("thinking")
            }
            "happy" -> {
                showExpression("happy")
            }
        }
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        currentAnimator?.cancel()
    }
}