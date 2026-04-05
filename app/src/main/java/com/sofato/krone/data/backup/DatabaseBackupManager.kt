package com.sofato.krone.data.backup

import android.content.Context
import android.net.Uri
import com.sofato.krone.data.db.KroneDatabase
import com.sofato.krone.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseBackupManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val database: KroneDatabase,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    private val dbFile: File
        get() = context.getDatabasePath("krone.db")

    internal companion object {
        const val DB_ENTRY = "database.db"
        const val PREFS_ENTRY = "preferences.json"
        private val SQLITE_MAGIC = byteArrayOf(0x53, 0x51, 0x4C, 0x69) // "SQLi"
    }

    suspend fun exportTo(outputUri: Uri) {
        database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()

        val prefsData = userPreferencesRepository.getBackupData()
        val prefsJson = Json.encodeToString(
            JsonObject.serializer(),
            JsonObject(prefsData.mapValues { JsonPrimitive(it.value) }),
        )

        context.contentResolver.openOutputStream(outputUri)?.use { output ->
            ZipOutputStream(output).use { zip ->
                zip.putNextEntry(ZipEntry(DB_ENTRY))
                dbFile.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()

                zip.putNextEntry(ZipEntry(PREFS_ENTRY))
                zip.write(prefsJson.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
        } ?: throw IllegalStateException("Could not open output stream")
    }

    suspend fun importFrom(inputUri: Uri) {
        val tempDbFile = File(dbFile.parent, "krone_import_temp.db")
        var prefsJson: String? = null

        try {
            val inputBytes = context.contentResolver.openInputStream(inputUri)?.use {
                it.readBytes()
            } ?: throw IllegalStateException("Could not open input stream")

            val isLegacyFormat = inputBytes.size >= 4 &&
                inputBytes[0] == SQLITE_MAGIC[0] &&
                inputBytes[1] == SQLITE_MAGIC[1] &&
                inputBytes[2] == SQLITE_MAGIC[2] &&
                inputBytes[3] == SQLITE_MAGIC[3]

            if (isLegacyFormat) {
                tempDbFile.writeBytes(inputBytes)
            } else {
                ZipInputStream(inputBytes.inputStream()).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        when (entry.name) {
                            DB_ENTRY -> tempDbFile.outputStream().use { zip.copyTo(it) }
                            PREFS_ENTRY -> prefsJson = zip.readBytes().toString(Charsets.UTF_8)
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
                if (!tempDbFile.exists()) {
                    throw IllegalStateException("Backup file does not contain a database")
                }
            }

            database.close()
            deleteDbFiles()

            tempDbFile.renameTo(dbFile)

            database.openHelper.writableDatabase

            if (prefsJson != null) {
                val jsonObj = Json.parseToJsonElement(prefsJson!!).jsonObject
                val prefsMap = jsonObj.mapValues { it.value.jsonPrimitive.content }
                userPreferencesRepository.restoreFromBackupData(prefsMap)
            } else {
                userPreferencesRepository.setHasCompletedOnboarding(true)
            }
        } finally {
            tempDbFile.delete()
        }
    }

    fun deleteAll() {
        database.close()
        deleteDbFiles()
        database.openHelper.writableDatabase
    }

    private fun deleteDbFiles() {
        dbFile.delete()
        File(dbFile.path + "-wal").delete()
        File(dbFile.path + "-shm").delete()
    }
}
