package ru.yandexpraktikum.cardsanimation.views

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import ru.yandexpraktikum.cardsanimation.R

/**
 * Individual animated card view - equivalent to AnimatedStackCard in Compose
 */
class AnimatedCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val cardView: CardView
    private val rankTextView: TextView
    private val suitTextView: TextView
    private val rotationInfoTextView: TextView

    private var currentRotation = 0f
    private var rotationAnimator: ObjectAnimator? = null

    init {
        // Inflate the card layout
        LayoutInflater.from(context).inflate(R.layout.card_view, this, true)
        
        cardView = this.getChildAt(0) as CardView
        rankTextView = findViewById(R.id.cardRank)
        suitTextView = findViewById(R.id.cardSuit)
        rotationInfoTextView = findViewById(R.id.rotationInfo)

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
        rankTextView.text = cardData.rank
        suitTextView.text = cardData.suit
        rankTextView.setTextColor(ContextCompat.getColor(context, cardData.textColor))
        cardView.setCardBackgroundColor(ContextCompat.getColor(context, cardData.backgroundColor))
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
                rotationInfoTextView.text = "${currentRotation.toInt()}°"
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