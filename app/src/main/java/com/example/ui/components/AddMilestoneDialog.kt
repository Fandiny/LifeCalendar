package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.MilestoneEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMilestoneDialog(
    currentAgeYears: Int,
    targetLifespan: Int,
    onDismiss: () -> Unit,
    onConfirm: (MilestoneEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var yearText by remember { mutableStateOf(currentAgeYears.toString()) }
    var weekText by remember { mutableStateOf("1") }
    var selectedCategory by remember { mutableStateOf("ACHIEVEMENT") }
    var note by remember { mutableStateOf("") }
    var isCompleted by remember { mutableStateOf(true) }

    val categories = listOf(
        "ACHIEVEMENT" to "重要成就",
        "EDUCATION" to "求学历程",
        "CAREER" to "职场起航",
        "LOVE" to "情感家庭",
        "TRAVEL" to "远方旅途",
        "GOAL" to "心愿目标",
        "CUSTOM" to "特别纪念"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("记录人生里程碑") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("里程碑名称 (例如：大学毕业、创业启航)") },
                    modifier = Modifier.fillMaxWidth().testTag("milestone_title_input"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = yearText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) yearText = it },
                        label = { Text("年龄/岁数 (0-${targetLifespan - 1})") },
                        modifier = Modifier.weight(1f).testTag("milestone_age_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = weekText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) weekText = it },
                        label = { Text("第几周 (1-52)") },
                        modifier = Modifier.weight(1f).testTag("milestone_week_input"),
                        singleLine = true
                    )
                }

                Text("分类类型", style = MaterialTheme.typography.labelMedium)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            row.forEach { (catKey, catLabel) ->
                                FilterChip(
                                    selected = selectedCategory == catKey,
                                    onClick = {
                                        selectedCategory = catKey
                                        if (catKey == "GOAL") isCompleted = false
                                    },
                                    label = { Text(catLabel, style = MaterialTheme.typography.bodySmall) }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("是否已达成", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = isCompleted,
                        onCheckedChange = { isCompleted = it },
                        modifier = Modifier.testTag("milestone_completed_switch")
                    )
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备忘与感悟 (可选)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val year = (yearText.toIntOrNull() ?: currentAgeYears).coerceIn(0, targetLifespan - 1)
                    val week = ((weekText.toIntOrNull() ?: 1) - 1).coerceIn(0, 51)
                    if (title.isNotBlank()) {
                        onConfirm(
                            MilestoneEntity(
                                title = title.trim(),
                                yearIndex = year,
                                weekIndex = week,
                                category = selectedCategory,
                                dateDisplay = "${year}岁 第${week + 1}周",
                                note = note.trim(),
                                isCompleted = isCompleted
                            )
                        )
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("confirm_add_milestone_button")
            ) {
                Text("记录到日历")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
