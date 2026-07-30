package eu.hxreborn.tfs.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import eu.hxreborn.tfs.ui.navigation.AppNavHost
import eu.hxreborn.tfs.ui.theme.AppTheme
import eu.hxreborn.tfs.ui.viewmodel.SettingsViewModel
import eu.hxreborn.tfs.ui.viewmodel.SettingsViewModelImpl

class MainActivity : ComponentActivity() {
    private val viewModel: SettingsViewModel by viewModels { SettingsViewModelImpl.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme {
                AppNavHost(viewModel)
            }
        }
    }
}
