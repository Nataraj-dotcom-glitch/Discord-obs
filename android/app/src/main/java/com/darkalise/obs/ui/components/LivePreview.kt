package com.darkalise.obs.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darkalise.obs.model.Scene
import com.darkalise.obs.model.Source
import com.darkalise.obs.model.SourceType
import com.darkalise.obs.ui.theme.ObsBorder
import com.darkalise.obs.ui.theme.ObsPurplePrimary
import com.darkalise.obs.ui.theme.ObsRedRec
import com.darkalise.obs.ui.theme.ObsTextMuted
import kotlin.math.roundToInt

@Composable
fun LivePreview(
    activeScene: Scene,
    onSourceTransformChange: (String, Float, Float, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF07060D))
            .border(1.5.dp, ObsBorder, RoundedCornerShape(8.dp))
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val canvasWidthPx = constraints.maxWidth.toFloat()
            val canvasHeightPx = constraints.maxHeight.toFloat()

            // Filter enabled visual sources sorted by z-index
            val visualSources = activeScene.sources
                .filter { it.isEnabled && it.type != SourceType.MICROPHONE && it.type != SourceType.DEVICE_AUDIO }
                .sortedBy { it.zIndex }

            if (visualSources.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PREVIEW IDLE • NO VISUAL SOURCES",
                        color = ObsTextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                visualSources.forEach { source ->
                    SourceCanvasItem(
                        source = source,
                        canvasWidthPx = canvasWidthPx,
                        canvasHeightPx = canvasHeightPx,
                        onTransform = { x, y, w, h ->
                            onSourceTransformChange(source.id, x, y, w, h)
                        }
                    )
                }
            }
        }

        // Top-Left Watermark / Scene Name
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(Color(0xAA000000), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "${activeScene.name.uppercase()} • PROGRAM",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun SourceCanvasItem(
    source: Source,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    onTransform: (Float, Float, Float, Float) -> Unit
) {
    var posX by remember(source.posX) { mutableFloatStateOf(source.posX) }
    var posY by remember(source.posY) { mutableFloatStateOf(source.posY) }

    val itemWidthDp = (source.width * canvasWidthPx / 2.7f).dp
    val itemHeightDp = (source.height * canvasHeightPx / 2.7f).dp

    val xOffsetPx = (posX * canvasWidthPx).roundToInt()
    val yOffsetPx = (posY * canvasHeightPx).roundToInt()

    Box(
        modifier = Modifier
            .offset { IntOffset(xOffsetPx, yOffsetPx) }
            .size(itemWidthDp, itemHeightDp)
            .then(
                if (!source.isLocked) {
                    Modifier.pointerInput(source.id) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            posX = (posX + dragAmount.x / canvasWidthPx).coerceIn(0f, 0.85f)
                            posY = (posY + dragAmount.y / canvasHeightPx).coerceIn(0f, 0.85f)
                            onTransform(posX, posY, source.width, source.height)
                        }
                    }
                } else Modifier
            )
            .then(
                if (!source.isLocked) {
                    Modifier.border(1.dp, ObsRedRec, RoundedCornerShape(6.dp))
                } else Modifier
            )
            .clip(RoundedCornerShape(6.dp))
            .background(
                when (source.type) {
                    SourceType.SCREEN_CAPTURE -> Color(0xFF181528)
                    SourceType.FRONT_CAMERA -> Color(0xFF281C3F)
                    SourceType.COLOR -> Color(android.graphics.Color.parseColor(source.colorHex.ifEmpty { "#7C3AED" }))
                    SourceType.TEXT -> Color(0xCC110F1C)
                    else -> Color(0xFF1E1B2E)
                }
            )
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        when (source.type) {
            SourceType.SCREEN_CAPTURE -> {
                Text(
                    text = "🖥 DISPLAY CAPTURE (1080p)",
                    color = Color(0xFFA5B4FC),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            SourceType.FRONT_CAMERA -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📷 FRONT CAM OVERLAY",
                        color = Color(0xFFE9D5FF),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            SourceType.TEXT -> {
                Text(
                    text = source.textContent,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            SourceType.COLOR -> {
                Text(
                    text = "COLOR MATTE",
                    color = Color.White,
                    fontSize = 10.sp
                )
            }
            else -> {
                Text(
                    text = source.name,
                    color = Color.White,
                    fontSize = 10.sp
                )
            }
        }
    }
}
