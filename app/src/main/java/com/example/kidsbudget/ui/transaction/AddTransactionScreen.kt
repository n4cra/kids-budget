package com.example.kidsbudget.ui.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.kidsbudget.data.Account
import com.example.kidsbudget.data.Dates
import com.example.kidsbudget.data.Money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
  accounts: List<Account>,
  preselectedAccountId: Long?,
  onBack: () -> Unit,
  onSave: (accountId: Long, amountCents: Long, memo: String, occurredAt: Long) -> Unit,
  modifier: Modifier = Modifier,
) {
  var accountId by rememberSaveable(accounts, preselectedAccountId) {
    mutableStateOf(preselectedAccountId ?: accounts.firstOrNull()?.id)
  }
  var isIncome by rememberSaveable { mutableStateOf(true) }
  var amountText by rememberSaveable { mutableStateOf("") }
  var memo by rememberSaveable { mutableStateOf("") }
  var occurredAt by rememberSaveable { mutableLongStateOf(Dates.todayMillis()) }
  var showDatePicker by rememberSaveable { mutableStateOf(false) }
  val cents = Money.parseToCents(amountText)
  val canSave = accountId != null && cents != null && cents > 0

  Scaffold(
    modifier = modifier,
    contentWindowInsets = WindowInsets.safeDrawing,
    topBar = {
      TopAppBar(
        title = { Text("Add transaction") },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
      )
    },
  ) { innerPadding ->
    Column(
      modifier =
        Modifier.fillMaxSize()
          .padding(innerPadding)
          .consumeWindowInsets(innerPadding)
          .verticalScroll(rememberScrollState())
          .padding(16.dp)
    ) {
      Text("Account", style = MaterialTheme.typography.titleSmall)
      Spacer(Modifier.height(8.dp))
      FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        accounts.forEach { account ->
          FilterChip(
            selected = account.id == accountId,
            onClick = { accountId = account.id },
            label = { Text(account.name) },
          )
        }
      }
      Spacer(Modifier.height(16.dp))
      Text("Type", style = MaterialTheme.typography.titleSmall)
      Spacer(Modifier.height(8.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = isIncome, onClick = { isIncome = true }, label = { Text("Received") })
        FilterChip(selected = !isIncome, onClick = { isIncome = false }, label = { Text("Spent") })
      }
      Spacer(Modifier.height(16.dp))
      OutlinedTextField(
        value = amountText,
        onValueChange = { amountText = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Amount") },
        prefix = { Text("$") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
      )
      Spacer(Modifier.height(16.dp))
      OutlinedTextField(
        value = memo,
        onValueChange = { memo = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Memo") },
        placeholder = { Text(if (isIncome) "Allowance, gift, chore..." else "Snack, toy, donation...") },
        singleLine = true,
      )
      Spacer(Modifier.height(16.dp))
      TextButton(onClick = { showDatePicker = true }) { Text("Date: ${Dates.formatMedium(occurredAt)}") }
      Spacer(Modifier.height(24.dp))
      Button(
        onClick = {
          val selectedAccount = accountId ?: return@Button
          val amount = cents ?: return@Button
          val signed = if (isIncome) amount else -amount
          onSave(selectedAccount, signed, memo.trim(), occurredAt)
        },
        enabled = canSave,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text("Save")
      }
    }
  }

  if (showDatePicker) {
    val pickerState =
      rememberDatePickerState(initialSelectedDateMillis = Dates.localDateToUtcMillis(Dates.millisToLocalDate(occurredAt)))
    DatePickerDialog(
      onDismissRequest = { showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = {
            pickerState.selectedDateMillis?.let { utcMillis ->
              occurredAt = Dates.localDateToMillis(Dates.utcMillisToLocalDate(utcMillis))
            }
            showDatePicker = false
          }
        ) {
          Text("OK")
        }
      },
      dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
    ) {
      DatePicker(state = pickerState)
    }
  }
}
