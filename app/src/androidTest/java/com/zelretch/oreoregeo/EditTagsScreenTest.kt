package com.zelretch.oreoregeo

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
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
        composeTestRule.onNode(
            hasText("amenity", substring = true) and !hasTestTag("categoryValueField")
        ).assertIsDisplayed()
        composeTestRule.onNode(
            hasText("cafe", substring = true) and hasTestTag("categoryValueField")
        ).assertIsDisplayed()
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

        // Save button should be displayed (uses "Save to OSM" text)
        composeTestRule.onNodeWithText(
            context.getString(R.string.save_to_osm)
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
        composeTestRule.onNode(
            hasText("amenity", substring = true) and !hasTestTag("categoryValueField")
        ).assertIsDisplayed()
        composeTestRule.onNode(
            hasText("restaurant", substring = true) and hasTestTag("categoryValueField")
        ).assertIsDisplayed()
        composeTestRule.onNodeWithText("cuisine", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("japanese", substring = true).assertIsDisplayed()
    }

    @Test
    fun editTagsScreen_categoryValueFieldAcceptsInput() {
        val existingTags = mapOf(
            "amenity" to "cafe"
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

        // Input text into category value field
        val categoryValueField = composeTestRule.onNodeWithTag("categoryValueField")
        categoryValueField.performClick()
        categoryValueField.performTextInput("restaurant")
        composeTestRule.waitForIdle()

        // Verify text was input
        composeTestRule.onNodeWithText("restaurant", substring = true).assertExists()
    }

    @Test
    fun editTagsScreen_showsCategoryValueFieldWhenKeyIsAmenity() {
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

        // Input "amenity" into key field
        val keyLabel = InstrumentationRegistry.getInstrumentation().targetContext
            .getString(R.string.key_label)
        composeTestRule.onNodeWithText(keyLabel).performClick()
        composeTestRule.onNodeWithText(keyLabel).performTextInput("amenity")

        // CategoryValueField should appear for value input
        composeTestRule.waitUntil(3000) {
            composeTestRule.onAllNodesWithTag("categoryValueField").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("categoryValueField").assertIsDisplayed()
    }

    @Test
    fun editTagsScreen_deletesTagCorrectly() {
        var capturedTags: Map<String, String>? = null
        val existingTags = mapOf(
            "name" to "Test Place",
            "amenity" to "cafe"
        )

        composeTestRule.setContent {
            OreoregeoTheme {
                EditTagsScreen(
                    placeKey = "osm:node:123",
                    existingTags = existingTags,
                    onSave = { _, tags -> capturedTags = tags },
                    onCancel = {}
                )
            }
        }

        // Delete "amenity" tag
        // Find the card containing "amenity" and click delete button inside it
        val deleteContentDesc = InstrumentationRegistry.getInstrumentation()
            .targetContext.getString(R.string.delete_tag)

        // We look for a delete icon button where the key "amenity" is present in the same Card
        // Since our UI structure has the key text and delete button in the same Row
        composeTestRule.onAllNodes(
            hasText("amenity", substring = true)
        ).assertCountEquals(2) // Key label and the editable value field (or dropdown)

        // Click delete for amenity
        // In this case, we have two tags. Let's just click the one next to amenity.
        composeTestRule.onAllNodes(hasContentDescription(deleteContentDesc))[1].performClick()

        // Click save
        val saveText = InstrumentationRegistry.getInstrumentation().targetContext
            .getString(R.string.save_to_osm)
        composeTestRule.onNodeWithText(saveText).performClick()

        // Verify captured tags doesn't have amenity
        assert(capturedTags != null)
        assert(capturedTags?.containsKey("amenity") == false)
        assert(capturedTags?.containsKey("name") == true)
    }
}
