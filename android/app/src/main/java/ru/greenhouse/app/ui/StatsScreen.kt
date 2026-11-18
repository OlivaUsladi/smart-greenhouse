package ru.greenhouse.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StatsScreen(state: UiState, vm: GreenhouseViewModel, modifier: Modifier = Modifier) {
    LaunchedEffect(Unit) { vm.loadStats() }
    Column(
        modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(onClick = { vm.loadStats() }, modifier = Modifier.fillMaxWidth()) { Text("Обновить") }
        val stats = state.stats ?: return@Column
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Приём данных", style = MaterialTheme.typography.titleMedium)
                Text("Принято пакетов: ${stats.accepted}, отброшено: ${stats.rejected}")
                Text("Отсчётов: ${stats.samples}, пропусков: ${stats.gaps}")
                Text("Событий: ${stats.events}, поливов: ${stats.waterings}")
            }
        }
        stats.sensors.forEach { s ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(s.name, style = MaterialTheme.typography.titleMedium)
                    if (s.count == 0L) {
                        Text("Нет измерений")
                    } else {
                        Text("мин %.1f · средн %.1f · макс %.1f %s".format(s.min, s.avg, s.max, s.unit))
                        s.inNormPct?.let { Text("В норме %.0f %% времени".format(it)) }
                        Text("Отсчётов: ${s.count}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
