package com.example.ui.components.three_d

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SimulationState
import kotlin.math.sin

@Composable
fun CockpitInteractiveHUD(
    speed: Float,
    progress: Float,
    hazardType: String,
    simulationState: SimulationState,
    modifier: Modifier = Modifier
) {
    // Cyberpunk/Neon Palette
    val neonBlue = Color(0xCC00F0FF)
    val neonMagenta = Color(0xCCFF007F)
    val neonLime = Color(0xCC00FF66)
    val neonYellow = Color(0xCCFFD600)
    val neonRed = Color(0xCCFF1744)

    // Breathing animations for holographic widgets
    val infiniteTransition = rememberInfiniteTransition(label = "hud_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hud_pulse_alpha"
    )

    val radarSweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_sweep"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("cockpit_interactive_hud")
    ) {
        // Overlay HUD graphic Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val centerX = w / 2f
            val centerY = h / 2f

            // 1. Crosshair targeting reticle in the exact center
            val reticleRadius = 45.dp.toPx()
            drawCircle(
                color = neonBlue.copy(alpha = 0.25f),
                radius = reticleRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = neonBlue,
                radius = 8.dp.toPx(),
                center = Offset(centerX, centerY),
                style = Stroke(width = 2.dp.toPx())
            )

            // Dynamic pitch indicators (climbing ladders)
            val pitchOffset = sin(System.currentTimeMillis() * 0.001f) * 15f
            val ladderY1 = centerY - 60.dp.toPx() + pitchOffset
            val ladderY2 = centerY + 60.dp.toPx() + pitchOffset

            // Left pitch bar
            drawLine(neonBlue, Offset(centerX - 100f, ladderY1), Offset(centerX - 40f, ladderY1), strokeWidth = 1.5.dp.toPx())
            drawLine(neonBlue, Offset(centerX - 100f, ladderY1), Offset(centerX - 100f, ladderY1 + 15f), strokeWidth = 1.5.dp.toPx())
            // Right pitch bar
            drawLine(neonBlue, Offset(centerX + 40f, ladderY1), Offset(centerX + 100f, ladderY1), strokeWidth = 1.5.dp.toPx())
            drawLine(neonBlue, Offset(centerX + 100f, ladderY1), Offset(centerX + 100f, ladderY1 + 15f), strokeWidth = 1.5.dp.toPx())

            // Lower ladder
            drawLine(neonBlue, Offset(centerX - 80f, ladderY2), Offset(centerX - 40f, ladderY2), strokeWidth = 1.5.dp.toPx())
            drawLine(neonBlue, Offset(centerX - 80f, ladderY2), Offset(centerX - 80f, ladderY2 - 15f), strokeWidth = 1.5.dp.toPx())
            drawLine(neonBlue, Offset(centerX + 40f, ladderY2), Offset(centerX + 80f, ladderY2), strokeWidth = 1.5.dp.toPx())
            drawLine(neonBlue, Offset(centerX + 80f, ladderY2), Offset(centerX + 80f, ladderY2 - 15f), strokeWidth = 1.5.dp.toPx())

            // Center cross lines
            drawLine(neonBlue, Offset(centerX - 20f, centerY), Offset(centerX - 5f, centerY), strokeWidth = 2.dp.toPx())
            drawLine(neonBlue, Offset(centerX + 5f, centerY), Offset(centerX + 20f, centerY), strokeWidth = 2.dp.toPx())
            drawLine(neonBlue, Offset(centerX, centerY - 20f), Offset(centerX, centerY - 5f), strokeWidth = 2.dp.toPx())
            drawLine(neonBlue, Offset(centerX, centerY + 5f), Offset(centerX, centerY + 20f), strokeWidth = 2.dp.toPx())

            // 2. Tactical peripheral brackets framing the viewport
            val frameOffset = 20.dp.toPx()
            val bracketPath = Path().apply {
                // Top-Left bracket
                moveTo(frameOffset, frameOffset + 40f)
                lineTo(frameOffset, frameOffset)
                lineTo(frameOffset + 40f, frameOffset)

                // Top-Right bracket
                moveTo(w - frameOffset, frameOffset + 40f)
                lineTo(w - frameOffset, frameOffset)
                lineTo(w - frameOffset - 40f, frameOffset)

                // Bottom-Left bracket
                moveTo(frameOffset, h - frameOffset - 40f)
                lineTo(frameOffset, h - frameOffset)
                lineTo(frameOffset + 40f, h - frameOffset)

                // Bottom-Right bracket
                moveTo(w - frameOffset, h - frameOffset - 40f)
                lineTo(w - frameOffset, h - frameOffset)
                lineTo(w - frameOffset - 40f, h - frameOffset)
            }
            drawPath(
                path = bracketPath,
                color = neonBlue.copy(alpha = 0.5f),
                style = Stroke(width = 2.dp.toPx())
            )

            // 3. Circular mini-radar sweep (Bottom Right)
            val radarRadius = 50.dp.toPx()
            val radarCX = w - radarRadius - 25.dp.toPx()
            val radarCY = h - radarRadius - 25.dp.toPx()

            drawCircle(
                color = neonLime.copy(alpha = 0.15f),
                radius = radarRadius,
                center = Offset(radarCX, radarCY)
            )
            drawCircle(
                color = neonLime.copy(alpha = 0.4f),
                radius = radarRadius,
                center = Offset(radarCX, radarCY),
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = neonLime.copy(alpha = 0.3f),
                radius = radarRadius * 0.5f,
                center = Offset(radarCX, radarCY),
                style = Stroke(width = 0.5.dp.toPx())
            )

            // Sweep line
            val sweepX = radarCX + radarRadius * kotlin.math.cos(Math.toRadians(radarSweepAngle.toDouble())).toFloat()
            val sweepY = radarCY + radarRadius * sin(Math.toRadians(radarSweepAngle.toDouble())).toFloat()
            drawLine(
                color = neonLime.copy(alpha = pulseAlpha),
                start = Offset(radarCX, radarCY),
                end = Offset(sweepX, sweepY),
                strokeWidth = 2.dp.toPx()
            )

            // Draw obstacle target dot on radar if hazard is present
            if (hazardType != "none") {
                val dotZ = (1f - progress) * radarRadius
                val dotY = radarCY - dotZ
                drawCircle(
                    color = neonMagenta,
                    radius = 4.dp.toPx(),
                    center = Offset(radarCX, dotY)
                )
            }
        }

        // Holographic indicators panel (Left Overlay)
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp)
                .width(100.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .border(1.dp, neonBlue.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "VELOCITY",
                color = neonBlue,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            
            // Vertical bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color.DarkGray.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(speed.coerceIn(0f, 2f) / 2f)
                        .background(Brush.verticalGradient(listOf(neonMagenta, neonBlue)))
                        .align(Alignment.BottomCenter)
                )
            }

            Text(
                text = String.format("%.1f M/S", speed * 12.5f),
                color = CyberWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        // Sector progress & hazard readouts (Right Overlay)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp)
                .width(110.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .border(1.dp, neonBlue.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "SECTOR DIST",
                color = neonBlue,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            // Distance meter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = neonLime,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "OK",
                    color = neonLime.copy(alpha = pulseAlpha),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Hazard warning
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "HAZARD FLUX",
                color = if (hazardType != "none") neonMagenta else Color.Gray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (hazardType != "none") neonMagenta.copy(alpha = 0.15f) else Color.DarkGray.copy(alpha = 0.2f))
                    .border(1.dp, if (hazardType != "none") neonMagenta else Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = hazardType.uppercase(),
                    color = if (hazardType != "none") neonMagenta else Color.Gray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Top HUD horizontal bar (Status readouts)
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .fillMaxWidth(0.85f)
                .height(30.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .border(0.5.dp, neonBlue.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val statusColor = when (simulationState) {
                is SimulationState.Success -> neonLime
                is SimulationState.Failure -> neonRed
                is SimulationState.Running -> neonBlue
                else -> neonYellow
            }

            val statusText = when (simulationState) {
                is SimulationState.Success -> "SYSTEM SECURE // COMPLETE"
                is SimulationState.Failure -> "CRITICAL BREACH // COLLAPSE"
                is SimulationState.Running -> "LIVE TRANSIT FLIGHT MODE"
                else -> "CONSOLE STANDBY // READY"
            }

            Text(
                text = statusText,
                color = statusColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 2.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (simulationState is SimulationState.Running) neonLime else Color.DarkGray)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (hazardType != "none") neonMagenta else Color.DarkGray)
                )
            }
        }
    }
}
