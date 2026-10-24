package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.example.data.local.MilestoneEntity
import com.example.data.local.WeekNoteEntity
import com.example.data.model.GridStyle
import com.example.data.model.LifeStages
import com.example.ui.theme.*

@Composable
fun LifeCalendarCanvas(
    totalYears: Int,
    currentWeekIndex: Int, // Total weeks lived from birth (0 .. totalYears*52 - 1)
    gridStyle: GridStyle,
    showMilestones: Boolean,
    milestones: List<MilestoneEntity>,
    weekNotes: Map<String, WeekNoteEntity>,
    isFitToScreen: Boolean,
    onWeekClick: (yearIndex: Int, weekIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Zoom and pan state when not in fit-to-screen mode
    var scale by remember { mutableFloatStateOf(1.0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Colors
    val pastFillColor = if (isDark) InkFilledPastDark else InkFilledPast
    val futureBorderColor = if (isDark) FutureWeekBorderDark else FutureWeekBorderLight
    val futureFillColor = if (isDark) Color(0xFF141A24) else Color(0xFFFFFFFF)
    val labelTextColor = if (isDark) android.graphics.Color.parseColor("#94A3B8") else android.graphics.Color.parseColor("#64748B")
    val gridLineColor = if (isDark) Color(0x22FFFFFF) else Color(0x15000000)

    val milestoneMap = remember(milestones) {
        milestones.associateBy { "${it.yearIndex}-${it.weekIndex}" }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isFitToScreen, totalYears, scale, offset) {
                    if (!isFitToScreen) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1.0f, 6.0f)
                            offset = Offset(
                                x = (offset.x + pan.x).coerceIn(-1500f * scale, 500f),
                                y = (offset.y + pan.y).coerceIn(-3000f * scale, 500f)
                            )
                        }
                    }
                }
                .pointerInput(isFitToScreen, totalYears, scale, offset) {
                    detectTapGestures { tapOffset ->
                        val leftMargin = 38f * (if (isFitToScreen) 1f else scale)
                        val topMargin = 28f * (if (isFitToScreen) 1f else scale)

                        val canvasWidth = size.width.toFloat()
                        val canvasHeight = size.height.toFloat()

                        val availableWidth = (canvasWidth - leftMargin - 16f)
                        val availableHeight = (canvasHeight - topMargin - 16f)

                        val cols = 52
                        val rows = totalYears.coerceAtLeast(1)

                        val colWidth: Float
                        val rowHeight: Float

                        if (isFitToScreen) {
                            colWidth = availableWidth / cols
                            rowHeight = availableHeight / rows
                        } else {
                            val baseSize = (canvasWidth - 40f) / 52f
                            colWidth = baseSize * scale
                            rowHeight = baseSize * scale
                        }

                        val adjustedX = if (isFitToScreen) tapOffset.x else (tapOffset.x - offset.x)
                        val adjustedY = if (isFitToScreen) tapOffset.y else (tapOffset.y - offset.y)

                        val col = ((adjustedX - leftMargin) / colWidth).toInt()
                        val row = ((adjustedY - topMargin) / rowHeight).toInt()

                        if (col in 0 until 52 && row in 0 until totalYears) {
                            onWeekClick(row, col)
                        }
                    }
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val leftMargin = 38f * (if (isFitToScreen) 1f else scale)
            val topMargin = 28f * (if (isFitToScreen) 1f else scale)

            val cols = 52
            val rows = totalYears.coerceAtLeast(1)

            val availableWidth = canvasWidth - leftMargin - 16f
            val availableHeight = canvasHeight - topMargin - 16f

            val colWidth: Float
            val rowHeight: Float

            if (isFitToScreen) {
                colWidth = availableWidth / cols
                rowHeight = availableHeight / rows
            } else {
                val baseSize = (canvasWidth - 40f) / 52f
                colWidth = baseSize * scale
                rowHeight = baseSize * scale
            }

            // Cell geometry: padding between squares
            val cellPaddingX = (colWidth * 0.18f).coerceIn(0.5f, 3.5f)
            val cellPaddingY = (rowHeight * 0.18f).coerceIn(0.5f, 3.5f)
            val squareWidth = (colWidth - cellPaddingX).coerceAtLeast(1.5f)
            val squareHeight = (rowHeight - cellPaddingY).coerceAtLeast(1.5f)
            val cornerRad = (squareWidth * 0.22f).coerceIn(0.5f, 4f)

            // Draw header labels (Quarter weeks: W1, W13, W26, W39, W52)
            val textPaint = android.graphics.Paint().apply {
                color = labelTextColor
                textSize = (if (isFitToScreen) 9f else 12f * scale).coerceIn(7f, 22f)
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
            }

            val agePaint = android.graphics.Paint().apply {
                color = labelTextColor
                textSize = (if (isFitToScreen) 8.5f else 11.5f * scale).coerceIn(6.5f, 20f)
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.RIGHT
            }

            // Top week markers: 1, 13, 26, 39, 52
            val quarterWeeks = listOf(0 to "W1", 12 to "W13", 25 to "W26", 38 to "W39", 51 to "W52")
            for ((wIdx, label) in quarterWeeks) {
                val x = leftMargin + (if (!isFitToScreen) offset.x else 0f) + wIdx * colWidth + colWidth / 2f
                val y = topMargin + (if (!isFitToScreen) offset.y else 0f) - 6f
                if (x in 0f..canvasWidth && y in 0f..canvasHeight) {
                    drawContext.canvas.nativeCanvas.drawText(label, x, y, textPaint)
                }
            }

            // Draw decade & life stage marker lines and ages
            for (row in 0 until rows) {
                val y = topMargin + (if (!isFitToScreen) offset.y else 0f) + row * rowHeight

                // Draw age labels every 5 or 10 years
                val showAgeLabel = if (isFitToScreen) (row % 10 == 0 || row == rows - 1) else (row % 5 == 0)
                if (showAgeLabel && y in -20f..(canvasHeight + 20f)) {
                    val label = "${row}岁"
                    val labelX = leftMargin + (if (!isFitToScreen) offset.x else 0f) - 6f
                    val labelY = y + rowHeight / 2f + textPaint.textSize / 3f
                    drawContext.canvas.nativeCanvas.drawText(label, labelX, labelY, agePaint)
                }

                // Subtle decade separator line
                if (row > 0 && row % 10 == 0) {
                    val lineY = y - cellPaddingY / 2f
                    drawLine(
                        color = gridLineColor,
                        start = Offset(leftMargin + (if (!isFitToScreen) offset.x else 0f), lineY),
                        end = Offset(leftMargin + (if (!isFitToScreen) offset.x else 0f) + cols * colWidth, lineY),
                        strokeWidth = 1f
                    )
                }
            }

            // Render all 52 * totalYears squares
            for (row in 0 until rows) {
                val y = topMargin + (if (!isFitToScreen) offset.y else 0f) + row * rowHeight
                // Skip off-screen rows when zoomed
                if (!isFitToScreen && (y + rowHeight < 0 || y > canvasHeight)) continue

                val stage = LifeStages.getStageForYear(row)

                for (col in 0 until cols) {
                    val x = leftMargin + (if (!isFitToScreen) offset.x else 0f) + col * colWidth
                    if (!isFitToScreen && (x + colWidth < 0 || x > canvasWidth)) continue

                    val totalIndex = row * 52 + col
                    val key = "$row-$col"
                    val hasMilestone = showMilestones && milestoneMap.containsKey(key)
                    val hasNote = weekNotes.containsKey(key)

                    val isCurrent = (totalIndex == currentWeekIndex)
                    val isPast = (totalIndex < currentWeekIndex)

                    when {
                        isCurrent -> {
                            // Pulsing golden/amber square for CURRENT WEEK
                            val glowColor = CurrentWeekGlow.copy(alpha = pulseAlpha)
                            drawRoundRect(
                                color = glowColor,
                                topLeft = Offset(x, y),
                                size = Size(squareWidth, squareHeight),
                                cornerRadius = CornerRadius(cornerRad, cornerRad),
                                style = Fill
                            )
                            // Outer glowing ring
                            drawRoundRect(
                                color = CurrentWeekGlow,
                                topLeft = Offset(x - 1.5f, y - 1.5f),
                                size = Size(squareWidth + 3f, squareHeight + 3f),
                                cornerRadius = CornerRadius(cornerRad + 1.5f, cornerRad + 1.5f),
                                style = Stroke(width = 1.8f)
                            )
                        }

                        isPast -> {
                            // PAST WEEKS (涂黑 / Filled)
                            val fillCol = when (gridStyle) {
                                GridStyle.CLASSIC_INK -> pastFillColor
                                GridStyle.LIFE_STAGES -> stage.color.copy(alpha = 0.92f)
                                GridStyle.GOLDEN_HOUR -> {
                                    val progress = (row.toFloat() / rows.toFloat()).coerceIn(0f, 1f)
                                    // Golden hour shift from warm amber to violet dusk
                                    Color(
                                        red = 0.85f - progress * 0.25f,
                                        green = 0.55f - progress * 0.35f,
                                        blue = 0.25f + progress * 0.45f,
                                        alpha = 0.95f
                                    )
                                }
                            }
                            drawRoundRect(
                                color = fillCol,
                                topLeft = Offset(x, y),
                                size = Size(squareWidth, squareHeight),
                                cornerRadius = CornerRadius(cornerRad, cornerRad),
                                style = Fill
                            )
                        }

                        else -> {
                            // FUTURE WEEKS (Unfilled outline)
                            val borderCol = when (gridStyle) {
                                GridStyle.CLASSIC_INK -> futureBorderColor
                                GridStyle.LIFE_STAGES -> stage.color.copy(alpha = 0.4f)
                                GridStyle.GOLDEN_HOUR -> Color(0xFF64748B).copy(alpha = 0.4f)
                            }
                            // Subtle background fill
                            drawRoundRect(
                                color = futureFillColor,
                                topLeft = Offset(x, y),
                                size = Size(squareWidth, squareHeight),
                                cornerRadius = CornerRadius(cornerRad, cornerRad),
                                style = Fill
                            )
                            drawRoundRect(
                                color = borderCol,
                                topLeft = Offset(x, y),
                                size = Size(squareWidth, squareHeight),
                                cornerRadius = CornerRadius(cornerRad, cornerRad),
                                style = Stroke(width = (0.8f * (if (isFitToScreen) 1f else scale * 0.8f)).coerceIn(0.6f, 2f))
                            )
                        }
                    }

                    // Milestone marker pin (Crimson or Cyan badge)
                    if (hasMilestone) {
                        val milestone = milestoneMap[key]
                        val pinColor = if (milestone?.isCompleted == true) Color(0xFFEF4444) else Color(0xFF06B6D4)
                        val pinRadius = (squareWidth * 0.32f).coerceIn(1.5f, 5f)
                        drawCircle(
                            color = pinColor,
                            radius = pinRadius,
                            center = Offset(x + squareWidth / 2f, y + squareHeight / 2f)
                        )
                        // White dot center
                        drawCircle(
                            color = Color.White,
                            radius = pinRadius * 0.45f,
                            center = Offset(x + squareWidth / 2f, y + squareHeight / 2f)
                        )
                    } else if (hasNote) {
                        // User note indicator dot
                        val noteDotRadius = (squareWidth * 0.22f).coerceIn(1f, 3.5f)
                        drawCircle(
                            color = if (isPast) Color(0xFF38BDF8) else Color(0xFFF59E0B),
                            radius = noteDotRadius,
                            center = Offset(x + squareWidth / 2f, y + squareHeight / 2f)
                        )
                    }
                }
            }
        }
    }
}
