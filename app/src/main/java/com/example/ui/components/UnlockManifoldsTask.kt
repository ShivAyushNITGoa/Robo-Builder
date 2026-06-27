package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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

@Composable
fun UnlockManifoldsTask(
    onTaskComplete: (coinsEarned: Int) -> Unit,
    onClose: () -> Unit
) {
    // List of numbers 1..10
    var numbers by remember { mutableStateOf<List<Int>>(emptyList()) }
    // Next number we expect the user to click
    var nextExpectedNum by remember { mutableStateOf(1) }
    // Any error feedback state
    var isErrorActive by remember { mutableStateOf(false) }

    var isCompleted by remember { mutableStateOf(false) }
    var showCompletionBanner by remember { mutableStateOf(false) }

    fun initGame() {
        numbers = (1..10).shuffled()
        nextExpectedNum = 1
        isErrorActive = false
        isCompleted = false
        showCompletionBanner = false
    }

    LaunchedEffect(Unit) {
        initGame()
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2640)),
        border = BorderStroke(2.dp, Color(0xFF334155)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .testTag("unlock_manifolds_task_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Task Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TASK: UNLOCK MANIFOLDS",
                        color = Color(0xFFFFCC00),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Tap numbers 1 to 10 in exact sequential order.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }

                IconButton(onClick = { initGame() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color.White)
                }
            }

            // Numeric Grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A))
                    .border(2.dp, if (isErrorActive) Color(0xFFEF4444) else Color(0xFF334155), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                if (numbers.size == 10) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Two rows, 5 buttons each
                        for (row in 0..1) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (col in 0..4) {
                                    val index = row * 5 + col
                                    val num = numbers[index]
                                    val isClicked = num < nextExpectedNum

                                    // Render button
                                    val buttonBg = when {
                                        isErrorActive -> Color(0xFFEF4444).copy(alpha = 0.85f)
                                        isClicked -> Color(0xFF06B6D4) // Glowing Neon Cyan
                                        else -> Color(0xFF1E293B) // Dark default gray
                                    }

                                    val borderStrokeColor = when {
                                        isClicked -> Color(0xFF22D3EE)
                                        else -> Color(0xFF475569)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(width = 54.dp, height = 110.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(buttonBg)
                                            .border(2.dp, borderStrokeColor, RoundedCornerShape(4.dp))
                                            .clickable(enabled = !isCompleted && !isErrorActive) {
                                                if (num == nextExpectedNum) {
                                                    // Correct click!
                                                    nextExpectedNum++
                                                    if (nextExpectedNum > 10) {
                                                        // Complete!
                                                        isCompleted = true
                                                        showCompletionBanner = true
                                                        onTaskComplete(100)
                                                    }
                                                } else {
                                                    // Incorrect click! Reset!
                                                    isErrorActive = true
                                                    nextExpectedNum = 1
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = num.toString(),
                                            color = if (isClicked) Color.White else Color(0xFF94A3B8),
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // If error, trigger quick auto-reset of the error visual state
                if (isErrorActive) {
                    LaunchedEffect(isErrorActive) {
                        kotlinx.coroutines.delay(600)
                        isErrorActive = false
                    }
                }

                // Satisfying TASK COMPLETED giant alert overlay
                if (showCompletionBanner) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.82f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "TASK COMPLETED",
                                color = Color(0xFF22C55E),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            )
                            
                            Text(
                                text = "Shield manifolds aligned! Node unlocked. (+100 gold awarded)",
                                color = Color.White,
                                fontSize = 13.sp
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Button(
                                onClick = {
                                    showCompletionBanner = false
                                    onClose()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                            ) {
                                Text("CONTINUE", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Bottom control actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onClose) {
                    Text("CLOSE DECK", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
