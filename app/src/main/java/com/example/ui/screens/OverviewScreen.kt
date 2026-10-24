package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GridStyle
import com.example.ui.components.LIFE_GRID_COLUMNS
import com.example.ui.components.LifeCalendarGrid
import com.example.ui.theme.CurrentWeekGlow
import com.example.ui.theme.InkFilledPast
import com.example.ui.theme.InkFilledPastDark
import com.example.ui.viewmodel.LifeCalendarUiState

@Composable
fun OverviewScreen(
    state: LifeCalendarUiState,
    onWeekClick: (yearIndex: Int, weekIndex: Int) -> Unit,
    onToggleFitToScreen: () -> Unit,
    onStyleChange: (GridStyle) -> Unit,
    onToggleMilestones: () -> Unit,
    onLocateCurrentWeek: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    var showStyleMenu by remember { mutableStateOf(false) }

    val currentGridStyle = remember(state.settings.gridStyle) {
        try {
            GridStyle.valueOf(state.settings.gridStyle)
        } catch (_: Exception) {
            GridStyle.CLASSIC_INK
        }
    }

    val totalYears = state.settings.targetLifespanYears
    val totalSquares = totalYears * LIFE_GRID_COLUMNS
    val passedWeeks = state.currentTotalWeekIndex.coerceIn(0, totalSquares)
    val futureWeeks = (totalSquares - passedWeeks - 1).coerceAtLeast(0)
    val currentAgeYear = passedWeeks / LIFE_GRID_COLUMNS
    val currentWeekOfYear = (passedWeeks % LIFE_GRID_COLUMNS) + 1

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("overview_screen")
    ) {
        // Top Info & Statistics Strip
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "已度过 ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$passedWeeks",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("passed_weeks_count")
                        )
                        Text(
                            text = " 周",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "  ·  当下 ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${currentAgeYear}岁第${currentWeekOfYear}周",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = CurrentWeekGlow,
                            modifier = Modifier.testTag("current_week_badge")
                        )
                        Text(
                            text = "  ·  余 ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$futureWeeks",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("remaining_weeks_count")
                        )
                        Text(
                            text = " 周",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "${LIFE_GRID_COLUMNS}×${totalYears} 人生周历（共 $totalSquares 个小方块 · 已涂黑 ${String.format("%.1f", state.stats?.passedPercentage ?: 0f)}%）",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                        modifier = Modifier.testTag("grid_summary_subtitle")
                    )
                }

                // Controls row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Fit vs Zoom mode toggle
                    IconButton(
                        onClick = onToggleFitToScreen,
                        modifier = Modifier.size(36.dp).testTag("fit_mode_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (state.isFitToScreen) Icons.Default.ZoomIn else Icons.Default.FitScreen,
                            contentDescription = if (state.isFitToScreen) "缩放模式" else "全景适应",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Style Switcher Menu
                    Box {
                        IconButton(
                            onClick = { showStyleMenu = true },
                            modifier = Modifier.size(36.dp).testTag("style_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "色彩风格",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showStyleMenu,
                            onDismissRequest = { showStyleMenu = false }
                        ) {
                            GridStyle.values().forEach { style ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(style.label, fontWeight = FontWeight.SemiBold)
                                            Text(style.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        onStyleChange(style)
                                        showStyleMenu = false
                                    },
                                    leadingIcon = {
                                        if (currentGridStyle == style) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Milestone overlay toggle
                    IconButton(
                        onClick = onToggleMilestones,
                        modifier = Modifier.size(36.dp).testTag("toggle_milestones_button")
                    ) {
                        Icon(
                            imageVector = if (state.settings.showMilestonesOnGrid) Icons.Default.Stars else Icons.Default.StarOutline,
                            contentDescription = "显示里程碑",
                            tint = if (state.settings.showMilestonesOnGrid) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Mode helper notice if in Zoom mode
        AnimatedVisibility(visible = !state.isFitToScreen) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "双指可捏合缩放与拖动画布，点击任意小方块可查看该周详情",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        // The 52x90 Grid of Squares
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            LifeCalendarGrid(
                currentWeekIndex = state.currentTotalWeekIndex,
                totalYears = totalYears,
                columns = LIFE_GRID_COLUMNS,
                gridStyle = currentGridStyle,
                showMilestones = state.settings.showMilestonesOnGrid,
                milestones = state.milestones,
                weekNotes = state.weekNotes,
                isFitToScreen = state.isFitToScreen,
                selectedYearIndex = state.selectedWeek?.yearIndex,
                selectedWeekIndex = state.selectedWeek?.weekIndex,
                onWeekClick = onWeekClick
            )
        }

        // Bottom Legend Bar showing Past (Filled), Current (Highlighted), Future (Outlined)
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth().testTag("grid_legend_bar")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Past item (Filled)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("legend_past_filled")
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isDark) InkFilledPastDark else InkFilledPast)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("过去已涂黑 ($passedWeeks)", style = MaterialTheme.typography.labelSmall)
                }

                // Current item (Highlighted)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("legend_current_highlighted")
                ) {
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(CurrentWeekGlow)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("当前周高亮 (1)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }

                // Future item (Outlined)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("legend_future_outlined")
                ) {
                    Surface(
                        modifier = Modifier.size(10.dp),
                        shape = RoundedCornerShape(2.dp),
                        border = CardDefaults.outlinedCardBorder()
                    ) {}
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("未来留白边框 ($futureWeeks)", style = MaterialTheme.typography.labelSmall)
                }

                // Milestone item
                if (state.settings.showMilestonesOnGrid) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("里程碑", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
