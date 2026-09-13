package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.AuthenticationManager
import com.example.BuildConfig
import com.example.UzzapApplication
import com.example.data.local.UzzapDatabase
import com.example.data.model.ChatroomEntity
import com.example.data.model.ContactCategory
import com.example.data.model.ContactEntity
import com.example.data.model.ConversationEntity
import com.example.data.model.MessageEntity
import com.example.data.model.RoomMessageEntity
import com.example.data.model.UserPresence
import com.example.data.model.UserProfileEntity
import com.example.data.remote.firestore.FirestoreSyncStatus
import com.example.data.repository.UzzapRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class MainTab(val title: String) {
    BUDDIES("Buddies"),
    CHATS("Chats"),
    ROOMS("Rooms"),
    PROFILE("Profile"),
    SETTINGS("Settings")
}

@OptIn(ExperimentalCoroutinesApi::class)
class UzzapViewModel(application: Application) : AndroidViewModel(application) {
    private val database = UzzapDatabase.getDatabase(application, viewModelScope)
    val repository = UzzapRepository(database, viewModelScope)

    // Session / Auth state
    private val prefs = application.getSharedPreferences("uzzap_session", Context.MODE_PRIVATE)
    private val _isLoggedIn = MutableStateFlow(
        prefs.getBoolean("is_logged_in", false) && (
            AuthenticationManager.getInstance().isUserSignedIn() ||
                (BuildConfig.ALLOW_DEMO_AUTH && !UzzapApplication.isRealFirebaseConfigured)
            )
    )
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Current navigation state
    private val _currentTab = MutableStateFlow(MainTab.BUDDIES)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    private val _activeRoomId = MutableStateFlow<String?>(null)
    val activeRoomId: StateFlow<String?> = _activeRoomId.asStateFlow()

    // Filters and Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _buddyCategoryFilter = MutableStateFlow("All")
    val buddyCategoryFilter: StateFlow<String> = _buddyCategoryFilter.asStateFlow()

    private val _roomCategoryFilter = MutableStateFlow("All")
    val roomCategoryFilter: StateFlow<String> = _roomCategoryFilter.asStateFlow()

    private val _selectedRegion = MutableStateFlow<String?>(null)
    val selectedRegion: StateFlow<String?> = _selectedRegion.asStateFlow()

    // UI Dialogs
    private val _isAddContactDialogOpen = MutableStateFlow(false)
    val isAddContactDialogOpen: StateFlow<Boolean> = _isAddContactDialogOpen.asStateFlow()

    private val _isCreateRoomDialogOpen = MutableStateFlow(false)
    val isCreateRoomDialogOpen: StateFlow<Boolean> = _isCreateRoomDialogOpen.asStateFlow()

    private val _isPresenceMenuOpen = MutableStateFlow(false)
    val isPresenceMenuOpen: StateFlow<Boolean> = _isPresenceMenuOpen.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Settings State
    private val settingsPrefs = application.getSharedPreferences("uzzap_settings", Context.MODE_PRIVATE)
    private val _buzzShakeTrigger = MutableStateFlow(0)
    val buzzShakeTrigger: StateFlow<Int> = _buzzShakeTrigger.asStateFlow()

    // Persistent Flows from Room
    val profile: StateFlow<UserProfileEntity?> = repository.profileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val contacts: StateFlow<List<ContactEntity>> = repository.contactsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingRequests: StateFlow<List<ContactEntity>> = repository.pendingRequestsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val conversations: StateFlow<List<ConversationEntity>> = repository.conversationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatrooms: StateFlow<List<ChatroomEntity>> = repository.chatroomsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val firestoreSyncStatus: StateFlow<FirestoreSyncStatus> = repository.firestoreSyncStatus

