package com.seasentry.app.geofence

import androidx.compose.ui.graphics.Color
import com.seasentry.app.ui.theme.EmergencyRed
import com.seasentry.app.ui.theme.OrangePrimary
import com.seasentry.app.ui.theme.StatusAmber
import com.seasentry.app.ui.theme.StatusGreen

enum class AlertLevel(
    val title: String,
    val severityTag: String,
    val color: Color,
    val description: String,
    val defaultAction: String
) {
    SAFE(
        title = "Zone Safe",
        severityTag = "SAFE",
        color = StatusGreen,
        description = "Vessel is operating within permitted territorial waters.",
        defaultAction = "Maintain standard navigation course."
    ),
    ADVISORY(
        title = "Boundary Advisory",
        severityTag = "ADVISORY",
        color = StatusAmber,
        description = "Approaching maritime boundary zone (~1 km).",
        defaultAction = "Monitor NavIC radar and verify course."
    ),
    WARNING(
        title = "Boundary Warning",
        severityTag = "HIGH",
        color = OrangePrimary,
        description = "Close proximity to IMBL boundary (~500 m).",
        defaultAction = "Reduce vessel speed and prepare to alter course."
    ),
    CRITICAL(
        title = "Critical Breach Warning",
        severityTag = "CRITICAL",
        color = EmergencyRed,
        description = "IMBL BORDER BREACHED / IMMEDIATE PROXIMITY (< 200 m).",
        defaultAction = "TURN BOAT 180° SOUTH IMMEDIATELY"
    );

    fun isMoreSevereThan(other: AlertLevel): Boolean {
        return this.ordinal > other.ordinal
    }
}
