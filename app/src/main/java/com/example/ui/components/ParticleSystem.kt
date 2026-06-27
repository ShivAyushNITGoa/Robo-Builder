package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.ui.Particle

object ParticleSystem {
    fun DrawScope.drawSimParticles(particles: List<Particle>) {
        particles.forEach { p ->
            val parsedColor = try {
                Color(android.graphics.Color.parseColor(p.color))
            } catch (e: Exception) {
                CyberBlue
            }
            drawCircle(
                color = parsedColor.copy(alpha = (p.life / 30f).coerceIn(0f, 1f)),
                radius = 3f + (p.life / 15f),
                center = Offset(p.x, p.y)
            )
        }
    }
}
