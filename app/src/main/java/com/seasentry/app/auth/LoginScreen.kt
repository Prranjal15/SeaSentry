package com.seasentry.app.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seasentry.app.ui.components.SeaSentryPrimaryButton
import com.seasentry.app.ui.components.VesselLineArtIcon
import com.seasentry.app.ui.theme.CardWhite
import com.seasentry.app.ui.theme.DeepBlue
import com.seasentry.app.ui.theme.OceanDark
import com.seasentry.app.ui.theme.OrangePrimary
import com.seasentry.app.ui.theme.PeachSquare
import com.seasentry.app.ui.theme.SkyBlueBackground
import com.seasentry.app.ui.theme.TextDarkNavy
import com.seasentry.app.ui.theme.TextSecondarySlate

/**
 * Demo Login Screen for vessel commanders with local validation & sample prefill.
 */
@Composable
fun LoginScreen(
    onLoginClick: (regNumber: String, pin: String) -> Unit,
    onContinueDemoClick: () -> Unit,
    onNavigateToRegister: () -> Unit,
    errorMessage: String?,
    onDismissError: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var registrationNumber by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SkyBlueBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main Auth Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Vessel Icon Header
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PeachSquare),
                        contentAlignment = Alignment.Center
                    ) {
                        VesselLineArtIcon(
                            modifier = Modifier.size(42.dp),
                            color = OceanDark,
                            strokeWidth = 2.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text(
                        text = "Vessel Commander Login",
                        color = TextDarkNavy,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Sign in to activate onboard sentinel",
                        color = TextSecondarySlate,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Demo Badge
                    DemoBadge()

                    Spacer(modifier = Modifier.height(20.dp))

                    // Error Alert Banner (if any)
                    if (errorMessage != null) {
                        AuthErrorBanner(
                            errorMessage = errorMessage,
                            onDismiss = onDismissError
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Registration Number Field
                    AuthTextField(
                        value = registrationNumber,
                        onValueChange = { registrationNumber = it },
                        label = "Vessel Registration Number",
                        placeholder = "e.g. TN-12345 or IND-8890",
                        leadingIconText = "🚢"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Security PIN Field
                    AuthTextField(
                        value = pin,
                        onValueChange = { if (it.length <= 6 && it.all { ch -> ch.isDigit() }) pin = it },
                        label = "Security PIN (4 to 6 digits)",
                        placeholder = "••••",
                        isPassword = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        leadingIconText = "🔒"
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    // Login Button
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = OrangePrimary, strokeWidth = 3.dp)
                        }
                    } else {
                        SeaSentryPrimaryButton(
                            text = "LOGIN",
                            onClick = { onLoginClick(registrationNumber, pin) },
                            backgroundColor = OrangePrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // One-tap "Continue as Demo Vessel" Button
                    Button(
                        onClick = onContinueDemoClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = DeepBlue
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, DeepBlue)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "⚡", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Continue as Demo Vessel (Sea Falcon)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Navigate to Register Option
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Need to register a vessel? ",
                            color = TextSecondarySlate,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Register Here",
                            color = OrangePrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { onNavigateToRegister() }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}
