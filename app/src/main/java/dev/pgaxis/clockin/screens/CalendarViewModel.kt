package dev.pgaxis.clockin.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dev.pgaxis.clockin.models.ClockEntry
import dev.pgaxis.clockin.models.MonthTime
import dev.pgaxis.clockin.settings.SettingsSave
import java.time.LocalDateTime
import java.time.YearMonth

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>()
    val settings = SettingsSave.getInstance(context)

    fun updateEntry(originalMonth: YearMonth, original: ClockEntry, newStart: LocalDateTime, newEnd: LocalDateTime) {
        val current = settings.times.toMutableList()
        val monthIndex = current.indexOfFirst { it.month == originalMonth }
        if (monthIndex == -1) return

        val month = current[monthIndex]
        val entryIndex = month.entries.indexOf(original)
        if (entryIndex == -1) return

        val updated = original.copy(startTime = newStart, endTime = newEnd)
        val targetMonthKey = YearMonth.from(newStart)

        if (targetMonthKey == originalMonth) {
            val newEntries = month.entries.toMutableList()
            newEntries[entryIndex] = updated
            current[monthIndex] = month.copy(entries = newEntries)
        } else {
            // Date edit moved the entry into a different month.
            val trimmed = month.entries.toMutableList().apply { removeAt(entryIndex) }
            current[monthIndex] = month.copy(entries = trimmed)

            val targetIndex = current.indexOfFirst { it.month == targetMonthKey }
            if (targetIndex != -1) {
                val target = current[targetIndex]
                current[targetIndex] = target.copy(entries = target.entries + updated)
            } else {
                current.add(MonthTime(month = targetMonthKey, entries = listOf(updated)))
            }
        }

        settings.times = current
    }

    fun deleteEntry(month: YearMonth, entry: ClockEntry) {
        val current = settings.times.toMutableList()
        val monthIndex = current.indexOfFirst { it.month == month }
        if (monthIndex == -1) return

        val monthTime = current[monthIndex]
        val newEntries = monthTime.entries.toMutableList().apply { remove(entry) }

        if (newEntries.isEmpty()) {
            current.removeAt(monthIndex) // no entries left this month
        } else {
            current[monthIndex] = monthTime.copy(entries = newEntries)
        }

        settings.times = current
    }
}