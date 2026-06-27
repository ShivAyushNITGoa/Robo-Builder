package com.example.engine.three_d.objects

import androidx.compose.ui.graphics.Color
import com.example.engine.three_d.Mesh3D

data class Level3DObstacle(
    val id: String,
    var x: Float,
    var y: Float,
    var z: Float,
    val sizeX: Float,
    val sizeY: Float,
    val sizeZ: Float,
    val color: Color,
    val type: String = "solid", // "solid", "hazard", "checkpoint", "goal"
    var velX: Float = 0f,
    var velY: Float = 0f,
    var velZ: Float = 0f
) {
    private val cachedMesh: Mesh3D by lazy {
        Mesh3D.createCube(1f, color, id) // Create a unit cube to be scaled during draw
    }

    fun getMesh(): Mesh3D = cachedMesh

    fun update(deltaTime: Float) {
        x += velX * deltaTime
        y += velY * deltaTime
        z += velZ * deltaTime
    }

    fun getAABB(): com.example.engine.three_d.BoundingBox3D {
        val hx = sizeX / 2f
        val hy = sizeY / 2f
        val hz = sizeZ / 2f
        return com.example.engine.three_d.BoundingBox3D(
            minX = x - hx, maxX = x + hx,
            minY = y - hy, maxY = y + hy,
            minZ = z - hz, maxZ = z + hz
        )
    }
}
