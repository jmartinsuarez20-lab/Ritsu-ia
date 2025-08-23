package com.ritsu.ai.data.util

import androidx.room.TypeConverter
import com.ritsu.ai.data.model.*

class Converters {
    
    // Convertidores para ClothingCategory
    @TypeConverter
    fun fromClothingCategory(category: ClothingCategory): String {
        return category.name
    }
    
    @TypeConverter
    fun toClothingCategory(category: String): ClothingCategory {
        return try {
            ClothingCategory.valueOf(category)
        } catch (e: IllegalArgumentException) {
            ClothingCategory.TOP
        }
    }
    
    // Convertidores para ClothingStyle
    @TypeConverter
    fun fromClothingStyle(style: ClothingStyle): String {
        return style.name
    }
    
    @TypeConverter
    fun toClothingStyle(style: String): ClothingStyle {
        return try {
            ClothingStyle.valueOf(style)
        } catch (e: IllegalArgumentException) {
            ClothingStyle.CASUAL
        }
    }
    
    // Convertidores para ClothingColor
    @TypeConverter
    fun fromClothingColor(color: ClothingColor): String {
        return color.name
    }
    
    @TypeConverter
    fun toClothingColor(color: String): ClothingColor {
        return try {
            ClothingColor.valueOf(color)
        } catch (e: IllegalArgumentException) {
            ClothingColor.BLUE
        }
    }
    
    // Convertidores para ClothingPattern
    @TypeConverter
    fun fromClothingPattern(pattern: ClothingPattern): String {
        return pattern.name
    }
    
    @TypeConverter
    fun toClothingPattern(pattern: String): ClothingPattern {
        return try {
            ClothingPattern.valueOf(pattern)
        } catch (e: IllegalArgumentException) {
            ClothingPattern.PLAIN
        }
    }
    
    // Convertidores para ClothingSeason
    @TypeConverter
    fun fromClothingSeason(season: ClothingSeason): String {
        return season.name
    }
    
    @TypeConverter
    fun toClothingSeason(season: String): ClothingSeason {
        return try {
            ClothingSeason.valueOf(season)
        } catch (e: IllegalArgumentException) {
            ClothingSeason.ALL
        }
    }
    
    // Convertidores para ClothingFormality
    @TypeConverter
    fun fromClothingFormality(formality: ClothingFormality): String {
        return formality.name
    }
    
    @TypeConverter
    fun toClothingFormality(formality: String): ClothingFormality {
        return try {
            ClothingFormality.valueOf(formality)
        } catch (e: IllegalArgumentException) {
            ClothingFormality.CASUAL
        }
    }
    
    // Convertidores para PatternType
    @TypeConverter
    fun fromPatternType(patternType: PatternType): String {
        return patternType.name
    }
    
    @TypeConverter
    fun toPatternType(patternType: String): PatternType {
        return try {
            PatternType.valueOf(patternType)
        } catch (e: IllegalArgumentException) {
            PatternType.COMMAND
        }
    }
    
    // Convertidores para PreferenceCategory
    @TypeConverter
    fun fromPreferenceCategory(category: PreferenceCategory): String {
        return category.name
    }
    
    @TypeConverter
    fun toPreferenceCategory(category: String): PreferenceCategory {
        return try {
            PreferenceCategory.valueOf(category)
        } catch (e: IllegalArgumentException) {
            PreferenceCategory.GENERAL
        }
    }
    
    // Convertidores para List<String>
    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return list.joinToString(",")
    }
    
    @TypeConverter
    fun toStringList(string: String): List<String> {
        return if (string.isEmpty()) {
            emptyList()
        } else {
            string.split(",")
        }
    }
    
    // Convertidores para Map<String, String>
    @TypeConverter
    fun fromStringMap(map: Map<String, String>): String {
        return map.entries.joinToString(";") { "${it.key}:${it.value}" }
    }
    
    @TypeConverter
    fun toStringMap(string: String): Map<String, String> {
        return if (string.isEmpty()) {
            emptyMap()
        } else {
            string.split(";").associate { 
                val parts = it.split(":", limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else parts[0] to ""
            }
        }
    }
    
    // Convertidores para Boolean (para compatibilidad)
    @TypeConverter
    fun fromBoolean(value: Boolean): Int {
        return if (value) 1 else 0
    }
    
    @TypeConverter
    fun toBoolean(value: Int): Boolean {
        return value == 1
    }
    
    // Convertidores para Long
    @TypeConverter
    fun fromLong(value: Long): String {
        return value.toString()
    }
    
    @TypeConverter
    fun toLong(value: String): Long {
        return try {
            value.toLong()
        } catch (e: NumberFormatException) {
            0L
        }
    }
    
    // Convertidores para Float
    @TypeConverter
    fun fromFloat(value: Float): String {
        return value.toString()
    }
    
    @TypeConverter
    fun toFloat(value: String): Float {
        return try {
            value.toFloat()
        } catch (e: NumberFormatException) {
            0f
        }
    }
    
    // Convertidores para Int
    @TypeConverter
    fun fromInt(value: Int): String {
        return value.toString()
    }
    
    @TypeConverter
    fun toInt(value: String): Int {
        return try {
            value.toInt()
        } catch (e: NumberFormatException) {
            0
        }
    }
}