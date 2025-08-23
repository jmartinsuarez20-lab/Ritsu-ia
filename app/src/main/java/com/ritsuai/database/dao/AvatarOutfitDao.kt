package com.ritsuai.database.dao

import androidx.room.*
import com.ritsuai.database.entities.AvatarOutfitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AvatarOutfitDao {
    @Query("SELECT * FROM avatar_outfits ORDER BY createdAt DESC")
    fun getAllOutfits(): Flow<List<AvatarOutfitEntity>>
    
    @Query("SELECT * FROM avatar_outfits WHERE category = :category")
    fun getOutfitsByCategory(category: String): Flow<List<AvatarOutfitEntity>>
    
    @Query("SELECT * FROM avatar_outfits WHERE is_favorite = 1")
    fun getFavoriteOutfits(): Flow<List<AvatarOutfitEntity>>
    
    @Insert
    suspend fun insertOutfit(outfit: AvatarOutfitEntity)
    
    @Update
    suspend fun updateOutfit(outfit: AvatarOutfitEntity)
    
    @Delete
    suspend fun deleteOutfit(outfit: AvatarOutfitEntity)
}