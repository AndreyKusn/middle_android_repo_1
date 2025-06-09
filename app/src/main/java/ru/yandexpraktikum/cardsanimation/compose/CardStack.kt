package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import ru.yandexpraktikum.cardsanimation.model.CardData

@Composable
fun CardStack(cards: List<CardData>) {
    val cardCount = cards.size
    var isRotated by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var cardOffset by remember { mutableIntStateOf(0) } // Track which card is on top
    var accumulatedHorizontalMovement by remember { mutableFloatStateOf(0f) } // Track cumulative horizontal movement

    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        // When user lifts finger, decide what to do based on how far they dragged
                        handleDragEnd(
                            verticalDragDistance = dragOffset,
                            onFanStateChange = { newFanState -> isRotated = newFanState }
                        )
                        dragOffset = 0f // Reset for next gesture
                        accumulatedHorizontalMovement = 0f // Reset horizontal accumulation
                    }
                ) { _, dragAmount ->
                    // This runs while user is dragging their finger
                    val horizontalMovement = dragAmount.x
                    val verticalMovement = dragAmount.y

                    // Determine swipe direction
                    val isHorizontalSwipe =
                        kotlin.math.abs(horizontalMovement) > kotlin.math.abs(verticalMovement)
                    val isVerticalSwipe =
                        kotlin.math.abs(verticalMovement) > kotlin.math.abs(horizontalMovement)

                    // Only handle vertical swipes for fan animation
                    if (isVerticalSwipe) {
                        handleVerticalSwipe(
                            verticalMovement = verticalMovement,
                            onVerticalDrag = { verticalDistance -> dragOffset += verticalDistance }
                        )
                    }

                    // Only handle horizontal swipes for card cycling
                    if (isHorizontalSwipe) {
                        handleHorizontalSwipe(
                            horizontalMovement = horizontalMovement,
                            accumulatedHorizontalMovement = accumulatedHorizontalMovement,
                            currentCardOffset = cardOffset,
                            cardCount = cardCount,
                            onCardCycle = { newCardOffset -> cardOffset = newCardOffset },
                            onHorizontalAccumulate = { newAccumulation ->
                                accumulatedHorizontalMovement = newAccumulation
                            }
                        )
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Create cards from bottom to top (reverse order for proper layering)
        for (i in 0 until cardCount) {
            // Calculate actual card index after cycling
            val actualCardIndex = (i + cardOffset) % cardCount
            val cardData = cards[actualCardIndex]

            // Natural fan rotation pattern - distribute cards across 90° span
            val baseRotation = if (cardCount > 1) {
                val angleStep = 45f / (cardCount - 1) // Distribute across 45 degrees
                22.5f - (i * angleStep) // Start from +22.5° and go to -22.5°
            } else {
                0f // Single card at center
            }

            AnimatedStackCard(
                cardIndex = i,
                totalCards = cardCount,
                isRotated = isRotated,
                baseRotation = baseRotation,
                cardData = cardData
            )
        }
    }
}