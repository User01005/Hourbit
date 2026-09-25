package com.example.focusgrid.domain.heatmap

object HeatmapClassifier {

    fun classify(focusedMinutes: Long): HeatLevel {
        return when {
            focusedMinutes <= 0L -> HeatLevel.NONE
            focusedMinutes <= 50L -> HeatLevel.LOW
            focusedMinutes <= 124L -> HeatLevel.MEDIUM
            focusedMinutes <= 200L -> HeatLevel.HIGH
            else -> HeatLevel.VERY_HIGH
        }
    }

    fun classifyMillis(focusedMillis: Long): HeatLevel {
        val minutes = focusedMillis / (60 * 1000L)
        return classify(minutes)
    }
}
