package com.zelretch.oreoregeo.data

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream

class DriveBackupManager(private val context: Context) {
    
    fun getSignInIntent(): Intent {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
        
        val client = GoogleSignIn.getClient(context, signInOptions)
        return client.signInIntent
    }

    suspend fun backupDatabase(account: GoogleSignInAccount): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                listOf(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = account.account

            val driveService = Drive.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName("Oreoregeo")
                .build()

            val dbPath = context.getDatabasePath("oreoregeo_database")
            val walPath = java.io.File(dbPath.absolutePath + "-wal")

            // Backup main database file
            backupFile(driveService, dbPath, "oreoregeo_database.db")
            
            // Backup WAL file if it exists
            if (walPath.exists()) {
                backupFile(driveService, walPath, "oreoregeo_database.db-wal")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun backupFile(driveService: Drive, localFile: java.io.File, remoteName: String) {
        // Check if file already exists
        val query = "name='$remoteName' and trashed=false"
        val existingFiles = driveService.files().list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute()

        val fileMetadata = File().apply {
            name = remoteName
        }

        val mediaContent = com.google.api.client.http.FileContent(
            "application/octet-stream",
            localFile
        )

        if (existingFiles.files.isNotEmpty()) {
            // Update existing file
            val fileId = existingFiles.files[0].id
            driveService.files().update(fileId, fileMetadata, mediaContent).execute()
        } else {
            // Create new file
            driveService.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute()
        }
    }

    suspend fun restoreDatabase(account: GoogleSignInAccount): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                listOf(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = account.account

            val driveService = Drive.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName("Oreoregeo")
                .build()

            val dbPath = context.getDatabasePath("oreoregeo_database")
            val walPath = java.io.File(dbPath.absolutePath + "-wal")

            // Restore main database file
            restoreFile(driveService, "oreoregeo_database.db", dbPath)
            
            // Restore WAL file if it exists
            try {
                restoreFile(driveService, "oreoregeo_database.db-wal", walPath)
            } catch (e: Exception) {
                // WAL file might not exist, ignore
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun restoreFile(driveService: Drive, remoteName: String, localFile: java.io.File) {
        val query = "name='$remoteName' and trashed=false"
        val files = driveService.files().list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute()

        if (files.files.isEmpty()) {
            throw Exception("Backup file not found: $remoteName")
        }

        val fileId = files.files[0].id
        val outputStream = FileOutputStream(localFile)
        driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
        outputStream.close()
    }
}
