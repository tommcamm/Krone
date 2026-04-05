package com.sofato.krone.data.repository

import com.google.common.truth.Truth.assertThat
import com.sofato.krone.data.datastore.PreferenceKeys
import com.sofato.krone.testutil.FakeDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
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

        assertThat(backup).containsExactly(
            "home_currency_code", "SEK",
            "dynamic_color_enabled", "false",
            "dark_mode_override", "dark",
            "has_completed_onboarding", "true",
            "income_day", "25",
            "show_monthly_card", "false",
            "show_daily_card", "true",
        )
    }

    @Test
    fun `getBackupData excludes unset preferences`() = runTest {
        fakeDataStore.updateData {
            val mutable = it.toMutablePreferences()
            mutable[PreferenceKeys.HOME_CURRENCY_CODE] = "DKK"
            mutable
        }

        val backup = repository.getBackupData()

        assertThat(backup).containsExactly("home_currency_code", "DKK")
    }

    @Test
    fun `restoreFromBackupData sets all values and clears previous state`() = runTest {
        fakeDataStore.updateData {
            val mutable = it.toMutablePreferences()
            mutable[PreferenceKeys.HOME_CURRENCY_CODE] = "DKK"
            mutable[PreferenceKeys.SHOW_DAILY_CARD] = false
            mutable
        }

        repository.restoreFromBackupData(
            mapOf(
                "home_currency_code" to "SEK",
                "has_completed_onboarding" to "true",
                "income_day" to "15",
            ),
        )

        val prefs = fakeDataStore.data.first()
        assertThat(prefs[PreferenceKeys.HOME_CURRENCY_CODE]).isEqualTo("SEK")
        assertThat(prefs[PreferenceKeys.HAS_COMPLETED_ONBOARDING]).isTrue()
        assertThat(prefs[PreferenceKeys.INCOME_DAY]).isEqualTo(15)
        // Previously set SHOW_DAILY_CARD should be cleared
        assertThat(prefs[PreferenceKeys.SHOW_DAILY_CARD]).isNull()
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
        assertThat(prefs[PreferenceKeys.HOME_CURRENCY_CODE]).isEqualTo("EUR")
        assertThat(prefs[PreferenceKeys.DYNAMIC_COLOR_ENABLED]).isFalse()
        assertThat(prefs[PreferenceKeys.DARK_MODE_OVERRIDE]).isEqualTo("light")
        assertThat(prefs[PreferenceKeys.HAS_COMPLETED_ONBOARDING]).isTrue()
        assertThat(prefs[PreferenceKeys.INCOME_DAY]).isEqualTo(28)
        assertThat(prefs[PreferenceKeys.SHOW_MONTHLY_CARD]).isTrue()
        assertThat(prefs[PreferenceKeys.SHOW_DAILY_CARD]).isFalse()
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
        assertThat(prefs.asMap()).isEmpty()
    }

    @Test
    fun `restoreFromBackupData handles invalid boolean gracefully`() = runTest {
        repository.restoreFromBackupData(
            mapOf(
                "dynamic_color_enabled" to "not_a_boolean",
                "has_completed_onboarding" to "TRUE",
            ),
        )

        val prefs = fakeDataStore.data.first()
        // toBooleanStrictOrNull returns null for invalid input, fallback values apply
        assertThat(prefs[PreferenceKeys.DYNAMIC_COLOR_ENABLED]).isTrue()
        assertThat(prefs[PreferenceKeys.HAS_COMPLETED_ONBOARDING]).isFalse()
    }

    @Test
    fun `restoreFromBackupData handles invalid int gracefully`() = runTest {
        repository.restoreFromBackupData(mapOf("income_day" to "not_a_number"))

        val prefs = fakeDataStore.data.first()
        assertThat(prefs[PreferenceKeys.INCOME_DAY]).isEqualTo(1)
    }
}
