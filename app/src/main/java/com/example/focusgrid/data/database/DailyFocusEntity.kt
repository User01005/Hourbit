package com.example.focusgrid.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_focus")
data class DailyFocusEntity(
    @PrimaryKey
    val date: String, // Format: YYYY-MM-DD
    val focusedMillis: Long,
    val sessionCount: Int
)
