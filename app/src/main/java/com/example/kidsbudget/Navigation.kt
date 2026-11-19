package com.example.kidsbudget

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.kidsbudget.data.LocalKidsRepository
import com.example.kidsbudget.ui.account.AddAccountScreen
import com.example.kidsbudget.ui.childedit.EditChildScreen
import com.example.kidsbudget.ui.kid.KidDetailPlaceholder
import com.example.kidsbudget.ui.kid.KidDetailScreen
import com.example.kidsbudget.ui.kid.KidDetailViewModel
import com.example.kidsbudget.ui.kids.KidsListScreen
import com.example.kidsbudget.ui.kids.KidsListViewModel
import com.example.kidsbudget.ui.transaction.AddTransactionScreen

@Composable
fun MainNavigation() {
  val repository = LocalKidsRepository.current
  val backStack = rememberNavBackStack(KidsList)
  val kidsListViewModel = viewModel { KidsListViewModel(repository) }
  val kidsListState by kidsListViewModel.uiState.collectAsStateWithLifecycle()
  val backupMessage by kidsListViewModel.message.collectAsStateWithLifecycle()
  val selectedChildId = backStack.filterIsInstance<KidDetail>().lastOrNull()?.childId
  val appContext = LocalContext.current.applicationContext

  fun openKid(childId: Long) {
    while (backStack.size > 1) {
      backStack.removeLastOrNull()
    }
    backStack.add(KidDetail(childId))
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val twoPane = maxWidth >= 700.dp
    if (twoPane) {
      BackHandler(enabled = backStack.size > 1) { backStack.removeLastOrNull() }
      Row(Modifier.fillMaxSize()) {
        Box(Modifier.weight(0.4f).fillMaxHeight()) {
          KidsListScreen(
            state = kidsListState,
            selectedChildId = selectedChildId,
            onChildClick = { childId -> openKid(childId) },
            onAddChild = dropUnlessResumed { backStack.add(AddChild) },
            message = backupMessage,
            onMessageShown = kidsListViewModel::clearMessage,
            onExportTo = { uri -> kidsListViewModel.exportTo(uri, appContext.contentResolver) },
            onImportFrom = { uri ->
              kidsListViewModel.importFrom(uri, appContext.contentResolver)
              while (backStack.size > 1) backStack.removeLastOrNull()
            },
          )
        }
        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.width(1.dp).fillMaxHeight())
        Box(Modifier.weight(0.6f).fillMaxHeight()) {
          TwoPaneDetail(
            backStack = backStack,
            kidsListViewModel = kidsListViewModel,
          )
        }
      }
    } else {
      NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider =
          entryProvider {
            entry<KidsList> {
              KidsListScreen(
                state = kidsListState,
                selectedChildId = selectedChildId,
                onChildClick = { childId -> openKid(childId) },
                onAddChild = dropUnlessResumed { backStack.add(AddChild) },
                message = backupMessage,
                onMessageShown = kidsListViewModel::clearMessage,
                onExportTo = { uri -> kidsListViewModel.exportTo(uri, appContext.contentResolver) },
                onImportFrom = { uri ->
                  kidsListViewModel.importFrom(uri, appContext.contentResolver)
                  while (backStack.size > 1) backStack.removeLastOrNull()
                },
              )
            }
            entry<KidDetail> { key ->
              KidDestination(
                childId = key.childId,
                showUpButton = true,
                onBack = { backStack.removeLastOrNull() },
                onEditChild = dropUnlessResumed { backStack.add(EditChild(key.childId)) },
                onDeleteChild = {
                  while (backStack.lastOrNull() != KidsList) {
                    backStack.removeLastOrNull()
                  }
                },
                onAddAccount = dropUnlessResumed { backStack.add(AddAccount(key.childId)) },
                onAddTransaction = dropUnlessResumed { backStack.add(AddTransaction(key.childId)) },
              )
            }
            entry<AddChild> {
              EditChildScreen(
                existing = null,
                onBack = { backStack.removeLastOrNull() },
                onSave = { name, colorArgb, withDefaultAccounts ->
                  kidsListViewModel.addChild(name, colorArgb, withDefaultAccounts)
                  backStack.removeLastOrNull()
                },
              )
            }
            entry<EditChild> { key ->
              val child = kidsListState.children.firstOrNull { it.child.id == key.childId }?.child
              EditChildScreen(
                existing = child,
                onBack = { backStack.removeLastOrNull() },
                onSave = { name, colorArgb, _ ->
                  kidsListViewModel.updateChild(key.childId, name, colorArgb)
                  backStack.removeLastOrNull()
                },
              )
            }
            entry<AddAccount> { key ->
              AddAccountDestination(
                childId = key.childId,
                onBack = { backStack.removeLastOrNull() },
              )
            }
            entry<AddTransaction> { key ->
              AddTransactionDestination(
                childId = key.childId,
                accountId = key.accountId,
                onBack = { backStack.removeLastOrNull() },
              )
            }
          },
      )
    }
  }
}

