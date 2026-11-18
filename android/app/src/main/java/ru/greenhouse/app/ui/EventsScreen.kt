package ru.greenhouse.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun EventsScreen(state: UiState, modifier: Modifier = Modifier) {
    if (state.events.isEmpty()) {
        Text("Событий пока нет", modifier.padding(16.dp))
        return
    }
    LazyColumn(modifier, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(state.events) { e ->
            val color = when (e.kind) {
                "rejected" -> Color(0xFFFFCDD2)
                "pump", "lamp" -> Color(0xFFDCEDC8)
                else -> Color(0xFFE3F2FD)
            }
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = color)) {
                Column(Modifier.padding(12.dp)) {
                    Text(e.text)
                    Text(shortTime(e.time), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
