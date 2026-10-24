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
import com.example.data.local.MilestoneEntity
import com.example.ui.viewmodel.LifeCalendarUiState

@Composable
fun MilestonesScreen(
    state: LifeCalendarUiState,
    onAddMilestoneClick: () -> Unit,
    onDeleteMilestone: (Long) -> Unit,
    onJumpToMilestoneYear: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, COMPLETED, GOALS

    val filteredMilestones = remember(state.milestones, selectedFilter) {
        when (selectedFilter) {
            "COMPLETED" -> state.milestones.filter { it.isCompleted }
            "GOALS" -> state.milestones.filter { !it.isCompleted }
            else -> state.milestones
        }.sortedBy { it.yearIndex * 52 + it.weekIndex }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddMilestoneClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("记录新里程碑") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_milestone_fab")
            )
        },
        modifier = modifier.testTag("milestones_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Filter Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("全部 (${state.milestones.size})") }
                )
                FilterChip(
                    selected = selectedFilter == "COMPLETED",
                    onClick = { selectedFilter = "COMPLETED" },
                    label = { Text("已走过成就 (${state.milestones.count { it.isCompleted }})") }
                )
                FilterChip(
                    selected = selectedFilter == "GOALS",
                    onClick = { selectedFilter = "GOALS" },
                    label = { Text("未来心愿 (${state.milestones.count { !it.isCompleted }})") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredMilestones.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.StarOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "暂无里程碑记录",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "点击右下角按钮，记录生命中的重要时刻或未来心愿",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredMilestones, key = { it.id }) { milestone ->
                        MilestoneCard(
                            milestone = milestone,
                            onDelete = { onDeleteMilestone(milestone.id) },
                            onClick = { onJumpToMilestoneYear(milestone.yearIndex) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MilestoneCard(
    milestone: MilestoneEntity,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val categoryIcon = when (milestone.category) {
        "BIRTH" -> Icons.Default.ChildCare
        "EDUCATION" -> Icons.Default.School
        "CAREER" -> Icons.Default.Work
        "LOVE" -> Icons.Default.Favorite
        "TRAVEL" -> Icons.Default.FlightTakeoff
        "ACHIEVEMENT" -> Icons.Default.EmojiEvents
        "GOAL" -> Icons.Default.Flag
        else -> Icons.Default.Stars
    }

    val iconColor = if (milestone.isCompleted) Color(0xFFEF4444) else Color(0xFF06B6D4)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("milestone_card_${milestone.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = milestone.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${milestone.yearIndex}岁·第${milestone.weekIndex + 1}周",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (milestone.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = milestone.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp).testTag("delete_milestone_${milestone.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
