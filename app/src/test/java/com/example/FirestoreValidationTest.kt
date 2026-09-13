package com.example

import com.example.data.remote.firestore.MIN_PASSWORD_LENGTH
import com.example.data.remote.firestore.authEmailForUsername
import com.example.data.remote.firestore.canonicalDirectParticipants
import com.example.data.remote.firestore.directConversationId
import com.example.data.remote.firestore.normalizeUsername
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FirestoreValidationTest {
    @Test
    fun normalizeUsername_trimsAndNormalizesPrefix() {
        assertEquals("juan.delacruz", normalizeUsername("  @Juan.DelaCruz  "))
    }

    @Test
    fun authEmailForUsername_usesStableAppDomain() {
        assertEquals("juan@accounts.uzzap.app", authEmailForUsername(" @Juan "))
    }

    @Test
    fun passwordPolicy_requiresAtLeastSixCharacters() {
        assertTrue(MIN_PASSWORD_LENGTH >= 6)
    }

    @Test
    fun directConversationId_isStableForBothParticipants() {
        assertEquals(
            directConversationId("alice", "bob"),
            directConversationId("BOB", "@Alice")
        )
        assertNotEquals(
            directConversationId("alice", "bob"),
            directConversationId("alice", "charlie")
        )
    }

    @Test
    fun directParticipants_keepUsernamesAndUidsInCanonicalOrder() {
        val expected = listOf("alice", "bob") to listOf("alice-uid", "bob-uid")

        assertEquals(
            expected,
            canonicalDirectParticipants("bob", "bob-uid", "alice", "alice-uid")
        )
        assertEquals(
            expected,
            canonicalDirectParticipants("Alice", "alice-uid", "@Bob", "bob-uid")
        )
    }
}
