package com.example.data.local

import androidx.room.Entity

@Entity(
    tableName = "week_notes",
    primaryKeys = ["yearIndex", "weekIndex"]
)
data class WeekNoteEntity(
    val yearIndex: Int,
    val weekIndex: Int,
    val note: String,
    val mood: String = "PEACE", // HAPPY, PEACE, HARD, MEMORABLE, HOPE
    val updatedAt: Long = System.currentTimeMillis()
)
