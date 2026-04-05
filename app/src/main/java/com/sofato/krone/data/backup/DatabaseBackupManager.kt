package com.sofato.krone.data.backup

import android.content.Context
import android.net.Uri
import com.sofato.krone.data.db.KroneDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseBackupManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val database: KroneDatabase,
) {
    private val dbFile: File
        get() = context.getDatabasePath("krone.db")

    fun exportTo(outputUri: Uri) {
        database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()

        context.contentResolver.openOutputStream(outputUri)?.use { output ->
            dbFile.inputStream().use { input ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Could not open output stream")
    }

    fun importFrom(inputUri: Uri) {
        database.close()
        deleteDbFiles()

        context.contentResolver.openInputStream(inputUri)?.use { input ->
            dbFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Could not open input stream")

        // Force Room to reopen the database on next access
        database.openHelper.writableDatabase
    }

    fun deleteAll() {
        database.close()
        deleteDbFiles()
        // Force Room to reopen (and re-seed via callback)
        database.openHelper.writableDatabase
    }

    private fun deleteDbFiles() {
        dbFile.delete()
        File(dbFile.path + "-wal").delete()
        File(dbFile.path + "-shm").delete()
    }
}
