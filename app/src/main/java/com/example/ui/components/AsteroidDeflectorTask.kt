package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.util.Random

data class AsteroidTarget(
    val id: Int,
    var x: Float, // relative 0..1
    var y: Float, // relative 0..1
    val size: Float,
    var hit: Boolean = false
)

@Composable
fun AsteroidDeflectorTask(
    modifier: Modifier = Modifier,
    onTaskComplete: (Int) -> Unit
) {
    val random = remember { Random() }
    var score by remember { mutableStateOf(0) }
    var hitsNeeded by remember { mutableStateOf(5) }
    val asteroidTargets = remember {
        mutableStateListOf<AsteroidTarget>().apply {
            repeat(4) { idx ->
                add(
                    AsteroidTarget(
                        id = idx,
                        x = 0.1f + random.nextFloat() * 0.8f,
                        y = 0.1f + random.nextFloat() * 0.5f,
                        size = 20f + random.nextFloat() * 20f
                    )
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberObsidian)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CyberSlate)
                .border(2.dp, NeonMagenta, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Shield, contentDescription = "Shield", tint = NeonMagenta)
                Text(
                    text = "ASTEROID SHIELD DEFLECTOR",
                    color = CyberWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = "INTERCEPT TARGETS BEFORE THEY BREACH OUTER SHELL",
                color = LaserRed,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Text(
                text = "TARGETS SECURED: $score / 5",
                color = NeonCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            // Interactive Radar Scope
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
                    .border(1.dp, NeonMagenta.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .testTag("asteroid_radar_scope")
                    .pointerInput(Unit) {
                        detectTapGestures { tapOffset ->
                            // Check collision
                            val w = size.width
                            val h = size.height

                            asteroidTargets.forEach { target ->
                                if (!target.hit) {
                                    val tx = target.x * w
                                    val ty = target.y * h
                                    val dx = tapOffset.x - tx
                                    val dy = tapOffset.y - ty
                                    val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                                    if (dist < target.size + 30f) {
                                        target.hit = true
                                        score++
                                        if (score >= 5) {
                                            onTaskComplete(100)
                                        } else {
                                            // Respawn target
                                            target.x = 0.1f + random.nextFloat() * 0.8f
                                            target.y = 0.1f + random.nextFloat() * 0.5f
                                            target.hit = false
                                        }
                                    }
                                }
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f

                    // Draw sonar radar concentric circles
                    drawCircle(Color(0xFF0F1A24), radius = size.minDimension * 0.45f)
                    drawCircle(NeonMagenta.copy(alpha = 0.15f), radius = size.minDimension * 0.45f, style = Stroke(width = 1.5f))
                    drawCircle(NeonMagenta.copy(alpha = 0.1f), radius = size.minDimension * 0.3f, style = Stroke(width = 1f))
                    drawCircle(NeonMagenta.copy(alpha = 0.05f), radius = size.minDimension * 0.15f, style = Stroke(width = 1f))

                    // Center shield base
                    drawCircle(NeonCyan, radius = 10f)

                    // Draw active asteroids
                    asteroidTargets.forEach { target ->
                        if (!target.hit) {
                            val tx = target.x * size.width
                            val ty = target.y * size.height

                            // Draw alert marker rings
                            drawCircle(
                                color = LaserRed.copy(alpha = 0.4f),
                                radius = target.size + 8f,
                                center = Offset(tx, ty),
                                style = Stroke(width = 2f)
                            )
                            // Draw core solid asteroid body
                            drawCircle(
                                color = Color(0xFF64748B),
                                radius = target.size,
                                center = Offset(tx, ty)
                            )
                        }
                    }
                }
            }
        }
    }
}
