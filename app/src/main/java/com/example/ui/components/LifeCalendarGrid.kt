package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.data.local.MilestoneEntity
import com.example.data.local.WeekNoteEntity
import com.example.data.model.GridStyle
import com.example.data.model.LifeStages
import com.example.ui.theme.CurrentWeekGlow
import com.example.ui.theme.FutureWeekBorderDark
import com.example.ui.theme.FutureWeekBorderLight
import com.example.ui.theme.InkFilledPast
import com.example.ui.theme.InkFilledPastDark
import kotlin.math.min

const val LIFE_GRID_COLUMNS = 52
const val LIFE_GRID_DEFAULT_ROWS = 90

/**
 * Visual state of each square in the 52x90 Life Calendar grid.
 */
enum class WeekSquareStatus {
    PAST_FILLED,
    CURRENT_HIGHLIGHTED,
    FUTURE_OUTLINED
}

/**
 * Immutable model representing a single square in the 52x90 grid.
 */
data class WeekSquareCell(
    val yearIndex: Int,
    val weekIndex: Int,
    val totalWeekIndex: Int,
    val status: WeekSquareStatus
)

/**
 * Pure helper to determine the status of a square at (yearIndex, weekIndex)
 * within the 52x90 grid.
 */
fun resolveWeekSquareStatus(
    yearIndex: Int,
    weekIndex: Int,
    currentWeekIndex: Int
): WeekSquareStatus {
    val totalIndex = yearIndex * LIFE_GRID_COLUMNS + weekIndex
    return when {
        totalIndex < currentWeekIndex -> WeekSquareStatus.PAST_FILLED
        totalIndex == currentWeekIndex -> WeekSquareStatus.CURRENT_HIGHLIGHTED
        else -> WeekSquareStatus.FUTURE_OUTLINED
    }
}

/**
 * Generates the 52x90 grid state for verification and inspection.
 */
fun generateLifeGridCells(
    totalYears: Int = LIFE_GRID_DEFAULT_ROWS,
    columns: Int = LIFE_GRID_COLUMNS,
    currentWeekIndex: Int
): List<WeekSquareCell> {
    val totalCells = totalYears * columns
    return List(totalCells) { index ->
        val year = index / columns
        val week = index % columns
        WeekSquareCell(
            yearIndex = year,
            weekIndex = week,
            totalWeekIndex = index,
            status = resolveWeekSquareStatus(year, week, currentWeekIndex)
        )
    }
}

val GridColumnsKey = SemanticsPropertyKey<Int>("GridColumns")
val GridRowsKey = SemanticsPropertyKey<Int>("GridRows")
val PastFilledCountKey = SemanticsPropertyKey<Int>("PastFilledCount")
val CurrentHighlightIndexKey = SemanticsPropertyKey<Int>("CurrentHighlightIndex")
val FutureOutlinedCountKey = SemanticsPropertyKey<Int>("FutureOutlinedCount")

var SemanticsPropertyReceiver.gridColumns by GridColumnsKey
var SemanticsPropertyReceiver.gridRows by GridRowsKey
var SemanticsPropertyReceiver.pastFilledCount by PastFilledCountKey
var SemanticsPropertyReceiver.currentHighlightIndex by CurrentHighlightIndexKey
var SemanticsPropertyReceiver.futureOutlinedCount by FutureOutlinedCountKey

/**
 * A Compose UI component that renders a 52x90 grid of squares representing a human life in weeks:
 * - Past weeks (< currentWeekIndex) are filled.
 * - The current week (== currentWeekIndex) is highlighted.
 * - Future weeks (> currentWeekIndex) remain outlined.
 */
