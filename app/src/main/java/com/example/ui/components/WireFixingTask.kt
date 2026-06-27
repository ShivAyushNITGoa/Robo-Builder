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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.hypot

// Wire definition
data class WireNode(
    val index: Int,
    val color: Color,
    val colorName: String,
    val initialYPercent: Float // 0.15f, 0.35f, 0.55f, 0.75f
)

@Composable
fun WireFixingTask(
    onTaskComplete: (coinsEarned: Int) -> Unit,
    onClose: () -> Unit
) {
    // Shuffled wire color arrangements
    val availableColors = listOf(
        Color(0xFFFF1744) to "Red",
        Color(0xFF2979FF) to "Blue",
        Color(0xFFFFEA00) to "Yellow",
        Color(0xFFE040FB) to "Magenta"
    )

    var leftNodes by remember { mutableStateOf<List<WireNode>>(emptyList()) }
    var rightNodes by remember { mutableStateOf<List<WireNode>>(emptyList()) }
    
    // State of connections: maps left index (0..3) to connected right index (0..3)
    var connections by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    
    // Active dragging state
    var activeDragLeftIndex by remember { mutableStateOf<Int?>(null) }
    var currentDragPosition by remember { mutableStateOf(Offset.Zero) }

    var isCompleted by remember { mutableStateOf(false) }
    var showCompletionBanner by remember { mutableStateOf(false) }

    // Init or restart game
    fun initGame() {
        val leftShuffled = availableColors.shuffled()
        leftNodes = leftShuffled.mapIndexed { idx, pair ->
            WireNode(idx, pair.first, pair.second, 0.18f + idx * 0.22f)
        }
        val rightShuffled = availableColors.shuffled()
        rightNodes = rightShuffled.mapIndexed { idx, pair ->
            WireNode(idx, pair.first, pair.second, 0.18f + idx * 0.22f)
        }
        connections = emptyMap()
        activeDragLeftIndex = null
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
            .testTag("wire_fixing_task_card")
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
                        text = "TASK: FIX WIRING",
                        color = Color(0xFFFFCC00),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Connect same-colored wire terminals by dragging.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { initGame() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color.White)
                    }
                }
            }

            // Interactive Wiring Sandbox
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A))
                    .border(2.dp, Color(0xFF0F172A), RoundedCornerShape(8.dp))
            ) {
                // Background metal detailing (vents, seams)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw seam lines
                    drawLine(Color(0xFF1E293B), Offset(0f, 150.dp.toPx()), Offset(size.width, 150.dp.toPx()), strokeWidth = 3f)
                    drawLine(Color(0xFF1E293B), Offset(size.width * 0.25f, 0f), Offset(size.width * 0.25f, size.height), strokeWidth = 2f)
                    drawLine(Color(0xFF1E293B), Offset(size.width * 0.75f, 0f), Offset(size.width * 0.75f, size.height), strokeWidth = 2f)
                }

                if (leftNodes.isNotEmpty() && rightNodes.isNotEmpty()) {
                    val density = LocalDensity.current
                    
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(leftNodes, rightNodes, connections, isCompleted) {
                                if (isCompleted) return@pointerInput
                                detectDragGestures(
                                    onDragStart = { startOffset ->
                                        // Find if tap is near a left node wire output
                                        val leftX = 40.dp.toPx()
                                        leftNodes.forEach { node ->
                                            val nodeY = node.initialYPercent * size.height
                                            val dist = hypot(startOffset.x - leftX, startOffset.y - nodeY)
                                            if (dist < 40.dp.toPx() && !connections.containsKey(node.index)) {
                                                activeDragLeftIndex = node.index
                                                currentDragPosition = startOffset
                                            }
                                        }
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        if (activeDragLeftIndex != null) {
                                            currentDragPosition += dragAmount
                                        }
                                    },
                                    onDragEnd = {
                                        val leftIdx = activeDragLeftIndex
                                        if (leftIdx != null) {
                                            // Check if near any right node terminal
                                            val rightX = size.width - 40.dp.toPx()
                                            var snapped = false
                                            
                                            rightNodes.forEach { rightNode ->
                                                val rightY = rightNode.initialYPercent * size.height
                                                val dist = hypot(currentDragPosition.x - rightX, currentDragPosition.y - rightY)
                                                
                                                // If within snap radius and colors match!
                                                if (dist < 38.dp.toPx()) {
                                                    val leftNode = leftNodes.find { it.index == leftIdx }
                                                    if (leftNode != null && leftNode.colorName == rightNode.colorName) {
                                                        // Ensure this right node is not already connected
                                                        if (!connections.values.contains(rightNode.index)) {
                                                            connections = connections + (leftIdx to rightNode.index)
                                                            snapped = true
                                                            
                                                            // Check if completed all 4 connections
                                                            if (connections.size == 4) {
                                                                isCompleted = true
                                                                showCompletionBanner = true
                                                                onTaskComplete(120) // Award 120 coins
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            activeDragLeftIndex = null
                                        }
                                    },
                                    onDragCancel = {
                                        activeDragLeftIndex = null
                                    }
                                )
                            }
                    ) {
                        val w = size.width
                        val h = size.height
                        
                        val leftTerminalX = 40.dp.toPx()
                        val rightTerminalX = w - 40.dp.toPx()

                        // 1. Draw Connected Wires
                        connections.forEach { (leftIdx, rightIdx) ->
                            val leftNode = leftNodes.find { it.index == leftIdx }!!
                            val rightNode = rightNodes.find { it.index == rightIdx }!!
                            
                            val startY = leftNode.initialYPercent * h
                            val endY = rightNode.initialYPercent * h
                            
                            // Beautiful natural hanging wire curve (Bézier spline)
                            val path = Path().apply {
                                moveTo(leftTerminalX, startY)
                                cubicTo(
                                    x1 = leftTerminalX + w * 0.35f, y1 = startY,
                                    x2 = rightTerminalX - w * 0.35f, y2 = endY,
                                    x3 = rightTerminalX, y3 = endY
                                )
                            }
                            
                            // Thick bold outer black cartoon casing
                            drawPath(
                                path = path,
                                color = Color(0xFF070A13),
                                style = Stroke(width = 16f, cap = StrokeCap.Round)
                            )
                            // Colorful wire core
                            drawPath(
                                path = path,
                                color = leftNode.color,
                                style = Stroke(width = 10f, cap = StrokeCap.Round)
                            )
                            // Inner glowing core streak
                            drawPath(
                                path = path,
                                color = Color.White.copy(alpha = 0.35f),
                                style = Stroke(width = 3f, cap = StrokeCap.Round)
                            )
                        }

                        // 2. Draw Active Dragging Wire
                        val activeIdx = activeDragLeftIndex
                        if (activeIdx != null) {
                            val activeNode = leftNodes.find { it.index == activeIdx }!!
                            val startY = activeNode.initialYPercent * h
                            
                            val path = Path().apply {
                                moveTo(leftTerminalX, startY)
                                cubicTo(
                                    x1 = leftTerminalX + (currentDragPosition.x - leftTerminalX) * 0.4f, y1 = startY,
                                    x2 = currentDragPosition.x - (currentDragPosition.x - leftTerminalX) * 0.2f, y2 = currentDragPosition.y,
                                    x3 = currentDragPosition.x, y3 = currentDragPosition.y
                                )
                            }

                            // Outline
                            drawPath(
                                path = path,
                                color = Color(0xFF070A13),
                                style = Stroke(width = 16f, cap = StrokeCap.Round)
                            )
                            // Body wire
                            drawPath(
                                path = path,
                                color = activeNode.color,
                                style = Stroke(width = 10f, cap = StrokeCap.Round)
                            )
                        }

                        // 3. Draw Left Terminal Junctions
                        leftNodes.forEach { node ->
                            val nY = node.initialYPercent * h
                            val isConnected = connections.containsKey(node.index)

                            // Base black box socket
                            drawRect(
                                color = Color(0xFF070A13),
                                topLeft = Offset(0f, nY - 24f),
                                size = Size(leftTerminalX, 48f)
                            )
                            // Steel bracket
                            drawRect(
                                color = Color(0xFF475569),
                                topLeft = Offset(4f, nY - 18f),
                                size = Size(leftTerminalX - 10f, 36f)
                            )
                            // Colorful terminal head
                            drawRect(
                                color = node.color,
                                topLeft = Offset(leftTerminalX - 14f, nY - 12f),
                                size = Size(14f, 24f)
                            )
                            // Outline terminal head
                            drawRect(
                                color = Color(0xFF070A13),
                                topLeft = Offset(leftTerminalX - 14f, nY - 12f),
                                size = Size(14f, 24f),
                                style = Stroke(width = 3f)
                            )

                            // Status LED Indicator (Green if connected, Red if loose)
                            val ledColor = if (isConnected) Color(0xFF22C55E) else Color(0xFFEF4444)
                            drawCircle(
                                color = Color(0xFF070A13),
                                radius = 7f,
                                center = Offset(20f, nY - 32f)
                            )
                            drawCircle(
                                color = ledColor,
                                radius = 4.5f,
                                center = Offset(20f, nY - 32f)
                            )
                            // Small LED glare
                            drawCircle(
                                color = Color.White,
                                radius = 1.2f,
                                center = Offset(19.2f, nY - 32.8f)
                            )
                        }

                        // 4. Draw Right Terminal Junctions
                        rightNodes.forEach { node ->
                            val nY = node.initialYPercent * h
                            val isConnected = connections.values.contains(node.index)

                            // Base black box socket
                            drawRect(
                                color = Color(0xFF070A13),
                                topLeft = Offset(rightTerminalX, nY - 24f),
                                size = Size(leftTerminalX, 48f)
                            )
                            // Steel bracket
                            drawRect(
                                color = Color(0xFF475569),
                                topLeft = Offset(rightTerminalX + 6f, nY - 18f),
                                size = Size(leftTerminalX - 10f, 36f)
                            )
                            // Colorful terminal head facing left
                            drawRect(
                                color = node.color,
                                topLeft = Offset(rightTerminalX, nY - 12f),
                                size = Size(14f, 24f)
                            )
                            // Outline
                            drawRect(
                                color = Color(0xFF070A13),
                                topLeft = Offset(rightTerminalX, nY - 12f),
                                size = Size(14f, 24f),
                                style = Stroke(width = 3f)
                            )

                            // LED Indicator
                            val ledColor = if (isConnected) Color(0xFF22C55E) else Color(0xFFEF4444)
                            drawCircle(
                                color = Color(0xFF070A13),
                                radius = 7f,
                                center = Offset(w - 20f, nY - 32f)
                            )
                            drawCircle(
                                color = ledColor,
                                radius = 4.5f,
                                center = Offset(w - 20f, nY - 32f)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 1.2f,
                                center = Offset(w - 20.8f, nY - 32.8f)
                            )
                        }
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
                                text = "Power restored to security arrays! (+120 gold awarded)",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.SansSerif
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
