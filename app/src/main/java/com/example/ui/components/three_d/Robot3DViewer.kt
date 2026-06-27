package com.example.ui.components.three_d

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.example.engine.three_d.Camera3D
import com.example.engine.three_d.objects.Robot3DAssembler

@Composable
fun Robot3DViewer(
    modifier: Modifier = Modifier,
    legsType: String,
    leftArmType: String,
    rightArmType: String,
    utilityType: String,
    themeColor: Color = Color(0xFF00F0FF)
) {
    var rotationY by remember { mutableStateOf(0.4f) }
    var rotationX by remember { mutableStateOf(-0.2f) }

    val robotMesh = remember(legsType, leftArmType, rightArmType, utilityType, themeColor) {
        Robot3DAssembler.assembleCustomRobot(legsType, leftArmType, rightArmType, utilityType, themeColor)
    }

    val camera = remember { Camera3D().apply { posZ = 300f } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    rotationY += dragAmount.x * 0.007f
                    rotationX = (rotationX + dragAmount.y * 0.007f).coerceIn(-1.2f, 1.2f)
                }
            }
    ) {
        val transform = remember(rotationX, rotationY) {
            Transform3D(
                rotationX = rotationX,
                rotationY = rotationY,
                translationY = 10f
            )
        }

        Canvas3DRenderer(
            modifier = Modifier.fillMaxSize(),
            meshes = listOf(robotMesh),
            camera = camera,
            meshTransforms = mapOf(robotMesh to transform)
        )
    }
}
