package com.adikob.galaxychat.utility

import android.text.format.DateUtils
import java.util.Date

fun formatDate(date: Date): String = DateUtils.getRelativeTimeSpanString(
    date.time,
    System.currentTimeMillis(),
    DateUtils.MINUTE_IN_MILLIS,
    DateUtils.FORMAT_ABBREV_RELATIVE
).toString()

/**
 * Generates a deterministic conversation ID for the 1-to-1 chat.
 * The smaller userId (lexicographically) always comes first.
 */
fun generateConversationId(userId1: String, userId2: String): String {
    return if (userId1 == userId2) {
        throw IllegalArgumentException("User IDs must be different")
    } else if (userId1 < userId2) {
        "${userId1}_$userId2"
    } else {
        "${userId2}_$userId1"
    }
}

