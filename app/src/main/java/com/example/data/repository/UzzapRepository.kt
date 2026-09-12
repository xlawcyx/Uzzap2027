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
import com.example.data.remote.firestore.UzzapFirestoreService
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
                val myUsername = profile?.username ?: "juandelacruz"

                // 1. Sync current user presence/profile to Firestore
                profile?.let { firestoreService.syncUserProfile(it) }

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
                firestoreService.listenToFriendRequests(myUsername) { newContact ->
                    scope.launch(Dispatchers.IO) {
                        val existing = contactDao.getContactByUsername(newContact.username)
                        if (existing == null) {
                            contactDao.insertContact(newContact)
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
            val myUsername = profile?.username ?: "juandelacruz"
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
        contactDao.updateFriendshipState(contactId, FriendshipState.ACCEPTED)
    }

    suspend fun declineFriendRequest(contactId: String) {
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
        val newContact = ContactEntity(
            id = "contact_${UUID.randomUUID().toString().take(8)}",
            username = username.lowercase().trim(),
            displayName = displayName.trim(),
            nickname = displayName.split(" ").firstOrNull() ?: displayName,
            phoneNumber = phoneNumber,
            presence = UserPresence.ONLINE,
            statusMessage = "Added via Uzzap \uD83D\uDCF1",
            category = category,
            friendshipState = FriendshipState.ACCEPTED,
            avatarEmoji = listOf("\uD83D\uDE0A", "\uD83E\uDD17", "\uD83D\uDC36", "\uD83C\uDF89", "\u2B50", "\uD83D\uDCBB").random(),
            avatarBgColor = listOf(0xFFE91E63, 0xFF3F51B5, 0xFF009688, 0xFFFF9800, 0xFF673AB7).random()
        )
        contactDao.insertContact(newContact)

        // Sync friend request to Firestore
        val myProfile = userDao.getProfile()
        if (myProfile != null) {
            firestoreService.sendFriendRequest(myProfile, username.lowercase().trim())
        }
    }

    suspend fun startOrGetConversation(contact: ContactEntity): String {
        val convoId = "convo_${contact.username}"
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
        firestoreService.sendRoomMessage(roomId, systemNotice)
        firestoreService.updateRoomChatterCount(roomId, if (join) 1 else -1)
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
        firestoreService.sendRoomMessage(roomId, msg)
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

        firestoreService.createChatroom(newRoom)
    }

    suspend fun signIn(usernameOrPhone: String, pin: String): Result<UserProfileEntity> {
        val result = firestoreService.signInWithFirestore(usernameOrPhone, pin)
        if (result.isSuccess) {
            val user = result.getOrThrow()
            userDao.insertProfile(user)
            initCloudSync()
        }
        return result
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
        }
        return result
    }

    suspend fun getContactByUsername(username: String): ContactEntity? {
        return contactDao.getContactByUsername(username)
    }

    suspend fun submitReport(target: String, reason: String, details: String) {
        val myProfile = userDao.getProfile()
        val myUsername = myProfile?.username ?: "anonymous"
        firestoreService.submitReport(myUsername, target, reason, details)
    }

    suspend fun deleteAccountData() {
        val myProfile = userDao.getProfile()
        val myUsername = myProfile?.username
        if (myUsername != null) {
            firestoreService.deleteUserCloudData(myUsername).getOrThrow()
        }
        database.clearAllTables()
    }

    fun close() {
        firestoreService.cleanUp()
    }
}
