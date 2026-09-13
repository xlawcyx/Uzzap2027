package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatroomEntity
import com.example.data.model.RoomMessageEntity
import com.example.data.model.RoomRole
import com.example.ui.components.ClassicEmoticonMessage
import com.example.ui.components.ClassicEmoticonPicker
import com.example.ui.components.appendClassicEmoticon
import com.example.ui.theme.UzzapOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RoomDetailScreen(
    room: ChatroomEntity?,
    messages: List<RoomMessageEntity>,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    onToggleJoin: ((Boolean) -> Unit)? = null,
    onReportRoom: ((String, String, String) -> Unit)? = null
) {
    var inputText by remember { mutableStateOf("") }
    var showEmoticons by remember { mutableStateOf(false) }
    var showRoomMenu by remember { mutableStateOf(false) }
    var showReportRoomDialog by remember { mutableStateOf(false) }
    var reportRoomReason by remember { mutableStateOf("Inappropriate Content") }
    var reportRoomDetails by remember { mutableStateOf("") }
    var hasPositionedInitialMessages by remember { mutableStateOf(false) }
    var previousMessageCount by remember { mutableStateOf(0) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            if (!hasPositionedInitialMessages) {
                listState.scrollToItem(messages.lastIndex)
                hasPositionedInitialMessages = true
            } else {
                val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                if (lastVisibleItem >= previousMessageCount - 2) {
                    listState.animateScrollToItem(messages.lastIndex)
                }
            }
            previousMessageCount = messages.size
        } else {
            hasPositionedInitialMessages = false
            previousMessageCount = 0
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Room Top Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("room_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(UzzapOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = room?.name ?: "Chatroom",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${room?.chatterCount ?: 0} online • ${room?.category ?: ""}",
                        fontSize = 11.sp,
                        color = UzzapOrange,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // JOIN / LEAVE ROOM BUTTON IN TOP BAR
                if (room != null && onToggleJoin != null) {
                    if (room.isJoined) {
                        OutlinedButton(
                            onClick = { onToggleJoin(false) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.7f)),
                            modifier = Modifier.testTag("room_detail_leave_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Leave Room",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Leave",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        Button(
                            onClick = { onToggleJoin(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = UzzapOrange),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("room_detail_join_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Join Room",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Join",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Room Safety Menu (Report Chatroom)
                Box {
                    IconButton(
                        onClick = { showRoomMenu = true },
                        modifier = Modifier.testTag("room_more_options_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = showRoomMenu,
                        onDismissRequest = { showRoomMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Report Chatroom") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ReportProblem,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                showRoomMenu = false
                                showReportRoomDialog = true
                            },
                            modifier = Modifier.testTag("menu_report_room")
                        )
                    }
                }
            }
        }

        // Room Topic Banner
        if (room != null) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Topic: ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = UzzapOrange
                    )
                    Text(
                        text = room.topic,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }

        // Guest preview notice if user has not joined yet
        AnimatedVisibility(
            visible = room != null && !room.isJoined && onToggleJoin != null,
            enter = fadeIn(tween(180)) +
                expandVertically(tween(220, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(140)) +
                shrinkVertically(tween(180, easing = FastOutSlowInEasing))
        ) {
            Surface(
                color = UzzapOrange.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "\uD83D\uDC4B You're previewing this room. Join to save to your rooms!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onToggleJoin?.invoke(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = UzzapOrange),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Join Room", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start the conversation",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Say hello or send a classic UZZ-APP emoticon.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                    }
                }
            }
            items(messages, key = { it.id }) { msg ->
                RoomMessageItem(msg)
            }
        }

        AnimatedVisibility(
            visible = showEmoticons,
            enter = fadeIn(tween(180)) +
                expandVertically(tween(220, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(140)) +
                shrinkVertically(tween(180, easing = FastOutSlowInEasing))
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                ClassicEmoticonPicker(
                    onEmoticonSelected = { emoticon ->
                        inputText = appendClassicEmoticon(inputText, emoticon.token)
                    },
                    modifier = Modifier.testTag("room_emoticon_picker")
                )
            }
        }

        // Composer
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(tween(180, easing = FastOutSlowInEasing))
                .imePadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showEmoticons = !showEmoticons },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("room_emoticon_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEmotions,
                        contentDescription = "Classic emoticons",
                        tint = if (showEmoticons) {
                            UzzapOrange
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Chat in ${room?.name ?: "room"}...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    maxLines = 3,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UzzapOrange,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .testTag("room_message_input")
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            if (room?.isJoined == false && onToggleJoin != null) {
                                onToggleJoin(true)
                                return@IconButton
                            }
                            onSendMessage(inputText.trim())
                            inputText = ""
                            showEmoticons = false
                        }
                    },
                    enabled = inputText.isNotBlank(),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputText.isNotBlank()) UzzapOrange
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .testTag("send_room_message_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank()) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Report Chatroom Dialog
        if (showReportRoomDialog && room != null) {
            AlertDialog(
                onDismissRequest = { showReportRoomDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.ReportProblem,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                },
                title = {
                    Text(
                        text = "Report #${room.name}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Help keep UZZ-APP chatrooms safe. Reports are reviewed by human moderators in compliance with Google Play & App Store policies.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Select Reason:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val reasons = listOf(
                            "Inappropriate / NSFW Content",
                            "Harassment or Bullying",
                            "Spam / Commercial Ads",
                            "Hate Speech or Violence",
                            "Other"
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            reasons.forEach { reason ->
                                FilterChip(
                                    selected = reportRoomReason == reason,
                                    onClick = { reportRoomReason = reason },
                                    label = { Text(reason, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = UzzapOrange.copy(alpha = 0.2f),
                                        selectedLabelColor = UzzapOrange
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        OutlinedTextField(
                            value = reportRoomDetails,
                            onValueChange = { reportRoomDetails = it },
                            placeholder = { Text("Optional details...", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onReportRoom?.invoke(room.id, reportRoomReason, reportRoomDetails)
                            showReportRoomDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("submit_room_report_button")
                    ) {
                        Text("Submit Report", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showReportRoomDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

    }
}

@Composable
fun RoomMessageItem(
    message: RoomMessageEntity,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(message.timestamp))

    if (message.isSystem) {
        val lowerText = message.message.lowercase()
        val isJoin = lowerText.contains("join") || lowerText.contains("joins") || lowerText.contains("joined")
        val isLeave = lowerText.contains("left") || lowerText.contains("leave") || lowerText.contains("leaves")

        val textColor = when {
            isJoin -> MaterialTheme.colorScheme.primary
            isLeave -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

        val iconTint = when {
            isJoin -> MaterialTheme.colorScheme.primary
            isLeave -> MaterialTheme.colorScheme.error
            else -> UzzapOrange
        }

        val iconVector = when {
            isJoin -> Icons.Default.PersonAdd
            isLeave -> Icons.AutoMirrored.Filled.Logout
            else -> Icons.Default.Info
        }

        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .testTag("system_indicator_${message.id}"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = message.message,
                fontSize = 11.sp,
                fontWeight = if (isJoin || isLeave) FontWeight.SemiBold else FontWeight.Medium,
                color = textColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "• $formattedTime",
                fontSize = 9.sp,
                color = textColor.copy(alpha = 0.75f)
            )
        }
        return
    }

    val isMe = message.isFromMe

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isMe) "You" else message.senderUsername,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = UzzapOrange
            )
            if (message.senderRole != RoomRole.MEMBER) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = message.senderRole.title.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = UzzapOrange
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = formattedTime,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        ClassicEmoticonMessage(message = message.message, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
