package com.example.engine.three_d

import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin

data class Point3D(val x: Float, val y: Float, val z: Float)

object VectorMath {
    fun rotateX(p: Point3D, angleRad: Float): Point3D {
        val cos = cos(angleRad)
        val sin = sin(angleRad)
        return Point3D(
            p.x,
            p.y * cos - p.z * sin,
            p.y * sin + p.z * cos
        )
    }

    fun rotateY(p: Point3D, angleRad: Float): Point3D {
        val cos = cos(angleRad)
        val sin = sin(angleRad)
        return Point3D(
            p.x * cos + p.z * sin,
            p.y,
            -p.x * sin + p.z * cos
        )
    }

    fun rotateZ(p: Point3D, angleRad: Float): Point3D {
        val cos = cos(angleRad)
        val sin = sin(angleRad)
        return Point3D(
            p.x * cos - p.y * sin,
            p.x * sin + p.y * cos,
            p.z
        )
    }

    fun project(p: Point3D, cameraDist: Float, rx: Float, ry: Float): Offset {
        val scale = cameraDist / (cameraDist + p.z)
        return Offset(
            x = rx + p.x * scale,
            y = ry + p.y * scale
        )
    }
}
