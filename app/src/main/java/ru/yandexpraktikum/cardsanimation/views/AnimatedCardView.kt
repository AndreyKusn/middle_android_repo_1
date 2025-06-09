package ru.yandexpraktikum.cardsanimation.views

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import ru.yandexpraktikum.cardsanimation.R
import ru.yandexpraktikum.cardsanimation.model.CardData

/**
 * Individual animated card view - equivalent to AnimatedStackCard in Compose
 */
class AnimatedCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val cardView: CardView
    private val cardImageView: ImageView

    private var currentRotation = 0f
    private var rotationAnimator: ObjectAnimator? = null

    init {
        // Inflate the card layout
        LayoutInflater.from(context).inflate(R.layout.card_view, this, true)
        
        cardView = this.getChildAt(0) as CardView
        cardImageView = findViewById(R.id.cardImage)

        // Set transform origin to bottom center (like Compose version)
        pivotX = width / 2f
        pivotY = height.toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Update pivot point when size changes
        pivotX = w / 2f
        pivotY = h.toFloat()
    }

    /**
     * Set card data and update the UI
     */
    fun setCardData(cardData: CardData) {
        cardImageView.setImageResource(cardData.imageResId)
    }

    /**
     * Animate card to target rotation (like Compose animateFloatAsState)
     */
    fun animateToRotation(targetRotation: Float, duration: Long = 300) {
        rotationAnimator?.cancel()
        
        rotationAnimator = ObjectAnimator.ofFloat(this, "rotation", rotation, targetRotation).apply {
            this.duration = duration
            addUpdateListener { 
                currentRotation = rotation
            }
            start()
        }
    }

    /**
     * Set card stack position (for layering like z-index)
     */
    fun setStackPosition(index: Int) {
        // Higher index = higher elevation
        cardView.cardElevation = (4 + index * 1).toFloat() * resources.displayMetrics.density
    }
} 