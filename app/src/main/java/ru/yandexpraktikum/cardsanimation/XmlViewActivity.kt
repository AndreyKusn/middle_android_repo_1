package ru.yandexpraktikum.cardsanimation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.yandexpraktikum.cardsanimation.views.AnimatedCardStackView

/**
 * Activity demonstrating the XML View version of the animated card stack
 * Compare this to MainActivity which uses Compose
 */
class XmlViewActivity : AppCompatActivity() {
    
    private lateinit var cardStackView: AnimatedCardStackView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_xml)
        
        // Get reference to the card stack view
        cardStackView = findViewById(R.id.cardStackView)
        
        // The card stack is automatically set up through XML inflation
        // All gesture handling and animations are built into the custom view
    }
} 