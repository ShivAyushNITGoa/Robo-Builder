package com.example.engine.three_d

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

object MeshRenderer {
    fun DrawScope.drawMesh3D(
        mesh: Mesh3D,
        rx: Float, ry: Float, // Center pivot screen coordinate
        cx: Float, cy: Float, cz: Float, // Position offset
        scaleX: Float = 1f, scaleY: Float = 1f, scaleZ: Float = 1f,
        rotX: Float = 0f, rotY: Float = 0f, rotZ: Float = 0f,
        cameraDist: Float = 350f,
        lightDirection: Point3D = Point3D(0.4f, -0.8f, -0.4f),
        wireframeOnly: Boolean = false,
        customBaseColor: Color? = null
    ) {
        // Build transformation matrix
        val transformMatrix = Matrix4.makeTranslation(cx, cy, cz)
            .multiply(Matrix4.makeRotationX(rotX))
            .multiply(Matrix4.makeRotationY(rotY))
            .multiply(Matrix4.makeRotationZ(rotZ))
            .multiply(Matrix4.makeScale(scaleX, scaleY, scaleZ))

        // Transform vertices
        val transformedVertices = mesh.vertices.map { v ->
            transformMatrix.transform(v)
        }

        // Project vertices to screen coordinates
        val projectedPoints = transformedVertices.map { p ->
            VectorMath.project(p, cameraDist, rx, ry)
        }

        // Normalize light direction
        val len = kotlin.math.sqrt(lightDirection.x * lightDirection.x + lightDirection.y * lightDirection.y + lightDirection.z * lightDirection.z)
        val lx = if (len > 0) lightDirection.x / len else 0f
        val ly = if (len > 0) lightDirection.y / len else 0f
        val lz = if (len > 0) lightDirection.z / len else 0f

        // Class representing face sorted calculations
        data class SortedFace(
            val face: Face3D,
            val avgZ: Float,
            val normalLightFactor: Float,
            val projectedCorners: List<Offset>
        )

        val sortedFaces = mesh.faces.mapNotNull { face ->
            if (face.indices.size < 3) return@mapNotNull null

            val faceVertices = face.indices.map { transformedVertices[it] }
            val avgZ = faceVertices.map { it.z }.average().toFloat()

            // Compute face normal
            val v0 = faceVertices[0]
            val v1 = faceVertices[1]
            val v2 = faceVertices[2]

            val ax = v1.x - v0.x
            val ay = v1.y - v0.y
            val az = v1.z - v0.z
            val bx = v2.x - v0.x
            val by = v2.y - v0.y
            val bz = v2.z - v0.z

            var nx = ay * bz - az * by
            var ny = az * bx - ax * bz
            var nz = ax * by - ay * bx
            val nLen = kotlin.math.sqrt(nx * nx + ny * ny + nz * nz)
            if (nLen > 0f) {
                nx /= nLen
                ny /= nLen
                nz /= nLen
            }

            // Back-face culling (for closed solids, normal points outward. If nz > 0, it points away from camera, but here coordinates match traditional z)
            // If normal dot camera direction (0,0,-1) is positive, we cull or keep. Let's just keep simple Painter's sorting and skip full culling to handle flat parts.
            val dot = nx * lx + ny * ly + nz * lz
            val lightFactor = ((dot + 1f) / 2f).coerceIn(0f, 1f)

            val projectedCorners = face.indices.map { projectedPoints[it] }

            SortedFace(face, avgZ, lightFactor, projectedCorners)
        }.sortedByDescending { it.avgZ }

        // Render each face
        sortedFaces.forEach { sortedFace ->
            val path = Path().apply {
                val start = sortedFace.projectedCorners.first()
                moveTo(start.x, start.y)
                for (i in 1 until sortedFace.projectedCorners.size) {
                    val p = sortedFace.projectedCorners[i]
                    lineTo(p.x, p.y)
                }
                close()
            }

            val baseColor = customBaseColor ?: sortedFace.face.color

            if (!wireframeOnly) {
                // Apply diffuse shading
                val factor = 0.5f + sortedFace.normalLightFactor * 0.5f
                val shadedColor = Color(
                    red = (baseColor.red * factor).coerceIn(0f, 1f),
                    green = (baseColor.green * factor).coerceIn(0f, 1f),
                    blue = (baseColor.blue * factor).coerceIn(0f, 1f),
                    alpha = baseColor.alpha
                )
                drawPath(path, shadedColor)
            }

            // Outline for a cartoon-shader or blueprint detail aesthetic
            if (sortedFace.face.isOutlineEnabled) {
                drawPath(
                    path = path,
                    color = Color(0xFF070A13),
                    style = Stroke(width = 2.0f)
                )
            }
        }
    }
}
