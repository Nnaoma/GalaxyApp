package com.adikob.galaxychat.ui.views.authentication.login

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.adikob.galaxychat.R
import com.adikob.galaxychat.ui.views.authentication.AuthScreenRootLayout
import com.adikob.galaxychat.viewmodels.AuthScreenViewModel

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthScreenViewModel,
    navigateToSignUpScreen: () -> Unit,
    navigateToHomeScreen: () -> Unit
) {
    var isLoggingIn by remember { mutableStateOf(false) }

    val context = LocalContext.current

    AuthScreenRootLayout(
        modifier = modifier,
        isSignUp = false,
        actionText = stringResource(R.string.login),
        isLoading = isLoggingIn,
        viewModel = viewModel,
        onPerformAction = { email, password, _ ->
            isLoggingIn = true
            viewModel.loginWithEmailAndPassword(
                email = email,
                password = password,
                onResult = { wasSuccessful ->
                    isLoggingIn = false
                    if (wasSuccessful) { navigateToHomeScreen() }
                    else {
                        Toast.makeText(context, R.string.error_occurred_signing_in, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        },
        alternativeAuthContentActionText = stringResource(R.string.sign_up),
        alternativeAuthContentText = stringResource(R.string.no_account),
        onPerformAlternativeAuthContentAction = navigateToSignUpScreen,
        navigateToHomeScreen = navigateToHomeScreen
    )
}