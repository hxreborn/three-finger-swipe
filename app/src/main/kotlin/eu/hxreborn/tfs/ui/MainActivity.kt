package eu.hxreborn.tfs.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import eu.hxreborn.tfs.App
import eu.hxreborn.tfs.prefs.Prefs
import eu.hxreborn.tfs.prefs.PrefsRepository
import eu.hxreborn.tfs.ui.navigation.AppNavHost
import eu.hxreborn.tfs.ui.theme.AppTheme
import eu.hxreborn.tfs.ui.viewmodel.SettingsViewModel
import eu.hxreborn.tfs.ui.viewmodel.SettingsViewModelFactory
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper

class MainActivity : ComponentActivity() {
    private val repository by lazy {
        PrefsRepository(
            localPrefs = getSharedPreferences(Prefs.GROUP, MODE_PRIVATE),
        )
    }

    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(repository)
    }

    private val serviceListener =
        object : XposedServiceHelper.OnServiceListener {
            override fun onServiceBind(service: XposedService) {
                repository.attachRemotePrefs(service.getRemotePreferences(Prefs.GROUP))
                viewModel.setXposedActive(true)
            }

            override fun onServiceDied(service: XposedService) {
                repository.attachRemotePrefs(null)
                viewModel.setXposedActive(false)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        App.addServiceListener(serviceListener)

        setContent {
            AppTheme {
                AppNavHost(viewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        App.removeServiceListener(serviceListener)
    }
}
