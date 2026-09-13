package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserPresence(val label: String) {
    ONLINE("Online"),
    AWAY("Away"),
    BUSY("Busy"),
    INVISIBLE("Invisible"),
    OFFLINE("Offline")
}

enum class ContactCategory(val displayName: String) {
    BUDDIES("Buddies"),
    CHATTERBOX("Chatterbox"),
    MOST_FREQUENT("Most Frequent"),
    OTHER("Other Contacts")
}

enum class FriendshipState {
    ACCEPTED,
    PENDING_INCOMING,
    PENDING_OUTGOING,
    BLOCKED
}

enum class MessageType {
    TEXT,
    BUZZ,
    SYSTEM
}

enum class MessageDeliveryStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED
}

enum class RoomRole(val title: String) {
    OWNER("Owner"),
    ADMIN("Admin"),
    MODERATOR("Mod"),
    MEMBER("Member"),
    GUEST("Guest")
}

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "me",
    val username: String = "uzzap_user",
    val displayName: String = "Juan Dela Cruz",
    val phoneNumber: String = "+63 918 555 1014",
    val status: UserPresence = UserPresence.ONLINE,
    val statusMessage: String = "Chatting on UZZ-APP v1.0.14 \uD83D\uDCF1",
    val avatarEmoji: String = "\uD83D\uDE0E",
    val phoneVerified: Boolean = true,
    val vibrationEnabled: Boolean = true
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val username: String,
    val displayName: String,
    val nickname: String,
    val phoneNumber: String,
    val presence: UserPresence,
    val statusMessage: String,
    val category: ContactCategory,
    val friendshipState: FriendshipState = FriendshipState.ACCEPTED,
    val avatarEmoji: String,
    val avatarBgColor: Long,
    val isFavorite: Boolean = false,
    val lastSeen: String = "Online now"
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val type: String = "DIRECT", // "DIRECT" or "GROUP"
    val title: String,
    val recipientUsername: String,
    val avatarEmoji: String,
    val avatarBgColor: Long,
    val lastMessage: String,
    val lastTimestamp: Long,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderUsername: String,
    val senderDisplayName: String,
    val type: MessageType = MessageType.TEXT,
    val body: String,
    val replyToBody: String? = null,
    val timestamp: Long,
    val status: MessageDeliveryStatus = MessageDeliveryStatus.READ,
    val isFromMe: Boolean
)

@Entity(tableName = "chatrooms")
data class ChatroomEntity(
    @PrimaryKey val id: String,
    val name: String,
    val topic: String,
    val category: String,
    val chatterCount: Int,
    val isJoined: Boolean = false,
    val userRole: RoomRole = RoomRole.MEMBER
)

@Entity(tableName = "room_messages")
data class RoomMessageEntity(
    @PrimaryKey val id: String,
    val roomId: String,
    val senderUsername: String,
    val senderRole: RoomRole,
    val message: String,
    val timestamp: Long,
    val isFromMe: Boolean = false,
    val isSystem: Boolean = false
)
