package com.zelretch.oreoregeo.data.remote

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import java.io.File

class DriveBackupManager(private val context: Context) {
    fun getSignInClient(): GoogleSignInClient {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        return GoogleSignIn.getClient(context, options)
    }

    fun driveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_APPDATA)
        )
        credential.selectedAccount = account.account
        return Drive.Builder(
            com.google.api.client.http.javanet.NetHttpTransport(),
            com.google.api.client.json.gson.GsonFactory(),
            credential
        ).setApplicationName("Oreoregeo")
            .build()
    }

    suspend fun backupDatabase(account: GoogleSignInAccount, dbPath: File): Result<Unit> {
        return runCatching {
            val drive = driveService(account)
            val wal = File(dbPath.absolutePath + "-wal")
            listOf(dbPath, wal).forEach { file ->
                val metadata = com.google.api.services.drive.model.File().apply {
                    name = file.name
                }
                val content = FileContent("application/octet-stream", file)
                val existing = drive.files().list()
                    .setSpaces("appDataFolder")
                    .setQ("name='${'$'}{file.name}'")
                    .execute()
                    .files
                    .firstOrNull()
                if (existing != null) {
                    drive.files().update(existing.id, metadata, content).execute()
                } else {
                    metadata.parents = listOf("appDataFolder")
                    drive.files().create(metadata, content).execute()
                }
            }
        }
    }
}
