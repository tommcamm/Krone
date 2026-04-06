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
fun VicoStackedBarChart(
    data: List<StackedBarData>,
    modifier: Modifier = Modifier,
    chartHeight: Int = 220,
) {
    if (data.isEmpty()) return

    // Collect all unique segments across all bars to create series
    val allSegmentLabels = data.flatMap { bar -> bar.segments.map { it.label } }.distinct()
    if (allSegmentLabels.isEmpty()) return

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(data) {
        modelProducer.runTransaction {
            columnSeries {
                // One series per segment type
                for (segLabel in allSegmentLabels) {
                    series(data.map { bar ->
                        bar.segments.find { it.label == segLabel }?.value ?: 0L
                    })
                }
            }
        }
    }

    val labels = remember(data) {
        data.map { bar ->
            bar.label.takeLast(2).let { m ->
                val monthNum = m.toIntOrNull() ?: return@let m
                java.time.Month.of(monthNum).name.take(3).lowercase()
                    .replaceFirstChar { it.uppercase() }
            }
        }
    }
    val valueFormatter = remember(labels) {
        CartesianValueFormatter { _, x, _ ->
            labels.getOrElse(x.toInt()) { "" }
        }
    }

    // Use first segment's colors for columns
    val columns = remember(data) {
        allSegmentLabels.mapNotNull { label ->
            data.firstNotNullOfOrNull { bar ->
                bar.segments.find { it.label == label }
            }
        }
    }

    val columnComponents = columns.map { seg ->
        rememberLineComponent(
            fill = Fill(seg.color),
            thickness = 24.dp,
        )
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(*columnComponents.toTypedArray()),
                mergeMode = { ColumnCartesianLayer.MergeMode.Stacked },
            ),
            bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = valueFormatter),
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight.dp),
    )
}
