package com.example.kidsbudget.ui.kid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kidsbudget.data.ChildDetail
import com.example.kidsbudget.data.KidsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class KidDetailViewModel(
  private val childId: Long,
  private val repository: KidsRepository,
) : ViewModel() {
  val uiState: StateFlow<ChildDetail?> =
    repository
      .observeChildDetail(childId)
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

  fun addAccount(name: String) {
    viewModelScope.launch { repository.addAccount(childId, name) }
  }

  fun deleteAccount(accountId: Long) {
    viewModelScope.launch { repository.deleteAccount(accountId) }
  }

  fun addTransaction(accountId: Long, amountCents: Long, memo: String, occurredAt: Long) {
    viewModelScope.launch { repository.addTransaction(accountId, amountCents, memo, occurredAt) }
  }

  fun deleteTransaction(transactionId: Long) {
    viewModelScope.launch { repository.deleteTransaction(transactionId) }
  }

  fun deleteChild() {
    viewModelScope.launch { repository.deleteChild(childId) }
  }
}
