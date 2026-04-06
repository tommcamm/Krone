package com.sofato.krone.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.userPreferencesDataStore by preferencesDataStore(name = "user_preferences")

object PreferenceKeys {
    val HOME_CURRENCY_CODE = stringPreferencesKey("home_currency_code")
    val DYNAMIC_COLOR_ENABLED = booleanPreferencesKey("dynamic_color_enabled")
    val DARK_MODE_OVERRIDE = stringPreferencesKey("dark_mode_override")
    val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
    val INCOME_DAY = intPreferencesKey("income_day")
}
