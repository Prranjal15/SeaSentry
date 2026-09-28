package com.seasentry.app.auth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seasentry.app.ui.theme.CyanAccent
import com.seasentry.app.ui.theme.OceanDark
import com.seasentry.app.ui.theme.OrangePrimary
import com.seasentry.app.ui.theme.TextMutedSlate
import com.seasentry.app.ui.theme.TextPrimary
import kotlinx.coroutines.delay

/**
 * Animated Splash Screen matching the system splash theme (OceanDark).
 * Fades and scales in the SeaSentry shield logo, displays the app name & tagline for ~1.5s,
 * then seamlessly transitions to the auth gate.
 */
@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var animationStarted by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "SplashAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0.82f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "SplashScale"
    )

    LaunchedEffect(Unit) {
        animationStarted = true
        delay(1500)
        onSplashComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OceanDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(alpha)
                .scale(scale)
                .padding(24.dp)
        ) {
            // Shield & Anchor Brand Logo Canvas
            SplashBrandLogo(
                modifier = Modifier.size(110.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App Brand Name with Orange Highlight Accent
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Sea",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Sentry",
                    color = OrangePrimary,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tagline
            Text(
                text = "Offline Maritime Geofencing & Safety Sentinel",
                color = TextMutedSlate,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Subtle Sentinel Status Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(RoundedCornerShape(3.5.dp))
                            .background(CyanAccent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "100% OFFLINE SATELLITE DEFENSE",
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

/**
 * Maritime Safety Boundary Shield & Vessel Canvas matching SeaSentry visual palette.
 */
@Composable
fun SplashBrandLogo(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Shield Background Glow Base
        val shieldPath = Path().apply {
            moveTo(w * 0.50f, h * 0.18f)
            cubicTo(w * 0.62f, h * 0.21f, w * 0.72f, h * 0.21f, w * 0.76f, h * 0.26f)
            cubicTo(w * 0.76f, h * 0.55f, w * 0.65f, h * 0.74f, w * 0.50f, h * 0.81f)
            cubicTo(w * 0.35f, h * 0.74f, w * 0.24f, h * 0.55f, w * 0.24f, h * 0.26f)
            cubicTo(w * 0.28f, h * 0.21f, w * 0.38f, h * 0.21f, w * 0.50f, h * 0.18f)
            close()
        }

        // Shield Outer Border Stroke
        drawPath(
            path = shieldPath,
            color = OrangePrimary,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 2. Horizontal Boundary Line
        drawLine(
            color = CyanAccent,
            start = Offset(w * 0.32f, h * 0.48f),
            end = Offset(w * 0.68f, h * 0.48f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 3. Mast
        drawLine(
            color = Color.White,
            start = Offset(w * 0.50f, h * 0.26f),
            end = Offset(w * 0.50f, h * 0.58f),
            strokeWidth = 2.2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 4. Main Sail
        val mainSail = Path().apply {
            moveTo(w * 0.51f, h * 0.28f)
            lineTo(w * 0.66f, h * 0.45f)
            lineTo(w * 0.51f, h * 0.45f)
            close()
        }
        drawPath(path = mainSail, color = OrangePrimary)

        // 5. Jib Sail
        val jibSail = Path().apply {
            moveTo(w * 0.49f, h * 0.32f)
            lineTo(w * 0.38f, h * 0.45f)
            lineTo(w * 0.49f, h * 0.45f)
            close()
        }
        drawPath(path = jibSail, color = Color.White)

        // 6. Hull
        val hull = Path().apply {
            moveTo(w * 0.35f, h * 0.53f)
            lineTo(w * 0.65f, h * 0.53f)
            cubicTo(w * 0.63f, h * 0.62f, w * 0.57f, h * 0.67f, w * 0.50f, h * 0.67f)
            cubicTo(w * 0.43f, h * 0.67f, w * 0.37f, h * 0.62f, w * 0.35f, h * 0.53f)
            close()
        }
        drawPath(path = hull, color = Color.White)

        // 7. Anchor Flukes at bottom
        val anchorFlukes = Path().apply {
            moveTo(w * 0.40f, h * 0.69f)
            cubicTo(w * 0.44f, h * 0.74f, w * 0.50f, h * 0.75f, w * 0.50f, h * 0.75f)
            cubicTo(w * 0.50f, h * 0.75f, w * 0.56f, h * 0.74f, w * 0.60f, h * 0.69f)
        }
        drawPath(
            path = anchorFlukes,
            color = CyanAccent,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
