package com.sofato.krone.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sofato.krone.domain.model.Category

@Composable
fun CompactCategoryChip(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = try {
        Color(android.graphics.Color.parseColor(category.colorHex))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primaryContainer
    }

    val shape = MaterialTheme.shapes.small

    Box(
        modifier = Modifier
            .clip(shape)
            .then(
                if (isSelected) Modifier.border(1.5.dp, bgColor, shape)
                else Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape),
            )
            .background(if (isSelected) bgColor.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CategoryIcon(
                iconName = category.iconName,
                colorHex = category.colorHex,
                size = 24.dp,
                iconSize = 14.dp,
            )
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) bgColor else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
