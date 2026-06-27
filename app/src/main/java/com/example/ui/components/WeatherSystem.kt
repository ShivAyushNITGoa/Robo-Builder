package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonOrange
import java.util.Random

data class WeatherParticle(
    var x: Float,
    var y: Float,
    var speed: Float,
    var size: Float,
    val color: Color,
    val type: String // "snow", "plasma", "dust"
)

@Composable
fun WeatherSystem(
    modifier: Modifier = Modifier,
    weatherProfile: String = "NEBULA_DUST", // "NEBULA_DUST", "SOLAR_STORM", "ICE_FIELD"
    speedMultiplier: Float = 1.0f
) {
    val random = remember { Random() }
    val particles = remember(weatherProfile) {
        mutableStateListOf<WeatherParticle>().apply {
            repeat(40) {
                add(
                    WeatherParticle(
                        x = random.nextFloat() * 1000f,
                        y = random.nextFloat() * 1000f,
                        speed = 1f + random.nextFloat() * 4f,
                        size = 2f + random.nextFloat() * 8f,
                        color = when (weatherProfile) {
                            "SOLAR_STORM" -> if (random.nextBoolean()) NeonOrange else Color.Yellow
                            "ICE_FIELD" -> Color.White.copy(alpha = 0.8f)
                            else -> if (random.nextBoolean()) NeonCyan else NeonMagenta
                        },
                        type = when (weatherProfile) {
                            "SOLAR_STORM" -> "plasma"
                            "ICE_FIELD" -> "snow"
                            else -> "dust"
                        }
                    )
                )
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "weather_ticks")
    val ticker by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(16, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ticker"
    )

    // Side effect to update positions
    LaunchedEffect(ticker) {
        particles.forEach { p ->
            p.x -= p.speed * speedMultiplier
            if (p.x < -50f) {
                p.x = 1100f
                p.y = random.nextFloat() * 800f
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { p ->
            // Scale positions to local Canvas dimensions
            val drawX = (p.x / 1000f) * size.width
            val drawY = (p.y / 1000f) * size.height

            if (p.type == "plasma") {
                // Glow tail or cross spark
                drawLine(
                    color = p.color,
                    start = androidx.compose.ui.geometry.Offset(drawX - p.size, drawY),
                    end = androidx.compose.ui.geometry.Offset(drawX + p.size, drawY),
                    strokeWidth = 3f
                )
                drawLine(
                    color = p.color,
                    start = androidx.compose.ui.geometry.Offset(drawX, drawY - p.size),
                    end = androidx.compose.ui.geometry.Offset(drawX, drawY + p.size),
                    strokeWidth = 3f
                )
            } else {
                drawCircle(
                    color = p.color,
                    radius = p.size,
                    center = androidx.compose.ui.geometry.Offset(drawX, drawY)
                )
            }
        }
    }
}
