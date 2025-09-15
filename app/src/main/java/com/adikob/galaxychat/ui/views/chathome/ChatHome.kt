package com.adikob.galaxychat.ui.views.chathome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.paging.compose.collectAsLazyPagingItems
import com.adikob.galaxychat.R
import com.adikob.galaxychat.datamodels.Conversation
import com.adikob.galaxychat.datamodels.User
import com.adikob.galaxychat.ui.theme.SpaceGrotesk
import com.adikob.galaxychat.ui.theme.WorkSans
import com.adikob.galaxychat.ui.views.reusable.FullScreenLoadingDialog
import com.adikob.galaxychat.ui.views.reusable.NetworkImageWidget
import com.adikob.galaxychat.utility.formatDate
import com.adikob.galaxychat.viewmodels.ConversationViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHome(
    modifier: Modifier = Modifier,
    viewModel: ConversationViewModel,
    onStartNewConversation: () -> Unit,
    navigateToAuth: () -> Unit,
    onNavigateToChatRoom: (conversation: Conversation) -> Unit
) {
    val conversations = viewModel.conversations.collectAsLazyPagingItems()

    var isLoggingOut by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    NetworkImageWidget(
                        imageUrl = viewModel.getCurrentUserDisplayPhoto() ?: "",
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    val displayName = viewModel.getCurrentUserDisplayName() ?: ""
                    Text(
                        text = stringResource(R.string.hi_user, displayName),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = SpaceGrotesk
                        )
                    )
                },
                actions = {
                    OptionsMenu {
                        isLoggingOut = true
                        viewModel.logOut {
                            isLoggingOut = false
                            navigateToAuth()
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onStartNewConversation) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.start_new_conversation)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 15.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = conversations.itemSnapshotList,
                    key = { index, item -> item?.id ?: index }
                ) { index, _ ->
                    val conversation = conversations[index]
                    if (conversation != null) {
                        ConversationTile(
                            currentUserId = viewModel.getCurrentUserDisplayId(),
                            conversation = conversation,
                            onClick = { id -> onNavigateToChatRoom(id) }
                        )
                    }
                }
            }
        }
    }

    if (isLoggingOut) { FullScreenLoadingDialog() }
}

@Composable
private fun ConversationTile(
    modifier: Modifier = Modifier,
    conversation: Conversation,
    currentUserId: String?,
    onClick: (conversation: Conversation) -> Unit
) {
    val user = conversation.participants.firstOrNull { it.id != currentUserId }

    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(conversation) }
            .padding(vertical = 8.dp, horizontal = 10.dp)
    ) {
        val (profileImage, name, lastMessage, unreadMessageCount, lastUpdated) = createRefs()
        NetworkImageWidget(
            imageUrl = user?.photoUrl.toString(),
            modifier = Modifier
                .size(40.dp)
                .constrainAs(profileImage) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                }
        )

        Text(
            text = user?.name.toString(),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = WorkSans
            ),
            modifier = Modifier.constrainAs(name) {
                top.linkTo(parent.top)
                bottom.linkTo(lastMessage.top)
                start.linkTo(profileImage.end, margin = 10.dp)
                end.linkTo(lastUpdated.start, margin = 10.dp)
                width = Dimension.fillToConstraints
                height = Dimension.wrapContent
            }
        )

        Text(
            text = conversation.lastMessage,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.labelMedium.copy(
                fontStyle = FontStyle.Italic
            ),
            modifier = Modifier.constrainAs(lastMessage) {
                top.linkTo(name.bottom, margin = 5.dp)
                bottom.linkTo(parent.bottom)
                start.linkTo(profileImage.end, margin = 10.dp)
                end.linkTo(unreadMessageCount.start, margin = 10.dp)
                width = Dimension.fillToConstraints
                height = Dimension.wrapContent
            }
        )

        Text(
            text = formatDate(conversation.lastUpdatedDate),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.constrainAs(lastUpdated) {
                top.linkTo(parent.top)
                bottom.linkTo(unreadMessageCount.top, margin = 5.dp)
                start.linkTo(name.end)
                end.linkTo(parent.end)
                width = Dimension.wrapContent
                height = Dimension.wrapContent
            }
        )

        if (conversation.unreadMessageCount > 0) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(5.dp)
                    .constrainAs(unreadMessageCount) {
                        top.linkTo(lastUpdated.bottom)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(lastMessage.end)
                        end.linkTo(parent.end)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conversation.unreadMessageCount.toString(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun OptionsMenu(
    onLogOut: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.padding(16.dp)
    ) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more_options))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.log_out)) },
                onClick = {
                    expanded = false
                    onLogOut()
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConversationTilePreview() {
    val cal: Calendar  = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -2)
    val twoDaysAgo = cal.getTime()

    MaterialTheme {
        ConversationTile(
            conversation = Conversation(
                id = "123",
                participants = listOf(User(name = "John Doe", email = "", id = "123", photoUrl = "")),
                unreadMessageCount = 0,
                lastMessage = "Hello, how are you?",
                lastMessageStatus = "READ",
                isLastMessageRead = false,
                lastMessageType = "TEXT",
                lastMessageCreationTime = twoDaysAgo.time,
                createdAt = twoDaysAgo.time
            ),
            currentUserId = "653"
        ) {}
    }
}
