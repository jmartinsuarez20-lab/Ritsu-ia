package com.ritsu.ai.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "clothing_items")
data class ClothingItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val category: ClothingCategory,
    val style: ClothingStyle,
    val color: ClothingColor,
    val pattern: ClothingPattern,
    val season: ClothingSeason,
    val formality: ClothingFormality,
    val imagePath: String? = null,
    val isGenerated: Boolean = false,
    val generationPrompt: String? = null,
    val isSpecialMode: Boolean = false,
    val isNude: Boolean = false,
    val isFavorite: Boolean = false,
    val usageCount: Int = 0,
    val lastUsed: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun createDefaultClothing(): ClothingItem {
            return ClothingItem(
                name = "Uniforme Escolar",
                description = "Uniforme escolar estándar de Ritsu",
                category = ClothingCategory.UNIFORM,
                style = ClothingStyle.SCHOOL,
                color = ClothingColor.BLUE,
                pattern = ClothingPattern.PLAIN,
                season = ClothingSeason.ALL,
                formality = ClothingFormality.FORMAL
            )
        }
        
        fun createSpecialModeClothing(): ClothingItem {
            return ClothingItem(
                name = "Modo Especial",
                description = "Ropa especial para modo sin censura",
                category = ClothingCategory.SPECIAL,
                style = ClothingStyle.SPECIAL,
                color = ClothingColor.BLACK,
                pattern = ClothingPattern.PLAIN,
                season = ClothingSeason.ALL,
                formality = ClothingFormality.CASUAL,
                isSpecialMode = true,
                isNude = true
            )
        }
        
        fun fromContentValues(values: android.content.ContentValues): ClothingItem {
            return ClothingItem(
                id = values.getAsString("id") ?: UUID.randomUUID().toString(),
                name = values.getAsString("name") ?: "",
                description = values.getAsString("description") ?: "",
                category = ClothingCategory.valueOf(values.getAsString("category") ?: "TOP"),
                style = ClothingStyle.valueOf(values.getAsString("style") ?: "CASUAL"),
                color = ClothingColor.valueOf(values.getAsString("color") ?: "BLUE"),
                pattern = ClothingPattern.valueOf(values.getAsString("pattern") ?: "PLAIN"),
                season = ClothingSeason.valueOf(values.getAsString("season") ?: "ALL"),
                formality = ClothingFormality.valueOf(values.getAsString("formality") ?: "CASUAL"),
                imagePath = values.getAsString("imagePath"),
                isGenerated = values.getAsBoolean("isGenerated") ?: false,
                generationPrompt = values.getAsString("generationPrompt"),
                isSpecialMode = values.getAsBoolean("isSpecialMode") ?: false,
                isNude = values.getAsBoolean("isNude") ?: false,
                isFavorite = values.getAsBoolean("isFavorite") ?: false,
                usageCount = values.getAsInteger("usageCount") ?: 0,
                lastUsed = values.getAsLong("lastUsed") ?: 0L,
                createdAt = values.getAsLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = values.getAsLong("updatedAt") ?: System.currentTimeMillis()
            )
        }
    }
    
    fun incrementUsage(): ClothingItem {
        return copy(
            usageCount = usageCount + 1,
            lastUsed = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
    
    fun toggleFavorite(): ClothingItem {
        return copy(
            isFavorite = !isFavorite,
            updatedAt = System.currentTimeMillis()
        )
    }
    
    fun updateImagePath(path: String): ClothingItem {
        return copy(
            imagePath = path,
            updatedAt = System.currentTimeMillis()
        )
    }
    
    fun getDisplayName(): String {
        return if (isSpecialMode && isNude) {
            "Modo Especial"
        } else {
            name
        }
    }
    
    fun getFullDescription(): String {
        val parts = mutableListOf<String>()
        
        if (isSpecialMode) {
            parts.add("Modo especial")
        }
        
        parts.add(style.displayName)
        parts.add(color.displayName)
        
        if (pattern != ClothingPattern.PLAIN) {
            parts.add(pattern.displayName)
        }
        
        parts.add(category.displayName)
        
        return parts.joinToString(" ")
    }
}

enum class ClothingCategory(val displayName: String) {
    TOP("Parte Superior"),
    BOTTOM("Parte Inferior"),
    DRESS("Vestido"),
    UNIFORM("Uniforme"),
    SWIMWEAR("Traje de Baño"),
    UNDERWEAR("Ropa Interior"),
    ACCESSORY("Accesorio"),
    SPECIAL("Especial"),
    NUDE("Sin Ropa")
}

enum class ClothingStyle(val displayName: String) {
    CASUAL("Casual"),
    ELEGANT("Elegante"),
    SPORT("Deportivo"),
    SCHOOL("Escolar"),
    FORMAL("Formal"),
    BEACH("Playa"),
    WINTER("Invierno"),
    SUMMER("Verano"),
    SPECIAL("Especial"),
    NUDE("Sin Ropa")
}

enum class ClothingColor(val displayName: String, val hexCode: String) {
    PINK("Rosa", "#FFC0CB"),
    BLUE("Azul", "#0000FF"),
    GREEN("Verde", "#008000"),
    RED("Rojo", "#FF0000"),
    YELLOW("Amarillo", "#FFFF00"),
    BLACK("Negro", "#000000"),
    WHITE("Blanco", "#FFFFFF"),
    GRAY("Gris", "#808080"),
    PURPLE("Morado", "#800080"),
    ORANGE("Naranja", "#FFA500"),
    BROWN("Marrón", "#A52A2A"),
    CYAN("Cian", "#00FFFF"),
    MAGENTA("Magenta", "#FF00FF"),
    GOLD("Dorado", "#FFD700"),
    SILVER("Plateado", "#C0C0C0")
}

enum class ClothingPattern(val displayName: String) {
    PLAIN("Liso"),
    STRIPES("Rayas"),
    CHECKERED("Cuadros"),
    FLORAL("Flores"),
    POLKA("Puntos"),
    GEOMETRIC("Geométrico"),
    ANIMAL("Animal"),
    ABSTRACT("Abstracto"),
    PLAID("Escocés"),
    DOT("Puntos"),
    WAVE("Ondas"),
    STAR("Estrellas"),
    HEART("Corazones"),
    NONE("Sin Patrón")
}

enum class ClothingSeason(val displayName: String) {
    SPRING("Primavera"),
    SUMMER("Verano"),
    AUTUMN("Otoño"),
    WINTER("Invierno"),
    ALL("Todas las Estaciones")
}

enum class ClothingFormality(val displayName: String) {
    CASUAL("Casual"),
    SEMI_FORMAL("Semi Formal"),
    FORMAL("Formal"),
    VERY_FORMAL("Muy Formal"),
    SPECIAL("Especial")
}