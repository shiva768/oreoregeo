package com.zelretch.oreoregeo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zelretch.oreoregeo.data.remote.ReverseGeocodeResult
import com.zelretch.oreoregeo.domain.ProvisionalCheckin
import com.zelretch.oreoregeo.domain.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ProvisionalCheckinConfirmState {
    object Idle : ProvisionalCheckinConfirmState()
    object Loading : ProvisionalCheckinConfirmState()
    object Success : ProvisionalCheckinConfirmState()
    data class Error(val message: String) : ProvisionalCheckinConfirmState()
}

class ProvisionalCheckinViewModel(
    private val repository: Repository
) : ViewModel() {

    val pendingCheckins: StateFlow<List<ProvisionalCheckin>> = repository
        .getPendingProvisionalCheckins()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingCount: StateFlow<Int> = repository
        .getPendingProvisionalCheckinCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _confirmState = MutableStateFlow<ProvisionalCheckinConfirmState>(
        ProvisionalCheckinConfirmState.Idle
    )
    val confirmState: StateFlow<ProvisionalCheckinConfirmState> = _confirmState.asStateFlow()

    private val _geocodeResult = MutableStateFlow<ReverseGeocodeResult?>(null)
    val geocodeResult: StateFlow<ReverseGeocodeResult?> = _geocodeResult.asStateFlow()

    fun loadGeocode(lat: Double, lon: Double) {
        _geocodeResult.value = null
        viewModelScope.launch {
            repository.reverseGeocode(lat, lon).onSuccess { _geocodeResult.value = it }
        }
    }

    fun clearGeocode() {
        _geocodeResult.value = null
    }

    fun confirm(provisionalId: Long, placeKey: String, note: String) {
        viewModelScope.launch {
            _confirmState.value = ProvisionalCheckinConfirmState.Loading
            val result = repository.confirmProvisionalCheckin(provisionalId, placeKey, note)
            _confirmState.value = result.fold(
                onSuccess = { ProvisionalCheckinConfirmState.Success },
                onFailure = { ProvisionalCheckinConfirmState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun dismiss(id: Long) {
        viewModelScope.launch {
            repository.dismissProvisionalCheckin(id)
        }
    }

    fun resetConfirmState() {
        _confirmState.value = ProvisionalCheckinConfirmState.Idle
    }
}

class ProvisionalCheckinViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProvisionalCheckinViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProvisionalCheckinViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
