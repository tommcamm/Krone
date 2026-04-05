package com.sofato.krone.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.sofato.krone.data.datastore.PreferenceKeys
import com.sofato.krone.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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

    override suspend fun getBackupData(): Map<String, String> {
        val prefs = dataStore.data.first()
        val result = mutableMapOf<String, String>()
        prefs[PreferenceKeys.HOME_CURRENCY_CODE]?.let { result["home_currency_code"] = it }
        prefs[PreferenceKeys.DYNAMIC_COLOR_ENABLED]?.let { result["dynamic_color_enabled"] = it.toString() }
        prefs[PreferenceKeys.DARK_MODE_OVERRIDE]?.let { result["dark_mode_override"] = it }
        prefs[PreferenceKeys.HAS_COMPLETED_ONBOARDING]?.let { result["has_completed_onboarding"] = it.toString() }
        prefs[PreferenceKeys.INCOME_DAY]?.let { result["income_day"] = it.toString() }
        prefs[PreferenceKeys.SHOW_MONTHLY_CARD]?.let { result["show_monthly_card"] = it.toString() }
        prefs[PreferenceKeys.SHOW_DAILY_CARD]?.let { result["show_daily_card"] = it.toString() }
        return result
    }

    override suspend fun restoreFromBackupData(data: Map<String, String>) {
        dataStore.edit { prefs ->
            prefs.clear()
            data["home_currency_code"]?.let { prefs[PreferenceKeys.HOME_CURRENCY_CODE] = it }
            data["dynamic_color_enabled"]?.let { prefs[PreferenceKeys.DYNAMIC_COLOR_ENABLED] = it.toBooleanStrictOrNull() ?: true }
            data["dark_mode_override"]?.let { prefs[PreferenceKeys.DARK_MODE_OVERRIDE] = it }
            data["has_completed_onboarding"]?.let { prefs[PreferenceKeys.HAS_COMPLETED_ONBOARDING] = it.toBooleanStrictOrNull() ?: false }
            data["income_day"]?.let { prefs[PreferenceKeys.INCOME_DAY] = it.toIntOrNull() ?: 1 }
            data["show_monthly_card"]?.let { prefs[PreferenceKeys.SHOW_MONTHLY_CARD] = it.toBooleanStrictOrNull() ?: true }
            data["show_daily_card"]?.let { prefs[PreferenceKeys.SHOW_DAILY_CARD] = it.toBooleanStrictOrNull() ?: true }
        }
    }
}