@Composable
fun LifeCalendarGrid(
    currentWeekIndex: Int,
    modifier: Modifier = Modifier,
    totalYears: Int = LIFE_GRID_DEFAULT_ROWS,
    columns: Int = LIFE_GRID_COLUMNS,
    gridStyle: GridStyle = GridStyle.CLASSIC_INK,
    showMilestones: Boolean = true,
    milestones: List<MilestoneEntity> = emptyList(),
    weekNotes: Map<String, WeekNoteEntity> = emptyMap(),
    isFitToScreen: Boolean = true,
    selectedYearIndex: Int? = null,
    selectedWeekIndex: Int? = null,
    onWeekClick: (yearIndex: Int, weekIndex: Int) -> Unit = { _, _ -> }
) {
    val isDark = isSystemInDarkTheme()
    val rows = totalYears.coerceAtLeast(1)
    val cols = columns.coerceAtLeast(1)
    val totalSquares = rows * cols
    val clampedCurrentWeek = currentWeekIndex.coerceIn(0, (totalSquares - 1).coerceAtLeast(0))

    val pastFilledSquares = clampedCurrentWeek.coerceIn(0, totalSquares)
    val futureOutlinedSquares = (totalSquares - clampedCurrentWeek - 1).coerceAtLeast(0)

    // Zoom and pan state when not in fit-to-screen mode
    var scale by remember { mutableFloatStateOf(1.0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Palette
    val pastFillColor = if (isDark) InkFilledPastDark else InkFilledPast
    val futureBorderColor = if (isDark) FutureWeekBorderDark else FutureWeekBorderLight
    val futureFillColor = if (isDark) Color(0xFF141A24) else Color(0xFFFFFFFF)
    val labelTextColor = if (isDark) android.graphics.Color.parseColor("#94A3B8") else android.graphics.Color.parseColor("#64748B")
    val gridLineColor = if (isDark) Color(0x22FFFFFF) else Color(0x15000000)

    val milestoneMap = remember(milestones) {
        milestones.associateBy { "${it.yearIndex}-${it.weekIndex}" }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("life_calendar_grid")
            .semantics {
                contentDescription = "52x$rows 人生周历网格：过去已涂黑 $pastFilledSquares 周，当前高亮第 ${clampedCurrentWeek + 1} 周，未来未涂黑边框 $futureOutlinedSquares 周"
                gridColumns = cols
                gridRows = rows
                pastFilledCount = pastFilledSquares
                currentHighlightIndex = clampedCurrentWeek
                futureOutlinedCount = futureOutlinedSquares
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("life_calendar_canvas")
                .pointerInput(isFitToScreen, rows, cols, scale, offset) {
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
                .pointerInput(isFitToScreen, rows, cols, scale, offset) {
                    detectTapGestures { tapOffset ->
                        val leftMargin = 36f * (if (isFitToScreen) 1f else scale)
                        val topMargin = 24f * (if (isFitToScreen) 1f else scale)

                        val canvasWidth = size.width.toFloat()
                        val canvasHeight = size.height.toFloat()

                        val availableWidth = (canvasWidth - leftMargin - 12f).coerceAtLeast(1f)
                        val availableHeight = (canvasHeight - topMargin - 12f).coerceAtLeast(1f)

                        val cellStepX: Float
                        val cellStepY: Float
                        val startX: Float
                        val startY: Float

                        if (isFitToScreen) {
                            val cellStep = min(availableWidth / cols, availableHeight / rows)
                            cellStepX = cellStep
                            cellStepY = cellStep
                            val gridTotalWidth = cellStep * cols
                            val gridTotalHeight = cellStep * rows
                            startX = leftMargin + ((availableWidth - gridTotalWidth) / 2f).coerceAtLeast(0f)
                            startY = topMargin + ((availableHeight - gridTotalHeight) / 2f).coerceAtLeast(0f)
                        } else {
                            val baseSize = (canvasWidth - 44f) / cols
                            cellStepX = baseSize * scale
                            cellStepY = baseSize * scale
                            startX = leftMargin + offset.x
                            startY = topMargin + offset.y
                        }

                        val col = ((tapOffset.x - startX) / cellStepX).toInt()
                        val row = ((tapOffset.y - startY) / cellStepY).toInt()

                        if (col in 0 until cols && row in 0 until rows) {
                            onWeekClick(row, col)
                        }
                    }
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val leftMargin = 36f * (if (isFitToScreen) 1f else scale)
            val topMargin = 24f * (if (isFitToScreen) 1f else scale)

            val availableWidth = (canvasWidth - leftMargin - 12f).coerceAtLeast(1f)
            val availableHeight = (canvasHeight - topMargin - 12f).coerceAtLeast(1f)

            val cellStepX: Float
            val cellStepY: Float
            val startX: Float
            val startY: Float

            if (isFitToScreen) {
                val cellStep = min(availableWidth / cols, availableHeight / rows)
                cellStepX = cellStep
                cellStepY = cellStep
                val gridTotalWidth = cellStep * cols
                val gridTotalHeight = cellStep * rows
                startX = leftMargin + ((availableWidth - gridTotalWidth) / 2f).coerceAtLeast(0f)
                startY = topMargin + ((availableHeight - gridTotalHeight) / 2f).coerceAtLeast(0f)
            } else {
                val baseSize = (canvasWidth - 44f) / cols
                cellStepX = baseSize * scale
                cellStepY = baseSize * scale
                startX = leftMargin + offset.x
                startY = topMargin + offset.y
            }

            // Geometric square side length and gap
            val cellGap = (min(cellStepX, cellStepY) * 0.20f).coerceIn(0.6f, 3.5f)
            val squareSize = (min(cellStepX, cellStepY) - cellGap).coerceAtLeast(1.5f)
            val cornerRad = (squareSize * 0.18f).coerceIn(0.4f, 3.5f)

            // Header & Axis Labels
            val textPaint = android.graphics.Paint().apply {
                color = labelTextColor
                textSize = (if (isFitToScreen) 8.5f else 11.5f * scale).coerceIn(7f, 20f)
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
            }

            val agePaint = android.graphics.Paint().apply {
                color = labelTextColor
                textSize = (if (isFitToScreen) 8.5f else 11.5f * scale).coerceIn(6.5f, 20f)
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.RIGHT
            }

            // Week column markers (1, 13, 26, 39, 52)
            val quarterWeeks = listOf(0 to "1", 12 to "13", 25 to "26", 38 to "39", 51 to "52")
            for ((wIdx, label) in quarterWeeks) {
                if (wIdx < cols) {
                    val x = startX + wIdx * cellStepX + squareSize / 2f
                    val y = startY - 6f
                    if (x in 0f..canvasWidth && y in 0f..canvasHeight) {
                        drawContext.canvas.nativeCanvas.drawText("W$label", x, y, textPaint)
                    }
                }
            }

            // Decade markers and age axis
            for (row in 0 until rows) {
                val y = startY + row * cellStepY
                val showAgeLabel = if (isFitToScreen) (row % 10 == 0 || row == rows - 1) else (row % 5 == 0)
                if (showAgeLabel && y in -20f..(canvasHeight + 20f)) {
                    val label = "${row}岁"
                    val labelX = startX - 6f
                    val labelY = y + squareSize / 2f + agePaint.textSize / 3f
                    drawContext.canvas.nativeCanvas.drawText(label, labelX, labelY, agePaint)
                }

                if (row > 0 && row % 10 == 0) {
                    val lineY = y - cellGap / 2f
                    drawLine(
                        color = gridLineColor,
                        start = Offset(startX, lineY),
                        end = Offset(startX + cols * cellStepX, lineY),
                        strokeWidth = 0.8f
                    )
                }
            }

            // Render all 52 x 90 squares
            for (row in 0 until rows) {
                val y = startY + row * cellStepY
                if (!isFitToScreen && (y + cellStepY < 0f || y > canvasHeight)) continue

                val stage = LifeStages.getStageForYear(row)

                for (col in 0 until cols) {
                    val x = startX + col * cellStepX
                    if (!isFitToScreen && (x + cellStepX < 0f || x > canvasWidth)) continue

                    val status = resolveWeekSquareStatus(row, col, clampedCurrentWeek)
                    val key = "$row-$col"
                    val hasMilestone = showMilestones && milestoneMap.containsKey(key)
                    val hasNote = weekNotes.containsKey(key)
                    val isSelected = (selectedYearIndex == row && selectedWeekIndex == col)

                    when (status) {
                        WeekSquareStatus.PAST_FILLED -> {
                            // Past weeks: FILLED square
                            val fillCol = when (gridStyle) {
                                GridStyle.CLASSIC_INK -> pastFillColor
                                GridStyle.LIFE_STAGES -> stage.color.copy(alpha = 0.92f)
                                GridStyle.GOLDEN_HOUR -> {
                                    val progress = (row.toFloat() / rows.toFloat()).coerceIn(0f, 1f)
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
                                size = Size(squareSize, squareSize),
                                cornerRadius = CornerRadius(cornerRad, cornerRad),
                                style = Fill
                            )
                        }

                        WeekSquareStatus.CURRENT_HIGHLIGHTED -> {
                            // Current week: HIGHLIGHTED square with outer halo + filled bright core
                            drawRoundRect(
                                color = CurrentWeekGlow.copy(alpha = 0.28f),
                                topLeft = Offset(x - 2f, y - 2f),
                                size = Size(squareSize + 4f, squareSize + 4f),
                                cornerRadius = CornerRadius(cornerRad + 1.5f, cornerRad + 1.5f),
                                style = Fill
                            )
                            drawRoundRect(
                                color = CurrentWeekGlow,
                                topLeft = Offset(x, y),
                                size = Size(squareSize, squareSize),
                                cornerRadius = CornerRadius(cornerRad, cornerRad),
                                style = Fill
                            )
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.9f),
                                topLeft = Offset(x - 1f, y - 1f),
                                size = Size(squareSize + 2f, squareSize + 2f),
                                cornerRadius = CornerRadius(cornerRad + 1f, cornerRad + 1f),
                                style = Stroke(width = 1.4f)
                            )
                        }

                        WeekSquareStatus.FUTURE_OUTLINED -> {
                            // Future weeks: OUTLINED square
                            val borderCol = when (gridStyle) {
                                GridStyle.CLASSIC_INK -> futureBorderColor
                                GridStyle.LIFE_STAGES -> stage.color.copy(alpha = 0.45f)
                                GridStyle.GOLDEN_HOUR -> Color(0xFF64748B).copy(alpha = 0.45f)
                            }
                            drawRoundRect(
                                color = futureFillColor,
                                topLeft = Offset(x, y),
                                size = Size(squareSize, squareSize),
                                cornerRadius = CornerRadius(cornerRad, cornerRad),
                                style = Fill
                            )
                            drawRoundRect(
                                color = borderCol,
                                topLeft = Offset(x, y),
                                size = Size(squareSize, squareSize),
                                cornerRadius = CornerRadius(cornerRad, cornerRad),
                                style = Stroke(
                                    width = (0.85f * (if (isFitToScreen) 1f else scale * 0.8f)).coerceIn(0.65f, 2f)
                                )
                            )
                        }
                    }

                    // Selected square outline indicator
                    if (isSelected) {
                        drawRoundRect(
                            color = Color(0xFF38BDF8),
                            topLeft = Offset(x - 1.5f, y - 1.5f),
                            size = Size(squareSize + 3f, squareSize + 3f),
                            cornerRadius = CornerRadius(cornerRad + 1.5f, cornerRad + 1.5f),
                            style = Stroke(width = 1.8f)
                        )
                    }

                    // Milestone or Note indicator dot
                    if (hasMilestone) {
                        val milestone = milestoneMap[key]
                        val pinColor = if (milestone?.isCompleted == true) Color(0xFFEF4444) else Color(0xFF06B6D4)
                        val pinRadius = (squareSize * 0.30f).coerceIn(1.2f, 4.5f)
                        drawCircle(
                            color = pinColor,
                            radius = pinRadius,
                            center = Offset(x + squareSize / 2f, y + squareSize / 2f)
                        )
                    } else if (hasNote) {
                        val noteDotRadius = (squareSize * 0.22f).coerceIn(1f, 3.2f)
                        drawCircle(
                            color = if (status == WeekSquareStatus.PAST_FILLED) Color(0xFF38BDF8) else CurrentWeekGlow,
                            radius = noteDotRadius,
                            center = Offset(x + squareSize / 2f, y + squareSize / 2f)
                        )
                    }
                }
            }
        }
    }
}
