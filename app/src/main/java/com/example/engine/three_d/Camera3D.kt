package com.example.engine.three_d

import kotlin.math.cos
import kotlin.math.sin

class Camera3D {
    var posX = 0f
    var posY = 0f
    var posZ = 350f

    var pitch = 0f // Rotation around X axis
    var yaw = 0f   // Rotation around Y axis
    var roll = 0f  // Rotation around Z axis

    var targetX = 0f
    var targetY = 0f
    var targetZ = 0f

    var zoomFactor = 1.0f

    fun getProjectedOffset(p: Point3D, rx: Float, ry: Float): androidx.compose.ui.geometry.Offset {
        // Rotate point relative to camera gaze
        var temp = Point3D(p.x - targetX, p.y - targetY, p.z - targetZ)
        temp = VectorMath.rotateY(temp, yaw)
        temp = VectorMath.rotateX(temp, pitch)

        val cameraDistance = posZ * zoomFactor
        val scale = cameraDistance / (cameraDistance + temp.z).coerceAtLeast(1.0f)
        return androidx.compose.ui.geometry.Offset(
            x = rx + temp.x * scale,
            y = ry + temp.y * scale
        )
    }
}
