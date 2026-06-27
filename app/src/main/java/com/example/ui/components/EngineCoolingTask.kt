package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun EngineCoolingTask(
    modifier: Modifier = Modifier,
    onTaskComplete: (Int) -> Unit
) {
    var engineTemperature by remember { mutableStateOf(95f) } // Overheated starting temp
    var clickStreak by remember { mutableStateOf(0) }

    // Temperature naturally climbs unless user pumps
    LaunchedEffect(Unit) {
        while (engineTemperature > 10f) {
            delay(200)
            engineTemperature = (engineTemperature + 1.2f).coerceAtMost(100f)
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
                .border(2.dp, NeonGreen, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.AcUnit, contentDescription = "Cooling", tint = NeonCyan)
                Text(
                    text = "ENGINE CRYOGENIC INJECTION",
                    color = CyberWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = "PUMP THE LEVER RAPIDLY TO INJECT COLD CONDENSATE",
                color = CyberGray,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            // Temperature Progress indicator
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "ENGINE TEMP: ${engineTemperature.toInt()}°C",
                    color = if (engineTemperature > 80f) LaserRed else if (engineTemperature > 45f) NeonOrange else NeonCyan,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { engineTemperature / 100f },
                    modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(8.dp)),
                    color = if (engineTemperature > 80f) LaserRed else if (engineTemperature > 45f) NeonOrange else NeonCyan,
                    trackColor = CyberObsidian
                )
            }

            // Big pump button
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(55.dp))
                    .background(NeonCyan)
                    .clickable {
                        engineTemperature = (engineTemperature - 8f).coerceAtLeast(0f)
                        clickStreak++
                        if (engineTemperature <= 15f) {
                            onTaskComplete(100)
                        }
                    }
                    .testTag("coolant_lever_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PUMP",
                        color = CyberObsidian,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "COOLANT",
                        color = CyberObsidian,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Text(
                text = "COOLING TARGET: REDUCE BELOW 15°C",
                color = if (engineTemperature <= 15f) NeonGreen else CyberWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
