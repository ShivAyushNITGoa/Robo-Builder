package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun ShieldGeneratorRepairTask(
    modifier: Modifier = Modifier,
    onTaskComplete: (Int) -> Unit
) {
    var amplitude by remember { mutableStateOf(0.2f) }
    var frequency by remember { mutableStateOf(0.8f) }

    // Constants for target wave
    val targetAmplitude = 0.6f
    val targetFrequency = 0.4f

    val waveMatch = remember(amplitude, frequency) {
        val ampDiff = kotlin.math.abs(amplitude - targetAmplitude)
        val freqDiff = kotlin.math.abs(frequency - targetFrequency)
        (1.0f - (ampDiff + freqDiff)).coerceIn(0f, 1f)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "wave_oscillation")
    val timeOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time_offset"
    )

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
                .border(2.dp, NeonCyan, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Waves, contentDescription = "Shield Wave", tint = NeonCyan)
                Text(
                    text = "SHIELD OSCILLOSCOPE REPAIR",
                    color = CyberWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = "SYNCHRONIZE FREQUENCY AND AMPLITUDE TO LOCK SHIELDS",
                color = CyberGray,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            // Live Oscilloscope Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
                    .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .testTag("oscilloscope_canvas")
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val centerY = h / 2f

                    // Draw static target wave (dashed yellow)
                    val targetPath = Path().apply {
                        moveTo(0f, centerY)
                        for (x in 0 until w.toInt() step 5) {
                            val rad = (x.toFloat() / w) * 4 * Math.PI.toFloat() * (targetFrequency * 5)
                            val y = centerY + kotlin.math.sin(rad) * (targetAmplitude * centerY * 0.8f)
                            lineTo(x.toFloat(), y)
                        }
                    }
                    drawPath(
                        path = targetPath,
                        color = Color.Yellow.copy(alpha = 0.4f),
                        style = Stroke(width = 3f)
                    )

                    // Draw animated user wave (solid neon cyan)
                    val userPath = Path().apply {
                        moveTo(0f, centerY)
                        for (x in 0 until w.toInt() step 5) {
                            val rad = (x.toFloat() / w) * 4 * Math.PI.toFloat() * (frequency * 5) + timeOffset
                            val y = centerY + kotlin.math.sin(rad) * (amplitude * centerY * 0.8f)
                            lineTo(x.toFloat(), y)
                        }
                    }
                    drawPath(
                        path = userPath,
                        color = NeonCyan,
                        style = Stroke(width = 4f)
                    )
                }
            }

            // Sliders
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("SHIELD AMPLITUDE", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Slider(
                    value = amplitude,
                    onValueChange = { amplitude = it },
                    modifier = Modifier.testTag("amplitude_slider"),
                    colors = SliderDefaults.colors(thumbColor = NeonCyan)
                )

                Text("SHIELD FREQUENCY", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Slider(
                    value = frequency,
                    onValueChange = { frequency = it },
                    modifier = Modifier.testTag("frequency_slider"),
                    colors = SliderDefaults.colors(thumbColor = NeonCyan)
                )
            }

            Text(
                text = "WAVE OVERLAY STABILITY: ${(waveMatch * 100).toInt()}%",
                color = if (waveMatch > 0.9f) NeonGreen else if (waveMatch > 0.6f) NeonYellow else LaserRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Button(
                onClick = { onTaskComplete((waveMatch * 100).toInt()) },
                enabled = waveMatch > 0.9f,
                modifier = Modifier.fillMaxWidth().testTag("sync_shields_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Text("LOCK DEFLECTOR SHIELDS", color = CyberObsidian, fontWeight = FontWeight.Bold)
            }
        }
    }
}
