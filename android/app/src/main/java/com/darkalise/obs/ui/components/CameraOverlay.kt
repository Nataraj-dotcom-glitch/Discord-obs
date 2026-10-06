package com.darkalise.obs.ui.components

import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.darkalise.obs.recorder.CameraOverlayManager
import com.darkalise.obs.ui.theme.ObsPurplePrimary
import kotlin.math.roundToInt

@Composable
fun CameraOverlay(
    cameraManager: CameraOverlayManager,
    initialX: Float = 50f,
    initialY: Float = 100f,
    widthDp: Int = 120,
    heightDp: Int = 160,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var offsetX by remember { mutableFloatStateOf(initialX) }
    var offsetY by remember { mutableFloatStateOf(initialY) }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .size(widthDp.dp, heightDp.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, ObsPurplePrimary, RoundedCornerShape(16.dp))
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    cameraManager.bindCameraPreview(lifecycleOwner, this)
                }
            },
            modifier = Modifier.clip(RoundedCornerShape(16.dp))
        )
    }
}
