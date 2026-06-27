package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun HyperdriveTask(
    modifier: Modifier = Modifier,
    onTaskComplete: (Int) -> Unit // score
) {
    var coolingRod1 by remember { mutableStateOf(0.2f) }
    var coolingRod2 by remember { mutableStateOf(0.8f) }
    var ignitionPulled by remember { mutableStateOf(false) }

    val coreStability = remember(coolingRod1, coolingRod2) {
        val diff1 = kotlin.math.abs(coolingRod1 - 0.5f)
        val diff2 = kotlin.math.abs(coolingRod2 - 0.5f)
        (1.0f - (diff1 + diff2)).coerceIn(0f, 1f)
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
                .border(2.dp, NeonCyan, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Title Banner
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.OfflineBolt,
                    contentDescription = "Power Icon",
                    tint = NeonYellow,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "HYPERDRIVE IGNITION",
                    color = CyberWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = "Align the cooling rods to 50% to balance the reactor cores.",
                color = CyberGray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )

            // Stability readout
            LinearProgressIndicator(
                progress = { coreStability },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = if (coreStability > 0.85f) NeonGreen else if (coreStability > 0.5f) NeonYellow else LaserRed,
                trackColor = CyberObsidian
            )

            Text(
                text = "CORE STABILITY: ${(coreStability * 100).toInt()}%",
                color = if (coreStability > 0.85f) NeonGreen else if (coreStability > 0.5f) NeonYellow else LaserRed,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            // Dynamic core container
            Row(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rod 1 slider
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ROD_01", color = NeonCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = coolingRod1,
                        onValueChange = { coolingRod1 = it },
                        modifier = Modifier.height(120.dp).testTag("rod_slider_1"),
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = CyberObsidian
                        )
                    )
                    Text("${(coolingRod1 * 100).toInt()}%", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }

                // Rod 2 slider
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ROD_02", color = NeonCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Spacer(Modifier.height(8.dp))
                    Slider(
                        value = coolingRod2,
                        onValueChange = { coolingRod2 = it },
                        modifier = Modifier.height(120.dp).testTag("rod_slider_2"),
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = CyberObsidian
                        )
                    )
                    Text("${(coolingRod2 * 100).toInt()}%", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }

            // Big red master lever
            Button(
                onClick = {
                    if (coreStability > 0.85f) {
                        ignitionPulled = true
                        onTaskComplete((coreStability * 100).toInt())
                    }
                },
                enabled = coreStability > 0.85f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("hyperdrive_master_lever"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LaserRed,
                    disabledContainerColor = CyberGray.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (ignitionPulled) "WARP ACTIVE" else "PULL IGNITION LEVER",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
