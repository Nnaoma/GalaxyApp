package com.adikob.galaxychat.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.adikob.galaxychat.database.converters.UserModelTypeConverter
import com.adikob.galaxychat.database.dao.ChatDao
import com.adikob.galaxychat.database.dao.ConversationsDao
import com.adikob.galaxychat.datamodels.Conversation
import com.adikob.galaxychat.datamodels.Message

@Database(entities = [Message::class, Conversation::class], version = 1, exportSchema = false)
@TypeConverters(UserModelTypeConverter::class)
abstract class GalaxyChatDatabase: RoomDatabase() {
    abstract fun getChatDao(): ChatDao
    abstract fun getConversationsDao(): ConversationsDao
}