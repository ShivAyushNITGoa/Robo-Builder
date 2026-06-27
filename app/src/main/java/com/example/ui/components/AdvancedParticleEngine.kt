package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import java.util.Random

data class AdvancedParticle2D(
    var x: Float, var y: Float,
    var vx: Float, var vy: Float,
    val color: Color,
    var life: Int,
    val maxLife: Int,
    val size: Float
)

class AdvancedParticleEngine {
    private val particles = mutableListOf<AdvancedParticle2D>()
    private val random = Random()

    fun spawnExplosion(cx: Float, cy: Float, color: Color = Color(0xFFFF5F00), count: Int = 15) {
        for (i in 0 until count) {
            val speed = 2f + random.nextFloat() * 12f
            val angle = random.nextFloat() * 2f * Math.PI.toFloat()
            particles.add(
                AdvancedParticle2D(
                    x = cx,
                    y = cy,
                    vx = kotlin.math.cos(angle) * speed,
                    vy = kotlin.math.sin(angle) * speed,
                    color = color,
                    life = 10 + random.nextInt(15),
                    maxLife = 25,
                    size = 3f + random.nextFloat() * 8f
                )
            )
        }
    }

    fun updateAndDraw(drawScope: DrawScope, bounceWidth: Float, bounceHeight: Float) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()

            // Update with basic gravity + bounce physics
            p.vy += 0.2f // gravity
            p.x += p.vx
            p.y += p.vy

            // Bounce off sides
            if (p.x < 0f || p.x > bounceWidth) {
                p.vx *= -0.7f
                p.x = p.x.coerceIn(0f, bounceWidth)
            }
            if (p.y > bounceHeight) {
                p.vy *= -0.6f
                p.y = bounceHeight
            }

            p.life--
            if (p.life <= 0) {
                iterator.remove()
                continue
            }

            // Draw with alpha fade
            val alpha = p.life.toFloat() / p.maxLife.toFloat()
            drawScope.drawCircle(
                color = p.color.copy(alpha = alpha),
                radius = p.size,
                center = Offset(p.x, p.y)
            )
        }
    }
}
