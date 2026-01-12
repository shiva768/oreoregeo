package com.zelretch.oreoregeo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zelretch.oreoregeo.domain.Checkin
import com.zelretch.oreoregeo.domain.Repository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: Repository
) : ViewModel() {

    private val _placeNameQuery = MutableStateFlow("")
    val placeNameQuery: StateFlow<String> = _placeNameQuery

    private val _areaQuery = MutableStateFlow("")
    val areaQuery: StateFlow<String> = _areaQuery

    private val _startDate = MutableStateFlow<Long?>(null)
    val startDate: StateFlow<Long?> = _startDate

    private val _endDate = MutableStateFlow<Long?>(null)
    val endDate: StateFlow<Long?> = _endDate

    private data class SearchFilters(
        val placeName: String,
        val area: String,
        val startDate: Long?,
        val endDate: Long?
    )

    @OptIn(FlowPreview::class)
    private val searchFilters: StateFlow<SearchFilters> = combine(
        _placeNameQuery.debounce(300), // Debounce input changes
        _areaQuery.debounce(300),
        _startDate,
        _endDate
    ) { placeName, area, start, end ->
        SearchFilters(placeName, area, start, end)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchFilters("", "", null, null)
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val checkins: StateFlow<List<Checkin>> = searchFilters.flatMapLatest { filters ->
        if (filters.placeName.isBlank() &&
            filters.area.isBlank() &&
            filters.startDate == null &&
            filters.endDate == null
        ) {
            repository.getAllCheckins()
        } else {
            repository.searchCheckins(
                placeNameQuery = filters.placeName.ifBlank { null },
                areaQuery = filters.area.ifBlank { null },
                startDate = filters.startDate,
                endDate = filters.endDate
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setPlaceNameQuery(query: String) {
        _placeNameQuery.value = query
    }

    fun setAreaQuery(query: String) {
        _areaQuery.value = query
    }

    fun setStartDate(date: Long?) {
        _startDate.value = date
    }

    fun setEndDate(date: Long?) {
        _endDate.value = date
    }

    fun clearFilters() {
        _placeNameQuery.value = ""
        _areaQuery.value = ""
        _startDate.value = null
        _endDate.value = null
    }

    fun deleteCheckin(checkinId: Long) {
        viewModelScope.launch {
            repository.deleteCheckin(checkinId)
        }
    }
}

class HistoryViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
