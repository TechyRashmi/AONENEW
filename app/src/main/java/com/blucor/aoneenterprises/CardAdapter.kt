package com.blucor.aoneenterprises

import androidx.cardview.widget.CardView

interface CardAdapter {
    val baseElevation: Float
    fun getCardViewAt(i: Int): CardView?
    val count: Int

    companion object {
        const val MAX_ELEVATION_FACTOR = 8
    }
}