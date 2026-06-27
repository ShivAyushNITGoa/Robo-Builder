package com.example.engine.three_d

import androidx.compose.ui.graphics.Color

data class Face3D(
    val indices: List<Int>,
    val color: Color,
    val isOutlineEnabled: Boolean = true
)

data class Mesh3D(
    val vertices: List<Point3D>,
    val faces: List<Face3D>,
    val tag: String = "mesh"
) {
    companion object {
        fun createCube(size: Float, color: Color, tag: String = "cube"): Mesh3D {
            val h = size / 2f
            val vertices = listOf(
                Point3D(-h, -h, -h),
                Point3D(h, -h, -h),
                Point3D(h, h, -h),
                Point3D(-h, h, -h),
                Point3D(-h, -h, h),
                Point3D(h, -h, h),
                Point3D(h, h, h),
                Point3D(-h, h, h)
            )
            val faces = listOf(
                Face3D(listOf(0, 1, 2, 3), color), // Back
                Face3D(listOf(4, 5, 6, 7), color), // Front
                Face3D(listOf(0, 1, 5, 4), color), // Top
                Face3D(listOf(3, 2, 6, 7), color), // Bottom
                Face3D(listOf(0, 3, 7, 4), color), // Left
                Face3D(listOf(1, 2, 6, 5), color)  // Right
            )
            return Mesh3D(vertices, faces, tag)
        }

        fun createCylinder(radius: Float, height: Float, segments: Int, color: Color, tag: String = "cylinder"): Mesh3D {
            val vertices = mutableListOf<Point3D>()
            val faces = mutableListOf<Face3D>()
            val h = height / 2f

            // Top center and bottom center
            val topCenterIdx = segments * 2
            val bottomCenterIdx = segments * 2 + 1

            for (i in 0 until segments) {
                val angle = (2 * Math.PI * i / segments).toFloat()
                val x = radius * kotlin.math.cos(angle)
                val z = radius * kotlin.math.sin(angle)
                // Top cap vertex
                vertices.add(Point3D(x, -h, z))
                // Bottom cap vertex
                vertices.add(Point3D(x, h, z))
            }

            vertices.add(Point3D(0f, -h, 0f)) // Top center
            vertices.add(Point3D(0f, h, 0f))  // Bottom center

            // Generate side faces and cap faces
            for (i in 0 until segments) {
                val next = (i + 1) % segments
                val tCurrent = i * 2
                val bCurrent = i * 2 + 1
                val tNext = next * 2
                val bNext = next * 2 + 1

                // Side wall face
                faces.add(Face3D(listOf(tCurrent, tNext, bNext, bCurrent), color))

                // Top cap face
                faces.add(Face3D(listOf(tCurrent, topCenterIdx, tNext), color))

                // Bottom cap face
                faces.add(Face3D(listOf(bCurrent, bNext, bottomCenterIdx), color))
            }

            return Mesh3D(vertices, faces, tag)
        }
    }
}
