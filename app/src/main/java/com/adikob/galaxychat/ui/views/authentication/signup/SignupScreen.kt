package com.adikob.galaxychat.ui.views.authentication.signup

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
fun SignupScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthScreenViewModel,
    navigateToLoginScreen: () -> Unit,
    navigateToHomeScreen: () -> Unit
) {
    var isSigningUp by remember { mutableStateOf(false) }

    val context = LocalContext.current

    AuthScreenRootLayout(
        modifier = modifier,
        isSignUp = true,
        isLoading = isSigningUp,
        viewModel = viewModel,
        actionText = stringResource(R.string.sign_up),
        onPerformAction = { email, password, name ->
            isSigningUp = true
            viewModel.signUpWithEmailAndPassword(
                email = email,
                password = password,
                name = name,
            ) { wasSuccessful ->
                isSigningUp = false
                if (wasSuccessful) { navigateToHomeScreen() }
                else {
                    Toast.makeText(context, R.string.error_occurred_signing_in, Toast.LENGTH_SHORT).show()
                }
            }
        },
        alternativeAuthContentText = stringResource(R.string.already_have_an_account),
        alternativeAuthContentActionText = stringResource(R.string.login),
        onPerformAlternativeAuthContentAction = navigateToLoginScreen,
        navigateToHomeScreen = navigateToHomeScreen
    )
}
