package dev.pgaxis.clockin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dev.pgaxis.clockin.settings.SettingsSave
import dev.pgaxis.clockin.ui.theme.ClockinAetherScheme
import dev.pgaxis.clockin.ui.theme.ClockinBordoScheme
import dev.pgaxis.clockin.ui.theme.ClockinChalkScheme
import dev.pgaxis.clockin.ui.theme.ClockinCyanScheme
import dev.pgaxis.clockin.ui.theme.ClockinEmberScheme
import dev.pgaxis.clockin.ui.theme.ClockinGrayscaleScheme
import dev.pgaxis.clockin.ui.theme.ClockinPhosphorScheme
import dev.pgaxis.clockin.ui.theme.ClockinSoleilScheme
import dev.pgaxis.clockin.ui.theme.ClockinTheme
import dev.pgaxis.clockin.ui.theme.ClockinVoidScheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings = remember { SettingsSave.getInstance(this) }
            val colorScheme = when (settings.theme) {
                Theme.CYAN -> ClockinCyanScheme
                Theme.GRAYSCALE -> ClockinGrayscaleScheme
                Theme.EMBER -> ClockinEmberScheme
                Theme.AETHER -> ClockinAetherScheme
                Theme.PHOSPHOR -> ClockinPhosphorScheme
                Theme.CHALK -> ClockinChalkScheme
                Theme.SUNSHINE -> ClockinSoleilScheme
                Theme.BORDO -> ClockinBordoScheme
                Theme.VOID -> ClockinVoidScheme
            }
            ClockinTheme(colorScheme = colorScheme) {
                AppNavigation()
            }
        }
    }
}