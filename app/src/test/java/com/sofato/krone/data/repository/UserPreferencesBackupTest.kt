package com.sofato.krone.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesOf
import com.sofato.krone.data.datastore.PreferenceKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserPreferencesBackupTest {

    private lateinit var fakeDataStore: FakeDataStore
    private lateinit var repository: UserPreferencesRepositoryImpl

    @Before
    fun setUp() {
        fakeDataStore = FakeDataStore()
        repository = UserPreferencesRepositoryImpl(fakeDataStore)
    }

    @Test
    fun `getBackupData returns all set preferences`() = runTest {
        fakeDataStore.updateData {
            val mutable = it.toMutablePreferences()
            mutable[PreferenceKeys.HOME_CURRENCY_CODE] = "SEK"
            mutable[PreferenceKeys.DYNAMIC_COLOR_ENABLED] = false
            mutable[PreferenceKeys.DARK_MODE_OVERRIDE] = "dark"
            mutable[PreferenceKeys.HAS_COMPLETED_ONBOARDING] = true
            mutable[PreferenceKeys.INCOME_DAY] = 25
            mutable[PreferenceKeys.SHOW_MONTHLY_CARD] = false
            mutable[PreferenceKeys.SHOW_DAILY_CARD] = true
            mutable
        }

        val backup = repository.getBackupData()

        assertEquals("SEK", backup["home_currency_code"])
        assertEquals("false", backup["dynamic_color_enabled"])
        assertEquals("dark", backup["dark_mode_override"])
        assertEquals("true", backup["has_completed_onboarding"])
        assertEquals("25", backup["income_day"])
        assertEquals("false", backup["show_monthly_card"])
        assertEquals("true", backup["show_daily_card"])
    }

    @Test
    fun `getBackupData excludes unset preferences`() = runTest {
        fakeDataStore.updateData {
            val mutable = it.toMutablePreferences()
            mutable[PreferenceKeys.HOME_CURRENCY_CODE] = "DKK"
            mutable
        }

        val backup = repository.getBackupData()

        assertEquals(1, backup.size)
        assertEquals("DKK", backup["home_currency_code"])
    }

    @Test
    fun `restoreFromBackupData sets all values and clears previous state`() = runTest {
        // Set some initial state
        fakeDataStore.updateData {
            val mutable = it.toMutablePreferences()
            mutable[PreferenceKeys.HOME_CURRENCY_CODE] = "DKK"
            mutable[PreferenceKeys.SHOW_DAILY_CARD] = false
            mutable
        }

        val backupData = mapOf(
            "home_currency_code" to "SEK",
            "has_completed_onboarding" to "true",
            "income_day" to "15",
        )

        repository.restoreFromBackupData(backupData)

        val prefs = fakeDataStore.data.first()
        assertEquals("SEK", prefs[PreferenceKeys.HOME_CURRENCY_CODE])
        assertEquals(true, prefs[PreferenceKeys.HAS_COMPLETED_ONBOARDING])
        assertEquals(15, prefs[PreferenceKeys.INCOME_DAY])
        // Previously set SHOW_DAILY_CARD should be cleared
        assertTrue(prefs[PreferenceKeys.SHOW_DAILY_CARD] == null)
    }

    @Test
    fun `backup and restore round-trip preserves all preferences`() = runTest {
        fakeDataStore.updateData {
            val mutable = it.toMutablePreferences()
            mutable[PreferenceKeys.HOME_CURRENCY_CODE] = "EUR"
            mutable[PreferenceKeys.DYNAMIC_COLOR_ENABLED] = false
            mutable[PreferenceKeys.DARK_MODE_OVERRIDE] = "light"
            mutable[PreferenceKeys.HAS_COMPLETED_ONBOARDING] = true
            mutable[PreferenceKeys.INCOME_DAY] = 28
            mutable[PreferenceKeys.SHOW_MONTHLY_CARD] = true
            mutable[PreferenceKeys.SHOW_DAILY_CARD] = false
            mutable
        }

        val backup = repository.getBackupData()

        // Clear all state
        fakeDataStore.updateData {
            val mutable = it.toMutablePreferences()
            mutable.clear()
            mutable
        }

        // Restore
        repository.restoreFromBackupData(backup)

        // Verify all values match original
        val prefs = fakeDataStore.data.first()
        assertEquals("EUR", prefs[PreferenceKeys.HOME_CURRENCY_CODE])
        assertEquals(false, prefs[PreferenceKeys.DYNAMIC_COLOR_ENABLED])
        assertEquals("light", prefs[PreferenceKeys.DARK_MODE_OVERRIDE])
        assertEquals(true, prefs[PreferenceKeys.HAS_COMPLETED_ONBOARDING])
        assertEquals(28, prefs[PreferenceKeys.INCOME_DAY])
        assertEquals(true, prefs[PreferenceKeys.SHOW_MONTHLY_CARD])
        assertEquals(false, prefs[PreferenceKeys.SHOW_DAILY_CARD])
    }

    @Test
    fun `restoreFromBackupData with empty map clears all preferences`() = runTest {
        fakeDataStore.updateData {
            val mutable = it.toMutablePreferences()
            mutable[PreferenceKeys.HOME_CURRENCY_CODE] = "SEK"
            mutable[PreferenceKeys.HAS_COMPLETED_ONBOARDING] = true
            mutable
        }

        repository.restoreFromBackupData(emptyMap())

        val prefs = fakeDataStore.data.first()
        assertTrue("All prefs should be cleared", prefs.asMap().isEmpty())
    }

    @Test
    fun `restoreFromBackupData handles invalid boolean gracefully`() = runTest {
        val backupData = mapOf(
            "dynamic_color_enabled" to "not_a_boolean",
            "has_completed_onboarding" to "TRUE", // wrong case
        )

        repository.restoreFromBackupData(backupData)

        val prefs = fakeDataStore.data.first()
        // toBooleanStrictOrNull returns null for invalid input, fallback values apply
        assertEquals(true, prefs[PreferenceKeys.DYNAMIC_COLOR_ENABLED])
        assertEquals(false, prefs[PreferenceKeys.HAS_COMPLETED_ONBOARDING])
    }

    @Test
    fun `restoreFromBackupData handles invalid int gracefully`() = runTest {
        val backupData = mapOf("income_day" to "not_a_number")

        repository.restoreFromBackupData(backupData)

        val prefs = fakeDataStore.data.first()
        assertEquals(1, prefs[PreferenceKeys.INCOME_DAY])
    }
}

private class FakeDataStore : DataStore<Preferences> {
    private val state = MutableStateFlow<Preferences>(preferencesOf())

    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val newPrefs = transform(state.value)
        state.value = newPrefs
        return newPrefs
    }
}
