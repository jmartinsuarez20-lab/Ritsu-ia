package com.ritsu.ai.data.dao

import androidx.room.*
import com.ritsu.ai.data.model.UserPreference
import com.ritsu.ai.data.model.PreferenceCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPreferenceDao {
    
    @Query("SELECT * FROM user_preferences WHERE id = :id")
    suspend fun getUserPreference(id: String): UserPreference?
    
    @Query("SELECT * FROM user_preferences WHERE id = :id")
    fun getUserPreferenceFlow(id: String): Flow<UserPreference?>
    
    @Query("SELECT * FROM user_preferences ORDER BY lastUsed DESC")
    suspend fun getAllUserPreferences(): List<UserPreference>
    
    @Query("SELECT * FROM user_preferences ORDER BY lastUsed DESC")
    fun getAllUserPreferencesFlow(): Flow<List<UserPreference>>
    
    @Query("SELECT * FROM user_preferences WHERE category = :category ORDER BY lastUsed DESC")
    suspend fun getUserPreferencesByCategory(category: PreferenceCategory): List<UserPreference>
    
    @Query("SELECT * FROM user_preferences WHERE `key` LIKE '%' || :searchQuery || '%' ORDER BY lastUsed DESC")
    suspend fun searchUserPreferences(searchQuery: String): List<UserPreference>
    
    @Query("SELECT * FROM user_preferences WHERE `key` = :key ORDER BY lastUsed DESC")
    suspend fun getUserPreferencesByKey(key: String): List<UserPreference>
    
    @Query("SELECT * FROM user_preferences WHERE usageCount >= 3 AND confidence >= 0.8 ORDER BY usageCount DESC")
    suspend fun getReliableUserPreferences(): List<UserPreference>
    
    @Query("SELECT * FROM user_preferences WHERE lastUsed > :timestamp ORDER BY lastUsed DESC")
    suspend fun getRecentlyUsedUserPreferences(timestamp: Long): List<UserPreference>
    
    @Query("SELECT * FROM user_preferences WHERE usageCount > 0 ORDER BY usageCount DESC LIMIT :limit")
    suspend fun getMostUsedUserPreferences(limit: Int = 10): List<UserPreference>
    
    @Query("SELECT * FROM user_preferences WHERE confidence > :minConfidence ORDER BY confidence DESC")
    suspend fun getHighConfidenceUserPreferences(minConfidence: Float = 0.8f): List<UserPreference>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreference(userPreference: UserPreference)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(userPreferences: List<UserPreference>)
    
    @Update
    suspend fun updateUserPreference(userPreference: UserPreference)
    
    @Delete
    suspend fun deleteUserPreference(userPreference: UserPreference)
    
    @Query("DELETE FROM user_preferences WHERE id = :id")
    suspend fun deleteUserPreferenceById(id: String)
    
    @Query("DELETE FROM user_preferences WHERE category = :category")
    suspend fun deleteUserPreferencesByCategory(category: PreferenceCategory)
    
    @Query("DELETE FROM user_preferences WHERE `key` = :key")
    suspend fun deleteUserPreferencesByKey(key: String)
    
    @Query("DELETE FROM user_preferences")
    suspend fun deleteAllUserPreferences()
    
    @Query("UPDATE user_preferences SET usageCount = usageCount + 1, lastUsed = :timestamp, updatedAt = :timestamp WHERE id = :id")
    suspend fun incrementUsage(id: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET value = :value, lastUsed = :timestamp, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateValue(id: String, value: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET confidence = :confidence, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateConfidence(id: String, confidence: Float, timestamp: Long = System.currentTimeMillis())
    
    @Query("SELECT COUNT(*) FROM user_preferences")
    suspend fun getUserPreferenceCount(): Int
    
    @Query("SELECT COUNT(*) FROM user_preferences WHERE category = :category")
    suspend fun getUserPreferenceCountByCategory(category: PreferenceCategory): Int
    
    @Query("SELECT COUNT(*) FROM user_preferences WHERE usageCount >= 3 AND confidence >= 0.8")
    suspend fun getReliableUserPreferenceCount(): Int
    
    @Query("SELECT AVG(confidence) FROM user_preferences")
    suspend fun getAverageConfidence(): Float?
    
    @Query("SELECT AVG(usageCount) FROM user_preferences")
    suspend fun getAverageUsageCount(): Float?
    
    @Query("SELECT DISTINCT category FROM user_preferences")
    suspend fun getAvailableCategories(): List<PreferenceCategory>
    
    @Query("SELECT DISTINCT `key` FROM user_preferences")
    suspend fun getAvailableKeys(): List<String>
    
    @Query("SELECT DISTINCT value FROM user_preferences")
    suspend fun getAvailableValues(): List<String>
}