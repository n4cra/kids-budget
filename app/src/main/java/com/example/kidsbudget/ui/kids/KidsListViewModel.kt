package com.example.kidsbudget.ui.kids

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidsbudget.data.ChildSummary
import com.example.kidsbudget.data.KidsRepository
import com.example.kidsbudget.data.backup.BackupCodec
import com.example.kidsbudget.data.backup.BackupException
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class KidsListUiState(
  val children: List<ChildSummary> = emptyList(),
  val isLoading: Boolean = true,
)

class KidsListViewModel(private val repository: KidsRepository) : ViewModel() {
  val uiState: StateFlow<KidsListUiState> =
    repository
      .observeChildren()
      .map { KidsListUiState(children = it, isLoading = false) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), KidsListUiState())

  private val _message = MutableStateFlow<String?>(null)
  val message: StateFlow<String?> = _message.asStateFlow()

  fun addChild(name: String, colorArgb: Int, withDefaultAccounts: Boolean) {
    viewModelScope.launch { repository.addChild(name, colorArgb, withDefaultAccounts) }
  }

  fun updateChild(childId: Long, name: String, colorArgb: Int) {
    viewModelScope.launch { repository.updateChild(childId, name, colorArgb) }
  }

  fun deleteChild(childId: Long) {
    viewModelScope.launch { repository.deleteChild(childId) }
  }

  fun clearMessage() {
    _message.value = null
  }

  fun exportTo(uri: Uri, resolver: ContentResolver) {
    viewModelScope.launch {
      try {
        val backup = repository.exportBackup()
        val json = BackupCodec.encode(backup)
        withContext(Dispatchers.IO) {
          resolver.openOutputStream(uri)?.use { stream ->
            stream.write(json.toByteArray(StandardCharsets.UTF_8))
          } ?: throw BackupException("Could not write the backup file.")
        }
        val childCount = backup.children.size
        _message.value = "Exported $childCount ${if (childCount == 1) "child" else "children"}."
      } catch (error: Exception) {
        _message.value = error.userMessage()
      }
    }
  }

  fun importFrom(uri: Uri, resolver: ContentResolver) {
    viewModelScope.launch {
      try {
        val raw =
          withContext(Dispatchers.IO) {
            resolver.openInputStream(uri)?.use { stream ->
              stream.readBytes().toString(StandardCharsets.UTF_8)
            } ?: throw BackupException("Could not read the backup file.")
          }
        val backup = BackupCodec.decode(raw)
        repository.importBackup(backup)
        val childCount = backup.children.size
        _message.value = "Imported $childCount ${if (childCount == 1) "child" else "children"}."
      } catch (error: Exception) {
        _message.value = error.userMessage()
      }
    }
  }
}

private fun Exception.userMessage(): String =
  when (this) {
    is BackupException -> message ?: "Backup failed."
    else -> "Backup failed."
  }
