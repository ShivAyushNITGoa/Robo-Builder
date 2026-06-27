package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GameViewModel
import com.example.ui.Screen
import com.example.ui.components.*

enum class ActiveTaskType {
    NONE,
    WIRES,
    CARD_SWIPE,
    MANIFOLDS
}

data class TaskItem(
    val type: ActiveTaskType,
    val title: String,
    val location: String,
    val description: String,
    val goldValue: Int,
    var isFinished: Boolean = false
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SpaceshipTasksScreen(viewModel: GameViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()

    // Task definitions
    var tasksList by remember {
        mutableStateOf(
            listOf(
                TaskItem(
                    type = ActiveTaskType.WIRES,
                    title = "Fix Wiring",
                    location = "Security Deck",
                    description = "Re-route loose copper wires back into correct terminal sockets.",
                    goldValue = 120
                ),
                TaskItem(
                    type = ActiveTaskType.CARD_SWIPE,
                    title = "Swipe ID Card",
                    location = "Admin Office",
                    description = "Register security clearance by swiping ID card at the correct speed.",
                    goldValue = 150
                ),
                TaskItem(
                    type = ActiveTaskType.MANIFOLDS,
                    title = "Unlock Manifolds",
                    location = "Shield Room",
                    description = "Sequence numerical fuel injectors in strict order 1 to 10.",
                    goldValue = 100
                )
            )
        )
    }

    // Active screen modal task
    var selectedTask by remember { mutableStateOf(ActiveTaskType.NONE) }

    // Live calculations
    val totalTasks = tasksList.size
    val finishedTasks = tasksList.count { it.isFinished }
    val progressPercent = if (totalTasks > 0) finishedTasks.toFloat() / totalTasks else 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("spaceship_tasks_screen")
    ) {
        // SpaceStarfieldBackground is active globally under everything

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Header Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.onScreenChanged(Screen.Menu) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Text(
                    text = "SPACESHIP CREWMATE TASKS",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )

                // Gold Coins Readout
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "Gold",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${userProfile?.coins ?: 0}",
                            color = Color(0xFFFFD700),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Total Task Progress Bar (Among Us iconic top progress bar)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                border = BorderStroke(1.5.dp, Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL TASK COMPLETION",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "$finishedTasks / $totalTasks COMPLETED",
                            color = if (progressPercent == 1f) Color(0xFF22C55E) else Color(0xFFFFEA00),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Green thick Among Us styled progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(Color(0xFF1E293B))
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progressPercent)
                                .clip(RoundedCornerShape(7.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF15803D), Color(0xFF22C55E))
                                    )
                                )
                        )
                    }
                }
            }

            // Task list Directory
            Text(
                text = "PENDING SHIP TASKS",
                color = Color(0xFFFFEA00),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(tasksList) { task ->
                    val borderCol = if (task.isFinished) Color(0xFF22C55E) else Color(0xFF334155)
                    val bgCol = if (task.isFinished) Color(0xFF152A21).copy(alpha = 0.85f) else Color(0xFF121B2F).copy(alpha = 0.85f)

                    Card(
                        onClick = {
                            if (!task.isFinished) {
                                selectedTask = task.type
                            }
                        },
                        colors = CardDefaults.cardColors(containerColor = bgCol),
                        border = BorderStroke(1.5.dp, borderCol),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Left Icon status
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (task.isFinished) Color(0xFF22C55E).copy(alpha = 0.15f) else Color(0xFFFFEA00).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (task.isFinished) Icons.Default.CheckCircle else Icons.Default.BuildCircle,
                                    contentDescription = "Status",
                                    tint = if (task.isFinished) Color(0xFF22C55E) else Color(0xFFFFEA00),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Center descriptions
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = task.title,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.Black.copy(alpha = 0.3f))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = task.location.uppercase(),
                                            color = Color(0xFF60A5FA),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = task.description,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }

                            // Right Action state
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (task.isFinished) {
                                    Text(
                                        text = "DONE",
                                        color = Color(0xFF22C55E),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                } else {
                                    Text(
                                        text = "+${task.goldValue}g",
                                        color = Color(0xFFFFD700),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "START",
                                        color = Color(0xFF60A5FA),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Animated full-screen minigame overlay when a task is open!
        AnimatedVisibility(
            visible = selectedTask != ActiveTaskType.NONE,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                when (selectedTask) {
                    ActiveTaskType.WIRES -> {
                        WireFixingTask(
                            onTaskComplete = { coins ->
                                // Award gold
                                viewModel.awardCustomCoins(coins)
                                // Mark finished
                                tasksList = tasksList.map {
                                    if (it.type == ActiveTaskType.WIRES) it.copy(isFinished = true) else it
                                }
                            },
                            onClose = { selectedTask = ActiveTaskType.NONE }
                        )
                    }
                    ActiveTaskType.CARD_SWIPE -> {
                        CardSwipeTask(
                            onTaskComplete = { coins ->
                                viewModel.awardCustomCoins(coins)
                                tasksList = tasksList.map {
                                    if (it.type == ActiveTaskType.CARD_SWIPE) it.copy(isFinished = true) else it
                                }
                            },
                            onClose = { selectedTask = ActiveTaskType.NONE }
                        )
                    }
                    ActiveTaskType.MANIFOLDS -> {
                        UnlockManifoldsTask(
                            onTaskComplete = { coins ->
                                viewModel.awardCustomCoins(coins)
                                tasksList = tasksList.map {
                                    if (it.type == ActiveTaskType.MANIFOLDS) it.copy(isFinished = true) else it
                                }
                            },
                            onClose = { selectedTask = ActiveTaskType.NONE }
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}
