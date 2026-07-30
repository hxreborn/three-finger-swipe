package eu.hxreborn.tfs.action.flashlight

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import eu.hxreborn.tfs.action.Action
import eu.hxreborn.tfs.util.Logger

class ToggleFlashlightAction(
    context: Context,
) : Action {
    private val cameraManager =
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private val cameraId: String? = findFlashCameraId()

    @Volatile
    private var flashlightOn = false

    private val torchCallback =
        object : CameraManager.TorchCallback() {
            override fun onTorchModeChanged(
                cameraId: String,
                enabled: Boolean,
            ) {
                if (cameraId == this@ToggleFlashlightAction.cameraId) {
                    flashlightOn = enabled
                }
            }
        }

    init {
        if (cameraId != null) {
            cameraManager.registerTorchCallback(
                torchCallback,
                Handler(Looper.getMainLooper()),
            )
        }
    }

    override fun execute() {
        val id =
            cameraId ?: run {
                Logger.warn("flashlight unavailable reason=no-camera")
                return
            }
        val enabled = !flashlightOn
        runCatching {
            cameraManager.setTorchMode(id, enabled)
        }.onSuccess {
            Logger.info("flashlight set enabled=$enabled")
        }.onFailure {
            Logger.error("flashlight set failed enabled=$enabled", it)
        }
    }

    override fun close() {
        if (cameraId == null) return
        runCatching { cameraManager.unregisterTorchCallback(torchCallback) }.onFailure {
            Logger.warn(
                "flashlight callback unregister failed",
                it,
            )
        }
    }

    private fun findFlashCameraId(): String? =
        runCatching {
            cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager
                    .getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        }.onFailure {
            Logger.warn("flashlight unavailable reason=camera-lookup", it)
        }.getOrNull()
}
