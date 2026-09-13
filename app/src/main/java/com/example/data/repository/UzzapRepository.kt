package com.example.data.repository

import com.example.data.local.UzzapDatabase
import com.example.data.model.ChatroomEntity
import com.example.data.model.ContactCategory
import com.example.data.model.ContactEntity
import com.example.data.model.ConversationEntity
import com.example.data.model.FriendshipState
import com.example.data.model.MessageDeliveryStatus
import com.example.data.model.MessageEntity
import com.example.data.model.MessageType
import com.example.data.model.RoomMessageEntity
import com.example.data.model.RoomRole
import com.example.data.model.UserPresence
import com.example.data.model.UserProfileEntity
import com.example.data.remote.firestore.FirestoreSyncStatus
import com.example.data.remote.firestore.FriendRequestStatus
import com.example.data.remote.firestore.UzzapFirestoreService
import com.example.data.remote.firestore.directConversationId
import com.example.data.remote.firestore.normalizeUsername
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

internal fun mergeRemoteChatroom(
    remoteRoom: ChatroomEntity,
    localRoom: ChatroomEntity?
): ChatroomEntity = if (localRoom == null) {
    remoteRoom
} else {
    remoteRoom.copy(
        isJoined = localRoom.isJoined,
        userRole = localRoom.userRole
    )
}

