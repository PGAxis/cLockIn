package dev.pgaxis.clockin.models

import androidx.annotation.Keep
import java.time.Duration
import java.time.LocalDateTime
import java.time.YearMonth

@Keep
data class ClockEntry(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null
) {
    val duration: Duration
        get() = Duration.between(startTime, endTime ?: LocalDateTime.now())

    val isActive: Boolean
        get() = endTime == null
}

@Keep
data class MonthTime(
    val month: YearMonth = YearMonth.now(),
    val entries: List<ClockEntry> = emptyList()
) {
    val totalDuration: Duration
        get() = entries.fold(Duration.ZERO) { acc, e -> acc + e.duration }

    val displayName: String
        get() = month.month.name.lowercase()
            .replaceFirstChar { it.uppercase() } + " " + month.year

    val activeClock: ClockEntry
        get() = entries.last()
}