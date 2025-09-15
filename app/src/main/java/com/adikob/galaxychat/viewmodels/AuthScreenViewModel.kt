package com.adikob.galaxychat.viewmodels

import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adikob.galaxychat.BuildConfig
import com.adikob.galaxychat.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthScreenViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    fun authenticateWithGoogle(
        credential: Credential,
        onResult: (wasSuccessful: Boolean) -> Unit
    ) {
        viewModelScope.launch {
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                try {
                    authRepository.authenticateWithGoogle(googleIdTokenCredential.idToken)
                    onResult(true)
                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) Log.e("Google", "Error with Sign up", e)

                    onResult(false)
                }
            } else { onResult(false) }
        }
    }

    fun signUpWithEmailAndPassword(
        email: String,
        password: String,
        name: String,
        onResult: (wasSuccessful: Boolean) -> Unit
    ) {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            onResult(false)
            return
        }

        viewModelScope.launch {
            try {
                authRepository.signUpWithEmailPassword(
                    email = email,
                    password = password,
                    name = name
                )
                onResult(true)
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) Log.e("SIGN UP", "Error with Sign up", e)
                onResult(false)
            }
        }
    }

    fun loginWithEmailAndPassword(
        email: String,
        password: String,
        onResult: (wasSuccessful: Boolean) -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            onResult(false)
            return
        }

        viewModelScope.launch {
            try {
                authRepository.loginWithEmailPassword(email, password)
                onResult(true)
            } catch (_: Exception) { onResult(false) }
        }
    }
}