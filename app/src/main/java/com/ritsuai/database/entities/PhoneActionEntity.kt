package com.ritsuai.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

@Entity(tableName = "phone_actions")
data class PhoneActionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "action_type")
    val actionType: String,
    
    @ColumnInfo(name = "target_app")
    val targetApp: String? = null,
    
    @ColumnInfo(name = "parameters")
    val parameters: String? = null,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Date,
    
    @ColumnInfo(name = "success")
    val success: Boolean,
    
    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null
)