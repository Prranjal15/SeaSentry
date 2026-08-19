package com.seasentry.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seasentry.app.data.SOSEvent
import com.seasentry.app.relay.RelayTelemetry
import com.seasentry.app.sos.SOSUiState
import com.seasentry.app.ui.components.AlertBellIcon
import com.seasentry.app.ui.components.SeverityBadge
import com.seasentry.app.ui.components.VesselCommanderHeader
import com.seasentry.app.ui.components.VesselLineArtIcon
import com.seasentry.app.ui.theme.CardWhite
import com.seasentry.app.ui.theme.EmergencyRed
import com.seasentry.app.ui.theme.OceanCard
import com.seasentry.app.ui.theme.OceanDark
import com.seasentry.app.ui.theme.OrangePrimary
import com.seasentry.app.ui.theme.OrangeText
import com.seasentry.app.ui.theme.PeachSquare
import com.seasentry.app.ui.theme.SkyBlueBackground
import com.seasentry.app.ui.theme.StatusAmber
import com.seasentry.app.ui.theme.StatusGreen
import com.seasentry.app.ui.theme.TextDarkNavy
import com.seasentry.app.ui.theme.TextSecondarySlate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CoastGuardScreen(
    sosState: SOSUiState,
    relayTelemetry: RelayTelemetry,
    onAcknowledgeSOS: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SkyBlueBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Navy Header matching Image 3 exactly
            VesselCommanderHeader(
                vesselId = "SEASENTRY-PRO-2026",
                onBackClick = onBackClick
            )

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Active Distress Alert Card if SOS is active
                if (sosState.isDistressActive && sosState.currentSOSEvent != null) {
                    val event = sosState.currentSOSEvent
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = EmergencyRed),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AlertBellIcon(
                                    modifier = Modifier.size(28.dp),
                                    color = Color.White,
                                    showRings = true
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "🚨 ACTIVE DISTRESS SIGNAL RECEIVED",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "Sector Alpha Command Station",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Vessel Info
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Distress Vessel:", color = TextSecondarySlate, fontSize = 12.sp)
                                        Text(event.senderId, color = TextDarkNavy, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("GPS Coordinates:", color = TextSecondarySlate, fontSize = 12.sp)
                                        Text(
                                            String.format(Locale.US, "%.4f° N, %.4f° E", event.latitude, event.longitude),
                                            color = TextDarkNavy,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Relay Channel:", color = TextSecondarySlate, fontSize = 12.sp)
                                        Text(
                                            relayTelemetry.routeDescription,
                                            color = TextDarkNavy,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Mesh Hop Count:", color = TextSecondarySlate, fontSize = 12.sp)
                                        Text("${relayTelemetry.hopCount} Hops", color = OrangePrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Time Broadcast:", color = TextSecondarySlate, fontSize = 12.sp)
                                        Text(
                                            SimpleDateFormat("HH:mm:ss 'UTC'", Locale.US).format(Date(event.timestamp)),
                                            color = TextDarkNavy,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Dispatch Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onAcknowledgeSOS,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = EmergencyRed
                                    )
                                ) {
                                    Text(
                                        text = "ACKNOWLEDGE & RESOLVE",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmergencyRed
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Normal Standby Status
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(StatusGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✔", color = StatusGreen, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "SECTOR ALPHA PATROL ACTIVE",
                                    color = TextDarkNavy,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "All vessels within authorized maritime boundaries.",
                                    color = TextSecondarySlate,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Sector Fleet Radar / Vessels Monitor
                Text(
                    text = "SECTOR ALPHA ACTIVE FLEET",
                    color = TextDarkNavy,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                // Vessel 1: Sentry Pro (User Vessel)
                VesselStatusRow(
                    vesselId = "SEASENTRY-PRO-2026",
                    vesselType = "Deep Sea Fishing Trawler",
                    coords = "18.9220° N, 72.8347° E",
                    status = if (sosState.isDistressActive) "DISTRESS" else "NORMAL",
                    isDistress = sosState.isDistressActive
                )

                // Vessel 2: Sagar Ratna
                VesselStatusRow(
                    vesselId = "SAGAR-RATNA-04",
                    vesselType = "Coastal Patrol Craft",
                    coords = "18.9110° N, 72.8105° E",
                    status = "PATROLLING",
                    isDistress = false
                )

                // Vessel 3: Matsya Kanya
                VesselStatusRow(
                    vesselId = "MATSYA-KANYA-12",
                    vesselType = "Gillnetter Trawler",
                    coords = "18.9280° N, 72.8450° E",
                    status = "NORMAL",
                    isDistress = false
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun VesselStatusRow(
    vesselId: String,
    vesselType: String,
    coords: String,
    status: String,
    isDistress: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDistress) EmergencyRed.copy(alpha = 0.08f) else CardWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isDistress) EmergencyRed.copy(alpha = 0.15f) else OceanDark.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                VesselLineArtIcon(
                    modifier = Modifier.size(26.dp),
                    color = if (isDistress) EmergencyRed else OceanDark,
                    strokeWidth = 1.6.dp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vesselId,
                    color = TextDarkNavy,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$vesselType • $coords",
                    color = TextSecondarySlate,
                    fontSize = 11.sp
                )
            }

            SeverityBadge(tier = if (isDistress) "CRITICAL" else status)
        }
    }
}
