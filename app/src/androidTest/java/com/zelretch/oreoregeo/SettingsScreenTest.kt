package com.zelretch.oreoregeo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.zelretch.oreoregeo.ui.SettingsScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * SettingsScreenのUIテスト
 * 設定画面の表示をテストします
 */
@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settingsScreen_displaysBackupOption() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                SettingsScreen(
                    onBackupClick = {},
                    onOsmLoginClick = {},
                    onOsmDisconnectClick = suspend {}
                )
            }
        }

        // バックアップオプションが表示されることを確認
        composeTestRule.onNodeWithText(
            context.getString(R.string.backup_to_drive)
        ).assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysOsmLoginOption() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            OreoregeoTheme {
                SettingsScreen(
                    onBackupClick = {},
                    onOsmLoginClick = {},
                    onOsmDisconnectClick = suspend {}
                )
            }
        }

        // OSMログインオプションが表示されることを確認
        composeTestRule.onNodeWithText(
            context.getString(R.string.connect_osm_account)
        ).assertIsDisplayed()
    }
}
