package com.example.engine.three_d.objects

import com.example.engine.three_d.Mesh3D
import com.example.engine.three_d.Point3D

data class Model3D(
    val mesh: Mesh3D,
    var posX: Float = 0f,
    var posY: Float = 0f,
    var posZ: Float = 0f,
    var scaleX: Float = 1f,
    var scaleY: Float = 1f,
    var scaleZ: Float = 1f,
    var rotX: Float = 0f,
    var rotY: Float = 0f,
    var rotZ: Float = 0f,
    val id: String = "model"
) {
    fun getCenter(): Point3D {
        return Point3D(posX, posY, posZ)
    }
}
