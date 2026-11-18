package ru.greenhouse.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.greenhouse.app.data.SensorValue

@Composable
fun DashboardScreen(state: UiState, modifier: Modifier = Modifier) {
    val current = state.current
    Column(
        modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (current == null) {
            Text("Загрузка данных…")
            return@Column
        }
        Text(
            if (current.receiving) "Идёт приём данных" else "Приём данных завершён",
            color = if (current.receiving) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            fontWeight = FontWeight.Bold,
        )
        Text("Отсчёт за ${shortTime(current.time)} · источник: ${current.source}", style = MaterialTheme.typography.bodySmall)
        current.sensors.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { SensorCard(it, Modifier.weight(1f)) }
                if (row.size == 1) Column(Modifier.weight(1f)) {}
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Состояние теплицы", style = MaterialTheme.typography.titleMedium)
                Text(if (current.pumpOn) "Насос работает" else "Насос выключен")
                Text(if (current.lampOn) "Фитолента светит" else "Фитолента не светит")
                Text(
                    if (current.waterOk) "Вода в баке есть" else "В баке нет воды",
                    color = if (current.waterOk) Color.Unspecified else MaterialTheme.colorScheme.error,
                )
            }
        }
        Text(
            "Принято пакетов: ${current.accepted} · отброшено: ${current.rejected}",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun SensorCard(sensor: SensorValue, modifier: Modifier = Modifier) {
    val color = when {
        sensor.value == null -> Color(0xFFECEFF1)
        sensor.inNorm -> Color(0xFFDCEDC8)
        else -> Color(0xFFFFF3C4)
    }
    Card(modifier, colors = CardDefaults.cardColors(containerColor = color)) {
        Column(Modifier.padding(12.dp)) {
            Text(sensor.name, style = MaterialTheme.typography.labelLarge)
            Text(
                sensor.value?.let { "%.1f %s".format(it, sensor.unit) } ?: "—",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                "норма %.0f…%.0f %s".format(sensor.normMin, sensor.normMax, sensor.unit),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
