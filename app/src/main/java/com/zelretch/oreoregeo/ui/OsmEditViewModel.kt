package com.zelretch.oreoregeo.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zelretch.oreoregeo.data.remote.OsmNodeDetail
import com.zelretch.oreoregeo.domain.OreoregeoRepository
import com.zelretch.oreoregeo.ui.NetworkUtil
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

sealed interface NodeLoadState {
    object Idle : NodeLoadState
    object Loading : NodeLoadState
    data class Loaded(val detail: OsmNodeDetail) : NodeLoadState
    data class Error(val message: String) : NodeLoadState
}

class OsmEditViewModel(
    private val repository: OreoregeoRepository,
    private val appContext: Context,
) : ViewModel() {

    private val _editState = MutableStateFlow<OsmEditState>(OsmEditState.Idle)
    val editState: StateFlow<OsmEditState> = _editState.asStateFlow()

    private val _nodeState = MutableStateFlow<NodeLoadState>(NodeLoadState.Idle)
    val nodeState: StateFlow<NodeLoadState> = _nodeState.asStateFlow()

    fun createPlace(lat: Double, lon: Double, tags: Map<String, String>) {
        viewModelScope.launch {
            if (!NetworkUtil.isNetworkAvailable(appContext)) {
                _editState.value = OsmEditState.Error("オフラインのため作成できません")
                return@launch
            }
            _editState.value = OsmEditState.Loading
            val result = repository.addNode(lat, lon, tags)
            _editState.value = result.fold(
                onSuccess = { OsmEditState.Success("osm:node:${System.currentTimeMillis()}") },
                onFailure = { OsmEditState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun loadNode(placeKey: String) {
        val nodeId = extractNodeId(placeKey)
        if (nodeId == null) {
            _nodeState.value = NodeLoadState.Error("node のみ編集できます")
            return
        }
        viewModelScope.launch {
            if (!NetworkUtil.isNetworkAvailable(appContext)) {
                _nodeState.value = NodeLoadState.Error("オフラインのため取得できません")
                return@launch
            }
            _nodeState.value = NodeLoadState.Loading
            val result = repository.fetchNode(nodeId)
            _nodeState.value = result.fold(
                onSuccess = { NodeLoadState.Loaded(it) },
                onFailure = { NodeLoadState.Error(it.message ?: "取得に失敗しました") }
            )
        }
    }

    fun updateNodeTags(placeKey: String, tags: Map<String, String>) {
        val nodeId = extractNodeId(placeKey)
        val current = (_nodeState.value as? NodeLoadState.Loaded)?.detail
        if (nodeId == null || current == null) {
            _editState.value = OsmEditState.Error("タグを読み込んでから更新してください")
            return
        }
        viewModelScope.launch {
            if (!NetworkUtil.isNetworkAvailable(appContext)) {
                _editState.value = OsmEditState.Error("オフラインのため更新できません")
                return@launch
            }
            _editState.value = OsmEditState.Loading
            val mergedTags = current.tags.toMutableMap().apply { putAll(tags) }
            val result = repository.updateNode(nodeId, mergedTags)
            _editState.value = result.fold(
                onSuccess = { OsmEditState.Success("osm:node:${nodeId}") },
                onFailure = { OsmEditState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun reset() {
        _editState.value = OsmEditState.Idle
    }
}

class OsmEditViewModelFactory(
    private val repository: OreoregeoRepository,
    private val appContext: Context,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OsmEditViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OsmEditViewModel(repository, appContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

private fun extractNodeId(placeKey: String): Long? {
    if (!placeKey.startsWith("osm:node:")) return null
    return placeKey.removePrefix("osm:node:").toLongOrNull()
}
