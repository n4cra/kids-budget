package com.example.kidsbudget.data.local

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(tableName = "children")
data class ChildEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val colorArgb: Int,
  val createdAt: Long,
)

@Entity(
  tableName = "accounts",
  foreignKeys =
    [
      ForeignKey(
        entity = ChildEntity::class,
        parentColumns = ["id"],
        childColumns = ["childId"],
        onDelete = ForeignKey.CASCADE,
      )
    ],
  indices = [Index("childId")],
)
data class AccountEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val childId: Long,
  val name: String,
  val createdAt: Long,
)

@Entity(
  tableName = "transactions",
  foreignKeys =
    [
      ForeignKey(
        entity = AccountEntity::class,
        parentColumns = ["id"],
        childColumns = ["accountId"],
        onDelete = ForeignKey.CASCADE,
      )
    ],
  indices = [Index("accountId"), Index("occurredAt")],
)
data class TransactionEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val accountId: Long,
  val amountCents: Long,
  val memo: String,
  val occurredAt: Long,
  val createdAt: Long,
)
