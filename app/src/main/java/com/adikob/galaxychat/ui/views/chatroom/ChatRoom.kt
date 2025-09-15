package com.adikob.galaxychat.ui.views.chatroom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.paging.compose.collectAsLazyPagingItems
import com.adikob.galaxychat.datamodels.Message
import com.adikob.galaxychat.utility.formatDate
import com.adikob.galaxychat.viewmodels.ChatRoomViewModel
import com.adikob.galaxychat.R
import com.adikob.galaxychat.ui.theme.SpaceGrotesk
import com.adikob.galaxychat.ui.views.reusable.NetworkImageWidget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    modifier: Modifier = Modifier,
    userName: String?,
    pictureUrl: String?,
    userId: String?,
    viewModel: ChatRoomViewModel,
    onNavigateBack: () -> Unit
) {
    val messages = viewModel.chatMessages.collectAsLazyPagingItems()

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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NetworkImageWidget(
                            imageUrl = "$pictureUrl",
                            modifier = Modifier.size(36.dp)
                        )

                        Text(
                            text = "$userName",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = SpaceGrotesk,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(10.dp)
        ) {
            ConstraintLayout(
                modifier = Modifier.fillMaxSize()
            ) {
                val (itemsList, textBox) = createRefs()

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    reverseLayout = true,
                    modifier = Modifier
                        .constrainAs(itemsList) {
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            top.linkTo(parent.top)
                            bottom.linkTo(textBox.top, margin = 5.dp)
                            width = Dimension.matchParent
                            height = Dimension.fillToConstraints
                        }
                ) {
                    itemsIndexed(
                        items = messages.itemSnapshotList,
                        key = { index, item -> (item?.id ?: index) }
                    ) { index, _ ->
                        val message = messages[index]
                        if (message != null) {
                            MessageWidget(
                                isFromMe = (viewModel.getCurrentUserUID() == message.senderId),
                                message = message
                            )
                        }
                    }
                }

                TextBox(
                    onSendMessage = { message ->
                        viewModel.sendMessage(
                            message = message,
                            userId = userId.toString(),
                            userName = userName.toString(),
                            userPhotoUrl = pictureUrl.toString()
                        )
                    },
                    modifier = Modifier
                        .constrainAs(textBox) {
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom)
                            top.linkTo(itemsList.bottom)
                            width = Dimension.matchParent
                            height = Dimension.wrapContent
                        }
                )
            }
        }
    }
}

@Composable
private fun MessageWidget(
    modifier: Modifier = Modifier,
    isFromMe: Boolean,
    message: Message
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(7.dp),
        contentAlignment = if (isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.56f)
                .wrapContentHeight()
                .clip(
                    RoundedCornerShape(
                        topEnd = if (isFromMe) 0.dp else 18.dp,
                        topStart = if (isFromMe) 18.dp else 0.dp,
                        bottomEnd = 18.dp,
                        bottomStart = 18.dp
                    )
                )
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = message.body,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Normal
                )
            )

            Text(
                text = formatDate(message.createdAt),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.End
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            )
        }
    }
}

@Composable
private fun TextBox(
    modifier: Modifier = Modifier,
    onSendMessage: (message: String) -> Unit
) {
    var message by remember { mutableStateOf("") }

    val isSendButtonEnabled by remember { derivedStateOf { message.isNotBlank() } }

    Row(
        modifier = modifier.padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            modifier = Modifier
                .weight(1f)
                .wrapContentHeight(),
            placeholder = {
                Text(
                    text = stringResource(R.string.type_a_message),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Light)
                )
            },
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = Color.Gray,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                disabledBorderColor = Color.Gray
            )
        )
        IconButton(
            enabled = isSendButtonEnabled,
            onClick = {
                onSendMessage(message)
                message = ""
            },
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = stringResource(R.string.send_message),
                modifier = Modifier.size(30.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MessageWidgetPreview() {
    val mockMessage = Message(
        id = "msg_001",
        body = "Hello, how are you? It's NMI5 here!",
        timestamp = System.currentTimeMillis(),
        status = "sent",
        type = "text",
        reaction = null,
        senderId = "user_123",
        conversationId = "conv_456",
        quotedMessageId = null,
        quotedMessageBody = null,
        quotedMessageSenderId = null,
        quotedMessageType = null
    )

    MaterialTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MessageWidget(
                isFromMe = true,
                message = mockMessage
            )

            MessageWidget(
                isFromMe = false,
                message = mockMessage
            )
        }
    }
}