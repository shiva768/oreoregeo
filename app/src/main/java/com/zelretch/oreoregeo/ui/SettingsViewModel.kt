package com.zelretch.oreoregeo.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.zelretch.oreoregeo.data.remote.DriveBackupManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed interface BackupState {
    object Idle : BackupState
    object Loading : BackupState
    object Success : BackupState
    data class Error(val message: String) : BackupState
}

class SettingsViewModel(private val context: Context) : ViewModel() {
    private val manager = DriveBackupManager(context)
    private val _state = MutableStateFlow<BackupState>(BackupState.Idle)
    val state: StateFlow<BackupState> = _state

    fun backup(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _state.value = BackupState.Loading
            val dbFile = context.getDatabasePath("oreoregeo.db")
            val result = manager.backupDatabase(account, dbFile)
            _state.value = result.fold(
                onSuccess = { BackupState.Success },
                onFailure = { BackupState.Error(it.message ?: "バックアップ失敗") }
            )
        }
    }

    fun signInClient() = manager.getSignInClient()
}
