package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.LifeStages
import com.example.ui.theme.CurrentWeekGlow
import com.example.ui.viewmodel.LifeCalendarUiState
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun YearDetailScreen(
    state: LifeCalendarUiState,
    onYearChange: (Int) -> Unit,
    onWeekClick: (yearIndex: Int, weekIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentYear = state.selectedYearForFocus
    val totalYears = state.settings.targetLifespanYears
    val stage = remember(currentYear) { LifeStages.getStageForYear(currentYear) }

    val currentTotalWeek = state.currentTotalWeekIndex
    val currentAgeYear = state.stats?.currentAgeYears ?: 0

    // Calculate how many weeks passed in this year
    val weeksPassedThisYear = when {
        currentYear < currentAgeYear -> 52
        currentYear == currentAgeYear -> (state.stats?.currentYearWeeksPassed ?: 0).coerceIn(0, 52)
        else -> 0
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("year_detail_screen")
    ) {
        // Year Navigator Header
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (currentYear > 0) onYearChange(currentYear - 1) },
                        enabled = currentYear > 0,
                        modifier = Modifier.testTag("prev_year_button")
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "前一年")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$currentYear 岁",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(stage.color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${stage.name} · ${stage.subtitle}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = { if (currentYear < totalYears - 1) onYearChange(currentYear + 1) },
                        enabled = currentYear < totalYears - 1,
                        modifier = Modifier.testTag("next_year_button")
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "后一年")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar for this year's 52 weeks
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "本年 52 周进度",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$weeksPassedThisYear / 52 周 (${String.format("%.1f", weeksPassedThisYear / 52f * 100f)}%)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { (weeksPassedThisYear / 52f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // Season definition tabs: 春 (0..12), 夏 (13..25), 秋 (26..38), 冬 (39..51)
        val seasons = listOf(
            "春 · 萌芽 (1-13周)" to (0..12),
            "夏 · 炽热 (14-26周)" to (13..25),
            "秋 · 丰盈 (27-39周)" to (26..38),
            "冬 · 沉淀 (40-52周)" to (39..51)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            seasons.forEach { (seasonTitle, weekRange) ->
                item(key = seasonTitle) {
                    Text(
                        text = seasonTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(weekRange.toList(), key = { "$currentYear-$it" }) { weekIndex ->
                    val totalWeek = currentYear * 52 + weekIndex
                    val isCurrent = (totalWeek == currentTotalWeek)
                    val isPast = (totalWeek < currentTotalWeek)

                    val key = "$currentYear-$weekIndex"
                    val milestone = state.milestones.find { it.yearIndex == currentYear && it.weekIndex == weekIndex }
                    val note = state.weekNotes[key]

                    // Calculate dates
                    val weekStartMillis = state.settings.birthDateMillis + totalWeek * 7L * 24 * 3600 * 1000L
                    val weekEndMillis = weekStartMillis + 6L * 24 * 3600 * 1000L
                    val sdf = SimpleDateFormat("M月d日", Locale.CHINA)
                    val dateRangeStr = "${sdf.format(weekStartMillis)} ~ ${sdf.format(weekEndMillis)}"

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onWeekClick(currentYear, weekIndex) }
                            .testTag("year_week_card_$weekIndex"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isCurrent -> CurrentWeekGlow.copy(alpha = 0.12f)
                                isPast -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                else -> MaterialTheme.colorScheme.surface
                            }
                        ),
                        border = if (isCurrent) CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(CurrentWeekGlow)
                        ) else CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Status Indicator box
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            when {
                                                isCurrent -> CurrentWeekGlow
                                                isPast -> MaterialTheme.colorScheme.onSurface
                                                else -> Color.Transparent
                                            }
                                        )
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "第 ${weekIndex + 1} 周",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = dateRangeStr,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (milestone != null) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Stars,
                                                contentDescription = null,
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = milestone.title,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFEF4444)
                                            )
                                        }
                                    }

                                    if (note != null) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = note.note,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = "记录",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
