package com.example.kidsbudget.data

data class Child(
  val id: Long,
  val name: String,
  val colorArgb: Int,
)

data class Account(
  val id: Long,
  val childId: Long,
  val name: String,
  val balanceCents: Long,
)

data class CashTransaction(
  val id: Long,
  val accountId: Long,
  val accountName: String,
  val amountCents: Long,
  val memo: String,
  val occurredAt: Long,
)

data class ChildSummary(
  val child: Child,
  val totalBalanceCents: Long,
  val accounts: List<Account>,
)

data class ChildDetail(
  val child: Child,
  val totalBalanceCents: Long,
  val accounts: List<Account>,
  val transactions: List<CashTransaction>,
  val balanceHistory: List<BalancePoint>,
)

data class BalancePoint(val timestamp: Long, val balanceCents: Long)

object DefaultAccounts {
  val names: List<String> = listOf("Saving", "Spending", "Giving")
}
