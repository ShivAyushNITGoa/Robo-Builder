package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta

@Composable
fun HolographicDiagnosticUI(
    modifier: Modifier = Modifier,
    title: String = "SYSTEM_OK",
    progressValue: Float = 0.85f,
    accentColor: Color = NeonCyan
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hologram_inf")

    // Slow rotate
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Pulse brightness
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.minDimension * 0.45f
            val innerRadius = outerRadius * 0.8f

            // 1. Draw outer rotating compass notches
            rotate(rotationAngle, pivot = center) {
                drawCircle(
                    color = accentColor.copy(alpha = 0.25f * pulseAlpha),
                    radius = outerRadius,
                    center = center,
                    style = Stroke(width = 2f)
                )

                // 4 compass notches
                val notchLen = 15f
                for (angle in 0 until 360 step 45) {
                    val rad = Math.toRadians(angle.toDouble()).toFloat()
                    val dx = kotlin.math.cos(rad)
                    val dy = kotlin.math.sin(rad)
                    drawLine(
                        color = accentColor.copy(alpha = 0.7f * pulseAlpha),
                        start = Offset(center.x + dx * (outerRadius - notchLen), center.y + dy * (outerRadius - notchLen)),
                        end = Offset(center.x + dx * outerRadius, center.y + dy * outerRadius),
                        strokeWidth = 3f
                    )
                }
            }

            // 2. Draw interior progress bar ring
            drawArc(
                color = accentColor.copy(alpha = 0.1f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
                size = Size(innerRadius * 2, innerRadius * 2),
                style = Stroke(width = 6f)
            )

            drawArc(
                color = accentColor,
                startAngle = -90f,
                sweepAngle = progressValue * 360f,
                useCenter = false,
                topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
                size = Size(innerRadius * 2, innerRadius * 2),
                style = Stroke(width = 8f)
            )

            // Outer warning bounding box
            drawRect(
                color = NeonMagenta.copy(alpha = 0.15f * pulseAlpha),
                topLeft = Offset(10f, 10f),
                size = Size(size.width - 20f, size.height - 20f),
                style = Stroke(width = 1.5f)
            )
        }

        // Inner text readouts
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp).alpha(pulseAlpha)
        ) {
            Text(
                text = title,
                color = accentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "${(progressValue * 100f).toInt()}%",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "COCKPIT DIAGNOSTICS",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
