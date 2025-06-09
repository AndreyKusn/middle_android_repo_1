package ru.yandexpraktikum.cardsanimation.views

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import ru.yandexpraktikum.cardsanimation.R
import ru.yandexpraktikum.cardsanimation.model.CardData
import kotlin.math.abs

/**
 * Animated card stack view - equivalent to CardStack composable
 * Uses traditional View gesture detectors and animations
 */
class AnimatedCardStackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val cards = mutableListOf<AnimatedCardView>()
    private var cardDataList = listOf<CardData>() // Now mutable and empty by default

    private var cardOffset = 0
    private var isRotated = false
    private var dragOffsetY = 0f

    // Gesture detector for handling swipes (like detectDragGestures in Compose)
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            // Handle drag movement (like onDrag in Compose)
            val horizontalMovement = -distanceX  // Negative because distanceX is opposite
            val verticalMovement = -distanceY
            
            handleGestureMovement(horizontalMovement, verticalMovement)
            return true
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            handleGestureEnd()
            return true
        }
    })

    init {
        // Set default cards if none provided
        if (cardDataList.isEmpty()) {
            setCards(listOf(
                CardData(R.drawable.card_clover),
                CardData(R.drawable.card_hearts),
                CardData(R.drawable.card_spades),
                CardData(R.drawable.card_diamond)
            ))
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        
        // Handle touch end events
        if (event.action == MotionEvent.ACTION_UP) {
            handleGestureEnd()
        }
        
        return true
    }

    /**
     * Set cards dynamically and refresh the stack
     */
    fun setCards(newCardDataList: List<CardData>) {
        cardDataList = newCardDataList
        setupCards()
    }

    /**
     * Setup initial cards in the stack
     */
    private fun setupCards() {
        // Clear existing cards
        clearCards()
        
        // Create new cards
        cardDataList.forEachIndexed { index, cardData ->
            val cardView = AnimatedCardView(context).apply {
                setCardData(cardData)
                setStackPosition(index)
            }
            cards.add(cardView)
            addView(cardView)
        }
        
        // Reset state
        cardOffset = 0
        isRotated = false
        
        // Position cards in initial fan arrangement
        updateCardPositions()
    }

    /**
     * Clear all cards from the stack
     */
    private fun clearCards() {
        cards.clear()
        removeAllViews()
    }

    /**
     * Simplified gesture movement handler for educational purposes
     */
    private fun handleGestureMovement(horizontalMovement: Float, verticalMovement: Float) {
        val isHorizontalSwipe = abs(horizontalMovement) > abs(verticalMovement)
        val isVerticalSwipe = abs(verticalMovement) > abs(horizontalMovement)
        
        // Handle vertical swipes for fan animation
        if (isVerticalSwipe) {
            dragOffsetY += verticalMovement
        }
        
        // Handle horizontal swipes for card cycling (simplified)
        if (isHorizontalSwipe) {
            handleHorizontalSwipe(horizontalMovement)
        }
    }

    /**
     * Simplified horizontal swipe handler for educational purposes
     * Detects single swipe movements above threshold
     */
    private fun handleHorizontalSwipe(horizontalMovement: Float) {
        val swipeThreshold = 50f // Simple threshold for single swipe detection
        
        // Check if single movement exceeds threshold
        if (abs(horizontalMovement) > swipeThreshold) {
            // Both left and right swipes move bottom card to top
            cardOffset = if (cardOffset - 1 < 0) cardDataList.size - 1 else cardOffset - 1
            updateCardData()
        }
    }

    /**
     * Handle gesture end (equivalent to Compose handleDragEnd)
     */
    private fun handleGestureEnd() {
        val threshold = 100f
        
        when {
            dragOffsetY < -threshold -> isRotated = true   // Swiped up = fan out
            dragOffsetY > threshold -> isRotated = false   // Swiped down = fold back
        }
        
        dragOffsetY = 0f
        updateCardPositions()
    }

    /**
     * Update card data after cycling (like actualCardIndex in Compose)
     */
    private fun updateCardData() {
        cards.forEachIndexed { index, cardView ->
            val actualCardIndex = (index + cardOffset) % cardDataList.size
            cardView.setCardData(cardDataList[actualCardIndex])
        }
    }

    /**
     * Update card positions and rotations (like Compose fan positioning)
     */
    private fun updateCardPositions() {
        val cardCount = cards.size
        
        cards.forEachIndexed { index, cardView ->
            // Calculate base rotation (same logic as Compose)
            val baseRotation = if (cardCount > 1) {
                val angleStep = 45f / (cardCount - 1)
                22.5f - (index * angleStep)
            } else {
                0f
            }
            
            // Calculate target rotation - reduced fan spread for better spacing
            val targetRotation = if (isRotated) {
                val angleStep = if (cardCount > 1) 180f / (cardCount - 1) else 0f  // Reduced from 180° to 90°
                -90f + (index * angleStep)  // Range from -45° to +45°
            } else {
                baseRotation
            }
            
            // Calculate the shared position for all cards (center of container)
            val cardWidth = 100f * resources.displayMetrics.density
            val cardHeight = 160f * resources.displayMetrics.density
            val sharedX = width / 2f - cardWidth / 2f
            val sharedY = height / 2f - cardHeight / 2f
            
            // Position all cards at the exact same location
            cardView.x = sharedX
            cardView.y = sharedY
            
            // Set the same pivot point for all cards (bottom center of the card position)
            cardView.pivotX = cardWidth / 2f
            cardView.pivotY = cardHeight
            
            // Apply rotation animation
            cardView.animateToRotation(targetRotation)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            // Update positions when layout changes
            updateCardPositions()
        }
    }
} 