package com.seasentry.app.sos

import com.seasentry.app.data.SOSDao
import com.seasentry.app.data.SOSEvent
import com.seasentry.app.relay.RelayManager
import com.seasentry.app.relay.RelayStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class SOSUiState(
    val isDistressActive: Boolean = false,
    val currentSOSEvent: SOSEvent? = null,
    val transmissionStatus: String = "IDLE",
    val relayHop: Int = 0,
    val lastBroadcastTime: Long = 0L
)

class SOSManager(
    private val scope: CoroutineScope,
    private val sosDao: SOSDao? = null,
    val relayManager: RelayManager = RelayManager(scope)
) {
    private val _sosState = MutableStateFlow(SOSUiState())
    val sosState: StateFlow<SOSUiState> = _sosState.asStateFlow()

    fun triggerSOS(
        senderId: String = "SEASENTRY-PRO-2026",
        latitude: Double = 18.9220,
        longitude: Double = 72.8347,
        message: String = "EMERGENCY: Distress Signal - Immediate Assistance Requested"
    ) {
        val now = System.currentTimeMillis()
        val event = SOSEvent(
            id = UUID.randomUUID().toString(),
            senderId = senderId,
            latitude = latitude,
            longitude = longitude,
            timestamp = now,
            message = message,
            hopCount = 0,
            status = "TRANSMITTING"
        )

        _sosState.value = SOSUiState(
            isDistressActive = true,
            currentSOSEvent = event,
            transmissionStatus = "TRANSMITTING DISTRESS BEACON...",
            relayHop = 0,
            lastBroadcastTime = now
        )

        // Persist to Room database
        sosDao?.let { dao ->
            scope.launch {
                dao.insertSOS(event)
            }
        }

        // Broadcast through RelayManager
        relayManager.broadcastSOS(event) {
            _sosState.value = _sosState.value.copy(
                transmissionStatus = "DELIVERED TO COAST GUARD",
                relayHop = 2,
                currentSOSEvent = event.copy(status = "RECEIVED_BY_COAST_GUARD", hopCount = 2)
            )

            sosDao?.let { dao ->
                scope.launch {
                    dao.updateStatus(event.id, "RECEIVED_BY_COAST_GUARD")
                }
            }
        }
    }

    fun resolveSOS() {
        val current = _sosState.value.currentSOSEvent
        if (current != null && sosDao != null) {
            scope.launch {
                sosDao.updateStatus(current.id, "RESOLVED")
            }
        }
        relayManager.reset()
        _sosState.value = SOSUiState(
            isDistressActive = false,
            currentSOSEvent = null,
            transmissionStatus = "STANDBY",
            relayHop = 0
        )
    }
}
