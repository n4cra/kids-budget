package com.example.kidsbudget.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChildDao {
  @Query("SELECT * FROM children ORDER BY name COLLATE NOCASE")
  fun observeAll(): Flow<List<ChildEntity>>

  @Query("SELECT * FROM children ORDER BY id")
  suspend fun getAll(): List<ChildEntity>

  @Query("DELETE FROM children") suspend fun deleteAll()

  @Query("SELECT * FROM children WHERE id = :id")
  fun observeById(id: Long): Flow<ChildEntity?>

  @Insert suspend fun insert(child: ChildEntity): Long

  @Query("UPDATE children SET name = :name, colorArgb = :colorArgb WHERE id = :id")
  suspend fun update(id: Long, name: String, colorArgb: Int)

  @Query("DELETE FROM children WHERE id = :id") suspend fun deleteById(id: Long)
}

@Dao
interface AccountDao {
  @Query("SELECT * FROM accounts") fun observeAll(): Flow<List<AccountEntity>>

  @Query("SELECT * FROM accounts ORDER BY createdAt, id") suspend fun getAll(): List<AccountEntity>

  @Query("SELECT * FROM accounts WHERE childId = :childId ORDER BY createdAt, id")
  fun observeByChild(childId: Long): Flow<List<AccountEntity>>

  @Insert suspend fun insert(account: AccountEntity): Long

  @Query("DELETE FROM accounts WHERE id = :id") suspend fun deleteById(id: Long)
}

@Dao
interface TransactionDao {
  @Query("SELECT * FROM transactions") fun observeAll(): Flow<List<TransactionEntity>>

  @Query("SELECT * FROM transactions ORDER BY occurredAt, id")
  suspend fun getAll(): List<TransactionEntity>

  @Query(
    """
    SELECT * FROM transactions
    WHERE accountId IN (SELECT id FROM accounts WHERE childId = :childId)
    ORDER BY occurredAt DESC, id DESC
    """
  )
  fun observeByChild(childId: Long): Flow<List<TransactionEntity>>

  @Insert suspend fun insert(transaction: TransactionEntity): Long

  @Query("DELETE FROM transactions WHERE id = :id") suspend fun deleteById(id: Long)
}
