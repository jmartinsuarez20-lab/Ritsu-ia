package com.ritsuai.database.dao

import androidx.room.*
import com.ritsuai.database.entities.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentConversations(limit: Int): List<ConversationEntity>
    
    @Query("SELECT * FROM conversations ORDER BY timestamp DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>
    
    @Insert
    suspend fun insertConversation(conversation: ConversationEntity)
    
    @Update
    suspend fun updateConversation(conversation: ConversationEntity)
    
    @Delete
    suspend fun deleteConversation(conversation: ConversationEntity)
    
    @Query("DELETE FROM conversations")
    suspend fun deleteAllConversations()
}