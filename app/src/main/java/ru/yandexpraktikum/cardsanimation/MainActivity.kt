package ru.yandexpraktikum.cardsanimation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.yandexpraktikum.cardsanimation.ui.theme.CardsAnimationTheme
import kotlin.math.abs

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
        AnimatedCard()
    }
}

@Composable
fun AnimatedCard() {
    var isRotated by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(0f) }
    
    // Animate the rotation based on the state
    val animatedRotationZ by animateFloatAsState(
        targetValue = if (isRotated) 45f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "card_rotation"
    )
    
    Card(
        modifier = Modifier
            .size(width = 280.dp, height = 200.dp)
            .graphicsLayer {
                rotationZ = animatedRotationZ
            }
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🎴",
                fontSize = 48.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "Animated Card",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Swipe up to rotate!",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = if (isRotated) "Rotated 45°" else "Normal Position",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 12.dp)
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