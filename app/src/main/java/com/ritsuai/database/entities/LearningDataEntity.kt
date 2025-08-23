package com.ritsuai.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

@Entity(tableName = "learning_data")
data class LearningDataEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "user_input")
    val userInput: String,
    
    @ColumnInfo(name = "ai_response")
    val aiResponse: String,
    
    @ColumnInfo(name = "context")
    val context: String? = null,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Date,
    
    @ColumnInfo(name = "learning_type")
    val learningType: String,
    
    @ColumnInfo(name = "confidence")
    val confidence: Float
)