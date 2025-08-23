package com.ritsuai.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

@Entity(tableName = "clothing_items")
data class ClothingItemEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "description")
    val description: String,
    
    @ColumnInfo(name = "category")
    val category: String,
    
    @ColumnInfo(name = "subcategory")
    val subcategory: String,
    
    @ColumnInfo(name = "color")
    val color: Int,
    
    @ColumnInfo(name = "style")
    val style: String,
    
    @ColumnInfo(name = "is_uncensored")
    val isUncensored: Boolean = false,
    
    @ColumnInfo(name = "accessibility_level")
    val accessibilityLevel: Int = 0,
    
    @ColumnInfo(name = "generated_by_ai")
    val generatedByAI: Boolean = true,
    
    @ColumnInfo(name = "user_prompt")
    val userPrompt: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Date,
    
    @ColumnInfo(name = "image_data")
    val imageData: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClothingItemEntity

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}