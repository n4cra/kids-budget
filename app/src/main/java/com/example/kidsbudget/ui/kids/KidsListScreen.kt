package com.example.kidsbudget.ui.kids

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kidsbudget.data.ChildSummary
import com.example.kidsbudget.data.Money
import com.example.kidsbudget.ui.components.KidAvatar
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KidsListScreen(
  state: KidsListUiState,
  selectedChildId: Long?,
  onChildClick: (Long) -> Unit,
  onAddChild: () -> Unit,
  message: String? = null,
  onMessageShown: () -> Unit = {},
  onExportTo: (Uri) -> Unit = {},
  onImportFrom: (Uri) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val snackbarHostState = remember { SnackbarHostState() }
  var menuOpen by remember { mutableStateOf(false) }
  var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
  val suggestedName = "kids-budget-${LocalDate.now()}.json"
  val exportLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
      if (uri != null) onExportTo(uri)
    }
  val importLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
      if (uri != null) pendingImportUri = uri
    }

  LaunchedEffect(message) {
    if (message != null) {
      snackbarHostState.showSnackbar(message)
      onMessageShown()
    }
  }

  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text("Kids Budget") },
        actions = {
          IconButton(onClick = { menuOpen = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = "More options")
          }
          DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            DropdownMenuItem(
              text = { Text("Export data") },
              onClick = {
                menuOpen = false
                exportLauncher.launch(suggestedName)
              },
            )
            DropdownMenuItem(
              text = { Text("Import data") },
              onClick = {
                menuOpen = false
                importLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
              },
            )
          }
        },
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) },
    floatingActionButton = {
      FloatingActionButton(onClick = onAddChild) {
        Icon(Icons.Filled.Add, contentDescription = "Add child")
      }
    },
  ) { innerPadding ->
    if (!state.isLoading && state.children.isEmpty()) {
      EmptyKidsState(
        modifier =
          Modifier.fillMaxSize()
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)
            .padding(24.dp)
      )
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize().consumeWindowInsets(innerPadding),
        contentPadding =
          PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = innerPadding.calculateTopPadding() + 8.dp,
            bottom = innerPadding.calculateBottomPadding() + 96.dp,
          ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        items(state.children, key = { it.child.id }) { summary ->
          KidCard(
            summary = summary,
            selected = summary.child.id == selectedChildId,
            onClick = { onChildClick(summary.child.id) },
          )
        }
      }
    }
  }

  pendingImportUri?.let { uri ->
    AlertDialog(
      onDismissRequest = { pendingImportUri = null },
      title = { Text("Replace all data?") },
      text = {
        Text("Importing a backup replaces every child, account, and transaction currently in the app.")
      },
      confirmButton = {
        TextButton(
          onClick = {
            pendingImportUri = null
            onImportFrom(uri)
          }
        ) {
          Text("Import")
        }
      },
      dismissButton = { TextButton(onClick = { pendingImportUri = null }) { Text("Cancel") } },
    )
  }
}

@Composable
private fun EmptyKidsState(modifier: Modifier = Modifier) {
  Column(modifier = modifier, verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = "Add your first child", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
    Text(
      text = "Track saving, spending, and giving for each kid in one place.",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun KidCard(
  summary: ChildSummary,
  selected: Boolean,
  onClick: () -> Unit,
) {
  Card(
    onClick = onClick,
    colors =
      CardDefaults.cardColors(
        containerColor =
          if (selected) MaterialTheme.colorScheme.primaryContainer
          else MaterialTheme.colorScheme.surface
      ),
    elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 0.dp else 1.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      KidAvatar(name = summary.child.name, colorArgb = summary.child.colorArgb, size = 52.dp)
      Spacer(Modifier.width(16.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(text = summary.child.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
          text =
            if (summary.accounts.isEmpty()) "No accounts yet"
            else summary.accounts.joinToString(" · ") { it.name },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      Text(
        text = Money.format(summary.totalBalanceCents),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}
