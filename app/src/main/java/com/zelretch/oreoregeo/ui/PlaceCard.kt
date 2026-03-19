package com.zelretch.oreoregeo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zelretch.oreoregeo.R
import com.zelretch.oreoregeo.domain.PlaceWithDistance
import java.text.DecimalFormat

private const val KM_THRESHOLD = 1000

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("FunctionNaming", "LongParameterList")
fun PlaceCard(
    placeWithDistance: PlaceWithDistance,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onCheckinClick: () -> Unit,
    onEditClick: (() -> Unit)? = null
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = if (isSelected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        }
    ) {
        PlaceCardContent(placeWithDistance, isSelected, onCheckinClick, onEditClick)
    }
}

@Composable
@Suppress("FunctionNaming", "LongParameterList")
private fun PlaceCardContent(
    placeWithDistance: PlaceWithDistance,
    isSelected: Boolean,
    onCheckinClick: () -> Unit,
    onEditClick: (() -> Unit)? = null
) {
    val place = placeWithDistance.place
    val distanceText = formatDistance(placeWithDistance.distanceMeters)

    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = place.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = place.category,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = distanceText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        PlaceCardActions(isSelected, onCheckinClick, onEditClick)
    }
}

@Composable
@Suppress("FunctionNaming")
private fun PlaceCardActions(
    isSelected: Boolean,
    onCheckinClick: () -> Unit,
    onEditClick: (() -> Unit)? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isSelected) {
            Button(
                onClick = onCheckinClick,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(stringResource(R.string.checkin))
            }
            Spacer(Modifier.width(8.dp))
        }

        if (onEditClick != null) {
            IconButton(onClick = onEditClick) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit_tags_desc),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun formatDistance(distanceMeters: Float): String {
    val df = DecimalFormat("#.#")
    return if (distanceMeters < KM_THRESHOLD) {
        "${df.format(distanceMeters)}m"
    } else {
        "${df.format(distanceMeters / KM_THRESHOLD)}km"
    }
}
