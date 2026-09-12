package com.example.data.remote.firestore

import android.util.Log
import com.example.data.model.ChatroomEntity
import com.example.data.model.ContactCategory
import com.example.data.model.ContactEntity
import com.example.data.model.FriendshipState
import com.example.data.model.MessageDeliveryStatus
import com.example.data.model.MessageEntity
import com.example.data.model.MessageType
import com.example.data.model.RoomMessageEntity
import com.example.data.model.RoomRole
import com.example.data.model.UserPresence
import com.example.data.model.UserProfileEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

internal const val MIN_PASSWORD_LENGTH = 6
private const val AUTH_EMAIL_DOMAIN = "accounts.uzzap.app"

internal fun normalizeUsername(username: String): String =
    username.trim().lowercase(Locale.ROOT).removePrefix("@")

internal fun authEmailForUsername(username: String): String =
    "${normalizeUsername(username)}@$AUTH_EMAIL_DOMAIN"

enum class FirestoreSyncStatus(val label: String) {
    INITIALIZING("Connecting to Cloud..."),
    CONNECTED("Connected to Firestore"),
    SYNCING("Syncing with Cloud..."),
    OFFLINE_CACHE("Cloud Offline (Cached)"),
    ERROR("Cloud Sync Warning")
}

class UzzapFirestoreService(
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "UzzapFirestore"
        private const val USERS_COLLECTION = "users"
        private const val PUBLIC_PROFILES_COLLECTION = "public_profiles"
        private const val CHATROOMS_COLLECTION = "chatrooms"
        private const val ROOM_MESSAGES_SUBCOLLECTION = "messages"
        private const val CONVERSATIONS_COLLECTION = "conversations"
        private const val CONVO_MESSAGES_SUBCOLLECTION = "messages"
        private const val INBOX_SUBCOLLECTION = "inbox"
        private const val FRIEND_REQUESTS_COLLECTION = "friend_requests"
    }

    private val firestore: FirebaseFirestore by lazy {
        try {
            val db = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
            db.firestoreSettings = settings
            db
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firestore with persistent cache: ${e.message}", e)
            FirebaseFirestore.getInstance()
        }
    }

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val _syncStatus = MutableStateFlow(FirestoreSyncStatus.INITIALIZING)
    val syncStatus: StateFlow<FirestoreSyncStatus> = _syncStatus.asStateFlow()

    private var presenceListener: ListenerRegistration? = null
    private var activeRoomListener: ListenerRegistration? = null
    private var inboxListener: ListenerRegistration? = null
    private var friendRequestsListener: ListenerRegistration? = null
    private var chatroomsListener: ListenerRegistration? = null

    init {
        checkConnectivity()
    }

    private fun checkConnectivity() {
        scope.launch(Dispatchers.IO) {
            try {
                // Quick ping document to verify connectivity
                firestore.collection(USERS_COLLECTION).document("_ping").get()
                    .addOnSuccessListener {
                        _syncStatus.value = FirestoreSyncStatus.CONNECTED
                    }
                    .addOnFailureListener {
                        _syncStatus.value = FirestoreSyncStatus.OFFLINE_CACHE
                    }
            } catch (e: Exception) {
                _syncStatus.value = FirestoreSyncStatus.OFFLINE_CACHE
            }
        }
    }

    // ==========================================
    // USER AUTHENTICATION & PROFILE SYNC
    // ==========================================

    suspend fun signUpWithFirestore(
        username: String,
        displayName: String,
        phoneNumber: String,
        password: String,
        avatarEmoji: String,
        statusMessage: String
    ): Result<UserProfileEntity> {
        val cleanUsername = normalizeUsername(username)
        if (cleanUsername.length < 3) {
            return Result.failure(IllegalArgumentException("Username must be at least 3 characters."))
        }
        if (!cleanUsername.matches(Regex("^[a-z0-9_.]+$"))) {
            return Result.failure(IllegalArgumentException("Username can only contain letters, numbers, underscores and dots."))
        }
        if (cleanUsername.length > 32 || cleanUsername.startsWith(".") ||
            cleanUsername.endsWith(".") || ".." in cleanUsername
        ) {
            return Result.failure(IllegalArgumentException("Username format is invalid."))
        }
        if (displayName.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your display name."))
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            return Result.failure(
                IllegalArgumentException("Password must be at least $MIN_PASSWORD_LENGTH characters.")
            )
        }

        var createdUser: FirebaseUser? = null
        return try {
            val docRef = firestore.collection(USERS_COLLECTION).document(cleanUsername)
            val authResult = auth.createUserWithEmailAndPassword(
                authEmailForUsername(cleanUsername),
                password
            ).await()
            val authUser = authResult.user
                ?: return Result.failure(IllegalStateException("Firebase did not create an account."))
            createdUser = authUser

            val finalStatusMsg = if (statusMessage.isBlank()) "Chatting on Uzzap \uD83D\uDCF1" else statusMessage.trim()
            val newProfile = UserProfileEntity(
                id = "me",
                username = cleanUsername,
                displayName = displayName.trim(),
                phoneNumber = phoneNumber.trim(),
                status = UserPresence.ONLINE,
                statusMessage = finalStatusMsg,
                avatarEmoji = avatarEmoji.ifBlank { "\uD83D\uDE0A" },
                phoneVerified = false,
                vibrationEnabled = true
            )

            val userData = hashMapOf<String, Any>(
                "authUid" to authUser.uid,
                "username" to cleanUsername,
                "displayName" to displayName.trim(),
                "phoneNumber" to phoneNumber.trim(),
                "status" to UserPresence.ONLINE.name,
                "statusMessage" to finalStatusMsg,
                "avatarEmoji" to newProfile.avatarEmoji,
                "phoneVerified" to false,
                "vibrationEnabled" to true,
                "createdAt" to System.currentTimeMillis()
            )
            val publicProfileData = hashMapOf<String, Any>(
                "authUid" to authUser.uid,
                "username" to cleanUsername,
                "displayName" to displayName.trim(),
                "status" to UserPresence.ONLINE.name,
                "statusMessage" to finalStatusMsg,
                "avatarEmoji" to newProfile.avatarEmoji,
                "lastSeen" to FieldValue.serverTimestamp()
            )

            firestore.batch()
                .set(docRef, userData)
                .set(
                    firestore.collection(PUBLIC_PROFILES_COLLECTION).document(cleanUsername),
                    publicProfileData
                )
                .commit()
                .await()
            createdUser = null
            _syncStatus.value = FirestoreSyncStatus.CONNECTED
            Log.d(TAG, "User registered in Firestore: $cleanUsername")
            Result.success(newProfile)
        } catch (e: Exception) {
            try {
                createdUser?.delete()?.await()
            } catch (cleanupError: Exception) {
                Log.w(TAG, "Could not roll back incomplete Firebase account creation", cleanupError)
            }
            Log.e(TAG, "Failed signUpWithFirestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithFirestore(
        usernameOrPhone: String,
        password: String
    ): Result<UserProfileEntity> {
        val cleanInput = normalizeUsername(usernameOrPhone)
        if (cleanInput.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your username."))
        }
        if (password.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your password."))
        }

        return try {
            val authResult = auth.signInWithEmailAndPassword(
                authEmailForUsername(cleanInput),
                password
            ).await()
            val authUser = authResult.user
                ?: return Result.failure(IllegalStateException("Firebase did not return an authenticated account."))

            val userDocRef = firestore.collection(USERS_COLLECTION).document(cleanInput)
            val targetDoc = userDocRef.get().await()

            if (targetDoc.exists()) {
                val ownerUid = targetDoc.getString("authUid")
                if (ownerUid != null && ownerUid != authUser.uid) {
                    auth.signOut()
                    return Result.failure(IllegalStateException("The profile does not belong to this account."))
                }

                val username = targetDoc.getString("username") ?: cleanInput
                val displayName = targetDoc.getString("displayName") ?: username
                val phoneNumber = targetDoc.getString("phoneNumber") ?: ""
                val statusMsg = targetDoc.getString("statusMessage") ?: "Chatting on Uzzap \uD83D\uDCF1"
                val avatarEmoji = targetDoc.getString("avatarEmoji") ?: "\uD83D\uDE0E"
                val phoneVerified = targetDoc.getBoolean("phoneVerified") ?: true
                val vibrationEnabled = targetDoc.getBoolean("vibrationEnabled") ?: true

                val profile = UserProfileEntity(
                    id = "me",
                    username = username,
                    displayName = displayName,
                    phoneNumber = phoneNumber,
                    status = UserPresence.ONLINE,
                    statusMessage = statusMsg,
                    avatarEmoji = avatarEmoji,
                    phoneVerified = phoneVerified,
                    vibrationEnabled = vibrationEnabled
                )

                targetDoc.reference.update(
                    mapOf(
                        "authUid" to authUser.uid,
                        "status" to UserPresence.ONLINE.name,
                        "lastSeen" to FieldValue.serverTimestamp()
                    )
                ).await()
                _syncStatus.value = FirestoreSyncStatus.CONNECTED
                Result.success(profile)
            } else {
                auth.signOut()
                Result.failure(
                    IllegalStateException("No profile found for '@$cleanInput'. Please create an account.")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "signInWithFirestore exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun syncUserProfile(profile: UserProfileEntity) {
        scope.launch(Dispatchers.IO) {
            try {
                _syncStatus.value = FirestoreSyncStatus.SYNCING
                val userData = hashMapOf<String, Any>(
                    "authUid" to (auth.currentUser?.uid ?: return@launch),
                    "username" to profile.username,
                    "displayName" to profile.displayName,
                    "phoneNumber" to profile.phoneNumber,
                    "status" to profile.status.name,
                    "statusMessage" to profile.statusMessage,
                    "avatarEmoji" to profile.avatarEmoji,
                    "lastSeen" to FieldValue.serverTimestamp(),
                    "phoneVerified" to profile.phoneVerified,
                    "vibrationEnabled" to profile.vibrationEnabled
                )
                val publicProfileData = hashMapOf<String, Any>(
                    "authUid" to (auth.currentUser?.uid ?: return@launch),
                    "username" to profile.username,
                    "displayName" to profile.displayName,
                    "status" to profile.status.name,
                    "statusMessage" to profile.statusMessage,
                    "avatarEmoji" to profile.avatarEmoji,
                    "lastSeen" to FieldValue.serverTimestamp()
                )

                firestore.batch()
                    .set(
                        firestore.collection(USERS_COLLECTION).document(profile.username),
                        userData,
                        SetOptions.merge()
                    )
                    .set(
                        firestore.collection(PUBLIC_PROFILES_COLLECTION).document(profile.username),
                        publicProfileData,
                        SetOptions.merge()
                    )
                    .commit()
                    .addOnSuccessListener {
                        _syncStatus.value = FirestoreSyncStatus.CONNECTED
                        Log.d(TAG, "User profile synced to Firestore: ${profile.username}")
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Failed syncing user profile: ${e.message}")
                        _syncStatus.value = FirestoreSyncStatus.OFFLINE_CACHE
                    }
            } catch (e: Exception) {
                Log.w(TAG, "Error in syncUserProfile: ${e.message}")
            }
        }
    }

    fun listenToUsersPresence(
        onPresenceChanged: (username: String, presence: UserPresence, statusMessage: String) -> Unit
    ) {
        presenceListener?.remove()
        try {
            presenceListener = firestore.collection(PUBLIC_PROFILES_COLLECTION)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "listenToUsersPresence error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        for (doc in snapshot.documentChanges) {
                            val username = doc.document.getString("username") ?: doc.document.id
                            val statusStr = doc.document.getString("status") ?: "ONLINE"
                            val statusMsg = doc.document.getString("statusMessage") ?: ""
                            val presence = try {
                                UserPresence.valueOf(statusStr)
                            } catch (e: Exception) {
                                UserPresence.ONLINE
                            }
                            onPresenceChanged(username, presence, statusMsg)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Error setting up presence listener: ${e.message}")
        }
    }

    // ==========================================
    // CHATROOMS SYNC
    // ==========================================

    fun seedInitialRoomsIfEmpty(initialRooms: List<ChatroomEntity>) {
        scope.launch(Dispatchers.IO) {
            try {
                val creatorUid = auth.currentUser?.uid ?: return@launch
                val snapshot = firestore.collection(CHATROOMS_COLLECTION).limit(1).get().await()
                if (snapshot.isEmpty) {
                    Log.d(TAG, "Firestore chatrooms collection is empty. Seeding initial rooms...")
                    val batch = firestore.batch()
                    initialRooms.forEach { room ->
                        val docRef = firestore.collection(CHATROOMS_COLLECTION).document(room.id)
                        val data = hashMapOf<String, Any>(
                            "id" to room.id,
                            "name" to room.name,
                            "topic" to room.topic,
                            "category" to room.category,
                            "chatterCount" to room.chatterCount,
                            "createdByUid" to creatorUid,
                            "createdAt" to System.currentTimeMillis()
                        )
                        batch.set(docRef, data, SetOptions.merge())
                    }
                    batch.commit().await()
                    Log.d(TAG, "Successfully seeded ${initialRooms.size} chatrooms to Firestore.")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not check/seed chatrooms: ${e.message}")
            }
        }
    }

    fun listenToChatrooms(onRoomsUpdated: (List<ChatroomEntity>) -> Unit) {
        chatroomsListener?.remove()
        try {
            chatroomsListener = firestore.collection(CHATROOMS_COLLECTION)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "listenToChatrooms error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val rooms = snapshot.documents.mapNotNull { doc ->
                            val id = doc.getString("id") ?: doc.id
                            val name = doc.getString("name") ?: return@mapNotNull null
                            val topic = doc.getString("topic") ?: ""
                            val category = doc.getString("category") ?: "General"
                            val chatterCount = (doc.getLong("chatterCount") ?: 1L).toInt()
                            ChatroomEntity(
                                id = id,
                                name = name,
                                topic = topic,
                                category = category,
                                chatterCount = chatterCount,
                                isJoined = false,
                                userRole = RoomRole.MEMBER
                            )
                        }
                        if (rooms.isNotEmpty()) {
                            onRoomsUpdated(rooms)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Error registering chatrooms listener: ${e.message}")
        }
    }

    fun createChatroom(room: ChatroomEntity) {
        scope.launch(Dispatchers.IO) {
            try {
                val creatorUid = auth.currentUser?.uid ?: return@launch
                val data = hashMapOf<String, Any>(
                    "id" to room.id,
                    "name" to room.name,
                    "topic" to room.topic,
                    "category" to room.category,
                    "chatterCount" to room.chatterCount,
                    "createdByUid" to creatorUid,
                    "createdAt" to System.currentTimeMillis()
                )
                firestore.collection(CHATROOMS_COLLECTION)
                    .document(room.id)
                    .set(data, SetOptions.merge())
                    .await()
                Log.d(TAG, "Created room in Firestore: ${room.name}")
            } catch (e: Exception) {
                Log.w(TAG, "Failed creating room in Firestore: ${e.message}")
            }
        }
    }

    fun updateRoomChatterCount(roomId: String, delta: Int) {
        scope.launch(Dispatchers.IO) {
            try {
                firestore.collection(CHATROOMS_COLLECTION)
                    .document(roomId)
                    .update("chatterCount", FieldValue.increment(delta.toLong()))
            } catch (e: Exception) {
                Log.w(TAG, "Failed updating chatter count: ${e.message}")
            }
        }
    }

    fun sendRoomMessage(roomId: String, message: RoomMessageEntity) {
        scope.launch(Dispatchers.IO) {
            try {
                val senderUid = auth.currentUser?.uid ?: return@launch
                val data = hashMapOf<String, Any>(
                    "id" to message.id,
                    "roomId" to roomId,
                    "senderUsername" to message.senderUsername,
                    "senderUid" to senderUid,
                    "senderRole" to message.senderRole.name,
                    "message" to message.message,
                    "timestamp" to message.timestamp,
                    "isSystem" to message.isSystem
                )
                firestore.collection(CHATROOMS_COLLECTION)
                    .document(roomId)
                    .collection(ROOM_MESSAGES_SUBCOLLECTION)
                    .document(message.id)
                    .set(data)
                    .await()
                Log.d(TAG, "Room message uploaded to Firestore: ${message.id}")
            } catch (e: Exception) {
                Log.w(TAG, "Failed sending room message to Firestore: ${e.message}")
            }
        }
    }

    fun listenToRoomMessages(
        roomId: String,
        currentUsername: String,
        onNewMessage: (RoomMessageEntity) -> Unit
    ) {
        activeRoomListener?.remove()
        try {
            activeRoomListener = firestore.collection(CHATROOMS_COLLECTION)
                .document(roomId)
                .collection(ROOM_MESSAGES_SUBCOLLECTION)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "listenToRoomMessages error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        for (change in snapshot.documentChanges) {
                            val doc = change.document
                            val msgId = doc.getString("id") ?: doc.id
                            val sender = doc.getString("senderUsername") ?: "unknown"
                            val roleStr = doc.getString("senderRole") ?: "MEMBER"
                            val text = doc.getString("message") ?: ""
                            val time = doc.getLong("timestamp") ?: System.currentTimeMillis()
                            val isSystem = doc.getBoolean("isSystem") ?: false
                            val role = try {
                                RoomRole.valueOf(roleStr)
                            } catch (e: Exception) {
                                RoomRole.MEMBER
                            }

                            val entity = RoomMessageEntity(
                                id = msgId,
                                roomId = roomId,
                                senderUsername = sender,
                                senderRole = role,
                                message = text,
                                timestamp = time,
                                isFromMe = (sender == currentUsername),
                                isSystem = isSystem
                            )
                            onNewMessage(entity)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Error listening to room messages: ${e.message}")
        }
    }

    fun stopListeningToRoomMessages() {
        activeRoomListener?.remove()
        activeRoomListener = null
    }

    // ==========================================
    // 1-ON-1 DIRECT MESSAGES & BUZZ
    // ==========================================

    suspend fun sendDirectMessage(
        conversationId: String,
        recipientUsername: String,
        message: MessageEntity
    ): Result<Unit> = try {
        val senderUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Sign in before sending messages."))
        val recipientUid = firestore.collection(PUBLIC_PROFILES_COLLECTION)
            .document(recipientUsername)
            .get()
            .await()
            .getString("authUid")
            ?: return Result.failure(IllegalStateException("The recipient must sign in again before receiving messages."))
        val msgData = hashMapOf<String, Any>(
            "id" to message.id,
            "conversationId" to conversationId,
            "senderUsername" to message.senderUsername,
            "senderDisplayName" to message.senderDisplayName,
            "recipientUsername" to recipientUsername,
            "senderUid" to senderUid,
            "recipientUid" to recipientUid,
            "type" to message.type.name,
            "body" to message.body,
            "timestamp" to message.timestamp,
            "status" to MessageDeliveryStatus.SENT.name
        )
        message.replyToBody?.let { msgData["replyToBody"] = it }

        val messageRef = firestore.collection(CONVERSATIONS_COLLECTION)
            .document(conversationId)
            .collection(CONVO_MESSAGES_SUBCOLLECTION)
            .document(message.id)

        val convoData = hashMapOf<String, Any>(
            "id" to conversationId,
            "lastMessage" to message.body,
            "lastTimestamp" to message.timestamp,
            "lastSender" to message.senderUsername,
            "lastSenderUid" to senderUid,
            "participants" to listOf(message.senderUsername, recipientUsername),
            "participantUids" to listOf(senderUid, recipientUid)
        )
        val conversationRef = firestore.collection(CONVERSATIONS_COLLECTION)
            .document(conversationId)

        val inboxRef = firestore.collection(USERS_COLLECTION)
            .document(recipientUsername)
            .collection(INBOX_SUBCOLLECTION)
            .document(message.id)

        // Commit the server history, preview, and recipient inbox atomically so
        // optimistic local state cannot be acknowledged after a partial write.
        firestore.batch()
            .set(messageRef, msgData)
            .set(conversationRef, convoData, SetOptions.merge())
            .set(inboxRef, msgData)
            .commit()
            .await()

        Log.d(TAG, "Sent direct message to $recipientUsername via Firestore")
        Result.success(Unit)
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        Log.w(TAG, "Failed sending direct message to Firestore: ${error.message}")
        Result.failure(error)
    }

    fun listenToUserInbox(
        currentUsername: String,
        onIncomingMessage: (MessageEntity) -> Unit
    ) {
        inboxListener?.remove()
        try {
            inboxListener = firestore.collection(USERS_COLLECTION)
                .document(currentUsername)
                .collection(INBOX_SUBCOLLECTION)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "listenToUserInbox error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        for (doc in snapshot.documents) {
                            val id = doc.getString("id") ?: doc.id
                            val convoId = doc.getString("conversationId") ?: "convo_${doc.getString("senderUsername")}"
                            val senderUser = doc.getString("senderUsername") ?: "uzzap_buddy"
                            val senderName = doc.getString("senderDisplayName") ?: senderUser
                            val typeStr = doc.getString("type") ?: "TEXT"
                            val body = doc.getString("body") ?: ""
                            val replyTo = doc.getString("replyToBody")
                            val time = doc.getLong("timestamp") ?: System.currentTimeMillis()

                            val msgType = try {
                                MessageType.valueOf(typeStr)
                            } catch (e: Exception) {
                                MessageType.TEXT
                            }

                            val incomingMsg = MessageEntity(
                                id = id,
                                conversationId = convoId,
                                senderUsername = senderUser,
                                senderDisplayName = senderName,
                                type = msgType,
                                body = body,
                                replyToBody = replyTo,
                                timestamp = time,
                                status = MessageDeliveryStatus.READ,
                                isFromMe = false
                            )
                            onIncomingMessage(incomingMsg)

                            // Clear processed inbox message document
                            doc.reference.delete()
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Error listening to inbox: ${e.message}")
        }
    }

    // ==========================================
    // FRIEND REQUESTS SYNC
    // ==========================================

    fun sendFriendRequest(fromUser: UserProfileEntity, toUsername: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val senderUid = auth.currentUser?.uid ?: return@launch
                val recipientUid = firestore.collection(PUBLIC_PROFILES_COLLECTION)
                    .document(toUsername)
                    .get()
                    .await()
                    .getString("authUid")
                    ?: return@launch
                val requestId = "${fromUser.username}_to_${toUsername}"
                val reqData = hashMapOf<String, Any>(
                    "id" to requestId,
                    "fromUsername" to fromUser.username,
                    "fromDisplayName" to fromUser.displayName,
                    "toUsername" to toUsername,
                    "fromUid" to senderUid,
                    "toUid" to recipientUid,
                    "status" to "PENDING",
                    "timestamp" to System.currentTimeMillis()
                )
                firestore.collection(FRIEND_REQUESTS_COLLECTION)
                    .document(requestId)
                    .set(reqData, SetOptions.merge())
                    .await()
                Log.d(TAG, "Sent friend request to $toUsername")
            } catch (e: Exception) {
                Log.w(TAG, "Failed sending friend request to Firestore: ${e.message}")
            }
        }
    }

    fun listenToFriendRequests(
        myUsername: String,
        onNewRequest: (ContactEntity) -> Unit
    ) {
        friendRequestsListener?.remove()
        try {
            friendRequestsListener = firestore.collection(FRIEND_REQUESTS_COLLECTION)
                .whereEqualTo("toUsername", myUsername)
                .whereEqualTo("status", "PENDING")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "listenToFriendRequests error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        for (doc in snapshot.documents) {
                            val fromUser = doc.getString("fromUsername") ?: continue
                            val fromName = doc.getString("fromDisplayName") ?: fromUser
                            val contact = ContactEntity(
                                id = "contact_$fromUser",
                                username = fromUser,
                                displayName = fromName,
                                nickname = fromName.split(" ").firstOrNull() ?: fromName,
                                phoneNumber = "",
                                presence = UserPresence.ONLINE,
                                statusMessage = "Wants to connect on Uzzap \uD83D\uDCF1",
                                category = ContactCategory.BUDDIES,
                                friendshipState = FriendshipState.PENDING_INCOMING,
                                avatarEmoji = "\uD83D\uDE0A",
                                avatarBgColor = 0xFF3F51B5,
                                isFavorite = false,
                                lastSeen = "Online now"
                            )
                            onNewRequest(contact)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Error listening to friend requests: ${e.message}")
        }
    }

    fun submitReport(
        reporterUsername: String,
        target: String,
        reason: String,
        details: String
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val reportData = hashMapOf(
                    "reporterUid" to (auth.currentUser?.uid ?: return@launch),
                    "reporter" to reporterUsername,
                    "target" to target,
                    "reason" to reason,
                    "details" to details,
                    "timestamp" to System.currentTimeMillis()
                )
                firestore.collection("ugc_reports").add(reportData)
            } catch (e: Exception) {
                Log.e(TAG, "Error submitting report", e)
            }
        }
    }

    suspend fun deleteUserCloudData(username: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(IllegalStateException("No authenticated account to delete."))
            firestore.batch()
                .delete(firestore.collection(USERS_COLLECTION).document(username))
                .delete(firestore.collection(PUBLIC_PROFILES_COLLECTION).document(username))
                .commit()
                .await()
            currentUser.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user cloud data", e)
            Result.failure(e)
        }
    }

    fun cleanUp() {
        presenceListener?.remove()
        presenceListener = null
        activeRoomListener?.remove()
        inboxListener?.remove()
        friendRequestsListener?.remove()
        chatroomsListener?.remove()
        activeRoomListener = null
        inboxListener = null
        friendRequestsListener = null
        chatroomsListener = null
    }
}
