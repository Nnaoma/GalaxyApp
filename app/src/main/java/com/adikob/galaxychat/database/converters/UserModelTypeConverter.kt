package com.adikob.galaxychat.database.converters

import androidx.room.TypeConverter
import com.adikob.galaxychat.datamodels.User

class UserModelTypeConverter {
    @TypeConverter
    fun fromUsersList(values: List<User>?): String? {
        return values?.joinToString(separator = "{") { user ->
            user.toRoomDBString()
        }
    }

    @TypeConverter
    fun toUsersList(value: String?): List<User>? {
        val users = mutableListOf<User>()

        value?.split("{")?.forEach { userString ->
            if (userString.isNotEmpty()) {
                users.add(User().fromRoomDBString(userString))
            }
        }

        return users
    }
}