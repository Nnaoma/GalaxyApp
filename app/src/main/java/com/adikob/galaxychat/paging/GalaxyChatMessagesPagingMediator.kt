package com.adikob.galaxychat.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.adikob.galaxychat.database.GalaxyChatDatabase
import com.adikob.galaxychat.database.dao.ChatDao
import com.adikob.galaxychat.datamodels.Message
import com.adikob.galaxychat.network.ChatService

@OptIn(ExperimentalPagingApi::class)
class GalaxyChatMessagesPagingMediator(
    private val database: GalaxyChatDatabase,
    private val chatDao: ChatDao,
    private val chatService: ChatService,
    private val conversationId: String
): RemoteMediator<Int, Message>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Message>
    ): MediatorResult {
        return try {
            when(loadType) {
                LoadType.REFRESH -> {
                    val messages = chatService.getChats(conversationId)

                    if (messages.isNotEmpty()) {
                        database.withTransaction {
                            chatDao.deleteMessages(conversationId)
                            chatDao.insertAllMessages(messages)
                        }
                        MediatorResult.Success(endOfPaginationReached = false)
                    } else { MediatorResult.Success(endOfPaginationReached = true) }
                }
                LoadType.APPEND -> {
                    val oldestTimeStamp = state.lastItemOrNull()?.timestamp ?: return MediatorResult
                        .Success(endOfPaginationReached = true)

                    val messages = chatService.getChats(
                        conversationId = conversationId,
                        lastTimeStamp = oldestTimeStamp
                    )

                    if (messages.isNotEmpty()) {
                        chatDao.insertAllMessages(messages)
                        MediatorResult.Success(endOfPaginationReached = false)
                    } else { MediatorResult.Success(endOfPaginationReached = true) }
                }

                LoadType.PREPEND -> { MediatorResult.Success(endOfPaginationReached = true) }
            }
        } catch (e: Exception) { MediatorResult.Error(e) }
    }
}