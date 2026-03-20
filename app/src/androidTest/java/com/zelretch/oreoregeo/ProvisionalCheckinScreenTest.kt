package com.zelretch.oreoregeo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.zelretch.oreoregeo.domain.ProvisionalCheckin
import com.zelretch.oreoregeo.ui.ProvisionalCheckinConfirmState
import com.zelretch.oreoregeo.ui.ProvisionalCheckinScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProvisionalCheckinScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun provisionalCheckinScreen_showsEmptyMessage_whenNoPending() {
        composeTestRule.setContent {
            OreoregeoTheme {
                ProvisionalCheckinScreen(
                    pendingCheckins = emptyList(),
                    confirmState = ProvisionalCheckinConfirmState.Idle,
                    onConfirm = { _, _, _ -> },
                    onDismiss = {},
                    onConfirmStateReset = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.no_provisional_checkins))
            .assertIsDisplayed()
    }

    @Test
    fun provisionalCheckinScreen_showsPlaceName() {
        val checkin = ProvisionalCheckin(
            id = 1L,
            placeKey = "osm:node:123",
            placeName = "テストカフェ",
            detectedAt = System.currentTimeMillis(),
            lat = 35.0,
            lon = 139.0
        )

        composeTestRule.setContent {
            OreoregeoTheme {
                ProvisionalCheckinScreen(
                    pendingCheckins = listOf(checkin),
                    confirmState = ProvisionalCheckinConfirmState.Idle,
                    onConfirm = { _, _, _ -> },
                    onDismiss = {},
                    onConfirmStateReset = {}
                )
            }
        }

        composeTestRule.onNodeWithText("テストカフェ").assertIsDisplayed()
    }

    @Test
    fun provisionalCheckinScreen_confirmButtonOpensDialog() {
        val checkin = ProvisionalCheckin(
            id = 1L,
            placeKey = "osm:node:123",
            placeName = "テストカフェ",
            detectedAt = System.currentTimeMillis(),
            lat = 35.0,
            lon = 139.0
        )

        composeTestRule.setContent {
            OreoregeoTheme {
                ProvisionalCheckinScreen(
                    pendingCheckins = listOf(checkin),
                    confirmState = ProvisionalCheckinConfirmState.Idle,
                    onConfirm = { _, _, _ -> },
                    onDismiss = {},
                    onConfirmStateReset = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.provisional_confirm))
            .performClick()

        composeTestRule
            .onNodeWithText(context.getString(R.string.provisional_confirm_title))
            .assertIsDisplayed()
    }

    @Test
    fun provisionalCheckinScreen_dismissCallsOnDismiss() {
        var dismissedId: Long? = null
        val checkin = ProvisionalCheckin(
            id = 42L,
            placeKey = "osm:node:123",
            placeName = "テストカフェ",
            detectedAt = System.currentTimeMillis(),
            lat = 35.0,
            lon = 139.0
        )

        composeTestRule.setContent {
            OreoregeoTheme {
                ProvisionalCheckinScreen(
                    pendingCheckins = listOf(checkin),
                    confirmState = ProvisionalCheckinConfirmState.Idle,
                    onConfirm = { _, _, _ -> },
                    onDismiss = { dismissedId = it },
                    onConfirmStateReset = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.provisional_dismiss))
            .performClick()

        assert(dismissedId == 42L)
    }
}
