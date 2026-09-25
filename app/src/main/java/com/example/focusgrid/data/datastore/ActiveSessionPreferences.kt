package com.example.focusgrid.data.datastore

data class ActiveSessionPreferences(
    val status: String, // "IDLE", "RUNNING", "PAUSED", "FINISHING"
    val sessionId: Long,
    val startedAtEpochMillis: Long,
    val runningSinceEpochMillis: Long,
    val accumulatedMillis: Long,
    val pauseStartedAtEpochMillis: Long,
    val timezoneIdAtStart: String
) {
    fun calculateElapsedMillis(nowEpochMillis: Long = System.currentTimeMillis()): Long {
        return when (status) {
            "RUNNING" -> accumulatedMillis + maxOf(0L, nowEpochMillis - runningSinceEpochMillis)
            "PAUSED", "FINISHING" -> accumulatedMillis
            else -> 0L
        }
    }

    val isSessionActive: Boolean
        get() = status == "RUNNING" || status == "PAUSED"
}
