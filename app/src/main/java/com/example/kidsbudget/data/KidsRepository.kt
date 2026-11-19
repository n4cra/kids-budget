package com.example.kidsbudget.data

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.room3.withWriteTransaction
import com.example.kidsbudget.data.backup.BackupAccount
import com.example.kidsbudget.data.backup.BackupChild
import com.example.kidsbudget.data.backup.BackupFile
import com.example.kidsbudget.data.backup.BackupTransaction
import com.example.kidsbudget.data.local.AccountEntity
import com.example.kidsbudget.data.local.ChildEntity
import com.example.kidsbudget.data.local.KidsDatabase
import com.example.kidsbudget.data.local.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

interface KidsRepository {
  fun observeChildren(): Flow<List<ChildSummary>>

  fun observeChildDetail(childId: Long): Flow<ChildDetail?>

  suspend fun addChild(name: String, colorArgb: Int, withDefaultAccounts: Boolean): Long

  suspend fun updateChild(childId: Long, name: String, colorArgb: Int)

  suspend fun deleteChild(childId: Long)

  suspend fun addAccount(childId: Long, name: String): Long

  suspend fun deleteAccount(accountId: Long)

  suspend fun addTransaction(accountId: Long, amountCents: Long, memo: String, occurredAt: Long): Long

  suspend fun deleteTransaction(transactionId: Long)

  suspend fun exportBackup(): BackupFile

  suspend fun importBackup(backup: BackupFile)
}

class DefaultKidsRepository(private val database: KidsDatabase) : KidsRepository {
  private val childDao = database.childDao()
  private val accountDao = database.accountDao()
  private val transactionDao = database.transactionDao()

  override fun observeChildren(): Flow<List<ChildSummary>> =
    combine(childDao.observeAll(), accountDao.observeAll(), transactionDao.observeAll()) {
      children,
      accounts,
      transactions ->
      val balanceByAccount =
        transactions.groupBy { it.accountId }.mapValues { (_, rows) -> rows.sumOf { it.amountCents } }
      children.map { child ->
        val childAccounts =
          accounts
            .filter { it.childId == child.id }
            .map { account ->
              Account(
                id = account.id,
                childId = account.childId,
                name = account.name,
                balanceCents = balanceByAccount[account.id] ?: 0L,
              )
            }
        ChildSummary(
          child = child.toDomain(),
          totalBalanceCents = childAccounts.sumOf { it.balanceCents },
          accounts = childAccounts,
        )
      }
    }

  override fun observeChildDetail(childId: Long): Flow<ChildDetail?> =
    combine(
      childDao.observeById(childId),
      accountDao.observeByChild(childId),
      transactionDao.observeByChild(childId),
    ) { child, accounts, transactions ->
      if (child == null) return@combine null
      val balanceByAccount =
        transactions.groupBy { it.accountId }.mapValues { (_, rows) -> rows.sumOf { it.amountCents } }
      val domainAccounts =
        accounts.map { account ->
          Account(
            id = account.id,
            childId = account.childId,
            name = account.name,
            balanceCents = balanceByAccount[account.id] ?: 0L,
          )
        }
      val accountNames = domainAccounts.associate { it.id to it.name }
      val domainTransactions =
        transactions.map { transaction ->
          CashTransaction(
            id = transaction.id,
            accountId = transaction.accountId,
            accountName = accountNames[transaction.accountId].orEmpty(),
            amountCents = transaction.amountCents,
            memo = transaction.memo,
            occurredAt = transaction.occurredAt,
          )
        }
      ChildDetail(
        child = child.toDomain(),
        totalBalanceCents = domainAccounts.sumOf { it.balanceCents },
        accounts = domainAccounts,
        transactions = domainTransactions,
        balanceHistory = BalanceHistory.from(domainTransactions),
      )
    }

  override suspend fun addChild(name: String, colorArgb: Int, withDefaultAccounts: Boolean): Long {
    val now = System.currentTimeMillis()
    val childId =
      childDao.insert(ChildEntity(name = name.trim(), colorArgb = colorArgb, createdAt = now))
    if (withDefaultAccounts) {
      DefaultAccounts.names.forEach { accountName ->
        accountDao.insert(AccountEntity(childId = childId, name = accountName, createdAt = now))
      }
    }
    return childId
  }

  override suspend fun updateChild(childId: Long, name: String, colorArgb: Int) {
    childDao.update(id = childId, name = name.trim(), colorArgb = colorArgb)
  }

  override suspend fun deleteChild(childId: Long) {
    childDao.deleteById(childId)
  }

  override suspend fun addAccount(childId: Long, name: String): Long =
    accountDao.insert(
      AccountEntity(childId = childId, name = name.trim(), createdAt = System.currentTimeMillis())
    )

  override suspend fun deleteAccount(accountId: Long) {
    accountDao.deleteById(accountId)
  }

  override suspend fun addTransaction(
    accountId: Long,
    amountCents: Long,
    memo: String,
    occurredAt: Long,
  ): Long =
    transactionDao.insert(
      TransactionEntity(
        accountId = accountId,
        amountCents = amountCents,
        memo = memo.trim(),
        occurredAt = occurredAt,
        createdAt = System.currentTimeMillis(),
      )
    )

  override suspend fun deleteTransaction(transactionId: Long) {
    transactionDao.deleteById(transactionId)
  }

  override suspend fun exportBackup(): BackupFile {
    val children = childDao.getAll()
    val accounts = accountDao.getAll().groupBy { it.childId }
    val transactions = transactionDao.getAll().groupBy { it.accountId }
    return BackupFile(
      exportedAt = System.currentTimeMillis(),
      children =
        children.map { child ->
          BackupChild(
            name = child.name,
            colorArgb = child.colorArgb,
            createdAt = child.createdAt,
            accounts =
              accounts[child.id].orEmpty().map { account ->
                BackupAccount(
                  name = account.name,
                  createdAt = account.createdAt,
                  transactions =
                    transactions[account.id].orEmpty().map { transaction ->
                      BackupTransaction(
                        amountCents = transaction.amountCents,
                        memo = transaction.memo,
                        occurredAt = transaction.occurredAt,
                        createdAt = transaction.createdAt,
                      )
                    },
                )
              },
          )
        },
    )
  }

  override suspend fun importBackup(backup: BackupFile) {
    database.withWriteTransaction {
      childDao.deleteAll()
      backup.children.forEach { child ->
        val childId =
          childDao.insert(
            ChildEntity(name = child.name.trim(), colorArgb = child.colorArgb, createdAt = child.createdAt)
          )
        child.accounts.forEach { account ->
          val accountId =
            accountDao.insert(
              AccountEntity(childId = childId, name = account.name.trim(), createdAt = account.createdAt)
            )
          account.transactions.forEach { transaction ->
            transactionDao.insert(
              TransactionEntity(
                accountId = accountId,
                amountCents = transaction.amountCents,
                memo = transaction.memo,
                occurredAt = transaction.occurredAt,
                createdAt = transaction.createdAt,
              )
            )
          }
        }
      }
    }
  }
}

private fun ChildEntity.toDomain() = Child(id = id, name = name, colorArgb = colorArgb)

val LocalKidsRepository =
  staticCompositionLocalOf<KidsRepository> { error("KidsRepository is not provided") }
