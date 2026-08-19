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
import com.seasentry.app.ui.components.AlertBellIcon
import com.seasentry.app.ui.components.SatelliteIcon
import com.seasentry.app.ui.components.SeaSentryTopHeader
import com.seasentry.app.ui.components.VesselLineArtIcon
import com.seasentry.app.ui.theme.CardWhite
import com.seasentry.app.ui.theme.EmergencyRed
import com.seasentry.app.ui.theme.OceanCard
import com.seasentry.app.ui.theme.OrangePrimary
import com.seasentry.app.ui.theme.OrangeText
import com.seasentry.app.ui.theme.SkyBlueBackground
import com.seasentry.app.ui.theme.StatusGreen
import com.seasentry.app.ui.theme.TextDarkNavy
import com.seasentry.app.ui.theme.TextSecondarySlate
import java.util.Locale

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
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val formattedCoords = String.format(
        Locale.US,
        "%.4f° N, %.4f° E",
        currentLat,
        currentLon
    )

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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
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
                        Text(
                            text = "Wind: 12 kts NE • Sea State 2 • Barometer 1012 hPa",
                            color = TextSecondarySlate,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
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

                // Interactive Demo Card (Key requirement)
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

        // Bottom Docked SOS Button (matching Image 4)
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
    }
}
