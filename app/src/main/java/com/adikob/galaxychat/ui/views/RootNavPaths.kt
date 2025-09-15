package com.adikob.galaxychat.ui.views

import kotlinx.serialization.Serializable

@Serializable
sealed class RootNavPaths(val route: String) {
    object AuthenticationRoute: RootNavPaths("Authentication")
    object ConversationRoute: RootNavPaths("ConversationRoute")
    object ChatsListRoute: RootNavPaths("ChatList")
    object StartNewChatRoute: RootNavPaths("StartNewChat")
    @Serializable
    class ChatScreenRoute(
        val userName: String,
        val userId: String,
        val pictureUrl: String,
        val conversationId: String
    ): RootNavPaths("ChatScreen")
}