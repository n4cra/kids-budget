package com.example.kidsbudget.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BalanceHistoryTest {
  @Test
  fun from_emptyList() {
    assertTrue(BalanceHistory.from(emptyList()).isEmpty())
  }

  @Test
  fun from_computesRunningBalanceInChronologicalOrder() {
    val transactions =
      listOf(
        txn(id = 2, amount = -400, at = 200),
        txn(id = 1, amount = 1000, at = 100),
        txn(id = 3, amount = 250, at = 300),
      )

    val history = BalanceHistory.from(transactions)

    assertEquals(listOf(1000L, 600L, 850L), history.map { it.balanceCents })
    assertEquals(listOf(100L, 200L, 300L), history.map { it.timestamp })
  }

  @Test
  fun from_breaksTimestampTiesUsingId() {
    val transactions =
      listOf(
        txn(id = 2, amount = 50, at = 10),
        txn(id = 1, amount = 100, at = 10),
      )

    val history = BalanceHistory.from(transactions)
    assertEquals(listOf(100L, 150L), history.map { it.balanceCents })
  }

  private fun txn(id: Long, amount: Long, at: Long) =
    CashTransaction(
      id = id,
      accountId = 1,
      accountName = "Spending",
      amountCents = amount,
      memo = "",
      occurredAt = at,
    )
}
