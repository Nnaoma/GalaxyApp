package com.adikob.galaxychat.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.adikob.galaxychat.BuildConfig
import com.adikob.galaxychat.datamodels.User
import com.adikob.galaxychat.repository.ChatDataRepository
import com.adikob.galaxychat.utility.FirebaseHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val conversationRepository: ChatDataRepository,
    private val firebaseHelper: FirebaseHelper
): ViewModel() {
    private val _usersList = MutableStateFlow<List<User>>(emptyList())

    val usersList: StateFlow<List<User>> = _usersList

    val conversations = conversationRepository
        .getConversations()
        .cachedIn(viewModelScope)

    override fun onCleared() {
        conversationRepository.detachConversationsListener()
        super.onCleared()
    }

    fun getCurrentUserDisplayId() = firebaseHelper.getUserId()

    fun getCurrentUserDisplayName() = firebaseHelper.getCurrentUser()?.displayName

    fun getCurrentUserDisplayPhoto(): String? = firebaseHelper
        .getCurrentUser()
        ?.photoUrl
        ?.toString()

    fun fetchUsers(result: (isSuccess: Boolean) -> Unit) {
        viewModelScope.launch {
            runCatching { conversationRepository.fetchUsers() }.onSuccess { users ->
                _usersList.value = users
                result(true)
            }.onFailure { throwable ->
                if (BuildConfig.DEBUG)
                    Log.e("ConversationViewModel", "Error Fetching Users", throwable)
                result(false)
            }
        }
    }

    fun logOut(
        onFinished: () -> Unit
    ) {
        viewModelScope.launch {
            Firebase.auth.signOut()
            conversationRepository.signOut()
            onFinished()
        }
    }

    init {
        firebaseHelper.getUserId()?.let { conversationRepository.attachConversationsListener(it) }
    }
}