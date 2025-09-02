package com.platform.platformdelivery.presentation.pages.available_routes

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt


@Composable
fun StepSlider(
    stepValues: List<Int> = listOf(10, 20, 30, 40, 50),
    initialIndex: Int = 0,
    modifier: Modifier = Modifier,
    onStepChanged: (Int) -> Unit = {}
) {
    val density = LocalDensity.current

    // sizes in px
    val dotRadiusPx = with(density) { 5.dp.toPx() }    // dot = 10.dp
    val handleRadiusPx = with(density) { 12.dp.toPx() } // handle = 24.dp
    val bubbleHalfWidthPx = with(density) { 20.dp.toPx() } // approx bubble half width

    var trackWidthPx by remember { mutableStateOf(0f) } // measured width of the line track
    val stepCount = stepValues.size

    // computed spacing between centers (only valid once trackWidthPx > 0)
    val spacingPx = remember(trackWidthPx, stepCount) {
        if (trackWidthPx > 0f && stepCount > 1) (trackWidthPx - 2 * dotRadiusPx) / (stepCount - 1)
        else 0f
    }

    fun centerForIndex(index: Int): Float = dotRadiusPx + index * spacingPx

    var currentStep by remember { mutableStateOf(initialIndex.coerceIn(0, stepCount - 1)) }
    var dragOffset by remember { mutableStateOf(0f) } // px representing the **center** of handle
    var isDragging by remember { mutableStateOf(false) }

    // init dragOffset after we know track width
    LaunchedEffect(trackWidthPx) {
        if (trackWidthPx > 0f) dragOffset = centerForIndex(currentStep)
    }

    // notify on step change
    LaunchedEffect(currentStep) {
        onStepChanged(stepValues[currentStep])
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp), // provide enough vertical space for bubble
        contentAlignment = Alignment.CenterStart
    ) {
        // --- TRACK (background) - measure width here ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .align(Alignment.Center)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                .onGloballyPositioned { coords ->
                    trackWidthPx = coords.size.width.toFloat()
                }
        )

        // --- ACTIVE LINE: width up to handle center ---
        if (trackWidthPx > 0f) {
            Box(
                modifier = Modifier
                    .width(with(density) { dragOffset.toDp() })
                    .height(4.dp)
                    .align(Alignment.CenterStart)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        // --- DOTS: place exactly at computed centers ---
        if (trackWidthPx > 0f) {
            for (index in 0 until stepCount) {
                val cx = centerForIndex(index)
                Box(
                    modifier = Modifier
                        .offset { IntOffset((cx - dotRadiusPx).roundToInt(), 0) }
                        .size(10.dp) // dot size
                        .align(Alignment.CenterStart)
                        .background(
                            if (index <= currentStep) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            CircleShape
                        )
                )
            }
        }

        // --- BUBBLE (above handle) ---
        if (isDragging && trackWidthPx > 0f) {
            Column(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (dragOffset - bubbleHalfWidthPx).roundToInt(),
                            (-80) // vertical lift for bubble + circle
                        )
                    }
                    .wrapContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Bubble body (rounded rect)
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = stepValues[currentStep].toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

            }
        }

        // --- HANDLE (draggable) ---
        if (trackWidthPx > 0f) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset((dragOffset - handleRadiusPx).roundToInt(), 0)
                    }
                    .size(24.dp)
                    .align(Alignment.CenterStart)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            isDragging = true
                            // update dragOffset and clamp to valid center range
                            val minCenter = dotRadiusPx
                            val maxCenter = trackWidthPx - dotRadiusPx
                            dragOffset = (dragOffset + delta).coerceIn(minCenter, maxCenter)

                            // compute nearest step
                            val rawIndex = ((dragOffset - dotRadiusPx) / spacingPx)
                            val newIndex = rawIndex.roundToInt().coerceIn(0, stepCount - 1)
                            if (newIndex != currentStep) {
                                currentStep = newIndex
                            }
                        },
                        onDragStopped = {
                            isDragging = false
                            // snap to exact center of currentStep
                            dragOffset = centerForIndex(currentStep)
                        }
                    )
            )
        }
    }
}
