package com.adikob.galaxychat.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.adikob.galaxychat.database.GalaxyChatDatabase
import com.adikob.galaxychat.database.dao.ChatDao
import com.adikob.galaxychat.database.dao.ConversationsDao
import com.adikob.galaxychat.datamodels.Conversation
import com.adikob.galaxychat.datamodels.Message
import com.adikob.galaxychat.network.ChatService
import javax.inject.Inject

class PagingConfigAbstraction @Inject constructor(
    private val database: GalaxyChatDatabase,
    private val chatDao: ChatDao,
    private val conversationsDao: ConversationsDao,
    private val chatService: ChatService
) {
    @OptIn(ExperimentalPagingApi::class)
    fun createChatRoomPager(
        conversationId: String
    ): Pager<Int, Message> = Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = false
        ),
        remoteMediator = GalaxyChatMessagesPagingMediator(
            database = database,
            chatDao = chatDao,
            chatService = chatService,
            conversationId = conversationId
        ),
        pagingSourceFactory = { chatDao.getMessages(conversationId) }
    )

    @OptIn(ExperimentalPagingApi::class)
    fun createConversationsListPager(): Pager<Int, Conversation> = Pager(
        config = PagingConfig(
            pageSize = 30,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { conversationsDao.getConversations() }
    )
}