class UzzapRepository(
    private val database: UzzapDatabase,
    private val scope: CoroutineScope
) {
    private val userDao = database.userDao()
    private val contactDao = database.contactDao()
    private val conversationDao = database.conversationDao()
    private val messageDao = database.messageDao()
    private val chatroomDao = database.chatroomDao()

    val firestoreService = UzzapFirestoreService(scope)
    val firestoreSyncStatus: StateFlow<FirestoreSyncStatus> = firestoreService.syncStatus

    // Event bus for buzzer effect
    private val _buzzEvents = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val buzzEvents: SharedFlow<String> = _buzzEvents

    val profileFlow: Flow<UserProfileEntity?> = userDao.getProfileFlow()
    val contactsFlow: Flow<List<ContactEntity>> = contactDao.getAcceptedContactsFlow()
    val pendingRequestsFlow: Flow<List<ContactEntity>> = contactDao.getPendingRequestsFlow()
    val conversationsFlow: Flow<List<ConversationEntity>> = conversationDao.getAllConversationsFlow()
    val chatroomsFlow: Flow<List<ChatroomEntity>> = chatroomDao.getAllChatroomsFlow()

    fun initCloudSync() {
        scope.launch(Dispatchers.IO) {
            configureCloudSync()
        }
    }

    private suspend fun configureCloudSync() {
        try {
                // Wait for user profile
                val profile = userDao.getProfile()
                val myUsername = profile?.username ?: return

                // 1. Sync current user presence/profile to Firestore
                profile.let { firestoreService.syncUserProfile(it) }

                // 2. Seed initial chatrooms if needed & listen to cloud chatrooms
                val localRooms = chatroomDao.getAllChatroomsFlow().firstOrNull() ?: emptyList()
                if (localRooms.isNotEmpty()) {
                    firestoreService.seedInitialRoomsIfEmpty(localRooms)
                }
                firestoreService.listenToChatrooms { remoteRooms ->
                    scope.launch(Dispatchers.IO) {
                        val mergedRooms = remoteRooms.map { remoteRoom ->
                            val localRoom = chatroomDao.getChatroomById(remoteRoom.id)
                            mergeRemoteChatroom(remoteRoom, localRoom)
                        }
                        chatroomDao.insertAll(mergedRooms)
                    }
                }

                // 3. Listen to incoming 1-on-1 messages & BUZZes directed to this user
                firestoreService.listenToUserInbox(myUsername) { incoming ->
                    scope.launch(Dispatchers.IO) {
                        val existing = messageDao.getMessageById(incoming.id)
                        if (existing == null) {
                            messageDao.insertMessage(incoming)

                            // Ensure conversation entity exists
                            val existingConvo = conversationDao.getConversationById(incoming.conversationId)
                            if (existingConvo == null) {
                                val contact = contactDao.getContactByUsername(incoming.senderUsername)
                                val title = contact?.let { "${it.nickname} (${it.displayName})" } ?: incoming.senderDisplayName
                                val emoji = contact?.avatarEmoji ?: "💬"
                                val bg = contact?.avatarBgColor ?: 0xFF3F51B5
                                conversationDao.insertConversation(
                                    ConversationEntity(
                                        id = incoming.conversationId,
                                        type = "DIRECT",
                                        title = title,
                                        recipientUsername = incoming.senderUsername,
                                        avatarEmoji = emoji,
                                        avatarBgColor = bg,
                                        lastMessage = incoming.body,
                                        lastTimestamp = incoming.timestamp,
                                        unreadCount = 1,
                                        isPinned = false
                                    )
                                )
                            } else {
                                conversationDao.updateLastMessage(
                                    id = incoming.conversationId,
                                    lastMessage = incoming.body,
                                    timestamp = incoming.timestamp,
                                    unreadCount = existingConvo.unreadCount + 1
                                )
                            }

                            // Trigger BUZZ event if message is a buzz
                            if (incoming.type == MessageType.BUZZ) {
                                _buzzEvents.emit("⚡ BUZZ from ${incoming.senderDisplayName}!")
                            }
                        }
                    }
                }

                // 4. Listen to buddy presence updates from Firestore
                firestoreService.listenToUsersPresence { username, presence, statusMessage ->
                    scope.launch(Dispatchers.IO) {
                        contactDao.updatePresenceByUsername(username, presence, statusMessage)
                    }
                }

                // 5. Listen to incoming friend requests from Firestore
                firestoreService.listenToFriendRequests { newContact ->
                    scope.launch(Dispatchers.IO) {
                        val existing = contactDao.getContactByUsername(newContact.username)
                        if (existing == null) {
                            contactDao.insertContact(newContact)
                        }
                    }
                }

                // 6. Reflect the recipient's response in the sender's local buddy list.
                firestoreService.listenToOutgoingFriendRequests { username, status ->
                    scope.launch(Dispatchers.IO) {
                        when (status) {
                            FriendRequestStatus.PENDING -> Unit
                            FriendRequestStatus.ACCEPTED -> contactDao.updateFriendshipStateByUsername(
                                username,
                                FriendshipState.ACCEPTED
                            )
                            FriendRequestStatus.DECLINED -> contactDao.deleteContactByUsername(username)
                        }
                    }
                }
        } catch (e: Exception) {
            // Graceful fallback to local Room
        }
    }

    /** Re-establishes cloud listeners while Room remains the immediate source of truth. */
    suspend fun refreshCloudData() = withContext(Dispatchers.IO) {
        configureCloudSync()
    }

    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForConversationFlow(conversationId)
    }

    fun getRoomMessages(roomId: String): Flow<List<RoomMessageEntity>> {
        return chatroomDao.getRoomMessagesFlow(roomId)
    }

    fun enterRoom(roomId: String) {
        scope.launch(Dispatchers.IO) {
            val profile = userDao.getProfile()
            val myUsername = profile?.username ?: return@launch
            firestoreService.listenToRoomMessages(roomId, myUsername) { incomingRoomMsg ->
                scope.launch(Dispatchers.IO) {
                    val existing = chatroomDao.getRoomMessageById(incomingRoomMsg.id)
                    if (existing == null) {
                        chatroomDao.insertRoomMessage(incomingRoomMsg)
                    }
                }
            }
        }
    }

    fun leaveActiveRoom() {
        firestoreService.stopListeningToRoomMessages()
    }

    suspend fun updatePresence(status: UserPresence, statusMessage: String) {
        userDao.updatePresence(status, statusMessage)
        val profile = userDao.getProfile()
        if (profile != null) {
            firestoreService.syncUserProfile(profile)
        }
    }

    suspend fun updateVibration(enabled: Boolean) {
        userDao.updateVibration(enabled)
    }

    suspend fun updateProfile(profile: UserProfileEntity) {
        userDao.insertProfile(profile)
        firestoreService.syncUserProfile(profile)
    }

    suspend fun toggleFavoriteContact(contactId: String) {
        contactDao.toggleFavorite(contactId)
    }

    suspend fun acceptFriendRequest(contactId: String) {
        val contact = contactDao.getContactById(contactId) ?: return
        val myUsername = userDao.getProfile()?.username ?: return
        firestoreService.respondToFriendRequest(contact.username, myUsername, accepted = true).getOrThrow()
        contactDao.updateFriendshipState(contactId, FriendshipState.ACCEPTED)
    }

    suspend fun declineFriendRequest(contactId: String) {
        val contact = contactDao.getContactById(contactId) ?: return
        val myUsername = userDao.getProfile()?.username ?: return
        firestoreService.respondToFriendRequest(contact.username, myUsername, accepted = false).getOrThrow()
        contactDao.deleteContact(contactId)
    }

    suspend fun deleteContact(contactId: String) {
        contactDao.deleteContact(contactId)
    }

    suspend fun addContact(
        username: String,
        displayName: String,
        phoneNumber: String,
        category: ContactCategory
    ) {
        val cleanUsername = normalizeUsername(username)
        val cloudEnabled = com.example.UzzapApplication.isRealFirebaseConfigured
        val newContact = ContactEntity(
            id = "contact_$cleanUsername",
            username = cleanUsername,
            displayName = displayName.trim(),
            nickname = displayName.split(" ").firstOrNull() ?: displayName,
            phoneNumber = phoneNumber,
            presence = UserPresence.ONLINE,
            statusMessage = "Added via UZZ-APP \uD83D\uDCF1",
            category = category,
            friendshipState = if (cloudEnabled) {
                FriendshipState.PENDING_OUTGOING
            } else {
                FriendshipState.ACCEPTED
            },
            avatarEmoji = listOf("\uD83D\uDE0A", "\uD83E\uDD17", "\uD83D\uDC36", "\uD83C\uDF89", "\u2B50", "\uD83D\uDCBB").random(),
            avatarBgColor = listOf(0xFFE91E63, 0xFF3F51B5, 0xFF009688, 0xFFFF9800, 0xFF673AB7).random()
        )
        val myProfile = userDao.getProfile()
        if (cloudEnabled) {
            checkNotNull(myProfile) { "Complete your profile before adding a buddy." }
            firestoreService.sendFriendRequest(myProfile, cleanUsername).getOrThrow()
        } else if (!com.example.BuildConfig.ALLOW_DEMO_AUTH) {
            throw IllegalStateException("Cloud connection is required to add a buddy.")
        }
        contactDao.insertContact(newContact)
    }

    suspend fun startOrGetConversation(contact: ContactEntity): String {
        val myUsername = userDao.getProfile()?.username
            ?: throw IllegalStateException("Complete your profile before starting a conversation.")
        val convoId = directConversationId(myUsername, contact.username)
        val existing = conversationDao.getConversationById(convoId)
        if (existing != null) {
            return existing.id
        }

        val newConvo = ConversationEntity(
            id = convoId,
            type = "DIRECT",
            title = "${contact.nickname} (${contact.displayName})",
            recipientUsername = contact.username,
            avatarEmoji = contact.avatarEmoji,
            avatarBgColor = contact.avatarBgColor,
            lastMessage = "Started a conversation",
            lastTimestamp = System.currentTimeMillis(),
            unreadCount = 0,
            isPinned = false
        )
        conversationDao.insertConversation(newConvo)
        return convoId
    }

    suspend fun sendMessage(conversationId: String, text: String, replyToBody: String? = null) {
        val profile = userDao.getProfile()
        val myName = profile?.displayName ?: "Juan Dela Cruz"
        val myUsername = profile?.username ?: "juandelacruz"

        val msgId = "msg_${UUID.randomUUID().toString().take(8)}"
        val now = System.currentTimeMillis()

        val msg = MessageEntity(
            id = msgId,
            conversationId = conversationId,
            senderUsername = myUsername,
            senderDisplayName = myName,
            type = MessageType.TEXT,
            body = text,
            replyToBody = replyToBody,
            timestamp = now,
            status = MessageDeliveryStatus.SENDING,
            isFromMe = true
        )
        messageDao.insertMessage(msg)
        conversationDao.updateLastMessage(conversationId, text, now, 0)

        // Sync to Firestore & deliver to recipient
        val convo = conversationDao.getConversationById(conversationId)
        val recipient = convo?.recipientUsername ?: "uzzap_buddy"
        val result = firestoreService.sendDirectMessage(conversationId, recipient, msg)
        messageDao.updateStatus(
            msgId,
            if (result.isSuccess) MessageDeliveryStatus.SENT else MessageDeliveryStatus.FAILED
        )
        result.getOrThrow()
    }

    suspend fun sendBuzz(conversationId: String) {
        val profile = userDao.getProfile()
        val myName = profile?.displayName ?: "Juan Dela Cruz"
        val myUsername = profile?.username ?: "juandelacruz"

        val msgId = "msg_${UUID.randomUUID().toString().take(8)}"
        val now = System.currentTimeMillis()

        val msg = MessageEntity(
            id = msgId,
            conversationId = conversationId,
            senderUsername = myUsername,
            senderDisplayName = myName,
            type = MessageType.BUZZ,
            body = "BUZZED YOU!",
            timestamp = now,
            status = MessageDeliveryStatus.SENDING,
            isFromMe = true
        )
        messageDao.insertMessage(msg)
        conversationDao.updateLastMessage(conversationId, "\u26A1 BUZZED YOU!", now, 0)
        _buzzEvents.emit("Outgoing BUZZ sent!")

        val convo = conversationDao.getConversationById(conversationId)
        val recipient = convo?.recipientUsername ?: "uzzap_buddy"
        val result = firestoreService.sendDirectMessage(conversationId, recipient, msg)
        messageDao.updateStatus(
            msgId,
            if (result.isSuccess) MessageDeliveryStatus.SENT else MessageDeliveryStatus.FAILED
        )
        result.getOrThrow()
    }

    suspend fun markConversationRead(conversationId: String) {
        conversationDao.markAsRead(conversationId)
    }

    suspend fun deleteConversation(conversationId: String) {
        messageDao.clearHistory(conversationId)
        conversationDao.deleteConversation(conversationId)
    }

    suspend fun joinOrLeaveRoom(roomId: String, join: Boolean) {
        val room = chatroomDao.getChatroomById(roomId) ?: return
        chatroomDao.updateJoinState(roomId, join, if (join) 1 else -1)

        val profile = userDao.getProfile()
        val nickname = profile?.displayName?.split(" ")?.firstOrNull()?.ifBlank { null }
            ?: profile?.displayName?.ifBlank { null }
            ?: profile?.username
            ?: "Juan"

        val now = System.currentTimeMillis()
        val noticeText = if (join) "$nickname joins the chat" else "$nickname left the chat"

        val systemNotice = RoomMessageEntity(
            id = "rm_${UUID.randomUUID().toString().take(8)}",
            roomId = roomId,
            senderUsername = "System",
            senderRole = RoomRole.ADMIN,
            message = noticeText,
            timestamp = now,
            isSystem = true
        )
        chatroomDao.insertRoomMessage(systemNotice)
        firestoreService.sendRoomMembershipChange(
            roomId,
            systemNotice,
            if (join) 1 else -1
        ).getOrThrow()
    }

    suspend fun sendRoomMessage(roomId: String, text: String) {
        val profile = userDao.getProfile()
        val username = profile?.username ?: "juandelacruz"

        val msg = RoomMessageEntity(
            id = "rm_${UUID.randomUUID().toString().take(8)}",
            roomId = roomId,
            senderUsername = username,
            senderRole = RoomRole.MEMBER,
            message = text,
            timestamp = System.currentTimeMillis(),
            isFromMe = true,
            isSystem = false
        )
        chatroomDao.insertRoomMessage(msg)
        firestoreService.sendRoomMessage(roomId, msg).getOrThrow()
    }

    suspend fun createChatroom(name: String, topic: String, category: String) {
        val formattedName = if (name.startsWith("#")) name else "#$name"
        val newRoom = ChatroomEntity(
            id = "room_${UUID.randomUUID().toString().take(8)}",
            name = formattedName,
            topic = topic,
            category = category,
            chatterCount = 1,
            isJoined = true,
            userRole = RoomRole.OWNER
        )
        chatroomDao.insertChatroom(newRoom)

        val welcomeMsg = RoomMessageEntity(
            id = "rm_${UUID.randomUUID().toString().take(8)}",
            roomId = newRoom.id,
            senderUsername = "System",
            senderRole = RoomRole.ADMIN,
            message = "Room created: $formattedName ($category). Welcome!",
            timestamp = System.currentTimeMillis(),
            isSystem = true
        )
        chatroomDao.insertRoomMessage(welcomeMsg)

        firestoreService.createChatroom(newRoom).getOrThrow()
    }

    suspend fun signIn(usernameOrPhone: String, pin: String): Result<UserProfileEntity> {
        val result = firestoreService.signInWithFirestore(usernameOrPhone, pin)
        if (result.isSuccess) {
            val user = result.getOrThrow()
            userDao.insertProfile(user)
            initCloudSync()
            return result
        }

        val cleanInput = usernameOrPhone.trim().lowercase().replace("@uzzap.ph", "")

        // If connected to live Firebase and demo user doesn't exist yet, auto-provision on Firebase
        if (com.example.BuildConfig.ALLOW_DEMO_AUTH &&
            com.example.UzzapApplication.isRealFirebaseConfigured &&
            cleanInput == "juandelacruz"
        ) {
            val autoSignUpResult = firestoreService.signUpWithFirestore(
                username = "juandelacruz",
                displayName = "Juan Dela Cruz",
                phoneNumber = "+63 918 555 1014",
                password = pin.ifBlank { "password" },
                avatarEmoji = "😎",
                statusMessage = "Mabuhay! Connecting on UZZ-APP 🇵🇭"
            )
            if (autoSignUpResult.isSuccess) {
                val user = autoSignUpResult.getOrThrow()
                userDao.insertProfile(user)
                initCloudSync()
                return autoSignUpResult
            }
        }

        if (com.example.UzzapApplication.isRealFirebaseConfigured || !com.example.BuildConfig.ALLOW_DEMO_AUTH) {
            return result
        }

        // When cloud auth is unavailable or offline,
        // seamlessly fall back to local Room storage so the user can continue using Uzzap.
        val existingProfile = userDao.getProfile()
        val user = if (existingProfile != null && (existingProfile.username.equals(cleanInput, ignoreCase = true) || existingProfile.phoneNumber.contains(cleanInput))) {
            existingProfile
        } else {
            UserProfileEntity(
                id = "me",
                username = cleanInput.ifBlank { "juandelacruz" },
                displayName = if (cleanInput.isNotBlank() && cleanInput != "juandelacruz") {
                    cleanInput.replaceFirstChar { it.uppercase() }
                } else {
                    existingProfile?.displayName ?: "Juan Dela Cruz"
                },
                phoneNumber = existingProfile?.phoneNumber ?: "+63 918 555 1014",
                status = UserPresence.ONLINE,
                statusMessage = existingProfile?.statusMessage ?: "Mabuhay! Connecting on UZZ-APP 🇵🇭",
                avatarEmoji = existingProfile?.avatarEmoji ?: "😎",
                phoneVerified = true,
                vibrationEnabled = true
            )
        }
        userDao.insertProfile(user)
        return Result.success(user)
    }

    suspend fun signUp(
        username: String,
        displayName: String,
        phoneNumber: String,
        pin: String,
        avatarEmoji: String,
        statusMessage: String
    ): Result<UserProfileEntity> {
        val result = firestoreService.signUpWithFirestore(
            username = username,
            displayName = displayName,
            phoneNumber = phoneNumber,
            password = pin,
            avatarEmoji = avatarEmoji,
            statusMessage = statusMessage
        )
        if (result.isSuccess) {
            val user = result.getOrThrow()
            userDao.insertProfile(user)
            initCloudSync()
            return result
        }

        if (com.example.UzzapApplication.isRealFirebaseConfigured || !com.example.BuildConfig.ALLOW_DEMO_AUTH) {
            return result
        }

        // When cloud registration is unavailable or offline, create the user in local Room database
        val cleanUsername = username.trim().lowercase()
        val user = UserProfileEntity(
            id = "me",
            username = cleanUsername.ifBlank { "juandelacruz" },
            displayName = displayName.trim().ifBlank { "UZZ-APP User" },
            phoneNumber = phoneNumber.trim().ifBlank { "+63 918 555 1014" },
            status = UserPresence.ONLINE,
            statusMessage = if (statusMessage.isBlank()) "Chatting on UZZ-APP 🇵🇭" else statusMessage.trim(),
            avatarEmoji = avatarEmoji.ifBlank { "😎" },
            phoneVerified = false,
            vibrationEnabled = true
        )
        userDao.insertProfile(user)
        return Result.success(user)
    }

    suspend fun getContactByUsername(username: String): ContactEntity? {
        return contactDao.getContactByUsername(username)
    }

    suspend fun submitReport(target: String, reason: String, details: String) {
        val myProfile = userDao.getProfile()
        val myUsername = myProfile?.username ?: "anonymous"
        firestoreService.submitReport(myUsername, target, reason, details).getOrThrow()
    }

    suspend fun deleteAccountData(password: String) {
        val myProfile = userDao.getProfile()
        val myUsername = myProfile?.username
        if (myUsername != null) {
            firestoreService.deleteUserCloudData(myUsername, password).getOrThrow()
        }
        database.clearAllTables()
    }

    fun close() {
        firestoreService.cleanUp()
    }
}
