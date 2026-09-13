package com.example

import com.example.data.remote.firestore.MIN_PASSWORD_LENGTH
import com.example.data.remote.firestore.authEmailForUsername
import com.example.data.remote.firestore.normalizeUsername
import org.junit.Assert.assertEquals
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
}
