package com.adikob.galaxychat.modules

import android.content.Context
import androidx.room.Room
import com.adikob.galaxychat.database.GalaxyChatDatabase
import com.adikob.galaxychat.database.dao.ChatDao
import com.adikob.galaxychat.database.dao.ConversationsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DbModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): GalaxyChatDatabase = Room
        .databaseBuilder(context, GalaxyChatDatabase::class.java, "GalaxyChatDatabase")
        .fallbackToDestructiveMigration(true)
        .build()

    @Singleton
    @Provides
    fun provideChatDao(db: GalaxyChatDatabase): ChatDao = db.getChatDao()

    @Singleton
    @Provides
    fun provideConversationsDao(db: GalaxyChatDatabase): ConversationsDao = db.getConversationsDao()
}