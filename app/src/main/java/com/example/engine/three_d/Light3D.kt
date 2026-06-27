package com.example.engine.three_d

import androidx.compose.ui.graphics.Color

data class Light3D(
    val type: LightType,
    val positionOrDirection: Point3D,
    val color: Color,
    val intensity: Float = 1.0f
) {
    enum class LightType {
        AMBIENT,
        DIRECTIONAL,
        POINT
    }

    companion object {
        fun createDefaultSun(): Light3D {
            return Light3D(
                type = LightType.DIRECTIONAL,
                positionOrDirection = Point3D(0.5f, -1.0f, -0.5f),
                color = Color.White,
                intensity = 0.8f
            )
        }

        fun createDefaultAmbient(): Light3D {
            return Light3D(
                type = LightType.AMBIENT,
                positionOrDirection = Point3D(0f, 0f, 0f),
                color = Color(0xFF1E293B),
                intensity = 0.3f
            )
        }

        fun createThrusterGlow(pos: Point3D): Light3D {
            return Light3D(
                type = LightType.POINT,
                positionOrDirection = pos,
                color = Color(0xFF00F0FF),
                intensity = 1.2f
            )
        }
    }
}
