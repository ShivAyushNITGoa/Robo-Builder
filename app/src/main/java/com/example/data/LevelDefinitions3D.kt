package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.engine.three_d.objects.Level3DObstacle

data class Level3DMap(
    val id: String,
    val name: String,
    val initialSpeed: Float,
    val obstacles: List<Level3DObstacle>
)

object LevelDefinitions3D {
    val levelsList = listOf(
        Level3DMap(
            id = "map_01",
            name = "ANDROMEDA PIPELINE",
            initialSpeed = 35f,
            obstacles = listOf(
                Level3DObstacle("ob_1", 0f, 0f, -100f, 30f, 30f, 30f, Color(0xFFFE0032), "solid"),
                Level3DObstacle("ob_2", -50f, 10f, -200f, 20f, 60f, 20f, Color(0xFF64748B), "solid"),
                Level3DObstacle("ob_3", 50f, -10f, -300f, 40f, 20f, 40f, Color(0xFFFF5F00), "hazard")
            )
        ),
        Level3DMap(
            id = "map_02",
            name = "NEBULA GATEWAY",
            initialSpeed = 50f,
            obstacles = listOf(
                Level3DObstacle("gate_1", 0f, -20f, -150f, 60f, 10f, 10f, Color(0xFF00F0FF), "checkpoint"),
                Level3DObstacle("meteor_1", -30f, 15f, -280f, 35f, 35f, 35f, Color(0xFF94A3B8), "hazard")
            )
        )
    )
}
