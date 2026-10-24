package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val birthDateMillis: Long = 895200000000L, // 1998-05-15 default (around 27-28 yrs old)
    val targetLifespanYears: Int = 90,
    val userName: String = "时间旅人",
    val gridStyle: String = "CLASSIC_INK",
    val showMilestonesOnGrid: Boolean = true
)
