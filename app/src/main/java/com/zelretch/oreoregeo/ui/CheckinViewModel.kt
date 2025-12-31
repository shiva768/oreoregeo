package com.zelretch.oreoregeo.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zelretch.oreoregeo.domain.OreoregeoRepository
import com.zelretch.oreoregeo.domain.Place
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface CheckinState {
    object Idle : CheckinState
    object Saving : CheckinState
    data class Error(val message: String) : CheckinState
    object Success : CheckinState
}

class CheckinViewModel(
    private val repository: OreoregeoRepository,
    private val appContext: Context,
) : ViewModel() {
    private val _state = MutableStateFlow<CheckinState>(CheckinState.Idle)
    val state: StateFlow<CheckinState> = _state

    fun checkIn(place: Place, note: String?, visitedAt: Long) {
        viewModelScope.launch {
            if (!NetworkUtil.isNetworkAvailable(appContext)) {
                _state.value = CheckinState.Error("オフラインのためチェックインできません")
                return@launch
            }
            _state.value = CheckinState.Saving
            val result = repository.checkIn(place, note, visitedAt)
            _state.value = result.fold(
                onSuccess = { CheckinState.Success },
                onFailure = { CheckinState.Error(it.message ?: "チェックインできませんでした") }
            )
        }
    }
}
