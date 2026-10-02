package dev.pgaxis.clockin.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.pgaxis.clockin.R
import dev.pgaxis.clockin.models.ClockEntry
import dev.pgaxis.clockin.models.MonthTime
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val dayFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")

private fun formatTime(dt: LocalDateTime?): String = dt?.format(timeFormatter) ?: "—"

private fun formatDuration(duration: java.time.Duration): String {
    val totalMinutes = duration.toMinutes()
    return "${totalMinutes / 60}h %02dm".format(totalMinutes % 60)
}

@Composable
fun CalendarScreen(
    onBack: () -> Unit,
    vm: CalendarViewModel = viewModel()
) {
    val months by remember(vm.settings.times) {
        mutableStateOf(vm.settings.times.sortedByDescending { it.month })
    }

    var editing by remember { mutableStateOf<Pair<YearMonth, ClockEntry>?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .windowInsetsPadding(WindowInsets.systemBars),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.size(45.dp).padding(horizontal = 5.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.back),
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.width(4.dp))

            Text(
                text = "Calendar",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(months, key = { it.month }) { monthTime ->
                MonthCard(
                    monthTime = monthTime,
                    onEditEntry = { entry -> editing = monthTime.month to entry }
                )
            }
        }
    }

    editing?.let { (month, entry) ->
        EditEntryDialog(
            entry = entry,
            onDismiss = { editing = null },
            onConfirm = { newStart, newEnd ->
                vm.updateEntry(month, entry, newStart, newEnd)
                editing = null
            },
            onDelete = {
                vm.deleteEntry(month, entry)
                editing = null
            }
        )
    }
}

@Composable
private fun MonthCard(monthTime: MonthTime, onEditEntry: (ClockEntry) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = monthTime.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formatDuration(monthTime.totalDuration),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            val groupedByDay = monthTime.entries
                .sortedByDescending { it.startTime }
                .groupBy { it.startTime.toLocalDate() }

            groupedByDay.forEach { (date, dayEntries) ->
                Text(
                    text = date.format(dayFormatter),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
                dayEntries.forEach { entry ->
                    EntryRow(entry, onClick = { onEditEntry(entry) })
                }
            }
        }
    }
}

@Composable
private fun EntryRow(entry: ClockEntry, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !entry.isActive, onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Text(formatTime(entry.startTime), modifier = Modifier.weight(1f))
        Text(formatTime(entry.endTime), modifier = Modifier.weight(1f))
        Text(
            text = formatDuration(entry.duration),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditEntryDialog(
    entry: ClockEntry,
    onDismiss: () -> Unit,
    onConfirm: (newStart: LocalDateTime, newEnd: LocalDateTime) -> Unit,
    onDelete: () -> Unit
) {
    var date by remember { mutableStateOf(entry.startTime.toLocalDate()) }
    var startTime by remember { mutableStateOf(entry.startTime.toLocalTime()) }
    var endTime by remember { mutableStateOf(entry.endTime?.toLocalTime() ?: entry.startTime.toLocalTime()) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val newStart = LocalDateTime.of(date, startTime)
    val newEnd = LocalDateTime.of(date, endTime)
    val isValid = newEnd.isAfter(newStart)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Edit Entry")
                IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.delete),
                        contentDescription = "Delete entry",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        text = {
            Column {
                EditRow("Date", date.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy"))) {
                    showDatePicker = true
                }
                EditRow("Start", startTime.format(timeFormatter)) { showStartPicker = true }
                EditRow("End", endTime.format(timeFormatter)) { showEndPicker = true }

                if (!isValid) {
                    Text(
                        text = "End time must be after start time.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(newStart, newEnd) }, enabled = isValid) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showDatePicker) {
        val initialMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = state) }
    }

    if (showStartPicker) {
        TimeSelectionDialog(
            initialTime = startTime,
            title = "Start Time",
            onDismiss = { showStartPicker = false },
            onConfirm = { startTime = it; showStartPicker = false }
        )
    }

    if (showEndPicker) {
        TimeSelectionDialog(
            initialTime = endTime,
            title = "End Time",
            onDismiss = { showEndPicker = false },
            onConfirm = { endTime = it; showEndPicker = false }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Entry?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = onDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun EditRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeSelectionDialog(
    initialTime: LocalTime,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}