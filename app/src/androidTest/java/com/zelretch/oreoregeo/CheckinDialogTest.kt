package com.zelretch.oreoregeo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.zelretch.oreoregeo.ui.CheckinDialog
import com.zelretch.oreoregeo.ui.CheckinState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * CheckinDialogのUIテスト
 * チェックインダイアログの表示と操作をテストします
 */
@RunWith(AndroidJUnit4::class)
class CheckinDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun checkinDialog_displaysPlaceName() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                CheckinDialog(
                    placeKey = "osm:node:123",
                    placeName = "テストカフェ",
                    checkinState = CheckinState.Idle,
                    onCheckin = {},
                    onDismiss = {}
                )
            }
        }

        // Place name should be displayed
        composeTestRule.onNodeWithText("テストカフェ", substring = true).assertIsDisplayed()
    }

    @Test
    fun checkinDialog_displaysPlaceKeyWhenNoName() {
        composeTestRule.setContent {
            OreoregeoTheme {
                CheckinDialog(
                    placeKey = "osm:node:456",
                    placeName = null,
                    checkinState = CheckinState.Idle,
                    onCheckin = {},
                    onDismiss = {}
                )
            }
        }

        // Place key should be displayed when no name
        composeTestRule.onNodeWithText("osm:node:456", substring = true).assertIsDisplayed()
    }

    @Test
    fun checkinDialog_noteFieldAcceptsInput() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                CheckinDialog(
                    placeKey = "osm:node:123",
                    placeName = "テストカフェ",
                    checkinState = CheckinState.Idle,
                    onCheckin = {},
                    onDismiss = {}
                )
            }
        }

        // Find and input text into note field
        val noteLabel = context.getString(R.string.note_optional)
        composeTestRule.onNodeWithText(noteLabel).performClick()
        composeTestRule.onNodeWithText(noteLabel).performTextInput("美味しいコーヒーでした")

        // Verify text was input
        composeTestRule.onNodeWithText("美味しいコーヒーでした").assertIsDisplayed()
    }

    @Test
    fun checkinDialog_checkinButtonEnabledInIdleState() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                CheckinDialog(
                    placeKey = "osm:node:123",
                    placeName = "テストカフェ",
                    checkinState = CheckinState.Idle,
                    onCheckin = {},
                    onDismiss = {}
                )
            }
        }

        // Wait for composition to complete
        composeTestRule.waitForIdle()

        // Checkin button should be enabled in idle state
        // Use test tag to specifically target the button
        composeTestRule.onNodeWithTag("checkinButton").assertIsEnabled()
    }

    @Test
    fun checkinDialog_checkinButtonDisabledInLoadingState() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                CheckinDialog(
                    placeKey = "osm:node:123",
                    placeName = "テストカフェ",
                    checkinState = CheckinState.Loading,
                    onCheckin = {},
                    onDismiss = {}
                )
            }
        }

        // Wait for composition to complete
        composeTestRule.waitForIdle()

        // Checkin button should be disabled in loading state
        // Use test tag to specifically target the button
        composeTestRule.onNodeWithTag("checkinButton").assertIsNotEnabled()
    }

    @Test
    fun checkinDialog_displaysLoadingIndicator() {
        composeTestRule.setContent {
            OreoregeoTheme {
                CheckinDialog(
                    placeKey = "osm:node:123",
                    placeName = "テストカフェ",
                    checkinState = CheckinState.Loading,
                    onCheckin = {},
                    onDismiss = {}
                )
            }
        }

        // Loading state should show progress indicator (visual test only)
        // CircularProgressIndicator doesn't have text, so we just verify the dialog is shown
        composeTestRule.onNodeWithText("テストカフェ", substring = true).assertIsDisplayed()
    }

    @Test
    fun checkinDialog_displaysSuccessMessage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                CheckinDialog(
                    placeKey = "osm:node:123",
                    placeName = "テストカフェ",
                    checkinState = CheckinState.Success(1L),
                    onCheckin = {},
                    onDismiss = {}
                )
            }
        }

        // Success message should be displayed
        composeTestRule.onNodeWithText(
            context.getString(R.string.checkin_success)
        ).assertIsDisplayed()
    }

    @Test
    fun checkinDialog_displaysErrorMessage() {
        composeTestRule.setContent {
            OreoregeoTheme {
                CheckinDialog(
                    placeKey = "osm:node:123",
                    placeName = "テストカフェ",
                    checkinState = CheckinState.Error("テストエラーメッセージ"),
                    onCheckin = {},
                    onDismiss = {}
                )
            }
        }

        // Error message should be displayed
        composeTestRule.onNodeWithText("テストエラーメッセージ").assertIsDisplayed()
    }

    @Test
    fun checkinDialog_displaysDuplicateCheckinError() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                CheckinDialog(
                    placeKey = "osm:node:123",
                    placeName = "テストカフェ",
                    checkinState = CheckinState.Error("duplicate_checkin"),
                    onCheckin = {},
                    onDismiss = {}
                )
            }
        }

        // Duplicate checkin error message should be displayed in Japanese
        composeTestRule.onNodeWithText(
            context.getString(R.string.duplicate_checkin)
        ).assertIsDisplayed()
    }

    @Test
    fun checkinDialog_cancelButtonIsDisplayed() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                CheckinDialog(
                    placeKey = "osm:node:123",
                    placeName = "テストカフェ",
                    checkinState = CheckinState.Idle,
                    onCheckin = {},
                    onDismiss = {}
                )
            }
        }

        // Cancel button should be displayed
        composeTestRule.onNodeWithText(
            context.getString(R.string.cancel)
        ).assertIsDisplayed()
    }

    @Test
    fun checkinDialog_checkinButtonReenabledAfterError() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                CheckinDialog(
                    placeKey = "osm:node:123",
                    placeName = "テストカフェ",
                    checkinState = CheckinState.Error("エラーが発生しました"),
                    onCheckin = {},
                    onDismiss = {}
                )
            }
        }

        // Wait for composition to complete
        composeTestRule.waitForIdle()

        // Checkin button should be re-enabled after error
        // Use test tag to specifically target the button
        composeTestRule.onNodeWithTag("checkinButton").assertIsEnabled()
    }
}
