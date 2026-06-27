package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue

@Composable
fun CardSwipeTask(
    onTaskComplete: (coinsEarned: Int) -> Unit,
    onClose: () -> Unit
) {
    // Game States
    var statusText by remember { mutableStateOf("SYSTEM IDLE - INSERT CARD") }
    var statusColor by remember { mutableStateOf(Color(0xFFFFEA00)) } // Amber Yellow
    
    // Position of the draggable card
    var cardOffsetX by remember { mutableStateOf(35f) } // Initial X position inside wallet
    var cardOffsetY by remember { mutableStateOf(165f) } // Initial Y position (draggable)

    // Swipe timing parameters
    var swipeStartTime by remember { mutableStateOf(0L) }
    var isSwipingAlongTrack by remember { mutableStateOf(false) }

    var isCompleted by remember { mutableStateOf(false) }
    var showCompletionBanner by remember { mutableStateOf(false) }

    fun resetGame() {
        statusText = "SYSTEM IDLE - INSERT CARD"
        statusColor = Color(0xFFFFEA00)
        cardOffsetX = 35f
        cardOffsetY = 165f
        swipeStartTime = 0L
        isSwipingAlongTrack = false
        isCompleted = false
        showCompletionBanner = false
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2640)),
        border = BorderStroke(2.dp, Color(0xFF334155)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .testTag("card_swipe_task_card")
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
                        text = "TASK: SWIPE CARD",
                        color = Color(0xFFFFCC00),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Drag card out of wallet and slide through the terminal.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }

                IconButton(onClick = { resetGame() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color.White)
                }
            }

            // Swipe Box Layout
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF334155)) // Spaceship slate plastic housing
                    .border(2.dp, Color(0xFF0F172A), RoundedCornerShape(8.dp))
            ) {
                // Main Terminal Graphics (the Reader Slot and LED screen)
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(isCompleted) {
                            if (isCompleted) return@pointerInput
                            detectDragGestures(
                                onDragStart = { startPos ->
                                    // Check if user is touching the card boundary
                                    val cardW = 110.dp.toPx()
                                    val cardH = 75.dp.toPx()
                                    val startX = cardOffsetX.dp.toPx()
                                    val startY = cardOffsetY.dp.toPx()

                                    if (startPos.x >= startX && startPos.x <= startX + cardW &&
                                        startPos.y >= startY && startPos.y <= startY + cardH) {
                                        
                                        // If card is lifted to the slot track height (~60dp to 120dp range)
                                        if (startPos.y < 120.dp.toPx()) {
                                            isSwipingAlongTrack = true
                                            swipeStartTime = System.currentTimeMillis()
                                        }
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    
                                    // Move card within bounded container limits
                                    val maxW = size.width
                                    val maxH = size.height
                                    val cardW = 110.dp.toPx()
                                    val cardH = 75.dp.toPx()

                                    val newX = (cardOffsetX.dp.toPx() + dragAmount.x)
                                        .coerceIn(0f, maxW - cardW)
                                    val newY = (cardOffsetY.dp.toPx() + dragAmount.y)
                                        .coerceIn(0f, maxH - cardH)

                                    cardOffsetX = newX.toDp().value
                                    cardOffsetY = newY.toDp().value

                                    // Check if they are swiping inside the reader slot (upper track)
                                    val trackTop = 60.dp.toPx()
                                    val trackBottom = 110.dp.toPx()
                                    if (newY >= trackTop && newY <= trackBottom) {
                                        if (!isSwipingAlongTrack) {
                                            isSwipingAlongTrack = true
                                            swipeStartTime = System.currentTimeMillis()
                                        }
                                    } else {
                                        isSwipingAlongTrack = false
                                    }
                                },
                                onDragEnd = {
                                    val cardW = 110.dp.toPx()
                                    val totalWidth = size.width
                                    
                                    // Did the card finish at the far right of the slot track?
                                    if (isSwipingAlongTrack && (cardOffsetX.dp.toPx() + cardW) >= totalWidth - 30f) {
                                        val duration = System.currentTimeMillis() - swipeStartTime
                                        
                                        if (duration < 190) {
                                            // Too fast!
                                            statusText = "BAD SWIPE: TOO FAST! TRY AGAIN."
                                            statusColor = Color(0xFFEF4444)
                                        } else if (duration > 680) {
                                            // Too slow!
                                            statusText = "BAD SWIPE: TOO SLOW! TRY AGAIN."
                                            statusColor = Color(0xFFEF4444)
                                        } else {
                                            // Success!
                                            statusText = "SYSTEM ACCEPTED. WELCOME CREWMATE!"
                                            statusColor = Color(0xFF22C55E)
                                            isCompleted = true
                                            showCompletionBanner = true
                                            onTaskComplete(150)
                                        }
                                    } else {
                                        statusText = "SWIPE FAILED: REMOVED TOO EARLY."
                                        statusColor = Color(0xFFEF4444)
                                    }
                                    isSwipingAlongTrack = false
                                }
                            )
                        }
                ) {
                    val w = size.width
                    val h = size.height

                    // 1. Terminal LED Screen Box
                    drawRoundRect(
                        color = Color(0xFF0F172A),
                        topLeft = Offset(20f, 20f),
                        size = Size(w - 40f, 110f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                    )
                    
                    // Screen internal grid overlay
                    for (i in 30.. (w - 30f).toInt() step 45) {
                        drawLine(
                            color = Color(0xFF1E293B).copy(alpha = 0.3f),
                            start = Offset(i.toFloat(), 20f),
                            end = Offset(i.toFloat(), 130f),
                            strokeWidth = 1f
                        )
                    }

                    // 2. Main Reader Slide Channel (Plastic groove)
                    drawRect(
                        color = Color(0xFF070A13),
                        topLeft = Offset(0f, 160f),
                        size = Size(w, 100f)
                    )
                    // Yellow/Orange arrows showing direction
                    val arrowColor = Color(0xFFFF9100)
                    for (x in 60..(w - 80f).toInt() step 80) {
                        val path = Path().apply {
                            moveTo(x.toFloat(), 200f)
                            lineTo(x.toFloat() + 15f, 210f)
                            lineTo(x.toFloat(), 220f)
                        }
                        drawPath(path, arrowColor, style = Stroke(width = 3f))
                    }

                    // 3. Wallet base pocket (draw at bottom half)
                    drawRoundRect(
                        color = Color(0xFF1E293B), // Leather wallet color
                        topLeft = Offset(20f, 350f),
                        size = Size(w - 40f, h - 330f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
                    )
                    // Stitching line
                    drawRoundRect(
                        color = Color(0xFF475569),
                        topLeft = Offset(25f, 355f),
                        size = Size(w - 50f, h - 340f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
                        style = Stroke(width = 2f)
                    )
                }

                // Status Screen overlay text (simulates high-tech led display)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp, start = 20.dp, end = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 2,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "SWIPE RANGE: 190ms - 680ms",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // 4. Draggable Plastic ID Keycard
                Box(
                    modifier = Modifier
                        .offset(x = cardOffsetX.dp, y = cardOffsetY.dp)
                        .size(width = 110.dp, height = 75.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF1F5F9))
                        .border(2.dp, Color(0xFF070A13), RoundedCornerShape(6.dp))
                        .padding(4.dp)
                ) {
                    // Swipe card decoration (looks like Among Us crewmate ID card)
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top blue banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF2563EB))
                        ) {
                            Text(
                                text = "MEMBER ID",
                                color = Color.White,
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Crewmate face photo icon
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF94A3B8)),
                                contentAlignment = Alignment.Center
                            ) {
                                // Draw a miniature Crewmate head visor inside photo
                                Box(
                                    modifier = Modifier
                                        .size(width = 16.dp, height = 12.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFEF4444)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(width = 8.dp, height = 5.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color(0xFF60A5FA))
                                    )
                                }
                            }

                            // ID signature text bars
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Box(modifier = Modifier.size(width = 50.dp, height = 3.dp).background(Color(0xFF475569)))
                                Box(modifier = Modifier.size(width = 35.dp, height = 3.dp).background(Color(0xFF475569)))
                                Box(modifier = Modifier.size(width = 45.dp, height = 3.dp).background(Color(0xFF94A3B8)))
                            }
                        }

                        // Bottom black magnetic reader stripe
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(Color(0xFF070A13))
                        )
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
                                text = "Security cleared! Access key logged. (+150 gold awarded)",
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
