package com.adikob.galaxychat.utility

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

fun Modifier.shimmerBackground(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmerTransition")

    val xShimmer by transition.animateFloat(
        initialValue = -(size.width.toFloat()),
        targetValue = size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ), label = "xShimmer"
    )

    val brush = remember(xShimmer) {
        Brush.linearGradient(
            colors = listOf(
                Color.LightGray.copy(alpha = 0.6f),
                Color.LightGray.copy(alpha = 0.9f),
                Color.LightGray.copy(alpha = 0.6f)
            ),
            start = Offset(xShimmer, 0f),
            end = Offset(xShimmer + size.width.toFloat(), 0f)
        )
    }

    this
        .onGloballyPositioned { coordinates -> size = coordinates.size }
        .background(brush)
}

@Composable
inline fun <reified T: ViewModel> NavBackStackEntry.scopedViewModel(
    route: String,
    navController: NavController
): T {
    val parentEntry = remember(this) { navController.getBackStackEntry(route) }
    return hiltViewModel(parentEntry)
}