package com.seasentry.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seasentry.app.data.AlertDao
import com.seasentry.app.data.AlertEvent
import com.seasentry.app.ui.components.AlertBellIcon
import com.seasentry.app.ui.components.ClockIcon
import com.seasentry.app.ui.components.SeverityBadge
import com.seasentry.app.ui.components.VesselLineArtIcon
import com.seasentry.app.ui.theme.EmergencyRed
import com.seasentry.app.ui.theme.OrangePrimary
import com.seasentry.app.ui.theme.OrangeText
import com.seasentry.app.ui.theme.SkyBlueBackground
import com.seasentry.app.ui.theme.StatusAmber
import com.seasentry.app.ui.theme.TextDarkNavy
import com.seasentry.app.ui.theme.TextSecondarySlate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun SafetyHistoryScreen(
    alertEventsFlow: Flow<List<AlertEvent>>,
    onClearLogs: () -> Unit,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val alertList by alertEventsFlow.collectAsState(initial = emptyList())

    // Provide baseline events if database was freshly initialized
    val displayList = if (alertList.isEmpty()) {
        getSeedAlertEvents()
    } else {
        alertList
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SkyBlueBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Top Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    if (onBackClick != null) {
                        Text(
                            text = "‹ BACK TO HUB",
                            color = OrangeText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            modifier = Modifier
                                .clickable { onBackClick() }
                                .padding(bottom = 4.dp)
                        )
                    }
                    Text(
                        text = "SEASENTRY",
                        color = OrangeText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Safety History",
                        color = TextDarkNavy,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Day / Filter Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("☀", color = OrangePrimary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "DAY",
                            color = OrangePrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Alert Events List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 100.dp, top = 4.dp)
            ) {
                items(displayList, key = { it.id }) { item ->
                    SafetyEventCard(item)
                }
            }
        }

        // Bottom Clear All Logs Button
        Button(
            onClick = onClearLogs,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .height(56.dp)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OrangePrimary,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🗑", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CLEAR ALL LOGS",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun SafetyEventCard(event: AlertEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier.size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                when (event.tier.uppercase()) {
                    "CRITICAL" -> AlertBellIcon(
                        modifier = Modifier.size(30.dp),
                        color = EmergencyRed,
                        showRings = false,
                        strokeWidth = 2.dp
                    )
                    "HIGH", "WARNING" -> if (event.title.contains("Anchor", ignoreCase = true)) {
                        VesselLineArtIcon(
                            modifier = Modifier.size(28.dp),
                            color = OrangePrimary,
                            strokeWidth = 1.8.dp
                        )
                    } else {
                        ClockIcon(
                            modifier = Modifier.size(28.dp),
                            color = OrangePrimary
                        )
                    }
                    "MEDIUM", "ADVISORY" -> if (event.title.contains("Sea", ignoreCase = true)) {
                        VesselLineArtIcon(
                            modifier = Modifier.size(28.dp),
                            color = StatusAmber,
                            strokeWidth = 1.8.dp
                        )
                    } else {
                        AlertBellIcon(
                            modifier = Modifier.size(28.dp),
                            color = StatusAmber,
                            showRings = false,
                            strokeWidth = 1.8.dp
                        )
                    }
                    else -> AlertBellIcon(
                        modifier = Modifier.size(28.dp),
                        color = OrangePrimary,
                        showRings = false,
                        strokeWidth = 1.8.dp
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    color = TextDarkNavy,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatTimestamp(event.timestamp),
                    color = TextSecondarySlate,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Severity Tag
            SeverityBadge(tier = event.tier)
        }
    }
}

private fun formatTimestamp(millis: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy • HH:mm 'UTC'", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date(millis))
}

private fun getSeedAlertEvents(): List<AlertEvent> {
    val now = System.currentTimeMillis()
    return listOf(
        AlertEvent(
            id = "seed-1",
            tier = "CRITICAL",
            latitude = 18.9390,
            longitude = 72.8347,
            timestamp = now - 3600000 * 48,
            title = "Collision Risk",
            details = "Proximity alarm triggered near commercial shipping fairway"
        ),
        AlertEvent(
            id = "seed-2",
            tier = "HIGH",
            latitude = 18.9350,
            longitude = 72.8347,
            timestamp = now - 3600000 * 24,
            title = "Boundary Approach",
            details = "Approached within 500m of maritime boundary"
        ),
        AlertEvent(
            id = "seed-3",
            tier = "HIGH",
            latitude = 18.9220,
            longitude = 72.8347,
            timestamp = now - 3600000 * 36,
            title = "Anchor Drag Detected",
            details = "GPS position shift while vessel engine idle"
        ),
        AlertEvent(
            id = "seed-4",
            tier = "MEDIUM",
            latitude = 18.9100,
            longitude = 72.8300,
            timestamp = now - 3600000 * 50,
            title = "Gale Warning",
            details = "NavIC advisory: Squall winds up to 35 knots"
        ),
        AlertEvent(
            id = "seed-5",
            tier = "MEDIUM",
            latitude = 18.9150,
            longitude = 72.8250,
            timestamp = now - 3600000 * 12,
            title = "Rough Sea State",
            details = "Wave height exceeded 2.5 meters in Sector 4"
        )
    )
}
