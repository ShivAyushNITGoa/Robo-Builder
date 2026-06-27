package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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

@Composable
fun CockpitControls(
    thrusterPower: Float,
    onThrusterChange: (Float) -> Unit,
    shieldFrequency: Float,
    onShieldChange: (Float) -> Unit,
    laserIntensity: Float,
    onLaserChange: (Float) -> Unit,
    safetyBreaker: Boolean,
    onBreakerToggle: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F1524))
            .border(1.dp, CyberBlue.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Circuit Breaker Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF070A13), RoundedCornerShape(8.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MAIN OVERLOAD CIRCUIT BREAKER",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (safetyBreaker) "Breaker Connected - System Protected" else "DANGER: Core Meltdown Override Active",
                    color = if (safetyBreaker) CyberLime else CyberRed,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Switch(
                checked = safetyBreaker,
                onCheckedChange = onBreakerToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CyberLime,
                    checkedTrackColor = CyberLime.copy(alpha = 0.3f),
                    uncheckedThumbColor = CyberRed,
                    uncheckedTrackColor = CyberRed.copy(alpha = 0.3f)
                )
            )
        }

        // Thruster Power Slider
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "HYDRAULIC THRUSTER PRESSURE",
                    color = CyberSteel,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${thrusterPower.toInt()}%",
                    color = CyberBlue,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Slider(
                value = thrusterPower,
                onValueChange = onThrusterChange,
                valueRange = 0f..100f,
                enabled = safetyBreaker,
                colors = SliderDefaults.colors(
                    thumbColor = CyberBlue,
                    activeTrackColor = CyberBlue,
                    inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                )
            )
        }

        // Shield Frequency Slider
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "EM SHIELD HARMONIC FREQUENCY",
                    color = CyberSteel,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${"%.1f".format(shieldFrequency)} GHz",
                    color = CyberOrange,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Slider(
                value = shieldFrequency,
                onValueChange = onShieldChange,
                valueRange = 1.0f..12.0f,
                enabled = safetyBreaker,
                colors = SliderDefaults.colors(
                    thumbColor = CyberOrange,
                    activeTrackColor = CyberOrange,
                    inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                )
            )
        }

        // Laser Intensity Slider
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "QUANTUM COHERENCE INTENSITY",
                    color = CyberSteel,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${laserIntensity.toInt()} MW",
                    color = CyberRed,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Slider(
                value = laserIntensity,
                onValueChange = onLaserChange,
                valueRange = 0f..250f,
                enabled = safetyBreaker,
                colors = SliderDefaults.colors(
                    thumbColor = CyberRed,
                    activeTrackColor = CyberRed,
                    inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                )
            )
        }
    }
}
