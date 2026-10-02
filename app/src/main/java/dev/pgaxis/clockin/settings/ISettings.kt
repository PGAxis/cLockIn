package dev.pgaxis.clockin.settings

import dev.pgaxis.clockin.Theme
import dev.pgaxis.clockin.models.MonthTime

interface ISettings {
    var times: List<MonthTime>
    var isClockedIn: Boolean
    var theme: Theme
}