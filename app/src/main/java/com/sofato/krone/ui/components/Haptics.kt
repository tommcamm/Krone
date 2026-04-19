package com.sofato.krone.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Semantic wrapper around [HapticFeedback] that also respects a user preference.
 * Callers use the named methods below; the wrapper picks the [HapticFeedbackType]
 * and no-ops when the user has opted out. The system-level touch-feedback setting
 * is already honoured by [HapticFeedback.performHapticFeedback].
 */
class Haptics(
    private val feedback: HapticFeedback,
    private val enabled: Boolean,
) {
    /** Keyboard-like tick — numeric keypad digits, decimal, operators, backspace. */
    fun tap() = perform(HapticFeedbackType.VirtualKey)

    /** Brief click — chip/selector changes (category, currency, date). */
    fun select() = perform(HapticFeedbackType.ContextClick)

    /** Positive confirmation — primary save/complete actions. */
    fun confirm() = perform(HapticFeedbackType.Confirm)

    /** Firmer thud — destructive/negative actions (delete). */
    fun reject() = perform(HapticFeedbackType.Reject)

    /** Light tick when a drag crosses the commit threshold — swipe-to-delete etc. */
    fun thresholdCross() = perform(HapticFeedbackType.GestureThresholdActivate)

    private fun perform(type: HapticFeedbackType) {
        if (enabled) feedback.performHapticFeedback(type)
    }
}

val LocalHaptics = compositionLocalOf<Haptics> {
    error("Haptics not provided. Wrap your content in a CompositionLocalProvider.")
}

@Composable
fun rememberHaptics(enabled: Boolean): Haptics {
    val feedback = LocalHapticFeedback.current
    return remember(feedback, enabled) { Haptics(feedback, enabled) }
}
