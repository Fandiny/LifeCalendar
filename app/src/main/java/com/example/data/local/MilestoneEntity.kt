package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "milestones")
data class MilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val yearIndex: Int,      // 0..89
    val weekIndex: Int,      // 0..51
    val category: String,    // BIRTH, EDUCATION, CAREER, LOVE, TRAVEL, ACHIEVEMENT, GOAL, CUSTOM
    val dateDisplay: String,
    val note: String = "",
    val isCompleted: Boolean = true
)
