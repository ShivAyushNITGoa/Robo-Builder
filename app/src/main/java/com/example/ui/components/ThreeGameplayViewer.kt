package com.example.ui.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.SimulationState

@Composable
fun ThreeGameplayViewer(
    modifier: Modifier = Modifier,
    robotX: Float,
    robotY: Float,
    robotZ: Float,
    speed: Float,
    paintColor: String,
    eyesType: String,
    hatType: String,
    legs: String,
    leftArm: String,
    rightArm: String,
    utility: String,
    hazardType: String,
    progress: Float,
    simulationState: SimulationState
) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
            }
            webViewClient = WebViewClient()
            loadUrl("file:///android_asset/three_gameplay_sandbox.html")
        }
    }

    // Push full coordinates, parts, cosmetics and progress down to WebGL context smoothly
    LaunchedEffect(
        robotX, robotY, robotZ, speed, paintColor, eyesType, hatType,
        legs, leftArm, rightArm, utility, hazardType, progress, simulationState
    ) {
        val stateString = when (simulationState) {
            is SimulationState.Success -> "success"
            is SimulationState.Failure -> "failure"
            is SimulationState.Running -> "running"
            else -> "idle"
        }
        val safePaint = paintColor.replace("#", "")
        
        // Escape strings to prevent JS syntax injection errors
        val escapedLegs = legs.replace("'", "\\'")
        val escapedLeftArm = leftArm.replace("'", "\\'")
        val escapedRightArm = rightArm.replace("'", "\\'")
        val escapedUtility = utility.replace("'", "\\'")
        val escapedHazard = hazardType.replace("'", "\\'")

        webView.evaluateJavascript(
            "if (typeof window.updateRobotState === 'function') { " +
            "window.updateRobotState(" +
                "$robotX, $robotY, $robotZ, $speed, " +
                "'$safePaint', '$eyesType', '$hatType', " +
                "'$escapedLegs', '$escapedLeftArm', '$escapedRightArm', '$escapedUtility', " +
                "'$escapedHazard', $progress, '$stateString'" +
            "); }",
            null
        )
    }

    AndroidView(
        factory = { webView },
        modifier = modifier.fillMaxSize()
    )
}

