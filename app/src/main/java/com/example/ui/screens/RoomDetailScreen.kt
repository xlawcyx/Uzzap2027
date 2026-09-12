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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    var reportRoomSubmitted by remember { mutableStateOf(false) }
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
                        color = Color(0xFF10B981),
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
                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.7f)),
                            modifier = Modifier.testTag("room_detail_leave_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Leave Room",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Leave",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFEF4444)
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
                            text = "Say hello or send a classic Uzzap emoticon.",
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
                        .background(if (inputText.isNotBlank()) UzzapOrange else Color.LightGray)
                        .testTag("send_room_message_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.onPrimary,
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
                            text = "Help keep Uzzap chatrooms safe. Reports are reviewed by human moderators in compliance with Google Play & App Store policies.",
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
                            reportRoomSubmitted = true
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

        // Report Room Submitted Dialog
        if (reportRoomSubmitted) {
            AlertDialog(
                onDismissRequest = { reportRoomSubmitted = false },
                title = {
                    Text(
                        text = "Report Received",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Text(
                        text = "Thank you. Our moderation team reviews reported chatrooms within 24 hours to enforce our Community Guidelines.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { reportRoomSubmitted = false },
                        colors = ButtonDefaults.buttonColors(containerColor = UzzapOrange)
                    ) {
                        Text("OK")
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

        val containerColor = when {
            isJoin -> MaterialTheme.colorScheme.secondaryContainer
            isLeave -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        }

        val borderColor = when {
            isJoin || isLeave -> Color.Transparent
            else -> MaterialTheme.colorScheme.outlineVariant
        }

        val textColor = when {
            isJoin -> MaterialTheme.colorScheme.onSecondaryContainer
            isLeave -> MaterialTheme.colorScheme.onErrorContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

        val iconTint = when {
            isJoin -> Color(0xFF2E7D32)
            isLeave -> Color(0xFFC62828)
            else -> UzzapOrange
        }

        val iconVector = when {
            isJoin -> Icons.Default.PersonAdd
            isLeave -> Icons.AutoMirrored.Filled.Logout
            else -> Icons.Default.Info
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = containerColor,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.testTag("system_indicator_${message.id}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
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
            }
        }
        return
    }

    val isMe = message.isFromMe
    val alignment = if (isMe) Alignment.End else Alignment.Start
    val bubbleColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val timestampColor = if (isMe) {
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalAlignment = alignment
    ) {
        // Sender Info
        if (!isMe) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            ) {
                Text(
                    text = message.senderUsername,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = UzzapOrange
                )

                if (message.senderRole != RoomRole.MEMBER) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = UzzapOrange.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = message.senderRole.title,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = UzzapOrange,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            border = if (isMe) null else androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 16.dp
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                ClassicEmoticonMessage(
                    message = message.message,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formattedTime,
                    fontSize = 10.sp,
                    color = timestampColor,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
