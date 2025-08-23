package com.ritsuai.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

@Entity(tableName = "avatar_outfits")
data class AvatarOutfitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "description")
    val description: String,
    
    @ColumnInfo(name = "clothing_items")
    val clothingItems: List<String>, // Lista de IDs de ropa
    
    @ColumnInfo(name = "category")
    val category: String, // casual, formal, sport, special, uncensored
    
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date,
    
    @ColumnInfo(name = "last_worn")
    val lastWorn: Date? = null,
    
    @ColumnInfo(name = "wear_count")
    val wearCount: Int = 0,
    
    @ColumnInfo(name = "generated_by_ai")
    val generatedByAI: Boolean = true,
    
    @ColumnInfo(name = "user_prompt")
    val userPrompt: String? = null,
    
    @ColumnInfo(name = "is_uncensored")
    val isUncensored: Boolean = false,
    
    @ColumnInfo(name = "accessibility_level")
    val accessibilityLevel: Int = 0 // 0 = normal, 1 = partial, 2 = full
)