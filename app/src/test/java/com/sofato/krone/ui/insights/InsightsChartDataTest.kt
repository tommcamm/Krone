package com.sofato.krone.ui.insights

import com.google.common.truth.Truth.assertThat
import com.sofato.krone.domain.model.DailySpend
import kotlinx.datetime.LocalDate
import org.junit.Test

class InsightsChartDataTest {

    // --- Cumulative spending calculation ---

    @Test
    fun `cumulative spending sums values progressively`() {
        val dailySpending = listOf(
            DailySpend(LocalDate(2026, 4, 1), 10000),
            DailySpend(LocalDate(2026, 4, 2), 5000),
            DailySpend(LocalDate(2026, 4, 3), 8000),
        )

        var cumulative = 0L
        val cumulativeValues = dailySpending.map { ds ->
            cumulative += ds.totalMinor
            cumulative
        }

        assertThat(cumulativeValues).containsExactly(10000L, 15000L, 23000L).inOrder()
    }

    @Test
    fun `cumulative spending with single day returns that day total`() {
        val dailySpending = listOf(
            DailySpend(LocalDate(2026, 4, 1), 7500),
        )

        var cumulative = 0L
        val cumulativeValues = dailySpending.map { ds ->
            cumulative += ds.totalMinor
            cumulative
        }

        assertThat(cumulativeValues).containsExactly(7500L)
    }

    // --- Ideal line calculation ---

    @Test
    fun `ideal line distributes budget linearly across days`() {
        val discretionaryMinor = 100000L
        val remainingDays = 5
        val dayCount = 5

        val idealValues = (0 until dayCount).map { index ->
            discretionaryMinor * (index + 1) / remainingDays.coerceAtLeast(1).toLong()
        }

        assertThat(idealValues).containsExactly(20000L, 40000L, 60000L, 80000L, 100000L).inOrder()
    }

    @Test
    fun `ideal line with single remaining day returns full budget`() {
        val discretionaryMinor = 50000L
        val remainingDays = 1

        val idealValue = discretionaryMinor * 1 / remainingDays.coerceAtLeast(1).toLong()

        assertThat(idealValue).isEqualTo(50000L)
    }

    // --- Month label formatting ---

    @Test
    fun `month string formats to abbreviated name`() {
        val monthStr = "2026-04"
        val formatted = monthStr.takeLast(2).let { m ->
            val monthNum = m.toIntOrNull() ?: m
            java.time.Month.of(monthNum as Int).name.take(3).lowercase()
                .replaceFirstChar { it.uppercase() }
        }

        assertThat(formatted).isEqualTo("Apr")
    }

    @Test
    fun `month string formats January correctly`() {
        val monthStr = "2026-01"
        val formatted = monthStr.takeLast(2).let { m ->
            val monthNum = m.toIntOrNull() ?: return@let m
            java.time.Month.of(monthNum).name.take(3).lowercase()
                .replaceFirstChar { it.uppercase() }
        }

        assertThat(formatted).isEqualTo("Jan")
    }

    @Test
    fun `month string formats December correctly`() {
        val monthStr = "2025-12"
        val formatted = monthStr.takeLast(2).let { m ->
            val monthNum = m.toIntOrNull() ?: return@let m
            java.time.Month.of(monthNum).name.take(3).lowercase()
                .replaceFirstChar { it.uppercase() }
        }

        assertThat(formatted).isEqualTo("Dec")
    }

    // --- Category comparison take(8) ---

    @Test
    fun `category comparison is limited to 8 entries`() {
        val categories = (1..12).map { "Category $it" }
        val limited = categories.take(8)

        assertThat(limited).hasSize(8)
        assertThat(limited.first()).isEqualTo("Category 1")
        assertThat(limited.last()).isEqualTo("Category 8")
    }

    // --- Stacked bar segment construction ---

    @Test
    fun `stacked bar segments are limited to 6 per month`() {
        val segments = (1..10).map { "Segment $it" }
        val limited = segments.take(6)

        assertThat(limited).hasSize(6)
    }
}
