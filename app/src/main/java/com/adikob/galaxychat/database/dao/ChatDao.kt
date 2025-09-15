package com.adikob.galaxychat.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.adikob.galaxychat.datamodels.Message

@Dao
interface ChatDao {
    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY timestamp DESC")
    fun getMessages(conversationId: String): PagingSource<Int, Message>

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("DELETE FROM messages WHERE conversation_id = :conversationId")
    suspend fun deleteMessages(conversationId: String)

    @Query("DELETE FROM messages")
    suspend fun clearAllMessages()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMessages(messages: List<Message>)
}
