package com.adikob.galaxychat.ui.views.conversation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adikob.galaxychat.R
import com.adikob.galaxychat.ui.theme.SpaceGrotesk
import com.adikob.galaxychat.ui.theme.WorkSans
import com.adikob.galaxychat.ui.views.reusable.NetworkImageWidget
import com.adikob.galaxychat.utility.generateConversationId
import com.adikob.galaxychat.viewmodels.ConversationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartNewConversationScreen(
    modifier: Modifier = Modifier,
    viewModel: ConversationViewModel,
    onNavigateBack: () -> Unit,
    navigateToChatRoom: (id: String, name: String, photoUrl: String, userId: String) -> Unit
) {
    val context = LocalContext.current
    val usersList by viewModel.usersList.collectAsStateWithLifecycle()

    var isLoading by remember { mutableStateOf(false) }
    var didLoadingEncounterError by remember { mutableStateOf(false) }

    val snackBarHostState = remember { SnackbarHostState() }

    val loadContent: () -> Unit = {
        isLoading = true
        viewModel.fetchUsers { isSuccessful ->
            isLoading = false
            if (!isSuccessful) { didLoadingEncounterError = true }
        }
    }

    Scaffold(
        modifier = modifier.imePadding(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = stringResource(R.string.navigate_back),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.contacts),
                        style = LocalTextStyle.current.copy(
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            PullToRefreshBox(
                modifier = Modifier.fillMaxSize(),
                isRefreshing = isLoading,
                onRefresh = loadContent
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = usersList,
                        key = { user -> user.id }
                    ) { user ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .clickable(
                                    enabled = (viewModel.getCurrentUserDisplayId() != user.id)
                                ) {
                                    navigateToChatRoom(
                                        generateConversationId(user.id, viewModel.getCurrentUserDisplayId() ?: ""),
                                        user.name,
                                        user.photoUrl,
                                        user.id
                                    )
                                }
                                .padding(4.dp)
                        ) {
                            NetworkImageWidget(
                                imageUrl = user.photoUrl,
                                modifier = Modifier.size(36.dp)
                            )

                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = WorkSans,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_START) { loadContent.invoke() }

    LaunchedEffect(didLoadingEncounterError) {
        if (didLoadingEncounterError) {
            val result = snackBarHostState.showSnackbar(
                message = context.getString(R.string.error_occurred_loading_data),
                actionLabel = context.getString(R.string.retry)
            )

            if (result == SnackbarResult.ActionPerformed) {
                loadContent.invoke()
            }

            didLoadingEncounterError = false
        }
    }
}