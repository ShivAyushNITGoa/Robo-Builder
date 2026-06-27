package com.example.engine.three_d.objects

import androidx.compose.ui.graphics.Color
import com.example.engine.three_d.Face3D
import com.example.engine.three_d.Mesh3D
import com.example.engine.three_d.Point3D

object Robot3DAssembler {
    fun assembleCustomRobot(
        legsType: String,
        leftArmType: String,
        rightArmType: String,
        utilityType: String,
        themeColor: Color = Color(0xFF00F0FF)
    ): Mesh3D {
        val mergedVertices = mutableListOf<Point3D>()
        val mergedFaces = mutableListOf<Face3D>()

        fun addSubMesh(mesh: Mesh3D, dx: Float, dy: Float, dz: Float, scale: Float = 1f) {
            val offset = mergedVertices.size
            mesh.vertices.forEach { v ->
                mergedVertices.add(
                    Point3D(
                        x = v.x * scale + dx,
                        y = v.y * scale + dy,
                        z = v.z * scale + dz
                    )
                )
            }
            mesh.faces.forEach { f ->
                mergedFaces.add(
                    Face3D(
                        indices = f.indices.map { it + offset },
                        color = f.color,
                        isOutlineEnabled = f.isOutlineEnabled
                    )
                )
            }
        }

        // 1. Torso / Cabin (Center) - Dynamic color
        val cabinColor = themeColor
        val visorColor = Color(0xFF070A13)
        val cabin = Mesh3D.createCube(40f, cabinColor, "cabin")
        addSubMesh(cabin, 0f, -10f, 0f)

        // 2. Visor / Glass Eye (Front)
        val visor = Mesh3D.createCube(20f, Color(0xFF00FFCC), "visor")
        addSubMesh(visor, 0f, -15f, 15f)

        // 3. Legs
        when (legsType) {
            "Biped", "Standard Biped" -> {
                // Two separate leg shafts
                val legLeft = Mesh3D.createCylinder(4f, 30f, 8, Color.DarkGray, "leg_l")
                addSubMesh(legLeft, -15f, 15f, 0f)

                val legRight = Mesh3D.createCylinder(4f, 30f, 8, Color.DarkGray, "leg_r")
                addSubMesh(legRight, 15f, 15f, 0f)

                // Big biped feet
                val footLeft = Mesh3D.createCube(12f, cabinColor, "foot_l")
                addSubMesh(footLeft, -15f, 30f, 5f)
                val footRight = Mesh3D.createCube(12f, cabinColor, "foot_r")
                addSubMesh(footRight, 15f, 30f, 5f)
            }
            "Tracks", "Treads" -> {
                // Heavy track bases left & right
                val trackLeft = Mesh3D.createCube(18f, Color.DarkGray, "track_l")
                addSubMesh(trackLeft, -22f, 20f, 0f)
                val trackRight = Mesh3D.createCube(18f, Color.DarkGray, "track_r")
                addSubMesh(trackRight, 22f, 20f, 0f)
            }
            else -> {
                // Default quad pedestal or column leg
                val pedestal = Mesh3D.createCylinder(12f, 24f, 8, Color.LightGray, "pedestal")
                addSubMesh(pedestal, 0f, 15f, 0f)
            }
        }

        // 4. Left Arm
        when (leftArmType) {
            "Drill", "Heavy Drill" -> {
                val shoulder = Mesh3D.createCylinder(8f, 12f, 8, Color.Gray, "shoulder_l")
                addSubMesh(shoulder, -28f, -10f, 0f)
                val drillCone = Mesh3D.createCylinder(8f, 24f, 6, Color(0xFFFFD700), "drill_l")
                addSubMesh(drillCone, -35f, -10f, 12f)
            }
            "Laser", "Plasma Blaster" -> {
                val shoulder = Mesh3D.createCylinder(8f, 12f, 8, Color.Gray, "shoulder_l")
                addSubMesh(shoulder, -28f, -10f, 0f)
                val barrel = Mesh3D.createCylinder(4f, 28f, 8, Color(0xFFFE0032), "laser_l")
                addSubMesh(barrel, -32f, -10f, 10f)
            }
            else -> {
                // Standard default utility hook
                val hook = Mesh3D.createCube(10f, Color.Gray, "arm_l")
                addSubMesh(hook, -25f, -10f, 5f)
            }
        }

        // 5. Right Arm
        when (rightArmType) {
            "Drill", "Heavy Drill" -> {
                val shoulder = Mesh3D.createCylinder(8f, 12f, 8, Color.Gray, "shoulder_r")
                addSubMesh(shoulder, 28f, -10f, 0f)
                val drillCone = Mesh3D.createCylinder(8f, 24f, 6, Color(0xFFFFD700), "drill_r")
                addSubMesh(drillCone, 35f, -10f, 12f)
            }
            "Laser", "Plasma Blaster" -> {
                val shoulder = Mesh3D.createCylinder(8f, 12f, 8, Color.Gray, "shoulder_r")
                addSubMesh(shoulder, 28f, -10f, 0f)
                val barrel = Mesh3D.createCylinder(4f, 28f, 8, Color(0xFFFE0032), "laser_r")
                addSubMesh(barrel, 32f, -10f, 10f)
            }
            else -> {
                val hand = Mesh3D.createCube(10f, Color.Gray, "arm_r")
                addSubMesh(hand, 25f, -10f, 5f)
            }
        }

        // 6. Utility (Top / Back slot)
        when (utilityType) {
            "Wings", "Solar Sails" -> {
                // Left Wing extension
                val wingL = Mesh3D.createCube(28f, Color(0xAA00FFCC), "wing_l")
                addSubMesh(wingL, -32f, -25f, -15f)
                // Right Wing extension
                val wingR = Mesh3D.createCube(28f, Color(0xAA00FFCC), "wing_r")
                addSubMesh(wingR, 32f, -25f, -15f)
            }
            "Shield", "Deflector Field" -> {
                // Floating power disk on back
                val shieldDisk = Mesh3D.createCylinder(22f, 6f, 10, Color(0x6600F0FF), "shield")
                addSubMesh(shieldDisk, 0f, -10f, -24f)
            }
            "Radar", "Scanner Array" -> {
                // Rotating satellite dish on top
                val radarPost = Mesh3D.createCylinder(3f, 15f, 6, Color.DarkGray, "radar_post")
                addSubMesh(radarPost, 0f, -35f, 0f)
                val dish = Mesh3D.createCylinder(16f, 4f, 8, Color.LightGray, "radar_dish")
                addSubMesh(dish, 0f, -42f, 2f)
            }
        }

        return Mesh3D(mergedVertices, mergedFaces, "custom_robot")
    }
}
