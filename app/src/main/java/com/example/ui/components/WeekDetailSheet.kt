package com.example.ui.components

import androidx.compose.foundation.background
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
import com.example.data.model.LifeStages
import com.example.ui.theme.CurrentWeekGlow
import com.example.ui.viewmodel.SelectedWeekInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekDetailSheet(
    weekInfo: SelectedWeekInfo,
    onDismiss: () -> Unit,
    onSaveNote: (yearIndex: Int, weekIndex: Int, note: String, mood: String) -> Unit,
    onJumpToYear: (yearIndex: Int) -> Unit
) {
    var noteText by remember(weekInfo) { mutableStateOf(weekInfo.note?.note ?: "") }
    var selectedMood by remember(weekInfo) { mutableStateOf(weekInfo.note?.mood ?: "PEACE") }
    var isEditing by remember { mutableStateOf(weekInfo.note != null) }

    val stage = remember(weekInfo.yearIndex) {
        LifeStages.getStageForYear(weekInfo.yearIndex)
    }

    val statusBadgeColor = when (weekInfo.status) {
        "CURRENT" -> CurrentWeekGlow
        "PAST" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.secondary
    }

    val statusLabel = when (weekInfo.status) {
        "CURRENT" -> "跳动的当下 · 此周即是人生"
        "PAST" -> "已逝岁月 · 铸就今天的你"
        else -> "尚未展开 · 充满无限可能"
    }

    val moods = listOf(
        "PEACE" to "平和",
        "HAPPY" to "喜悦",
        "HARD" to "沉潜",
        "MEMORABLE" to "铭刻",
        "HOPE" to "期盼"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("week_detail_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            // Header Row: Age & Week & Status Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${weekInfo.yearIndex} 岁 · 第 ${weekInfo.weekIndex + 1} 周",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = weekInfo.dateRange,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = statusBadgeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(statusBadgeColor)
                    )
                ) {
                    Text(
                        text = if (weekInfo.status == "CURRENT") "当前周" else if (weekInfo.status == "PAST") "已涂黑" else "未到来",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusBadgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Life Stage & Meaning Banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(stage.color)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${stage.name} (${stage.subtitle})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stage.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Milestone Banner if present
            if (weekInfo.milestone != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEF4444).copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stars,
                            contentDescription = "里程碑",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "里程碑：${weekInfo.milestone.title}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (weekInfo.milestone.note.isNotBlank()) {
                                Text(
                                    text = weekInfo.milestone.note,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Week Journal / Reflection
            Text(
                text = if (weekInfo.status == "PAST") "这一周的记忆与回响" else if (weekInfo.status == "CURRENT") "记录当下这一周的感悟" else "对未来这一周的寄语/目标",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mood Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                moods.forEach { (moodKey, moodLabel) ->
                    val isSelected = selectedMood == moodKey
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedMood = moodKey },
                        label = { Text(moodLabel, fontSize = 12.sp) },
                        modifier = Modifier.testTag("mood_chip_$moodKey")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("week_note_input"),
                placeholder = {
                    Text(
                        if (weekInfo.status == "PAST") "写下在这一周里发生的故事、心情或感悟..."
                        else if (weekInfo.status == "CURRENT") "把握今天，这一周你想专注完成什么？"
                        else "许下一个愿望或目标..."
                    )
                },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onJumpToYear(weekInfo.yearIndex)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("view_year_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("查看这整年")
                }

                Button(
                    onClick = {
                        onSaveNote(weekInfo.yearIndex, weekInfo.weekIndex, noteText, selectedMood)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_week_note_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("保存记录")
                }
            }
        }
    }
}
