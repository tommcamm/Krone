package com.sofato.krone.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Teal/blue-green palette generated from seed #006874

// Light scheme
private val md_theme_light_primary = Color(0xFF006874)
private val md_theme_light_onPrimary = Color(0xFFFFFFFF)
private val md_theme_light_primaryContainer = Color(0xFF97F0FF)
private val md_theme_light_onPrimaryContainer = Color(0xFF001F24)
private val md_theme_light_secondary = Color(0xFF4A6267)
private val md_theme_light_onSecondary = Color(0xFFFFFFFF)
private val md_theme_light_secondaryContainer = Color(0xFFCDE7EC)
private val md_theme_light_onSecondaryContainer = Color(0xFF051F23)
private val md_theme_light_tertiary = Color(0xFF525E7D)
private val md_theme_light_onTertiary = Color(0xFFFFFFFF)
private val md_theme_light_tertiaryContainer = Color(0xFFDAE2FF)
private val md_theme_light_onTertiaryContainer = Color(0xFF0E1B36)
private val md_theme_light_error = Color(0xFFBA1A1A)
private val md_theme_light_onError = Color(0xFFFFFFFF)
private val md_theme_light_errorContainer = Color(0xFFFFDAD6)
private val md_theme_light_onErrorContainer = Color(0xFF410002)
private val md_theme_light_background = Color(0xFFFAFDFD)
private val md_theme_light_onBackground = Color(0xFF191C1D)
private val md_theme_light_surface = Color(0xFFFAFDFD)
private val md_theme_light_onSurface = Color(0xFF191C1D)
private val md_theme_light_surfaceVariant = Color(0xFFDBE4E6)
private val md_theme_light_onSurfaceVariant = Color(0xFF3F484A)
private val md_theme_light_outline = Color(0xFF6F797B)
private val md_theme_light_outlineVariant = Color(0xFFBFC8CA)
private val md_theme_light_scrim = Color(0xFF000000)
private val md_theme_light_inverseSurface = Color(0xFF2E3132)
private val md_theme_light_inverseOnSurface = Color(0xFFEFF1F1)
private val md_theme_light_inversePrimary = Color(0xFF4FD8EB)
private val md_theme_light_surfaceDim = Color(0xFFDADCDD)
private val md_theme_light_surfaceBright = Color(0xFFFAFDFD)
private val md_theme_light_surfaceContainerLowest = Color(0xFFFFFFFF)
private val md_theme_light_surfaceContainerLow = Color(0xFFF4F6F6)
private val md_theme_light_surfaceContainer = Color(0xFFEEF0F1)
private val md_theme_light_surfaceContainerHigh = Color(0xFFE8EBEB)
private val md_theme_light_surfaceContainerHighest = Color(0xFFE3E5E6)

