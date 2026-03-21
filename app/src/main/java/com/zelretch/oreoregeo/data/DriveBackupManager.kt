package com.zelretch.oreoregeo.data

import android.accounts.Account
import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.FileOutputStream

class DriveBackupManager(private val context: Context) {

    companion object {
        private const val FOLDER_NAME = "Oreoregeo"
        private const val MIME_FOLDER = "application/vnd.google-apps.folder"
    }

    suspend fun backupDatabase(account: Account): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Timber.i("Starting database backup to Google Drive")
            val driveService = buildDriveService(account)
            val folderId = getOrCreateFolder(driveService)

            val dbPath = context.getDatabasePath("oreoregeo_database")
            val walPath = java.io.File(dbPath.absolutePath + "-wal")

            Timber.d("Backing up main database file")
            backupFile(driveService, folderId, dbPath, "oreoregeo_database.db")

            if (walPath.exists()) {
                Timber.d("Backing up WAL file")
                backupFile(driveService, folderId, walPath, "oreoregeo_database.db-wal")
            } else {
                Timber.d("WAL file does not exist, skipping")
            }

            Timber.i("Database backup completed successfully")
            Result.success(Unit)
        } catch (e: UserRecoverableAuthIOException) {
            Timber.w(e, "Drive authorization required")
            Result.failure(e)
        } catch (e: Exception) {
            Timber.e(e, "Error during database backup")
            Result.failure(e)
        }
    }

    suspend fun restoreDatabase(account: Account): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Timber.i("Starting database restore from Google Drive")
            val driveService = buildDriveService(account)
            val folderId = getOrCreateFolder(driveService)

            val dbPath = context.getDatabasePath("oreoregeo_database")
            val walPath = java.io.File(dbPath.absolutePath + "-wal")

            Timber.d("Restoring main database file")
            restoreFile(driveService, folderId, "oreoregeo_database.db", dbPath)

            try {
                Timber.d("Attempting to restore WAL file")
                restoreFile(driveService, folderId, "oreoregeo_database.db-wal", walPath)
            } catch (e: Exception) {
                Timber.w(e, "WAL file not found in backup, continuing without it")
            }

            Timber.i("Database restore completed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error during database restore")
            Result.failure(e)
        }
    }

    private fun buildDriveService(account: Account): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Oreoregeo")
            .build()
    }

    private fun getOrCreateFolder(driveService: Drive): String {
        val query = "name='$FOLDER_NAME' and mimeType='$MIME_FOLDER' and trashed=false"
        val existing = driveService.files().list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id)")
            .execute()

        if (existing.files.isNotEmpty()) {
            Timber.d("Found existing folder: $FOLDER_NAME (ID: ${existing.files[0].id})")
            return existing.files[0].id
        }

        Timber.d("Creating folder: $FOLDER_NAME")
        val folderMetadata = File().apply {
            name = FOLDER_NAME
            mimeType = MIME_FOLDER
        }
        val folder = driveService.files().create(folderMetadata)
            .setFields("id")
            .execute()
        Timber.d("Created folder: $FOLDER_NAME (ID: ${folder.id})")
        return folder.id
    }

    private fun backupFile(driveService: Drive, folderId: String, localFile: java.io.File, remoteName: String) {
        Timber.d("Backing up file: $remoteName (${localFile.length()} bytes)")
        val query = "name='$remoteName' and '$folderId' in parents and trashed=false"
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
            val fileId = existingFiles.files[0].id
            Timber.d("Updating existing file: $remoteName (ID: $fileId)")
            driveService.files().update(fileId, fileMetadata, mediaContent).execute()
        } else {
            fileMetadata.parents = listOf(folderId)
            Timber.d("Creating new file: $remoteName in folder $folderId")
            driveService.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute()
        }
        Timber.d("Successfully backed up file: $remoteName")
    }

    private fun restoreFile(driveService: Drive, folderId: String, remoteName: String, localFile: java.io.File) {
        Timber.d("Restoring file: $remoteName")
        val query = "name='$remoteName' and '$folderId' in parents and trashed=false"
        val files = driveService.files().list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute()

        if (files.files.isEmpty()) {
            Timber.w("Backup file not found: $remoteName")
            throw Exception("Backup file not found: $remoteName")
        }

        val fileId = files.files[0].id
        Timber.d("Found backup file: $remoteName (ID: $fileId), downloading...")
        val outputStream = FileOutputStream(localFile)
        driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
        outputStream.close()
        Timber.d("Successfully restored file: $remoteName (${localFile.length()} bytes)")
    }
}
