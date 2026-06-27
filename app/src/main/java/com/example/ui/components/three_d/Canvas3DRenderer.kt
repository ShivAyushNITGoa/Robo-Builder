package com.example.ui.components.three_d

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.engine.three_d.Camera3D
import com.example.engine.three_d.Mesh3D
import com.example.engine.three_d.MeshRenderer.drawMesh3D

@Composable
fun Canvas3DRenderer(
    modifier: Modifier = Modifier,
    meshes: List<Mesh3D>,
    camera: Camera3D,
    meshTransforms: Map<Mesh3D, Transform3D> = emptyMap()
) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f

        meshes.forEach { mesh ->
            val transform = meshTransforms[mesh] ?: Transform3D()
            drawMesh3D(
                mesh = mesh,
                rx = centerX,
                ry = centerY,
                cx = transform.translationX,
                cy = transform.translationY,
                cz = transform.translationZ,
                scaleX = transform.scaleX,
                scaleY = transform.scaleY,
                scaleZ = transform.scaleZ,
                rotX = transform.rotationX + camera.pitch,
                rotY = transform.rotationY + camera.yaw,
                rotZ = transform.rotationZ,
                cameraDist = camera.posZ
            )
        }
    }
}

data class Transform3D(
    val translationX: Float = 0f,
    val translationY: Float = 0f,
    val translationZ: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val scaleZ: Float = 1f,
    val rotationX: Float = 0f,
    val rotationY: Float = 0f,
    val rotationZ: Float = 0f
)
