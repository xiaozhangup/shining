package io.github.sunshinewzy.sunstcore.core.machine

import io.github.sunshinewzy.sunstcore.api.NamespacedKey
import kotlinx.serialization.Serializable

/**
 * Represent properties of a machine.
 */
@Serializable
data class MachineProperty(
    val id: NamespacedKey
)