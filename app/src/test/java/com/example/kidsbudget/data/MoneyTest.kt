package com.example.kidsbudget.data

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyTest {
  @Test
  fun parseToCents_wholeAndFractionalDollars() {
    assertEquals(500L, Money.parseToCents("5"))
    assertEquals(500L, Money.parseToCents("5.00"))
    assertEquals(550L, Money.parseToCents("5.5"))
    assertEquals(1L, Money.parseToCents("0.01"))
    assertEquals(556L, Money.parseToCents("5.555"))
  }

  @Test
  fun parseToCents_stripsCurrencyAndCommas() {
    assertEquals(123456L, Money.parseToCents("$1,234.56"))
    assertEquals(1050L, Money.parseToCents("10,50"))
  }

  @Test
  fun parseToCents_invalidInput() {
    assertNull(Money.parseToCents(""))
    assertNull(Money.parseToCents("abc"))
    assertNull(Money.parseToCents("."))
  }

  @Test
  fun format_usesCurrencyForLocale() {
    assertEquals("$12.50", Money.format(1250L, Locale.US))
    assertEquals("+$12.50", Money.formatSigned(1250L, Locale.US))
    assertEquals("-$12.50", Money.formatSigned(-1250L, Locale.US))
  }
}
