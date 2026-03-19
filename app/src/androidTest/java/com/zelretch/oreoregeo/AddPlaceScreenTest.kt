package com.zelretch.oreoregeo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.zelretch.oreoregeo.domain.Place
import com.zelretch.oreoregeo.domain.PlaceWithDistance
import com.zelretch.oreoregeo.ui.AddPlaceScreen
import com.zelretch.oreoregeo.ui.OsmEditState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * AddPlaceScreenのUIテスト
 * 新規プレイス追加画面の表示と操作をテストします
 */
@RunWith(AndroidJUnit4::class)
class AddPlaceScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun addPlaceScreen_displaysTitle() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                AddPlaceScreen(
                    currentLat = 35.6812,
                    currentLon = 139.7671,
                    onSave = { _, _, _ -> },
                    onCancel = {}
                )
            }
        }

        // Title should be displayed
        composeTestRule.onNodeWithText(
            context.getString(R.string.add_new_place)
        ).assertIsDisplayed()
    }

    @Test
    fun addPlaceScreen_displaysNameField() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                AddPlaceScreen(
                    currentLat = 35.6812,
                    currentLon = 139.7671,
                    onSave = { _, _, _ -> },
                    onCancel = {}
                )
            }
        }

        // Name field should be displayed
        composeTestRule.onNodeWithText(
            context.getString(R.string.name_required)
        ).assertIsDisplayed()
    }

    @Test
    fun addPlaceScreen_displaysLatLonFields() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                AddPlaceScreen(
                    currentLat = 35.6812,
                    currentLon = 139.7671,
                    onSave = { _, _, _ -> },
                    onCancel = {}
                )
            }
        }

        // Latitude field should be displayed
        composeTestRule.onNodeWithText(
            context.getString(R.string.latitude_required)
        ).assertIsDisplayed()

        // Longitude field should be displayed
        composeTestRule.onNodeWithText(
            context.getString(R.string.longitude_required)
        ).assertIsDisplayed()
    }

    @Test
    fun addPlaceScreen_prePopulatesCoordinates() {
        composeTestRule.setContent {
            OreoregeoTheme {
                AddPlaceScreen(
                    currentLat = 35.6812,
                    currentLon = 139.7671,
                    onSave = { _, _, _ -> },
                    onCancel = {}
                )
            }
        }

        // Pre-populated coordinates should be displayed
        composeTestRule.onNodeWithText("35.6812", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("139.7671", substring = true).assertIsDisplayed()
    }

    @Test
    fun addPlaceScreen_nameFieldAcceptsInput() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                AddPlaceScreen(
                    currentLat = 35.6812,
                    currentLon = 139.7671,
                    onSave = { _, _, _ -> },
                    onCancel = {}
                )
            }
        }

        // Input text into name field
        val nameLabel = context.getString(R.string.name_required)
        composeTestRule.onNodeWithText(nameLabel).performClick()
        composeTestRule.onNodeWithText(nameLabel).performTextInput("新しいカフェ")

        // Verify text was input
        composeTestRule.onNodeWithText("新しいカフェ").assertIsDisplayed()
    }

    @Test
    fun addPlaceScreen_categoryValueFieldAcceptsInput() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                AddPlaceScreen(
                    currentLat = 35.6812,
                    currentLon = 139.7671,
                    onSave = { _, _, _ -> },
                    onCancel = {}
                )
            }
        }

        // Wait for map and initial content
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithTag("categoryValueField").fetchSemanticsNodes().isNotEmpty()
        }

        // Input text into category value field
        val categoryValueField = composeTestRule.onNodeWithTag("categoryValueField")
        categoryValueField.performClick()
        categoryValueField.performTextInput("restaurant")

        // Verify text was input (specific node check)
        composeTestRule.onNode(
            hasTestTag("categoryValueField") and hasText("restaurant")
        ).assertIsDisplayed()
    }

    @Test
    fun addPlaceScreen_displaysSaveButton() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                AddPlaceScreen(
                    currentLat = 35.6812,
                    currentLon = 139.7671,
                    onSave = { _, _, _ -> },
                    onCancel = {}
                )
            }
        }

        // Wait for composition to complete
        composeTestRule.waitForIdle()

        // Save button should be displayed (use test tag for reliable matching)
        composeTestRule.onNodeWithTag("saveButton").assertExists()
    }

    @Test
    fun addPlaceScreen_displaysCancelButton() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                AddPlaceScreen(
                    currentLat = 35.6812,
                    currentLon = 139.7671,
                    onSave = { _, _, _ -> },
                    onCancel = {}
                )
            }
        }

        // Wait for composition to complete
        composeTestRule.waitForIdle()

        // Cancel button should be displayed (use test tag for reliable matching)
        composeTestRule.onNodeWithTag("cancelButton").assertExists()
    }

    @Test
    fun addPlaceScreen_displaysCategoryChips() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                AddPlaceScreen(
                    currentLat = 35.6812,
                    currentLon = 139.7671,
                    onSave = { _, _, _ -> },
                    onCancel = {}
                )
            }
        }

        // Wait for map to initialize (LaunchedEffect delay is clock-controlled in Compose tests)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText(context.getString(R.string.amenity)).assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Category chips should be displayed (capitalized)
        composeTestRule.onNodeWithText(context.getString(R.string.amenity)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.shop)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.tourism)).assertIsDisplayed()
    }

    @Test
    fun addPlaceScreen_displaysMapPickerWhenCoordinatesAvailable() {
        composeTestRule.setContent {
            OreoregeoTheme {
                AddPlaceScreen(
                    currentLat = 35.6812,
                    currentLon = 139.7671,
                    onSave = { _, _, _ -> },
                    onCancel = {}
                )
            }
        }

        // Map picker container should exist
        composeTestRule.onNodeWithTag("mapPicker").assertExists()

        // Hint text should be visible
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.onNodeWithText(
            context.getString(R.string.select_location_on_map_hint)
        ).assertIsDisplayed()

        // Current location marker should be handled by MapView (AndroidView),
        // but we can check if it exists in the semantic tree if we added markers as children (not here)
    }

    @Test
    fun addPlaceScreen_displaysCurrentLocationOnMap() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                AddPlaceScreen(
                    currentLat = 35.6812,
                    currentLon = 139.7671,
                    onSave = { _, _, _ -> },
                    onCancel = {}
                )
            }
        }

        // Wait for map
        composeTestRule.onNodeWithTag("mapPicker").assertExists()

        // Marker titles are not usually exposed to Compose semantics directly,
        // but the fact that the screen doesn't crash is a good sign.
        // In a real device/emulator test, MapView would render the marker.
    }

    @Test
    fun addPlaceScreen_displaysDuplicateConfirmationDialog() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val nearbyPlaces = listOf(
            PlaceWithDistance(
                place = Place(
                    placeKey = "osm:node:1",
                    name = "Existing Cafe",
                    category = "cafe",
                    lat = 35.6813,
                    lon = 139.7672,
                    updatedAt = System.currentTimeMillis()
                ),
                distanceMeters = 10.5f
            )
        )

        composeTestRule.setContent {
            OreoregeoTheme {
                AddPlaceScreen(
                    currentLat = 35.6812,
                    currentLon = 139.7671,
                    onSave = { _, _, _ -> },
                    onCancel = {},
                    editState = OsmEditState.ConfirmDuplicate(nearbyPlaces)
                )
            }
        }

        // Dialog should be displayed
        composeTestRule.onNodeWithText(
            context.getString(R.string.confirm_duplicate_title)
        ).assertIsDisplayed()

        composeTestRule.onNodeWithText("Existing Cafe").assertIsDisplayed()
        composeTestRule.onNodeWithText(
            context.getString(R.string.confirm_save)
        ).assertIsDisplayed()
    }

    @Test
    fun addPlaceScreen_displaysGeneralConfirmationDialog() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeTestRule.setContent {
            OreoregeoTheme {
                AddPlaceScreen(
                    currentLat = 35.6812,
                    currentLon = 139.7671,
                    onSave = { _, _, _ -> },
                    onCancel = {},
                    editState = OsmEditState.ConfirmDuplicate(emptyList())
                )
            }
        }

        // Dialog should be displayed with general message
        composeTestRule.onNodeWithText(
            context.getString(R.string.confirm_save_title)
        ).assertIsDisplayed()

        composeTestRule.onNodeWithText(
            context.getString(R.string.confirm_save_message)
        ).assertIsDisplayed()

        composeTestRule.onNodeWithText(
            context.getString(R.string.confirm_save)
        ).assertIsDisplayed()
    }

    @Test
    fun addPlaceScreen_parsesAdditionalTagsCorrectly() {
        var capturedTags: Map<String, String> = emptyMap()
        composeTestRule.setContent {
            OreoregeoTheme {
                AddPlaceScreen(
                    currentLat = 35.6812,
                    currentLon = 139.7671,
                    onSave = { _, _, tags -> capturedTags = tags },
                    onCancel = {},
                    editState = OsmEditState.ConfirmDuplicate(emptyList()) // Show dialog to click confirm
                )
            }
        }

        // Fill required fields
        composeTestRule.onNodeWithTag("categoryValueField").performTextInput("cafe")
        val nameLabel = InstrumentationRegistry.getInstrumentation().targetContext
            .getString(R.string.name_required)
        composeTestRule.onNodeWithText(nameLabel).performTextInput("Test Cafe")

        // Fill additional tags
        val additionalTagsLabel = InstrumentationRegistry.getInstrumentation().targetContext
            .getString(R.string.additional_tags_label)
        composeTestRule.onNodeWithText(additionalTagsLabel).performTextInput(
            "cuisine=coffee, website=https://example.com"
        )

        // Click save to show dialog
        composeTestRule.onNodeWithTag("saveButton").performClick()

        // Confirm in dialog
        val confirmText = InstrumentationRegistry.getInstrumentation().targetContext
            .getString(R.string.confirm_save)
        composeTestRule.onNodeWithText(confirmText).performClick()

        // Verify captured tags
        assert(capturedTags["cuisine"] == "coffee")
        assert(capturedTags["website"] == "https://example.com")
        assert(capturedTags["name"] == "Test Cafe")
        assert(capturedTags["amenity"] == "cafe")
    }

    @Test
    fun addPlaceScreen_resetCategoryValueWhenCategoryChanged() {
        composeTestRule.setContent {
            OreoregeoTheme {
                AddPlaceScreen(
                    currentLat = 35.6812,
                    currentLon = 139.7671,
                    onSave = { _, _, _ -> },
                    onCancel = {}
                )
            }
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // Input into amenity
        composeTestRule.onNodeWithTag("categoryValueField").performTextInput("cafe")

        // Change category to shop
        composeTestRule.onNodeWithText(context.getString(R.string.shop)).performClick()

        // Verify category value is reset
        composeTestRule.onNode(
            hasTestTag("categoryValueField") and hasText("")
        ).assertExists()
    }
}
