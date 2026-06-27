package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
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

data class TutorialStep(
    val id: Int,
    val title: String,
    val message: String,
    val anchorLabel: String
)

@Composable
fun InteractiveTutorialSystem(
    modifier: Modifier = Modifier,
    steps: List<TutorialStep> = listOf(
        TutorialStep(1, "REACTOR INITIATION", "Access the Hyperdrive console to balance cooling rods and engage the warp engines.", "CORE_VALVES"),
        TutorialStep(2, "SHIELD REPAIR", "Harmonize the raw frequency of the deflector shields inside the oscilloscope minigame.", "OSCILLOSCOPE")
    ),
    onTutorialDismiss: () -> Unit
) {
    var currentStepIdx by remember { mutableStateOf(0) }
    val isVisible = remember { mutableStateOf(true) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    if (isVisible.value && steps.isNotEmpty()) {
        val currentStep = steps[currentStepIdx]

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberSlate)
                    .border(2.dp, NeonCyan, RoundedCornerShape(12.dp))
                    .padding(20.dp)
                    .testTag("tutorial_container"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Companion title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Companion Info",
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "A.I. COMPANION LOG: ${currentStep.title}",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Companion message
                Text(
                    text = currentStep.message,
                    color = CyberWhite,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )

                // Navigation Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STEP ${currentStepIdx + 1} OF ${steps.size}",
                        color = CyberGray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Button(
                        onClick = {
                            if (currentStepIdx < steps.size - 1) {
                                currentStepIdx++
                            } else {
                                isVisible.value = false
                                onTutorialDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        modifier = Modifier.testTag("tutorial_next_btn")
                    ) {
                        Text(
                            text = if (currentStepIdx == steps.size - 1) "DISMISS LOGS" else "NEXT INSTRUCTION",
                            color = CyberObsidian,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
