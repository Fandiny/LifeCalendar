package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LifeCalendarDao {
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getUserSettings(): Flow<UserSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserSettings(settings: UserSettingsEntity)

    @Query("SELECT * FROM milestones ORDER BY yearIndex ASC, weekIndex ASC")
    fun getAllMilestones(): Flow<List<MilestoneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: MilestoneEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<MilestoneEntity>)

    @Delete
    suspend fun deleteMilestone(milestone: MilestoneEntity)

    @Query("DELETE FROM milestones WHERE id = :id")
    suspend fun deleteMilestoneById(id: Long)

    @Query("SELECT * FROM week_notes")
    fun getAllWeekNotes(): Flow<List<WeekNoteEntity>>

    @Query("SELECT * FROM week_notes WHERE yearIndex = :year AND weekIndex = :week LIMIT 1")
    suspend fun getWeekNote(year: Int, week: Int): WeekNoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWeekNote(weekNote: WeekNoteEntity)

    @Query("DELETE FROM week_notes WHERE yearIndex = :year AND weekIndex = :week")
    suspend fun deleteWeekNote(year: Int, week: Int)
}
