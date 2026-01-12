package com.zelretch.oreoregeo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.zelretch.oreoregeo.ui.EditTagsScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * EditTagsScreenのUIテスト
 * タグ編集画面の表示と操作をテストします
 */
@RunWith(AndroidJUnit4::class)
class EditTagsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun editTagsScreen_displaysTitle() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                EditTagsScreen(
                    placeKey = "osm:node:123",
                    existingTags = emptyMap(),
                    onSave = { _, _ -> },
                    onCancel = {}
                )
            }
        }

        // Title should be displayed
        composeTestRule.onNodeWithText(
            context.getString(R.string.edit_tags_title)
        ).assertIsDisplayed()
    }

    @Test
    fun editTagsScreen_displaysPlaceKey() {
        composeTestRule.setContent {
            OreoregeoTheme {
                EditTagsScreen(
                    placeKey = "osm:node:456",
                    existingTags = emptyMap(),
                    onSave = { _, _ -> },
                    onCancel = {}
                )
            }
        }

        // Place key should be displayed
        composeTestRule.onNodeWithText("osm:node:456", substring = true).assertIsDisplayed()
    }

    @Test
    fun editTagsScreen_displaysExistingTags() {
        val existingTags = mapOf(
            "name" to "テストカフェ",
            "amenity" to "cafe",
            "cuisine" to "coffee"
        )

        composeTestRule.setContent {
            OreoregeoTheme {
                EditTagsScreen(
                    placeKey = "osm:node:123",
                    existingTags = existingTags,
                    onSave = { _, _ -> },
                    onCancel = {}
                )
            }
        }

        // Existing tags should be displayed
        composeTestRule.onNodeWithText("name", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("テストカフェ", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("amenity", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("cafe", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("cuisine", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("coffee", substring = true).assertIsDisplayed()
    }

    @Test
    fun editTagsScreen_displaysAddNewTagSection() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                EditTagsScreen(
                    placeKey = "osm:node:123",
                    existingTags = emptyMap(),
                    onSave = { _, _ -> },
                    onCancel = {}
                )
            }
        }

        // Add new tag section should be displayed
        composeTestRule.onNodeWithText(
            context.getString(R.string.add_new_tag)
        ).assertIsDisplayed()
    }

    @Test
    fun editTagsScreen_newTagFieldsAcceptInput() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                EditTagsScreen(
                    placeKey = "osm:node:123",
                    existingTags = emptyMap(),
                    onSave = { _, _ -> },
                    onCancel = {}
                )
            }
        }

        // Input text into key field
        val keyLabel = context.getString(R.string.key_label)
        composeTestRule.onNodeWithText(keyLabel).performClick()
        composeTestRule.onNodeWithText(keyLabel).performTextInput("opening_hours")

        // Verify text was input
        composeTestRule.onNodeWithText("opening_hours").assertIsDisplayed()

        // Input text into value field
        val valueLabel = context.getString(R.string.value_label)
        composeTestRule.onNodeWithText(valueLabel).performClick()
        composeTestRule.onNodeWithText(valueLabel).performTextInput("Mo-Fr 09:00-18:00")

        // Verify text was input
        composeTestRule.onNodeWithText("Mo-Fr 09:00-18:00").assertIsDisplayed()
    }

    @Test
    fun editTagsScreen_displaysSaveButton() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                EditTagsScreen(
                    placeKey = "osm:node:123",
                    existingTags = emptyMap(),
                    onSave = { _, _ -> },
                    onCancel = {}
                )
            }
        }

        // Save button should be displayed
        composeTestRule.onNodeWithText(
            context.getString(R.string.save)
        ).assertIsDisplayed()
    }

    @Test
    fun editTagsScreen_displaysCancelButton() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                EditTagsScreen(
                    placeKey = "osm:node:123",
                    existingTags = emptyMap(),
                    onSave = { _, _ -> },
                    onCancel = {}
                )
            }
        }

        // Cancel button should be displayed
        composeTestRule.onNodeWithText(
            context.getString(R.string.cancel)
        ).assertIsDisplayed()
    }

    @Test
    fun editTagsScreen_handlesEmptyTags() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                EditTagsScreen(
                    placeKey = "osm:node:123",
                    existingTags = emptyMap(),
                    onSave = { _, _ -> },
                    onCancel = {}
                )
            }
        }

        // Title should still be displayed even with empty tags
        composeTestRule.onNodeWithText(
            context.getString(R.string.edit_tags_title)
        ).assertIsDisplayed()

        // Add new tag section should be displayed
        composeTestRule.onNodeWithText(
            context.getString(R.string.add_new_tag)
        ).assertIsDisplayed()
    }

    @Test
    fun editTagsScreen_displaysMultipleTags() {
        val existingTags = mapOf(
            "name" to "テストレストラン",
            "amenity" to "restaurant",
            "cuisine" to "japanese",
            "opening_hours" to "11:00-22:00",
            "phone" to "+81-3-1234-5678"
        )

        composeTestRule.setContent {
            OreoregeoTheme {
                EditTagsScreen(
                    placeKey = "osm:node:789",
                    existingTags = existingTags,
                    onSave = { _, _ -> },
                    onCancel = {}
                )
            }
        }

        // All tags should be displayed
        composeTestRule.onNodeWithText("name", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("テストレストラン", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("amenity", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("restaurant", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("cuisine", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("japanese", substring = true).assertIsDisplayed()
    }
}
