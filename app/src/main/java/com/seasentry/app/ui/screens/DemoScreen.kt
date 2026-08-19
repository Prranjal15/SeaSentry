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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seasentry.app.demo.DemoUiState
import com.seasentry.app.geofence.AlertLevel
import com.seasentry.app.ui.components.AlertBellIcon
import com.seasentry.app.ui.components.SeaSentryNavyCard
import com.seasentry.app.ui.components.SeaSentryPrimaryButton
import com.seasentry.app.ui.components.SeaSentryTopHeader
import com.seasentry.app.ui.components.SeaSentryWhiteCard
import com.seasentry.app.ui.components.SeverityBadge
import com.seasentry.app.ui.components.VesselLineArtIcon
import com.seasentry.app.ui.theme.CardWhite
import com.seasentry.app.ui.theme.EmergencyRed
import com.seasentry.app.ui.theme.OceanCard
import com.seasentry.app.ui.theme.OrangePrimary
import com.seasentry.app.ui.theme.OrangeText
import com.seasentry.app.ui.theme.SkyBlueBackground
import com.seasentry.app.ui.theme.StatusAmber
import com.seasentry.app.ui.theme.StatusGreen
import com.seasentry.app.ui.theme.TextDarkNavy
import com.seasentry.app.ui.theme.TextSecondarySlate
import java.util.Locale

@Composable
fun DemoScreen(
    demoState: DemoUiState,
    onStartDemo: () -> Unit,
    onRestartDemo: () -> Unit,
    onPauseDemo: () -> Unit,
    onTriggerSOS: () -> Unit,
    onBackClick: () -> Unit,
    onOpenCriticalScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val progress = (demoState.currentStep.toFloat() / (demoState.totalSteps - 1).coerceAtLeast(1).toFloat())
        .coerceIn(0f, 1f)

    val formattedCoords = String.format(
        Locale.US,
        "%.4f° N, %.4f° E",
        demoState.latitude,
        demoState.longitude
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SkyBlueBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Navy Header
            SeaSentryTopHeader(
                title = "IMBL Live Simulation",
                subtitle = "Stationary Vessel Geofence Demo",
                onBackClick = onBackClick,
                rightContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    demoState.isRunning -> StatusGreen.copy(alpha = 0.2f)
                                    demoState.isCompleted -> OrangePrimary.copy(alpha = 0.2f)
                                    else -> Color.White.copy(alpha = 0.12f)
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = when {
                                demoState.isRunning -> "● SIM RUNNING"
                                demoState.isCompleted -> "✔ COMPLETE"
                                else -> "IDLE"
                            },
                            color = when {
                                demoState.isRunning -> StatusGreen
                                demoState.isCompleted -> OrangePrimary
                                else -> Color.White
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Live Vessel Telemetry Card (Navy)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = OceanCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SIMULATED VESSEL TELEMETRY",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            SeverityBadge(tier = demoState.alertLevel.severityTag)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                VesselLineArtIcon(
                                    modifier = Modifier.size(38.dp),
                                    color = Color.White,
                                    strokeWidth = 1.8.dp
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = formattedCoords,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Heading: 000° N • Speed: ${demoState.speedKnots} kts",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Target line
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Target: Sector Alpha IMBL",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Step ${demoState.currentStep + 1} of ${demoState.totalSteps}",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Geofence Distance Card (White Card)
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DISTANCE TO BOUNDARY",
                                color = OrangeText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = demoState.formattedDistance,
                                color = when (demoState.alertLevel) {
                                    AlertLevel.CRITICAL -> EmergencyRed
                                    AlertLevel.WARNING -> OrangePrimary
                                    AlertLevel.ADVISORY -> StatusAmber
                                    AlertLevel.SAFE -> StatusGreen
                                },
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Linear Progress Bar towards boundary
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = when (demoState.alertLevel) {
                                AlertLevel.CRITICAL -> EmergencyRed
                                AlertLevel.WARNING -> OrangePrimary
                                AlertLevel.ADVISORY -> StatusAmber
                                AlertLevel.SAFE -> StatusGreen
                            },
                            trackColor = Color(0xFFE2E8F0)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action instruction box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when (demoState.alertLevel) {
                                        AlertLevel.CRITICAL -> EmergencyRed.copy(alpha = 0.1f)
                                        AlertLevel.WARNING -> OrangePrimary.copy(alpha = 0.1f)
                                        AlertLevel.ADVISORY -> StatusAmber.copy(alpha = 0.1f)
                                        AlertLevel.SAFE -> StatusGreen.copy(alpha = 0.1f)
                                    }
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = demoState.actionRequired,
                                color = when (demoState.alertLevel) {
                                    AlertLevel.CRITICAL -> EmergencyRed
                                    AlertLevel.WARNING -> OrangePrimary
                                    AlertLevel.ADVISORY -> Color(0xFFB45309)
                                    AlertLevel.SAFE -> Color(0xFF047857)
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (demoState.alertLevel == AlertLevel.CRITICAL) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(EmergencyRed)
                                    .clickable { onOpenCriticalScreen() }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "VIEW FULL EMERGENCY SCREEN",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                // Threshold Reference Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "GEOFENCE THRESHOLDS",
                            color = TextDarkNavy,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Safe Zone (> 1000m)", color = TextSecondarySlate, fontSize = 12.sp)
                            Text("SAFE", color = StatusGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Advisory Zone (~1000m)", color = TextSecondarySlate, fontSize = 12.sp)
                            Text("ADVISORY", color = StatusAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Warning Zone (~500m)", color = TextSecondarySlate, fontSize = 12.sp)
                            Text("WARNING", color = OrangePrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Critical Breach (≤ 200m)", color = TextSecondarySlate, fontSize = 12.sp)
                            Text("CRITICAL", color = EmergencyRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Control Buttons
                if (!demoState.isRunning && !demoState.isCompleted) {
                    SeaSentryPrimaryButton(
                        text = "START DEMO",
                        onClick = onStartDemo,
                        backgroundColor = OrangePrimary
                    )
                } else if (demoState.isRunning) {
                    SeaSentryPrimaryButton(
                        text = "PAUSE SIMULATION",
                        onClick = onPauseDemo,
                        backgroundColor = Color(0xFF475569)
                    )
                } else {
                    SeaSentryPrimaryButton(
                        text = "RESTART DEMO",
                        onClick = onRestartDemo,
                        backgroundColor = OrangePrimary
                    )
                }

                if (demoState.isRunning || demoState.isCompleted) {
                    SeaSentryPrimaryButton(
                        text = "RESTART FROM BEGINNING",
                        onClick = onRestartDemo,
                        backgroundColor = CardWhite,
                        contentColor = TextDarkNavy
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
