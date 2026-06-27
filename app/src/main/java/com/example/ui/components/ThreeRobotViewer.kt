package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ThreeRobotViewer(
    paintColor: String,
    eyesType: String,
    hatType: String,
    legs: String,
    leftArm: String,
    rightArm: String,
    utility: String,
    headStyle: String = "Standard Dome",
    headMaterial: String = "Chrome Metal",
    torsoStyle: String = "Fusion Frame",
    torsoMaterial: String = "Chrome Metal",
    armsStyle: String = "Standard Claw",
    armsMaterial: String = "Chrome Metal",
    legsStyle: String = "Standard Biped",
    legsMaterial: String = "Chrome Metal",
    activeAnimation: String = "Idle Float",
    isAnimating: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Remember the WebView instance so it is not recreated during recomposition,
    // which allows the 3D camera controls and orbit angle to remain perfectly continuous.
    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            
            // Set client to ensure links are not opened in an external browser
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // Trigger initial sync once the page is fully loaded
                    val jsCommand = "if (window.updateRobot) { window.updateRobot('$paintColor', '$eyesType', '$hatType', '$legs', '$leftArm', '$rightArm', '$utility', '$headStyle', '$headMaterial', '$torsoStyle', '$torsoMaterial', '$armsStyle', '$armsMaterial', '$legsStyle', '$legsMaterial', '$activeAnimation', $isAnimating); }"
                    view?.evaluateJavascript(jsCommand, null)
                }
            }
            
            // Load the Three.js 3D environment from the local Android assets
            loadUrl("file:///android_asset/three_robot_viewer.html")
        }
    }

    // Reactively update the 3D robot model in real-time when any part selection changes in Kotlin
    LaunchedEffect(paintColor, eyesType, hatType, legs, leftArm, rightArm, utility, headStyle, headMaterial, torsoStyle, torsoMaterial, armsStyle, armsMaterial, legsStyle, legsMaterial, activeAnimation, isAnimating) {
        val jsCommand = "if (window.updateRobot) { window.updateRobot('$paintColor', '$eyesType', '$hatType', '$legs', '$leftArm', '$rightArm', '$utility', '$headStyle', '$headMaterial', '$torsoStyle', '$torsoMaterial', '$armsStyle', '$armsMaterial', '$legsStyle', '$legsMaterial', '$activeAnimation', $isAnimating); }"
        webView.evaluateJavascript(jsCommand, null)
    }

    // Embed the Android WebView within Jetpack Compose
    AndroidView(
        factory = { webView },
        modifier = modifier
    )
}
