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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seasentry.app.location.GpsFixState
import com.seasentry.app.location.LiveTrackingUiState
import com.seasentry.app.location.TrackingMode
import com.seasentry.app.navarea.NavigationalWarning
import com.seasentry.app.ui.components.AlertBellIcon
import com.seasentry.app.ui.components.SatelliteIcon
import com.seasentry.app.ui.components.SeaSentryTopHeader
import com.seasentry.app.ui.components.VesselLineArtIcon
import com.seasentry.app.ui.theme.CardWhite
import com.seasentry.app.ui.theme.EmergencyRed
import com.seasentry.app.ui.theme.OceanCard
import com.seasentry.app.ui.theme.OceanDark
import com.seasentry.app.ui.theme.OrangePrimary
import com.seasentry.app.ui.theme.OrangeText
import com.seasentry.app.ui.theme.SkyBlueBackground
import com.seasentry.app.ui.theme.StatusAmber
import com.seasentry.app.ui.theme.StatusGreen
import com.seasentry.app.ui.theme.TextDarkNavy
import com.seasentry.app.ui.theme.TextSecondarySlate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun HubScreen(
    currentLat: Double,
    currentLon: Double,
    isSOSActive: Boolean,
    onSendSOS: () -> Unit,
    onNavigateToDemo: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSurvival: () -> Unit,
    onNavigateToCoastGuard: () -> Unit,
    onNavigateToSettings: () -> Unit,
    navWarnings: List<NavigationalWarning> = emptyList(),
    onSimulateHazardWarning: () -> Unit = {},
    onDismissNavWarning: ((String) -> Unit)? = null,
    trackingMode: TrackingMode = TrackingMode.DEMO,
    onToggleTrackingMode: (TrackingMode) -> Unit = {},
    gpsFixState: GpsFixState = GpsFixState.IDLE,
    liveTrackingUiState: LiveTrackingUiState? = null,
    onRequestLocationPermission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var selectedWarning by remember { mutableStateOf<NavigationalWarning?>(null) }

    val hasGpsCoords = liveTrackingUiState?.latitude != null && liveTrackingUiState.longitude != null
    val formattedCoords = if (trackingMode == TrackingMode.LIVE_GPS) {
        if (hasGpsCoords) {
            String.format(
                Locale.US,
                "%.4f° N, %.4f° E",
                liveTrackingUiState!!.latitude,
                liveTrackingUiState.longitude
            )
        } else {
            when (gpsFixState) {
                GpsFixState.PERMISSION_DENIED -> "Location Permission Required"
                GpsFixState.GPS_DISABLED -> "GPS Hardware Disabled"
                GpsFixState.ACQUIRING -> "Acquiring GPS fix (offline satellites)..."
                GpsFixState.IDLE -> "GPS Standby"
                else -> "No coordinates acquired"
            }
        }
    } else {
        String.format(
            Locale.US,
            "%.4f° N, %.4f° E",
            currentLat,
            currentLon
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SkyBlueBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Navy Top Header Bar
            SeaSentryTopHeader(
                title = "SeaSentry Hub",
                subtitle = "Active Vessel: SEASENTRY-PRO-2026",
                rightContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable { onNavigateToSettings() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("⚙ Settings", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tracking Mode Segmented Switcher
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Live GPS Pill
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (trackingMode == TrackingMode.LIVE_GPS) OceanDark else Color(0xFFF1F5F9)
                                )
                                .clickable { onToggleTrackingMode(TrackingMode.LIVE_GPS) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🛰", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LIVE HARDWARE GPS",
                                    color = if (trackingMode == TrackingMode.LIVE_GPS) Color.White else TextDarkNavy,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        // Demo Simulation Pill
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (trackingMode == TrackingMode.DEMO) OceanDark else Color(0xFFF1F5F9)
                                )
                                .clickable { onToggleTrackingMode(TrackingMode.DEMO) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚡", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "DEMO SIMULATION",
                                    color = if (trackingMode == TrackingMode.DEMO) Color.White else TextDarkNavy,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }

                // Card 1: Navy Vessel Status Card (matching Image 4)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = OceanCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top status badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (trackingMode == TrackingMode.LIVE_GPS) {
                                    when (gpsFixState) {
                                        GpsFixState.FIXED -> {
                                            SatelliteIcon(modifier = Modifier.size(18.dp), color = StatusGreen)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "GPS SATELLITE FIX: LOCKED",
                                                color = StatusGreen,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.8.sp
                                            )
                                        }
                                        GpsFixState.ACQUIRING -> {
                                            SatelliteIcon(modifier = Modifier.size(18.dp), color = StatusAmber)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (liveTrackingUiState?.seedFixAgeMs != null) "ACQUIRING (CACHED FIX)" else "ACQUIRING GPS SIGNAL…",
                                                color = StatusAmber,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.8.sp
                                            )
                                        }
                                        GpsFixState.PERMISSION_DENIED -> {
                                            SatelliteIcon(modifier = Modifier.size(18.dp), color = EmergencyRed)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "PERMISSION REQUIRED",
                                                color = EmergencyRed,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.8.sp
                                            )
                                        }
                                        GpsFixState.GPS_DISABLED -> {
                                            SatelliteIcon(modifier = Modifier.size(18.dp), color = EmergencyRed)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "GPS DISABLED IN SETTINGS",
                                                color = EmergencyRed,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.8.sp
                                            )
                                        }
                                        GpsFixState.IDLE -> {
                                            SatelliteIcon(modifier = Modifier.size(18.dp), color = Color.White.copy(alpha = 0.7f))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "GPS STANDBY",
                                                color = Color.White.copy(alpha = 0.7f),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.8.sp
                                            )
                                        }
                                    }
                                } else {
                                    SatelliteIcon(modifier = Modifier.size(18.dp), color = StatusGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "NAVIC SATELLITE: ACTIVE",
                                        color = StatusGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    )
                                }
                            }

                            // Accuracy badge or Mode tag
                            if (trackingMode == TrackingMode.LIVE_GPS && liveTrackingUiState?.accuracyMeters != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "±${liveTrackingUiState.accuracyMeters.toInt()}m",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else if (trackingMode == TrackingMode.DEMO) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(OrangePrimary.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "DEMO",
                                        color = OrangePrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Large White Boat Line Art
                        VesselLineArtIcon(
                            modifier = Modifier.size(100.dp),
                            color = Color.White.copy(alpha = 0.85f),
                            strokeWidth = 2.2.dp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Vessel Coordinates
                        Text(
                            text = formattedCoords,
                            color = Color.White,
                            fontSize = if (formattedCoords.length > 25) 14.sp else 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        if (trackingMode == TrackingMode.LIVE_GPS) {
                            when (gpsFixState) {
                                GpsFixState.ACQUIRING -> {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (liveTrackingUiState?.seedFixAgeMs != null)
                                            "Displaying cached fix from ${(liveTrackingUiState.seedFixAgeMs / 1000).coerceAtLeast(1)}s ago • Searching satellites"
                                        else
                                            "Standalone satellite lock in progress (No internet / SIM required)",
                                        color = Color.White.copy(alpha = 0.75f),
                                        fontSize = 11.sp
                                    )
                                }
                                GpsFixState.PERMISSION_DENIED -> {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = onRequestLocationPermission,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = EmergencyRed,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            "GRANT LOCATION PERMISSION",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                GpsFixState.FIXED -> {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Direct hardware GPS provider • 100% offline",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 11.sp
                                    )
                                }
                                else -> {}
                            }
                        }
                    }
                }

                // Card 2: Live Ocean Analytics (matching Image 4)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "LIVE OCEAN ANALYTICS",
                            color = OrangeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "29°C • Clear Skies",
                            color = TextDarkNavy,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val speedText = if (trackingMode == TrackingMode.LIVE_GPS && liveTrackingUiState?.fixState == GpsFixState.FIXED) {
                            String.format(Locale.US, "Speed: %.1f kts • Heading %03d° • Barometer 1012 hPa", liveTrackingUiState.speedKnots, liveTrackingUiState.headingDegrees.toInt())
                        } else if (trackingMode == TrackingMode.LIVE_GPS && liveTrackingUiState?.seedFixAgeMs != null) {
                            String.format(Locale.US, "Speed: %.1f kts (Cached) • Heading %03d° • Barometer 1012 hPa", liveTrackingUiState.speedKnots, liveTrackingUiState.headingDegrees.toInt())
                        } else {
                            "Wind: 12 kts NE • Sea State 2 • Barometer 1012 hPa"
                        }
                        Text(
                            text = speedText,
                            color = TextSecondarySlate,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Active NAVAREA Hazard Warnings (if any injected)
                if (navWarnings.isNotEmpty()) {
                    Text(
                        text = "ACTIVE NAVAREA HAZARD WARNINGS (${navWarnings.size})",
                        color = TextDarkNavy,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    navWarnings.forEach { warning ->
                        NavAreaWarningCard(
                            warning = warning,
                            onClick = { selectedWarning = warning }
                        )
                    }
                }

                // Quick Action Cards Section
                Text(
                    text = "SAFETY MODULES & CONTROLS",
                    color = TextDarkNavy,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Simulate Hazard Warning Demo Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSimulateHazardWarning() },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF6366F1).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚠️", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Simulate Hazard Warning",
                                    color = TextDarkNavy,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF6366F1).copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "DEMO",
                                        color = Color(0xFF4338CA),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                            Text(
                                text = "Broadcast realistic NAVAREA VIII hazard advisory",
                                color = TextSecondarySlate,
                                fontSize = 12.sp
                            )
                        }
                        Text("›", color = Color(0xFF6366F1), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Interactive Demo Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToDemo() },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(OrangePrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚡", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Interactive IMBL Demo",
                                color = TextDarkNavy,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Simulate boat moving towards maritime border",
                                color = TextSecondarySlate,
                                fontSize = 12.sp
                            )
                        }
                        Text("›", color = OrangePrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Safety History Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToHistory() },
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
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(StatusGreen.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🛡", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Safety History & Logs",
                                color = TextDarkNavy,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Inspect past geofence and danger alerts",
                                color = TextSecondarySlate,
                                fontSize = 12.sp
                            )
                        }
                        Text("›", color = TextSecondarySlate, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Offline Survival Guide Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToSurvival() },
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
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF3B82F6).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📖", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Offline Survival Guide",
                                color = TextDarkNavy,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Emergency checklists (Engine, MOB, Weather)",
                                color = TextSecondarySlate,
                                fontSize = 12.sp
                            )
                        }
                        Text("›", color = TextSecondarySlate, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Coast Guard Dashboard Switcher
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToCoastGuard() },
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
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(OceanCard),
                            contentAlignment = Alignment.Center
                        ) {
                            VesselLineArtIcon(modifier = Modifier.size(24.dp), color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Vessel Commander View",
                                color = TextDarkNavy,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Coast Guard fleet & distress monitoring",
                                color = TextSecondarySlate,
                                fontSize = 12.sp
                            )
                        }
                        Text("›", color = TextSecondarySlate, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Bottom Docked SOS Button
        Button(
            onClick = onSendSOS,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .height(58.dp)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(29.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSOSActive) Color(0xFF990012) else EmergencyRed,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AlertBellIcon(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    showRings = isSOSActive,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isSOSActive) "SOS TRANSMITTING • MESH ACTIVE" else "SEND SOS",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }

        // Bottom Sheet for Hazard Warning Details
        selectedWarning?.let { warning ->
            NavAreaWarningBottomSheet(
                warning = warning,
                onDismissRequest = { selectedWarning = null }
            )
        }
    }
}

