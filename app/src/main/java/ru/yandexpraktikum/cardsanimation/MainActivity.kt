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
import android.content.Intent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import ru.yandexpraktikum.cardsanimation.ui.theme.CardsAnimationTheme

// Card data structure
data class CardData(
    val rank: String,
    val suit: String,
    val color: androidx.compose.ui.graphics.Color
)

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
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Compose Version",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        val cards = listOf(
            CardData("A", "♠️", MaterialTheme.colorScheme.onPrimaryContainer),
            CardData("K", "♥️", MaterialTheme.colorScheme.onSecondaryContainer),
            CardData("Q", "♦️", MaterialTheme.colorScheme.onTertiaryContainer),
            CardData("J", "♣️", MaterialTheme.colorScheme.onPrimaryContainer)
        )
        
        CardStack(cards = cards)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = {
                context.startActivity(Intent(context, XmlViewActivity::class.java))
            }
        ) {
            Text("View XML Version")
        }
    }
}

@Composable
fun CardStack(cards: List<CardData>) {
    val cardCount = cards.size
    var isRotated by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var cardOffset by remember { mutableIntStateOf(0) } // Track which card is on top
    
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
                    }
                ) { _, dragAmount ->
                    // This runs while user is dragging their finger
                    val horizontalMovement = dragAmount.x
                    val verticalMovement = dragAmount.y
                    
                    // Determine swipe direction
                    val isHorizontalSwipe = kotlin.math.abs(horizontalMovement) > kotlin.math.abs(verticalMovement)
                    val isVerticalSwipe = kotlin.math.abs(verticalMovement) > kotlin.math.abs(horizontalMovement)
                    
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
                            currentCardOffset = cardOffset,
                            cardCount = cardCount,
                            onCardCycle = { newCardOffset -> cardOffset = newCardOffset }
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
                cardContent = {
                    PlayingCard(cardData = cardData)
                }
            )
        }
    }
}

@Composable
fun PlayingCard(cardData: CardData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Card rank/number - positioned at top for visibility in fan
        Text(
            text = cardData.rank,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = cardData.color,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Card suit - centered
        Text(
            text = cardData.suit,
            fontSize = 40.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}

@Composable
fun AnimatedStackCard(
    cardIndex: Int,
    totalCards: Int,
    isRotated: Boolean,
    baseRotation: Float,
    cardContent: @Composable () -> Unit
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
    
    // All cards positioned at the same location - rotation creates the fan (like View version)
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
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1.0f) // Rotate around bottom center
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (cardIndex % 3) {
                0 -> MaterialTheme.colorScheme.primaryContainer
                1 -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.tertiaryContainer
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = (4 + cardIndex * 1).dp // Subtle elevation increase
        )
    ) {
        // Use the provided card content
        cardContent()
        
        // Optional: Add rotation info overlay
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "${animatedRotationZ.toInt()}°",
                fontSize = 10.sp,
                color = when (cardIndex % 3) {
                    0 -> MaterialTheme.colorScheme.primary
                    1 -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.tertiary
                },
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

// Simplified gesture helper functions (like View gesture detectors!)

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
        // else -> do nothing (small movement, keep current state)
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
 * Handles horizontal finger movement for card cycling
 * Simple horizontal-only detection (like checking X movement in View)
 */
fun handleHorizontalSwipe(
    horizontalMovement: Float,
    currentCardOffset: Int,
    cardCount: Int,
    onCardCycle: (Int) -> Unit
) {
    val swipeThreshold = 30f // Increased threshold to avoid accidental triggers
    
    // Simple check: is horizontal movement significant enough?
    if (kotlin.math.abs(horizontalMovement) > swipeThreshold) {
        if (horizontalMovement > 0) {
            // Swipe right = move bottom card to top (like flipping through deck)
            val newOffset = (currentCardOffset + 1) % cardCount
            onCardCycle(newOffset)
        } else {
            // Swipe left = move top card to bottom (reverse direction)
            val newOffset = if (currentCardOffset - 1 < 0) cardCount - 1 else currentCardOffset - 1
            onCardCycle(newOffset)
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

@Preview(showBackground = true)
@Composable
fun PlayingCardPreview() {
    CardsAnimationTheme {
        PlayingCard(
            cardData = CardData("A", "♠️", MaterialTheme.colorScheme.onPrimaryContainer)
        )
    }
}