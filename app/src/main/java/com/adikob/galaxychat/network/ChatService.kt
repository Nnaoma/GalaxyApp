package com.adikob.galaxychat.network

import com.adikob.galaxychat.datamodels.Conversation
import com.adikob.galaxychat.datamodels.Message
import com.adikob.galaxychat.utility.FirebaseHelper
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface ChatService {
    suspend fun getChats(
        conversationId: String,
        limit: Int = 50,
        lastTimeStamp: Long? = null
    ): List<Message>
    suspend fun getChat(conversationId: String, messageId: String): Message?
    suspend fun sendMessage(message: Message, conversation: Conversation)
}

class ChatServiceImpl @Inject constructor(
    private val firebaseHelper: FirebaseHelper
): ChatService {
    override suspend fun getChats(
        conversationId: String,
        limit: Int,
        lastTimeStamp: Long?
    ): List<Message> {
        var query = firebaseHelper.getRootFirebaseDBReference()
            .child("messages-list")
            .child(conversationId)
            .child("messages")
            .orderByChild("timestamp")
        if (lastTimeStamp != null) { query = query.endAt(lastTimeStamp.toDouble()) }

        val snapShot = query.limitToLast(limit).get().await()
        return snapShot.children.mapNotNull { it.getValue(Message::class.java)!! }
    }

    override suspend fun getChat(
        conversationId: String,
        messageId: String
    ): Message? {
        val snapShot = firebaseHelper.getRootFirebaseDBReference()
            .child("messages-list")
            .child(conversationId)
            .child("messages")
            .child(messageId)
            .get()
            .await()
        return snapShot.getValue(Message::class.java)
    }

    override suspend fun sendMessage(message: Message, conversation: Conversation) {
        val currentUser = firebaseHelper.getUserId().toString()
        val secondUser = conversation.participants.firstOrNull { it.id != currentUser }

        firebaseHelper.getRootFirebaseDBReference()
            .child("conversations")
            .child(currentUser)
            .child(conversation.id)
            .setValue(conversation.toRealtimeDBMap()).await()

        firebaseHelper.getRootFirebaseDBReference()
            .child("conversations")
            .child(secondUser?.id.toString())
            .child(conversation.id)
            .setValue(conversation.toRealtimeDBMap()).await()

        firebaseHelper.getRootFirebaseDBReference()
            .child("messages-list")
            .child(message.conversationId)
            .child("messages")
            .child(message.id)
            .setValue(message.toRealtimeDBMap()).await()
    }
}