/**
 * Visually distinct card for NAVAREA warnings labeled with DEMO badge.
 */
@Composable
fun NavAreaWarningCard(
    warning: NavigationalWarning,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF6366F1).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚠️", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "NAVAREA VIII ADVISORY",
                        color = Color(0xFF4F46E5),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    )
                }

                // Distinct DEMO Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF6366F1).copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "DEMO",
                        color = Color(0xFF4338CA),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = warning.title,
                color = TextDarkNavy,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = warning.text,
                color = TextSecondarySlate,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format(Locale.US, "%.4f° N, %.4f° E", warning.latitude, warning.longitude),
                    color = TextSecondarySlate,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "Tap for details ›",
                    color = Color(0xFF4F46E5),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Bottom sheet displaying full NavigationalWarning details (title, text, authority, coords).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavAreaWarningBottomSheet(
    warning: NavigationalWarning,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFCBD5E1))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Top badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF6366F1).copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "NAVAREA VIII WARNING",
                        color = Color(0xFF4F46E5),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF6366F1).copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "DEMO DATA",
                        color = Color(0xFF4338CA),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Warning Title
            Text(
                text = warning.title,
                color = TextDarkNavy,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Reference ID and Issued time
            Text(
                text = "Notice ID: ${warning.id} • Issued: ${formatWarningTime(warning.issuedAt)}",
                color = TextSecondarySlate,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Detailed Bulletin Text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "BULLETIN TEXT",
                        color = TextSecondarySlate,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = warning.text,
                        color = TextDarkNavy,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Geospatial & Authority Grid
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Area / Region:", color = TextSecondarySlate, fontSize = 12.sp)
                        Text(warning.area, color = TextDarkNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Coordinates:", color = TextSecondarySlate, fontSize = 12.sp)
                        Text(
                            String.format(Locale.US, "%.4f° N, %.4f° E", warning.latitude, warning.longitude),
                            color = TextDarkNavy,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Issuing Authority:", color = TextSecondarySlate, fontSize = 12.sp)
                        Text(warning.authority, color = Color(0xFF4F46E5), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Demo disclaimer notice
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF6366F1).copy(alpha = 0.08f))
                    .padding(12.dp)
            ) {
                Text(
                    text = "ℹ This navigational warning is simulated local test data for SeaSentry. Production builds will connect to live NAVAREA VIII broadcast feeds.",
                    color = Color(0xFF4338CA),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Close Button
            Button(
                onClick = onDismissRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OceanDark,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "CLOSE BULLETIN",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun formatWarningTime(timestamp: Long): String {
    if (timestamp == 0L) return "Recently issued"
    val sdf = SimpleDateFormat("dd MMM, HH:mm 'UTC'", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date(timestamp))
}
