package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ru.yandexpraktikum.cardsanimation.model.CardData

@Composable
fun CardStack(cards: List<CardData>) {
    val cardCount = cards.size
    var isRotated by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier,
        contentAlignment = Alignment.Center
    ) {
        // Отрисовка колоды карт в исходной позиции
        for (i in 0 until cardCount) {
            val cardData = cards[i]

            val baseRotation = if (cardCount > 1) {
                val angleStep = 45f / (cardCount - 1)
                22.5f - (i * angleStep)
            } else {
                0f
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