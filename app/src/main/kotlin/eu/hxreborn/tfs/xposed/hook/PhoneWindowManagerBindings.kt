package eu.hxreborn.tfs.xposed.hook

import android.content.Context
import android.os.Handler
import eu.hxreborn.tfs.action.screenshot.ScreenshotActionResolver
import eu.hxreborn.tfs.action.screenshot.ScreenshotDispatch
import eu.hxreborn.tfs.prefs.CaptureMode
import eu.hxreborn.tfs.util.findMethodUpward
import eu.hxreborn.tfs.util.log
import eu.hxreborn.tfs.util.logDebug
import eu.hxreborn.tfs.util.readField
import java.lang.reflect.Method

internal data class PhoneWindowManagerBindings(
    val systemContext: Context,
    val pointerListenerClass: Class<*>,
    val pointerRegistration: PointerRegistration,
    val screenshotDispatch: ScreenshotDispatch?,
) {
    companion object {
        private const val POINTER_LISTENER_NAME =
            "android.view.WindowManagerPolicyConstants\$PointerEventListener"

        fun resolve(
            phoneWindowManager: Any,
            captureMode: CaptureMode = CaptureMode.REFLECTION,
        ): PhoneWindowManagerBindings {
            val classLoader = phoneWindowManager.javaClass.classLoader
            val pointerListenerClass = Class.forName(POINTER_LISTENER_NAME, false, classLoader)

            val systemContext =
                phoneWindowManager.readField("mContext") as? Context
                    ?: error("PhoneWindowManager.mContext is unavailable")

            val pointerRegistration =
                phoneWindowManager.resolvePointerRegistration(pointerListenerClass)
                    ?: error("registerPointerEventListener is unavailable")

            val displayPolicy =
                phoneWindowManager.readField("mDefaultDisplayPolicy") ?: phoneWindowManager
            val policyHandler = displayPolicy.readField("mHandler") as? Handler
            if (policyHandler == null) {
                log(
                    "PhoneWindowManagerBindings: mHandler unavailable, screenshot dispatch disabled",
                )
            }

            return PhoneWindowManagerBindings(
                systemContext = systemContext,
                pointerListenerClass = pointerListenerClass,
                pointerRegistration = pointerRegistration,
                screenshotDispatch =
                    policyHandler?.let {
                        ScreenshotActionResolver.resolve(phoneWindowManager, it, captureMode)
                    },
            )
        }

        private fun Any.resolvePointerRegistration(
            pointerListenerClass: Class<*>,
        ): PointerRegistration? {
            // This moved across releases and OEM forks
            // Check both paths so the hook does not care where it lives
            val displayContent =
                readField("mDefaultDisplayPolicy")?.readField("mDisplayContent").also {
                    if (it == null) {
                        logDebug {
                            "mDisplayContent unavailable, falling back to mWindowManagerFuncs"
                        }
                    }
                }

            displayContent?.findPointerRegistration(pointerListenerClass)?.let { return it }

            val windowManagerFuncs = readField("mWindowManagerFuncs") ?: return null
            return windowManagerFuncs.findPointerRegistration(pointerListenerClass)
        }

        private fun Any.findPointerRegistration(
            pointerListenerClass: Class<*>,
        ): PointerRegistration? {
            javaClass
                .findMethodUpward(
                    "registerPointerEventListener",
                    pointerListenerClass,
                    Int::class.javaPrimitiveType!!,
                )?.let {
                    return PointerRegistration(
                        target = this,
                        method = it,
                        usesDisplayId = true,
                    )
                }

            javaClass.findMethodUpward("registerPointerEventListener", pointerListenerClass)?.let {
                return PointerRegistration(
                    target = this,
                    method = it,
                    usesDisplayId = false,
                )
            }

            return null
        }
    }
}

internal data class PointerRegistration(
    val target: Any,
    val method: Method,
    val usesDisplayId: Boolean,
) {
    fun invoke(listener: Any): Any? =
        when {
            usesDisplayId -> method.invoke(target, listener, 0)
            else -> method.invoke(target, listener)
        }
}
