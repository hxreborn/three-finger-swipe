package eu.hxreborn.tfs.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Destination : NavKey {
    @Serializable
    data object Home : Destination

    @Serializable
    data object EdgeExclusion : Destination

    @Serializable
    data object TriggerDistance : Destination

    @Serializable
    data object CaptureMode : Destination
}
