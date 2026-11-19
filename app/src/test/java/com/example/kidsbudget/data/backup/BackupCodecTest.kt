package com.example.kidsbudget.data.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupCodecTest {
  @Test
  fun roundTrip_preservesNestedData() {
    val original =
      BackupFile(
        exportedAt = 1_700_000_000_000L,
        children =
          listOf(
            BackupChild(
              name = "Emma",
              colorArgb = 0xFF1D9A6C.toInt(),
              createdAt = 10,
              accounts =
                listOf(
                  BackupAccount(
                    name = "Spending",
                    createdAt = 11,
                    transactions =
                      listOf(
                        BackupTransaction(
                          amountCents = 1000,
                          memo = "Allowance",
                          occurredAt = 12,
                          createdAt = 13,
                        )
                      ),
                  )
                ),
            )
          ),
      )

    val restored = BackupCodec.decode(BackupCodec.encode(original))
    assertEquals("kids-budget", restored.format)
    assertEquals(1, restored.version)
    assertEquals("Emma", restored.children.single().name)
    assertEquals("Spending", restored.children.single().accounts.single().name)
    assertEquals(1000L, restored.children.single().accounts.single().transactions.single().amountCents)
    assertEquals("Allowance", restored.children.single().accounts.single().transactions.single().memo)
  }

  @Test
  fun decode_rejectsUnknownFormat() {
    val raw = """{"format":"something-else","version":1,"exportedAt":0,"children":[]}"""
    try {
      BackupCodec.decode(raw)
      throw AssertionError("Expected BackupException")
    } catch (error: BackupException) {
      assertTrue(error.message!!.contains("Kids Budget backup"))
    }
  }

  @Test
  fun decode_rejectsInvalidJson() {
    try {
      BackupCodec.decode("not json")
      throw AssertionError("Expected BackupException")
    } catch (error: BackupException) {
      assertTrue(error.message!!.contains("Kids Budget backup"))
    }
  }
}
