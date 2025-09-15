package com.adikob.galaxychat.repository

import androidx.paging.PagingData
import androidx.room.withTransaction
import com.adikob.galaxychat.BuildConfig
import com.adikob.galaxychat.database.GalaxyChatDatabase
import com.adikob.galaxychat.database.dao.ChatDao
import com.adikob.galaxychat.database.dao.ConversationsDao
import com.adikob.galaxychat.datamodels.Conversation
import com.adikob.galaxychat.datamodels.ConversationMetadata
import com.adikob.galaxychat.datamodels.ConversationMetadataModificationType
import com.adikob.galaxychat.datamodels.Message
import com.adikob.galaxychat.datamodels.MessageStatus
import com.adikob.galaxychat.datamodels.User
import com.adikob.galaxychat.network.ChatService
import com.adikob.galaxychat.network.NetworkTaskState
import com.adikob.galaxychat.paging.PagingConfigAbstraction
import com.adikob.galaxychat.utility.FirebaseHelper
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface ChatDataRepository {
    fun getMessages(conversationId: String): Flow<PagingData<Message>>
    fun getConversations(): Flow<PagingData<Conversation>>
    suspend fun sendMessage(message: Message, conversation: Conversation): NetworkTaskState
    suspend fun fetchUsers(): List<User>
    fun attachChatRoomListener(conversationId: String)
    fun attachConversationsListener(userId: String)
    fun detachChatRoomListener()
    fun detachConversationsListener()
    suspend fun signOut()
}

