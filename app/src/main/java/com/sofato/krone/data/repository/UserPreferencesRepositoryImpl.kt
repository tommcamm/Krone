package com.sofato.krone.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.sofato.krone.data.datastore.PreferenceKeys
import com.sofato.krone.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {

    override val homeCurrencyCode: Flow<String> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.HOME_CURRENCY_CODE] ?: "DKK"
    }

    override val isDynamicColorEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.DYNAMIC_COLOR_ENABLED] ?: true
    }

    override val darkModeOverride: Flow<String> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.DARK_MODE_OVERRIDE] ?: "system"
    }

    override val hasCompletedOnboarding: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.HAS_COMPLETED_ONBOARDING] ?: false
    }

    override val incomeDay: Flow<Int> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.INCOME_DAY] ?: 1
    }

    override val showMonthlyCard: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.SHOW_MONTHLY_CARD] ?: true
    }

    override val showDailyCard: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferenceKeys.SHOW_DAILY_CARD] ?: true
    }

    override suspend fun setHomeCurrencyCode(code: String) {
        dataStore.edit { it[PreferenceKeys.HOME_CURRENCY_CODE] = code }
    }

    override suspend fun setDynamicColorEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferenceKeys.DYNAMIC_COLOR_ENABLED] = enabled }
    }

    override suspend fun setDarkModeOverride(mode: String) {
        dataStore.edit { it[PreferenceKeys.DARK_MODE_OVERRIDE] = mode }
    }

    override suspend fun setHasCompletedOnboarding(completed: Boolean) {
        dataStore.edit { it[PreferenceKeys.HAS_COMPLETED_ONBOARDING] = completed }
    }

    override suspend fun setIncomeDay(day: Int) {
        dataStore.edit { it[PreferenceKeys.INCOME_DAY] = day }
    }

    override suspend fun setShowMonthlyCard(show: Boolean) {
        dataStore.edit { it[PreferenceKeys.SHOW_MONTHLY_CARD] = show }
    }

    override suspend fun setShowDailyCard(show: Boolean) {
        dataStore.edit { it[PreferenceKeys.SHOW_DAILY_CARD] = show }
    }

    override suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
