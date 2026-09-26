package com.seasentry.app.navarea

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicInteger

class NavAreaRepository {
    private val _warnings = MutableStateFlow<List<NavigationalWarning>>(emptyList())
    val warnings: StateFlow<List<NavigationalWarning>> = _warnings.asStateFlow()

    private val counter = AtomicInteger(0)

    private val simulatedWarningTemplates = listOf(
        NavigationalWarning(
            id = "NAV8-2026-0841",
            title = "Fishing net debris reported",
            text = "Submerged drift nets and unlit fishing gear reported adrift between coordinates 18.9250° N and 18.9380° N in Mumbai fairway approaches. High risk of propeller entanglement. All vessels in vicinity exercise caution.",
            area = "NAVAREA VIII - Arabian Sea (Mumbai Approaches)",
            latitude = 18.9315,
            longitude = 72.8380,
            authority = "NAVAREA VIII Coordinator - India",
            issuedAt = 0L
        ),
        NavigationalWarning(
            id = "NAV8-2026-0842",
            title = "Temporary buoy malfunction",
            text = "Prongs Reef South Lighted Buoy (FL 10s) reported unlit and drifting 0.4 NM off charted position. Inbound and outbound mariners keep sharp lookout and maintain safe passing distance.",
            area = "NAVAREA VIII - West Coast of India",
            latitude = 18.9265,
            longitude = 72.8290,
            authority = "NAVAREA VIII Coordinator - India",
            issuedAt = 0L
        ),
        NavigationalWarning(
            id = "NAV8-2026-0843",
            title = "Semi-submerged container hazard",
            text = "Partially submerged 40ft container spotted adrift drifting southwest at estimated speed 1.3 knots. Hazardous navigational obstruction. Transiting vessels report sightings to MRCC Mumbai.",
            area = "NAVAREA VIII - Arabian Sea",
            latitude = 18.9360,
            longitude = 72.8420,
            authority = "NAVAREA VIII Coordinator - India",
            issuedAt = 0L
        )
    )

    fun injectSimulatedWarning() {
        val index = counter.getAndIncrement() % simulatedWarningTemplates.size
        val template = simulatedWarningTemplates[index]
        val timestamp = System.currentTimeMillis()

        val newWarning = template.copy(
            id = "${template.id.substringBeforeLast("-")}-${(1000..9999).random()}",
            issuedAt = timestamp
        )

        _warnings.value = listOf(newWarning) + _warnings.value
    }

    fun removeWarning(id: String) {
        _warnings.value = _warnings.value.filterNot { it.id == id }
    }

    fun clearWarnings() {
        _warnings.value = emptyList()
    }
}
