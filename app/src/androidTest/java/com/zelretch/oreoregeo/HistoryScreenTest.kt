package com.zelretch.oreoregeo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.zelretch.oreoregeo.domain.Checkin
import com.zelretch.oreoregeo.domain.Place
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
                    areaQuery = "",
                    startDate = null,
                    endDate = null,
                    onPlaceNameQueryChange = {},
                    onAreaQueryChange = {},
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
                    areaQuery = "",
                    startDate = null,
                    endDate = null,
                    onPlaceNameQueryChange = {},
                    onAreaQueryChange = {},
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
    fun historyScreen_tapCheckinWithPlace_showsMapDialog() {
        val placeName = "テストカフェ"
        val testCheckin = Checkin(
            id = 1,
            placeKey = "osm:node:123",
            visitedAt = System.currentTimeMillis(),
            note = "",
            place = Place(
                placeKey = "osm:node:123",
                name = placeName,
                category = "amenity",
                lat = 35.6812,
                lon = 139.7671,
                updatedAt = System.currentTimeMillis()
            )
        )

        composeTestRule.setContent {
            OreoregeoTheme {
                HistoryScreen(
                    checkins = listOf(testCheckin),
                    placeNameQuery = "",
                    areaQuery = "",
                    startDate = null,
                    endDate = null,
                    onPlaceNameQueryChange = {},
                    onAreaQueryChange = {},
                    onStartDateChange = {},
                    onEndDateChange = {},
                    onClearFilters = {},
                    onDeleteClick = {}
                )
            }
        }

        // カードをタップしてマップダイアログが表示されることを確認
        composeTestRule.onAllNodes(androidx.compose.ui.test.hasText(placeName))[0].performClick()
        // ダイアログ内にもプレイス名が表示されること（2件以上になる）
        composeTestRule.onAllNodes(androidx.compose.ui.test.hasText(placeName)).fetchSemanticsNodes().let {
            assert(it.size >= 2) { "ダイアログが表示されていない（ノード数: ${it.size}）" }
        }
    }

    @Test
    fun historyScreen_tapCheckinWithoutPlace_noMapDialog() {
        val testCheckin = Checkin(
            id = 1,
            placeKey = "osm:node:123",
            visitedAt = System.currentTimeMillis(),
            note = "",
            place = null,
            placeName = "場所なしチェックイン"
        )

        composeTestRule.setContent {
            OreoregeoTheme {
                HistoryScreen(
                    checkins = listOf(testCheckin),
                    placeNameQuery = "",
                    areaQuery = "",
                    startDate = null,
                    endDate = null,
                    onPlaceNameQueryChange = {},
                    onAreaQueryChange = {},
                    onStartDateChange = {},
                    onEndDateChange = {},
                    onClearFilters = {},
                    onDeleteClick = {}
                )
            }
        }

        // place が null のカードをタップしてもダイアログが出ないことを確認
        composeTestRule.onNodeWithText("場所なしチェックイン").performClick()
        composeTestRule.waitForIdle()
        // Dialog はなく、カード自体は表示されたまま
        composeTestRule.onNodeWithText("場所なしチェックイン").assertIsDisplayed()
    }

    @Test
    fun historyScreen_displaysSearchButton() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                HistoryScreen(
                    checkins = emptyList(),
                    placeNameQuery = "",
                    areaQuery = "",
                    startDate = null,
                    endDate = null,
                    onPlaceNameQueryChange = {},
                    onAreaQueryChange = {},
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