    // Active conversation messages
    val activeConversationMessages: StateFlow<List<MessageEntity>> = _activeConversationId
        .flatMapLatest { id ->
            if (id != null) repository.getMessagesForConversation(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active room messages
    val activeRoomMessages: StateFlow<List<RoomMessageEntity>> = _activeRoomId
        .flatMapLatest { id ->
            if (id != null) repository.getRoomMessages(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        if (_isLoggedIn.value) {
            repository.initCloudSync()
        }
        // Collect buzz events to trigger vibration/screen shake
        viewModelScope.launch {
            repository.buzzEvents.collect {
                if (profile.value?.vibrationEnabled != false) {
                    _buzzShakeTrigger.value += 1
                    vibrateForBuzz()
                }
            }
        }
    }

    // Navigation Actions
    fun setTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun refreshData() {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.refreshCloudData()
                // Keep the Material indicator readable while listeners reconnect.
                delay(400)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                android.util.Log.e("UzzapViewModel", "Cloud refresh failed", error)
                _operationMessage.value = "Cloud refresh failed: ${error.localizedMessage ?: "please try again."}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun openConversation(conversationId: String) {
        _activeConversationId.value = conversationId
        viewModelScope.launch {
            repository.markConversationRead(conversationId)
        }
    }

    fun startConversationWithContact(contact: ContactEntity) {
        launchOperation("Conversation could not be opened") {
            val convoId = repository.startOrGetConversation(contact)
            _activeConversationId.value = convoId
        }
    }

    fun closeConversation() {
        _activeConversationId.value = null
    }

    fun openRoom(roomId: String) {
        _activeRoomId.value = roomId
        repository.enterRoom(roomId)
    }

    fun closeRoom() {
        _activeRoomId.value = null
        repository.leaveActiveRoom()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setBuddyCategoryFilter(filter: String) {
        _buddyCategoryFilter.value = filter
    }

    fun setRoomCategoryFilter(filter: String) {
        _roomCategoryFilter.value = filter
    }

    fun selectRegion(region: String?) {
        _selectedRegion.value = region
    }

    fun clearSelectedRegion() {
        _selectedRegion.value = null
    }

    // Dialog Controls
    fun setAddContactDialogOpen(open: Boolean) {
        _isAddContactDialogOpen.value = open
    }

    fun setCreateRoomDialogOpen(open: Boolean) {
        _isCreateRoomDialogOpen.value = open
    }

    fun setPresenceMenuOpen(open: Boolean) {
        _isPresenceMenuOpen.value = open
    }

    // Operational Actions
    fun updatePresence(status: UserPresence, message: String) {
        launchOperation("Presence could not be updated") {
            repository.updatePresence(status, message)
            _isPresenceMenuOpen.value = false
        }
    }

    fun updateVibrationSetting(enabled: Boolean) {
        launchOperation("Vibration setting could not be updated") {
            repository.updateVibration(enabled)
        }
    }

    fun updateFullProfile(
        displayName: String,
        statusMessage: String,
        avatarEmoji: String,
        phoneNumber: String = ""
    ) {
        launchOperation("Profile could not be updated") {
            val current = profile.value ?: return@launchOperation
            val updated = current.copy(
                displayName = displayName.trim().ifBlank { current.displayName },
                statusMessage = statusMessage.trim().ifBlank { current.statusMessage },
                avatarEmoji = avatarEmoji.ifBlank { current.avatarEmoji },
                phoneNumber = if (phoneNumber.isNotBlank()) phoneNumber.trim() else current.phoneNumber
            )
            repository.updateProfile(updated)
        }
    }

    fun toggleFavorite(contactId: String) {
        launchOperation("Favorite could not be updated") {
            repository.toggleFavoriteContact(contactId)
        }
    }

    fun acceptFriendRequest(contactId: String) {
        launchOperation("Could not accept the friend request") {
            repository.acceptFriendRequest(contactId)
        }
    }

    fun declineFriendRequest(contactId: String) {
        launchOperation("Could not decline the friend request") {
            repository.declineFriendRequest(contactId)
        }
    }

    fun addContact(username: String, displayName: String, phone: String, category: ContactCategory) {
        launchOperation("Could not send the friend request") {
            repository.addContact(username, displayName, phone, category)
            _isAddContactDialogOpen.value = false
        }
    }

    fun sendMessage(text: String, replyTo: String? = null) {
        val convoId = _activeConversationId.value ?: return
        if (text.isBlank()) return
        launchOperation("Message could not be sent") {
            repository.sendMessage(convoId, text, replyTo)
        }
    }

    fun sendBuzz() {
        val convoId = _activeConversationId.value ?: return
        launchOperation("BUZZ could not be sent") {
            repository.sendBuzz(convoId)
        }
    }

    fun joinOrLeaveRoom(roomId: String, join: Boolean) {
        launchOperation("Room membership could not be updated") {
            repository.joinOrLeaveRoom(roomId, join)
        }
    }

    fun sendRoomMessage(text: String) {
        val roomId = _activeRoomId.value ?: return
        if (text.isBlank()) return
        launchOperation("Room message could not be sent") {
            repository.sendRoomMessage(roomId, text)
        }
    }

    fun createChatroom(name: String, topic: String, category: String) {
        launchOperation("Chatroom could not be created") {
            repository.createChatroom(name, topic, category)
            _isCreateRoomDialogOpen.value = false
        }
    }

    /**
     * Signs out the user, updates their presence to OFFLINE, clears active chats/rooms,
     * and triggers the sign-in screen.
     */
    fun logout() {
        viewModelScope.launch {
            prefs.edit { putBoolean("is_logged_in", false) }
            runCatching { repository.updatePresence(UserPresence.OFFLINE, "Offline - Logged out") }
            try {
                AuthenticationManager.getInstance().signOut().getOrThrow()
            } catch (e: Exception) {
                // Graceful fallback if no active Firebase session
            }
            _activeConversationId.value = null
            _activeRoomId.value = null
            _selectedRegion.value = null
            _isLoggedIn.value = false
        }
    }

    fun syncProfileWithCloud() {
        launchOperation("Profile could not be synced") {
            val p = profile.value
            if (p != null) {
                repository.updateProfile(p)
                _operationMessage.value = "Profile synced successfully."
            }
        }
    }

    fun clearAuthError() {
        _authError.value = null
    }

    fun clearOperationMessage() {
        _operationMessage.value = null
    }

    fun signIn(usernameOrPhone: String, pin: String) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val result = repository.signIn(usernameOrPhone, pin)
            if (result.isSuccess) {
                prefs.edit { putBoolean("is_logged_in", true) }
                _isLoggedIn.value = true
                _currentTab.value = MainTab.BUDDIES
                _authError.value = null
            } else {
                _authError.value = result.exceptionOrNull()?.localizedMessage ?: "Sign in failed"
            }
            _authLoading.value = false
        }
    }

    fun signUp(
        username: String,
        displayName: String,
        phoneNumber: String,
        pin: String,
        avatarEmoji: String,
        statusMessage: String
    ) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val result = repository.signUp(
                username = username,
                displayName = displayName,
                phoneNumber = phoneNumber,
                pin = pin,
                avatarEmoji = avatarEmoji,
                statusMessage = statusMessage
            )
            if (result.isSuccess) {
                prefs.edit { putBoolean("is_logged_in", true) }
                _isLoggedIn.value = true
                _currentTab.value = MainTab.BUDDIES
                _authError.value = null
            } else {
                _authError.value = result.exceptionOrNull()?.localizedMessage ?: "Sign up failed"
            }
            _authLoading.value = false
        }
    }

