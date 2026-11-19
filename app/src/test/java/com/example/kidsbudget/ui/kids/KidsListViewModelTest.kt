package com.example.kidsbudget.ui.kids

import com.example.kidsbudget.data.Child
import com.example.kidsbudget.data.ChildDetail
import com.example.kidsbudget.data.ChildSummary
import com.example.kidsbudget.data.KidsRepository
import com.example.kidsbudget.data.backup.BackupFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class KidsListViewModelTest {
  private val dispatcher = UnconfinedTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(dispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun uiState_exposesChildrenFromRepository() = runTest {
    val child = Child(id = 1, name = "Emma", colorArgb = 1)
    val repository = FakeKidsRepository(listOf(ChildSummary(child, 500, emptyList())))
    val viewModel = KidsListViewModel(repository)

    val state = viewModel.uiState.first { !it.isLoading }
    assertFalse(state.isLoading)
    assertEquals("Emma", state.children.single().child.name)
    assertEquals(500L, state.children.single().totalBalanceCents)
  }
}

private class FakeKidsRepository(initial: List<ChildSummary>) : KidsRepository {
  private val children = MutableStateFlow(initial)

  override fun observeChildren(): Flow<List<ChildSummary>> = children

  override fun observeChildDetail(childId: Long): Flow<ChildDetail?> =
    children.map { list ->
      list.firstOrNull { it.child.id == childId }?.let { summary ->
        ChildDetail(
          child = summary.child,
          totalBalanceCents = summary.totalBalanceCents,
          accounts = summary.accounts,
          transactions = emptyList(),
          balanceHistory = emptyList(),
        )
      }
    }

  override suspend fun addChild(name: String, colorArgb: Int, withDefaultAccounts: Boolean): Long = 0

  override suspend fun updateChild(childId: Long, name: String, colorArgb: Int) = Unit

  override suspend fun deleteChild(childId: Long) = Unit

  override suspend fun addAccount(childId: Long, name: String): Long = 0

  override suspend fun deleteAccount(accountId: Long) = Unit

  override suspend fun addTransaction(
    accountId: Long,
    amountCents: Long,
    memo: String,
    occurredAt: Long,
  ): Long = 0

  override suspend fun deleteTransaction(transactionId: Long) = Unit

  override suspend fun exportBackup() = BackupFile()

  override suspend fun importBackup(backup: BackupFile) = Unit
}
