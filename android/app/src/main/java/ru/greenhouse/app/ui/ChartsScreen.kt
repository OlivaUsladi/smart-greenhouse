package ru.greenhouse.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import ru.greenhouse.app.data.Point

private val sensors = listOf(
    "soil" to "Почва", "light" to "Свет", "air_temp" to "Температура", "air_hum" to "Влажность",
)
private val periods = listOf(10 to "10 минут", 30 to "30 минут", 0 to "Всё время")
private val RawColor = Color(0xFFB0BEC5)

@Composable
fun ChartsScreen(state: UiState, vm: GreenhouseViewModel, modifier: Modifier = Modifier) {
    LaunchedEffect(Unit) { vm.loadHistory() }
    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            sensors.forEach { (type, title) ->
                FilterChip(state.historySensor == type, { vm.loadHistory(sensor = type) }, { Text(title) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            periods.forEach { (minutes, title) ->
                FilterChip(state.historyMinutes == minutes, { vm.loadHistory(minutes = minutes) }, { Text(title) })
            }
        }
        val values = state.history.flatMap { listOfNotNull(it.raw, it.filtered) }
        if (values.isEmpty()) {
            Text("Нет данных за выбранный период")
        } else {
            Text("мин %.1f · макс %.1f · отсчётов %d".format(values.min(), values.max(), state.history.size))
            LineChart(state.history, Modifier.fillMaxWidth().height(280.dp))
            Text("${shortTime(state.history.first().t)} … ${shortTime(state.history.last().t)}", style = MaterialTheme.typography.bodySmall)
            Text("— исходные значения", color = RawColor)
            Text("— сглаженные (EMA)", color = MaterialTheme.colorScheme.primary)
            Text("разрыв линии – пропуск данных", style = MaterialTheme.typography.bodySmall)
        }
    }
}

// Отсчёт без значения (пропуск) прерывает линию
@Composable
fun LineChart(points: List<Point>, modifier: Modifier = Modifier) {
    val filteredColor = MaterialTheme.colorScheme.primary
    val axisColor = MaterialTheme.colorScheme.outline
    Canvas(modifier) {
        val all = points.flatMap { listOfNotNull(it.raw, it.filtered) }
        val min = all.min()
        val max = all.max().let { if (it == min) min + 1 else it }
        fun x(i: Int) = if (points.size == 1) size.width / 2 else i * size.width / (points.size - 1)
        fun y(v: Double) = (size.height - (v - min) / (max - min) * size.height).toFloat()
        fun line(value: (Point) -> Double?, color: Color, width: Float) {
            val path = Path()
            var drawing = false
            points.forEachIndexed { i, p ->
                val v = value(p)
                if (v == null) {
                    drawing = false
                } else if (!drawing) {
                    path.moveTo(x(i), y(v))
                    drawing = true
                } else {
                    path.lineTo(x(i), y(v))
                }
            }
            drawPath(path, color, style = Stroke(width))
        }
        drawLine(axisColor, Offset(0f, size.height), Offset(size.width, size.height))
        drawLine(axisColor, Offset(0f, 0f), Offset(0f, size.height))
        line({ it.raw }, RawColor, 3f)
        line({ it.filtered }, filteredColor, 5f)
    }
}
