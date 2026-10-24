package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddMilestoneDialog
import com.example.ui.components.BirthdayPickerDialog
import com.example.ui.components.WeekDetailSheet
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.LifeCalendarViewModel
import com.example.ui.viewmodel.MainTab

class MainActivity : ComponentActivity() {

    private val viewModel: LifeCalendarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                // BackHandler: Return to OVERVIEW if on another tab
                BackHandler(enabled = uiState.currentTab != MainTab.OVERVIEW) {
                    viewModel.selectTab(MainTab.OVERVIEW)
                }

                Scaffold(
                    topBar = {
                        LifeCalendarTopBar(
                            currentTab = uiState.currentTab,
                            onBirthdayClick = { viewModel.openDatePicker(true) }
                        )
                    },
                    bottomBar = {
                        LifeCalendarBottomNav(
                            currentTab = uiState.currentTab,
                            onTabSelected = { viewModel.selectTab(it) }
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    when (uiState.currentTab) {
                        MainTab.OVERVIEW -> {
                            OverviewScreen(
                                state = uiState,
                                onWeekClick = { year, week -> viewModel.onWeekClicked(year, week) },
                                onToggleFitToScreen = { viewModel.toggleFitToScreen() },
                                onStyleChange = { viewModel.updateGridStyle(it) },
                                onToggleMilestones = { viewModel.toggleMilestonesOnGrid() },
                                onLocateCurrentWeek = {
                                    val currentAgeYear = uiState.stats?.currentAgeYears ?: 0
                                    viewModel.selectYearForFocus(currentAgeYear)
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        MainTab.YEAR_FOCUS -> {
                            YearDetailScreen(
                                state = uiState,
                                onYearChange = { viewModel.selectYearForFocus(it) },
                                onWeekClick = { year, week -> viewModel.onWeekClicked(year, week) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        MainTab.PERSPECTIVE -> {
                            PerspectiveScreen(
                                state = uiState,
                                onShuffleQuote = { viewModel.shuffleQuote() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        MainTab.MILESTONES -> {
                            MilestonesScreen(
                                state = uiState,
                                onAddMilestoneClick = { viewModel.openAddMilestone(true) },
                                onDeleteMilestone = { viewModel.deleteMilestone(it) },
                                onJumpToMilestoneYear = { viewModel.selectYearForFocus(it) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        MainTab.SETTINGS -> {
                            SettingsScreen(
                                state = uiState,
                                onOpenDatePicker = { viewModel.openDatePicker(true) },
                                onLifespanChange = { viewModel.updateLifespan(it) },
                                onStyleChange = { viewModel.updateGridStyle(it) },
                                onToggleMilestones = { viewModel.toggleMilestonesOnGrid() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }

                // Modal Week Inspector Sheet
                if (uiState.isWeekSheetOpen && uiState.selectedWeek != null) {
                    WeekDetailSheet(
                        weekInfo = uiState.selectedWeek!!,
                        onDismiss = { viewModel.closeWeekSheet() },
                        onSaveNote = { y, w, note, mood -> viewModel.saveWeekNote(y, w, note, mood) },
                        onJumpToYear = { y ->
                            viewModel.closeWeekSheet()
                            viewModel.selectYearForFocus(y)
                        }
                    )
                }

                // Birthday Date Picker Dialog
                if (uiState.isDatePickerOpen) {
                    BirthdayPickerDialog(
                        currentBirthMillis = uiState.settings.birthDateMillis,
                        onDismiss = { viewModel.openDatePicker(false) },
                        onDateSelected = { viewModel.updateBirthday(it) }
                    )
                }

                // Add Milestone Dialog
                if (uiState.isAddMilestoneOpen) {
                    AddMilestoneDialog(
                        currentAgeYears = uiState.stats?.currentAgeYears ?: 0,
                        targetLifespan = uiState.settings.targetLifespanYears,
                        onDismiss = { viewModel.openAddMilestone(false) },
                        onConfirm = { viewModel.addMilestone(it) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeCalendarTopBar(
    currentTab: MainTab,
    onBirthdayClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = currentTab.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            if (currentTab == MainTab.OVERVIEW) {
                IconButton(
                    onClick = onBirthdayClick,
                    modifier = Modifier.testTag("top_bar_birthday_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Cake,
                        contentDescription = "出生设置",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun LifeCalendarBottomNav(
    currentTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("main_bottom_nav")
    ) {
        NavigationBarItem(
            selected = currentTab == MainTab.OVERVIEW,
            onClick = { onTabSelected(MainTab.OVERVIEW) },
            icon = {
                Icon(
                    imageVector = if (currentTab == MainTab.OVERVIEW) Icons.Filled.GridOn else Icons.Outlined.GridOn,
                    contentDescription = MainTab.OVERVIEW.title
                )
            },
            label = { Text(MainTab.OVERVIEW.title) },
            modifier = Modifier.testTag("nav_overview")
        )

        NavigationBarItem(
            selected = currentTab == MainTab.YEAR_FOCUS,
            onClick = { onTabSelected(MainTab.YEAR_FOCUS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == MainTab.YEAR_FOCUS) Icons.Filled.CalendarViewWeek else Icons.Outlined.CalendarViewWeek,
                    contentDescription = MainTab.YEAR_FOCUS.title
                )
            },
            label = { Text(MainTab.YEAR_FOCUS.title) },
            modifier = Modifier.testTag("nav_year_focus")
        )

        NavigationBarItem(
            selected = currentTab == MainTab.PERSPECTIVE,
            onClick = { onTabSelected(MainTab.PERSPECTIVE) },
            icon = {
                Icon(
                    imageVector = if (currentTab == MainTab.PERSPECTIVE) Icons.Filled.HourglassBottom else Icons.Outlined.HourglassBottom,
                    contentDescription = MainTab.PERSPECTIVE.title
                )
            },
            label = { Text(MainTab.PERSPECTIVE.title) },
            modifier = Modifier.testTag("nav_perspective")
        )

        NavigationBarItem(
            selected = currentTab == MainTab.MILESTONES,
            onClick = { onTabSelected(MainTab.MILESTONES) },
            icon = {
                Icon(
                    imageVector = if (currentTab == MainTab.MILESTONES) Icons.Filled.Stars else Icons.Outlined.Stars,
                    contentDescription = MainTab.MILESTONES.title
                )
            },
            label = { Text(MainTab.MILESTONES.title) },
            modifier = Modifier.testTag("nav_milestones")
        )

        NavigationBarItem(
            selected = currentTab == MainTab.SETTINGS,
            onClick = { onTabSelected(MainTab.SETTINGS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == MainTab.SETTINGS) Icons.Filled.Tune else Icons.Outlined.Tune,
                    contentDescription = MainTab.SETTINGS.title
                )
            },
            label = { Text(MainTab.SETTINGS.title) },
            modifier = Modifier.testTag("nav_settings")
        )
    }
}
