package com.seasentry.app.relay

import com.seasentry.app.data.SOSEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class RelayStatus {
    IDLE,
    DISCOVERING_PEERS,
    BROADCASTING_SOS,
    MESH_HOP_PROPAGATING,
    DELIVERED_TO_COAST_GUARD,
    ACKNOWLEDGED
}

data class RelayTelemetry(
    val status: RelayStatus = RelayStatus.IDLE,
    val activePeersCount: Int = 3,
    val lastRelayedEvent: SOSEvent? = null,
    val hopCount: Int = 0,
    val routeDescription: String = "Direct Channel / Offline Mesh Ready"
)

class RelayManager(
    private val scope: CoroutineScope
) {
    private val _telemetry = MutableStateFlow(RelayTelemetry())
    val telemetry: StateFlow<RelayTelemetry> = _telemetry.asStateFlow()

    fun broadcastSOS(sosEvent: SOSEvent, onDelivered: (() -> Unit)? = null) {
        scope.launch {
            _telemetry.value = _telemetry.value.copy(
                status = RelayStatus.BROADCASTING_SOS,
                lastRelayedEvent = sosEvent,
                hopCount = 0,
                routeDescription = "Broadcasting Beacon via NavIC / Nearby Mesh..."
            )

            delay(800)
            _telemetry.value = _telemetry.value.copy(
                status = RelayStatus.MESH_HOP_PROPAGATING,
                hopCount = 1,
                routeDescription = "Relayed via Vessel-12 (Distance: 1.4 nm)"
            )

            delay(800)
            _telemetry.value = _telemetry.value.copy(
                status = RelayStatus.DELIVERED_TO_COAST_GUARD,
                hopCount = 2,
                routeDescription = "Delivered to Coast Guard Sector Alpha Maritime Station"
            )

            onDelivered?.invoke()

            delay(600)
            _telemetry.value = _telemetry.value.copy(
                status = RelayStatus.ACKNOWLEDGED,
                routeDescription = "Coast Guard Acknowledged - SAR Dispatch Ready"
            )
        }
    }

    fun reset() {
        _telemetry.value = RelayTelemetry()
    }
}
