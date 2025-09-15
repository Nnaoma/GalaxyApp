package com.adikob.galaxychat.utility

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import javax.inject.Inject

class FirebaseHelper @Inject constructor() {
    fun getRootFirebaseDBReference() = Firebase.database.reference
    fun getUserId() = Firebase.auth.currentUser?.uid
    fun getCurrentUser() = Firebase.auth.currentUser
}