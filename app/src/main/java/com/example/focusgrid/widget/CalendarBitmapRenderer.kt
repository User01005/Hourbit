package com.example.focusgrid.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.example.focusgrid.domain.heatmap.MonthlyCalendarCalculator
import com.example.focusgrid.domain.heatmap.MonthlyWidgetCell
import com.example.focusgrid.domain.heatmap.HeatLevel
import com.example.focusgrid.domain.theme.ThemeColorScheme
import java.time.LocalDate
import java.time.YearMonth

object CalendarBitmapRenderer {

    private val COLOR_BACKGROUND = Color.parseColor("#1B1B1B")
    private val COLOR_OUTSIDE_MONTH = Color.parseColor("#4C323232")
    private val COLOR_VALID_ZERO = Color.parseColor("#87323232")
    private val COLOR_SAND_PAUSED = Color.parseColor("#FF9800")
    private val COLOR_TEXT_PRIMARY = Color.parseColor("#F4F4F4")

    fun renderBitmap(
        widthPx: Int,
        heightPx: Int,
        yearMonth: YearMonth = YearMonth.now(),
        dailyFocusMap: Map<LocalDate, Long> = emptyMap(),
        today: LocalDate = LocalDate.now(),
        colorScheme: ThemeColorScheme = ThemeColorScheme.RED
    ): Bitmap {
        val w = maxOf(100, widthPx)
        val h = maxOf(100, heightPx)

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawColor(COLOR_BACKGROUND)

        val gridCells = MonthlyCalendarCalculator.calculateGrid(yearMonth, dailyFocusMap, today)

        // Equal padding from all sides
        val padding = maxOf(12f, minOf(w, h) * 0.035f)
        val availableW = w - (padding * 2f)
        val availableH = h - (padding * 2f)

        val cols = 7
        val rows = 6
        // Increased horizontal and vertical spacing between cells
        val gap = maxOf(8f, minOf(availableW, availableH) * 0.028f)

        // Cell size is reduced proportionally to accommodate the increased spacing
        val cellW = (availableW - (cols - 1) * gap) / cols
        val cellH = (availableH - (rows - 1) * gap) / rows

        val cellRadius = minOf(cellW, cellH) * 0.22f

        val startX = padding
        val startY = padding

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        val strokeWidth = maxOf(2.5f, cellW * 0.12f)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
        }

        val lowColor = Color.parseColor(colorScheme.colorLowHex)
        val mediumColor = Color.parseColor(colorScheme.colorMediumHex)
        val highColor = Color.parseColor(colorScheme.colorHighHex)
        val veryHighColor = Color.parseColor(colorScheme.colorVeryHighHex)

        for (cell in gridCells) {
            val col = cell.gridIndex % cols
            val row = cell.gridIndex / cols

            val left = startX + col * (cellW + gap)
            val top = startY + row * (cellH + gap)
            val right = left + cellW
            val bottom = top + cellH

            val rect = RectF(left, top, right, bottom)

            if (!cell.isCurrentMonth) {
                strokePaint.color = COLOR_OUTSIDE_MONTH
                val halfStroke = strokeWidth / 2f
                val strokeRect = RectF(
                    left + halfStroke,
                    top + halfStroke,
                    right - halfStroke,
                    bottom - halfStroke
                )
                val strokedRadius = maxOf(0f, cellRadius - halfStroke)
                canvas.drawRoundRect(strokeRect, strokedRadius, strokedRadius, strokePaint)
            } else {
                val fillColor = when (cell.heatLevel) {
                    HeatLevel.NONE -> COLOR_VALID_ZERO
                    HeatLevel.LOW -> lowColor
                    HeatLevel.MEDIUM -> mediumColor
                    HeatLevel.HIGH -> highColor
                    HeatLevel.VERY_HIGH -> veryHighColor
                }

                fillPaint.color = fillColor
                canvas.drawRoundRect(rect, cellRadius, cellRadius, fillPaint)

                // Today/active date does NOT have white outline per user request
            }
        }

