package com.adikob.galaxychat.datamodels

import com.google.firebase.database.PropertyName

data class User(
    val name: String,
    val email: String,
    val id: String,
    @get:PropertyName("photo_url")
    @set:PropertyName("photo_url")
    var photoUrl: String
) {
    constructor() : this("", "", "", "")

    fun toRoomDBString() = "$name}$email}$id}$photoUrl"

    fun fromRoomDBString(value: String): User {
        val values = value.split("}")
        if (values.size != 4) throw IllegalArgumentException("Invalid user string")

        return this.copy(name = values[0], email = values[1], id = values[2], photoUrl = values[3])
    }
}
