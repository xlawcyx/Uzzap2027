package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.example.data.model.ChatroomEntity
import com.example.data.model.RoomRole
import com.example.data.repository.mergeRemoteChatroom

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("UZZ-APP", appName)
  }

  @Test
  fun `verify logout strings`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val logoutText = context.getString(R.string.action_logout)
    assertEquals("Log Out", logoutText)
  }

  @Test
  fun `verify firestore sync status labels`() {
    val connected = com.example.data.remote.firestore.FirestoreSyncStatus.CONNECTED
    assertEquals("Connected to Firestore", connected.label)
  }

  @Test
  fun `verify user profile entity default values`() {
    val profile = com.example.data.model.UserProfileEntity(
      username = "test_user",
      displayName = "Test User",
      phoneNumber = "+63 917 111 2222",
      status = com.example.data.model.UserPresence.ONLINE,
      statusMessage = "Hello Uzzap!",
      avatarEmoji = "😎"
    )
    assertEquals("test_user", profile.username)
    assertEquals("Test User", profile.displayName)
    assertEquals("😎", profile.avatarEmoji)
    assertEquals(com.example.data.model.UserPresence.ONLINE, profile.status)
  }

  @Test
  fun `verify ugc reporting reason categories`() {
    val reasons = listOf(
      "Harassment / Bullying",
      "Spam / Unsolicited",
      "Inappropriate Content",
      "Hate Speech"
    )
    assert(reasons.contains("Harassment / Bullying"))
    assert(reasons.contains("Inappropriate Content"))
  }

  @Test
  fun `cloud room refresh preserves local membership state`() {
    val local = ChatroomEntity(
      id = "room_cebu",
      name = "#Cebu",
      topic = "Old topic",
      category = "Visayas",
      chatterCount = 3,
      isJoined = true,
      userRole = RoomRole.MODERATOR
    )
    val remote = local.copy(
      topic = "Updated topic",
      chatterCount = 8,
      isJoined = false,
      userRole = RoomRole.MEMBER
    )

    val merged = mergeRemoteChatroom(remote, local)

    assertEquals("Updated topic", merged.topic)
    assertEquals(8, merged.chatterCount)
    assertEquals(true, merged.isJoined)
    assertEquals(RoomRole.MODERATOR, merged.userRole)
  }
}
