package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.util.Random

@Composable
fun LifeSupportCalibrationTask(
    modifier: Modifier = Modifier,
    onTaskComplete: (Int) -> Unit
) {
    var o2Level by remember { mutableStateOf(0.15f) }
    var n2Level by remember { mutableStateOf(0.45f) }
    var leaksPatched by remember { mutableStateOf(0) }
    var showLeakAlert by remember { mutableStateOf(true) }

    val calibrationPercent = remember(o2Level, n2Level, leaksPatched) {
        val o2Diff = kotlin.math.abs(o2Level - 0.21f) // O2 should be 21%
        val n2Diff = kotlin.math.abs(n2Level - 0.78f) // N2 should be 78%
        val balance = (1f - (o2Diff + n2Diff)).coerceIn(0f, 1f)
        val leakBonus = (leaksPatched.toFloat() / 3f).coerceIn(0f, 1f)
        (balance * 0.6f + leakBonus * 0.4f).coerceIn(0f, 1f)
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
                .border(2.dp, NeonYellow, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = "Alert", tint = NeonYellow)
                Text(
                    text = "LIFE SUPPORT CALIBRATION",
                    color = CyberWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = "Adjust valves to match standard atmosphere: O2 = 21%, N2 = 78%",
                color = CyberGray,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            // Gas meters
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("OXYGEN LEVEL (Target: 21%)", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Slider(
                    value = o2Level,
                    onValueChange = { o2Level = it },
                    modifier = Modifier.testTag("o2_slider"),
                    colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                )
                Text("Current: ${(o2Level * 100).toInt()}%", color = NeonCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)

                Text("NITROGEN LEVEL (Target: 78%)", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Slider(
                    value = n2Level,
                    onValueChange = { n2Level = it },
                    modifier = Modifier.testTag("n2_slider"),
                    colors = SliderDefaults.colors(thumbColor = NeonYellow, activeTrackColor = NeonYellow)
                )
                Text("Current: ${(n2Level * 100).toInt()}%", color = NeonYellow, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }

            // Interactive pressure leaks
            if (showLeakAlert) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(LaserRed.copy(alpha = 0.2f))
                        .border(1.dp, LaserRed, RoundedCornerShape(8.dp))
                        .clickable {
                            leaksPatched++
                            if (leaksPatched >= 3) {
                                showLeakAlert = false
                            }
                        }
                        .padding(12.dp)
                        .testTag("leak_alert_patch"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚠️ PRESSURE BREACH IN PIPELINE! TAP TO PATCH [Patches: $leaksPatched/3]",
                        color = LaserRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Calibration progress indicator
            LinearProgressIndicator(
                progress = { calibrationPercent },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                color = NeonGreen,
                trackColor = CyberObsidian
            )

            Button(
                onClick = { onTaskComplete((calibrationPercent * 100).toInt()) },
                enabled = calibrationPercent > 0.9f && !showLeakAlert,
                modifier = Modifier.fillMaxWidth().testTag("calibrate_complete_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Text("CALIBRATION COMPLETE", color = CyberObsidian, fontWeight = FontWeight.Bold)
            }
        }
    }
}
