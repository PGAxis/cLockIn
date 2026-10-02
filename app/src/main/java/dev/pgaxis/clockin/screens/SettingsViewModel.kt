package dev.pgaxis.clockin.screens

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import dev.pgaxis.clockin.Theme
import dev.pgaxis.clockin.settings.SettingsSave

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>()
    val settings = SettingsSave.getInstance(context)

    val themeOptions = mapOf(
        Theme.CYAN to "Cyan",
        Theme.EMBER to "Ember",
        Theme.AETHER to "Aether",
        Theme.PHOSPHOR to "Phosphor",
        Theme.BORDO to "Bordo",
        Theme.VOID to "Void",
        Theme.CHALK to "Chalk",
        Theme.SUNSHINE to "Sunshine",
        Theme.GRAYSCALE to "Grayscale"
    )
    var selectedTheme by mutableStateOf(settings.theme)

    fun onThemeChanged(key: Theme) {
        selectedTheme = key
        settings.theme = key
    }
}