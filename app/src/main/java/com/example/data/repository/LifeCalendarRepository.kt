package com.example.data.repository

import com.example.data.local.LifeCalendarDao
import com.example.data.local.MilestoneEntity
import com.example.data.local.UserSettingsEntity
import com.example.data.local.WeekNoteEntity
import com.example.data.model.LifeStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LifeCalendarRepository(private val dao: LifeCalendarDao) {

    val userSettings: Flow<UserSettingsEntity?> = dao.getUserSettings()
    val milestones: Flow<List<MilestoneEntity>> = dao.getAllMilestones()
    val weekNotes: Flow<List<WeekNoteEntity>> = dao.getAllWeekNotes()

    suspend fun initializeDefaultsIfNeeded() {
        val currentSettings = dao.getUserSettings().firstOrNull()
        if (currentSettings == null) {
            // Default: Born 1998-05-15 (a standard young adult age around 27-28 in 2026)
            val cal = Calendar.getInstance().apply {
                set(1998, Calendar.MAY, 15, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            dao.saveUserSettings(
                UserSettingsEntity(
                    id = 1,
                    birthDateMillis = cal.timeInMillis,
                    targetLifespanYears = 90,
                    userName = "时间旅人",
                    gridStyle = "CLASSIC_INK",
                    showMilestonesOnGrid = true
                )
            )
        }

        val existingMilestones = dao.getAllMilestones().firstOrNull()
        if (existingMilestones.isNullOrEmpty()) {
            val defaultMilestones = listOf(
                MilestoneEntity(
                    title = "初临人间",
                    yearIndex = 0,
                    weekIndex = 0,
                    category = "BIRTH",
                    dateDisplay = "生命起点",
                    note = "一声啼哭，开启属于我的4680周旅途",
                    isCompleted = true
                ),
                MilestoneEntity(
                    title = "走进小学校园",
                    yearIndex = 6,
                    weekIndex = 35,
                    category = "EDUCATION",
                    dateDisplay = "6岁秋",
                    note = "背起小书包，好奇地探索世界的规律",
                    isCompleted = true
                ),
                MilestoneEntity(
                    title = "高考与十八岁成人礼",
                    yearIndex = 18,
                    weekIndex = 23,
                    category = "ACHIEVEMENT",
                    dateDisplay = "18岁夏",
                    note = "告别青涩少年，独自走向更广阔的天地",
                    isCompleted = true
                ),
                MilestoneEntity(
                    title = "大学毕业与步入职场",
                    yearIndex = 22,
                    weekIndex = 26,
                    category = "CAREER",
                    dateDisplay = "22岁夏",
                    note = "怀揣理想与期许，开始书写自己的独立篇章",
                    isCompleted = true
                ),
                MilestoneEntity(
                    title = "登顶雪山 / 环球旅行",
                    yearIndex = 30,
                    weekIndex = 40,
                    category = "GOAL",
                    dateDisplay = "30岁心愿",
                    note = "去追逐极光与荒野，见证天地的浩瀚",
                    isCompleted = false
                ),
                MilestoneEntity(
                    title = "出版一本属于自己的书",
                    yearIndex = 40,
                    weekIndex = 20,
                    category = "GOAL",
                    dateDisplay = "40岁心愿",
                    note = "记录半生阅历与真诚思考",
                    isCompleted = false
                )
            )
            dao.insertMilestones(defaultMilestones)
        }
    }

    suspend fun updateBirthday(birthDateMillis: Long) {
        val current = dao.getUserSettings().firstOrNull() ?: UserSettingsEntity()
        dao.saveUserSettings(current.copy(birthDateMillis = birthDateMillis))
    }

    suspend fun updateLifespan(lifespanYears: Int) {
        val current = dao.getUserSettings().firstOrNull() ?: UserSettingsEntity()
        dao.saveUserSettings(current.copy(targetLifespanYears = lifespanYears))
    }

    suspend fun updateGridStyle(style: String) {
        val current = dao.getUserSettings().firstOrNull() ?: UserSettingsEntity()
        dao.saveUserSettings(current.copy(gridStyle = style))
    }

    suspend fun updateUserName(name: String) {
        val current = dao.getUserSettings().firstOrNull() ?: UserSettingsEntity()
        dao.saveUserSettings(current.copy(userName = name))
    }

    suspend fun toggleMilestonesOnGrid(show: Boolean) {
        val current = dao.getUserSettings().firstOrNull() ?: UserSettingsEntity()
        dao.saveUserSettings(current.copy(showMilestonesOnGrid = show))
    }

    suspend fun addMilestone(milestone: MilestoneEntity) {
        dao.insertMilestone(milestone)
    }

    suspend fun deleteMilestone(id: Long) {
        dao.deleteMilestoneById(id)
    }

    suspend fun saveWeekNote(yearIndex: Int, weekIndex: Int, note: String, mood: String) {
        dao.saveWeekNote(
            WeekNoteEntity(
                yearIndex = yearIndex,
                weekIndex = weekIndex,
                note = note,
                mood = mood,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteWeekNote(yearIndex: Int, weekIndex: Int) {
        dao.deleteWeekNote(yearIndex, weekIndex)
    }

    suspend fun getWeekNote(yearIndex: Int, weekIndex: Int): WeekNoteEntity? {
        return dao.getWeekNote(yearIndex, weekIndex)
    }

    fun calculateStats(birthDateMillis: Long, targetLifespanYears: Int, currentTimeMillis: Long): LifeStats {
        val totalWeeks = targetLifespanYears * 52
        val elapsedMillis = (currentTimeMillis - birthDateMillis).coerceAtLeast(0L)
        val elapsedDays = elapsedMillis / (1000L * 60 * 60 * 24)
        val passedWeeks = ((elapsedDays / 7)).toInt().coerceIn(0, totalWeeks)
        val remainingWeeks = (totalWeeks - passedWeeks).coerceAtLeast(0)
        val passedPercentage = if (totalWeeks > 0) (passedWeeks.toFloat() / totalWeeks * 100f) else 0f

        val totalDays = targetLifespanYears * 365L + (targetLifespanYears / 4)
        val remainingDays = (totalDays - elapsedDays).coerceAtLeast(0L)
        val remainingWeekends = (remainingWeeks * 2).toLong()

        val currentAgeYears = passedWeeks / 52
        val currentAgeWeeks = passedWeeks % 52
        val currentYearWeeksPassed = currentAgeWeeks

        val remainingYears = (targetLifespanYears - currentAgeYears).coerceAtLeast(0)
        val remainingSummers = remainingYears
        // Tim Urban's perspective: reading ~5 books a year on average
        val estimatedBooksRead = remainingYears * 5
        // Visiting aging parents ~1-2 times a year if living elsewhere
        val estimatedFamilyVisitsRemaining = remainingYears * 2

        return LifeStats(
            totalWeeks = totalWeeks,
            passedWeeks = passedWeeks,
            remainingWeeks = remainingWeeks,
            passedPercentage = passedPercentage,
            passedDays = elapsedDays,
            remainingDays = remainingDays,
            remainingWeekends = remainingWeekends,
            currentAgeYears = currentAgeYears,
            currentAgeWeeks = currentAgeWeeks,
            currentYearWeeksPassed = currentYearWeeksPassed,
            remainingSummers = remainingSummers,
            estimatedBooksRead = estimatedBooksRead,
            estimatedFamilyVisitsRemaining = estimatedFamilyVisitsRemaining
        )
    }

    fun getWeekDateRange(birthDateMillis: Long, yearIndex: Int, weekIndex: Int): String {
        val totalWeeksFromBirth = yearIndex * 52L + weekIndex
        val weekStartMillis = birthDateMillis + totalWeeksFromBirth * 7L * 24 * 3600 * 1000L
        val weekEndMillis = weekStartMillis + 6L * 24 * 3600 * 1000L

        val sdf = SimpleDateFormat("yyyy年M月d日", Locale.CHINA)
        return "${sdf.format(weekStartMillis)} ~ ${sdf.format(weekEndMillis)}"
    }
}
