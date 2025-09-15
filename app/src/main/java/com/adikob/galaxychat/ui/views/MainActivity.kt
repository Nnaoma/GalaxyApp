package com.adikob.galaxychat.ui.views

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.adikob.galaxychat.BuildConfig
import com.adikob.galaxychat.ui.theme.GalaxyChatTheme
import com.adikob.galaxychat.ui.views.RootNavPaths.AuthenticationRoute
import com.adikob.galaxychat.ui.views.RootNavPaths.ChatScreenRoute
import com.adikob.galaxychat.ui.views.RootNavPaths.ChatsListRoute
import com.adikob.galaxychat.ui.views.RootNavPaths.ConversationRoute
import com.adikob.galaxychat.ui.views.RootNavPaths.StartNewChatRoute
import com.adikob.galaxychat.ui.views.authentication.authScreens
import com.adikob.galaxychat.ui.views.chathome.ChatHome
import com.adikob.galaxychat.ui.views.chatroom.ChatRoomScreen
import com.adikob.galaxychat.ui.views.conversation.StartNewConversationScreen
import com.adikob.galaxychat.utility.FirebaseHelper
import com.adikob.galaxychat.utility.scopedViewModel
import com.adikob.galaxychat.viewmodels.ChatRoomViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var firebaseHelper: FirebaseHelper

    var isEmulatorConfigured = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*if (BuildConfig.DEBUG && !isEmulatorConfigured) {
            Firebase.auth.useEmulator("192.168.1.174", 9099)
            Firebase.database.useEmulator("192.168.1.174", 9000)
            isEmulatorConfigured =  true
        }*/

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val startDestination = if (Firebase.auth.currentUser == null) AuthenticationRoute.route
                else ConversationRoute.route

            GalaxyChatTheme {
                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    authScreens(navController)

                    navigation(
                        route = ConversationRoute.route,
                        startDestination = ChatsListRoute.route
                    ) {
                        composable(ChatsListRoute.route) { backStackEntry ->
                            ChatHome(
                                viewModel = backStackEntry.scopedViewModel(
                                    route = ConversationRoute.route,
                                    navController = navController
                                ),
                                navigateToAuth = {
                                    navController.navigate(AuthenticationRoute.route) {
                                        popUpTo(ConversationRoute.route) { inclusive = true }
                                    }
                                },
                                onStartNewConversation = {
                                    navController.navigate(StartNewChatRoute.route)
                                },
                                onNavigateToChatRoom = { conversation ->
                                    val user = conversation.participants.firstOrNull {
                                        it.id != firebaseHelper.getUserId()
                                    }
                                    navController.navigate(ChatScreenRoute(
                                        userName = user?.name.toString(),
                                        conversationId = conversation.id,
                                        pictureUrl = user?.photoUrl.toString(),
                                        userId = user?.id.toString()
                                    ))
                                }
                            )
                        }

                        composable(StartNewChatRoute.route) { backStackEntry ->
                            StartNewConversationScreen(
                                viewModel = backStackEntry.scopedViewModel(
                                    route = ConversationRoute.route,
                                    navController = navController
                                ),
                                onNavigateBack = { navController.popBackStack() },
                                navigateToChatRoom = { id, name, photoUrl, userId ->
                                    navController.navigate(ChatScreenRoute(
                                        userName = name,
                                        conversationId = id,
                                        pictureUrl = photoUrl,
                                        userId = userId
                                    )) { popUpTo(StartNewChatRoute.route) { inclusive = true } }
                                }
                            )
                        }
                    }

                    composable<ChatScreenRoute> { navEntry ->
                        val details = navEntry.toRoute<ChatScreenRoute>()

                        val viewModel = hiltViewModel<ChatRoomViewModel, ChatRoomViewModel.Factory>(
                            creationCallback = { factory -> factory.create(details.conversationId) }
                        )

                        ChatRoomScreen(
                            userName = details.userName,
                            pictureUrl = details.pictureUrl,
                            viewModel = viewModel,
                            userId = details.userId,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}