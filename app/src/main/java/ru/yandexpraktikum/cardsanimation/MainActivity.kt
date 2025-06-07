package ru.yandexpraktikum.cardsanimation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
        CardStack(cardCount = 5)
    }
}

@Composable
fun CardStack(cardCount: Int) {
    var isRotated by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    
    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        // Determine final state based on total drag distance
                        val threshold = 100f
                        isRotated = when {
                            dragOffset < -threshold -> true  // Swiped up significantly
                            dragOffset > threshold -> false // Swiped down significantly
                            else -> isRotated // Keep current state if drag was minimal
                        }
                        dragOffset = 0f
                    }
                ) { _, dragAmount ->
                    dragOffset += dragAmount.y
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Create cards from bottom to top (reverse order for proper layering)
        for (i in cardCount - 1 downTo 0) {
            // Natural fan rotation pattern like real cards held in hand
            val baseRotation = when (i) {
                0 -> 15f  // Top card: clockwise 15°
                1 -> 3f   // Second card: clockwise 3°
                2 -> -9f  // Third card: counterclockwise 9°
                3 -> -21f // Fourth card: counterclockwise 21° (-9 + -12)
                4 -> -33f // Fifth card: counterclockwise 33° (-21 + -12)
                else -> -33f - (i - 4) * 12f // Continue pattern for more cards
            }
            
            AnimatedStackCard(
                cardIndex = i,
                isRotated = isRotated,
                baseRotation = baseRotation
            )
        }
    }
}

@Composable
fun AnimatedStackCard(
    cardIndex: Int,
    isRotated: Boolean,
    baseRotation: Float
) {
    // Calculate final rotation: base rotation + additional rotation when swiped
    val targetRotation = baseRotation + if (isRotated) 45f else 0f
    
    // Animate the rotation based on the state
    val animatedRotationZ by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(durationMillis = 300),
        label = "card_rotation_$cardIndex"
    )
    
    // Create fan effect like cards held in hand - expose upper portion of each card
    val fanRadius = 120f // Radius of the fan arc
    val angleInRadians = Math.toRadians(baseRotation.toDouble())
    
    // Calculate position based on fan arc - cards spread out horizontally and slightly vertically
    val offsetX = -(fanRadius * Math.sin(angleInRadians) * 0.6).dp // Horizontal spread (negative for left side)
    val offsetY = -(cardIndex * 12 + fanRadius * Math.cos(angleInRadians) * 0.2).dp // Negative Y to show upper parts of lower cards
    
    Card(
        modifier = Modifier
            .size(width = 200.dp, height = 280.dp)
            .graphicsLayer {
                rotationZ = animatedRotationZ
                translationX = offsetX.toPx()
                translationY = offsetY.toPx()
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Card rank/number - positioned at top for visibility in fan
            Text(
                text = when (cardIndex % 5) {
                    0 -> "A"
                    1 -> "K"
                    2 -> "Q"
                    3 -> "J"
                    else -> "${cardIndex + 7}"
                },
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = when (cardIndex % 3) {
                    0 -> MaterialTheme.colorScheme.onPrimaryContainer
                    1 -> MaterialTheme.colorScheme.onSecondaryContainer
                    else -> MaterialTheme.colorScheme.onTertiaryContainer
                },
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = when (cardIndex % 4) {
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
                color = when (cardIndex % 3) {
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