package com.ritsu.ai.data.dao

import androidx.room.*
import com.ritsu.ai.data.model.LearningPattern
import com.ritsu.ai.data.model.PatternType
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningPatternDao {
    
    @Query("SELECT * FROM learning_patterns WHERE id = :id")
    suspend fun getLearningPattern(id: String): LearningPattern?
    
    @Query("SELECT * FROM learning_patterns WHERE id = :id")
    fun getLearningPatternFlow(id: String): Flow<LearningPattern?>
    
    @Query("SELECT * FROM learning_patterns ORDER BY lastUsed DESC")
    suspend fun getAllLearningPatterns(): List<LearningPattern>
    
    @Query("SELECT * FROM learning_patterns ORDER BY lastUsed DESC")
    fun getAllLearningPatternsFlow(): Flow<List<LearningPattern>>
    
    @Query("SELECT * FROM learning_patterns WHERE patternType = :patternType ORDER BY lastUsed DESC")
    suspend fun getLearningPatternsByType(patternType: PatternType): List<LearningPattern>
    
    @Query("SELECT * FROM learning_patterns WHERE input LIKE '%' || :searchQuery || '%' ORDER BY lastUsed DESC")
    suspend fun searchLearningPatterns(searchQuery: String): List<LearningPattern>
    
    @Query("SELECT * FROM learning_patterns WHERE context = :context ORDER BY lastUsed DESC")
    suspend fun getLearningPatternsByContext(context: String): List<LearningPattern>
    
    @Query("SELECT * FROM learning_patterns WHERE usageCount >= 3 AND successRate >= 0.7 AND confidence >= 0.8 ORDER BY usageCount DESC")
    suspend fun getReliableLearningPatterns(): List<LearningPattern>
    
    @Query("SELECT * FROM learning_patterns WHERE lastUsed > :timestamp ORDER BY lastUsed DESC")
    suspend fun getRecentlyUsedLearningPatterns(timestamp: Long): List<LearningPattern>
    
    @Query("SELECT * FROM learning_patterns WHERE usageCount > 0 ORDER BY usageCount DESC LIMIT :limit")
    suspend fun getMostUsedLearningPatterns(limit: Int = 10): List<LearningPattern>
    
    @Query("SELECT * FROM learning_patterns WHERE confidence > :minConfidence ORDER BY confidence DESC")
    suspend fun getHighConfidenceLearningPatterns(minConfidence: Float = 0.8f): List<LearningPattern>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearningPattern(learningPattern: LearningPattern)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearningPatterns(learningPatterns: List<LearningPattern>)
    
    @Update
    suspend fun updateLearningPattern(learningPattern: LearningPattern)
    
    @Delete
    suspend fun deleteLearningPattern(learningPattern: LearningPattern)
    
    @Query("DELETE FROM learning_patterns WHERE id = :id")
    suspend fun deleteLearningPatternById(id: String)
    
    @Query("DELETE FROM learning_patterns WHERE patternType = :patternType")
    suspend fun deleteLearningPatternsByType(patternType: PatternType)
    
    @Query("DELETE FROM learning_patterns WHERE context = :context")
    suspend fun deleteLearningPatternsByContext(context: String)
    
    @Query("DELETE FROM learning_patterns")
    suspend fun deleteAllLearningPatterns()
    
    @Query("UPDATE learning_patterns SET usageCount = usageCount + 1, lastUsed = :timestamp, updatedAt = :timestamp WHERE id = :id")
    suspend fun incrementUsage(id: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE learning_patterns SET usageCount = usageCount + 1, successRate = CASE WHEN :success THEN ((successRate * usageCount) + 1) / (usageCount + 1) ELSE (successRate * usageCount) / (usageCount + 1) END, lastUsed = :timestamp, updatedAt = :timestamp WHERE id = :id")
    suspend fun incrementUsageWithSuccess(id: String, success: Boolean, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE learning_patterns SET confidence = :confidence, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateConfidence(id: String, confidence: Float, timestamp: Long = System.currentTimeMillis())
    
    @Query("SELECT COUNT(*) FROM learning_patterns")
    suspend fun getLearningPatternCount(): Int
    
    @Query("SELECT COUNT(*) FROM learning_patterns WHERE patternType = :patternType")
    suspend fun getLearningPatternCountByType(patternType: PatternType): Int
    
    @Query("SELECT COUNT(*) FROM learning_patterns WHERE usageCount >= 3 AND successRate >= 0.7 AND confidence >= 0.8")
    suspend fun getReliableLearningPatternCount(): Int
    
    @Query("SELECT AVG(confidence) FROM learning_patterns")
    suspend fun getAverageConfidence(): Float?
    
    @Query("SELECT AVG(successRate) FROM learning_patterns")
    suspend fun getAverageSuccessRate(): Float?
    
    @Query("SELECT AVG(usageCount) FROM learning_patterns")
    suspend fun getAverageUsageCount(): Float?
    
    @Query("SELECT DISTINCT patternType FROM learning_patterns")
    suspend fun getAvailablePatternTypes(): List<PatternType>
    
    @Query("SELECT DISTINCT context FROM learning_patterns WHERE context IS NOT NULL")
    suspend fun getAvailableContexts(): List<String>
}