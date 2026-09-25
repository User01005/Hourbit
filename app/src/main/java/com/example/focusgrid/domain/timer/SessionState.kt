package com.example.focusgrid.domain.timer

enum class SessionStatus {
    IDLE,
    RUNNING,
    PAUSED,
    FINISHING
}

data class TimerDisplayState(
    val status: SessionStatus,
    val elapsedMillis: Long,
    val cycleNumber: Int, // 0-based index of 25-minute cycle
    val cycleProgress: Float, // 0.0f to 1.0f
    val orientationIsInverted: Boolean, // True if odd cycle number (flipped 180 degrees)
    val formattedTime: String // "MM:SS" or "HH:MM:SS"
) {
    companion object {
        val IDLE = TimerDisplayState(
            status = SessionStatus.IDLE,
            elapsedMillis = 0L,
            cycleNumber = 0,
            cycleProgress = 0f,
            orientationIsInverted = false,
            formattedTime = "00:00"
        )

        fun formatDuration(totalMillis: Long): String {
            val totalSeconds = totalMillis / 1000L
            val hours = totalSeconds / 3600L
            val minutes = (totalSeconds % 3600L) / 60L
            val seconds = totalSeconds % 60L

            return if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }
    }
}
