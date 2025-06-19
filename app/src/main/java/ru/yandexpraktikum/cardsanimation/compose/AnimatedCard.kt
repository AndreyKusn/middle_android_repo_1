package ru.yandexpraktikum.cardsanimation.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.yandexpraktikum.cardsanimation.model.CardData

@Composable
fun AnimatedCard(
    cardIndex: Int,
    cardData: CardData,
    targetRotation: Float
) {
    // TODO: Добавьте анимацию поворота карты
    // Подсказка: используйте animateFloatAsState для плавной анимации

    // Здесь хранится высота карты (чем ближе к концу списка, тем выше она должна выглядеть)
    val cardElevation = (4 + cardIndex).dp

    Card(
        modifier = Modifier
            .size(width = 100.dp, height = 160.dp)
            // TODO: Добавьте анимацию карты при свайпе вправо или влево
            .graphicsLayer {
                rotationZ = targetRotation
                transformOrigin = TransformOrigin(0.5f, 1.0f)
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = cardElevation
            // TODO: Добавьте анимацию высоты карты
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