package eu.hxreborn.tfs.xposed.hook

import eu.hxreborn.tfs.action.Action
import eu.hxreborn.tfs.action.ActionId
import eu.hxreborn.tfs.action.ActionRegistry
import eu.hxreborn.tfs.action.splitscreen.ToggleSplitScreenAction
import eu.hxreborn.tfs.gesture.GestureConfig
import eu.hxreborn.tfs.gesture.GestureHandler
import eu.hxreborn.tfs.gesture.GestureInputMonitor
import eu.hxreborn.tfs.util.Logger
import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Proxy
import java.util.concurrent.atomic.AtomicBoolean

object PhoneWindowManagerHook {
    private val registered = AtomicBoolean(false)

    @Volatile
    private var phoneWindowManager: Any? = null

    @Volatile
    private var registeredListener: Any? = null

    @Volatile
    private var pointerUnregistration: PointerRegistration? = null

    @Volatile
    private var registeredActions: Collection<Action> = emptyList()

    fun createInterceptor(): XposedInterface.Hooker =
        XposedInterface.Hooker { chain ->
            val result = chain.proceed()
            if (registered.get()) return@Hooker result
            try {
                install(chain.thisObject ?: error("PhoneWindowManager missing"), reloaded = false)
            } catch (e: Exception) {
                Logger.error("gesture listener registration failed reason=install", e)
            }
            result
        }

    fun install(
        phoneWindowManager: Any,
        reloaded: Boolean,
    ) {
        check(registered.compareAndSet(false, true)) { "gesture listener already registered" }
        var installed = false
        try {
            registerGestureListener(phoneWindowManager, reloaded)
            installed = true
        } finally {
            if (!installed) registered.set(false)
        }
    }

    fun teardown(): Any? {
        val listener = registeredListener
        if (listener != null) {
            val unregister =
                pointerUnregistration ?: error("unregisterPointerEventListener unavailable")
            unregister.invoke(listener)
        }

        registeredActions.forEach { action ->
            runCatching(action::close).onFailure {
                Logger.warn("action close failed type=${action.javaClass.simpleName}", it)
            }
        }
        GestureInputMonitor.dispose()

        val pwm = phoneWindowManager
        phoneWindowManager = null
        registeredListener = null
        pointerUnregistration = null
        registeredActions = emptyList()
        registered.set(false)
        return pwm
    }

    private fun registerGestureListener(
        phoneWindowManager: Any,
        reloaded: Boolean,
    ) {
        val config =
            GestureConfig(
                swipeThresholdFraction = swipeThresholdPct / 100f,
                edgeExclusionDp = edgeExclusionDp.toFloat(),
                fingerLandingWindowMs = fingerLandingMs.toLong(),
                cooldownMs = cooldownMs.toLong(),
            )
        val bindings = PhoneWindowManagerBindings.resolve(phoneWindowManager, captureMode)
        val actions: Map<ActionId, Action> =
            ActionId.entries.associateWith { id ->
                ActionRegistry.build(
                    id,
                    bindings.systemContext,
                    bindings.screenshotDispatch,
                    phoneWindowManager.javaClass.classLoader,
                ) { splitMethod }
            }

        var installed = false
        try {
            // pilfering blocks the app from receiving touch events during the gesture
            val pilfers = GestureInputMonitor.create()
            val handler = bindings.handler
            val gestureHandler =
                GestureHandler(
                    context = bindings.systemContext,
                    config = config,
                    onTrigger = { handler.post { actions[selectedAction]?.execute() } },
                    onPilfer = { GestureInputMonitor.pilferPointers() },
                )
            val proxy =
                Proxy.newProxyInstance(
                    // PointerEventListener is hidden and lives in system_server's classloader
                    // https://cs.android.com/android/platform/superproject/main/+/main:core/java/android/view/WindowManagerPolicyConstants.java
                    phoneWindowManager.javaClass.classLoader,
                    arrayOf(bindings.pointerListenerClass),
                    PointerEventListenerProxy(gestureHandler),
                )
            bindings.pointerRegistration.invoke(proxy)

            this.phoneWindowManager = phoneWindowManager
            registeredListener = proxy
            pointerUnregistration = bindings.pointerUnregistration
            registeredActions = actions.values
            installed = true

            val splitScreen = actions[ActionId.TOGGLE_SPLIT_SCREEN] as? ToggleSplitScreenAction
            Logger.info(
                "hooks installed origin=${if (reloaded) "reload" else "boot"} " +
                    "unregister=${bindings.pointerUnregistration != null} " +
                    "screenshot=${bindings.screenshotDispatch != null} " +
                    "splitscreen=${splitScreen?.available == true} pilfer=$pilfers",
            )
        } finally {
            if (!installed) {
                actions.values.forEach { runCatching(it::close) }
                GestureInputMonitor.dispose()
            }
        }
    }
}
