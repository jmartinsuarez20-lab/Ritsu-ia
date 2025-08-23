package com.ritsuai.database.dao

import androidx.room.*
import com.ritsuai.database.entities.UserPreferenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPreferenceDao {
    @Query("SELECT * FROM user_preferences WHERE `key` = :key")
    suspend fun getPreference(key: String): UserPreferenceEntity?
    
    @Query("SELECT * FROM user_preferences")
    fun getAllPreferences(): Flow<List<UserPreferenceEntity>>
    
    @Insert
    suspend fun insertPreference(preference: UserPreferenceEntity)
    
    @Update
    suspend fun updatePreference(preference: UserPreferenceEntity)
    
    @Delete
    suspend fun deletePreference(preference: UserPreferenceEntity)
}