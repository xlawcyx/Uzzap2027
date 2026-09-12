package com.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.example.data.model.ChatroomEntity
import com.example.data.model.ContactCategory
import com.example.data.model.ContactEntity
import com.example.data.model.ConversationEntity
import com.example.data.model.MessageDeliveryStatus
import com.example.data.model.MessageEntity
import com.example.data.model.RoomMessageEntity
import com.example.data.model.RoomRole
import com.example.data.model.UserPresence
import com.example.data.model.UserProfileEntity
import com.example.data.remote.firestore.FirestoreSyncStatus
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
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private enum class AuditScreen(val fileName: String) {
    SPLASH("splash"),
    HEADER("header"),
    LOGIN("login"),
    LOGIN_ERROR("login-error"),
    LOGIN_LOADING("login-loading"),
    BUDDIES("buddies"),
    BUDDIES_EMPTY("buddies-empty"),
    CHATS("chats"),
    CHATS_EMPTY("chats-empty"),
    ROOMS("rooms"),
    ROOMS_EMPTY("rooms-empty"),
    PROFILE("profile"),
    SETTINGS("settings"),
    CHAT_DETAIL("chat-detail"),
    ROOM_DETAIL("room-detail")
}

private val auditProfile = UserProfileEntity(
    username = "long_username_for_layout",
    displayName = "Alexandra Dela Cruz-Santos",
    phoneNumber = "+63 917 555 0198",
    status = UserPresence.ONLINE,
    statusMessage = "Available to chat — exploring Uzzap communities",
    avatarEmoji = "😎"
)

private val auditContact = ContactEntity(
    id = "friend_1",
    username = "very_long_friend_username",
    displayName = "María de los Santos",
    nickname = "Maria",
    phoneNumber = "+63 917 555 0101",
    presence = UserPresence.ONLINE,
    statusMessage = "Online and ready to chat",
    category = ContactCategory.BUDDIES,
    avatarEmoji = "🌺",
    avatarBgColor = 0xFFFF5722,
    isFavorite = true
)

private val auditConversation = ConversationEntity(
    id = "conversation_1",
    title = "María de los Santos with a long name",
    recipientUsername = auditContact.username,
    avatarEmoji = auditContact.avatarEmoji,
    avatarBgColor = auditContact.avatarBgColor,
    lastMessage = "This is a longer recent message used to verify truncation and spacing.",
    lastTimestamp = 1_700_000_000_000,
    unreadCount = 12
)

private val auditRoom = ChatroomEntity(
    id = "room_1",
    name = "Metro Manila Community Lounge",
    topic = "A deliberately long room topic for layout and truncation checks",
    category = "National Capital Region",
    chatterCount = 1_284,
    isJoined = true,
    userRole = RoomRole.MEMBER
)

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class UiAuditRenderTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    @Config(qualifiers = "w320dp-h640dp-mdpi", sdk = [36])
    fun renderCompactLightAuditMatrix() {
        renderAuditMatrix(darkTheme = false, variant = "compact-light")
    }

    @Test
    @Config(qualifiers = "w320dp-h640dp-mdpi", sdk = [36])
    fun renderCompactDarkAuditMatrix() {
        renderAuditMatrix(darkTheme = true, variant = "compact-dark")
    }

    @Test
    @Config(qualifiers = "w600dp-h900dp-mdpi", sdk = [36])
    fun renderLargeLightAuditMatrix() {
        renderAuditMatrix(darkTheme = false, variant = "large-light")
    }

    @Test
    @Config(qualifiers = "w600dp-h900dp-mdpi", sdk = [36])
    fun renderLargeDarkAuditMatrix() {
        renderAuditMatrix(darkTheme = true, variant = "large-dark")
    }

    @Test
    @Config(qualifiers = "w320dp-h640dp-mdpi", sdk = [36])
    fun compactControlsKeepAccessibleTargetsAndLabels() {
        val selectedScreen = mutableStateOf(AuditScreen.SETTINGS)
        composeTestRule.setContent {
            MyApplicationTheme {
                AuditScreenContent(selectedScreen.value)
            }
        }

        composeTestRule.onNodeWithTag("toggle_vibration")
            .assertIsDisplayed()
            .assertHasClickAction()
            .assertContentDescriptionEquals("Buzz Vibration & Shake")
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)

        composeTestRule.runOnIdle { selectedScreen.value = AuditScreen.PROFILE }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("avatar_edit_button")
            .assertIsDisplayed()
            .assertHasClickAction()
            .assertWidthIsAtLeast(48.dp)
            .assertHeightIsAtLeast(48.dp)
    }

    @Test
    @Config(qualifiers = "w320dp-h640dp-mdpi", sdk = [36])
    fun compactChatKeepsPrimaryComposerActionUsable() {
        composeTestRule.setContent {
            MyApplicationTheme {
                AuditScreenContent(AuditScreen.CHAT_DETAIL)
            }
        }

        composeTestRule.onNodeWithTag("top_buzz_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("composer_buzz_button").assertDoesNotExist()
        composeTestRule.onNodeWithTag("message_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("send_button").assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w320dp-h640dp-mdpi", sdk = [36])
    fun signUpLoadingShowsProgressStagesInOrder() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            MyApplicationTheme {
                LoginScreen(isLoading = true)
            }
        }

        composeTestRule.onNodeWithTag("signup_tab").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Connecting...").assertExists()

        composeTestRule.mainClock.advanceTimeBy(800L)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Authenticating...").assertExists()

        composeTestRule.mainClock.advanceTimeBy(800L)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Initializing...").assertExists()
    }

    private fun renderAuditMatrix(darkTheme: Boolean, variant: String) {
        val selectedScreen = mutableStateOf(AuditScreen.HEADER)
        composeTestRule.setContent {
            MyApplicationTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AuditScreenContent(selectedScreen.value)
                }
            }
        }

        AuditScreen.entries.forEach { screen ->
            composeTestRule.runOnIdle { selectedScreen.value = screen }
            composeTestRule.waitForIdle()
            composeTestRule.onRoot().captureRoboImage(
                filePath = "build/ui-audit/$variant-${screen.fileName}.png"
            )
        }
    }
}

