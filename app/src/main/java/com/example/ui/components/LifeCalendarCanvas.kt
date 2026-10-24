package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.data.local.MilestoneEntity
import com.example.data.local.WeekNoteEntity
import com.example.data.model.GridStyle

@Composable
fun LifeCalendarCanvas(
    totalYears: Int,
    currentWeekIndex: Int,
    gridStyle: GridStyle,
    showMilestones: Boolean,
    milestones: List<MilestoneEntity>,
    weekNotes: Map<String, WeekNoteEntity>,
    isFitToScreen: Boolean,
    onWeekClick: (yearIndex: Int, weekIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    selectedYearIndex: Int? = null,
    selectedWeekIndex: Int? = null
) {
    LifeCalendarGrid(
        currentWeekIndex = currentWeekIndex,
        totalYears = totalYears,
        columns = LIFE_GRID_COLUMNS,
        gridStyle = gridStyle,
        showMilestones = showMilestones,
        milestones = milestones,
        weekNotes = weekNotes,
        isFitToScreen = isFitToScreen,
        selectedYearIndex = selectedYearIndex,
        selectedWeekIndex = selectedWeekIndex,
        onWeekClick = onWeekClick,
        modifier = modifier
    )
}
