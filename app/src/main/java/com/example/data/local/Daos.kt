package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChatroomEntity
import com.example.data.model.ContactCategory
import com.example.data.model.ContactEntity
import com.example.data.model.ConversationEntity
import com.example.data.model.FriendshipState
import com.example.data.model.MessageDeliveryStatus
import com.example.data.model.MessageEntity
import com.example.data.model.RoomMessageEntity
import com.example.data.model.UserPresence
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 'me' LIMIT 1")
    fun getProfileFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 'me' LIMIT 1")
    suspend fun getProfile(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET status = :status, statusMessage = :statusMessage WHERE id = 'me'")
    suspend fun updatePresence(status: UserPresence, statusMessage: String)

    @Query("UPDATE user_profile SET vibrationEnabled = :enabled WHERE id = 'me'")
    suspend fun updateVibration(enabled: Boolean)
}

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY isFavorite DESC, displayName ASC")
    fun getAllContactsFlow(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE friendshipState = 'ACCEPTED' ORDER BY isFavorite DESC, displayName ASC")
    fun getAcceptedContactsFlow(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE friendshipState = 'PENDING_INCOMING' ORDER BY displayName ASC")
    fun getPendingRequestsFlow(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
    suspend fun getContactById(id: String): ContactEntity?

    @Query("SELECT * FROM contacts WHERE username = :username LIMIT 1")
    suspend fun getContactByUsername(username: String): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<ContactEntity>)

    @Query("UPDATE contacts SET presence = :presence, statusMessage = :statusMessage WHERE id = :id")
    suspend fun updatePresence(id: String, presence: UserPresence, statusMessage: String)

    @Query("UPDATE contacts SET presence = :presence, statusMessage = :statusMessage WHERE username = :username")
    suspend fun updatePresenceByUsername(username: String, presence: UserPresence, statusMessage: String)

    @Query("UPDATE contacts SET friendshipState = :state WHERE id = :id")
    suspend fun updateFriendshipState(id: String, state: FriendshipState)

    @Query("UPDATE contacts SET friendshipState = :state WHERE username = :username")
    suspend fun updateFriendshipStateByUsername(username: String, state: FriendshipState)

    @Query("UPDATE contacts SET friendshipState = :newState WHERE username = :username AND friendshipState = :expectedState")
    suspend fun updateFriendshipStateIf(
        username: String,
        expectedState: FriendshipState,
        newState: FriendshipState
    )

    @Query("UPDATE contacts SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: String)

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteContact(id: String)

    @Query("DELETE FROM contacts WHERE username = :username AND friendshipState = :expectedState")
    suspend fun deleteContactIfState(username: String, expectedState: FriendshipState)
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY isPinned DESC, lastTimestamp DESC")
    fun getAllConversationsFlow(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(conversations: List<ConversationEntity>)

    @Query("UPDATE conversations SET lastMessage = :lastMessage, lastTimestamp = :timestamp, unreadCount = :unreadCount WHERE id = :id")
    suspend fun updateLastMessage(id: String, lastMessage: String, timestamp: Long, unreadCount: Int = 0)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversation(id: String)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversationFlow(conversationId: String): Flow<List<MessageEntity>>

    @Query("SELECT id FROM messages WHERE id = :id LIMIT 1")
    suspend fun getMessageById(id: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    @Query("UPDATE messages SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: MessageDeliveryStatus)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessage(id: String)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun clearHistory(conversationId: String)

    @Query("DELETE FROM messages")
    suspend fun deleteAll()
}

@Dao
interface ChatroomDao {
    @Query("SELECT * FROM chatrooms ORDER BY isJoined DESC, chatterCount DESC")
    fun getAllChatroomsFlow(): Flow<List<ChatroomEntity>>

    @Query("SELECT * FROM chatrooms WHERE id = :id LIMIT 1")
    suspend fun getChatroomById(id: String): ChatroomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatroom(room: ChatroomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rooms: List<ChatroomEntity>)

    @Query("UPDATE chatrooms SET isJoined = :isJoined, chatterCount = chatterCount + :chatterDelta WHERE id = :id")
    suspend fun updateJoinState(id: String, isJoined: Boolean, chatterDelta: Int)

    @Query("DELETE FROM chatrooms")
    suspend fun deleteAllChatrooms()

    @Query("SELECT * FROM room_messages WHERE roomId = :roomId ORDER BY timestamp ASC")
    fun getRoomMessagesFlow(roomId: String): Flow<List<RoomMessageEntity>>

    @Query("SELECT id FROM room_messages WHERE id = :id LIMIT 1")
    suspend fun getRoomMessageById(id: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoomMessage(message: RoomMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRoomMessages(messages: List<RoomMessageEntity>)

    @Query("DELETE FROM room_messages")
    suspend fun deleteAllRoomMessages()
}
