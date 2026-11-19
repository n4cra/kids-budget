package com.example.kidsbudget.ui.kid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kidsbudget.data.Account
import com.example.kidsbudget.data.CashTransaction
import com.example.kidsbudget.data.ChildDetail
import com.example.kidsbudget.data.Dates
import com.example.kidsbudget.data.Money
import com.example.kidsbudget.theme.ExpenseRed
import com.example.kidsbudget.theme.IncomeGreen
import com.example.kidsbudget.ui.components.BalanceChart
import com.example.kidsbudget.ui.components.KidAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KidDetailScreen(
  detail: ChildDetail?,
  showUpButton: Boolean,
  onBack: () -> Unit,
  onEditChild: () -> Unit,
  onDeleteChild: () -> Unit,
  onAddAccount: () -> Unit,
  onDeleteAccount: (Long) -> Unit,
  onAddTransaction: () -> Unit,
  onDeleteTransaction: (Long) -> Unit,
  modifier: Modifier = Modifier,
) {
  var confirmDeleteChild by remember { mutableStateOf(false) }
  var accountToDelete by remember { mutableStateOf<Account?>(null) }

  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(detail?.child?.name ?: "Child") },
        navigationIcon = {
          if (showUpButton) {
            IconButton(onClick = onBack) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
          }
        },
        actions = {
          if (detail != null) {
            IconButton(onClick = onEditChild) {
              Icon(Icons.Filled.Edit, contentDescription = "Edit child")
            }
            IconButton(onClick = { confirmDeleteChild = true }) {
              Icon(Icons.Filled.Delete, contentDescription = "Delete child")
            }
          }
        },
      )
    },
    floatingActionButton = {
      if (detail != null && detail.accounts.isNotEmpty()) {
        FloatingActionButton(onClick = onAddTransaction) {
          Icon(Icons.Filled.Add, contentDescription = "Add transaction")
        }
      }
    },
  ) { innerPadding ->
    if (detail == null) {
      Column(
        modifier =
          Modifier.fillMaxSize()
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text("Choose a child from the list", color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize().consumeWindowInsets(innerPadding),
        contentPadding =
          PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = innerPadding.calculateTopPadding() + 8.dp,
            bottom = innerPadding.calculateBottomPadding() + 24.dp,
          ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        item {
          HeaderCard(detail)
        }
        item {
          Text("Balance over time", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
          Spacer(Modifier.height(8.dp))
          Card { BalanceChart(points = detail.balanceHistory, modifier = Modifier.padding(16.dp)) }
        }
        item {
          Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Accounts",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onAddAccount) { Text("Add account") }
          }
        }
        if (detail.accounts.isEmpty()) {
          item {
            Text(
              "Add a Saving, Spending, or custom account to start logging cash.",
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        } else {
          items(detail.accounts, key = { "account-${it.id}" }) { account ->
            AccountRow(account = account, onDelete = { accountToDelete = account })
          }
        }
        item {
          Text("Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        if (detail.transactions.isEmpty()) {
          item {
            Text(
              "No transactions yet. Log an allowance, gift, or purchase.",
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        } else {
          items(detail.transactions, key = { "txn-${it.id}" }) { transaction ->
            TransactionRow(transaction = transaction, onDelete = { onDeleteTransaction(transaction.id) })
          }
        }
        item { Spacer(Modifier.height(80.dp)) }
      }
    }
  }

  if (confirmDeleteChild && detail != null) {
    AlertDialog(
      onDismissRequest = { confirmDeleteChild = false },
      title = { Text("Delete ${detail.child.name}?") },
      text = { Text("This removes their accounts and every transaction. This cannot be undone.") },
      confirmButton = {
        TextButton(
          onClick = {
            confirmDeleteChild = false
            onDeleteChild()
          }
        ) {
          Text("Delete")
        }
      },
      dismissButton = { TextButton(onClick = { confirmDeleteChild = false }) { Text("Cancel") } },
    )
  }

  accountToDelete?.let { account ->
    AlertDialog(
      onDismissRequest = { accountToDelete = null },
      title = { Text("Delete ${account.name}?") },
      text = { Text("Transactions in this account will also be deleted.") },
      confirmButton = {
        TextButton(
          onClick = {
            onDeleteAccount(account.id)
            accountToDelete = null
          }
        ) {
          Text("Delete")
        }
      },
      dismissButton = { TextButton(onClick = { accountToDelete = null }) { Text("Cancel") } },
    )
  }
}

@Composable
private fun HeaderCard(detail: ChildDetail) {
  Card(modifier = Modifier.fillMaxWidth()) {
    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
      KidAvatar(name = detail.child.name, colorArgb = detail.child.colorArgb, size = 64.dp)
      Spacer(Modifier.width(16.dp))
      Column {
        Text("Total balance", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
          text = Money.format(detail.totalBalanceCents),
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}

@Composable
private fun AccountRow(account: Account, onDelete: () -> Unit) {
  var menuOpen by remember { mutableStateOf(false) }
  Card {
    Row(
      modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(account.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
      }
      Text(Money.format(account.balanceCents), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
      IconButton(onClick = { menuOpen = true }) {
        Icon(Icons.Filled.MoreVert, contentDescription = "Account options")
      }
      DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
        DropdownMenuItem(
          text = { Text("Delete") },
          onClick = {
            menuOpen = false
            onDelete()
          },
        )
      }
    }
  }
}

@Composable
private fun TransactionRow(transaction: CashTransaction, onDelete: () -> Unit) {
  var menuOpen by remember { mutableStateOf(false) }
  val amountColor = if (transaction.amountCents >= 0) IncomeGreen else ExpenseRed
  Card {
    Row(
      modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = transaction.memo.ifBlank { if (transaction.amountCents >= 0) "Money in" else "Money out" },
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Medium,
        )
        Text(
          text = "${transaction.accountName} · ${Dates.formatMedium(transaction.occurredAt)}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      Text(
        text = Money.formatSigned(transaction.amountCents),
        color = amountColor,
        fontWeight = FontWeight.SemiBold,
      )
      IconButton(onClick = { menuOpen = true }) {
        Icon(Icons.Filled.MoreVert, contentDescription = "Transaction options")
      }
      DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
        DropdownMenuItem(
          text = { Text("Delete") },
          onClick = {
            menuOpen = false
            onDelete()
          },
        )
      }
    }
  }
}

@Composable
fun KidDetailPlaceholder(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.fillMaxSize().padding(24.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text("Choose a child", style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(8.dp))
    Text(
      "Their accounts, cash flow, and balance graph will show up here.",
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}
