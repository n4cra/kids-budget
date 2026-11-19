package com.example.kidsbudget.data.local

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver
import kotlinx.coroutines.Dispatchers

@Database(
  entities = [ChildEntity::class, AccountEntity::class, TransactionEntity::class],
  version = 1,
  exportSchema = false,
)
abstract class KidsDatabase : RoomDatabase() {
  abstract fun childDao(): ChildDao

  abstract fun accountDao(): AccountDao

  abstract fun transactionDao(): TransactionDao

  companion object {
    fun create(context: Context): KidsDatabase =
      Room.databaseBuilder<KidsDatabase>(context, "kids-budget.db")
        .setDriver(AndroidSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
  }
}
