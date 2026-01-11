package com.zelretch.oreoregeo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.zelretch.oreoregeo.domain.Checkin
import com.zelretch.oreoregeo.ui.HistoryScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * HistoryScreenのUIテスト
 * チェックイン履歴画面の表示をテストします
 */
@RunWith(AndroidJUnit4::class)
class HistoryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun historyScreen_displaysEmptyState() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                HistoryScreen(
                    checkins = emptyList(),
                    placeNameQuery = "",
                    startDate = null,
                    endDate = null,
                    onPlaceNameQueryChange = {},
                    onStartDateChange = {},
                    onEndDateChange = {},
                    onClearFilters = {},
                    onDeleteClick = {}
                )
            }
        }

        // 空の履歴メッセージが表示されることを確認
        composeTestRule.onNodeWithText(
            context.getString(R.string.no_checkins_yet)
        ).assertIsDisplayed()
    }

    @Test
    fun historyScreen_displaysCheckins() {
        val testCheckins = listOf(
            Checkin(
                id = 1,
                placeKey = "node/123",
                visitedAt = System.currentTimeMillis(),
                note = "テストノート1",
                place = null
            ),
            Checkin(
                id = 2,
                placeKey = "node/456",
                visitedAt = System.currentTimeMillis(),
                note = "",
                place = null
            )
        )

        composeTestRule.setContent {
            OreoregeoTheme {
                HistoryScreen(
                    checkins = testCheckins,
                    placeNameQuery = "",
                    startDate = null,
                    endDate = null,
                    onPlaceNameQueryChange = {},
                    onStartDateChange = {},
                    onEndDateChange = {},
                    onClearFilters = {},
                    onDeleteClick = {}
                )
            }
        }

        // チェックインが表示されることを確認
        composeTestRule.onNodeWithText("node/123").assertIsDisplayed()
        composeTestRule.onNodeWithText("node/456").assertIsDisplayed()
        composeTestRule.onNodeWithText("テストノート1").assertIsDisplayed()
    }

    @Test
    fun historyScreen_displaysSearchButton() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                HistoryScreen(
                    checkins = emptyList(),
                    placeNameQuery = "",
                    startDate = null,
                    endDate = null,
                    onPlaceNameQueryChange = {},
                    onStartDateChange = {},
                    onEndDateChange = {},
                    onClearFilters = {},
                    onDeleteClick = {}
                )
            }
        }

        // Search button should be displayed when no filters are active
        composeTestRule.onNodeWithText(
            context.getString(R.string.search)
        ).assertIsDisplayed()
    }
}
