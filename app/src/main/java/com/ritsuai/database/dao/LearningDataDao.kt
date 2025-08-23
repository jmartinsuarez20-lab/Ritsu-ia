package com.ritsuai.database.dao

import androidx.room.*
import com.ritsuai.database.entities.LearningDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningDataDao {
    @Query("SELECT * FROM learning_data ORDER BY timestamp DESC")
    fun getAllLearningData(): Flow<List<LearningDataEntity>>
    
    @Query("SELECT * FROM learning_data WHERE learning_type = :type")
    fun getLearningDataByType(type: String): Flow<List<LearningDataEntity>>
    
    @Insert
    suspend fun insertLearningData(data: LearningDataEntity)
    
    @Update
    suspend fun updateLearningData(data: LearningDataEntity)
    
    @Delete
    suspend fun deleteLearningData(data: LearningDataEntity)
}