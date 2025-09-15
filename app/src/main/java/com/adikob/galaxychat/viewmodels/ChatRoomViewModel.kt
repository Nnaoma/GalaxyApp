package com.adikob.galaxychat.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.adikob.galaxychat.database.dao.ConversationsDao
import com.adikob.galaxychat.datamodels.Conversation
import com.adikob.galaxychat.datamodels.Message
import com.adikob.galaxychat.datamodels.MessageStatus
import com.adikob.galaxychat.datamodels.User
import com.adikob.galaxychat.repository.ChatDataRepository
import com.adikob.galaxychat.utility.FirebaseHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

@HiltViewModel(assistedFactory = ChatRoomViewModel.Factory::class)
class ChatRoomViewModel @AssistedInject constructor(
    @Assisted private val conversationId: String,
    private val chatDataRepository: ChatDataRepository,
    private val firebaseHelper: FirebaseHelper,
    private val conversationsDao: ConversationsDao
): ViewModel() {
    @AssistedFactory interface Factory {
        fun create(conversationId: String): ChatRoomViewModel
    }

    val chatMessages: Flow<PagingData<Message>> = chatDataRepository
        .getMessages(conversationId)
        .cachedIn(viewModelScope)

    override fun onCleared() {
        chatDataRepository.detachChatRoomListener()
        super.onCleared()
    }

    fun getCurrentUserUID() = firebaseHelper.getUserId()

    fun getCurrentUserName() = firebaseHelper.getCurrentUser()?.displayName

    fun sendMessage(
        message: String,
        userName: String,
        userPhotoUrl: String,
        userId: String,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val message = Message(
                id = UUID.randomUUID().toString(),
                body = message,
                conversationId = conversationId,
                senderId = firebaseHelper.getUserId().toString(),
                timestamp = System.currentTimeMillis(),
                status = MessageStatus.SENDING,
                type = "TEXT",
                quotedMessageId = null,
                quotedMessageBody = null,
                quotedMessageSenderId = null,
                quotedMessageType = null,
                reaction = null
            )

            val roomConversation = conversationsDao.getConversation(conversationId)
            val conversation = roomConversation?.copy(
                lastMessage = message.body,
                lastMessageStatus = message.status,
                isLastMessageRead = false,
                lastMessageType = message.type
            ) ?: Conversation(
                    id = conversationId,
                    participants = listOf(
                        User(name = userName, email = "", id = userId, photoUrl = userPhotoUrl),
                        User(name = getCurrentUserName().toString(),
                            email = "", id = getCurrentUserUID().toString(),
                            photoUrl = firebaseHelper.getCurrentUser()?.photoUrl.toString()
                        )
                    ),
                    unreadMessageCount = 0,
                    lastMessage = message.body,
                    lastMessageStatus = message.status,
                    isLastMessageRead = false,
                    lastMessageType = message.type,
                    lastMessageCreationTime = message.timestamp,
                    createdAt = System.currentTimeMillis()
                )

            chatDataRepository.sendMessage(message, conversation)
        }
    }

    init { chatDataRepository.attachChatRoomListener(conversationId) }
}