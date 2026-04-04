package org.example.project.ui.theme

import androidx.compose.ui.graphics.Color

// ── Wandr palette ─────────────────────────────────────────────────────────────
val WandrSteelBlue   = Color(0xFF80A1BA)   // rgb(128, 161, 186)
val WandrTeal        = Color(0xFF91C4C3)   // rgb(145, 196, 195)
val WandrMint        = Color(0xFFB4DEBD)   // rgb(180, 222, 189)
val WandrCream       = Color(0xFFFFF7DD)   // rgb(255, 247, 221)

// Derived shades
val WandrSteelBlueDark  = Color(0xFF5A7E9A)
val WandrSteelBlueLight = Color(0xFFB0C8D8)
val WandrTealDark       = Color(0xFF6AA3A2)
val WandrMintDark       = Color(0xFF8EC49A)
val WandrCreamDark      = Color(0xFFE8D9A0)

// Neutrals
val Background      = WandrCream
val Surface         = Color(0xFFFFFFFF)
val SurfaceVariant  = Color(0xFFF0F8F4)

// Text
val TextPrimary     = Color(0xFF1E2D3A)
val TextSecondary   = Color(0xFF4A6275)
val TextDisabled    = Color(0xFFAABBC8)

// Semantic
val Error           = Color(0xFFD64E4E)
val Success         = Color(0xFF4CAF50)
val Warning         = Color(0xFFE8A838)

// Dark theme
val DarkBackground      = Color(0xFF1A2530)
val DarkSurface         = Color(0xFF223040)
val DarkSurfaceVariant  = Color(0xFF2C3E4F)

// Interest category colors
val InterestArt       = Color(0xFFE91E63)
val InterestMusic     = Color(0xFF9C27B0)
val InterestSports    = WandrMint
val InterestFood      = Color(0xFFFF5722)
val InterestTech      = WandrSteelBlue
val InterestNature    = WandrTeal
val InterestCulture   = WandrCreamDark
val InterestNightlife = Color(0xFF673AB7)

val LocationMarker   = Color(0xFFD64E4E)
val LocationSelected = WandrSteelBlue

fun getInterestColor(interestName: String): Color {
    return when (interestName.lowercase()) {
        "art", "painting", "photography" -> InterestArt
        "music", "concert", "festival"   -> InterestMusic
        "sports", "climbing", "hiking", "running" -> InterestSports
        "food", "cooking", "restaurant"  -> InterestFood
        "tech", "technology", "coding"   -> InterestTech
        "nature", "outdoor", "camping"   -> InterestNature
        "culture", "museum", "history"   -> InterestCulture
        "nightlife", "party", "bar"      -> InterestNightlife
        else -> WandrTeal
    }
}
