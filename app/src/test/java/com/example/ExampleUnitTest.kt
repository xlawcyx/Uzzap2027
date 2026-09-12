package com.example

import com.example.data.remote.firestore.MIN_PASSWORD_LENGTH
import com.example.data.remote.firestore.authEmailForUsername
import com.example.data.remote.firestore.normalizeUsername
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun `username normalization is stable for authentication and document IDs`() {
    assertEquals("juan.delacruz", normalizeUsername("  @Juan.DelaCruz  "))
    assertEquals("juan.delacruz@accounts.uzzap.app", authEmailForUsername("@Juan.DelaCruz"))
  }

  @Test
  fun `username normalization is locale independent`() {
    val previousLocale = java.util.Locale.getDefault()
    try {
      java.util.Locale.setDefault(java.util.Locale.forLanguageTag("tr-TR"))
      assertEquals("indigo", normalizeUsername("INDIGO"))
    } finally {
      java.util.Locale.setDefault(previousLocale)
    }
  }

  @Test
  fun `firebase password policy minimum is enforced consistently`() {
    assertEquals(6, MIN_PASSWORD_LENGTH)
  }
}
