package com.example.ui.components.three_d.fx

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.engine.three_d.Camera3D
import com.example.engine.three_d.Point3D

data class Particle3D(
    var x: Float, var y: Float, var z: Float,
    var vx: Float, var vy: Float, var vz: Float,
    val color: Color,
    var life: Int,
    val maxLife: Int,
    val size: Float = 4f
)

class ParticleSystem3D {
    private val particles = mutableListOf<Particle3D>()
    private val random = java.util.Random()

    fun spawnThrusterPlume(pos: Point3D, directionZ: Float, count: Int = 3, color: Color = Color(0xFF00F0FF)) {
        for (i in 0 until count) {
            particles.add(
                Particle3D(
                    x = pos.x + (random.nextFloat() - 0.5f) * 10f,
                    y = pos.y + (random.nextFloat() - 0.5f) * 10f,
                    z = pos.z,
                    vx = (random.nextFloat() - 0.5f) * 5f,
                    vy = (random.nextFloat() - 0.5f) * 5f + 10f, // drift down
                    vz = directionZ * (5f + random.nextFloat() * 10f),
                    color = color,
                    life = 15 + random.nextInt(15),
                    maxLife = 30,
                    size = 4f + random.nextFloat() * 6f
                )
            )
        }
    }

    fun spawnSparks(pos: Point3D, count: Int = 10, color: Color = Color(0xFFFF5F00)) {
        for (i in 0 until count) {
            particles.add(
                Particle3D(
                    x = pos.x,
                    y = pos.y,
                    z = pos.z,
                    vx = (random.nextFloat() - 0.5f) * 30f,
                    vy = (random.nextFloat() - 0.5f) * 30f,
                    vz = (random.nextFloat() - 0.5f) * 30f,
                    color = color,
                    life = 10 + random.nextInt(10),
                    maxLife = 20,
                    size = 3f + random.nextFloat() * 4f
                )
            )
        }
    }

    fun update() {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.x += p.vx
            p.y += p.vy
            p.z += p.vz
            p.life--
            if (p.life <= 0) {
                iterator.remove()
            }
        }
    }

    fun DrawScope.drawParticles3D(camera: Camera3D) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f

        // Sort by Z depth to correctly composite behind/in-front of 3D objects
        val sorted = particles.sortedByDescending { it.z }

        sorted.forEach { p ->
            val p3D = Point3D(p.x, p.y, p.z)
            val proj = camera.getProjectedOffset(p3D, centerX, centerY)

            // Check boundaries
            if (proj.x in 0f..size.width && proj.y in 0f..size.height) {
                val alpha = p.life.toFloat() / p.maxLife.toFloat()
                val fadeColor = p.color.copy(alpha = alpha)

                // Scale size based on distance
                val cameraDistance = camera.posZ * camera.zoomFactor
                val scaleFactor = cameraDistance / (cameraDistance + p.z).coerceAtLeast(1f)
                val dotSize = (p.size * scaleFactor).coerceAtLeast(1f)

                drawCircle(
                    color = fadeColor,
                    radius = dotSize,
                    center = proj
                )
            }
        }
    }
}