@Composable
private fun AuditScreenContent(screen: AuditScreen) {
    when (screen) {
        AuditScreen.SPLASH -> SplashScreen()
        AuditScreen.HEADER -> Column {
            UzzapTopHeader(
                profile = auditProfile,
                onPresenceClick = {},
                syncStatus = FirestoreSyncStatus.SYNCING
            )
        }
        AuditScreen.LOGIN -> LoginScreen()
        AuditScreen.LOGIN_ERROR -> LoginScreen(errorMessage = "Unable to connect. Check your connection and try again.")
        AuditScreen.LOGIN_LOADING -> LoginScreen(isLoading = true)
        AuditScreen.BUDDIES -> FriendsScreen(
            contacts = listOf(auditContact),
            pendingRequests = listOf(auditContact.copy(id = "request_1")),
            searchQuery = "",
            selectedCategory = "All",
            onSearchChange = {},
            onCategoryChange = {},
            onContactClick = {},
            onFavoriteToggle = {},
            onAcceptRequest = {},
            onDeclineRequest = {},
            onAddContactClick = {}
        )
        AuditScreen.BUDDIES_EMPTY -> FriendsScreen(
            contacts = emptyList(),
            pendingRequests = emptyList(),
            searchQuery = "",
            selectedCategory = "All",
            onSearchChange = {},
            onCategoryChange = {},
            onContactClick = {},
            onFavoriteToggle = {},
            onAcceptRequest = {},
            onDeclineRequest = {},
            onAddContactClick = {}
        )
        AuditScreen.CHATS -> ChatsScreen(
            conversations = listOf(auditConversation),
            onConversationClick = {},
            onStartNewChat = {}
        )
        AuditScreen.CHATS_EMPTY -> ChatsScreen(
            conversations = emptyList(),
            onConversationClick = {},
            onStartNewChat = {}
        )
        AuditScreen.ROOMS -> RoomsScreen(
            rooms = listOf(auditRoom),
            selectedCategory = "All",
            onCategoryChange = {},
            onRoomClick = {},
            onToggleJoin = { _, _ -> },
            onCreateRoomClick = {}
        )
        AuditScreen.ROOMS_EMPTY -> RoomsScreen(
            rooms = emptyList(),
            selectedCategory = "All",
            onCategoryChange = {},
            onRoomClick = {},
            onToggleJoin = { _, _ -> },
            onCreateRoomClick = {}
        )
        AuditScreen.PROFILE -> ProfileScreen(
            profile = auditProfile,
            totalBuddies = 128,
            totalChats = 42,
            totalRooms = 7,
            joinedRooms = listOf(auditRoom)
        )
        AuditScreen.SETTINGS -> SettingsScreen(profile = auditProfile)
        AuditScreen.CHAT_DETAIL -> ChatDetailScreen(
            conversation = auditConversation,
            messages = listOf(
                MessageEntity(
                    id = "message_1",
                    conversationId = auditConversation.id,
                    senderUsername = auditContact.username,
                    senderDisplayName = auditContact.displayName,
                    body = "A long incoming message to verify wrapping, spacing, and bubble width on every device.",
                    timestamp = 1_700_000_000_000,
                    status = MessageDeliveryStatus.READ,
                    isFromMe = false
                ),
                MessageEntity(
                    id = "message_2",
                    conversationId = auditConversation.id,
                    senderUsername = "me",
                    senderDisplayName = auditProfile.displayName,
                    body = "Thanks! This outgoing reply should remain readable in both themes.",
                    timestamp = 1_700_000_060_000,
                    status = MessageDeliveryStatus.READ,
                    isFromMe = true
                )
            ),
            onBack = {},
            onSendMessage = { _, _ -> },
            onSendBuzz = {},
            buzzTrigger = 0
        )
        AuditScreen.ROOM_DETAIL -> RoomDetailScreen(
            room = auditRoom,
            messages = listOf(
                RoomMessageEntity(
                    id = "room_message_1",
                    roomId = auditRoom.id,
                    senderUsername = auditContact.username,
                    senderRole = RoomRole.MODERATOR,
                    message = "Welcome! This message checks chatroom wrapping on compact layouts.",
                    timestamp = 1_700_000_000_000
                )
            ),
            onBack = {},
            onSendMessage = {},
            onToggleJoin = {}
        )
    }
}
