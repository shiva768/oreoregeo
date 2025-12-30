package com.example.oreoregeo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.oreoregeo.domain.PlaceWithDistance
import com.example.oreoregeo.domain.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SearchState {
    object Idle : SearchState()
    object Loading : SearchState()
    data class Success(val places: List<PlaceWithDistance>) : SearchState()
    data class Error(val message: String) : SearchState()
}

class SearchViewModel(
    private val repository: Repository
) : ViewModel() {

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Idle)
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    fun searchNearby(lat: Double, lon: Double) {
        viewModelScope.launch {
            _searchState.value = SearchState.Loading
            val result = repository.searchNearbyPlaces(lat, lon)
            _searchState.value = result.fold(
                onSuccess = { SearchState.Success(it) },
                onFailure = { SearchState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun reset() {
        _searchState.value = SearchState.Idle
    }
}

class SearchViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
