package com.adikob.galaxychat.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.adikob.galaxychat.datamodels.Conversation

@Dao
interface ConversationsDao {
    @Query("SELECT * FROM conversations ORDER BY last_message_creation_time DESC")
    fun getConversations(): PagingSource<Int, Conversation>

    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: String)

    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    fun getConversation(conversationId: String): Conversation?

    @Query("DELETE FROM conversations")
    suspend fun clearAllConversations()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: Conversation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllConversations(conversations: List<Conversation>)
}
