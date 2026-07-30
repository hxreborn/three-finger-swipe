package eu.hxreborn.tfs.xposed.hook

import android.content.Context
import android.os.Handler
import android.os.Looper
import eu.hxreborn.tfs.action.screenshot.ScreenshotActionResolver
import eu.hxreborn.tfs.action.screenshot.ScreenshotDispatch
import eu.hxreborn.tfs.prefs.CaptureMode
import eu.hxreborn.tfs.util.Logger
import eu.hxreborn.tfs.util.findMethodUpward
import eu.hxreborn.tfs.util.findMethodUpwardOrWidest
import eu.hxreborn.tfs.util.readField
import eu.hxreborn.tfs.util.signature
import java.lang.reflect.Method

private const val REGISTER = "registerPointerEventListener"
private const val UNREGISTER = "unregisterPointerEventListener"

internal data class PhoneWindowManagerBindings(
    val systemContext: Context,
    val handler: Handler,
    val pointerListenerClass: Class<*>,
    val pointerRegistration: PointerRegistration,
    val pointerUnregistration: PointerRegistration?,
    val screenshotDispatch: ScreenshotDispatch?,
) {
    companion object {
        private const val POINTER_LISTENER_NAME =
            "android.view.WindowManagerPolicyConstants\$PointerEventListener"

        fun resolve(
            phoneWindowManager: Any,
            captureMode: CaptureMode = CaptureMode.SYSTEM_API,
        ): PhoneWindowManagerBindings {
            val classLoader = phoneWindowManager.javaClass.classLoader
            val pointerListenerClass = Class.forName(POINTER_LISTENER_NAME, false, classLoader)

            val systemContext =
                phoneWindowManager.readField("mContext") as? Context
                    ?: error("PhoneWindowManager.mContext is unavailable")

            val displayPolicy =
                phoneWindowManager.readField("mDefaultDisplayPolicy") ?: phoneWindowManager
            val handler =
                displayPolicy.readField("mHandler") as? Handler
                    ?: Handler(Looper.getMainLooper()).also {
                        Logger.warn("policy handler absent reason=no-field fallback=main-looper")
                    }

            val pointerRegistration =
                phoneWindowManager.resolvePointerRegistration(pointerListenerClass)
                    ?: error("registerPointerEventListener is unavailable")
            Logger.info("resolved ${pointerRegistration.method.signature()}")

            val pointerUnregistration =
                pointerRegistration.target.findPointerMethod(UNREGISTER, pointerListenerClass)
            when (pointerUnregistration) {
                null -> Logger.warn("optional member absent $UNREGISTER reason=no-method")
                else -> Logger.info("resolved ${pointerUnregistration.method.signature()}")
            }

            val screenshotDispatch =
                ScreenshotActionResolver.resolve(phoneWindowManager, handler, captureMode)

            return PhoneWindowManagerBindings(
                systemContext = systemContext,
                handler = handler,
                pointerListenerClass = pointerListenerClass,
                pointerRegistration = pointerRegistration,
                pointerUnregistration = pointerUnregistration,
                screenshotDispatch = screenshotDispatch,
            )
        }

        private fun Any.resolvePointerRegistration(
            pointerListenerClass: Class<*>,
        ): PointerRegistration? {
            // DisplayContent first with WindowManagerFuncs as fallback
            val displayContent = readField("mDefaultDisplayPolicy")?.readField("mDisplayContent")
            if (displayContent == null) {
                Logger.debug { "pointer registration fallback reason=no-display-content" }
            }

            displayContent?.findPointerMethod(REGISTER, pointerListenerClass)?.let { return it }

            val windowManagerFuncs = readField("mWindowManagerFuncs") ?: return null
            return windowManagerFuncs.findPointerMethod(REGISTER, pointerListenerClass)
        }

        private fun Any.findPointerMethod(
            name: String,
            pointerListenerClass: Class<*>,
        ): PointerRegistration? {
            val method =
                javaClass.findMethodUpward(
                    name,
                    pointerListenerClass,
                    Int::class.javaPrimitiveType!!,
                ) ?: javaClass.findMethodUpwardOrWidest(name, pointerListenerClass) ?: return null
            return PointerRegistration(target = this, method = method)
        }
    }
}

internal data class PointerRegistration(
    val target: Any,
    val method: Method,
) {
    // every int arg is the default display id
    fun invoke(listener: Any): Any? =
        method.invoke(
            target,
            *method.parameterTypes.map { if (it.isPrimitive) 0 else listener }.toTypedArray(),
        )
}
