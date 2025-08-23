package com.ritsuai.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

@Entity(tableName = "system_logs")
data class SystemLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "log_level")
    val logLevel: String,
    
    @ColumnInfo(name = "message")
    val message: String,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Date,
    
    @ColumnInfo(name = "component")
    val component: String? = null,
    
    @ColumnInfo(name = "stack_trace")
    val stackTrace: String? = null
)