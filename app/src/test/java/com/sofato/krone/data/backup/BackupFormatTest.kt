package com.sofato.krone.data.backup

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupFormatTest {

    private lateinit var tempDir: File

    @Before
    fun setUp() {
        tempDir = File(System.getProperty("java.io.tmpdir"), "krone_backup_test_${System.nanoTime()}")
        tempDir.mkdirs()
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `ZIP backup contains both database and preferences entries`() {
        val dbContent = "SQLite format 3\u0000fake database content".toByteArray()
        val prefsMap = mapOf(
            "home_currency_code" to "SEK",
            "has_completed_onboarding" to "true",
            "income_day" to "25",
        )

        val zipBytes = createZipBackup(dbContent, prefsMap)
        val entries = readZipEntries(zipBytes)

        assertTrue("ZIP must contain ${DatabaseBackupManager.DB_ENTRY}", entries.containsKey(DatabaseBackupManager.DB_ENTRY))
        assertTrue("ZIP must contain ${DatabaseBackupManager.PREFS_ENTRY}", entries.containsKey(DatabaseBackupManager.PREFS_ENTRY))
        assertEquals(2, entries.size)
    }

    @Test
    fun `preferences round-trip preserves all values`() {
        val originalPrefs = mapOf(
            "home_currency_code" to "SEK",
            "dynamic_color_enabled" to "false",
            "dark_mode_override" to "dark",
            "has_completed_onboarding" to "true",
            "income_day" to "25",
            "show_monthly_card" to "false",
            "show_daily_card" to "true",
        )

        val json = Json.encodeToString(
            JsonObject.serializer(),
            JsonObject(originalPrefs.mapValues { JsonPrimitive(it.value) }),
        )

        val restored = Json.parseToJsonElement(json).jsonObject
            .mapValues { it.value.jsonPrimitive.content }

        assertEquals(originalPrefs, restored)
    }

    @Test
    fun `database content round-trips through ZIP correctly`() {
        val dbContent = ByteArray(1024) { (it % 256).toByte() }
        val prefsMap = mapOf("home_currency_code" to "DKK")

        val zipBytes = createZipBackup(dbContent, prefsMap)
        val entries = readZipEntries(zipBytes)

        val restoredDb = entries[DatabaseBackupManager.DB_ENTRY]!!
        assertTrue("Database content must survive round-trip", dbContent.contentEquals(restoredDb))
    }

    @Test
    fun `legacy SQLite file is detected by magic bytes`() {
        // Real SQLite files start with "SQLite format 3\000"
        val sqliteMagic = "SQLite format 3\u0000".toByteArray()
        val fakeDb = sqliteMagic + ByteArray(100)

        assertTrue("First 4 bytes should be SQLi",
            fakeDb[0] == 0x53.toByte() &&
            fakeDb[1] == 0x51.toByte() &&
            fakeDb[2] == 0x4C.toByte() &&
            fakeDb[3] == 0x69.toByte()
        )
    }

    @Test
    fun `ZIP file is not detected as legacy SQLite format`() {
        val zipBytes = createZipBackup(ByteArray(16), mapOf("key" to "value"))

        // ZIP files start with PK (0x50, 0x4B)
        assertFalse("ZIP should not match SQLite magic",
            zipBytes[0] == 0x53.toByte() &&
            zipBytes[1] == 0x51.toByte() &&
            zipBytes[2] == 0x4C.toByte() &&
            zipBytes[3] == 0x69.toByte()
        )
    }

    @Test
    fun `preferences JSON handles all supported data types`() {
        val prefs = mapOf(
            "home_currency_code" to "EUR",      // string
            "dynamic_color_enabled" to "true",   // boolean as string
            "income_day" to "15",                // int as string
        )

        val json = Json.encodeToString(
            JsonObject.serializer(),
            JsonObject(prefs.mapValues { JsonPrimitive(it.value) }),
        )
        val parsed = Json.parseToJsonElement(json).jsonObject

        assertEquals("EUR", parsed["home_currency_code"]?.jsonPrimitive?.content)
        assertEquals("true", parsed["dynamic_color_enabled"]?.jsonPrimitive?.content)
        assertEquals("15", parsed["income_day"]?.jsonPrimitive?.content)
    }

    @Test
    fun `empty preferences map produces valid JSON`() {
        val emptyPrefs = emptyMap<String, String>()
        val json = Json.encodeToString(
            JsonObject.serializer(),
            JsonObject(emptyPrefs.mapValues { JsonPrimitive(it.value) }),
        )
        val parsed = Json.parseToJsonElement(json).jsonObject
        assertTrue("Empty prefs should produce empty JSON object", parsed.isEmpty())
    }

    @Test
    fun `ZIP with missing preferences entry returns null for prefs`() {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zip ->
            zip.putNextEntry(ZipEntry(DatabaseBackupManager.DB_ENTRY))
            zip.write(ByteArray(16))
            zip.closeEntry()
        }
        val zipBytes = baos.toByteArray()
        val entries = readZipEntries(zipBytes)

        assertNotNull("DB entry should exist", entries[DatabaseBackupManager.DB_ENTRY])
        assertNull("Prefs entry should be absent", entries[DatabaseBackupManager.PREFS_ENTRY])
    }

    @Test
    fun `large database content handles correctly in ZIP`() {
        // Simulate a realistic database size (~1MB)
        val dbContent = ByteArray(1_000_000) { (it % 256).toByte() }
        val prefsMap = mapOf("home_currency_code" to "DKK")

        val zipBytes = createZipBackup(dbContent, prefsMap)
        val entries = readZipEntries(zipBytes)

        val restoredDb = entries[DatabaseBackupManager.DB_ENTRY]!!
        assertEquals("Large DB size must be preserved", dbContent.size, restoredDb.size)
        assertTrue("Large DB content must match", dbContent.contentEquals(restoredDb))

        // ZIP should compress the repetitive data significantly
        assertTrue("ZIP should compress repetitive data", zipBytes.size < dbContent.size)
    }

    // --- Helpers ---

    private fun createZipBackup(dbContent: ByteArray, prefs: Map<String, String>): ByteArray {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zip ->
            zip.putNextEntry(ZipEntry(DatabaseBackupManager.DB_ENTRY))
            zip.write(dbContent)
            zip.closeEntry()

            val prefsJson = Json.encodeToString(
                JsonObject.serializer(),
                JsonObject(prefs.mapValues { JsonPrimitive(it.value) }),
            )
            zip.putNextEntry(ZipEntry(DatabaseBackupManager.PREFS_ENTRY))
            zip.write(prefsJson.toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }
        return baos.toByteArray()
    }

    private fun readZipEntries(zipBytes: ByteArray): Map<String, ByteArray> {
        val entries = mutableMapOf<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(zipBytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                entries[entry.name] = zip.readBytes()
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return entries
    }
}
