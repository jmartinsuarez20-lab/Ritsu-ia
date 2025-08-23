package com.ritsuai.database.dao

import androidx.room.*
import com.ritsuai.database.entities.SystemLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SystemLogDao {
    @Query("SELECT * FROM system_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<SystemLogEntity>>
    
    @Query("SELECT * FROM system_logs WHERE log_level = :level")
    fun getLogsByLevel(level: String): Flow<List<SystemLogEntity>>
    
    @Insert
    suspend fun insertLog(log: SystemLogEntity)
    
    @Update
    suspend fun updateLog(log: SystemLogEntity)
    
    @Delete
    suspend fun deleteLog(log: SystemLogEntity)
    
    @Query("DELETE FROM system_logs WHERE timestamp < :timestamp")
    suspend fun deleteOldLogs(timestamp: Long)
}