@Composable
private fun TwoPaneDetail(
  backStack: NavBackStack<NavKey>,
  kidsListViewModel: KidsListViewModel,
) {
  val kidsListState by kidsListViewModel.uiState.collectAsStateWithLifecycle()
  when (val top = backStack.lastOrNull()) {
    is KidDetail ->
      KidDestination(
        childId = top.childId,
        showUpButton = false,
        onBack = { backStack.removeLastOrNull() },
        onEditChild = dropUnlessResumed { backStack.add(EditChild(top.childId)) },
        onDeleteChild = {
          while (backStack.lastOrNull() != KidsList) {
            backStack.removeLastOrNull()
          }
        },
        onAddAccount = dropUnlessResumed { backStack.add(AddAccount(top.childId)) },
        onAddTransaction = dropUnlessResumed { backStack.add(AddTransaction(top.childId)) },
      )
    is AddChild ->
      EditChildScreen(
        existing = null,
        onBack = { backStack.removeLastOrNull() },
        onSave = { name, colorArgb, withDefaultAccounts ->
          kidsListViewModel.addChild(name, colorArgb, withDefaultAccounts)
          backStack.removeLastOrNull()
        },
      )
    is EditChild -> {
      val child = kidsListState.children.firstOrNull { it.child.id == top.childId }?.child
      EditChildScreen(
        existing = child,
        onBack = { backStack.removeLastOrNull() },
        onSave = { name, colorArgb, _ ->
          kidsListViewModel.updateChild(top.childId, name, colorArgb)
          backStack.removeLastOrNull()
        },
      )
    }
    is AddAccount ->
      AddAccountDestination(childId = top.childId, onBack = { backStack.removeLastOrNull() })
    is AddTransaction ->
      AddTransactionDestination(
        childId = top.childId,
        accountId = top.accountId,
        onBack = { backStack.removeLastOrNull() },
      )
    else -> KidDetailPlaceholder()
  }
}

@Composable
private fun KidDestination(
  childId: Long,
  showUpButton: Boolean,
  onBack: () -> Unit,
  onEditChild: () -> Unit,
  onDeleteChild: () -> Unit,
  onAddAccount: () -> Unit,
  onAddTransaction: () -> Unit,
) {
  val repository = LocalKidsRepository.current
  val detailViewModel = viewModel(key = "kid-$childId") { KidDetailViewModel(childId, repository) }
  val detail by detailViewModel.uiState.collectAsStateWithLifecycle()
  KidDetailScreen(
    detail = detail,
    showUpButton = showUpButton,
    onBack = onBack,
    onEditChild = onEditChild,
    onDeleteChild = {
      detailViewModel.deleteChild()
      onDeleteChild()
    },
    onAddAccount = onAddAccount,
    onDeleteAccount = detailViewModel::deleteAccount,
    onAddTransaction = onAddTransaction,
    onDeleteTransaction = detailViewModel::deleteTransaction,
  )
}

@Composable
private fun AddAccountDestination(childId: Long, onBack: () -> Unit) {
  val repository = LocalKidsRepository.current
  val detailViewModel = viewModel(key = "kid-$childId") { KidDetailViewModel(childId, repository) }
  AddAccountScreen(
    onBack = onBack,
    onSave = { name ->
      detailViewModel.addAccount(name)
      onBack()
    },
  )
}

@Composable
private fun AddTransactionDestination(childId: Long, accountId: Long?, onBack: () -> Unit) {
  val repository = LocalKidsRepository.current
  val detailViewModel = viewModel(key = "kid-$childId") { KidDetailViewModel(childId, repository) }
  val detail by detailViewModel.uiState.collectAsStateWithLifecycle()
  AddTransactionScreen(
    accounts = detail?.accounts.orEmpty(),
    preselectedAccountId = accountId,
    onBack = onBack,
    onSave = { selectedAccountId, amountCents, memo, occurredAt ->
      detailViewModel.addTransaction(selectedAccountId, amountCents, memo, occurredAt)
      onBack()
    },
  )
}
