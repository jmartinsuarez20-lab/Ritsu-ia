package com.ritsu.ai.data.dao

import androidx.room.*
import com.ritsu.ai.data.model.ClothingItem
import com.ritsu.ai.data.model.ClothingCategory
import com.ritsu.ai.data.model.ClothingStyle
import com.ritsu.ai.data.model.ClothingColor
import com.ritsu.ai.data.model.ClothingPattern
import com.ritsu.ai.data.model.ClothingSeason
import com.ritsu.ai.data.model.ClothingFormality
import kotlinx.coroutines.flow.Flow

@Dao
interface ClothingItemDao {
    
    @Query("SELECT * FROM clothing_items WHERE id = :id")
    suspend fun getClothingItem(id: String): ClothingItem?
    
    @Query("SELECT * FROM clothing_items WHERE id = :id")
    fun getClothingItemFlow(id: String): Flow<ClothingItem?>
    
    @Query("SELECT * FROM clothing_items ORDER BY lastUsed DESC")
    suspend fun getAllClothingItems(): List<ClothingItem>
    
    @Query("SELECT * FROM clothing_items ORDER BY lastUsed DESC")
    fun getAllClothingItemsFlow(): Flow<List<ClothingItem>>
    
    @Query("SELECT * FROM clothing_items WHERE isFavorite = 1 ORDER BY lastUsed DESC")
    suspend fun getFavoriteClothingItems(): List<ClothingItem>
    
    @Query("SELECT * FROM clothing_items WHERE isGenerated = 1 ORDER BY createdAt DESC")
    suspend fun getGeneratedClothingItems(): List<ClothingItem>
    
    @Query("SELECT * FROM clothing_items WHERE isSpecialMode = 1")
    suspend fun getSpecialModeClothingItems(): List<ClothingItem>
    
    @Query("SELECT * FROM clothing_items WHERE category = :category ORDER BY lastUsed DESC")
    suspend fun getClothingItemsByCategory(category: ClothingCategory): List<ClothingItem>
    
    @Query("SELECT * FROM clothing_items WHERE style = :style ORDER BY lastUsed DESC")
    suspend fun getClothingItemsByStyle(style: ClothingStyle): List<ClothingItem>
    
    @Query("SELECT * FROM clothing_items WHERE color = :color ORDER BY lastUsed DESC")
    suspend fun getClothingItemsByColor(color: ClothingColor): List<ClothingItem>
    
    @Query("SELECT * FROM clothing_items WHERE pattern = :pattern ORDER BY lastUsed DESC")
    suspend fun getClothingItemsByPattern(pattern: ClothingPattern): List<ClothingItem>
    
    @Query("SELECT * FROM clothing_items WHERE season = :season ORDER BY lastUsed DESC")
    suspend fun getClothingItemsBySeason(season: ClothingSeason): List<ClothingItem>
    
    @Query("SELECT * FROM clothing_items WHERE formality = :formality ORDER BY lastUsed DESC")
    suspend fun getClothingItemsByFormality(formality: ClothingFormality): List<ClothingItem>
    
    @Query("SELECT * FROM clothing_items WHERE name LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%' ORDER BY lastUsed DESC")
    suspend fun searchClothingItems(searchQuery: String): List<ClothingItem>
    
    @Query("SELECT * FROM clothing_items WHERE usageCount > 0 ORDER BY usageCount DESC LIMIT :limit")
    suspend fun getMostUsedClothingItems(limit: Int = 10): List<ClothingItem>
    
    @Query("SELECT * FROM clothing_items WHERE lastUsed > :timestamp ORDER BY lastUsed DESC")
    suspend fun getRecentlyUsedClothingItems(timestamp: Long): List<ClothingItem>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClothingItem(clothingItem: ClothingItem)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClothingItems(clothingItems: List<ClothingItem>)
    
    @Update
    suspend fun updateClothingItem(clothingItem: ClothingItem)
    
    @Delete
    suspend fun deleteClothingItem(clothingItem: ClothingItem)
    
    @Query("DELETE FROM clothing_items WHERE id = :id")
    suspend fun deleteClothingItemById(id: String)
    
    @Query("DELETE FROM clothing_items WHERE isGenerated = 1")
    suspend fun deleteGeneratedClothingItems()
    
    @Query("DELETE FROM clothing_items WHERE isSpecialMode = 1")
    suspend fun deleteSpecialModeClothingItems()
    
    @Query("DELETE FROM clothing_items")
    suspend fun deleteAllClothingItems()
    
    @Query("UPDATE clothing_items SET usageCount = usageCount + 1, lastUsed = :timestamp, updatedAt = :timestamp WHERE id = :id")
    suspend fun incrementUsage(id: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE clothing_items SET isFavorite = :isFavorite, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE clothing_items SET imagePath = :imagePath, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateImagePath(id: String, imagePath: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("SELECT COUNT(*) FROM clothing_items")
    suspend fun getClothingItemCount(): Int
    
    @Query("SELECT COUNT(*) FROM clothing_items WHERE isFavorite = 1")
    suspend fun getFavoriteClothingItemCount(): Int
    
    @Query("SELECT COUNT(*) FROM clothing_items WHERE isGenerated = 1")
    suspend fun getGeneratedClothingItemCount(): Int
    
    @Query("SELECT COUNT(*) FROM clothing_items WHERE isSpecialMode = 1")
    suspend fun getSpecialModeClothingItemCount(): Int
    
    @Query("SELECT DISTINCT category FROM clothing_items")
    suspend fun getAvailableCategories(): List<ClothingCategory>
    
    @Query("SELECT DISTINCT style FROM clothing_items")
    suspend fun getAvailableStyles(): List<ClothingStyle>
    
    @Query("SELECT DISTINCT color FROM clothing_items")
    suspend fun getAvailableColors(): List<ClothingColor>
    
    @Query("SELECT DISTINCT pattern FROM clothing_items")
    suspend fun getAvailablePatterns(): List<ClothingPattern>
    
    @Query("SELECT DISTINCT season FROM clothing_items")
    suspend fun getAvailableSeasons(): List<ClothingSeason>
    
    @Query("SELECT DISTINCT formality FROM clothing_items")
    suspend fun getAvailableFormalities(): List<ClothingFormality>
}