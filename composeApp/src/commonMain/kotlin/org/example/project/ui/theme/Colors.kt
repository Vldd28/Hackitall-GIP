package org.example.project.ui.theme

import androidx.compose.ui.graphics.Color

// Primary - Vibrant travel blue (sky/adventure theme)
val Primary = Color(0xFF2196F3)
val PrimaryLight = Color(0xFF64B5F6)
val PrimaryDark = Color(0xFF1976D2)

// Secondary - Warm orange (sunset/excitement)
val Secondary = Color(0xFFFF9800)
val SecondaryLight = Color(0xFFFFB74D)
val SecondaryDark = Color(0xFFF57C00)

// Accent - Fresh green (adventure/nature)
val Accent = Color(0xFF4CAF50)
val AccentLight = Color(0xFF81C784)

// Neutral colors
val Background = Color(0xFFFAFAFA)
val Surface = Color(0xFFFFFFFF)
val SurfaceVariant = Color(0xFFF5F5F5)

// Text colors
val TextPrimary = Color(0xFF212121)
val TextSecondary = Color(0xFF757575)
val TextDisabled = Color(0xFFBDBDBD)

// Semantic colors
val Error = Color(0xFFE53935)
val Success = Color(0xFF43A047)
val Warning = Color(0xFFFFA726)

// Dark theme colors
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkSurfaceVariant = Color(0xFF2C2C2C)

// Interest category colors (for chips/tags)
val InterestArt = Color(0xFFE91E63)
val InterestMusic = Color(0xFF9C27B0)
val InterestSports = Color(0xFF4CAF50)
val InterestFood = Color(0xFFFF5722)
val InterestTech = Color(0xFF2196F3)
val InterestNature = Color(0xFF8BC34A)
val InterestCulture = Color(0xFFFF9800)
val InterestNightlife = Color(0xFF673AB7)

// Map/Location colors
val LocationMarker = Color(0xFFE53935)
val LocationSelected = Color(0xFFFF5722)

// Helper function to get interest color by category name
fun getInterestColor(interestName: String): Color {
    return when (interestName.lowercase()) {
        "art", "painting", "photography" -> InterestArt
        "music", "concert", "festival" -> InterestMusic
        "sports", "climbing", "hiking", "running" -> InterestSports
        "food", "cooking", "restaurant" -> InterestFood
        "tech", "technology", "coding" -> InterestTech
        "nature", "outdoor", "camping" -> InterestNature
        "culture", "museum", "history" -> InterestCulture
        "nightlife", "party", "bar" -> InterestNightlife
        else -> Accent
    }
}
