package com.example

import android.content.Context
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.ui.components.CurrentHighlightIndexKey
import com.example.ui.components.FutureOutlinedCountKey
import com.example.ui.components.GridColumnsKey
import com.example.ui.components.GridRowsKey
import com.example.ui.components.LIFE_GRID_COLUMNS
import com.example.ui.components.LIFE_GRID_DEFAULT_ROWS
import com.example.ui.components.LifeCalendarGrid
import com.example.ui.components.PastFilledCountKey
import com.example.ui.components.WeekSquareStatus
import com.example.ui.components.generateLifeGridCells
import com.example.ui.components.resolveWeekSquareStatus
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("人生日历", appName)
    }

    @Test
    fun `52x90 grid model classifies past filled, current highlighted, and future outlined squares`() {
        val currentWeek = 1400 // e.g., ~26 years and 48 weeks
        val cells = generateLifeGridCells(
            totalYears = LIFE_GRID_DEFAULT_ROWS,
            columns = LIFE_GRID_COLUMNS,
            currentWeekIndex = currentWeek
        )

        assertEquals(52 * 90, cells.size)
        assertEquals(1400, cells.count { it.status == WeekSquareStatus.PAST_FILLED })
        assertEquals(1, cells.count { it.status == WeekSquareStatus.CURRENT_HIGHLIGHTED })
        assertEquals(4680 - 1400 - 1, cells.count { it.status == WeekSquareStatus.FUTURE_OUTLINED })

        // Verify boundary transitions
        assertEquals(
            WeekSquareStatus.PAST_FILLED,
            resolveWeekSquareStatus(yearIndex = 0, weekIndex = 0, currentWeekIndex = currentWeek)
        )
        assertEquals(
            WeekSquareStatus.PAST_FILLED,
            resolveWeekSquareStatus(
                yearIndex = (currentWeek - 1) / 52,
                weekIndex = (currentWeek - 1) % 52,
                currentWeekIndex = currentWeek
            )
        )
        assertEquals(
            WeekSquareStatus.CURRENT_HIGHLIGHTED,
            resolveWeekSquareStatus(
                yearIndex = currentWeek / 52,
                weekIndex = currentWeek % 52,
                currentWeekIndex = currentWeek
            )
        )
        assertEquals(
            WeekSquareStatus.FUTURE_OUTLINED,
            resolveWeekSquareStatus(
                yearIndex = (currentWeek + 1) / 52,
                weekIndex = (currentWeek + 1) % 52,
                currentWeekIndex = currentWeek
            )
        )
        assertEquals(
            WeekSquareStatus.FUTURE_OUTLINED,
            resolveWeekSquareStatus(yearIndex = 89, weekIndex = 51, currentWeekIndex = currentWeek)
        )
    }

    @Test
    fun `LifeCalendarGrid renders 52x90 grid with expected semantics`() {
        val testCurrentWeek = 1040 // Exactly 20 years (20 * 52)
        composeTestRule.setContent {
            MyApplicationTheme {
                LifeCalendarGrid(
                    currentWeekIndex = testCurrentWeek,
                    totalYears = 90,
                    columns = 52,
                    modifier = Modifier.size(400.dp, 700.dp)
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("life_calendar_grid")
            .assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(GridColumnsKey, 52))
            .assert(SemanticsMatcher.expectValue(GridRowsKey, 90))
            .assert(SemanticsMatcher.expectValue(PastFilledCountKey, 1040))
            .assert(SemanticsMatcher.expectValue(CurrentHighlightIndexKey, 1040))
            .assert(SemanticsMatcher.expectValue(FutureOutlinedCountKey, 4680 - 1040 - 1))
    }
}
