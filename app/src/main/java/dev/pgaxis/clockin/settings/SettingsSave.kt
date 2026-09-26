package dev.pgaxis.clockin.settings

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.pgaxis.axs.AxsBoundObject
import dev.pgaxis.axs.AxsFile
import dev.pgaxis.clockin.models.MonthTime
import java.io.File
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

class SettingsSave private constructor(context: Context): ISettings {

    companion object {
        @Volatile
        private var instance: SettingsSave? = null

        fun getInstance(context: Context): SettingsSave =
            instance ?: synchronized(this) {
                instance ?: SettingsSave(context.applicationContext).also { instance = it }
            }
    }

    private val axsPath = context.filesDir.resolve("settings.axs").path
    private var isInitializing = true

    // --- AXS setup ---
    private val axsFile = AxsFile(axsPath)
    private lateinit var boundSettings: AxsBoundObject<SettingsData>

    // --- Setting fun ---
    private fun <V : Any> setting(
        initial: V,
        prop: KMutableProperty1<SettingsData, V>
    ): ReadWriteProperty<Any?, V> = object : ReadWriteProperty<Any?, V> {
        private var state by mutableStateOf(initial)

        override fun getValue(thisRef: Any?, property: KProperty<*>): V = state

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: V) {
            state = value
            if (::boundSettings.isInitialized && !isInitializing) boundSettings.setValue(prop, value)
        }
    }

    override var times by setting(emptyList(), SettingsData::times)
    override var isClockedIn by setting(false, SettingsData::isClockedIn)

    // -- Data class
    @Keep
    data class SettingsData(
        var times: List<MonthTime> = emptyList(),
        var isClockedIn: Boolean = false
    )

    fun flush() {
        if (::boundSettings.isInitialized) boundSettings.flush()
    }

    init {
        axsFile.setLogging(on = false)
        axsFile.open()

        try {
            boundSettings = axsFile.bind(SettingsData())

            val s = boundSettings.get()

            times = s.times
            isClockedIn = s.isClockedIn

            isInitializing = false
        } catch (e: Exception) {
            Log.d("SettingsSaveError", e.toString())
            Log.d("SettingsSaveError", e.stackTraceToString())
            axsFile.close()
            File(axsPath).delete()
            axsFile.open()
            boundSettings = axsFile.bind(SettingsData())
        } finally {
            isInitializing = false
        }
    }
}