package com.example.focusgrid.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Query("SELECT * FROM focus_sessions WHERE localStartDate = :date ORDER BY startedAtEpochMillis DESC")
    fun getSessionsForDate(date: String): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE localStartDate >= :startDate AND localStartDate <= :endDate ORDER BY startedAtEpochMillis DESC")
    fun getSessionsBetween(startDate: String, endDate: String): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions ORDER BY startedAtEpochMillis DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions ORDER BY startedAtEpochMillis DESC LIMIT :limit OFFSET :offset")
    suspend fun getSessionsPaged(limit: Int, offset: Int): List<FocusSessionEntity>

    @Query("SELECT MAX(durationMillis) FROM focus_sessions WHERE localStartDate = :date")
    suspend fun getLongestSessionForDate(date: String): Long?
}
