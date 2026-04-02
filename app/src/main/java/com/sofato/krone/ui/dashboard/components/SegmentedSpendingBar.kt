package com.sofato.krone.ui.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sofato.krone.domain.model.CategorySpend

/**
 * A horizontal bar that shows spending segments colored by category.
 * Each tracked category's spent amount is a segment in its color.
 * Untracked spending uses [uncategorizedColor].
 * The remaining (unspent) portion is the track.
 */
@Composable
fun SegmentedSpendingBar(
    totalBudget: Long,
    totalSpent: Long,
    trackedCategories: List<CategorySpend>,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    uncategorizedColor: Color = MaterialTheme.colorScheme.outline,
    trackColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
) {
    if (totalBudget <= 0) return

    // Compute segments as fractions of the total budget
    data class Segment(val fraction: Float, val color: Color)

    val segments = mutableListOf<Segment>()
    var trackedSpent = 0L

    for (cs in trackedCategories) {
        if (cs.spentMinor <= 0) continue
        val catColor = try {
            Color(android.graphics.Color.parseColor(cs.category.colorHex))
        } catch (_: Exception) {
            uncategorizedColor
        }
        val fraction = cs.spentMinor.toFloat() / totalBudget
        segments.add(Segment(fraction, catColor))
        trackedSpent += cs.spentMinor
    }

    // Untracked spending = total spent minus what's in tracked categories
    val untrackedSpent = (totalSpent - trackedSpent).coerceAtLeast(0)
    if (untrackedSpent > 0) {
        segments.add(Segment(untrackedSpent.toFloat() / totalBudget, uncategorizedColor))
    }

    // Remaining (unspent) track
    val spentFraction = segments.sumOf { it.fraction.toDouble() }.toFloat().coerceAtMost(1f)
    val remainingFraction = (1f - spentFraction).coerceAtLeast(0f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(MaterialTheme.shapes.small),
    ) {
        for (segment in segments) {
            val clamped = segment.fraction.coerceIn(0.001f, 1f)
            Box(
                modifier = Modifier
                    .weight(clamped)
                    .height(height)
                    .background(segment.color),
            )
        }
        if (remainingFraction > 0f) {
            Box(
                modifier = Modifier
                    .weight(remainingFraction)
                    .height(height)
                    .background(trackColor),
            )
        }
    }
}
