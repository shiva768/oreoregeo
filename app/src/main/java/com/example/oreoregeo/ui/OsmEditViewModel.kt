package com.example.oreoregeo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.oreoregeo.domain.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class OsmEditState {
    object Idle : OsmEditState()
    object Loading : OsmEditState()
    data class Success(val placeKey: String) : OsmEditState()
    data class Error(val message: String) : OsmEditState()
}

class OsmEditViewModel(
    private val repository: Repository
) : ViewModel() {

    private val _editState = MutableStateFlow<OsmEditState>(OsmEditState.Idle)
    val editState: StateFlow<OsmEditState> = _editState.asStateFlow()

    fun createPlace(lat: Double, lon: Double, tags: Map<String, String>) {
        viewModelScope.launch {
            _editState.value = OsmEditState.Loading
            val result = repository.createOsmNode(
                lat = lat,
                lon = lon,
                tags = tags,
                comment = "Added place via Oreoregeo app"
            )
            _editState.value = result.fold(
                onSuccess = { OsmEditState.Success(it) },
                onFailure = { OsmEditState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun updateNodeTags(nodeId: Long, tags: Map<String, String>) {
        viewModelScope.launch {
            _editState.value = OsmEditState.Loading
            val result = repository.updateOsmNodeTags(
                nodeId = nodeId,
                newTags = tags,
                comment = "Updated tags via Oreoregeo app"
            )
            _editState.value = result.fold(
                onSuccess = { OsmEditState.Success("osm:node:$nodeId") },
                onFailure = { OsmEditState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun reset() {
        _editState.value = OsmEditState.Idle
    }
}

class OsmEditViewModelFactory(
    private val repository: Repository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OsmEditViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OsmEditViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
