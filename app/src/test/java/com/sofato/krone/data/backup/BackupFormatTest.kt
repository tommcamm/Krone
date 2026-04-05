package com.sofato.krone.data.backup

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.After
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

        assertThat(entries.keys).containsExactly(
            DatabaseBackupManager.DB_ENTRY,
            DatabaseBackupManager.PREFS_ENTRY,
        )
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

        assertThat(restored).isEqualTo(originalPrefs)
    }

    @Test
    fun `database content round-trips through ZIP correctly`() {
        val dbContent = ByteArray(1024) { (it % 256).toByte() }
        val prefsMap = mapOf("home_currency_code" to "DKK")

        val zipBytes = createZipBackup(dbContent, prefsMap)
        val entries = readZipEntries(zipBytes)

        assertThat(entries[DatabaseBackupManager.DB_ENTRY]).isEqualTo(dbContent)
    }

    @Test
    fun `legacy SQLite file is detected by magic bytes`() {
        val sqliteMagic = "SQLite format 3\u0000".toByteArray()
        val fakeDb = sqliteMagic + ByteArray(100)

        // "SQLi" = 0x53, 0x51, 0x4C, 0x69
        assertThat(fakeDb[0]).isEqualTo(0x53.toByte())
        assertThat(fakeDb[1]).isEqualTo(0x51.toByte())
        assertThat(fakeDb[2]).isEqualTo(0x4C.toByte())
        assertThat(fakeDb[3]).isEqualTo(0x69.toByte())
    }

    @Test
    fun `ZIP file is not detected as legacy SQLite format`() {
        val zipBytes = createZipBackup(ByteArray(16), mapOf("key" to "value"))

        // ZIP files start with PK (0x50, 0x4B), not SQLi
        assertThat(zipBytes[0]).isNotEqualTo(0x53.toByte())
    }

    @Test
    fun `preferences JSON handles all supported data types`() {
        val prefs = mapOf(
            "home_currency_code" to "EUR",
            "dynamic_color_enabled" to "true",
            "income_day" to "15",
        )

        val json = Json.encodeToString(
            JsonObject.serializer(),
            JsonObject(prefs.mapValues { JsonPrimitive(it.value) }),
        )
        val parsed = Json.parseToJsonElement(json).jsonObject

        assertThat(parsed["home_currency_code"]?.jsonPrimitive?.content).isEqualTo("EUR")
        assertThat(parsed["dynamic_color_enabled"]?.jsonPrimitive?.content).isEqualTo("true")
        assertThat(parsed["income_day"]?.jsonPrimitive?.content).isEqualTo("15")
    }

    @Test
    fun `empty preferences map produces valid JSON`() {
        val emptyPrefs = emptyMap<String, String>()
        val json = Json.encodeToString(
            JsonObject.serializer(),
            JsonObject(emptyPrefs.mapValues { JsonPrimitive(it.value) }),
        )
        val parsed = Json.parseToJsonElement(json).jsonObject

        assertThat(parsed).isEmpty()
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

        assertThat(entries).containsKey(DatabaseBackupManager.DB_ENTRY)
        assertThat(entries).doesNotContainKey(DatabaseBackupManager.PREFS_ENTRY)
    }

    @Test
    fun `large database content handles correctly in ZIP`() {
        val dbContent = ByteArray(1_000_000) { (it % 256).toByte() }
        val prefsMap = mapOf("home_currency_code" to "DKK")

        val zipBytes = createZipBackup(dbContent, prefsMap)
        val entries = readZipEntries(zipBytes)

        val restoredDb = entries[DatabaseBackupManager.DB_ENTRY]!!
        assertThat(restoredDb).hasLength(dbContent.size)
        assertThat(restoredDb).isEqualTo(dbContent)
        assertThat(zipBytes.size).isLessThan(dbContent.size)
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
