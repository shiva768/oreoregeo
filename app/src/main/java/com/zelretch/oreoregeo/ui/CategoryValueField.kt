package com.zelretch.oreoregeo.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.zelretch.oreoregeo.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("FunctionNaming")
fun CategoryValueField(
    category: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val options = getCategoryOptions(category)

    val filteredOptions by remember(value, options) {
        derivedStateOf {
            if (value.isEmpty()) {
                options
            } else {
                options.filter { it.contains(value, ignoreCase = true) }
            }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(stringResource(R.string.category_value_label, category)) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = true)
                .testTag("categoryValueField"),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        if (filteredOptions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                filteredOptions.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            onValueChange(selectionOption)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

@Composable
private fun getCategoryOptions(category: String): List<String> = when (category) {
    "amenity" -> listOf(
        stringResource(R.string.amenity_restaurant),
        stringResource(R.string.amenity_cafe),
        stringResource(R.string.amenity_fast_food),
        stringResource(R.string.amenity_bar),
        stringResource(R.string.amenity_pub),
        stringResource(R.string.amenity_convenience),
        stringResource(R.string.amenity_vending_machine),
        stringResource(R.string.amenity_parking),
        stringResource(R.string.amenity_bench),
        stringResource(R.string.amenity_toilets),
        stringResource(R.string.amenity_post_box)
    )
    "shop" -> listOf(
        stringResource(R.string.shop_convenience),
        stringResource(R.string.shop_supermarket),
        stringResource(R.string.shop_clothes),
        stringResource(R.string.shop_hairdresser),
        stringResource(R.string.shop_bakery),
        stringResource(R.string.shop_drugstore)
    )
    "tourism" -> listOf(
        stringResource(R.string.tourism_information),
        stringResource(R.string.tourism_attraction),
        stringResource(R.string.tourism_viewpoint),
        stringResource(R.string.tourism_hotel),
        stringResource(R.string.tourism_museum),
        stringResource(R.string.tourism_artwork)
    )
    else -> emptyList()
}
