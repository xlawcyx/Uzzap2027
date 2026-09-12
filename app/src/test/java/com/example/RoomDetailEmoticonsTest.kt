package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.semantics.SemanticsProperties
import com.example.data.model.ChatroomEntity
import com.example.data.model.RoomMessageEntity
import com.example.data.model.RoomRole
import com.example.ui.components.CLASSIC_EMOTICONS
import com.example.ui.screens.RoomDetailScreen
import com.example.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoomDetailEmoticonsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val room = ChatroomEntity(
        id = "room_test",
        name = "Test room",
        topic = "Classic Uzzap chat",
        category = "General",
        chatterCount = 2,
        isJoined = true
    )

    @Test
    fun `room picker inserts a bundled emoticon token into the composer`() {
        composeTestRule.setContent {
            MyApplicationTheme {
                RoomDetailScreen(
                    room = room,
                    messages = emptyList(),
                    onBack = {},
                    onSendMessage = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("room_emoticon_button").performClick()
        composeTestRule.onNodeWithTag("room_emoticon_picker").assertIsDisplayed()
        composeTestRule.onNodeWithTag("emoticon_uzzap_11").performClick()
        val editableText = composeTestRule.onNodeWithTag("room_message_input")
            .fetchSemanticsNode().config[SemanticsProperties.EditableText].text
        assertEquals("${CLASSIC_EMOTICONS.first().token} ", editableText)
    }

    @Test
    fun `standalone room emoticon renders as an image inside its bubble`() {
        val emoticon = CLASSIC_EMOTICONS.first()
        val message = RoomMessageEntity(
            id = "message_1",
            roomId = room.id,
            senderUsername = "friend",
            senderRole = RoomRole.MEMBER,
            message = emoticon.token,
            timestamp = 1L
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                RoomDetailScreen(
                    room = room,
                    messages = listOf(message),
                    onBack = {},
                    onSendMessage = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(emoticon.description).assertIsDisplayed()
    }
}