    fun submitReport(target: String, reason: String, details: String) {
        launchOperation("Report could not be submitted") {
            repository.submitReport(target, reason, details)
            _operationMessage.value = "Report submitted successfully."
        }
    }

    fun blockUser(username: String) {
        launchOperation("User could not be blocked") {
            repository.blockUser(username)
            val blockedConversationId = conversations.value
                .firstOrNull { it.recipientUsername == username }
                ?.id
            if (_activeConversationId.value == blockedConversationId) {
                _activeConversationId.value = null
            }
        }
    }

    fun clearLocalChatCache() {
        launchOperation("Chat cache could not be cleared") {
            repository.clearLocalChatCache()
            _operationMessage.value = "Local chat cache cleared."
        }
    }

    fun deleteAccount(password: String) {
        viewModelScope.launch {
            try {
                repository.deleteAccountData(password)
                prefs.edit { clear() }
                settingsPrefs.edit { clear() }
                _isLoggedIn.value = false
                _currentTab.value = MainTab.BUDDIES
                _activeConversationId.value = null
                _activeRoomId.value = null
            } catch (e: Exception) {
                android.util.Log.e("UzzapViewModel", "Error deleting account", e)
                _operationMessage.value = e.message ?: "Account deletion failed. Please try again."
            }
        }
    }

    private fun launchOperation(
        failureMessage: String,
        operation: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                operation()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                android.util.Log.e("UzzapViewModel", failureMessage, error)
                _operationMessage.value = "$failureMessage: ${error.localizedMessage ?: "please try again."}"
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun vibrateForBuzz() {
        val application = getApplication<Application>()
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            application.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        } ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(250)
        }
    }

    override fun onCleared() {
        repository.close()
        super.onCleared()
    }
}
