package com.platform.platformdelivery.presentation.widgets

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material3.placeholder
import com.google.accompanist.placeholder.material3.shimmer


@Composable
fun Modifier.shimmerPlaceholder(
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    highlightColor: Color? = null
): Modifier {
    // If highlightColor is provided, use custom shimmer with gradient
    return if (highlightColor != null) {
        val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
        val shimmerTranslateAnim by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 700,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer_translate"
        )

        this
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        backgroundColor,
                        highlightColor,
                        backgroundColor
                    ),
                    start = Offset(shimmerTranslateAnim - 500f, shimmerTranslateAnim - 500f),
                    end = Offset(shimmerTranslateAnim, shimmerTranslateAnim)
                )
            )
    } else {
        // Use default Accompanist shimmer
        this.placeholder(
            visible = true,
            color = backgroundColor,
            shape = RoundedCornerShape(12.dp),
            highlight = PlaceholderHighlight.shimmer()
        )
    }
}