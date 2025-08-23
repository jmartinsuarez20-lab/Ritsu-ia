package com.ritsuai.database.dao

import androidx.room.*
import com.ritsuai.database.entities.PhoneActionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhoneActionDao {
    @Query("SELECT * FROM phone_actions ORDER BY timestamp DESC")
    fun getAllActions(): Flow<List<PhoneActionEntity>>
    
    @Query("SELECT * FROM phone_actions WHERE action_type = :actionType")
    fun getActionsByType(actionType: String): Flow<List<PhoneActionEntity>>
    
    @Insert
    suspend fun insertAction(action: PhoneActionEntity)
    
    @Update
    suspend fun updateAction(action: PhoneActionEntity)
    
    @Delete
    suspend fun deleteAction(action: PhoneActionEntity)
}