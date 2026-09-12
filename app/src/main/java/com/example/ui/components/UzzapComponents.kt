package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.example.data.model.PhilippineRegions
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactCategory
import com.example.data.model.UserPresence
import com.example.data.model.UserProfileEntity
import com.example.data.remote.firestore.FirestoreSyncStatus
import com.example.ui.theme.PresenceAway
import com.example.ui.theme.PresenceBusy
import com.example.ui.theme.PresenceOffline
import com.example.ui.theme.PresenceOnline
import com.example.ui.theme.UzzapCyan
import com.example.ui.theme.UzzapNavy
import com.example.ui.theme.UzzapOrange
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshableScreen(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        content = content
    )
}

@Composable
fun syncStatusContainerColor(status: FirestoreSyncStatus): Color = when (status) {
    FirestoreSyncStatus.CONNECTED -> MaterialTheme.colorScheme.secondaryContainer
    FirestoreSyncStatus.SYNCING -> MaterialTheme.colorScheme.primaryContainer
    FirestoreSyncStatus.OFFLINE_CACHE -> MaterialTheme.colorScheme.tertiaryContainer
    else -> MaterialTheme.colorScheme.surfaceVariant
}

@Composable
fun syncStatusContentColor(status: FirestoreSyncStatus): Color = when (status) {
    FirestoreSyncStatus.CONNECTED -> MaterialTheme.colorScheme.onSecondaryContainer
    FirestoreSyncStatus.SYNCING -> MaterialTheme.colorScheme.onPrimaryContainer
    FirestoreSyncStatus.OFFLINE_CACHE -> MaterialTheme.colorScheme.onTertiaryContainer
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
fun PresenceDot(
    presence: UserPresence,
    modifier: Modifier = Modifier,
    size: Int = 12
) {
    val color = when (presence) {
        UserPresence.ONLINE -> PresenceOnline
        UserPresence.AWAY -> PresenceAway
        UserPresence.BUSY -> PresenceBusy
        UserPresence.INVISIBLE, UserPresence.OFFLINE -> PresenceOffline
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color)
            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
    )
}

@Composable
fun UzzapAvatar(
    emoji: String,
    bgColor: Long,
    presence: UserPresence? = null,
    size: Int = 48,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(Color(bgColor).copy(alpha = 0.25f))
                .border(1.5.dp, Color(bgColor).copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = (size * 0.48).sp
            )
        }

        if (presence != null) {
            PresenceDot(
                presence = presence,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 1.dp, y = 1.dp),
                size = (size * 0.3).toInt().coerceAtLeast(10)
            )
        }
    }
}

@Composable
fun UzzapTopHeader(
    profile: UserProfileEntity?,
    onPresenceClick: () -> Unit,
    syncStatus: FirestoreSyncStatus = FirestoreSyncStatus.CONNECTED,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        BoxWithConstraints {
            val compactHeader = maxWidth < 400.dp
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = if (compactHeader) 10.dp else 16.dp,
                        vertical = 10.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brand Logo & Title
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(UzzapOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "U",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "uzzap",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = (-0.5).sp
                            )
                            if (!compactHeader) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = UzzapOrange.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "1.0.14",
                                        color = UzzapOrange,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = profile?.statusMessage ?: "Mobile Instant Messenger",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Cloud sync stays available to accessibility services on every width.
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = syncStatusContainerColor(syncStatus),
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .semantics {
                            contentDescription = syncStatus.label
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = if (compactHeader) 7.dp else 6.dp,
                            vertical = 4.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    syncStatusContentColor(syncStatus)
                                )
                        )
                        if (!compactHeader) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Cloud",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = syncStatusContentColor(syncStatus)
                            )
                        }
                    }
                }

                // User Presence Pill (Clickable)
                if (profile != null) {
                    Surface(
                        onClick = onPresenceClick,
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .semantics {
                                contentDescription = "Change presence, ${profile.status.label}"
                            }
                            .testTag("presence_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = if (compactHeader) 8.dp else 10.dp,
                                vertical = 6.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PresenceDot(presence = profile.status, size = 10)
                            if (!compactHeader) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = profile.status.label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PresenceSelectorDialog(
    currentPresence: UserPresence,
    currentMessage: String,
    onDismiss: () -> Unit,
    onSave: (UserPresence, String) -> Unit
) {
    var selectedPresence by remember { mutableStateOf(currentPresence) }
    var statusMessage by remember { mutableStateOf(currentMessage) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "My Status & Presence",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select Availability:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                UserPresence.values().forEach { presence ->
                    Surface(
                        onClick = { selectedPresence = presence },
                        color = if (selectedPresence == presence) {
                            UzzapOrange.copy(alpha = 0.15f)
                        } else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PresenceDot(presence = presence, size = 12)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = presence.label,
                                fontSize = 14.sp,
                                fontWeight = if (selectedPresence == presence) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Personal Status Message:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                OutlinedTextField(
                    value = statusMessage,
                    onValueChange = { statusMessage = it },
                    singleLine = true,
                    placeholder = { Text("What's on your mind?") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = UzzapOrange
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedPresence, statusMessage) },
                colors = ButtonDefaults.buttonColors(containerColor = UzzapOrange),
                modifier = Modifier.testTag("save_presence_button")
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddContactDialog(
    onDismiss: () -> Unit,
    onAdd: (username: String, displayName: String, phone: String, category: ContactCategory) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("+63 ") }
    var selectedCategory by remember { mutableStateOf(ContactCategory.BUDDIES) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Uzzap Buddy",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Uzzap Username (6-12 chars)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = UzzapOrange)
                )

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name / Nickname") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = UzzapOrange)
                )

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Mobile Number (SMS Match)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = UzzapOrange)
                )

                Text(
                    text = "Contact Group:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(ContactCategory.BUDDIES, ContactCategory.CHATTERBOX, ContactCategory.MOST_FREQUENT).forEach { cat ->
                        Surface(
                            onClick = { selectedCategory = cat },
                            color = if (selectedCategory == cat) UzzapOrange else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = cat.displayName,
                                fontSize = 11.sp,
                                fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedCategory == cat) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                maxLines = 1,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (username.isNotBlank()) {
                        onAdd(
                            username,
                            displayName.ifBlank { username },
                            phoneNumber,
                            selectedCategory
                        )
                    }
                },
                enabled = username.length >= 3,
                colors = ButtonDefaults.buttonColors(containerColor = UzzapOrange),
                modifier = Modifier.testTag("submit_add_contact")
            ) {
                Text("Add Buddy")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CreateRoomDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, topic: String, category: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    val regions = PhilippineRegions.REGION_LIST.filter { it != "All" }
    var category by remember { mutableStateOf(regions.firstOrNull() ?: "NCR (Metro Manila)") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create Chatroom",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Province / Room Name (e.g. #Batangas)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = UzzapOrange)
                )

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Topic / Description") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = UzzapOrange)
                )

                Text(
                    text = "Region (Category):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(regions) { reg ->
                        val isSelected = category == reg
                        Surface(
                            onClick = { category = reg },
                            color = if (isSelected) UzzapOrange else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = reg,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreate(name, topic.ifBlank { "Welcome to $name!" }, category)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = UzzapOrange),
                modifier = Modifier.testTag("submit_create_room")
            ) {
                Text("Create Room")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EmptyListState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "\uD83D\uDCAC",
            fontSize = 40.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
