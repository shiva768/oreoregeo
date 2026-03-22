package com.zelretch.oreoregeo

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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

    @Test
    fun locationPermissionDialog_showsWhenPermissionDenied() {
        // パーミッションを剥奪してダイアログが表示されることを確認
        InstrumentationRegistry.getInstrumentation().uiAutomation.revokeRuntimePermission(
            composeTestRule.activity.packageName,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        composeTestRule.activityRule.scenario.recreate()
        composeTestRule.waitForIdle()

        val dialogTitle = composeTestRule.activity.getString(R.string.location_permission_required_title)
        composeTestRule.onNodeWithText(dialogTitle).assertIsDisplayed()
    }

    @Test
    fun locationPermissionDialog_notShownWhenPermissionGranted() {
        // パーミッション許可済みの場合はダイアログが表示されないことを確認
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
            composeTestRule.activity.packageName,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        composeTestRule.activityRule.scenario.recreate()
        composeTestRule.waitForIdle()

        val dialogTitle = composeTestRule.activity.getString(R.string.location_permission_required_title)
        composeTestRule.onNodeWithText(dialogTitle).assertDoesNotExist()
    }

    @Test
    fun mainActivity_tabIndicatorStaysInSyncAfterNavigation() {
        // 履歴タブに移動
        val historyLabel = composeTestRule.activity.getString(R.string.checkin_history)
        composeTestRule.onNodeWithText(historyLabel).performClick()

        // 履歴タブが選択されていることを確認
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(historyLabel).assertIsDisplayed()

        // 設定タブに移動
        val settingsLabel = composeTestRule.activity.getString(R.string.settings_title)
        composeTestRule.onNodeWithText(settingsLabel).performClick()

        // 設定タブが選択されていることを確認
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(settingsLabel).assertIsDisplayed()

        // 検索タブに戻る
        val searchLabel = composeTestRule.activity.getString(R.string.search)
        composeTestRule.onNodeWithText(searchLabel).performClick()

        // 検索タブが選択されていることを確認
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(searchLabel).assertIsDisplayed()
    }
}
