package com.example.kidsbudget.data

object BalanceHistory {
  fun from(transactions: List<CashTransaction>): List<BalancePoint> {
    if (transactions.isEmpty()) return emptyList()
    val sorted = transactions.sortedWith(compareBy({ it.occurredAt }, { it.id }))
    var running = 0L
    return sorted.map { transaction ->
      running += transaction.amountCents
      BalancePoint(timestamp = transaction.occurredAt, balanceCents = running)
    }
  }
}
