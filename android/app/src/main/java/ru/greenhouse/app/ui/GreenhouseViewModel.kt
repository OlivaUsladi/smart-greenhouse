package ru.greenhouse.app.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.HttpException
import ru.greenhouse.app.data.Current
import ru.greenhouse.app.data.GreenhouseApi
import ru.greenhouse.app.data.LogEntry
import ru.greenhouse.app.data.LoginRequest
import ru.greenhouse.app.data.Point
import ru.greenhouse.app.data.Stats

data class UiState(
    val serverUrl: String = "http://192.168.0.102:8080/",
    val loggedIn: Boolean = false,
    val loading: Boolean = false,
    val current: Current? = null,
    val history: List<Point> = emptyList(),
    val historySensor: String = "soil",
    val historyMinutes: Int = 0,
    val stats: Stats? = null,
    val events: List<LogEntry> = emptyList(),
    val message: String? = null,
)

class GreenhouseViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("greenhouse", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(UiState(serverUrl = prefs.getString("url", null) ?: UiState().serverUrl))
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var token: String? = null
    private var api: GreenhouseApi? = null
    private var refreshJob: Job? = null

    fun login(url: String, username: String, password: String) = launchSafe {
        val client = GreenhouseApi.create(url.trim()) { token }
        token = client.login(LoginRequest(username.trim(), password)).token
        api = client
        prefs.edit().putString("url", url.trim()).apply()
        _state.update { it.copy(serverUrl = url.trim(), loggedIn = true) }
        startRefresh()
    }

    fun logout() {
        refreshJob?.cancel()
        val client = api
        viewModelScope.launch { runCatching { client?.logout() } }
        token = null
        api = null
        _state.update { UiState(serverUrl = it.serverUrl) }
    }

    // Пока идёт приём данных, текущие значения, график и журнал обновляются каждые 3 секунды
    private fun startRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (isActive) {
                runCatching {
                    val a = api ?: return@launch
                    val s = state.value
                    val current = a.current()
                    val history = a.samples(s.historySensor, s.historyMinutes)
                    val events = a.events()
                    _state.update { it.copy(current = current, history = history, events = events) }
                }
                delay(3_000)
            }
        }
    }

    fun loadHistory(sensor: String = state.value.historySensor, minutes: Int = state.value.historyMinutes) = launchSafe {
        _state.update { it.copy(historySensor = sensor, historyMinutes = minutes) }
        val points = requireApi().samples(sensor, minutes)
        _state.update { it.copy(history = points) }
    }

    fun loadStats() = launchSafe {
        val stats = requireApi().stats()
        _state.update { it.copy(stats = stats) }
    }

    fun messageShown() = _state.update { it.copy(message = null) }

    private fun requireApi(): GreenhouseApi = api ?: error("Сначала войди в систему")

    private fun launchSafe(block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            try {
                block()
            } catch (e: HttpException) {
                val text = if (e.code() == 401) "Неверный логин или пароль" else "Ошибка сервера (${e.code()})"
                _state.update { it.copy(message = text) }
            } catch (e: Exception) {
                _state.update { it.copy(message = "Нет связи с сервером: ${e.message ?: e.javaClass.simpleName}") }
            } finally {
                _state.update { it.copy(loading = false) }
            }
        }
    }
}

fun shortTime(iso: String?): String = iso?.substringAfter('T')?.take(8) ?: "—"
