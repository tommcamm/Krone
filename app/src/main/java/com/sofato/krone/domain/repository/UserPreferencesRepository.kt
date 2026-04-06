package com.sofato.krone.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val homeCurrencyCode: Flow<String>
    val isDynamicColorEnabled: Flow<Boolean>
    val darkModeOverride: Flow<String>
    val hasCompletedOnboarding: Flow<Boolean>
    val incomeDay: Flow<Int>
    suspend fun setHomeCurrencyCode(code: String)
    suspend fun setDynamicColorEnabled(enabled: Boolean)
    suspend fun setDarkModeOverride(mode: String)
    suspend fun setHasCompletedOnboarding(completed: Boolean)
    suspend fun setIncomeDay(day: Int)
    suspend fun clearAll()
    suspend fun getBackupData(): Map<String, String>
    suspend fun restoreFromBackupData(data: Map<String, String>)
}
