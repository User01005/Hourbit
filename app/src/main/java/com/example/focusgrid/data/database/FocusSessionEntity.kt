package com.example.focusgrid.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startedAtEpochMillis: Long,
    val endedAtEpochMillis: Long,
    val durationMillis: Long,
    val localStartDate: String,
    val localEndDate: String,
    val wasManuallyFinished: Boolean
)
