package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun GameHUDOverlay3D(
    modifier: Modifier = Modifier,
    speed: Float = 45f,
    energy: Float = 0.88f,
    shieldIntegrity: Float = 0.95f,
    levelName: String = "SECTOR_DELTA_BLUE"
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CyberSlate.copy(alpha = 0.75f))
                .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Speed Column
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Speed, contentDescription = "Velocity", tint = NeonCyan)
                Column {
                    Text("VELOCITY", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    Text("${speed.toInt()} KM/S", color = NeonCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            // Central Mission Sector Badge
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(levelName, color = NeonMagenta, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                Text("AUTO_CHASE_ACTIVE", color = Color.White, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
            }

            // Power levels
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.ElectricBolt, contentDescription = "Shield", tint = NeonYellow)
                Column {
                    Text("REACTOR POW", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    Text("${(energy * 100).toInt()}%", color = NeonYellow, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
