package com.ritsuai.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "user_message")
    val userMessage: String,
    
    @ColumnInfo(name = "ritsu_response")
    val ritsuResponse: String,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Date,
    
    @ColumnInfo(name = "context")
    val context: String? = null,
    
    @ColumnInfo(name = "action_taken")
    val actionTaken: String? = null,
    
    @ColumnInfo(name = "learning_data")
    val learningData: String? = null,
    
    @ColumnInfo(name = "mood")
    val mood: String = "neutral",
    
    @ColumnInfo(name = "voice_used")
    val voiceUsed: Boolean = false,
    
    @ColumnInfo(name = "avatar_expression")
    val avatarExpression: String = "neutral"
)