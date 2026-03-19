package com.zelretch.oreoregeo.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.zelretch.oreoregeo.domain.PlaceWithDistance

class AddPlaceState(
    currentLat: Double?,
    currentLon: Double?,
    initialEditState: OsmEditState
) {
    var lat by mutableStateOf(currentLat?.toString() ?: "")
    var lon by mutableStateOf(currentLon?.toString() ?: "")
    var name by mutableStateOf("")
    var category by mutableStateOf("amenity")
    var categoryValue by mutableStateOf("")
    var additionalTags by mutableStateOf("")
    var isSaving by mutableStateOf(false)
    var isMapReady by mutableStateOf(false)
    var showDuplicateDialog by mutableStateOf(false)
    var nearbyPlaces by mutableStateOf<List<PlaceWithDistance>>(emptyList())

    init {
        if (initialEditState is OsmEditState.ConfirmDuplicate) {
            nearbyPlaces = initialEditState.nearbyPlaces
            showDuplicateDialog = true
        }
    }

    fun isInputValid(): Boolean = lat.toDoubleOrNull() != null &&
        lon.toDoubleOrNull() != null &&
        name.isNotBlank() &&
        categoryValue.isNotBlank()

    fun createTagsMap(): Map<String, String> {
        val tags = mutableMapOf("name" to name, category to categoryValue)
        if (additionalTags.isNotBlank()) {
            additionalTags.split(",").forEach { tagPair ->
                val parts = tagPair.trim().split("=")
                if (parts.size == 2) {
                    tags[parts[0].trim()] = parts[1].trim()
                }
            }
        }
        return tags
    }
}
