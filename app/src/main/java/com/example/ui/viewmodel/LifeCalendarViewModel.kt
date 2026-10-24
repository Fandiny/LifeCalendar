package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LifeCalendarDatabase
import com.example.data.local.MilestoneEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.local.WeekNoteEntity
import com.example.data.model.GridStyle
import com.example.data.model.LifeQuotes
import com.example.data.model.LifeStats
import com.example.data.model.PhilosophicalQuote
import com.example.data.repository.LifeCalendarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

enum class MainTab(val title: String) {
    OVERVIEW("周历全景"),
    YEAR_FOCUS("当前年份"),
    PERSPECTIVE("时光透镜"),
    MILESTONES("人生长卷"),
    SETTINGS("时间档案")
}

data class SelectedWeekInfo(
    val yearIndex: Int,
    val weekIndex: Int,
    val totalWeekIndex: Int,
    val dateRange: String,
    val status: String, // PAST, CURRENT, FUTURE
    val milestone: MilestoneEntity?,
    val note: WeekNoteEntity?
)

data class LifeCalendarUiState(
    val settings: UserSettingsEntity = UserSettingsEntity(),
    val stats: LifeStats? = null,
    val milestones: List<MilestoneEntity> = emptyList(),
    val weekNotes: Map<String, WeekNoteEntity> = emptyMap(), // key: "$year-$week"
    val currentTab: MainTab = MainTab.OVERVIEW,
    val selectedWeek: SelectedWeekInfo? = null,
    val isWeekSheetOpen: Boolean = false,
    val isDatePickerOpen: Boolean = false,
    val isAddMilestoneOpen: Boolean = false,
    val selectedYearForFocus: Int = 0,
    val dailyQuote: PhilosophicalQuote = LifeQuotes.quotes.first(),
    val isFitToScreen: Boolean = true,
    val currentTotalWeekIndex: Int = 0
)

class LifeCalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LifeCalendarRepository

    private val _uiState = MutableStateFlow(LifeCalendarUiState())
    val uiState: StateFlow<LifeCalendarUiState> = _uiState.asStateFlow()

    init {
        val database = LifeCalendarDatabase.getDatabase(application)
        repository = LifeCalendarRepository(database.dao())

        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
        }

        viewModelScope.launch {
            combine(
                repository.userSettings,
                repository.milestones,
                repository.weekNotes
            ) { settings, milestones, notes ->
                Triple(settings ?: UserSettingsEntity(), milestones, notes)
            }.collect { (settings, milestones, notes) ->
                val now = System.currentTimeMillis()
                val stats = repository.calculateStats(
                    birthDateMillis = settings.birthDateMillis,
                    targetLifespanYears = settings.targetLifespanYears,
                    currentTimeMillis = now
                )

                val notesMap = notes.associateBy { "${it.yearIndex}-${it.weekIndex}" }

                _uiState.value = _uiState.value.copy(
                    settings = settings,
                    stats = stats,
                    milestones = milestones,
                    weekNotes = notesMap,
                    currentTotalWeekIndex = stats.passedWeeks,
                    selectedYearForFocus = if (_uiState.value.selectedYearForFocus == 0) stats.currentAgeYears.coerceIn(0, settings.targetLifespanYears - 1) else _uiState.value.selectedYearForFocus
                )
            }
        }
    }

    fun selectTab(tab: MainTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun toggleFitToScreen() {
        _uiState.value = _uiState.value.copy(isFitToScreen = !_uiState.value.isFitToScreen)
    }

    fun selectYearForFocus(year: Int) {
        _uiState.value = _uiState.value.copy(
            selectedYearForFocus = year.coerceIn(0, (_uiState.value.settings.targetLifespanYears - 1).coerceAtLeast(0)),
            currentTab = MainTab.YEAR_FOCUS
        )
    }

    fun onWeekClicked(yearIndex: Int, weekIndex: Int) {
        val state = _uiState.value
        val totalWeek = yearIndex * 52 + weekIndex
        val currentWeek = state.currentTotalWeekIndex

        val status = when {
            totalWeek < currentWeek -> "PAST"
            totalWeek == currentWeek -> "CURRENT"
            else -> "FUTURE"
        }

        val dateRange = repository.getWeekDateRange(
            birthDateMillis = state.settings.birthDateMillis,
            yearIndex = yearIndex,
            weekIndex = weekIndex
        )

        val milestone = state.milestones.find { it.yearIndex == yearIndex && it.weekIndex == weekIndex }
        val note = state.weekNotes["$yearIndex-$weekIndex"]

        _uiState.value = state.copy(
            selectedWeek = SelectedWeekInfo(
                yearIndex = yearIndex,
                weekIndex = weekIndex,
                totalWeekIndex = totalWeek,
                dateRange = dateRange,
                status = status,
                milestone = milestone,
                note = note
            ),
            isWeekSheetOpen = true
        )
    }

    fun closeWeekSheet() {
        _uiState.value = _uiState.value.copy(isWeekSheetOpen = false)
    }

    fun openDatePicker(open: Boolean) {
        _uiState.value = _uiState.value.copy(isDatePickerOpen = open)
    }

    fun openAddMilestone(open: Boolean) {
        _uiState.value = _uiState.value.copy(isAddMilestoneOpen = open)
    }

    fun updateBirthday(newBirthdayMillis: Long) {
        viewModelScope.launch {
            repository.updateBirthday(newBirthdayMillis)
            _uiState.value = _uiState.value.copy(isDatePickerOpen = false)
        }
    }

    fun updateLifespan(years: Int) {
        viewModelScope.launch {
            repository.updateLifespan(years)
        }
    }

    fun updateGridStyle(style: GridStyle) {
        viewModelScope.launch {
            repository.updateGridStyle(style.name)
        }
    }

    fun toggleMilestonesOnGrid() {
        viewModelScope.launch {
            val current = _uiState.value.settings.showMilestonesOnGrid
            repository.toggleMilestonesOnGrid(!current)
        }
    }

    fun saveWeekNote(yearIndex: Int, weekIndex: Int, note: String, mood: String) {
        viewModelScope.launch {
            if (note.isBlank()) {
                repository.deleteWeekNote(yearIndex, weekIndex)
            } else {
                repository.saveWeekNote(yearIndex, weekIndex, note, mood)
            }
            // Update selected week info note
            val updatedNote = repository.getWeekNote(yearIndex, weekIndex)
            _uiState.value = _uiState.value.copy(
                selectedWeek = _uiState.value.selectedWeek?.copy(note = updatedNote),
                isWeekSheetOpen = false
            )
        }
    }

    fun addMilestone(milestone: MilestoneEntity) {
        viewModelScope.launch {
            repository.addMilestone(milestone)
            _uiState.value = _uiState.value.copy(isAddMilestoneOpen = false)
        }
    }

    fun deleteMilestone(id: Long) {
        viewModelScope.launch {
            repository.deleteMilestone(id)
        }
    }

    fun shuffleQuote() {
        val quotes = LifeQuotes.quotes
        val nextQuote = quotes.filter { it != _uiState.value.dailyQuote }.randomOrNull() ?: quotes.first()
        _uiState.value = _uiState.value.copy(dailyQuote = nextQuote)
    }
}
