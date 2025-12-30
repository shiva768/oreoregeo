package com.zelretch.oreoregeo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zelretch.oreoregeo.domain.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CheckinState {
    object Idle : CheckinState()
    object Loading : CheckinState()
    data class Success(val checkinId: Long) : CheckinState()
    data class Error(val message: String) : CheckinState()
}

class CheckinViewModel(
    private val repository: Repository
) : ViewModel() {

    private val _checkinState = MutableStateFlow<CheckinState>(CheckinState.Idle)
    val checkinState: StateFlow<CheckinState> = _checkinState.asStateFlow()

    fun performCheckin(placeKey: String, note: String) {
        viewModelScope.launch {
            _checkinState.value = CheckinState.Loading
            val result = repository.performCheckin(placeKey, note)
            _checkinState.value = result.fold(
                onSuccess = { CheckinState.Success(it) },
                onFailure = { 
                    val message = if (it.message?.contains("UNIQUE constraint failed") == true) {
                        "Cannot check-in within 30 minutes of previous check-in"
                    } else {
                        it.message ?: "Unknown error"
                    }
                    CheckinState.Error(message)
                }
            )
        }
    }

    fun reset() {
        _checkinState.value = CheckinState.Idle
    }
}

class CheckinViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CheckinViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CheckinViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
