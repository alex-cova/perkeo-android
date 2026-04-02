package com.alexcova.perkeo.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.alexcova.perkeo.domain.engine.LegendaryJoker
import com.alexcova.perkeo.ui.sprite.SpriteView

@Composable
fun TribouleteView(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "triboulete")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -2f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1500, easing = EaseInOut), RepeatMode.Reverse),
        label = "rotation",
    )

    SpriteView(
        item = LegendaryJoker.Triboulet,
        showLabel = false,
        modifier = modifier
            .size(71.dp, 95.dp)
            .graphicsLayer { rotationZ = rotation },
    )
}
