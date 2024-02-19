package fi.metropolia.bibeks.ble

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun RSSIGraph(rssiValues: List<Int>) {
    val minX = 0f
    val maxX = rssiValues.size.toFloat() - 1
    val minY = rssiValues.minOrNull()?.toFloat() ?: 0f
    val maxY = rssiValues.maxOrNull()?.toFloat() ?: 100f

    LineChart(
        points = rssiValues.mapIndexed { index, rssi ->
            Offset(index.toFloat(), rssi.toFloat())
        },
        modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.5f).padding(16.dp),
        lineColor = Color.Blue,
        lineWidth = 2f
    )
}


@Composable
fun LineChart(
    points: List<Offset>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color.Blue,
    lineWidth: Float = 2f
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Draw X and Y axes
        drawLine(
            color = Color.Gray,
            start = Offset(0f, canvasHeight),
            end = Offset(canvasWidth, canvasHeight),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.Gray,
            start = Offset(0f, 0f),
            end = Offset(0f, canvasHeight),
            strokeWidth = 2f
        )

        val minX = points.minByOrNull { it.x }?.x ?: 0f
        val maxX = points.maxByOrNull { it.x }?.x ?: 0f
        val minY = points.minByOrNull { it.y }?.y ?: 0f
        val maxY = points.maxByOrNull { it.y }?.y ?: 0f

        val scaleX = canvasWidth / (maxX - minX)
        val scaleY = canvasHeight / (maxY - minY)

        for (i in 0 until points.size - 1) {
            val startPoint = Offset((points[i].x - minX) * scaleX, (maxY - points[i].y) * scaleY)
            val endPoint = Offset((points[i + 1].x - minX) * scaleX, (maxY - points[i + 1].y) * scaleY)
            drawLine(
                color = lineColor,
                start = startPoint,
                end = endPoint,
                strokeWidth = lineWidth
            )
        }
    }
}

