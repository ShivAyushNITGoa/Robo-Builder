package com.example.ui.components.three_d

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.engine.three_d.Camera3D
import com.example.engine.three_d.Mesh3D
import com.example.engine.three_d.MeshRenderer.drawMesh3D
import com.example.engine.three_d.Point3D
import com.example.engine.three_d.objects.Level3DObstacle
import com.example.engine.three_d.objects.Robot3DAssembler
import com.example.ui.components.three_d.fx.ParticleSystem3D
import com.example.ui.theme.CyberObsidian
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta

@Composable
fun Level3DViewport(
    modifier: Modifier = Modifier,
    robotX: Float,
    robotY: Float,
    robotZ: Float,
    legsType: String,
    leftArmType: String,
    rightArmType: String,
    utilityType: String,
    obstacles: List<Level3DObstacle>,
    particles: ParticleSystem3D,
    themeColor: Color = NeonCyan
) {
    val camera = remember {
        Camera3D().apply {
            posZ = 380f
            pitch = -0.25f // slight tilt down
        }
    }

    val robotMesh = remember(legsType, leftArmType, rightArmType, utilityType, themeColor) {
        Robot3DAssembler.assembleCustomRobot(legsType, leftArmType, rightArmType, utilityType, themeColor)
    }

    // Custom spatial sky grid mesh
    val gridMesh = remember {
        val verts = mutableListOf<Point3D>()
        val faces = mutableListOf<com.example.engine.three_d.Face3D>()
        // Floor lines
        val range = 250f
        val steps = 10
        val stepSize = range * 2f / steps
        for (i in 0..steps) {
            val offset = -range + i * stepSize
            // Horizontal lines
            verts.add(Point3D(-range, 50f, offset))
            verts.add(Point3D(range, 50f, offset))
            // Vertical lines
            verts.add(Point3D(offset, 50f, -range))
            verts.add(Point3D(offset, 50f, range))
        }
        // Let's bundle them as individual thin quads to make them mesh-renderable
        for (idx in verts.indices step 2) {
            if (idx + 1 < verts.size) {
                // simple face representation for wireframe grid
                faces.add(com.example.engine.three_d.Face3D(listOf(idx, idx + 1, idx), NeonMagenta.copy(alpha = 0.4f)))
            }
        }
        Mesh3D(verts, faces, "grid")
    }

    // Sync camera to chase the robot smoothly
    LaunchedEffect(robotX, robotY, robotZ) {
        camera.targetX = robotX
        camera.targetY = robotY
        camera.targetZ = robotZ
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberObsidian)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f

            // 1. Draw Environment Grid
            drawMesh3D(
                mesh = gridMesh,
                rx = centerX, ry = centerY,
                cx = 0f, cy = 0f, cz = 0f,
                cameraDist = camera.posZ,
                wireframeOnly = true
            )

            // 2. Draw 3D Obstacles
            obstacles.forEach { obstacle ->
                val obstacleMesh = obstacle.getMesh()
                drawMesh3D(
                    mesh = obstacleMesh,
                    rx = centerX, ry = centerY,
                    cx = obstacle.x, cy = obstacle.y, cz = obstacle.z,
                    scaleX = obstacle.sizeX, scaleY = obstacle.sizeY, scaleZ = obstacle.sizeZ,
                    cameraDist = camera.posZ
                )
            }

            // 3. Draw Assembled Robot
            drawMesh3D(
                mesh = robotMesh,
                rx = centerX, ry = centerY,
                cx = robotX, cy = robotY, cz = robotZ,
                cameraDist = camera.posZ
            )

            // 4. Draw Particles (Thruster plumes, sparks)
            with(particles) {
                drawParticles3D(camera)
            }
        }
    }
}
