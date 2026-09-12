package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserPresence
import com.example.ui.components.AddContactDialog
import com.example.ui.components.CreateRoomDialog
import com.example.ui.components.PresenceSelectorDialog
import com.example.ui.components.UzzapTopHeader
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChatsScreen
import com.example.ui.screens.FriendsScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RoomDetailScreen
import com.example.ui.screens.RoomsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.UzzapOrange
import com.example.ui.theme.UzzapOrangeContainer
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.UzzapViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var showSplash by rememberSaveable { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    delay(SPLASH_DURATION_MILLIS)
                    showSplash = false
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .safeDrawingPadding()
                ) {
                    if (showSplash) {
                        SplashScreen()
                    } else {
                        UzzapApp()
                    }
                }
            }
        }
    }
}

private const val SPLASH_DURATION_MILLIS = 1_200L

private enum class AppScreen {
    TABS,
    CONVERSATION,
    ROOM
}

private const val SCREEN_TRANSITION_MILLIS = 280
private const val CHROME_TRANSITION_MILLIS = 220

@Composable
fun UzzapApp(
    viewModel: UzzapViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activeConversationId by viewModel.activeConversationId.collectAsStateWithLifecycle()
    val activeRoomId by viewModel.activeRoomId.collectAsStateWithLifecycle()

    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val pendingRequests by viewModel.pendingRequests.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val chatrooms by viewModel.chatrooms.collectAsStateWithLifecycle()

    val activeMessages by viewModel.activeConversationMessages.collectAsStateWithLifecycle()
    val activeRoomMessages by viewModel.activeRoomMessages.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val buddyFilter by viewModel.buddyCategoryFilter.collectAsStateWithLifecycle()
    val roomFilter by viewModel.roomCategoryFilter.collectAsStateWithLifecycle()
    val selectedRegion by viewModel.selectedRegion.collectAsStateWithLifecycle()

    val isAddContactDialogOpen by viewModel.isAddContactDialogOpen.collectAsStateWithLifecycle()
    val isCreateRoomDialogOpen by viewModel.isCreateRoomDialogOpen.collectAsStateWithLifecycle()
    val isPresenceMenuOpen by viewModel.isPresenceMenuOpen.collectAsStateWithLifecycle()
    val buzzTrigger by viewModel.buzzShakeTrigger.collectAsStateWithLifecycle()
    val firestoreSyncStatus by viewModel.firestoreSyncStatus.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val soundEffectsEnabled by viewModel.soundEffectsEnabled.collectAsStateWithLifecycle()
    val enterKeySends by viewModel.enterKeySends.collectAsStateWithLifecycle()
    val cloudPresenceSync by viewModel.cloudPresenceSync.collectAsStateWithLifecycle()
    val autoSaveHistory by viewModel.autoSaveHistory.collectAsStateWithLifecycle()

    val activeConvo = conversations.firstOrNull { it.id == activeConversationId }
    val activeRoom = chatrooms.firstOrNull { it.id == activeRoomId }
    var retainedConversation by remember { mutableStateOf(activeConvo) }
    var retainedConversationMessages by remember { mutableStateOf(activeMessages) }
    var retainedRoom by remember { mutableStateOf(activeRoom) }
    var retainedRoomMessages by remember { mutableStateOf(activeRoomMessages) }

    SideEffect {
        if (activeConversationId != null) {
            retainedConversation = activeConvo
            retainedConversationMessages = activeMessages
        }
        if (activeRoomId != null) {
            retainedRoom = activeRoom
            retainedRoomMessages = activeRoomMessages
        }
    }

    // Intercept back navigation when in detail screens
    BackHandler(enabled = activeConversationId != null || activeRoomId != null) {
        if (activeConversationId != null) {
            viewModel.closeConversation()
        } else if (activeRoomId != null) {
            viewModel.closeRoom()
        }
    }

    val totalUnread = conversations.sumOf { it.unreadCount }
    val pendingRequestsCount = pendingRequests.size
    val authLoading by viewModel.authLoading.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()

    if (!isLoggedIn) {
        LoginScreen(
            isLoading = authLoading,
            errorMessage = authError,
            onClearError = { viewModel.clearAuthError() },
            onSignIn = { usernameOrPhone, pin ->
                viewModel.signIn(usernameOrPhone, pin)
            },
            onSignUp = { username, displayName, phone, password, emoji, status ->
                viewModel.signUp(username, displayName, phone, password, emoji, status)
            }
        )
        return
    }

    val appScreen = when {
        activeConversationId != null -> AppScreen.CONVERSATION
        activeRoomId != null -> AppScreen.ROOM
        else -> AppScreen.TABS
    }
    val showAppChrome = appScreen == AppScreen.TABS
    val tabStateHolder = rememberSaveableStateHolder()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AnimatedVisibility(
                visible = showAppChrome,
                enter = fadeIn(tween(CHROME_TRANSITION_MILLIS)) +
                    slideInVertically(tween(CHROME_TRANSITION_MILLIS)) { -it } +
                    expandVertically(tween(CHROME_TRANSITION_MILLIS), expandFrom = Alignment.Top),
                exit = fadeOut(tween(CHROME_TRANSITION_MILLIS)) +
                    slideOutVertically(tween(CHROME_TRANSITION_MILLIS)) { -it } +
                    shrinkVertically(tween(CHROME_TRANSITION_MILLIS), shrinkTowards = Alignment.Top)
            ) {
                UzzapTopHeader(
                    profile = profile,
                    onPresenceClick = { viewModel.setPresenceMenuOpen(true) },
                    syncStatus = firestoreSyncStatus
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = showAppChrome,
                enter = fadeIn(tween(CHROME_TRANSITION_MILLIS)) +
                    slideInVertically(tween(CHROME_TRANSITION_MILLIS)) { it } +
                    expandVertically(tween(CHROME_TRANSITION_MILLIS), expandFrom = Alignment.Bottom),
                exit = fadeOut(tween(CHROME_TRANSITION_MILLIS)) +
                    slideOutVertically(tween(CHROME_TRANSITION_MILLIS)) { it } +
                    shrinkVertically(tween(CHROME_TRANSITION_MILLIS), shrinkTowards = Alignment.Bottom)
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    // 1. Buddies
                    NavigationBarItem(
                        selected = currentTab == MainTab.BUDDIES,
                        onClick = { viewModel.setTab(MainTab.BUDDIES) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (pendingRequestsCount > 0) {
                                        Badge(containerColor = UzzapOrange) {
                                            Text("$pendingRequestsCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentTab == MainTab.BUDDIES) Icons.Filled.Group else Icons.Outlined.Group,
                                    contentDescription = "Buddies"
                                )
                            }
                        },
                        label = { Text("Buddies", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = UzzapOrange,
                            selectedTextColor = UzzapOrange,
                            indicatorColor = UzzapOrange.copy(alpha = 0.2f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_buddies")
                    )

                    // 2. Chats
                    NavigationBarItem(
                        selected = currentTab == MainTab.CHATS,
                        onClick = { viewModel.setTab(MainTab.CHATS) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (totalUnread > 0) {
                                        Badge(containerColor = UzzapOrange) {
                                            Text("$totalUnread")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentTab == MainTab.CHATS) {
                                        Icons.AutoMirrored.Filled.Chat
                                    } else {
                                        Icons.AutoMirrored.Outlined.Chat
                                    },
                                    contentDescription = "Chats"
                                )
                            }
                        },
                        label = { Text("Chats", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = UzzapOrange,
                            selectedTextColor = UzzapOrange,
                            indicatorColor = UzzapOrange.copy(alpha = 0.2f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_chats")
                    )

                    // 3. Rooms
                    NavigationBarItem(
                        selected = currentTab == MainTab.ROOMS,
                        onClick = { viewModel.setTab(MainTab.ROOMS) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.ROOMS) Icons.Filled.Tag else Icons.Outlined.Tag,
                                contentDescription = "Rooms"
                            )
                        },
                        label = { Text("Rooms", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = UzzapOrange,
                            selectedTextColor = UzzapOrange,
                            indicatorColor = UzzapOrange.copy(alpha = 0.2f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_rooms")
                    )

                    // 4. Profile
                    NavigationBarItem(
                        selected = currentTab == MainTab.PROFILE,
                        onClick = { viewModel.setTab(MainTab.PROFILE) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                                contentDescription = "Profile"
                            )
                        },
                        label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = UzzapOrange,
                            selectedTextColor = UzzapOrange,
                            indicatorColor = UzzapOrange.copy(alpha = 0.2f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_profile")
                    )

                    // 5. Settings
                    NavigationBarItem(
                        selected = currentTab == MainTab.SETTINGS,
                        onClick = { viewModel.setTab(MainTab.SETTINGS) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = UzzapOrange,
                            selectedTextColor = UzzapOrange,
                            indicatorColor = UzzapOrange.copy(alpha = 0.2f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .clipToBounds()
        ) {
            AnimatedContent(
                targetState = appScreen,
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopStart,
                transitionSpec = {
                    val openingDetail = initialState == AppScreen.TABS && targetState != AppScreen.TABS
                    val closingDetail = initialState != AppScreen.TABS && targetState == AppScreen.TABS

                    when {
                        openingDetail -> (
                            slideInHorizontally(
                                animationSpec = tween(SCREEN_TRANSITION_MILLIS, easing = FastOutSlowInEasing),
                                initialOffsetX = { it }
                            ) + fadeIn(tween(SCREEN_TRANSITION_MILLIS))
                        ).togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(SCREEN_TRANSITION_MILLIS, easing = FastOutSlowInEasing),
                                targetOffsetX = { -it / 4 }
                            ) + fadeOut(tween(SCREEN_TRANSITION_MILLIS))
                        )

                        closingDetail -> (
                            slideInHorizontally(
                                animationSpec = tween(SCREEN_TRANSITION_MILLIS, easing = FastOutSlowInEasing),
                                initialOffsetX = { -it / 4 }
                            ) + fadeIn(tween(SCREEN_TRANSITION_MILLIS))
                        ).togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(SCREEN_TRANSITION_MILLIS, easing = FastOutSlowInEasing),
                                targetOffsetX = { it }
                            ) + fadeOut(tween(SCREEN_TRANSITION_MILLIS))
                        )

                        else -> fadeIn(tween(SCREEN_TRANSITION_MILLIS)) togetherWith
                            fadeOut(tween(SCREEN_TRANSITION_MILLIS))
                    }.using(SizeTransform(clip = false))
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    AppScreen.CONVERSATION -> {
                        ChatDetailScreen(
                            conversation = activeConvo ?: retainedConversation,
                            messages = if (activeConversationId != null) {
                                activeMessages
                            } else {
                                retainedConversationMessages
                            },
                            onBack = { viewModel.closeConversation() },
                            onSendMessage = { text, replyTo -> viewModel.sendMessage(text, replyTo) },
                            onSendBuzz = { viewModel.sendBuzz() },
                            buzzTrigger = buzzTrigger,
                            onBlockUser = { username -> viewModel.blockUser(username) },
                            onReportUser = { username, reason, details ->
                                viewModel.submitReport(
                                    target = "user:$username",
                                    reason = reason,
                                    details = details
                                )
                            }
                        )
                    }
                    AppScreen.ROOM -> {
                        RoomDetailScreen(
                            room = activeRoom ?: retainedRoom,
                            messages = if (activeRoomId != null) activeRoomMessages else retainedRoomMessages,
                            onBack = { viewModel.closeRoom() },
                            onSendMessage = { text -> viewModel.sendRoomMessage(text) },
                            onToggleJoin = { join ->
                                activeRoom?.let { r ->
                                    viewModel.joinOrLeaveRoom(r.id, join)
                                }
                            },
                            onReportRoom = { roomId, reason, details ->
                                viewModel.submitReport(
                                    target = "room:$roomId",
                                    reason = reason,
                                    details = details
                                )
                            }
                        )
                    }
                    AppScreen.TABS -> {
                        AnimatedContent(
                            targetState = currentTab,
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.TopStart,
                            transitionSpec = {
                                (fadeIn(tween(180, delayMillis = 40)) togetherWith
                                    fadeOut(tween(140))).using(SizeTransform(clip = false))
                            },
                            label = "TabTransition"
                        ) { tab ->
                            tabStateHolder.SaveableStateProvider(tab) {
                                when (tab) {
                            MainTab.BUDDIES -> {
                                FriendsScreen(
                                    contacts = contacts,
                                    pendingRequests = pendingRequests,
                                    searchQuery = searchQuery,
                                    selectedCategory = buddyFilter,
                                    onSearchChange = { viewModel.setSearchQuery(it) },
                                    onCategoryChange = { viewModel.setBuddyCategoryFilter(it) },
                                    onContactClick = { contact ->
                                        viewModel.startConversationWithContact(contact)
                                    },
                                    onFavoriteToggle = { viewModel.toggleFavorite(it) },
                                    onAcceptRequest = { viewModel.acceptFriendRequest(it) },
                                    onDeclineRequest = { viewModel.declineFriendRequest(it) },
                                    onAddContactClick = { viewModel.setAddContactDialogOpen(true) },
                                    isRefreshing = isRefreshing,
                                    onRefresh = { viewModel.refreshData() }
                                )
                            }
                            MainTab.CHATS -> {
                                ChatsScreen(
                                    conversations = conversations,
                                    onConversationClick = { convoId ->
                                        viewModel.openConversation(convoId)
                                    },
                                    onStartNewChat = {
                                        viewModel.setTab(MainTab.BUDDIES)
                                    },
                                    isRefreshing = isRefreshing,
                                    onRefresh = { viewModel.refreshData() }
                                )
                            }
                            MainTab.ROOMS -> {
                                RoomsScreen(
                                    rooms = chatrooms,
                                    selectedCategory = roomFilter,
                                    onCategoryChange = { viewModel.setRoomCategoryFilter(it) },
                                    onRoomClick = { room ->
                                        viewModel.openRoom(room.id)
                                    },
                                    onToggleJoin = { roomId, join ->
                                        viewModel.joinOrLeaveRoom(roomId, join)
                                    },
                                    onCreateRoomClick = {
                                        viewModel.setCreateRoomDialogOpen(true)
                                    },
                                    isRefreshing = isRefreshing,
                                    onRefresh = { viewModel.refreshData() },
                                    selectedRegion = selectedRegion,
                                    onSelectRegion = { region -> viewModel.selectRegion(region) }
                                )
                            }
                            MainTab.PROFILE -> {
                                ProfileScreen(
                                    profile = profile,
                                    totalBuddies = contacts.size,
                                    totalChats = conversations.size,
                                    totalRooms = chatrooms.count { it.isJoined },
                                    joinedRooms = chatrooms.filter { it.isJoined },
                                    firestoreSyncStatus = firestoreSyncStatus,
                                    onSyncNowClick = { viewModel.syncProfileWithCloud() },
                                    onEditPresenceClick = { viewModel.setPresenceMenuOpen(true) },
                                    onUpdatePresence = { presence, statusMsg -> viewModel.updatePresence(presence, statusMsg) },
                                    onUpdateProfile = { name, status, emoji, phone -> viewModel.updateFullProfile(name, status, emoji, phone) },
                                    onOpenSettings = { viewModel.setTab(MainTab.SETTINGS) },
                                    onOpenRoom = { roomId -> viewModel.openRoom(roomId) },
                                    onBrowseRooms = { viewModel.setTab(MainTab.ROOMS) }
                                )
                            }
                            MainTab.SETTINGS -> {
                                SettingsScreen(
                                    profile = profile,
                                    firestoreSyncStatus = firestoreSyncStatus,
                                    notificationsEnabled = notificationsEnabled,
                                    soundEffectsEnabled = soundEffectsEnabled,
                                    enterKeySends = enterKeySends,
                                    cloudPresenceSync = cloudPresenceSync,
                                    autoSaveHistory = autoSaveHistory,
                                    onToggleVibration = { viewModel.updateVibrationSetting(it) },
                                    onToggleNotifications = { viewModel.updateNotificationSetting(it) },
                                    onToggleSoundEffects = { viewModel.updateSoundSetting(it) },
                                    onToggleEnterKeySends = { viewModel.updateEnterKeySends(it) },
                                    onToggleCloudPresenceSync = { viewModel.updateCloudPresenceSync(it) },
                                    onToggleAutoSaveHistory = { viewModel.updateAutoSaveHistory(it) },
                                    onSyncNowClick = { viewModel.syncProfileWithCloud() },
                                    onNavigateToProfile = { viewModel.setTab(MainTab.PROFILE) },
                                    onLogoutClick = { viewModel.logout(context) },
                                    onDeleteAccount = { viewModel.deleteAccount(context) }
                                )
                            }
                            }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (isPresenceMenuOpen) {
        PresenceSelectorDialog(
            currentPresence = profile?.status ?: UserPresence.ONLINE,
            currentMessage = profile?.statusMessage ?: "",
            onDismiss = { viewModel.setPresenceMenuOpen(false) },
            onSave = { presence, message ->
                viewModel.updatePresence(presence, message)
            }
        )
    }

    if (isAddContactDialogOpen) {
        AddContactDialog(
            onDismiss = { viewModel.setAddContactDialogOpen(false) },
            onAdd = { username, displayName, phone, category ->
                viewModel.addContact(username, displayName, phone, category)
            }
        )
    }

    if (isCreateRoomDialogOpen) {
        CreateRoomDialog(
            onDismiss = { viewModel.setCreateRoomDialogOpen(false) },
            onCreate = { name, topic, category ->
                viewModel.createChatroom(name, topic, category)
            }
        )
    }
}

// Preserve backwards-compatible Greeting composable for Robolectric screenshot test
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Android") }
}
