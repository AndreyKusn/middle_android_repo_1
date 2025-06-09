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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ru.yandexpraktikum.cardsanimation.model.CardData
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
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.jetpack_compose_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        val cards = listOf(
            CardData(R.drawable.card_clover),
            CardData(R.drawable.card_hearts),
            CardData(R.drawable.card_spades),
            CardData(R.drawable.card_diamond)
        )
        
        CardStack(cards = cards)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = {
                context.startActivity(Intent(context, XmlViewActivity::class.java))
            }
        ) {
            Text("Просмотреть версию на XML View")
        }
    }
}

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
                            accumulatedHorizontalMovement = accumulatedHorizontalMovement,
                            currentCardOffset = cardOffset,
                            cardCount = cardCount,
                            onCardCycle = { newCardOffset -> cardOffset = newCardOffset },
                            onHorizontalAccumulate = { newAccumulation -> accumulatedHorizontalMovement = newAccumulation }
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
 * Handles horizontal finger movement for card cycling
 * Accumulates movement like the smooth XML View version
 */
fun handleHorizontalSwipe(
    horizontalMovement: Float,
    accumulatedHorizontalMovement: Float,
    currentCardOffset: Int,
    cardCount: Int,
    onCardCycle: (Int) -> Unit,
    onHorizontalAccumulate: (Float) -> Unit
) {
    // Accumulate horizontal movement for smoother detection
    val newAccumulation = accumulatedHorizontalMovement + horizontalMovement
    onHorizontalAccumulate(newAccumulation)
    
    val swipeThreshold = 30f // Same threshold as XML View
    
    // Check if accumulated movement exceeds threshold
    if (kotlin.math.abs(newAccumulation) > swipeThreshold) {
        // Both left and right swipes move bottom card to top
        val newOffset = if (currentCardOffset - 1 < 0) cardCount - 1 else currentCardOffset - 1
        onCardCycle(newOffset)
        
        // Reset accumulation after triggering card cycle
        onHorizontalAccumulate(0f)
    }
}