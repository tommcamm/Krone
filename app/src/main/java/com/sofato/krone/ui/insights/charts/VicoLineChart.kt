package com.sofato.krone.ui.insights.charts

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill

@Composable
fun VicoLineChart(
    data: List<LineChartData>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    idealLine: List<LineChartData>? = null,
    idealLineColor: Color = MaterialTheme.colorScheme.outline,
    chartHeight: Int = 180,
) {
    if (data.size < 2) return

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(data, idealLine) {
        modelProducer.runTransaction {
            lineSeries {
                series(data.map { it.value })
            }
            if (idealLine != null && idealLine.size >= 2) {
                lineSeries {
                    series(idealLine.map { it.value })
                }
            }
        }
    }

    val labels = remember(data) { data.map { it.label } }
    val valueFormatter = remember(labels) {
        CartesianValueFormatter { _, x, _ ->
            labels.getOrElse(x.toInt()) { "" }
        }
    }

    val mainLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(lineColor)),
        interpolator = LineCartesianLayer.Interpolator.cubic(),
    )
    val idealLineSpec = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(idealLineColor)),
        stroke = LineCartesianLayer.LineStroke.Dashed(),
        interpolator = LineCartesianLayer.Interpolator.cubic(),
    )

    val lineProvider = if (idealLine != null && idealLine.size >= 2) {
        LineCartesianLayer.LineProvider.series(mainLine, idealLineSpec)
    } else {
        LineCartesianLayer.LineProvider.series(mainLine)
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(lineProvider = lineProvider),
            bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = valueFormatter),
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight.dp),
    )
}
