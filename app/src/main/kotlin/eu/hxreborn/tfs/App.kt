package eu.hxreborn.tfs

import android.app.Application
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import java.util.concurrent.CopyOnWriteArrayList

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        XposedServiceHelper.registerListener(
            object : XposedServiceHelper.OnServiceListener {
                override fun onServiceBind(service: XposedService) {
                    mService = service
                    listeners.forEach { it.onServiceBind(service) }
                }

                override fun onServiceDied(service: XposedService) {
                    mService = null
                    listeners.forEach { it.onServiceDied(service) }
                }
            },
        )
    }

    companion object {
        @Volatile
        var mService: XposedService? = null
            private set

        private val listeners = CopyOnWriteArrayList<XposedServiceHelper.OnServiceListener>()

        fun addServiceListener(l: XposedServiceHelper.OnServiceListener) {
            listeners += l
            mService?.let(l::onServiceBind)
        }

        fun removeServiceListener(l: XposedServiceHelper.OnServiceListener) {
            listeners -= l
        }
    }
}
