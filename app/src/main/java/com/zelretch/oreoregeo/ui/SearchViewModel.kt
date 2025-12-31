package com.zelretch.oreoregeo.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zelretch.oreoregeo.domain.OreoregeoRepository
import com.zelretch.oreoregeo.domain.SearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface SearchState {
    object Idle : SearchState
    object Loading : SearchState
    data class Error(val message: String) : SearchState
    data class Loaded(val results: List<SearchResult>) : SearchState
}

class SearchViewModel(
    private val repository: OreoregeoRepository,
    private val appContext: Context,
) : ViewModel() {
    private val _state = MutableStateFlow<SearchState>(SearchState.Idle)
    val state: StateFlow<SearchState> = _state

    fun search(lat: Double, lon: Double) {
        viewModelScope.launch {
            if (!NetworkUtil.isNetworkAvailable(appContext)) {
                _state.value = SearchState.Error("ネットワークに接続できません")
                return@launch
            }
            _state.value = SearchState.Loading
            val result = repository.searchNearby(lat, lon)
            _state.value = result.fold(
                onSuccess = { SearchState.Loaded(it) },
                onFailure = { SearchState.Error(it.message ?: "検索に失敗しました") }
            )
        }
    }
}
