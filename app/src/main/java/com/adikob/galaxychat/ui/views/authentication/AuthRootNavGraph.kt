package com.adikob.galaxychat.ui.views.authentication

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.adikob.galaxychat.ui.views.RootNavPaths.AuthenticationRoute
import com.adikob.galaxychat.ui.views.RootNavPaths.ConversationRoute
import com.adikob.galaxychat.ui.views.authentication.AuthScreenNavPaths.LoginScreenPath
import com.adikob.galaxychat.ui.views.authentication.AuthScreenNavPaths.SignUpScreenPath
import com.adikob.galaxychat.ui.views.authentication.login.LoginScreen
import com.adikob.galaxychat.ui.views.authentication.signup.SignupScreen
import com.adikob.galaxychat.utility.scopedViewModel

fun NavGraphBuilder.authScreens(
    navController: NavController
) {
    navigation(
        startDestination = SignUpScreenPath.route,
        route = AuthenticationRoute.route
    ) {
        composable(route = LoginScreenPath.route) { backStackEntry ->
            LoginScreen(
                viewModel = backStackEntry.scopedViewModel(
                    route = AuthenticationRoute.route,
                    navController = navController
                ),
                navigateToSignUpScreen = {
                    navController.navigate(SignUpScreenPath.route) {
                        popUpTo(LoginScreenPath.route) { inclusive = true }
                    }
                },
                navigateToHomeScreen = {
                    navController.navigate(ConversationRoute.route) {
                        popUpTo(LoginScreenPath.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = SignUpScreenPath.route) { backStackEntry ->
            SignupScreen(
                viewModel = backStackEntry.scopedViewModel(
                    route = AuthenticationRoute.route,
                    navController = navController
                ),
                navigateToLoginScreen = {
                    navController.navigate(LoginScreenPath.route) {
                        popUpTo(SignUpScreenPath.route) { inclusive = true }
                    }
                },
                navigateToHomeScreen = {
                    navController.navigate(ConversationRoute.route) {
                        popUpTo(SignUpScreenPath.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

private sealed class AuthScreenNavPaths(val route: String) {
    object LoginScreenPath: AuthScreenNavPaths("Login")
    object SignUpScreenPath: AuthScreenNavPaths("Signup")
}