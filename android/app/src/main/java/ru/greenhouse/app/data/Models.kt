package ru.greenhouse.app.data

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val token: String)

data class SensorValue(
    val type: String,
    val name: String,
    val unit: String,
    val raw: Double?,
    val value: Double?,
    val inNorm: Boolean,
    val normMin: Double,
    val normMax: Double,
)

data class Current(
    val time: String?,
    val receiving: Boolean,
    val source: String,
    val sensors: List<SensorValue>,
    val pumpOn: Boolean,
    val lampOn: Boolean,
    val waterOk: Boolean,
    val accepted: Int,
    val rejected: Int,
)

data class Point(val t: String, val raw: Double?, val filtered: Double?)

data class SensorStats(
    val type: String,
    val name: String,
    val unit: String,
    val min: Double?,
    val avg: Double?,
    val max: Double?,
    val count: Long,
    val inNormPct: Double?,
)

data class Stats(
    val sensors: List<SensorStats>,
    val accepted: Int,
    val rejected: Int,
    val events: Int,
    val samples: Int,
    val gaps: Long,
    val waterings: Long,
)

data class LogEntry(val time: String?, val kind: String, val text: String)
