package com.seasentry.app.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seasentry.app.ui.theme.CardWhite
import com.seasentry.app.ui.theme.EmergencyRed
import com.seasentry.app.ui.theme.EmergencyRedLight
import com.seasentry.app.ui.theme.OceanBorder
import com.seasentry.app.ui.theme.OceanDark
import com.seasentry.app.ui.theme.OrangeLight
import com.seasentry.app.ui.theme.OrangePrimary
import com.seasentry.app.ui.theme.OrangeText
import com.seasentry.app.ui.theme.StatusAmber
import com.seasentry.app.ui.theme.TextDarkNavy
import com.seasentry.app.ui.theme.TextMutedSlate
import com.seasentry.app.ui.theme.TextSecondarySlate

/**
 * Prominent "DEMO - local accounts only" badge matching SeaSentry UI conventions.
 */
@Composable
fun DemoBadge(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(OrangeLight)
            .border(1.dp, OrangePrimary.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "⚡",
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "DEMO — LOCAL ACCOUNTS ONLY",
                color = OrangeText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )
        }
    }
}

/**
 * Standardized SeaSentry Form Text Field.
 */
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIconText: String? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = TextDarkNavy,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = TextMutedSlate,
                    fontSize = 14.sp
                )
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            leadingIcon = if (leadingIconText != null) {
                {
                    Text(
                        text = leadingIconText,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                    )
                }
            } else null,
            trailingIcon = if (isPassword) {
                {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable { passwordVisible = !passwordVisible }
                            .padding(4.dp)
                    ) {
                        Text(
                            text = if (passwordVisible) "HIDE" else "SHOW",
                            color = OrangePrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardWhite,
                unfocusedContainerColor = CardWhite,
                focusedBorderColor = OrangePrimary,
                unfocusedBorderColor = OceanBorder.copy(alpha = 0.35f),
                focusedTextColor = TextDarkNavy,
                unfocusedTextColor = TextDarkNavy,
                cursorColor = OrangePrimary
            )
        )
    }
}

/**
 * Error alert card for invalid credentials or registration validation errors.
 */
@Composable
fun AuthErrorBanner(
    errorMessage: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(EmergencyRedLight)
            .border(1.dp, EmergencyRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "⚠️",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = errorMessage,
                    color = EmergencyRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✕",
                    color = EmergencyRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
