package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.isActive
import kotlin.random.Random

data class Star(
    var x: Float,
    var y: Float,
    val speed: Float,
    val size: Float,
    val alpha: Float,
    val color: Color
)

data class FloatingCrewmate(
    var x: Float,
    var y: Float,
    var rotation: Float,
    val speedX: Float,
    val speedY: Float,
    val rotSpeed: Float,
    val scale: Float,
    val color: Color,
    var isActive: Boolean = false
)

@Composable
fun SpaceStarfieldBackground(modifier: Modifier = Modifier) {
    var stars by remember { mutableStateOf<List<Star>>(emptyList()) }
    var crewmate by remember { mutableStateOf<FloatingCrewmate?>(null) }
    var frameTrigger by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        // Initialize 100 stars with varied depths and colors
        val list = List(100) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                speed = 0.0004f + Random.nextFloat() * 0.0018f,
                size = 1f + Random.nextFloat() * 3.5f,
                alpha = 0.3f + Random.nextFloat() * 0.7f,
                color = when (Random.nextInt(5)) {
                    0 -> Color(0xFF80DEEA) // Cyan/Ice Star
                    1 -> Color(0xFFFFCC80) // Soft Orange/Dwarf Star
                    2 -> Color(0xFFF48FB1) // Pink Nebula Star
                    else -> Color.White
                }
            )
        }
        stars = list

        // Starfield dynamic animation loop
        var lastTime = System.currentTimeMillis()
        while (isActive) {
            awaitFrame()
            val now = System.currentTimeMillis()
            frameTrigger = now
            
            // Update stars positions (scrolling smoothly to the left)
            stars.forEach { star ->
                star.x -= star.speed
                if (star.x < 0f) {
                    star.x = 1.0f
                    star.y = Random.nextFloat()
                }
            }

            // Update crewmate drifting across screen
            val currentCrewmate = crewmate
            if (currentCrewmate == null || !currentCrewmate.isActive) {
                // Low probability of spawning a drifting spacer
                if (Random.nextFloat() < 0.0025f) {
                    crewmate = FloatingCrewmate(
                        x = 1.1f, // Spawns off-screen right
                        y = 0.15f + Random.nextFloat() * 0.7f,
                        rotation = Random.nextFloat() * 360f,
                        speedX = -0.0008f - Random.nextFloat() * 0.0015f,
                        speedY = (Random.nextFloat() - 0.5f) * 0.0006f,
                        rotSpeed = 0.15f + Random.nextFloat() * 0.45f,
                        scale = 0.65f + Random.nextFloat() * 0.65f,
                        color = when (Random.nextInt(12)) {
                            0 -> Color(0xFFE53935) // Red
                            1 -> Color(0xFF1E88E5) // Blue
                            2 -> Color(0xFF43A047) // Green
                            3 -> Color(0xFFFDD835) // Yellow
                            4 -> Color(0xFF8E24AA) // Purple
                            5 -> Color(0xFFF4511E) // Orange
                            6 -> Color(0xFF00ACC1) // Cyan
                            7 -> Color(0xFFD81B60) // Pink
                            8 -> Color(0xFF3949AB) // Indigo
                            9 -> Color(0xFF757575) // Grey
                            10 -> Color(0xFF00E676) // Lime Green
                            else -> Color(0xFFFFFFFF) // White Crewmate
                        },
                        isActive = true
                    )
                }
            } else {
                currentCrewmate.x += currentCrewmate.speedX
                currentCrewmate.y += currentCrewmate.speedY
                currentCrewmate.rotation += currentCrewmate.rotSpeed
                
                // Reset if drifted completely off screen bounds
                if (currentCrewmate.x < -0.3f || currentCrewmate.y < -0.3f || currentCrewmate.y > 1.3f) {
                    currentCrewmate.isActive = false
                }
                // Update state trigger
                crewmate = currentCrewmate.copy()
            }
        }
    }

    // Reference frameTrigger to force recomposition with awaitFrame loop
    val tick = frameTrigger

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (width == 0f || height == 0f) return@Canvas

        // 1. Draw solid outer space dark backdrop
        drawRect(color = Color(0xFF070A13))

        // 2. Draw active stars
        stars.forEach { star ->
            val px = star.x * width
            val py = star.y * height
            drawCircle(
                color = star.color.copy(alpha = star.alpha),
                radius = star.size,
                center = Offset(px, py)
            )
        }

        // 3. Draw Floating Space Crewmate/Robot Silhouette (if active)
        crewmate?.let { c ->
            if (c.isActive) {
                val cx = c.x * width
                val cy = c.y * height
                val scaleFactor = c.scale * 34f // Base size of vector space avatar

                this.drawContext.canvas.save()
                this.drawContext.canvas.translate(cx, cy)
                this.drawContext.canvas.rotate(c.rotation)

                val outlineColor = Color(0xFF070A13)
                val primaryColor = c.color
                
                // Shaded version of primary color (multiply channels by 0.6)
                val shadowColor = Color(
                    red = (primaryColor.red * 0.6f).coerceIn(0f, 1f),
                    green = (primaryColor.green * 0.6f).coerceIn(0f, 1f),
                    blue = (primaryColor.blue * 0.6f).coerceIn(0f, 1f),
                    alpha = 1f
                )

                // BACKPACK: Rounded rect on left
                drawRoundRect(
                    color = outlineColor,
                    topLeft = Offset(-scaleFactor * 0.65f, -scaleFactor * 0.45f),
                    size = Size(scaleFactor * 0.42f, scaleFactor * 0.84f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(scaleFactor * 0.16f, scaleFactor * 0.16f)
                )
                drawRoundRect(
                    color = shadowColor,
                    topLeft = Offset(-scaleFactor * 0.60f, -scaleFactor * 0.40f),
                    size = Size(scaleFactor * 0.32f, scaleFactor * 0.74f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(scaleFactor * 0.12f, scaleFactor * 0.12f)
                )

                // CORE CAPSULE BODY
                drawRoundRect(
                    color = outlineColor,
                    topLeft = Offset(-scaleFactor * 0.4f, -scaleFactor * 0.62f),
                    size = Size(scaleFactor * 0.8f, scaleFactor * 1.15f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(scaleFactor * 0.4f, scaleFactor * 0.4f)
                )
                
                // Body fill
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(-scaleFactor * 0.34f, -scaleFactor * 0.56f),
                    size = Size(scaleFactor * 0.68f, scaleFactor * 1.03f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(scaleFactor * 0.34f, scaleFactor * 0.34f)
                )

                // Shadow lower-half overlap
                drawRoundRect(
                    color = shadowColor,
                    topLeft = Offset(-scaleFactor * 0.34f, 0f),
                    size = Size(scaleFactor * 0.68f, scaleFactor * 0.47f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(scaleFactor * 0.08f, scaleFactor * 0.08f)
                )

                // Legs divider cutout
                drawRect(
                    color = outlineColor,
                    topLeft = Offset(-scaleFactor * 0.10f, scaleFactor * 0.34f),
                    size = Size(scaleFactor * 0.20f, scaleFactor * 0.32f)
                )

                // VISOR (Standard Glass Mask)
                drawRoundRect(
                    color = outlineColor,
                    topLeft = Offset(scaleFactor * 0.05f, -scaleFactor * 0.35f),
                    size = Size(scaleFactor * 0.46f, scaleFactor * 0.32f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(scaleFactor * 0.16f, scaleFactor * 0.16f)
                )
                drawRoundRect(
                    color = Color(0xFF1E3A8A), // Visor dark shadow base
                    topLeft = Offset(scaleFactor * 0.09f, -scaleFactor * 0.31f),
                    size = Size(scaleFactor * 0.38f, scaleFactor * 0.24f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(scaleFactor * 0.12f, scaleFactor * 0.12f)
                )
                drawRoundRect(
                    color = Color(0xFF60A5FA), // Visor light main blue
                    topLeft = Offset(scaleFactor * 0.09f, -scaleFactor * 0.31f),
                    size = Size(scaleFactor * 0.38f, scaleFactor * 0.14f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(scaleFactor * 0.12f, scaleFactor * 0.12f)
                )
                // Glare highlight reflection
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(scaleFactor * 0.15f, -scaleFactor * 0.28f),
                    size = Size(scaleFactor * 0.18f, scaleFactor * 0.06f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(scaleFactor * 0.03f, scaleFactor * 0.03f)
                )

                this.drawContext.canvas.restore()
            }
        }
    }
}
