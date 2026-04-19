package com.sofato.krone.ui.components

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class HapticsTest {

    private val feedback = mockk<HapticFeedback>(relaxed = true)

    @Test
    fun `tap performs VirtualKey when enabled`() {
        Haptics(feedback, enabled = true).tap()
        verify(exactly = 1) { feedback.performHapticFeedback(HapticFeedbackType.VirtualKey) }
    }

    @Test
    fun `select performs ContextClick when enabled`() {
        Haptics(feedback, enabled = true).select()
        verify(exactly = 1) { feedback.performHapticFeedback(HapticFeedbackType.ContextClick) }
    }

    @Test
    fun `confirm performs Confirm when enabled`() {
        Haptics(feedback, enabled = true).confirm()
        verify(exactly = 1) { feedback.performHapticFeedback(HapticFeedbackType.Confirm) }
    }

    @Test
    fun `reject performs Reject when enabled`() {
        Haptics(feedback, enabled = true).reject()
        verify(exactly = 1) { feedback.performHapticFeedback(HapticFeedbackType.Reject) }
    }

    @Test
    fun `thresholdCross performs GestureThresholdActivate when enabled`() {
        Haptics(feedback, enabled = true).thresholdCross()
        verify(exactly = 1) {
            feedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
        }
    }

    @Test
    fun `no haptic fires when disabled`() {
        val haptics = Haptics(feedback, enabled = false)
        haptics.tap()
        haptics.select()
        haptics.confirm()
        haptics.reject()
        haptics.thresholdCross()
        verify(exactly = 0) { feedback.performHapticFeedback(any()) }
    }
}