class ChatDataRepositoryImpl @Inject constructor(
    private val chatService: ChatService,
    private val db: GalaxyChatDatabase,
    private val chatDao: ChatDao,
    private val conversationsDao: ConversationsDao,
    private val pagerAbstraction: PagingConfigAbstraction,
    private val firebaseHelper: FirebaseHelper
): ChatDataRepository {
    var liveMessagesChildListener: ChildEventListener? = null
    var liveMessageUpdatesListener: ValueEventListener? = null
    var liveMessagesQuery: Query? = null
    var liveMessagesUpdateQuery: Query? = null

    var liveConversationsUpdateListener: ChildEventListener? = null
    var liveConversationsQuery: Query? = null
    var conversationsEventChannel: Channel<Conversation>? = null

    var coroutineScope: CoroutineScope? = null

    override fun getMessages(
        conversationId: String
    ): Flow<PagingData<Message>> = pagerAbstraction.createChatRoomPager(conversationId).flow

    override fun getConversations(): Flow<PagingData<Conversation>> = pagerAbstraction
        .createConversationsListPager().flow

    override suspend fun sendMessage(
        message: Message,
        conversation: Conversation
    ): NetworkTaskState = withContext(Dispatchers.IO) {
        try {
            chatDao.insertMessage(message)
            conversationsDao.insertConversation(conversation)
            chatService.sendMessage(message, conversation)

            NetworkTaskState.SUCCESS
        } catch (exception: Exception) {
            if (BuildConfig.DEBUG) exception.printStackTrace()
            chatDao.insertMessage(message.copy(status = MessageStatus.FAILED))
            conversationsDao.insertConversation(conversation.copy(lastMessageStatus = MessageStatus.FAILED))

            NetworkTaskState.ERROR
        }
    }

    override fun attachChatRoomListener(conversationId: String) {
        if (liveMessagesChildListener != null || liveMessageUpdatesListener != null)
            detachChatRoomListener()

        if (coroutineScope == null) coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        liveMessagesQuery = firebaseHelper.getRootFirebaseDBReference()
            .child("messages-list")
            .child(conversationId)
            .child("messages")
            .orderByChild("timestamp")
            .limitToLast(3)
        liveMessagesUpdateQuery = firebaseHelper.getRootFirebaseDBReference()
            .child("messages-list")
            .child(conversationId)
            .child("metadata")

        liveMessagesChildListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                snapshot.getValue(Message::class.java)?.let {
                    coroutineScope?.launch { chatDao.insertMessage(it) }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}
        }

        liveMessagesQuery?.addChildEventListener(liveMessagesChildListener!!)

        liveMessageUpdatesListener = liveMessagesUpdateQuery?.addValueEventListener(
            object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(ConversationMetadata::class.java)?.let { metadata ->
                        when(metadata.modificationType) {
                            ConversationMetadataModificationType.MESSAGE_DELETED -> {
                                coroutineScope?.launch { chatDao.deleteMessage(metadata.messageId) }
                            }
                            ConversationMetadataModificationType.MESSAGE_UPDATED -> {
                                coroutineScope?.launch {
                                    runCatching {
                                        val message = chatService
                                            .getChat(conversationId, metadata.messageId)

                                        if (message != null) { chatDao.insertMessage(message) }
                                    }
                                }
                            }
                            ConversationMetadataModificationType.NEW_MESSAGE_ADDED -> {}
                            ConversationMetadataModificationType.IDLE_MODIFICATION -> {}
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            }
        )
    }

    @OptIn(FlowPreview::class)
    override fun attachConversationsListener(userId: String) {
        if (liveConversationsUpdateListener != null) detachConversationsListener()

        if (coroutineScope == null) coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        if (conversationsEventChannel == null) conversationsEventChannel = Channel(capacity = Channel.UNLIMITED)

        liveConversationsQuery = firebaseHelper.getRootFirebaseDBReference()
            .child("conversations")
            .child(userId)
            .orderByChild("last_message_creation_time")

        liveConversationsUpdateListener = liveConversationsQuery?.addChildEventListener(
            object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    snapshot.getValue(Conversation::class.java)?.let {
                        conversationsEventChannel?.trySend(it)
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    snapshot.getValue(Conversation::class.java)?.let {
                        coroutineScope?.launch {
                            db.withTransaction { conversationsDao.insertConversation(it) }
                        }
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    snapshot.getValue(Conversation::class.java)?.let {
                        coroutineScope?.launch {
                            db.withTransaction { conversationsDao.deleteConversation(it.id) }
                        }
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {}
            }
        )

        coroutineScope?.launch {
            val tempConversationsList: MutableList<Conversation> = mutableListOf()

            conversationsEventChannel
                ?.receiveAsFlow()
                ?.buffer()
                ?.onEach { conversation -> tempConversationsList.add(conversation) }
                // ?.debounce(200) // wait for 200ms gap from upstream before emitting
                ?.sample(700)
                ?.collect {
                    val insertable = ArrayList(tempConversationsList)
                    tempConversationsList.clear()
                    db.withTransaction { conversationsDao.insertAllConversations(insertable) }
                }

        }
    }

    override fun detachChatRoomListener() {
        coroutineScope?.cancel()
        coroutineScope = null

        if (liveMessagesChildListener != null) {
            liveMessagesQuery?.removeEventListener(liveMessagesChildListener!!)
            liveMessagesChildListener = null
            liveMessagesQuery = null
        }

        if (liveMessageUpdatesListener != null) {
            liveMessagesUpdateQuery?.removeEventListener(liveMessageUpdatesListener!!)
            liveMessageUpdatesListener = null
            liveMessagesUpdateQuery = null
        }
    }

    override fun detachConversationsListener() {
        conversationsEventChannel?.close()
        coroutineScope?.cancel()
        conversationsEventChannel = null
        coroutineScope = null

        if (liveConversationsUpdateListener != null) {
            liveConversationsQuery?.removeEventListener(liveConversationsUpdateListener!!)
            liveConversationsQuery = null
            liveConversationsUpdateListener = null
        }
    }

    override suspend fun fetchUsers(): List<User> = withContext(Dispatchers.IO) {
        val snapShot = firebaseHelper.getRootFirebaseDBReference()
            .child("users")
            .orderByChild("name")
            .get()
            .await()
        snapShot.children.mapNotNull { it.getValue(User::class.java)!! }
    }

    override suspend fun signOut() = withContext(Dispatchers.IO) {
        chatDao.clearAllMessages()
        conversationsDao.clearAllConversations()
    }
}