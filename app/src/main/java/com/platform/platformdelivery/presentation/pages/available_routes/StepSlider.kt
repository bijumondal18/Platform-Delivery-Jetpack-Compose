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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    var currentStep by remember { mutableStateOf(initialIndex) }

    val density = LocalDensity.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val stepWidthPx = with(density) { (screenWidth.toPx() / (stepValues.size - 1)) }

    // drag offset always tied to a step
    var dragOffset by remember { mutableStateOf(currentStep * stepWidthPx) }

    // show bubble while dragging
    var isDragging by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(30.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // --- Bubble above line ---
        if (isDragging) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(dragOffset.toInt() - 20, -100) }
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stepValues[currentStep].toString(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // --- Line background ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
        )

        // --- Active line ---
        Box(
            modifier = Modifier
                .width(with(density) { dragOffset.toDp() })
                .height(4.dp)
                .background(MaterialTheme.colorScheme.primary)
        )

        // --- Step points ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(stepValues.size) { index ->
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            if (index <= currentStep) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            CircleShape
                        )
                )
            }
        }

        // --- Draggable handle ---
        Box(
            modifier = Modifier
                .offset { IntOffset(dragOffset.toInt() - 12, 0) }
                .size(24.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        isDragging = true
                        dragOffset = (dragOffset + delta)
                            .coerceIn(0f, stepWidthPx * (stepValues.size - 1))

                        // calculate nearest step
                        val newStep = (dragOffset / stepWidthPx).roundToInt()
                        if (newStep != currentStep) {
                            currentStep = newStep
                            onStepChanged(stepValues[currentStep])
                        }
                    },
                    onDragStopped = {
                        isDragging = false
                        // snap circle exactly to nearest step
                        dragOffset = currentStep * stepWidthPx
                    }
                )
        )
    }
}
