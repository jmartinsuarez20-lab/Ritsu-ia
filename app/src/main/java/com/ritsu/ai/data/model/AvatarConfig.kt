package com.ritsu.ai.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "avatar_config")
data class AvatarConfig(
    @PrimaryKey val id: String = "ritsu_avatar",
    val name: String = "Ritsu",
    val size: Int = 200,
    val positionX: Float = 100f,
    val positionY: Float = 100f,
    val isVisible: Boolean = true,
    val expression: String = "neutral",
    val currentClothingId: String? = null,
    val isSpecialModeEnabled: Boolean = false,
    val animationSpeed: Float = 1.0f,
    val opacity: Float = 1.0f,
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    val isDraggable: Boolean = true,
    val isResizable: Boolean = true,
    val autoHide: Boolean = false,
    val autoHideDelay: Long = 5000L,
    val showShadow: Boolean = true,
    val shadowColor: Int = 0x80000000.toInt(),
    val shadowRadius: Float = 8f,
    val shadowOffsetX: Float = 2f,
    val shadowOffsetY: Float = 4f,
    val backgroundColor: Int = 0x00000000.toInt(),
    val borderColor: Int = 0x00000000.toInt(),
    val borderWidth: Float = 0f,
    val cornerRadius: Float = 0f,
    val zIndex: Int = 1000,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun getDefaultConfig(): AvatarConfig {
            return AvatarConfig()
        }
        
        fun getSpecialModeConfig(): AvatarConfig {
            return AvatarConfig(
                isSpecialModeEnabled = true,
                opacity = 0.9f,
                scale = 1.1f,
                shadowColor = 0x40FF0000.toInt(),
                borderColor = 0xFFFF0000.toInt(),
                borderWidth = 2f
            )
        }
        
        fun fromContentValues(values: android.content.ContentValues): AvatarConfig {
            return AvatarConfig(
                id = values.getAsString("id") ?: "ritsu_avatar",
                name = values.getAsString("name") ?: "Ritsu",
                size = values.getAsInteger("size") ?: 200,
                positionX = values.getAsFloat("positionX") ?: 100f,
                positionY = values.getAsFloat("positionY") ?: 100f,
                isVisible = values.getAsBoolean("isVisible") ?: true,
                expression = values.getAsString("expression") ?: "neutral",
                currentClothingId = values.getAsString("currentClothingId"),
                isSpecialModeEnabled = values.getAsBoolean("isSpecialModeEnabled") ?: false,
                animationSpeed = values.getAsFloat("animationSpeed") ?: 1.0f,
                opacity = values.getAsFloat("opacity") ?: 1.0f,
                scale = values.getAsFloat("scale") ?: 1.0f,
                rotation = values.getAsFloat("rotation") ?: 0f,
                isDraggable = values.getAsBoolean("isDraggable") ?: true,
                isResizable = values.getAsBoolean("isResizable") ?: true,
                autoHide = values.getAsBoolean("autoHide") ?: false,
                autoHideDelay = values.getAsLong("autoHideDelay") ?: 5000L,
                showShadow = values.getAsBoolean("showShadow") ?: true,
                shadowColor = values.getAsInteger("shadowColor") ?: 0x80000000.toInt(),
                shadowRadius = values.getAsFloat("shadowRadius") ?: 8f,
                shadowOffsetX = values.getAsFloat("shadowOffsetX") ?: 2f,
                shadowOffsetY = values.getAsFloat("shadowOffsetY") ?: 4f,
                backgroundColor = values.getAsInteger("backgroundColor") ?: 0x00000000.toInt(),
                borderColor = values.getAsInteger("borderColor") ?: 0x00000000.toInt(),
                borderWidth = values.getAsFloat("borderWidth") ?: 0f,
                cornerRadius = values.getAsFloat("cornerRadius") ?: 0f,
                zIndex = values.getAsInteger("zIndex") ?: 1000,
                createdAt = values.getAsLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = values.getAsLong("updatedAt") ?: System.currentTimeMillis()
            )
        }
    }
    
    fun updatePosition(x: Float, y: Float): AvatarConfig {
        return copy(
            positionX = x,
            positionY = y,
            updatedAt = System.currentTimeMillis()
        )
    }
    
    fun updateSize(size: Int): AvatarConfig {
        return copy(
            size = size,
            updatedAt = System.currentTimeMillis()
        )
    }
    
    fun updateExpression(expression: String): AvatarConfig {
        return copy(
            expression = expression,
            updatedAt = System.currentTimeMillis()
        )
    }
    
    fun updateClothing(clothingId: String?): AvatarConfig {
        return copy(
            currentClothingId = clothingId,
            updatedAt = System.currentTimeMillis()
        )
    }
    
    fun toggleSpecialMode(): AvatarConfig {
        return copy(
            isSpecialModeEnabled = !isSpecialModeEnabled,
            updatedAt = System.currentTimeMillis()
        )
    }
    
    fun setSpecialMode(enabled: Boolean): AvatarConfig {
        return copy(
            isSpecialModeEnabled = enabled,
            updatedAt = System.currentTimeMillis()
        )
    }
    
    fun updateVisibility(visible: Boolean): AvatarConfig {
        return copy(
            isVisible = visible,
            updatedAt = System.currentTimeMillis()
        )
    }
    
    fun updateOpacity(opacity: Float): AvatarConfig {
        return copy(
            opacity = opacity.coerceIn(0f, 1f),
            updatedAt = System.currentTimeMillis()
        )
    }
    
    fun updateScale(scale: Float): AvatarConfig {
        return copy(
            scale = scale.coerceIn(0.1f, 3f),
            updatedAt = System.currentTimeMillis()
        )
    }
    
    fun updateAnimationSpeed(speed: Float): AvatarConfig {
        return copy(
            animationSpeed = speed.coerceIn(0.1f, 3f),
            updatedAt = System.currentTimeMillis()
        )
    }
}