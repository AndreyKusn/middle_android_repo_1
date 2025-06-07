package ru.yandexpraktikum.cardsanimation.views

/**
 * Data class representing a playing card for the View system
 */
data class CardData(
    val rank: String,
    val suit: String,
    val backgroundColor: Int,  // Color resource
    val textColor: Int         // Color resource
) 