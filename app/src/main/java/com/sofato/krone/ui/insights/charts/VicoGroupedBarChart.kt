package com.sofato.krone.ui.insights.charts

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent

@Composable
fun VicoGroupedBarChart(
    groups: List<BarChartGroup>,
    modifier: Modifier = Modifier,
    chartHeight: Int = 200,
) {
    if (groups.isEmpty()) return

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(groups) {
        modelProducer.runTransaction {
            columnSeries {
                series(groups.map { it.previousValue })
                series(groups.map { it.currentValue })
            }
        }
    }

    val labels = remember(groups) { groups.map { g ->
        if (g.label.length > 8) g.label.take(7) + "\u2026" else g.label
    } }
    val valueFormatter = remember(labels) {
        CartesianValueFormatter { _, x, _ ->
            labels.getOrElse(x.toInt()) { "" }
        }
    }

    val prevColumn = rememberLineComponent(
        fill = Fill(groups.firstOrNull()?.color?.copy(alpha = 0.35f) ?: return),
        thickness = 16.dp,
    )
    val currColumn = rememberLineComponent(
        fill = Fill(groups.firstOrNull()?.color ?: return),
        thickness = 16.dp,
    )

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(prevColumn, currColumn),
                mergeMode = { ColumnCartesianLayer.MergeMode.Grouped() },
            ),
            bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = valueFormatter),
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight.dp),
    )
}
