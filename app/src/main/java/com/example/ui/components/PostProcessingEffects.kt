package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta

@Composable
fun PostProcessingOverlay(
    modifier: Modifier = Modifier,
    isGlitching: Boolean = false,
    isWarpActive: Boolean = false,
    damageIntensity: Float = 0f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "post_processing")

    // CRT Scanline position animation
    val scanlineOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanline"
    )

    // Aberration wiggle
    val aberrationFactor by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aberration"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Dynamic CRT Scanline grid
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.12f)) {
            val step = 8.dp.toPx()
            val startY = scanlineOffset * size.height
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = Color.Black,
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(size.width, y),
                    strokeWidth = 2.dp.toPx()
                )
                y += step
            }

            // Big scanning bar
            drawLine(
                color = NeonCyan,
                start = androidx.compose.ui.geometry.Offset(0f, startY),
                end = androidx.compose.ui.geometry.Offset(size.width, startY),
                strokeWidth = 10.dp.toPx()
            )
        }

        // 2. Neon vignette / laser boundary glow
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.15f)) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, NeonMagenta, Color.Black),
                    center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f),
                    radius = size.width * 0.8f
                ),
                blendMode = BlendMode.Screen
            )
        }

        // 3. Glitch overlay
        if (isGlitching || damageIntensity > 0.1f) {
            val glitchAlpha = if (isGlitching) 0.25f else damageIntensity * 0.35f
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(glitchAlpha)
                    .graphicsLayer {
                        translationX = aberrationFactor * 4f
                    }
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0x33FF0055), Color.Transparent, Color(0x3300FF99))
                        )
                    )
            )
        }

        // 4. Warp Speed Motion Blur Overlay
        if (isWarpActive) {
            Canvas(modifier = Modifier.fillMaxSize().alpha(0.3f)) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                // Draw zooming warp streaks outward
                val linesCount = 24
                for (i in 0 until linesCount) {
                    val angle = (2 * Math.PI * i / linesCount).toFloat()
                    val dx = kotlin.math.cos(angle)
                    val dy = kotlin.math.sin(angle)
                    drawLine(
                        color = NeonCyan,
                        start = androidx.compose.ui.geometry.Offset(cx + dx * 100f, cy + dy * 100f),
                        end = androidx.compose.ui.geometry.Offset(cx + dx * (size.width / 2f), cy + dy * (size.height / 2f)),
                        strokeWidth = 3f
                    )
                }
            }
        }
    }
}
