package com.sofato.krone.ui.insights.charts

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
fun VicoStackedAreaChart(
    data: List<AreaChartData>,
    modifier: Modifier = Modifier,
    spendingColor: Color = MaterialTheme.colorScheme.error,
    savingsColor: Color = MaterialTheme.colorScheme.tertiary,
    chartHeight: Int = 160,
) {
    if (data.size < 2) return

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(data) {
        modelProducer.runTransaction {
            lineSeries {
                series(data.map { it.spendingMinor })
                series(data.map { it.spendingMinor + it.savingsMinor })
            }
        }
    }

    val labels = remember(data) { data.map { it.label } }
    val valueFormatter = remember(labels) {
        CartesianValueFormatter { _, x, _ ->
            labels.getOrElse(x.toInt()) { "" }
        }
    }

    val spendingGradient = Brush.verticalGradient(
        colors = listOf(spendingColor.copy(alpha = 0.3f), Color.Transparent),
    )
    val savingsGradient = Brush.verticalGradient(
        colors = listOf(savingsColor.copy(alpha = 0.3f), Color.Transparent),
    )

    val spendingLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(spendingColor)),
        areaFill = LineCartesianLayer.AreaFill.single(Fill(spendingGradient)),
    )
    val savingsLine = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(savingsColor)),
        areaFill = LineCartesianLayer.AreaFill.single(Fill(savingsGradient)),
    )

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(spendingLine, savingsLine),
            ),
            bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = valueFormatter),
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight.dp),
    )
}
