package eu.hxreborn.tfs

import android.app.Application
import android.content.Context
import android.util.Log
import eu.hxreborn.tfs.prefs.Prefs
import eu.hxreborn.tfs.prefs.PrefsRepository
import eu.hxreborn.tfs.util.Logger
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import java.util.concurrent.CopyOnWriteArrayList

class App :
    Application(),
    XposedServiceHelper.OnServiceListener {
    @Volatile
    private var mService: XposedService? = null

    private val listeners = CopyOnWriteArrayList<XposedServiceHelper.OnServiceListener>()

    lateinit var prefs: PrefsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        prefs =
            PrefsRepository(getSharedPreferences(Prefs.GROUP, MODE_PRIVATE)) {
                runCatching { mService?.getRemotePreferences(Prefs.GROUP) }.getOrNull()
            }
        XposedServiceHelper.registerListener(this)
    }

    override fun onServiceBind(service: XposedService) {
        Log.i(
            Logger.TAG,
            "service bound name=${service.frameworkName} v=${service.frameworkVersion}",
        )
        mService = service
        prefs.syncToRemote()
        listeners.forEach { it.onServiceBind(service) }
    }

    override fun onServiceDied(service: XposedService) {
        Log.w(Logger.TAG, "service died")
        mService = null
        listeners.forEach { it.onServiceDied(service) }
    }

    fun addServiceListener(l: XposedServiceHelper.OnServiceListener) {
        listeners += l
        mService?.let(l::onServiceBind)
    }

    fun removeServiceListener(l: XposedServiceHelper.OnServiceListener) {
        listeners -= l
    }

    companion object {
        fun from(context: Context): App = context.applicationContext as App
    }
}
