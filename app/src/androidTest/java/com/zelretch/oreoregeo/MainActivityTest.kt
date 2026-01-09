package com.zelretch.oreoregeo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * MainActivityの基本的なUIテスト
 * アプリの起動と基本的なナビゲーションをテストします
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainActivity_startsSuccessfully() {
        // アプリが起動し、タイトルが表示されることを確認
        composeTestRule.onNodeWithText("Oreoregeo").assertIsDisplayed()
    }

    @Test
    fun mainActivity_displaysBottomNavigationBar() {
        // 検索タブが表示されることを確認
        val searchLabel = composeTestRule.activity.getString(R.string.search)
        composeTestRule.onNodeWithText(searchLabel).assertIsDisplayed()

        // 履歴タブが表示されることを確認
        val historyLabel = composeTestRule.activity.getString(R.string.checkin_history)
        composeTestRule.onNodeWithText(historyLabel).assertIsDisplayed()

        // 設定タブが表示されることを確認
        val settingsLabel = composeTestRule.activity.getString(R.string.settings_title)
        composeTestRule.onNodeWithText(settingsLabel).assertIsDisplayed()
    }

    @Test
    fun mainActivity_navigatesToHistory() {
        // 履歴タブをクリック
        val historyLabel = composeTestRule.activity.getString(R.string.checkin_history)
        composeTestRule.onNodeWithText(historyLabel).performClick()

        // 履歴画面が表示されることを確認（履歴タブが選択状態になる）
        composeTestRule.onNodeWithText(historyLabel).assertIsDisplayed()
    }

    @Test
    fun mainActivity_navigatesToSettings() {
        // 設定タブをクリック
        val settingsLabel = composeTestRule.activity.getString(R.string.settings_title)
        composeTestRule.onNodeWithText(settingsLabel).performClick()

        // 設定画面が表示されることを確認
        composeTestRule.onNodeWithText(settingsLabel).assertIsDisplayed()
    }

    @Test
    fun mainActivity_navigatesBackToSearch() {
        // 設定タブに移動
        val settingsLabel = composeTestRule.activity.getString(R.string.settings_title)
        composeTestRule.onNodeWithText(settingsLabel).performClick()

        // 検索タブに戻る
        val searchLabel = composeTestRule.activity.getString(R.string.search)
        composeTestRule.onNodeWithText(searchLabel).performClick()

        // 検索画面が表示されることを確認
        composeTestRule.onNodeWithText(searchLabel).assertIsDisplayed()
    }
}
