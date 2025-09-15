package com.adikob.galaxychat.repository

import android.content.Context
import androidx.core.net.toUri
import com.google.firebase.Firebase
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.adikob.galaxychat.R
import com.adikob.galaxychat.datamodels.User
import com.adikob.galaxychat.utility.FirebaseHelper
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseHelper: FirebaseHelper
) {
    suspend fun signUpWithEmailPassword(
        email: String,
        password: String,
        name: String
    ) {
        val result = Firebase.auth.createUserWithEmailAndPassword(email, password).await()

        val nameChangeRequest = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .setPhotoUri("https://ui-avatars.com/api/?name=$name&background=random&color=fff".toUri())
            .build()

        result.user?.updateProfile(nameChangeRequest)?.await()

        if (result.user == null) throw Exception(context.getString(R.string.error_occurred_signing_in))
        else createUserInDB()
    }

    suspend fun loginWithEmailPassword(email: String, password: String) {
        val result = Firebase.auth.signInWithEmailAndPassword(email, password).await()

        if (result.user == null) throw Exception(context.getString(R.string.error_occurred_signing_in))
    }

    suspend fun authenticateWithGoogle(idToken: String) = withContext(Dispatchers.IO) {
        val firebaseCredentials = GoogleAuthProvider.getCredential(idToken, null)
        val user = Firebase.auth.currentUser

        val result = if (user != null) user.linkWithCredential(firebaseCredentials).await()
        else Firebase.auth.signInWithCredential(firebaseCredentials).await()

        if (result.user == null) throw Exception(context.getString(R.string.error_occurred_signing_in))
        else createUserInDB()
    }

    private suspend fun createUserInDB() {
        val user = Firebase.auth.currentUser ?: throw Exception(context.getString(R.string.error_occurred_signing_in))
        val name = user.displayName ?: throw Exception(context.getString(R.string.error_occurred_signing_in))
        val email = user.email ?: throw Exception(context.getString(R.string.error_occurred_signing_in))
        val photoUrl = user.photoUrl?.toString() ?: "https://ui-avatars.com/api/?name=$name&background=random&color=fff"

        firebaseHelper.getRootFirebaseDBReference()
            .child("users")
            .child(user.uid)
            .setValue(
                User(
                    name = name,
                    email = email,
                    photoUrl = photoUrl,
                    id = user.uid
                )
            )
            .await()
    }
}