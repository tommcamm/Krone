package com.sofato.krone.ui.dashboard

import androidx.compose.ui.graphics.Color
import com.google.common.truth.Truth.assertThat
import com.sofato.krone.domain.model.BudgetOverview
import com.sofato.krone.domain.model.BudgetPeriod
import com.sofato.krone.domain.model.Category
import com.sofato.krone.domain.model.CategorySpend
import kotlinx.datetime.LocalDate
import org.junit.Test

class BuildArcSegmentsTest {

    private val period = BudgetPeriod(
        startDate = LocalDate(2026, 4, 1),
        endDate = LocalDate(2026, 4, 30),
    )

    private fun category(name: String, colorHex: String = "#FF5733") = Category(
        id = 0,
        name = name,
        iconName = "restaurant",
        colorHex = colorHex,
        isCustom = false,
        sortOrder = 0,
    )

    private fun overview(
        categoryBreakdown: List<CategorySpend> = emptyList(),
    ) = BudgetOverview(
        period = period,
        totalIncomeMinor = 200000,
        totalFixedMinor = 80000,
        totalSavingsMinor = 20000,
        discretionaryMinor = 100000,
        spentMinor = 50000,
        categoryBreakdown = categoryBreakdown,
        currencyCode = "DKK",
    )

    @Test
    fun `returns empty list when overview is null`() {
        val segments = buildArcSegments(overview = null, fixedMinor = 50000)
        assertThat(segments).isEmpty()
    }

    @Test
    fun `includes fixed segment when fixedMinor is positive`() {
        val segments = buildArcSegments(overview = overview(), fixedMinor = 80000)
        assertThat(segments).hasSize(1)
        assertThat(segments[0].label).isEqualTo("Fixed")
        assertThat(segments[0].value).isEqualTo(80000)
    }

    @Test
    fun `skips fixed segment when fixedMinor is zero`() {
        val segments = buildArcSegments(overview = overview(), fixedMinor = 0)
        assertThat(segments).isEmpty()
    }

    @Test
    fun `includes categories with spending and skips zero-spent`() {
        val breakdown = listOf(
            CategorySpend(category("Food"), allocatedMinor = 30000, spentMinor = 15000),
            CategorySpend(category("Transport"), allocatedMinor = 20000, spentMinor = 0),
            CategorySpend(category("Entertainment"), allocatedMinor = 10000, spentMinor = 5000),
        )
        val segments = buildArcSegments(
            overview = overview(categoryBreakdown = breakdown),
            fixedMinor = 0,
        )

        assertThat(segments).hasSize(2)
        assertThat(segments.map { it.label }).containsExactly("Food", "Entertainment")
        assertThat(segments.map { it.value }).containsExactly(15000L, 5000L)
    }

    @Test
    fun `fixed segment comes before category segments`() {
        val breakdown = listOf(
            CategorySpend(category("Food"), allocatedMinor = 30000, spentMinor = 15000),
        )
        val segments = buildArcSegments(
            overview = overview(categoryBreakdown = breakdown),
            fixedMinor = 80000,
        )

        assertThat(segments).hasSize(2)
        assertThat(segments[0].label).isEqualTo("Fixed")
        assertThat(segments[1].label).isEqualTo("Food")
    }

    @Test
    fun `uses fallback color for invalid hex`() {
        val breakdown = listOf(
            CategorySpend(
                category("Bad Color", colorHex = "not-a-color"),
                allocatedMinor = 10000,
                spentMinor = 5000,
            ),
        )
        val segments = buildArcSegments(
            overview = overview(categoryBreakdown = breakdown),
            fixedMinor = 0,
        )

        assertThat(segments).hasSize(1)
        assertThat(segments[0].color).isEqualTo(Color(0xFF94A3B8))
    }

    @Test
    fun `category segment uses category name and spent value`() {
        val breakdown = listOf(
            CategorySpend(
                category("Food", colorHex = "#FF0000"),
                allocatedMinor = 10000,
                spentMinor = 7500,
            ),
            CategorySpend(
                category("Transport", colorHex = "#00FF00"),
                allocatedMinor = 5000,
                spentMinor = 3000,
            ),
        )
        val segments = buildArcSegments(
            overview = overview(categoryBreakdown = breakdown),
            fixedMinor = 0,
        )

        assertThat(segments).hasSize(2)
        assertThat(segments[0].label).isEqualTo("Food")
        assertThat(segments[0].value).isEqualTo(7500L)
        assertThat(segments[1].label).isEqualTo("Transport")
        assertThat(segments[1].value).isEqualTo(3000L)
    }
}
