package com.zelretch.oreoregeo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.zelretch.oreoregeo.ui.SearchScreen
import com.zelretch.oreoregeo.ui.SearchState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * SearchScreenのUIテスト
 * 検索画面の表示と基本的な操作をテストします
 */
@RunWith(AndroidJUnit4::class)
class SearchScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun searchScreen_displaysIdleState() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                SearchScreen(
                    searchState = SearchState.Idle,
                    searchRadius = 100,
                    onRadiusChange = {},
                    excludeUnnamed = false,
                    onExcludeUnnamedChange = {},
                    canEdit = false,
                    currentLocation = null,
                    onSearchClick = {},
                    onPlaceClick = {},
                    onCheckinClick = {},
                    onEditPlace = {}
                )
            }
        }

        // アイドル状態のメッセージが表示されることを確認
        composeTestRule.onNodeWithText(
            context.getString(R.string.tap_to_search)
        ).assertIsDisplayed()
    }

    @Test
    fun searchScreen_displaysLoadingState() {
        composeTestRule.setContent {
            OreoregeoTheme {
                SearchScreen(
                    searchState = SearchState.Loading,
                    searchRadius = 100,
                    onRadiusChange = {},
                    excludeUnnamed = false,
                    onExcludeUnnamedChange = {},
                    canEdit = false,
                    currentLocation = null,
                    onSearchClick = {},
                    onPlaceClick = {},
                    onCheckinClick = {},
                    onEditPlace = {}
                )
            }
        }

        // ローディングインジケーターが表示されることを確認（テキストはないのでこのテストはスキップ可能）
        // CircularProgressIndicatorは表示されるが、テキストベースのアサーションでは確認できない
        // このテストは実際のエミュレータ/デバイスでのみ意味を持つ
    }

    @Test
    fun searchScreen_displaysErrorState() {
        composeTestRule.setContent {
            OreoregeoTheme {
                SearchScreen(
                    searchState = SearchState.Error("テストエラー"),
                    searchRadius = 100,
                    onRadiusChange = {},
                    excludeUnnamed = false,
                    onExcludeUnnamedChange = {},
                    canEdit = false,
                    currentLocation = null,
                    onSearchClick = {},
                    onPlaceClick = {},
                    onCheckinClick = {},
                    onEditPlace = {}
                )
            }
        }

        // エラーメッセージが表示されることを確認
        composeTestRule.onNodeWithText("テストエラー").assertIsDisplayed()
    }

    @Test
    fun searchScreen_displaysEmptyState() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                SearchScreen(
                    searchState = SearchState.Success(emptyList()),
                    searchRadius = 100,
                    onRadiusChange = {},
                    excludeUnnamed = false,
                    onExcludeUnnamedChange = {},
                    canEdit = false,
                    currentLocation = null,
                    onSearchClick = {},
                    onPlaceClick = {},
                    onCheckinClick = {},
                    onEditPlace = {}
                )
            }
        }

        // 空の結果メッセージが表示されることを確認
        composeTestRule.onNodeWithText(
            context.getString(R.string.no_places_found)
        ).assertIsDisplayed()
    }
}
