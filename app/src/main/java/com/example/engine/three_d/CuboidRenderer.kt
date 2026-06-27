package com.example.engine.three_d

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

object CuboidRenderer {
    fun DrawScope.draw3DCuboid(
        rx: Float, ry: Float, // Center pivot screen coordinate
        cx: Float, cy: Float, cz: Float, // Relative 3D offsets
        sizeX: Float, sizeY: Float, sizeZ: Float, // Dimensions
        rotX: Float, rotY: Float, rotZ: Float, // Radians rotation
        baseColor: Color,
        cameraDist: Float = 350f,
        wireframeOnly: Boolean = false,
        outlineColor: Color? = null
    ) {
        val hx = sizeX / 2f
        val hy = sizeY / 2f
        val hz = sizeZ / 2f

        // 8 vertices of a cuboid relative to its center
        val localVertices = listOf(
            Point3D(-hx, -hy, -hz), // 0
            Point3D(hx, -hy, -hz),  // 1
            Point3D(hx, hy, -hz),   // 2
            Point3D(-hx, hy, -hz),  // 3
            Point3D(-hx, -hy, hz),  // 4
            Point3D(hx, -hy, hz),   // 5
            Point3D(hx, hy, hz),    // 6
            Point3D(-hx, hy, hz)    // 7
        )

        // Translate to relative 3D space, then rotate, and project
        val worldVertices = localVertices.map { p ->
            val pt = Point3D(p.x + cx, p.y + cy, p.z + cz)
            var rotated = VectorMath.rotateX(pt, rotX)
            rotated = VectorMath.rotateY(rotated, rotY)
            rotated = VectorMath.rotateZ(rotated, rotZ)
            rotated
        }

        // Indices defining the 6 faces of a cuboid
        val faceIndices = listOf(
            listOf(0, 1, 2, 3), // Back
            listOf(4, 5, 6, 7), // Front
            listOf(0, 1, 5, 4), // Top
            listOf(3, 2, 6, 7), // Bottom
            listOf(0, 3, 7, 4), // Left
            listOf(1, 2, 6, 5)  // Right
        )

        data class FaceToDraw(val indices: List<Int>, val avgZ: Float, val normalLightFactor: Float)

        val faces = faceIndices.map { indices ->
            val v0 = worldVertices[indices[0]]
            val v1 = worldVertices[indices[1]]
            val v2 = worldVertices[indices[2]]
            val v3 = worldVertices[indices[3]]

            val avgZ = (v0.z + v1.z + v2.z + v3.z) / 4f

            // Compute face surface normal
            val ax = v1.x - v0.x
            val ay = v1.y - v0.y
            val az = v1.z - v0.z
            val bx = v2.x - v0.x
            val by = v2.y - v0.y
            val bz = v2.z - v0.z

            var nx = ay * bz - az * by
            var ny = az * bx - ax * bz
            var nz = ax * by - ay * bx
            val length = kotlin.math.sqrt(nx * nx + ny * ny + nz * nz)
            if (length > 0f) {
                nx /= length
                ny /= length
                nz /= length
            }

            // Diffuse light direction from upper-front-right (0.4, -0.8, -0.4)
            val dot = nx * 0.4f + ny * (-0.8f) + nz * (-0.4f)
            val lightFactor = (dot + 1f) / 2f

            FaceToDraw(indices, avgZ, lightFactor)
        }

        // Sort back-to-front (Painter's algorithm)
        val sortedFaces = faces.sortedByDescending { it.avgZ }

        val projectedPoints = worldVertices.map { p ->
            VectorMath.project(p, cameraDist, rx, ry)
        }

        sortedFaces.forEach { face ->
            val p0 = projectedPoints[face.indices[0]]
            val p1 = projectedPoints[face.indices[1]]
            val p2 = projectedPoints[face.indices[2]]
            val p3 = projectedPoints[face.indices[3]]

            val path = Path().apply {
                moveTo(p0.x, p0.y)
                lineTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
                lineTo(p3.x, p3.y)
                close()
            }

            if (!wireframeOnly) {
                // Among Us cel shading (flat light vs flat shadow)
                val isLit = face.normalLightFactor > 0.45f
                val factor = if (isLit) 1.0f else 0.65f
                val shadedColor = Color(
                    red = (baseColor.red * factor).coerceIn(0f, 1f),
                    green = (baseColor.green * factor).coerceIn(0f, 1f),
                    blue = (baseColor.blue * factor).coerceIn(0f, 1f),
                    alpha = baseColor.alpha
                )
                drawPath(path, shadedColor)
            }

            // Bold cartoon outlines like in Among Us
            drawPath(
                path = path,
                color = outlineColor ?: Color(0xFF070A13),
                style = Stroke(width = 2.8f)
            )
        }
    }
}
