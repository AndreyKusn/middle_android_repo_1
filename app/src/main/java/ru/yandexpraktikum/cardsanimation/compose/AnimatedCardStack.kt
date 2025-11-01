package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import ru.yandexpraktikum.cardsanimation.common.SwipeType.VERTICAL_UP
import ru.yandexpraktikum.cardsanimation.common.SwipeType.VERTICAL_DOWN
import ru.yandexpraktikum.cardsanimation.common.SwipeType.HORIZONTAL_LEFT
import ru.yandexpraktikum.cardsanimation.common.SwipeType.HORIZONTAL_RIGHT
import ru.yandexpraktikum.cardsanimation.common.SwipeType.NO_SWIPE
import ru.yandexpraktikum.cardsanimation.common.getSwipeType
import ru.yandexpraktikum.cardsanimation.model.CardData

/**
 * Метод для вычисления поворота карты в конкретной позиции
 */
fun calculateCardRotation(
    cardIndex: Int,
    cardCount: Int,
    isRotated: Boolean,
): Float {
    if (cardCount <= 1) return 0f

    return if (isRotated) {
        val angleStep = 180f / (cardCount - 1)
        90f - (cardIndex * angleStep)
    } else {
        val angleStep = 45f / (cardCount - 1)
        22.5f - (cardIndex * angleStep)
    }
}

@Composable
fun AnimatedCardStack(cards: List<CardData>) {
    val cardCount = cards.size
    var isRotated by remember { mutableStateOf(false) }
    var animationState by remember { mutableStateOf(CardSwapAnimationState()) }
    var currentCards by remember { mutableStateOf(cards) }

    var verticalDragOffset = 0f
    var horizontalDragOffset = 0f

    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (!animationState.isAnimating) {
                            when (getSwipeType(
                                verticalDragOffset,
                                horizontalDragOffset,
                            )) {
                                VERTICAL_UP -> isRotated = false
                                VERTICAL_DOWN -> isRotated = true
                                HORIZONTAL_LEFT -> animationState =
                                    animationState.copy(
                                        isAnimating = true,
                                        animationStep = 1
                                    )

                                HORIZONTAL_RIGHT -> animationState =
                                    animationState.copy(
                                        isAnimating = true,
                                        animationStep = 1
                                    )

                                NO_SWIPE -> {}
                            }
                        }
                        verticalDragOffset = 0f
                        horizontalDragOffset = 0f
                    }
                ) { change, dragAmount ->
                    verticalDragOffset += dragAmount.y
                    horizontalDragOffset += dragAmount.x
                }
            },
        contentAlignment = Alignment.Center
    ) {

        currentCards.forEachIndexed { cardIndex, cardData ->
            key(cardData.imageResId) {
                val targetRotation = calculateCardRotation(cardIndex, cardCount, isRotated)

                AnimatedCard(
                    cardIndex = cardIndex,
                    targetRotation = targetRotation,
                    cardData = cardData,
                    isAnimating = animationState.isAnimating,
                    animationStep = if (cardIndex == 0) animationState.animationStep else 0,
                    onAnimationStepComplete = { step ->
                        handleAnimationStepComplete(
                            step,
                            cardIndex,
                            { newStep ->
                                animationState = animationState.copy(
                                    animationStep = newStep
                                )
                                if (animationState.animationStep == 3)
                                    currentCards = reorderCards(currentCards)
                            },
                            onAnimationComplete = {
                                animationState =
                                    animationState.copy(
                                        isAnimating = false,
                                        animationStep = 0
                                    )
                            },
                        )
                    },
                )
            }
        }
    }
}

// Простая функция перестановки карт
fun reorderCards(cards: List<CardData>): List<CardData> {
    return cards.drop(1) + cards.first()
}

fun handleAnimationStepComplete(
    step: Int,
    cardIndex: Int,
    onStepChange: (Int) -> Unit,
    onAnimationComplete: () -> Unit
) {
    if (cardIndex == 0) {
        when (step) {
            1 -> onStepChange(2)
            2 -> onStepChange(3)
            3 -> onAnimationComplete()
        }
    }
}
