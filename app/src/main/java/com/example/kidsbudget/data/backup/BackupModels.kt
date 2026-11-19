package com.example.kidsbudget.data.backup

import kotlinx.serialization.Serializable

@Serializable
data class BackupFile(
  val format: String = FORMAT,
  val version: Int = VERSION,
  val exportedAt: Long = 0,
  val children: List<BackupChild> = emptyList(),
) {
  companion object {
    const val FORMAT = "kids-budget"
    const val VERSION = 1
  }
}

@Serializable
data class BackupChild(
  val name: String,
  val colorArgb: Int,
  val createdAt: Long = 0,
  val accounts: List<BackupAccount> = emptyList(),
)

@Serializable
data class BackupAccount(
  val name: String,
  val createdAt: Long = 0,
  val transactions: List<BackupTransaction> = emptyList(),
)

@Serializable
data class BackupTransaction(
  val amountCents: Long,
  val memo: String = "",
  val occurredAt: Long,
  val createdAt: Long = 0,
)

class BackupException(message: String, cause: Throwable? = null) : Exception(message, cause)