// Dark scheme
private val md_theme_dark_primary = Color(0xFF4FD8EB)
private val md_theme_dark_onPrimary = Color(0xFF00363D)
private val md_theme_dark_primaryContainer = Color(0xFF004F58)
private val md_theme_dark_onPrimaryContainer = Color(0xFF97F0FF)
private val md_theme_dark_secondary = Color(0xFFB1CBD0)
private val md_theme_dark_onSecondary = Color(0xFF1C3438)
private val md_theme_dark_secondaryContainer = Color(0xFF334B4F)
private val md_theme_dark_onSecondaryContainer = Color(0xFFCDE7EC)
private val md_theme_dark_tertiary = Color(0xFFBAC6EA)
private val md_theme_dark_onTertiary = Color(0xFF24304D)
private val md_theme_dark_tertiaryContainer = Color(0xFF3B4664)
private val md_theme_dark_onTertiaryContainer = Color(0xFFDAE2FF)
private val md_theme_dark_error = Color(0xFFFFB4AB)
private val md_theme_dark_onError = Color(0xFF690005)
private val md_theme_dark_errorContainer = Color(0xFF93000A)
private val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
private val md_theme_dark_background = Color(0xFF191C1D)
private val md_theme_dark_onBackground = Color(0xFFE1E3E3)
private val md_theme_dark_surface = Color(0xFF111415)
private val md_theme_dark_onSurface = Color(0xFFC5C7C8)
private val md_theme_dark_surfaceVariant = Color(0xFF3F484A)
private val md_theme_dark_onSurfaceVariant = Color(0xFFBFC8CA)
private val md_theme_dark_outline = Color(0xFF899294)
private val md_theme_dark_outlineVariant = Color(0xFF3F484A)
private val md_theme_dark_scrim = Color(0xFF000000)
private val md_theme_dark_inverseSurface = Color(0xFFE1E3E3)
private val md_theme_dark_inverseOnSurface = Color(0xFF2E3132)
private val md_theme_dark_inversePrimary = Color(0xFF006874)
private val md_theme_dark_surfaceDim = Color(0xFF111415)
private val md_theme_dark_surfaceBright = Color(0xFF373A3B)
private val md_theme_dark_surfaceContainerLowest = Color(0xFF0C0F0F)
private val md_theme_dark_surfaceContainerLow = Color(0xFF191C1D)
private val md_theme_dark_surfaceContainer = Color(0xFF1D2021)
private val md_theme_dark_surfaceContainerHigh = Color(0xFF282B2B)
private val md_theme_dark_surfaceContainerHighest = Color(0xFF333536)

val KroneLightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    errorContainer = md_theme_light_errorContainer,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
    inverseSurface = md_theme_light_inverseSurface,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceDim = md_theme_light_surfaceDim,
    surfaceBright = md_theme_light_surfaceBright,
    surfaceContainerLowest = md_theme_light_surfaceContainerLowest,
    surfaceContainerLow = md_theme_light_surfaceContainerLow,
    surfaceContainer = md_theme_light_surfaceContainer,
    surfaceContainerHigh = md_theme_light_surfaceContainerHigh,
    surfaceContainerHighest = md_theme_light_surfaceContainerHighest,
)

val KroneDarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
    errorContainer = md_theme_dark_errorContainer,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
    inverseSurface = md_theme_dark_inverseSurface,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceDim = md_theme_dark_surfaceDim,
    surfaceBright = md_theme_dark_surfaceBright,
    surfaceContainerLowest = md_theme_dark_surfaceContainerLowest,
    surfaceContainerLow = md_theme_dark_surfaceContainerLow,
    surfaceContainer = md_theme_dark_surfaceContainer,
    surfaceContainerHigh = md_theme_dark_surfaceContainerHigh,
    surfaceContainerHighest = md_theme_dark_surfaceContainerHighest,
)

// Semantic colors
val KroneSavingsGreen = Color(0xFF2E7D32)
val KroneSavingsGreenDark = Color(0xFF66BB6A)
val KroneForeignAmountColor = Color(0xFF78909C)
val KroneForeignAmountColorDark = Color(0xFFB0BEC5)

// Budget-bar palette. Uses three distinct hue families (orange/green/blue) so
// the three segments stay distinguishable under dark mode and the common
// red-green color-blindness types, instead of reusing theme roles that
// collapse into the same hue (primary + tertiary both skewed blue in dark).
@Immutable
data class BudgetBarColors(
    val fixed: Color,
    val savings: Color,
    val discretionary: Color,
)

val LightBudgetBarColors = BudgetBarColors(
    fixed = Color(0xFFD55E00),        // Okabe-Ito vermillion — committed/locked
    savings = Color(0xFF2E7D32),      // Green 800 — money saved
    discretionary = Color(0xFF1565C0), // Blue 800 — available to spend
)

val DarkBudgetBarColors = BudgetBarColors(
    fixed = Color(0xFFFFB74D),        // Orange 300 — softer on dark surface
    savings = Color(0xFF81C784),      // Green 300
    discretionary = Color(0xFF64B5F6), // Blue 300
)

val LocalBudgetBarColors = staticCompositionLocalOf { LightBudgetBarColors }
