package dev.pgaxis.clockin.screens

import android.app.Application
import androidx.compose.runtime.currentRecomposeScope
import androidx.lifecycle.AndroidViewModel
import dev.pgaxis.clockin.models.ClockEntry
import dev.pgaxis.clockin.models.MonthTime
import dev.pgaxis.clockin.settings.SettingsSave
import java.time.LocalDateTime
import java.time.YearMonth

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>()
    val settings = SettingsSave.getInstance(context)

    fun clockGeneral() {
        if (settings.isClockedIn) {
            clockOut()
        } else {
            clockIn()
        }
    }

    fun clockIn() {
        settings.isClockedIn = true

        val current = settings.times.toMutableList()
        val thisMonth = YearMonth.now()
        val newEntry = ClockEntry(startTime = LocalDateTime.now())

        val existingIndex = current.indexOfFirst { it.month == thisMonth }

        if (existingIndex != -1) {
            val existingMonth = current[existingIndex]
            current[existingIndex] = existingMonth.copy(
                entries = existingMonth.entries + newEntry
            )
        } else {
            current.add(MonthTime(month = thisMonth, entries = listOf(newEntry)))
        }

        settings.times = current
    }

    fun clockOut() {
        settings.isClockedIn = false

        val current = settings.times.toMutableList()
        val thisMonth = YearMonth.now()
        val now = LocalDateTime.now()

        val monthIndex = current.indexOfFirst { it.month == thisMonth }
        if (monthIndex == -1) return // shouldn't happen if clockIn() ran first, but guards against bad state

        val existingMonth = current[monthIndex]
        val entryIndex = existingMonth.entries.indexOfLast { it.isActive }
        if (entryIndex == -1) return // no open entry to close

        val updatedEntries = existingMonth.entries.toMutableList()
        updatedEntries[entryIndex] = updatedEntries[entryIndex].copy(endTime = now)

        current[monthIndex] = existingMonth.copy(entries = updatedEntries)
        settings.times = current
    }
}