        return bitmap
    }

    fun renderTimerBitmap(
        widthPx: Int,
        heightPx: Int,
        status: String,
        elapsedMillis: Long,
        todayTotalMillis: Long = 0L,
        colorScheme: ThemeColorScheme = ThemeColorScheme.RED
    ): Bitmap {
        val w = maxOf(140, widthPx)
        val h = maxOf(140, heightPx)

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawColor(COLOR_BACKGROUND)

        val isRunning = status == "RUNNING"
        val sandColor = Color.parseColor(colorScheme.colorVeryHighHex)

        // 25-minute Pomodoro cycle (1500000 ms)
        val cycleMs = 25 * 60 * 1000L
        val safeElapsed = maxOf(0L, elapsedMillis)
        val cycleElapsed = safeElapsed % cycleMs
        val progress = (cycleElapsed.toFloat() / cycleMs.toFloat()).coerceIn(0f, 1f)

        val totalSeconds = safeElapsed / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        val timeStr = if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }

        val minDim = minOf(w, h).toFloat()

        // --- 1. HOURGLASS SECTION (top section) ---
        val topPadding = 10f
        val availableHeight = (h * 0.50f).coerceIn(44f, h - 54f)
        val maxAvailableWidth = minOf(w - 24f, availableHeight * 1.18f)
        val hgWidth = maxAvailableWidth * 0.52f
        val hgHeight = availableHeight * 0.82f
        val centerX = w / 2f
        val centerY = topPadding + availableHeight / 2f
        val hgSectionBottom = topPadding + availableHeight

        // --- HOURGLASS GEOMETRY & PHYSICS (identical to SmoothPixelHourglass.kt) ---
        canvas.save()
        canvas.translate(centerX - hgWidth / 2f, centerY - hgHeight / 2f)

        val width = hgWidth
        val height = hgHeight
        val hgCenterX = width / 2f

        val topCapY = height * 0.055f
        val bottomCapY = height * 0.89f
        val capHeight = height * 0.065f
        val capInset = width * 0.055f

        val chamberLeft = width * 0.15f
        val chamberRight = width * 0.85f

        val topChamberY = topCapY + capHeight * 0.62f
        val bottomChamberY = bottomCapY + capHeight * 0.38f

        val neckY = height * 0.50f
        val neckHalfWidth = width * 0.04f

        val frameStroke = width * 0.034f
        val glassStroke = width * 0.012f

        val topPath = Path().apply {
            moveTo(chamberLeft, topChamberY)
            lineTo(chamberRight, topChamberY)
            lineTo(hgCenterX + neckHalfWidth, neckY)
            lineTo(hgCenterX - neckHalfWidth, neckY)
            close()
        }

        val bottomPath = Path().apply {
            moveTo(hgCenterX - neckHalfWidth, neckY)
            lineTo(hgCenterX + neckHalfWidth, neckY)
            lineTo(chamberRight, bottomChamberY)
            lineTo(chamberLeft, bottomChamberY)
            close()
        }

        val fullInteriorPath = Path().apply {
            addPath(topPath)
            addPath(bottomPath)
        }

        // --- SAND VOLUME CALCULATIONS ---
        val remaining = 1f - progress
        val topHeight = neckY - topChamberY
        val bottomHeight = bottomChamberY - neckY

        val upperHeightFraction = kotlin.math.sqrt(remaining)
        val upperSurfaceY = neckY - topHeight * upperHeightFraction
        val lowerSurfaceY = neckY + bottomHeight * kotlin.math.sqrt(remaining)

        val sandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = sandColor
        }

        // Clip interior sand inside glass geometry
        canvas.save()
        canvas.clipPath(fullInteriorPath)

        // 1. Upper Draining Sand
        canvas.save()
        canvas.clipPath(topPath)
        if (progress < 1f) {
            canvas.drawRect(
                0f,
                upperSurfaceY,
                width,
                neckY,
                sandPaint
            )
        }
        canvas.restore()

        // 2. Lower Accumulated Sand with Mound
        canvas.save()
        canvas.clipPath(bottomPath)
        if (progress > 0f) {
            canvas.drawRect(
                0f,
                lowerSurfaceY,
                width,
                bottomChamberY,
                sandPaint
            )

            val moundWidth = width * (0.06f + progress * 0.18f)
            val moundHeight = height * (0.008f + progress * 0.025f)

            val moundPath = Path().apply {
                moveTo(
                    hgCenterX - moundWidth,
                    lowerSurfaceY + moundHeight
                )
                quadTo(
                    hgCenterX,
                    lowerSurfaceY - moundHeight,
                    hgCenterX + moundWidth,
                    lowerSurfaceY + moundHeight
                )
                lineTo(
                    hgCenterX + moundWidth,
                    lowerSurfaceY + moundHeight * 1.5f
                )
                lineTo(
                    hgCenterX - moundWidth,
                    lowerSurfaceY + moundHeight * 1.5f
                )
                close()
            }
            canvas.drawPath(moundPath, sandPaint)
        }
        canvas.restore()

        // 3. Flowing Sand Stream & Impact Settling
        if (isRunning && progress > 0.001f && progress < 0.999f) {
            val travelDistance = lowerSurfaceY - neckY
            if (travelDistance > 0f) {
                // Continuous smooth trickle stream from neck to lower surface
                val streamPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = maxOf(1.8f, width * 0.035f)
                    strokeCap = Paint.Cap.ROUND
                    color = sandColor
                }
                canvas.drawLine(hgCenterX, neckY, hgCenterX, lowerSurfaceY, streamPaint)

                // Contact & settling merge at the lower sand surface
                val pixelSize = width * 0.045f
                val mergeWidth = pixelSize * 1.8f
                val mergePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    color = sandColor
                }
                val mergeRect = RectF(
                    hgCenterX - mergeWidth / 2f,
                    lowerSurfaceY - pixelSize * 0.10f,
                    hgCenterX + mergeWidth / 2f,
                    lowerSurfaceY + pixelSize * 0.24f
                )
                canvas.drawRoundRect(mergeRect, pixelSize * 0.5f, pixelSize * 0.5f, mergePaint)
            }
        }

        canvas.restore() // Restore interior clip

        // --- GLASS FRAME & HIGHLIGHTS ---
        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = frameStroke
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            color = Color.parseColor("#E6E6E6")
        }

        canvas.drawPath(topPath, framePaint)
        canvas.drawPath(bottomPath, framePaint)

        val glassHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = glassStroke
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            color = Color.parseColor("#26FFFFFF")
        }

        canvas.drawPath(topPath, glassHighlightPaint)
        canvas.drawPath(bottomPath, glassHighlightPaint)

        val capPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#E6E6E6")
        }

        val capRadius = capHeight / 2f
        val topCapRect = RectF(capInset, topCapY, width - capInset, topCapY + capHeight)
        canvas.drawRoundRect(topCapRect, capRadius, capRadius, capPaint)

        val bottomCapRect = RectF(capInset, bottomCapY, width - capInset, bottomCapY + capHeight)
        canvas.drawRoundRect(bottomCapRect, capRadius, capRadius, capPaint)

        canvas.restore() // Restore translate and rotate

        // --- 2. RUNNING TIMER TEXT: Pulled close to the Hourglass ---
        val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_PRIMARY
            textSize = maxOf(18f, minDim * 0.165f)
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.MONOSPACE
        }
        val timeFontMetrics = timePaint.fontMetrics
        val timeTextY = hgSectionBottom + 16f - timeFontMetrics.ascent
        canvas.drawText(timeStr, w / 2f, timeTextY, timePaint)

        // --- 3. BOTTOM TEXT: TODAY (positioned exactly 12px from bottom, tight kerning) ---
        val combinedTodayMillis = todayTotalMillis + safeElapsed
        val totalFormatted = formatTodayDuration(combinedTodayMillis)

        val totalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#8A8A8A")
            textSize = maxOf(9.5f, minDim * 0.072f)
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.MONOSPACE
            letterSpacing = -0.01f
        }
        val totalY = h - 12f - totalPaint.fontMetrics.descent
        canvas.drawText("TODAY · $totalFormatted", w / 2f, totalY, totalPaint)

        return bitmap
    }

    private fun formatTodayDuration(durationMillis: Long): String {
        val totalMinutes = maxOf(0L, durationMillis) / (60 * 1000L)
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60
        return when {
            hours == 1L -> "1 hr, ${mins} mins"
            hours > 1L -> "${hours} hrs, ${mins} mins"
            else -> "${mins} mins"
        }
    }
}
