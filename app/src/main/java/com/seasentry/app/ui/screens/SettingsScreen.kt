package com.seasentry.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seasentry.app.geofence.GeofenceConfig
import com.seasentry.app.ui.components.SeaSentryTopHeader
import com.seasentry.app.ui.components.VesselLineArtIcon
import com.seasentry.app.ui.theme.CardWhite
import com.seasentry.app.ui.theme.OrangePrimary
import com.seasentry.app.ui.theme.OrangeText
import com.seasentry.app.ui.theme.SkyBlueBackground
import com.seasentry.app.ui.theme.StatusGreen
import com.seasentry.app.ui.theme.TextDarkNavy
import com.seasentry.app.ui.theme.TextSecondarySlate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var selectedLanguage by remember { mutableStateOf("English") }
    var languageExpanded by remember { mutableStateOf(false) }
    val languageOptions = listOf("English", "हिंदी (Hindi)", "मराठी (Marathi)", "தமிழ் (Tamil)")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SkyBlueBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SeaSentryTopHeader(
                title = "Vessel Settings",
                subtitle = "Configuration & Device Telemetry",
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // App Language Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "APP LANGUAGE",
                            color = OrangeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "Select preferred language for navigation alerts and voice advisories.",
                            color = TextSecondarySlate,
                            fontSize = 13.sp
                        )

                        ExposedDropdownMenuBox(
                            expanded = languageExpanded,
                            onExpandedChange = { languageExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedLanguage,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageExpanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = CardWhite,
                                    unfocusedContainerColor = CardWhite,
                                    focusedBorderColor = OrangePrimary,
                                    unfocusedBorderColor = Color(0xFFD0D7DE),
                                    focusedTextColor = TextDarkNavy,
                                    unfocusedTextColor = TextDarkNavy
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = languageExpanded,
                                onDismissRequest = { languageExpanded = false },
                                modifier = Modifier.background(CardWhite)
                            ) {
                                languageOptions.forEach { language ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = language,
                                                color = if (language == selectedLanguage) OrangePrimary else TextDarkNavy,
                                                fontWeight = if (language == selectedLanguage) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 14.sp
                                            )
                                        },
                                        onClick = {
                                            selectedLanguage = language
                                            languageExpanded = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }
                    }
                }

                // Vessel Registration Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "VESSEL PROFILE",
                            color = OrangeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )

                        SettingRow("Vessel Name", "Sagar Sentry One")
                        SettingRow("Registration No", "IND-MH-01-2026")
                        SettingRow("MMSI Transponder", "4190002026")
                        SettingRow("Vessel Type", "Deep Sea Trawler (48 ft)")
                        SettingRow("Port of Registry", "Mumbai (Sasoon Docks)")
                    }
                }

                // Geofence Engine Settings
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "GEOFENCE & THRESHOLDS",
                            color = OrangeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )

                        SettingRow("Active Boundary", GeofenceConfig.DEFAULT_IMBL_NAME)
                        SettingRow("Reference Point", "${GeofenceConfig.DEFAULT_IMBL_LAT}° N, ${GeofenceConfig.DEFAULT_IMBL_LON}° E")
                        SettingRow("Advisory Distance", "${GeofenceConfig.ADVISORY_THRESHOLD_METERS.toInt()} meters (~1 km)")
                        SettingRow("Warning Distance", "${GeofenceConfig.WARNING_THRESHOLD_METERS.toInt()} meters (~500 m)")
                        SettingRow("Critical Breach Distance", "${GeofenceConfig.CRITICAL_THRESHOLD_METERS.toInt()} meters (~200 m)")
                    }
                }

                // Satellite & Offline Cache Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "COMMUNICATIONS & CACHE",
                            color = OrangeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )

                        SettingRow("NavIC Constellation", "Locked (7 Satellites)")
                        SettingRow("Nearby Mesh Relay", "Standby / Auto-hop")
                        SettingRow("Survival Guide Data", "Cached Locally (100% Offline)")
                        SettingRow("Room DB Persistence", "Active (Local SQLite)")
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun SettingRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondarySlate, fontSize = 13.sp)
        Text(text = value, color = TextDarkNavy, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
