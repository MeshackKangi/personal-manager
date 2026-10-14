package com.meshack.personalmanager.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Stable per-goal identity colors. Same goal id always maps to the same
 * color, in both light and dark themes. Saturated enough to read as a
 * small dot on any surface.
 */
object GoalColors {
    private val Palette = listOf(
        Color(0xFFF2C14E), // gold
        Color(0xFF4DD0A6), // mint
        Color(0xFFB39DDB), // violet
        Color(0xFFF08A8A), // rose
        Color(0xFF7CC4F2), // sky
        Color(0xFFAED581), // lime
        Color(0xFFFFB74D), // orange
        Color(0xFFF48FB1) // pink
    )

    fun forGoal(goalId: Long): Color {
        // Absolute value keeps the index stable for negative ids (unsaved rows).
        val index = (goalId % Palette.size).toInt().let { if (it < 0) it + Palette.size else it }
        return Palette[index]
    }
}
