package com.alexcova.perkeo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedTitle(text: String, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "title")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(1500, easing = EaseInOut), RepeatMode.Reverse),
        label = "scale",
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = -2f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1500, easing = EaseInOut), RepeatMode.Reverse),
        label = "rotation",
    )

    Text(
        text = text,
        style = MaterialTheme.typography.displaySmall,
        color = Color.White,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale; rotationZ = rotation },
    )
}
