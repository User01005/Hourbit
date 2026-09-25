package com.example.focusgrid.ui.hourglass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import kotlinx.coroutines.isActive
import kotlin.math.pow
import kotlin.math.sqrt

private const val CYCLE_DURATION_MS = 25L * 60L * 1_000L
private const val GRAIN_DROP_DURATION_MS = 460L

private val HourglassFrame = Color(0xFFE6E6E6)
private val HourglassSand = Color(0xFFFF1414)
private val GlassHighlight = Color(0x26FFFFFF)

/**
 * Smooth, frame-driven pixel hourglass.
 *
 * currentElapsedMillis must calculate and return the current authoritative
 * session duration whenever it is called.
 *
 * Do not pass a value that changes only once per second.
 */
@Composable
fun SmoothPixelHourglass(
    currentElapsedMillis: () -> Long,
    isRunning: Boolean,
    modifier: Modifier = Modifier,
    frameColor: Color = HourglassFrame,
    sandColor: Color = HourglassSand
) {
    val latestElapsedProvider by rememberUpdatedState(currentElapsedMillis)

    var renderedElapsedMillis by remember {
        mutableLongStateOf(
            currentElapsedMillis().coerceAtLeast(0L)
        )
    }

    /*
     * This is the critical smoothness fix.
     *
     * The elapsed value is sampled once per rendered display frame instead
     * of relying on a one-second timer or repeated delay(16) calls.
     */
    LaunchedEffect(isRunning) {
        if (!isRunning) {
            renderedElapsedMillis =
                latestElapsedProvider().coerceAtLeast(0L)

            return@LaunchedEffect
        }

        while (isActive) {
            withFrameNanos {
                renderedElapsedMillis =
                    latestElapsedProvider().coerceAtLeast(0L)
            }
        }
    }

    val cycleIndex = renderedElapsedMillis / CYCLE_DURATION_MS

    var previousCycleIndex by remember {
        mutableLongStateOf(cycleIndex)
    }

    var isFlipping by remember {
        mutableStateOf(false)
    }

    val flipRotation = remember {
        Animatable(0f)
    }

    /*
     * At a cycle boundary, freeze the geometry at "bottom full," rotate it,
     * then replace it with the next cycle's "top full" geometry.
     *
     * These two states look identical at the hand-off, avoiding a flash.
     */
    LaunchedEffect(cycleIndex) {
        when {
            cycleIndex == previousCycleIndex -> Unit

            cycleIndex == previousCycleIndex + 1L -> {
                isFlipping = true

                flipRotation.snapTo(0f)

                flipRotation.animateTo(
                    targetValue = 180f,
                    animationSpec = tween(
                        durationMillis = 520,
                        easing = FastOutSlowInEasing
                    )
                )

                flipRotation.snapTo(0f)
                previousCycleIndex = cycleIndex
                isFlipping = false
            }

            else -> {
                // App returned after missing one or more complete cycles.
                flipRotation.snapTo(0f)
                previousCycleIndex = cycleIndex
                isFlipping = false
            }
        }
    }

    val rawCycleProgress =
        (renderedElapsedMillis % CYCLE_DURATION_MS).toFloat() /
            CYCLE_DURATION_MS.toFloat()

    val displayedProgress = if (isFlipping) {
        1f
    } else {
        rawCycleProgress.coerceIn(0f, 1f)
    }

    val grainPhase =
        (renderedElapsedMillis % GRAIN_DROP_DURATION_MS).toFloat() /
            GRAIN_DROP_DURATION_MS.toFloat()

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerX = width / 2f

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
            lineTo(centerX + neckHalfWidth, neckY)
            lineTo(centerX - neckHalfWidth, neckY)
            close()
        }

        val bottomPath = Path().apply {
            moveTo(centerX - neckHalfWidth, neckY)
            lineTo(centerX + neckHalfWidth, neckY)
            lineTo(chamberRight, bottomChamberY)
            lineTo(chamberLeft, bottomChamberY)
            close()
        }

        val fullInteriorPath = Path().apply {
            addPath(topPath)
            addPath(bottomPath)
        }

        rotate(
            degrees = flipRotation.value,
            pivot = center
        ) {
            val progress = displayedProgress.coerceIn(0f, 1f)
            val remaining = 1f - progress

            val topHeight = neckY - topChamberY
            val bottomHeight = bottomChamberY - neckY

            /*
             * Triangle area changes with height squared.
             *
             * sqrt() makes the visible sand volume decrease consistently
             * instead of looking like it suddenly disappears.
             */
            val upperHeightFraction = sqrt(remaining)
            val upperSurfaceY =
                neckY - topHeight * upperHeightFraction

            /*
             * Lower fill occupies the lower part of a triangular chamber.
             *
             * For a filled fraction p:
             *
             * surfacePosition = sqrt(1 - p)
             */
            val lowerSurfaceY =
                neckY + bottomHeight * sqrt(remaining)

            clipPath(fullInteriorPath) {
                /*
                 * Upper sand is one continuous fill anchored toward the neck.
                 * It cannot remain stuck to the top cap.
                 */
                clipPath(topPath) {
                    if (progress < 1f) {
                        drawRect(
                            color = sandColor,
                            topLeft = Offset(
                                x = 0f,
                                y = upperSurfaceY
                            ),
                            size = Size(
                                width = width,
                                height = (
                                    neckY - upperSurfaceY
                                    ).coerceAtLeast(0f)
                            )
                        )
                    }
                }

                /*
                 * Bottom sand is one continuous accumulated region anchored
                 * to the chamber floor.
                 */
                clipPath(bottomPath) {
                    if (progress > 0f) {
                        drawRect(
                            color = sandColor,
                            topLeft = Offset(
                                x = 0f,
                                y = lowerSurfaceY
                            ),
                            size = Size(
                                width = width,
                                height = (
                                    bottomChamberY - lowerSurfaceY
                                    ).coerceAtLeast(0f)
                            )
                        )

                        /*
                         * A subtle mound makes the landing position feel less
                         * mathematically flat while remaining part of the
                         * continuous lower fill.
                         */
                        val moundWidth =
                            width * (0.06f + progress * 0.18f)

                        val moundHeight =
                            height * (0.008f + progress * 0.025f)

                        val moundPath = Path().apply {
                            moveTo(
                                centerX - moundWidth,
                                lowerSurfaceY + moundHeight
                            )

                            quadraticTo(
                                centerX,
                                lowerSurfaceY - moundHeight,
                                centerX + moundWidth,
                                lowerSurfaceY + moundHeight
                            )

                            lineTo(
                                centerX + moundWidth,
                                lowerSurfaceY + moundHeight * 1.5f
                            )

                            lineTo(
                                centerX - moundWidth,
                                lowerSurfaceY + moundHeight * 1.5f
                            )

                            close()
                        }

                        drawPath(
                            path = moundPath,
                            color = sandColor
                        )
                    }
                }

                drawFallingPixel(
                    progress = progress,
                    phase = grainPhase,
                    isRunning = isRunning && !isFlipping,
                    centerX = centerX,
                    neckY = neckY,
                    lowerSurfaceY = lowerSurfaceY,
                    hourglassWidth = width,
                    hourglassHeight = height,
                    sandColor = sandColor
                )
            }

            /*
             * Glass chamber outlines.
             */
            drawPath(
                path = topPath,
                color = frameColor,
                style = Stroke(
                    width = frameStroke,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            drawPath(
                path = bottomPath,
                color = frameColor,
                style = Stroke(
                    width = frameStroke,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            /*
             * Subtle internal glass highlight.
             */
            drawPath(
                path = topPath,
                color = GlassHighlight,
                style = Stroke(
                    width = glassStroke,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            drawPath(
                path = bottomPath,
                color = GlassHighlight,
                style = Stroke(
                    width = glassStroke,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            /*
             * Rounded upper cap.
             */
            drawRoundRect(
                color = frameColor,
                topLeft = Offset(
                    x = capInset,
                    y = topCapY
                ),
                size = Size(
                    width = width - capInset * 2f,
                    height = capHeight
                ),
                cornerRadius = CornerRadius(
                    x = capHeight / 2f,
                    y = capHeight / 2f
                )
            )

            /*
             * Rounded lower cap.
             */
            drawRoundRect(
                color = frameColor,
                topLeft = Offset(
                    x = capInset,
                    y = bottomCapY
                ),
                size = Size(
                    width = width - capInset * 2f,
                    height = capHeight
                ),
                cornerRadius = CornerRadius(
                    x = capHeight / 2f,
                    y = capHeight / 2f
                )
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFallingPixel(
    progress: Float,
    phase: Float,
    isRunning: Boolean,
    centerX: Float,
    neckY: Float,
    lowerSurfaceY: Float,
    hourglassWidth: Float,
    hourglassHeight: Float,
    sandColor: Color
) {
    if (!isRunning) return
    if (progress <= 0.001f || progress >= 0.999f) return

    val pixelSize = hourglassWidth * 0.045f
    val startY = neckY + pixelSize * 0.25f
    val landingY = lowerSurfaceY - pixelSize

    if (landingY <= startY + pixelSize) return

    /*
     * One drop consists of:
     *
     * 0%–82%: accelerating fall
     * 82%–100%: contact and visual merging
     */
    val travelEnd = 0.82f

    val travelProgress =
        (phase / travelEnd).coerceIn(0f, 1f)

    /*
     * Quadratic acceleration:
     *
     * y = start + distance × time²
     *
     * The grain begins slowly and accelerates downward.
     */
    val gravityProgress = travelProgress.pow(2f)

    val y = startY +
        (landingY - startY) * gravityProgress

    val settlingProgress = if (phase > travelEnd) {
        ((phase - travelEnd) / (1f - travelEnd))
            .coerceIn(0f, 1f)
    } else {
        0f
    }

    /*
     * The grain becomes slightly wider and flatter on impact.
     * Its opacity then disappears into the existing solid lower fill.
     */
    val impactWidth =
        pixelSize * (1f + settlingProgress * 0.55f)

    val impactHeight =
        pixelSize * (1f - settlingProgress * 0.45f)

    val alpha =
        if (settlingProgress == 0f) {
            1f
        } else {
            1f - settlingProgress
        }

    drawRoundRect(
        color = sandColor.copy(alpha = alpha),
        topLeft = Offset(
            x = centerX - impactWidth / 2f,
            y = y + pixelSize - impactHeight
        ),
        size = Size(
            width = impactWidth,
            height = impactHeight
        ),
        cornerRadius = CornerRadius(
            x = pixelSize * 0.12f,
            y = pixelSize * 0.12f
        )
    )

    /*
     * Brief contact highlight. It stays within the accumulated fill and
     * makes the pixel appear to become part of the pile.
     */
    if (settlingProgress > 0f) {
        val mergeWidth =
            pixelSize * (0.7f + settlingProgress * 1.5f)

        drawRoundRect(
            color = sandColor.copy(
                alpha = (1f - settlingProgress) * 0.55f
            ),
            topLeft = Offset(
                x = centerX - mergeWidth / 2f,
                y = lowerSurfaceY - pixelSize * 0.10f
            ),
            size = Size(
                width = mergeWidth,
                height = pixelSize * 0.28f
            ),
            cornerRadius = CornerRadius(
                x = pixelSize,
                y = pixelSize
            )
        )
    }
}
