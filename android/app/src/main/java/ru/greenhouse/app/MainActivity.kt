package ru.greenhouse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import ru.greenhouse.app.ui.GreenhouseApp
import ru.greenhouse.app.ui.GreenhouseViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GreenhouseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF2E7D32), secondary = Color(0xFF558B2F))) {
                GreenhouseApp(viewModel)
            }
        }
    }
}
