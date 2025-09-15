package com.adikob.galaxychat.datamodels

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.Exclude
import com.google.firebase.database.PropertyName
import com.google.firebase.database.ServerValue
import java.util.Date

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey
    val id: String,
    val participants: List<User>,
    @ColumnInfo("unread_message_count")
    @get:PropertyName("unread_message_count")
    @set:PropertyName("unread_message_count")
    var unreadMessageCount: Int,
    @ColumnInfo("last_message")
    @get:PropertyName("last_message")
    @set:PropertyName("last_message")
    var lastMessage: String,
    @ColumnInfo("last_message_status")
    @get:PropertyName("last_message_status")
    @set:PropertyName("last_message_status")
    var lastMessageStatus: String,
    @ColumnInfo("is_last_message_read")
    @get:PropertyName("is_last_message_read")
    @set:PropertyName("is_last_message_read")
    @field:JvmField var isLastMessageRead: Boolean,
    @ColumnInfo("last_message_type")
    @get:PropertyName("last_message_type")
    @set:PropertyName("last_message_type")
    var lastMessageType: String,
    @ColumnInfo("last_message_creation_time")
    @get:PropertyName("last_message_creation_time")
    @set:PropertyName("last_message_creation_time")
    var lastMessageCreationTime: Long,
    @ColumnInfo("created_at")
    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    var createdAt: Long
) {
    constructor() : this("", emptyList(), 0, "", "", false, "", 0, 0)

    @get:Exclude
    val createdDate: Date
        get() = Date(createdAt)

    @get:Exclude
    val lastUpdatedDate: Date
        get() = Date(lastMessageCreationTime)

    fun toRealtimeDBMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "participants" to participants,
        "unread_message_count" to ServerValue.increment(1),
        "last_message" to lastMessage,
        "last_message_status" to MessageStatus.READ,
        "is_last_message_read" to isLastMessageRead,
        "last_message_type" to lastMessageType,
        "last_message_creation_time" to ServerValue.TIMESTAMP,
        "created_at" to createdAt
    )
}

data class ConversationMetadata(
    @ColumnInfo("last_updated")
    @get:PropertyName("last_updated")
    val lastUpdated: Long,
    @ColumnInfo("updated_by")
    @get:PropertyName("updated_by")
    val updatedBy: String,
    @ColumnInfo("modification_type")
    @get:PropertyName("modification_type")
    val modificationType: String,
    @ColumnInfo("message_id")
    @get:PropertyName("message_id")
    val messageId: String,
    @ColumnInfo("is_first_participant_typing")
    @get:PropertyName("is_first_participant_typing")
    @field:JvmField val isFirstParticipantTyping: Boolean,
    @ColumnInfo("is_second_participant_typing")
    @get:PropertyName("is_second_participant_typing")
    @field:JvmField val isSecondParticipantTyping: Boolean
)

object ConversationMetadataModificationType {
    const val MESSAGE_UPDATED = "MESSAGE_UPDATED"
    const val MESSAGE_DELETED = "MESSAGE_DELETED"
    const val NEW_MESSAGE_ADDED = "NEW_MESSAGE_ADDED"
    const val IDLE_MODIFICATION = "IDLE_MODIFICATION"  // Intended for when user's are typing
}