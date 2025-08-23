package com.ritsu.ai.data.dao

import androidx.room.*
import com.ritsu.ai.data.model.AvatarConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface AvatarConfigDao {
    
    @Query("SELECT * FROM avatar_config WHERE id = :id")
    suspend fun getAvatarConfig(id: String): AvatarConfig?
    
    @Query("SELECT * FROM avatar_config WHERE id = :id")
    fun getAvatarConfigFlow(id: String): Flow<AvatarConfig?>
    
    @Query("SELECT * FROM avatar_config")
    suspend fun getAllAvatarConfigs(): List<AvatarConfig>
    
    @Query("SELECT * FROM avatar_config")
    fun getAllAvatarConfigsFlow(): Flow<List<AvatarConfig>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAvatarConfig(avatarConfig: AvatarConfig)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAvatarConfigs(avatarConfigs: List<AvatarConfig>)
    
    @Update
    suspend fun updateAvatarConfig(avatarConfig: AvatarConfig)
    
    @Delete
    suspend fun deleteAvatarConfig(avatarConfig: AvatarConfig)
    
    @Query("DELETE FROM avatar_config WHERE id = :id")
    suspend fun deleteAvatarConfigById(id: String)
    
    @Query("DELETE FROM avatar_config")
    suspend fun deleteAllAvatarConfigs()
    
    @Query("UPDATE avatar_config SET positionX = :x, positionY = :y, updatedAt = :timestamp WHERE id = :id")
    suspend fun updatePosition(id: String, x: Float, y: Float, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE avatar_config SET size = :size, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateSize(id: String, size: Int, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE avatar_config SET expression = :expression, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateExpression(id: String, expression: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE avatar_config SET currentClothingId = :clothingId, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateClothing(id: String, clothingId: String?, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE avatar_config SET isSpecialModeEnabled = :enabled, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateSpecialMode(id: String, enabled: Boolean, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE avatar_config SET isVisible = :visible, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateVisibility(id: String, visible: Boolean, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE avatar_config SET opacity = :opacity, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateOpacity(id: String, opacity: Float, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE avatar_config SET scale = :scale, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateScale(id: String, scale: Float, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE avatar_config SET animationSpeed = :speed, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateAnimationSpeed(id: String, speed: Float, timestamp: Long = System.currentTimeMillis())
    
    @Query("SELECT COUNT(*) FROM avatar_config")
    suspend fun getAvatarConfigCount(): Int
    
    @Query("SELECT * FROM avatar_config WHERE isSpecialModeEnabled = 1")
    suspend fun getSpecialModeConfigs(): List<AvatarConfig>
    
    @Query("SELECT * FROM avatar_config WHERE isVisible = 1")
    suspend fun getVisibleConfigs(): List<AvatarConfig>
}