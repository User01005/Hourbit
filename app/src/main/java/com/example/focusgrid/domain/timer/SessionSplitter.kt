package com.example.focusgrid.domain.timer

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

data class SplitSessionDayChunk(
    val date: LocalDate,
    val dateString: String, // YYYY-MM-DD
    val durationMillis: Long,
    val isStartDay: Boolean
)

object SessionSplitter {

    fun splitSessionAcrossDates(
        startedAtEpochMillis: Long,
        endedAtEpochMillis: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<SplitSessionDayChunk> {
        if (endedAtEpochMillis <= startedAtEpochMillis) {
            return emptyList()
        }

        val startZdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(startedAtEpochMillis), zoneId)
        val endZdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(endedAtEpochMillis), zoneId)

        val chunks = mutableListOf<SplitSessionDayChunk>()

        var currentStart = startZdt
        var isFirst = true

        while (currentStart.isBefore(endZdt)) {
            val currentDate = currentStart.toLocalDate()
            val nextMidnight = currentDate.plusDays(1).atStartOfDay(zoneId)

            val chunkEnd = if (endZdt.isBefore(nextMidnight)) endZdt else nextMidnight
            val chunkDuration = chunkEnd.toInstant().toEpochMilli() - currentStart.toInstant().toEpochMilli()

            if (chunkDuration > 0) {
                chunks.add(
                    SplitSessionDayChunk(
                        date = currentDate,
                        dateString = currentDate.toString(),
                        durationMillis = chunkDuration,
                        isStartDay = isFirst
                    )
                )
            }

            isFirst = false
            currentStart = nextMidnight
        }

        return chunks
    }
}
