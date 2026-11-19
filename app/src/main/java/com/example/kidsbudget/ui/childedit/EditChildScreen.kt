package com.example.kidsbudget.ui.childedit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.kidsbudget.data.Child
import com.example.kidsbudget.theme.KidPalette
import com.example.kidsbudget.ui.components.ColorPickerRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditChildScreen(
  existing: Child?,
  onBack: () -> Unit,
  onSave: (name: String, colorArgb: Int, withDefaultAccounts: Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  var name by rememberSaveable(existing?.id) { mutableStateOf(existing?.name.orEmpty()) }
  var colorArgb by rememberSaveable(existing?.id) {
    mutableIntStateOf(existing?.colorArgb ?: KidPalette.first().toArgb())
  }
  var withDefaultAccounts by rememberSaveable(existing?.id) { mutableStateOf(true) }
  val isNew = existing == null
  val canSave = name.trim().isNotEmpty()

  Scaffold(
    modifier = modifier,
    contentWindowInsets = WindowInsets.safeDrawing,
    topBar = {
      TopAppBar(
        title = { Text(if (isNew) "Add child" else "Edit child") },
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
      OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Name") },
        singleLine = true,
      )
      Spacer(Modifier.height(20.dp))
      Text("Color", style = MaterialTheme.typography.titleSmall)
      Spacer(Modifier.height(12.dp))
      ColorPickerRow(selectedArgb = colorArgb, onSelect = { colorArgb = it })
      if (isNew) {
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Checkbox(checked = withDefaultAccounts, onCheckedChange = { withDefaultAccounts = it })
          Text("Create Saving, Spending, and Giving accounts")
        }
      }
      Spacer(Modifier.height(24.dp))
      Button(
        onClick = { onSave(name.trim(), colorArgb, withDefaultAccounts) },
        enabled = canSave,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text("Save")
      }
    }
  }
}
