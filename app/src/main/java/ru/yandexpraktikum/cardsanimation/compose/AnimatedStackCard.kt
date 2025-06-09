package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ru.yandexpraktikum.cardsanimation.model.CardData

@Composable
fun AnimatedStackCard(
    cardIndex: Int,
    totalCards: Int,
    cardData: CardData,
    isRotated: Boolean,
    baseRotation: Float
) {
    // Calculate final rotation: base rotation or dramatic fan spread when swiped
    val targetRotation = if (isRotated) {
        // When swiped up: equally distribute cards across 180° fan
        val angleStep = if (totalCards > 1) 180f / (totalCards - 1) else 0f
        -90f + (cardIndex * angleStep) // Distribute from -90° to +90°
    } else {
        baseRotation // Use natural hand-held positions
    }

    // Animate the rotation based on the state
    val animatedRotationZ by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(durationMillis = 300),
        label = "card_rotation_$cardIndex"
    )

    val animatedOffset by animateIntOffsetAsState(
        targetValue = IntOffset(0, 0), // All cards at center position
        animationSpec = tween(durationMillis = 300),
        label = "card_position_$cardIndex"
    )

    Card(
        modifier = Modifier
            .size(width = 100.dp, height = 160.dp)
            .offset { animatedOffset }
            .graphicsLayer {
                rotationZ = animatedRotationZ
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(
                    0.5f,
                    1.0f
                ) // Rotate around bottom center
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = (4 + cardIndex * 1).dp
        )
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(cardData.imageResId),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    }
}

/**
 * Handles what happens when user lifts their finger (like onFling in View)
 * Similar to GestureDetector.OnGestureListener.onFling()
 */
fun handleDragEnd(
    verticalDragDistance: Float,
    onFanStateChange: (Boolean) -> Unit
) {
    val threshold = 100f // Minimum distance to trigger action

    when {
        verticalDragDistance < -threshold -> onFanStateChange(true)  // Swiped up = fan out
        verticalDragDistance > threshold -> onFanStateChange(false)  // Swiped down = fold back
    }
}

/**
 * Handles vertical finger movement for fan animation
 * Similar to handling Y-axis in GestureDetector.OnGestureListener.onScroll()
 */
fun handleVerticalSwipe(
    verticalMovement: Float,
    onVerticalDrag: (Float) -> Unit
) {
    // Simply track vertical movement for fan animation
    // Positive = swipe down, Negative = swipe up
    onVerticalDrag(verticalMovement)
}

/**
 * Simplified horizontal swipe handler for educational purposes
 * Detects single swipe movements above threshold
 */
fun handleHorizontalSwipe(
    horizontalMovement: Float,
    currentCardOffset: Int,
    cardCount: Int,
    onCardCycle: (Int) -> Unit
) {
    val swipeThreshold = 50f // Simple threshold for single swipe detection

    // Check if single movement exceeds threshold
    if (kotlin.math.abs(horizontalMovement) > swipeThreshold) {
        // Both left and right swipes move bottom card to top
        val newOffset = if (currentCardOffset - 1 < 0) cardCount - 1 else currentCardOffset - 1
        onCardCycle(newOffset)
    }
}