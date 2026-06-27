package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Color definitions matching the cyber style
val CyberBlue = Color(0xFF00E5FF)
val CyberOrange = Color(0xFFFF9100)
val CyberRed = Color(0xFFFF1744)
val CyberLime = Color(0xFF00E676)
val CyberSteel = Color(0xFF90A4AE)

@Composable
fun TelemetryDashboard(
    motorRpm: Float,
    coreHeat: Float,
    batteryCharge: Float,
    manualOverride: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hud_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0A0E1A))
            .border(1.dp, CyberBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LIVE DIAGNOSTICS TELEMETRY",
                color = CyberBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (manualOverride) CyberOrange.copy(alpha = pulseAlpha)
                            else CyberLime.copy(alpha = pulseAlpha)
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (manualOverride) "MANUAL OVERRIDE ACTIVE" else "AUTO PILOT LINKED",
                    color = if (manualOverride) CyberOrange else CyberLime,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Row of 3 Stats Gauges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // RPM
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF0E1626), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "ENGINE RPM",
                    color = CyberSteel,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${motorRpm.toInt()}",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (motorRpm / 9200f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = CyberBlue,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }

            // Core Heat
            val heatColor = when {
                coreHeat > 85f -> CyberRed
                coreHeat > 60f -> CyberOrange
                else -> CyberLime
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF0E1626), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "CORE TEMP",
                    color = CyberSteel,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${coreHeat.toInt()}°C",
                    color = heatColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (coreHeat / 120f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = heatColor,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }

            // Battery
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF0E1626), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "BATTERY CELL",
                    color = CyberSteel,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${batteryCharge.toInt()}%",
                    color = if (batteryCharge < 25f) CyberRed else CyberLime,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (batteryCharge / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = if (batteryCharge < 25f) CyberRed else CyberLime,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }
        }
    }
}
