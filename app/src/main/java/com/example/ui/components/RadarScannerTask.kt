package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
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

@Composable
fun RadarScannerTask(
    modifier: Modifier = Modifier,
    onTaskComplete: (Int) -> Unit
) {
    // Crosshair coordinates
    var crosshairX by remember { mutableStateOf(100f) }
    var crosshairY by remember { mutableStateOf(100f) }

    // Target coordinates (Fixed or slowly drift)
    val targetX = 300f
    val targetY = 180f

    val lockOnProgress = remember(crosshairX, crosshairY) {
        val dx = crosshairX - targetX
        val dy = crosshairY - targetY
        val dist = kotlin.math.sqrt(dx * dx + dy * dy)
        (1.0f - (dist / 200f)).coerceIn(0f, 1f)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.CellTower, contentDescription = "Radar", tint = NeonCyan)
                Text(
                    text = "RADAR FREQUENCY LOCK-ON",
                    color = CyberWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = "DRAG CROSSHAIRS UNTIL IT LOCKS ONTO THE BEACON SIGNAL",
                color = CyberGray,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            // Radar display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
                    .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            crosshairX = (crosshairX + dragAmount.x).coerceIn(0f, size.width.toFloat())
                            crosshairY = (crosshairY + dragAmount.y).coerceIn(0f, size.height.toFloat())
                        }
                    }
                    .testTag("radar_draggable_grid")
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw alignment grid lines
                    val cols = 8
                    val rows = 6
                    val colW = size.width / cols
                    val rowH = size.height / rows

                    for (i in 1 until cols) {
                        drawLine(
                            color = NeonCyan.copy(alpha = 0.1f),
                            start = Offset(i * colW, 0f),
                            end = Offset(i * colW, size.height),
                            strokeWidth = 1f
                        )
                    }
                    for (i in 1 until rows) {
                        drawLine(
                            color = NeonCyan.copy(alpha = 0.1f),
                            start = Offset(0f, i * rowH),
                            end = Offset(size.width, i * rowH),
                            strokeWidth = 1f
                        )
                    }

                    // Target (beacon)
                    drawCircle(
                        color = NeonMagenta.copy(alpha = 0.2f),
                        radius = 40f,
                        center = Offset(targetX, targetY)
                    )
                    drawCircle(
                        color = NeonMagenta,
                        radius = 8f,
                        center = Offset(targetX, targetY)
                    )

                    // User crosshair
                    drawLine(
                        color = NeonCyan,
                        start = Offset(crosshairX - 30f, crosshairY),
                        end = Offset(crosshairX + 30f, crosshairY),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = NeonCyan,
                        start = Offset(crosshairX, crosshairY - 30f),
                        end = Offset(crosshairX, crosshairY + 30f),
                        strokeWidth = 2f
                    )
                    drawCircle(
                        color = NeonCyan,
                        radius = 16f,
                        center = Offset(crosshairX, crosshairY),
                        style = Stroke(width = 2f)
                    )
                }
            }

            // Lock progress indicator
            LinearProgressIndicator(
                progress = { lockOnProgress },
                modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                color = if (lockOnProgress > 0.9f) NeonGreen else NeonCyan,
                trackColor = CyberObsidian
            )

            Text(
                text = "BEACON COHERENCE: ${(lockOnProgress * 100).toInt()}%",
                color = if (lockOnProgress > 0.9f) NeonGreen else NeonCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Button(
                onClick = { onTaskComplete((lockOnProgress * 100).toInt()) },
                enabled = lockOnProgress > 0.9f,
                modifier = Modifier.fillMaxWidth().testTag("radar_lock_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Text("DECRYPT RADIO BEACON", color = CyberObsidian, fontWeight = FontWeight.Bold)
            }
        }
    }
}
