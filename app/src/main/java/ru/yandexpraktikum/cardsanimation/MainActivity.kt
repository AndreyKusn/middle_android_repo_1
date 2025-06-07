package ru.yandexpraktikum.cardsanimation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.yandexpraktikum.cardsanimation.ui.theme.CardsAnimationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CardsAnimationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AnimatedCardScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedCardScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CardStack(cardCount = 4)
    }
}

@Composable
fun CardStack(cardCount: Int) {
    var isRotated by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var cardOffset by remember { mutableIntStateOf(0) } // Track which card is on top
    
    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        // Determine final state based on total drag distance
                        val threshold = 100f
                        val horizontalThreshold = 150f
                        
                        when {
                            dragOffset < -threshold -> isRotated = true  // Swiped up significantly
                            dragOffset > threshold -> isRotated = false // Swiped down significantly
                            else -> isRotated = isRotated // Keep current state if drag was minimal
                        }
                        
                        dragOffset = 0f
                    }
                ) { _, dragAmount ->
                    dragOffset += dragAmount.y
                    
                    // Detect horizontal swipes for card cycling
                    if (kotlin.math.abs(dragAmount.x) > kotlin.math.abs(dragAmount.y)) {
                        if (kotlin.math.abs(dragAmount.x) > 20f) { // Horizontal swipe threshold
                            if (dragAmount.x > 0) {
                                // Swipe right: move bottom card to top
                                cardOffset = (cardOffset + 1) % cardCount
                            } else {
                                // Swipe left: move top card to bottom
                                cardOffset = if (cardOffset - 1 < 0) cardCount - 1 else cardOffset - 1
                            }
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Create cards from bottom to top (reverse order for proper layering)
        for (i in cardCount - 1 downTo 0) {
            // Calculate actual card index after cycling
            val actualCardIndex = (i + cardOffset) % cardCount
            
            // Natural fan rotation pattern - distribute cards across 90° span
            val baseRotation = if (cardCount > 1) {
                val angleStep = 45f / (cardCount - 1) // Distribute across 45 degrees
                22.5f - (i * angleStep) // Start from +22.5° and go to -22.5°
            } else {
                0f // Single card at center
            }
            
            AnimatedStackCard(
                cardIndex = i,
                actualCardIndex = actualCardIndex,
                totalCards = cardCount,
                isRotated = isRotated,
                baseRotation = baseRotation
            )
        }
    }
}

@Composable
fun AnimatedStackCard(
    cardIndex: Int,
    actualCardIndex: Int,
    totalCards: Int,
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
    
    // Create fan effect like cards held in hand - pivot from bottom center
    val pivotDistance = 100f // Distance from bottom pivot point to card center
    val angleInRadians = Math.toRadians(baseRotation.toDouble())
    
    // Calculate target position to create narrow bottom grip area and fanned top
    val targetOffsetX = -(pivotDistance * Math.sin(angleInRadians)).toInt()
    val targetOffsetY = -(cardIndex * 24) // Convert dp to pixels (approximately 8dp * 3 density)
    
    // Animate the position using IntOffset
    val animatedOffset by animateIntOffsetAsState(
        targetValue = IntOffset(targetOffsetX, targetOffsetY),
        animationSpec = tween(durationMillis = 300),
        label = "card_position_$cardIndex"
    )
    
    Card(
        modifier = Modifier
            .size(width = 200.dp, height = 280.dp)
            .offset { animatedOffset }
            .graphicsLayer {
                rotationZ = animatedRotationZ
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1.0f) // Rotate around bottom center
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (actualCardIndex % 3) {
                0 -> MaterialTheme.colorScheme.primaryContainer
                1 -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.tertiaryContainer
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = (4 + cardIndex * 1).dp // Subtle elevation increase
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Card rank/number - positioned at top for visibility in fan
            Text(
                text = when (actualCardIndex % 5) {
                    0 -> "A"
                    1 -> "K"
                    2 -> "Q"
                    3 -> "J"
                    else -> "${actualCardIndex + 7}"
                },
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = when (actualCardIndex % 3) {
                    0 -> MaterialTheme.colorScheme.onPrimaryContainer
                    1 -> MaterialTheme.colorScheme.onSecondaryContainer
                    else -> MaterialTheme.colorScheme.onTertiaryContainer
                },
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = when (actualCardIndex % 4) {
                    0 -> "♠️"
                    1 -> "♥️"
                    2 -> "♦️"
                    else -> "♣️"
                },
                fontSize = 40.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Rotation info - smaller and at bottom
            Text(
                text = "${animatedRotationZ.toInt()}°",
                fontSize = 10.sp,
                color = when (actualCardIndex % 3) {
                    0 -> MaterialTheme.colorScheme.primary
                    1 -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.tertiary
                },
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnimatedCardPreview() {
    CardsAnimationTheme {
        AnimatedCardScreen()
    }
}