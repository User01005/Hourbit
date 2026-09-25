package com.example.focusgrid.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyFocusDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailyFocus(dailyFocus: DailyFocusEntity)

    @Query("SELECT * FROM daily_focus WHERE date = :date")
    suspend fun getDailyFocus(date: String): DailyFocusEntity?

    @Query("SELECT * FROM daily_focus WHERE date = :date")
    fun getDailyFocusFlow(date: String): Flow<DailyFocusEntity?>

    @Query("SELECT * FROM daily_focus WHERE date >= :startDate AND date <= :endDate")
    fun getDailyFocusBetween(startDate: String, endDate: String): Flow<List<DailyFocusEntity>>

    @Query("SELECT * FROM daily_focus WHERE date >= :startDate AND date <= :endDate")
    suspend fun getDailyFocusBetweenList(startDate: String, endDate: String): List<DailyFocusEntity>

    @Query("SELECT * FROM daily_focus ORDER BY date DESC")
    fun getAllDailyFocus(): Flow<List<DailyFocusEntity>>

    @Query("SELECT SUM(focusedMillis) FROM daily_focus WHERE date >= :startDate AND date <= :endDate")
    fun getTotalFocusedMillisBetween(startDate: String, endDate: String): Flow<Long?>
}
