package com.zelretch.oreoregeo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.zelretch.oreoregeo.OreoregeoTheme
import com.zelretch.oreoregeo.ui.AddPlaceScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * AddPlaceScreenのUIテスト
 * 新規スポット追加画面の表示と操作をテストします
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

        // Category chips should be displayed (capitalized)
        composeTestRule.onNodeWithText(context.getString(R.string.amenity)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.shop)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.tourism)).assertIsDisplayed()
    }

    @Test
    fun addPlaceScreen_handlesNullCoordinates() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                AddPlaceScreen(
                    currentLat = null,
                    currentLon = null,
                    onSave = { _, _, _ -> },
                    onCancel = {}
                )
            }
        }

        // Fields should be empty when coordinates are null
        composeTestRule.onNodeWithText(
            context.getString(R.string.latitude_required)
        ).assertIsDisplayed()

        composeTestRule.onNodeWithText(
            context.getString(R.string.longitude_required)
        ).assertIsDisplayed()
    }
}
