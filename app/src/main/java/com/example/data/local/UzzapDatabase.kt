package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ChatroomEntity
import com.example.data.model.ContactEntity
import com.example.data.model.ConversationEntity
import com.example.data.model.MessageEntity
import com.example.data.model.PhilippineRegions
import com.example.data.model.RoomMessageEntity
import com.example.data.model.RoomRole
import com.example.data.model.UserPresence
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfileEntity::class,
        ContactEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        ChatroomEntity::class,
        RoomMessageEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class UzzapDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun contactDao(): ContactDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun chatroomDao(): ChatroomDao

    companion object {
        @Volatile
        private var INSTANCE: UzzapDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): UzzapDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UzzapDatabase::class.java,
                    "uzzap_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(UzzapDatabaseCallback(scope))
                    .build()
                INSTANCE = instance

                // Verify and initialize Philippine provinces if needed
                scope.launch(Dispatchers.IO) {
                    ensurePhilippineRoomsInitialized(instance)
                }

                instance
            }
        }

        private class UzzapDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        ensurePhilippineRoomsInitialized(database)
                    }
                }
            }
        }

        suspend fun ensurePhilippineRoomsInitialized(database: UzzapDatabase) {
            val userDao = database.userDao()
            val chatroomDao = database.chatroomDao()

            // 1. Ensure user profile exists
            val existingProfile = userDao.getProfile()
            if (existingProfile == null) {
                userDao.insertProfile(
                    UserProfileEntity(
                        id = "me",
                        username = "juandelacruz",
                        displayName = "Juan Dela Cruz",
                        phoneNumber = "+63 918 555 1014",
                        status = UserPresence.ONLINE,
                        statusMessage = "Mabuhay! Connecting on Uzzap \uD83C\uDDF5\uD83C\uDDED",
                        avatarEmoji = "\uD83D\uDE0E",
                        phoneVerified = true,
                        vibrationEnabled = true
                    )
                )
            }

            // 2. Check if chatrooms need population or migration from old mock data
            val roomCount = chatroomDao.getChatroomCount()
            val hasOldLobby = chatroomDao.getChatroomById("room_lobby") != null

            if (roomCount == 0 || hasOldLobby) {
                // If old mock rooms existed, clear them out
                if (hasOldLobby) {
                    chatroomDao.deleteAllChatrooms()
                }

                // Populate real Philippine Provinces categorized by Region
                val now = System.currentTimeMillis()
                val rooms = mutableListOf<ChatroomEntity>()
                val initialNotices = mutableListOf<RoomMessageEntity>()

                PhilippineRegions.PROVINCES.forEachIndexed { index, province ->
                    val roomId = "room_${province.name.lowercase().replace(" ", "_").replace("'", "")}"
                    val isFirstJoined = index == 0 // Default join the capital/first room

                    rooms.add(
                        ChatroomEntity(
                            id = roomId,
                            name = province.roomTag,
                            topic = province.topic,
                            category = province.region,
                            chatterCount = if (isFirstJoined) 1 else 0,
                            isJoined = isFirstJoined,
                            userRole = if (isFirstJoined) RoomRole.MEMBER else RoomRole.GUEST
                        )
                    )

                    // Welcome notice for the room
                    initialNotices.add(
                        RoomMessageEntity(
                            id = "rm_welcome_$roomId",
                            roomId = roomId,
                            senderUsername = "System",
                            senderRole = RoomRole.ADMIN,
                            message = "Mabuhay! Maligayang pagdating sa opisyal na ${province.roomTag} chatroom ng ${province.region}. ${province.topic}",
                            timestamp = now - (index * 1000L),
                            isSystem = true
                        )
                    )
                }

                chatroomDao.insertAll(rooms)
                chatroomDao.insertAllRoomMessages(initialNotices)
            }
        }
    }
}
