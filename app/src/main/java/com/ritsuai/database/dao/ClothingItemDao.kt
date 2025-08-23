package com.ritsuai.database.dao

import androidx.room.*
import com.ritsuai.database.entities.ClothingItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClothingItemDao {
    @Query("SELECT * FROM clothing_items ORDER BY createdAt DESC")
    fun getAllClothingItems(): Flow<List<ClothingItemEntity>>
    
    @Query("SELECT * FROM clothing_items WHERE category = :category")
    fun getClothingByCategory(category: String): Flow<List<ClothingItemEntity>>
    
    @Insert
    suspend fun insertClothingItem(item: ClothingItemEntity)
    
    @Update
    suspend fun updateClothingItem(item: ClothingItemEntity)
    
    @Delete
    suspend fun deleteClothingItem(item: ClothingItemEntity)
}