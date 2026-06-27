package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import com.example.engine.three_d.objects.Level3DObstacle
import com.example.ui.components.three_d.Level3DViewport
import com.example.ui.components.three_d.fx.ParticleSystem3D
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomLevelEditor3D(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {
    val obstacles = remember {
        mutableStateListOf(
            Level3DObstacle("ob_01", 0f, 0f, -80f, 25f, 25f, 25f, Color(0xFFFF007F)),
            Level3DObstacle("ob_02", -40f, 15f, -180f, 30f, 30f, 30f, Color(0xFF00F0FF))
        )
    }

    var selectedObstacleIndex by remember { mutableStateOf(0) }
    val particles = remember { ParticleSystem3D() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CyberObsidian,
        topBar = {
            TopAppBar(
                title = { Text("3D LEVEL BLUEPRINT", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = CyberWhite) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CyberWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberSlate)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Live 3D Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Level3DViewport(
                    robotX = 0f, robotY = 0f, robotZ = 0f,
                    legsType = "Standard Biped",
                    leftArmType = "Heavy Drill",
                    rightArmType = "Plasma Blaster",
                    utilityType = "Wings",
                    obstacles = obstacles,
                    particles = particles
                )
            }

            // Controls Drawer Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                colors = CardDefaults.cardColors(containerColor = CyberSlate)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("ACTIVE OBSTACLE ADJUSTER", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)

                    if (obstacles.isNotEmpty() && selectedObstacleIndex in obstacles.indices) {
                        val activeOb = obstacles[selectedObstacleIndex]

                        // Adjust X Slider
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("AXIS_X (${activeOb.x.toInt()})", color = Color.White, modifier = Modifier.width(90.dp), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Slider(
                                value = activeOb.x,
                                onValueChange = { activeOb.x = it; obstacles[selectedObstacleIndex] = activeOb.copy(x = it) },
                                valueRange = -80f..80f,
                                modifier = Modifier.weight(1f).testTag("slide_axis_x")
                            )
                        }

                        // Adjust Y Slider
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("AXIS_Y (${activeOb.y.toInt()})", color = Color.White, modifier = Modifier.width(90.dp), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Slider(
                                value = activeOb.y,
                                onValueChange = { activeOb.y = it; obstacles[selectedObstacleIndex] = activeOb.copy(y = it) },
                                valueRange = -50f..50f,
                                modifier = Modifier.weight(1f).testTag("slide_axis_y")
                            )
                        }

                        // Adjust Z Slider
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("AXIS_Z (${activeOb.z.toInt()})", color = Color.White, modifier = Modifier.width(90.dp), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            Slider(
                                value = activeOb.z,
                                onValueChange = { activeOb.z = it; obstacles[selectedObstacleIndex] = activeOb.copy(z = it) },
                                valueRange = -300f..-30f,
                                modifier = Modifier.weight(1f).testTag("slide_axis_z")
                            )
                        }
                    }

                    // Bottom buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val idx = obstacles.size + 1
                                obstacles.add(Level3DObstacle("ob_$idx", 0f, 0f, -100f, 20f, 20f, 20f, Color.Green))
                                selectedObstacleIndex = obstacles.size - 1
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            modifier = Modifier.weight(1f).testTag("add_3d_obstacle_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                            Spacer(Modifier.width(4.dp))
                            Text("ADD OBSTACLE", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }

                        Button(
                            onClick = {
                                if (obstacles.isNotEmpty()) {
                                    obstacles.removeAt(selectedObstacleIndex)
                                    selectedObstacleIndex = (obstacles.size - 1).coerceAtLeast(0)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LaserRed),
                            modifier = Modifier.weight(1f).testTag("delete_3d_obstacle_btn")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                            Spacer(Modifier.width(4.dp))
                            Text("DELETE", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}
