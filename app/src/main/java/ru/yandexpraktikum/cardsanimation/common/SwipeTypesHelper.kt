package ru.yandexpraktikum.cardsanimation.common

import kotlin.math.abs

private const val DRAG_THRESHOLD = 100.0

internal fun getSwipeType(
    verticalDragOffset: Float,
    horizontalDragOffset: Float,
): SwipeType {
    val absV = abs(verticalDragOffset)
    val absH = abs(horizontalDragOffset)

    if (absV < DRAG_THRESHOLD && absH < DRAG_THRESHOLD)
        return SwipeType.NO_SWIPE

    return if (absV > absH) {
        if (verticalDragOffset > 0)
            SwipeType.VERTICAL_DOWN else SwipeType.VERTICAL_UP
    } else {
        if (horizontalDragOffset < 0)
            SwipeType.HORIZONTAL_LEFT else SwipeType.HORIZONTAL_RIGHT
    }
}

internal enum class SwipeType {
    VERTICAL_UP,
    VERTICAL_DOWN,
    HORIZONTAL_LEFT,
    HORIZONTAL_RIGHT,
    NO_SWIPE
}