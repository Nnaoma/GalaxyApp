package com.adikob.galaxychat.ui.views.authentication

import android.content.Context
import androidx.credentials.GetCredentialRequest
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.GetCredentialException
import com.adikob.galaxychat.R
import com.adikob.galaxychat.ui.theme.SpaceGrey80
import com.adikob.galaxychat.ui.theme.SpaceGrotesk
import com.adikob.galaxychat.ui.theme.WorkSans
import com.adikob.galaxychat.ui.views.reusable.FullScreenLoadingDialog
import com.adikob.galaxychat.viewmodels.AuthScreenViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import kotlinx.coroutines.launch

@Composable
fun AuthScreenRootLayout(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    isSignUp: Boolean,
    actionText: String,
    alternativeAuthContentText: String,
    alternativeAuthContentActionText: String,
    viewModel: AuthScreenViewModel,
    onPerformAction: (email: String, password: String, name: String) -> Unit,
    onPerformAlternativeAuthContentAction: () -> Unit,
    navigateToHomeScreen: () -> Unit
) {
    val context = LocalContext.current
    val credentialsManager = CredentialManager.create(context)
    val coroutineScope = rememberCoroutineScope()

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }

    var errorType by remember{ mutableStateOf(ErrorType.NONE) }
    var errorText by remember { mutableStateOf("") }
    var isSigningWithGoogle by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 0.dp, bottom = innerPadding.calculateBottomPadding()),
            horizontalAlignment = Alignment.CenterHorizontally,
            // verticalArrangement = Arrangement.Center
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                drawCircle(
                    color = primaryColor,
                    radius = 200.dp.toPx(),
                    center = Offset(10f, 5f)
                )
            }
            ItemsSpacerBig()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 15.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = stringResource(R.string.welcome),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }
            ItemsSpacerBig()
            if (isSignUp) {
                TextFieldWidget(
                    label = stringResource(R.string.full_name),
                    isError = errorType == ErrorType.NAME,
                    errorText = errorText,
                    value = name,
                    onChanged = { name = it }
                )
            }
            ItemsSpacerSmall()
            TextFieldWidget(
                label = stringResource(R.string.email),
                isError = errorType == ErrorType.EMAIL,
                errorText = errorText,
                value = email,
                onChanged = { email = it }
            )
            ItemsSpacerSmall()
            TextFieldWidget(
                label = stringResource(R.string.password),
                isError = errorType == ErrorType.PASSWORD,
                errorText = errorText,
                isPasswordField = isSignUp,
                value = password,
                onChanged = { password = it }
            )
            ItemsSpacerSmall()
            Button(
                content = { Text(text = actionText) },
                onClick = {
                    when {
                        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                            errorType = ErrorType.EMAIL
                        }
                        password.isBlank() -> { errorType = ErrorType.PASSWORD }
                        (isSignUp && name.isBlank()) -> { errorType = ErrorType.NAME }
                        else -> {
                            errorType = ErrorType.NONE
                            onPerformAction(email, password, name)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.85f)
            )
            ItemsSpacerBig()
            OrDivider()
            ItemsSpacerBig()
            OutlinedButton(onClick = {
                val request = generateGoogleCredentialRequest(context)
                coroutineScope.launch {
                    try {
                        val result = credentialsManager.getCredential(
                            request = request,
                            context = context
                        )
                        isSigningWithGoogle = true
                        viewModel.authenticateWithGoogle(
                            result.credential,
                            onResult = { wasSuccessful ->
                                isSigningWithGoogle = false
                                if (wasSuccessful) { navigateToHomeScreen() }
                                else {
                                    Toast.makeText(
                                        context,
                                        R.string.error_occurred_signing_in, Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    } catch (_: GetCredentialException) {
                        Toast.makeText(context, R.string.unknown_credentials, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }) {
                Icon(
                    painter = painterResource(R.drawable.google),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(text = stringResource(R.string.continue_with_google))
            }
            Spacer(Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = alternativeAuthContentText,
                    style = LocalTextStyle.current.copy(fontFamily = WorkSans)
                )
                TextButton(onClick = onPerformAlternativeAuthContentAction) {
                    Text(text = alternativeAuthContentActionText)
                }
            }
        }
    }

    if (isLoading || isSigningWithGoogle) {
        FullScreenLoadingDialog()
    }
}

@Composable
private fun TextFieldWidget(
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String = "",
    isError: Boolean = false,
    errorText: String = "",
    isPasswordField: Boolean = false,
    value: String,
    onChanged: (String) -> Unit
) {
    var hidePassword by remember { mutableStateOf(isPasswordField) }

    OutlinedTextField(
        value = value,
        onValueChange = onChanged,
        isError = isError,
        modifier = modifier.fillMaxWidth(0.9f),
        visualTransformation = if (hidePassword) PasswordVisualTransformation()
            else VisualTransformation.None,
        label = { Text(text = label) },
        placeholder = { Text(text = placeholder) },
        trailingIcon = {
            if (isPasswordField) {
                IconButton(onClick = { hidePassword = !hidePassword }) {
                    val resource = if (hidePassword) R.drawable.visibility else R.drawable.visibility_off
                    Icon(
                        painter = painterResource(resource),
                        contentDescription = null,
                        tint = SpaceGrey80
                    )
                }
            }
        },
        supportingText = { if (isError) { Text(text = errorText) } }
    )
}

@Composable
private fun ItemsSpacerBig() {
    Spacer(Modifier.height(15.dp))
}

@Composable
private fun ItemsSpacerSmall() {
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun OrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(Modifier.weight(1f))
        Text(
            text = stringResource(R.string.or),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        HorizontalDivider(Modifier.weight(1f))
    }
}

/**
 * Generates a GetCredentialRequest for Google ID.
 * @param context The application context.
 */
fun generateGoogleCredentialRequest(context: Context): GetCredentialRequest {
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(context.getString(R.string.default_web_client_id))
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    return request
}

private enum class ErrorType {
    NONE, NAME, EMAIL, PASSWORD
}

