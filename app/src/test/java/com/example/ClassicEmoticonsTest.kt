package com.example

import com.example.ui.components.CLASSIC_EMOTICONS
import com.example.ui.components.appendClassicEmoticon
import com.example.ui.components.classicEmoticonForMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClassicEmoticonsTest {
    @Test
    fun `catalog exposes all supplied emoticons with stable unique tokens`() {
        assertEquals(35, CLASSIC_EMOTICONS.size)
        assertEquals(35, CLASSIC_EMOTICONS.map { it.token }.toSet().size)
        assertEquals(35, CLASSIC_EMOTICONS.map { it.drawableRes }.toSet().size)
        assertEquals(":uzzap_head_valkyrie:", CLASSIC_EMOTICONS.last().token)
    }

    @Test
    fun `standalone emoticon messages tolerate surrounding whitespace`() {
        val token = CLASSIC_EMOTICONS.first().token

        assertEquals(token, classicEmoticonForMessage("  $token ")?.token)
        assertNull(classicEmoticonForMessage("hello $token"))
    }

    @Test
    fun `emoticons are appended to drafts with readable spacing`() {
        val token = CLASSIC_EMOTICONS.first().token

        assertEquals("$token ", appendClassicEmoticon("", token))
        assertEquals("hello $token ", appendClassicEmoticon("hello", token))
        assertEquals("hello  $token ", appendClassicEmoticon("hello  ", token))
    }
}
