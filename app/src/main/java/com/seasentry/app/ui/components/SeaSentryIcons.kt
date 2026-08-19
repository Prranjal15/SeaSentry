package com.seasentry.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seasentry.app.ui.theme.EmergencyRed
import com.seasentry.app.ui.theme.OrangePrimary
import com.seasentry.app.ui.theme.StatusAmber
import com.seasentry.app.ui.theme.StatusGreen

/**
 * Authentic Boat Line-Art matching SeaSentry visual branding.
 */
@Composable
fun VesselLineArtIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    strokeWidth: Dp = 2.dp
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val sw = strokeWidth.toPx()

        // 1. Boat Hull
        val hullPath = Path().apply {
            moveTo(w * 0.10f, h * 0.58f)
            lineTo(w * 0.90f, h * 0.58f)
            cubicTo(
                w * 0.82f, h * 0.75f,
                w * 0.65f, h * 0.82f,
                w * 0.50f, h * 0.82f
            )
            cubicTo(
                w * 0.35f, h * 0.82f,
                w * 0.18f, h * 0.75f,
                w * 0.10f, h * 0.58f
            )
            close()
        }
        drawPath(
            path = hullPath,
            color = color,
            style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 2. Cabin / Wheelhouse
        val cabinPath = Path().apply {
            moveTo(w * 0.28f, h * 0.58f)
            lineTo(w * 0.28f, h * 0.40f)
            lineTo(w * 0.55f, h * 0.40f)
            lineTo(w * 0.55f, h * 0.58f)
        }
        drawPath(
            path = cabinPath,
            color = color,
            style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Cabin window
        drawRect(
            color = color,
            topLeft = Offset(w * 0.34f, h * 0.44f),
            size = Size(w * 0.14f, h * 0.08f),
            style = Stroke(width = sw * 0.8f)
        )

        // 3. Mast
        drawLine(
            color = color,
            start = Offset(w * 0.58f, h * 0.58f),
            end = Offset(w * 0.58f, h * 0.14f),
            strokeWidth = sw,
            cap = StrokeCap.Round
        )

        // 4. Sail (Main Triangle)
        val sail1 = Path().apply {
            moveTo(w * 0.58f, h * 0.16f)
            lineTo(w * 0.82f, h * 0.45f)
            lineTo(w * 0.58f, h * 0.48f)
            close()
        }
        drawPath(
            path = sail1,
            color = color,
            style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 5. Jib Sail (Secondary Front Triangle)
        val sail2 = Path().apply {
            moveTo(w * 0.54f, h * 0.22f)
            lineTo(w * 0.44f, h * 0.38f)
            lineTo(w * 0.54f, h * 0.38f)
            close()
        }
        drawPath(
            path = sail2,
            color = color,
            style = Stroke(width = sw * 0.9f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 6. Water Waves below hull
        val wavePath = Path().apply {
            moveTo(w * 0.05f, h * 0.88f)
            cubicTo(w * 0.20f, h * 0.82f, w * 0.30f, h * 0.94f, w * 0.45f, h * 0.88f)
            cubicTo(w * 0.60f, h * 0.82f, w * 0.70f, h * 0.94f, w * 0.85f, h * 0.88f)
            cubicTo(w * 0.92f, h * 0.85f, w * 0.96f, h * 0.90f, w * 0.98f, h * 0.88f)
        }
        drawPath(
            path = wavePath,
            color = color,
            style = Stroke(width = sw, cap = StrokeCap.Round)
        )
    }
}

/**
 * Emergency Alert Bell Icon with vibration arcs.
 */
@Composable
fun AlertBellIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    showRings: Boolean = true,
    strokeWidth: Dp = 2.5.dp
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val sw = strokeWidth.toPx()

        // Bell Body
        val bellPath = Path().apply {
            moveTo(w * 0.50f, h * 0.15f)
            cubicTo(w * 0.30f, h * 0.20f, w * 0.25f, h * 0.55f, w * 0.20f, h * 0.70f)
            lineTo(w * 0.80f, h * 0.70f)
            cubicTo(w * 0.75f, h * 0.55f, w * 0.70f, h * 0.20f, w * 0.50f, h * 0.15f)
            close()
        }
        drawPath(
            path = bellPath,
            color = color,
            style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Top loop
        drawCircle(
            color = color,
            radius = w * 0.06f,
            center = Offset(w * 0.50f, h * 0.12f),
            style = Stroke(width = sw * 0.8f)
        )

        // Clapper bottom
        drawCircle(
            color = color,
            radius = w * 0.08f,
            center = Offset(w * 0.50f, h * 0.78f)
        )

        // Vibration rings
        if (showRings) {
            // Left ring
            val leftArc = Path().apply {
                moveTo(w * 0.12f, h * 0.35f)
                cubicTo(w * 0.06f, h * 0.45f, w * 0.06f, h * 0.55f, w * 0.12f, h * 0.65f)
            }
            drawPath(path = leftArc, color = color, style = Stroke(width = sw * 0.8f, cap = StrokeCap.Round))

            // Right ring
            val rightArc = Path().apply {
                moveTo(w * 0.88f, h * 0.35f)
                cubicTo(w * 0.94f, h * 0.45f, w * 0.94f, h * 0.55f, w * 0.88f, h * 0.65f)
            }
            drawPath(path = rightArc, color = color, style = Stroke(width = sw * 0.8f, cap = StrokeCap.Round))
        }
    }
}

/**
 * Satellite / NavIC Signal Icon.
 */
@Composable
fun SatelliteIcon(
    modifier: Modifier = Modifier,
    color: Color = StatusGreen
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val sw = 1.8.dp.toPx()

        // Satellite body
        drawRect(
            color = color,
            topLeft = Offset(w * 0.35f, h * 0.35f),
            size = Size(w * 0.30f, h * 0.30f),
            style = Stroke(width = sw)
        )

        // Solar panels left
        drawLine(
            color = color,
            start = Offset(w * 0.10f, h * 0.40f),
            end = Offset(w * 0.35f, h * 0.40f),
            strokeWidth = sw
        )
        drawLine(
            color = color,
            start = Offset(w * 0.10f, h * 0.60f),
            end = Offset(w * 0.35f, h * 0.60f),
            strokeWidth = sw
        )
        drawLine(
            color = color,
            start = Offset(w * 0.10f, h * 0.40f),
            end = Offset(w * 0.10f, h * 0.60f),
            strokeWidth = sw
        )

        // Solar panels right
        drawLine(
            color = color,
            start = Offset(w * 0.65f, h * 0.40f),
            end = Offset(w * 0.90f, h * 0.40f),
            strokeWidth = sw
        )
        drawLine(
            color = color,
            start = Offset(w * 0.65f, h * 0.60f),
            end = Offset(w * 0.90f, h * 0.60f),
            strokeWidth = sw
        )
        drawLine(
            color = color,
            start = Offset(w * 0.90f, h * 0.40f),
            end = Offset(w * 0.90f, h * 0.60f),
            strokeWidth = sw
        )

        // Center dot
        drawCircle(
            color = color,
            radius = w * 0.05f,
            center = Offset(w * 0.50f, h * 0.50f)
        )
    }
}

/**
 * Clock / Timer Icon for Boundary Approach.
 */
@Composable
fun ClockIcon(
    modifier: Modifier = Modifier,
    color: Color = OrangePrimary
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val sw = 2.dp.toPx()

        drawCircle(
            color = color,
            radius = w * 0.42f,
            center = Offset(w * 0.5f, h * 0.5f),
            style = Stroke(width = sw)
        )

        // Clock hands
        drawLine(
            color = color,
            start = Offset(w * 0.5f, h * 0.5f),
            end = Offset(w * 0.5f, h * 0.25f),
            strokeWidth = sw,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.5f, h * 0.5f),
            end = Offset(w * 0.72f, h * 0.5f),
            strokeWidth = sw,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Severity tag pill badge.
 */
@Composable
fun SeverityBadge(
    tier: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (tier.uppercase()) {
        "CRITICAL" -> Pair(EmergencyRed.copy(alpha = 0.15f), EmergencyRed)
        "HIGH", "WARNING" -> Pair(OrangePrimary.copy(alpha = 0.15f), OrangePrimary)
        "MEDIUM", "ADVISORY" -> Pair(StatusAmber.copy(alpha = 0.15f), StatusAmber)
        else -> Pair(StatusGreen.copy(alpha = 0.15f), StatusGreen)
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tier.uppercase(),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
