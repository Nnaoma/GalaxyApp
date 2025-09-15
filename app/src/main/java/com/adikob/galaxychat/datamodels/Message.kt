package com.adikob.galaxychat.datamodels

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.Exclude
import com.google.firebase.database.PropertyName
import com.google.firebase.database.ServerValue
import java.util.Date

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey
    val id: String,
    val body: String,
    val timestamp: Long,
    val status: String,
    val type: String,
    val reaction: String?,
    @ColumnInfo("sender_id")
    @get:PropertyName("sender_id")
    @set:PropertyName("sender_id")
    var senderId: String,
    @ColumnInfo("conversation_id")
    @get:PropertyName("conversation_id")
    @set:PropertyName("conversation_id")
    var conversationId: String,
    @ColumnInfo("quoted_message_id")
    @get:PropertyName("quoted_message_id")
    @set:PropertyName("quoted_message_id")
    var quotedMessageId: String?,
    @ColumnInfo("quoted_message_body")
    @get:PropertyName("quoted_message_body")
    @set:PropertyName("quoted_message_body")
    var quotedMessageBody: String?,
    @ColumnInfo("quoted_message_sender_id")
    @get:PropertyName("quoted_message_sender_id")
    @set:PropertyName("quoted_message_sender_id")
    var quotedMessageSenderId: String?,
    @ColumnInfo("quoted_message_type")
    @get:PropertyName("quoted_message_type")
    @set:PropertyName("quoted_message_type")
    var quotedMessageType: String?
) {
    constructor() : this("", "", 0, "", "", "", "", "", "", "", "", "")

    @get:Exclude
    val createdAt: Date
        get() = Date(timestamp)

    fun toRealtimeDBMap(): Map<String, Any?> = mapOf(
            "id" to id,
            "body" to body,
            "timestamp" to ServerValue.TIMESTAMP,
            "status" to MessageStatus.SENT,
            "type" to type,
            "reaction" to reaction,
            "sender_id" to senderId,
            "conversation_id" to conversationId,
            "quoted_message_id" to quotedMessageId,
            "quoted_message_body" to quotedMessageBody,
            "quoted_message_sender_id" to quotedMessageSenderId,
            "quoted_message_type" to quotedMessageType
    )

}

object MessageStatus {
    const val SENT = "sent"
    const val READ = "read"
    const val FAILED = "failed"
    const val SENDING = "sending"
}
