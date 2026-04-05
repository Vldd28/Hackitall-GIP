package org.example.project.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.example.project.data.model.Event
import org.example.project.data.model.Interest
import org.example.project.data.model.Profile
import org.example.project.ui.components.MapView
import org.example.project.viewmodel.EventViewModel
import org.example.project.viewmodel.ProfileViewModel

// ── palette ───────────────────────────────────────────────────────────────────
private val SteelBlue      = Color(0xFF80A1BA)
private val SteelBlueDark  = Color(0xFF5A7E9A)
private val SteelBlueLight = Color(0xFFB0C8D8)
private val Teal           = Color(0xFF91C4C3)
private val TealDark       = Color(0xFF6AA3A2)
private val Mint           = Color(0xFFB4DEBD)
private val MintDark       = Color(0xFF8EC49A)
private val Cream          = Color(0xFFFFF7DD)
private val CreamDark      = Color(0xFFE8D9A0)
private val BarWhite       = Color(0xFFFCFBF7)
private val TextDark       = Color(0xFF1E2D3A)

private val chipColors = listOf(SteelBlue, Teal, Mint, CreamDark, TealDark, MintDark, SteelBlueDark, SteelBlueLight)
private fun chipColorForIndex(i: Int): Color = chipColors[i % chipColors.size]

// ══════════════════════════════════════════════════════════════════════════════
//  Custom drawn icons
// ══════════════════════════════════════════════════════════════════════════════

// Profile silhouette (Instagram-style head + shoulders)
@Composable
private fun ProfileIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(26.dp)) {
        val w = size.width; val h = size.height
        drawCircle(color = tint, radius = w * 0.22f, center = Offset(w * 0.5f, h * 0.28f))
        val shoulders = Path().apply {
            moveTo(w * 0.08f, h * 0.95f)
            cubicTo(w * 0.08f, h * 0.58f, w * 0.28f, h * 0.48f, w * 0.5f, h * 0.48f)
            cubicTo(w * 0.72f, h * 0.48f, w * 0.92f, h * 0.58f, w * 0.92f, h * 0.95f)
        }
        drawPath(shoulders, color = tint, style = Fill)
    }
}

// iPhone-style location arrow (triangle pointing upper-left)
@Composable
private fun LocationArrowIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(26.dp)) {
        val w = size.width; val h = size.height
        val arrow = Path().apply {
            moveTo(w * 0.18f, h * 0.12f)  // Top-left tip
            lineTo(w * 0.88f, h * 0.50f)  // Right middle
            lineTo(w * 0.50f, h * 0.50f)  // Notch center
            lineTo(w * 0.50f, h * 0.88f)  // Bottom middle
            close()
        }
        drawPath(arrow, color = tint, style = Fill)
    }
}

// Calendar / to-do icon
@Composable
private fun CalendarIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(26.dp)) {
        val w = size.width; val h = size.height
        // Calendar body
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.10f, h * 0.22f),
            size = androidx.compose.ui.geometry.Size(w * 0.80f, h * 0.68f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.08f),
            style = Stroke(width = w * 0.08f)
        )
        // Top bar
        drawRect(
            color = tint,
            topLeft = Offset(w * 0.10f, h * 0.22f),
            size = androidx.compose.ui.geometry.Size(w * 0.80f, h * 0.18f)
        )
        // Binding rings
        drawLine(tint, Offset(w * 0.32f, h * 0.10f), Offset(w * 0.32f, h * 0.30f), strokeWidth = w * 0.07f)
        drawLine(tint, Offset(w * 0.68f, h * 0.10f), Offset(w * 0.68f, h * 0.30f), strokeWidth = w * 0.07f)
        // Check mark inside
        val check = Path().apply {
            moveTo(w * 0.28f, h * 0.58f)
            lineTo(w * 0.42f, h * 0.72f)
            lineTo(w * 0.72f, h * 0.48f)
        }
        drawPath(check, color = tint, style = Stroke(width = w * 0.07f))
    }
}

// Suggestion / lightbulb icon
@Composable
private fun SuggestionIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(26.dp)) {
        val w = size.width; val h = size.height
        // Bulb body
        drawCircle(color = tint, radius = w * 0.28f, center = Offset(w * 0.5f, h * 0.38f))
        // Glow ring
        drawCircle(color = tint.copy(alpha = 0.18f), radius = w * 0.42f, center = Offset(w * 0.5f, h * 0.38f))
        // Base/stem
        val stem = Path().apply {
            moveTo(w * 0.38f, h * 0.64f)
            lineTo(w * 0.62f, h * 0.64f)
            lineTo(w * 0.58f, h * 0.78f)
            lineTo(w * 0.42f, h * 0.78f)
            close()
        }
        drawPath(stem, color = tint, style = Fill)
        // Base cap
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.38f, h * 0.78f),
            size = androidx.compose.ui.geometry.Size(w * 0.24f, h * 0.08f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f)
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  HOBBY PICKER
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun HobbyPickerSheet(
    allInterests: List<Interest>,
    currentInterestIds: Set<Int>,
    onConfirm: (List<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember(currentInterestIds) { mutableStateOf(currentInterestIds.toMutableSet()) }
    val selectColor = Color(180, 222, 189)

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            .shadow(16.dp, RoundedCornerShape(24.dp), ambientColor = SteelBlue.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Cream.copy(alpha = 0.97f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Pick your hobbies", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SteelBlueDark)
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, "Close", tint = SteelBlue, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("Tap to select, then hit save", fontSize = 12.sp, color = Teal)
            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                allInterests.chunked(3).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        rowItems.forEach { interest ->
                            val isSelected = interest.id in selected
                            Box(
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) selectColor else selectColor.copy(alpha = 0.18f))
                                    .border(if (isSelected) 2.dp else 1.dp, if (isSelected) selectColor else selectColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                    .clickable { selected = selected.toMutableSet().apply { if (isSelected) remove(interest.id) else add(interest.id) } }
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSelected) { Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(4.dp)) }
                                    Text(interest.name, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextDark, maxLines = 1, textAlign = TextAlign.Center)
                                }
                            }
                        }
                        repeat(3 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(onClick = { onConfirm(selected.toList()) }, shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SteelBlue),
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) { Text("Save Hobbies (${selected.size})", color = Color.White, fontWeight = FontWeight.SemiBold) }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  PROFILE PAGE — travel themed & friendly
// ══════════════════════════════════════════════════════════════════════════════

// Decorative dotted routes drawn behind the profile content
@Composable
private fun ProfileBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width; val h = size.height
        val dash = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
        val routeStroke = Stroke(width = 3f, pathEffect = dash, cap = androidx.compose.ui.graphics.StrokeCap.Round)

        // Gentle decorative route curves
        val r1 = Path().apply {
            moveTo(w * 0.05f, h * 0.15f)
            cubicTo(w * 0.25f, h * 0.10f, w * 0.35f, h * 0.25f, w * 0.55f, h * 0.20f)
            cubicTo(w * 0.75f, h * 0.15f, w * 0.85f, h * 0.28f, w * 1.02f, h * 0.22f)
        }
        drawPath(r1, color = Teal.copy(alpha = 0.18f), style = routeStroke)

        val r2 = Path().apply {
            moveTo(-w * 0.02f, h * 0.55f)
            cubicTo(w * 0.18f, h * 0.50f, w * 0.30f, h * 0.62f, w * 0.50f, h * 0.58f)
            cubicTo(w * 0.70f, h * 0.54f, w * 0.80f, h * 0.65f, w * 1.05f, h * 0.60f)
        }
        drawPath(r2, color = Mint.copy(alpha = 0.15f), style = routeStroke)

        val r3 = Path().apply {
            moveTo(w * 0.10f, h * 0.85f)
            cubicTo(w * 0.30f, h * 0.80f, w * 0.50f, h * 0.90f, w * 0.70f, h * 0.82f)
            cubicTo(w * 0.85f, h * 0.76f, w * 0.95f, h * 0.88f, w * 1.05f, h * 0.84f)
        }
        drawPath(r3, color = SteelBlue.copy(alpha = 0.12f), style = routeStroke)

        // Tiny decorative planes
        val planeColor = Teal.copy(alpha = 0.15f)
        val ps = w * 0.04f
        // Top-right
        drawCircle(planeColor, radius = ps * 0.3f, center = Offset(w * 0.88f, h * 0.08f))
        val tp = Path().apply {
            moveTo(w * 0.88f, h * 0.08f - ps * 0.5f)
            lineTo(w * 0.88f + ps * 0.05f, h * 0.08f + ps * 0.3f)
            lineTo(w * 0.88f - ps * 0.05f, h * 0.08f + ps * 0.3f)
            close()
            moveTo(w * 0.88f - ps * 0.04f, h * 0.08f)
            lineTo(w * 0.88f - ps * 0.35f, h * 0.08f + ps * 0.15f)
            lineTo(w * 0.88f - ps * 0.04f, h * 0.08f + ps * 0.12f)
            close()
            moveTo(w * 0.88f + ps * 0.04f, h * 0.08f)
            lineTo(w * 0.88f + ps * 0.35f, h * 0.08f + ps * 0.15f)
            lineTo(w * 0.88f + ps * 0.04f, h * 0.08f + ps * 0.12f)
            close()
        }
        drawPath(tp, planeColor, style = Fill)
    }
}

// ── Settings icon (gear) ─────────────────────────────────────────────────────
@Composable
private fun SettingsIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(26.dp)) {
        val w = size.width; val h = size.height
        val cx = w * 0.5f; val cy = h * 0.5f
        // Outer gear teeth
        drawCircle(color = tint, radius = w * 0.38f, center = Offset(cx, cy), style = Stroke(width = w * 0.09f))
        // Inner circle
        drawCircle(color = tint, radius = w * 0.17f, center = Offset(cx, cy), style = Stroke(width = w * 0.07f))
        // Teeth (6)
        for (i in 0..5) {
            val angle = i * 60.0 * kotlin.math.PI / 180.0
            val x1 = cx + (w * 0.30f) * kotlin.math.cos(angle).toFloat()
            val y1 = cy + (h * 0.30f) * kotlin.math.sin(angle).toFloat()
            val x2 = cx + (w * 0.48f) * kotlin.math.cos(angle).toFloat()
            val y2 = cy + (h * 0.48f) * kotlin.math.sin(angle).toFloat()
            drawLine(tint, Offset(x1, y1), Offset(x2, y2), strokeWidth = w * 0.10f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        }
    }
}

// ── Sun icon ────────────────────────────────────────────────────────────────
@Composable
private fun SunIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(26.dp)) {
        val w = size.width; val h = size.height
        val cx = w * 0.5f; val cy = h * 0.5f
        drawCircle(color = tint, radius = w * 0.22f, center = Offset(cx, cy))
        for (i in 0..7) {
            val angle = i * 45.0 * kotlin.math.PI / 180.0
            val x1 = cx + (w * 0.30f) * kotlin.math.cos(angle).toFloat()
            val y1 = cy + (h * 0.30f) * kotlin.math.sin(angle).toFloat()
            val x2 = cx + (w * 0.46f) * kotlin.math.cos(angle).toFloat()
            val y2 = cy + (h * 0.46f) * kotlin.math.sin(angle).toFloat()
            drawLine(tint, Offset(x1, y1), Offset(x2, y2), strokeWidth = w * 0.06f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        }
    }
}

// ── Moon icon ───────────────────────────────────────────────────────────────
@Composable
private fun MoonIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(26.dp)) {
        val w = size.width; val h = size.height
        val moonPath = Path().apply {
            // Crescent moon using two arcs
            val cx = w * 0.5f; val cy = h * 0.5f; val r = w * 0.35f
            moveTo(cx, cy - r)
            // Full circle arc
            cubicTo(cx + r * 1.3f, cy - r * 0.6f, cx + r * 1.3f, cy + r * 0.6f, cx, cy + r)
            // Inner cutout arc (smaller offset circle)
            cubicTo(cx - r * 0.2f, cy + r * 0.3f, cx - r * 0.2f, cy - r * 0.3f, cx, cy - r)
            close()
        }
        drawPath(moonPath, color = tint, style = Fill)
    }
}

// ── Settings half-sheet overlay ─────────────────────────────────────────────
@Composable
private fun SettingsSheet(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit
) {
    var showSignOutConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.TopEnd
    ) {
        Card(
            modifier = Modifier
                .fillMaxHeight(0.45f)
                .fillMaxWidth(0.75f)
                .padding(top = 60.dp, end = 12.dp)
                .clickable(enabled = false) { },
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(12.dp),
            colors = CardDefaults.cardColors(containerColor = Cream.copy(alpha = 0.97f))
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Appearance", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = SteelBlue, letterSpacing = 1.sp)
                Spacer(Modifier.height(14.dp))

                // Light mode button
                Button(
                    onClick = { if (isDarkMode) onToggleDarkMode() },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isDarkMode) Teal else Teal.copy(alpha = 0.25f),
                        contentColor = if (!isDarkMode) Cream else SteelBlueDark
                    ),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    SunIcon(tint = if (!isDarkMode) Cream else SteelBlueDark, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Light Mode", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                Spacer(Modifier.height(8.dp))

                // Night mode button
                Button(
                    onClick = { if (!isDarkMode) onToggleDarkMode() },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDarkMode) SteelBlueDark else SteelBlueDark.copy(alpha = 0.15f),
                        contentColor = if (isDarkMode) Cream else SteelBlueDark
                    ),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    MoonIcon(tint = if (isDarkMode) Cream else SteelBlueDark, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Night Mode", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                Spacer(Modifier.weight(1f))

                // Sign out button
                Button(
                    onClick = { showSignOutConfirm = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD9534F).copy(alpha = 0.85f),
                        contentColor = Cream
                    ),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Sign out", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Sign Out", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    // Sign out confirmation dialog
    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = { Text("Sign Out", fontWeight = FontWeight.Bold, color = SteelBlueDark) },
            text = { Text("Are you sure you want to sign out?", color = SteelBlueDark.copy(alpha = 0.8f)) },
            confirmButton = {
                Button(
                    onClick = { showSignOutConfirm = false; onSignOut() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9534F).copy(alpha = 0.85f))
                ) { Text("Sign Out", color = Cream) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSignOutConfirm = false }) { Text("Cancel") }
            },
            containerColor = Cream
        )
    }
}

// Interest icon helper
private fun interestIcon(name: String): String {
    return when {
        name.contains("hik", ignoreCase = true) || name.contains("trek", ignoreCase = true) -> "🥾"
        name.contains("music", ignoreCase = true) -> "🎵"
        name.contains("photo", ignoreCase = true) -> "📷"
        name.contains("cook", ignoreCase = true) || name.contains("food", ignoreCase = true) -> "🍳"
        name.contains("read", ignoreCase = true) || name.contains("book", ignoreCase = true) -> "📚"
        name.contains("sport", ignoreCase = true) || name.contains("gym", ignoreCase = true) -> "🏋️"
        name.contains("travel", ignoreCase = true) -> "✈️"
        name.contains("art", ignoreCase = true) || name.contains("paint", ignoreCase = true) -> "🎨"
        name.contains("game", ignoreCase = true) -> "🎮"
        name.contains("film", ignoreCase = true) || name.contains("movie", ignoreCase = true) -> "🎬"
        name.contains("swim", ignoreCase = true) -> "🏊"
        name.contains("bike", ignoreCase = true) || name.contains("cycl", ignoreCase = true) -> "🚴"
        name.contains("yoga", ignoreCase = true) -> "🧘"
        name.contains("dance", ignoreCase = true) -> "💃"
        name.contains("camp", ignoreCase = true) -> "⛺"
        else -> "⭐"
    }
}

@Composable
private fun ProfilePage(
    profile: Profile?,
    isLoading: Boolean,
    userInterests: List<Interest>,
    allInterests: List<Interest>,
    onAddHobbies: (List<Int>) -> Unit,
    onLoadAllInterests: () -> Unit,
    onSignOut: () -> Unit
) {
    var showHobbyPicker by remember { mutableStateOf(false) }
    var isDarkMode by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }
    var showSignOutConfirmTop by remember { mutableStateOf(false) }

    val bgColor = Color(180, 222, 189)
    val profileTextColor = Color(0xFF3A5A6E)
    val interestCardColor = Color(225, 245, 229)
    val interestChipColor = Color(210, 237, 216)

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(color = SteelBlue, modifier = Modifier.align(Alignment.Center))
        } else if (profile != null) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(bottom = 100.dp)
            ) {
                // ── Full-width background section (no card, no padding) ──
                Box(
                    modifier = Modifier.fillMaxWidth().background(bgColor)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().statusBarsPadding()
                            .padding(top = 24.dp, bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top-left: light/night toggle; Top-right: sign out
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { isDarkMode = !isDarkMode },
                                modifier = Modifier.size(44.dp)
                                    .shadow(4.dp, CircleShape)
                                    .background(if (isDarkMode) SteelBlueDark else Cream, CircleShape)
                            ) {
                                if (isDarkMode) MoonIcon(tint = Cream, modifier = Modifier.size(22.dp))
                                else SunIcon(tint = SteelBlueDark, modifier = Modifier.size(22.dp))
                            }
                            IconButton(
                                onClick = { showSignOutConfirmTop = true },
                                modifier = Modifier.size(44.dp)
                                    .shadow(4.dp, CircleShape)
                                    .background(Color(0xFFD9534F), CircleShape)
                            ) {
                                Icon(Icons.Default.ExitToApp, contentDescription = "Sign out",
                                    tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Centered avatar
                        Box(contentAlignment = Alignment.Center) {
                            Box(modifier = Modifier.size(130.dp).clip(CircleShape)
                                .background(Brush.sweepGradient(listOf(SteelBlue, Teal, Mint, CreamDark, SteelBlue))))
                            Box(
                                modifier = Modifier.size(120.dp)
                                    .shadow(8.dp, CircleShape, ambientColor = SteelBlue.copy(alpha = 0.4f))
                                    .clip(CircleShape).background(SteelBlueLight.copy(alpha = 0.35f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!profile.avatarUrl.isNullOrBlank()) {
                                    AsyncImage(model = profile.avatarUrl, contentDescription = "Profile photo",
                                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape))
                                } else {
                                    ProfileIcon(tint = Cream, modifier = Modifier.size(56.dp))
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(profile.username, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = profileTextColor)
                        profile.fullName?.let {
                            Text(it, fontSize = 13.sp, color = profileTextColor.copy(alpha = 0.75f), fontWeight = FontWeight.Medium)
                        }

                        Spacer(Modifier.height(16.dp))

                        // Friends left, Events right
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("0", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = profileTextColor)
                                Text("Friends", fontSize = 12.sp, color = profileTextColor.copy(alpha = 0.75f), fontWeight = FontWeight.SemiBold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("0", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = profileTextColor)
                                Text("Events", fontSize = 12.sp, color = profileTextColor.copy(alpha = 0.75f), fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // ── Interests card (lighter, overlaid on background) ──
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            shape = RoundedCornerShape(22.dp),
                            elevation = CardDefaults.cardElevation(4.dp),
                            colors = CardDefaults.cardColors(containerColor = interestCardColor)
                        ) {
                            Column(Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("⭐", fontSize = 16.sp)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Interests", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                                        color = profileTextColor, letterSpacing = 0.5.sp)
                                }

                                if (userInterests.isNotEmpty()) {
                                    Spacer(Modifier.height(14.dp))
                                    val chunked = userInterests.chunked(3)
                                    chunked.forEach { rowItems ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                                        ) {
                                            rowItems.forEach { interest ->
                                                Box(modifier = Modifier.clip(RoundedCornerShape(20.dp))
                                                    .background(interestChipColor)
                                                    .padding(horizontal = 10.dp, vertical = 5.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(interestIcon(interest.name), fontSize = 11.sp)
                                                        Spacer(Modifier.width(4.dp))
                                                        Text(interest.name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = profileTextColor)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(Modifier.height(14.dp))
                                    Text("No interests yet — add some!",
                                        fontSize = 13.sp, color = profileTextColor.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                                }

                                Spacer(Modifier.height(12.dp))

                                Button(
                                    onClick = { onLoadAllInterests(); showHobbyPicker = true },
                                    shape = RoundedCornerShape(50.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SteelBlue.copy(alpha = 0.85f),
                                        contentColor = Cream
                                    ),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Add", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                    }
                }

            }
        } else {
            Text("Could not load profile", color = SteelBlueDark, modifier = Modifier.align(Alignment.Center))
        }

        // Hobby picker overlay
        if (showHobbyPicker) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f))
                .clickable { showHobbyPicker = false }, contentAlignment = Alignment.Center) {
                HobbyPickerSheet(allInterests = allInterests, currentInterestIds = userInterests.map { it.id }.toSet(),
                    onConfirm = { ids -> onAddHobbies(ids); showHobbyPicker = false },
                    onDismiss = { showHobbyPicker = false })
            }
        }

        // Sign out confirmation dialog (from top button)
        if (showSignOutConfirmTop) {
            AlertDialog(
                onDismissRequest = { showSignOutConfirmTop = false },
                title = { Text("Sign Out", fontWeight = FontWeight.Bold, color = SteelBlueDark) },
                text = { Text("Are you sure you want to sign out?", color = SteelBlueDark.copy(alpha = 0.8f)) },
                confirmButton = {
                    Button(
                        onClick = { showSignOutConfirmTop = false; onSignOut() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9534F).copy(alpha = 0.85f))
                    ) { Text("Sign Out", color = Cream) }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showSignOutConfirmTop = false }) { Text("Cancel") }
                },
                containerColor = Cream
            )
        }
    }
}


// ══════════════════════════════════════════════════════════════════════════════
//  CALENDAR PAGE (placeholder — replaces Explore)
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun CalendarPage() {
    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Teal.copy(alpha = 0.15f), Cream, Mint.copy(alpha = 0.1f)))
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CalendarIcon(tint = SteelBlue, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            Text("Events Calendar", fontSize = 22.sp, color = SteelBlueDark, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text("Coming soon", fontSize = 14.sp, color = SteelBlue.copy(alpha = 0.6f))
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  MAP PAGE
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun MapPage(
    events: List<Event>,
    userId: String,
    onEventClick: (Event) -> Unit,
    onPlaceClick: (org.example.project.data.model.PlaceResult) -> Unit,
    searchQuery: String,
    onSearchConsumed: () -> Unit
) {
    MapView(
        events = events,
        userId = userId,
        onEventClick = onEventClick,
        onPlaceClick = onPlaceClick,
        searchQuery = searchQuery,
        onSearchConsumed = onSearchConsumed,
        modifier = Modifier.fillMaxSize()
    )
}

// ══════════════════════════════════════════════════════════════════════════════
//  MAIN SCREEN
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun MainScreen(
    onSignOut: () -> Unit,
    userId: String,
    profileViewModel: ProfileViewModel,
    eventViewModel: EventViewModel,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(userId) {
        profileViewModel.loadProfile(userId)
        profileViewModel.loadAllInterests()
        eventViewModel.loadPublicEvents()
    }

    val profile by profileViewModel.profile.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val userInterests by profileViewModel.userInterests.collectAsState()
    val allInterests by profileViewModel.allInterests.collectAsState()
    val events by eventViewModel.events.collectAsState()

    // 0 = calendar, 1 = map, 2 = profile
    var selectedTab by remember { mutableStateOf(1) }
    var mapSearchQuery by remember { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize()) {
        when (selectedTab) {
            0 -> CalendarPage()
            1 -> MapPage(events = events, userId = userId, onEventClick = {}, onPlaceClick = {},
                searchQuery = mapSearchQuery, onSearchConsumed = { mapSearchQuery = "" })
            2 -> ProfilePage(
                profile = profile, isLoading = isLoading, userInterests = userInterests,
                allInterests = allInterests,
                onAddHobbies = { ids -> profileViewModel.setUserInterests(userId, ids) },
                onLoadAllInterests = { profileViewModel.loadAllInterests() },
                onSignOut = onSignOut
            )
        }

        // Top buttons (map only)
        if (selectedTab == 1) {
            var showSearchBar by remember { mutableStateOf(false) }
            var searchText by remember { mutableStateOf("") }

            if (showSearchBar) {
                // Full search bar at top
                Box(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp).align(Alignment.TopCenter)
                ) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = TextDark, fontSize = 15.sp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            imeAction = androidx.compose.ui.text.input.ImeAction.Search
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onSearch = {
                                if (searchText.isNotBlank()) {
                                    // trigger search via MapPage — pass callback up
                                    mapSearchQuery = searchText
                                    showSearchBar = false
                                    searchText = ""
                                }
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(24.dp))
                            .background(BarWhite, RoundedCornerShape(24.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        decorationBox = { inner ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Search, null, tint = SteelBlue, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Box { if (searchText.isEmpty()) Text("Search location…", color = SteelBlue.copy(alpha = 0.5f), fontSize = 15.sp); inner() }
                                Spacer(Modifier.weight(1f))
                                IconButton(onClick = { showSearchBar = false; searchText = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, null, tint = SteelBlue, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    )
                }
            } else {
                IconButton(onClick = { showSearchBar = true }, modifier = Modifier.statusBarsPadding().padding(start = 16.dp, top = 12.dp)
                    .align(Alignment.TopStart).shadow(6.dp, CircleShape).background(BarWhite, CircleShape).size(44.dp)) {
                    Icon(Icons.Default.Search, "Search place", tint = SteelBlueDark, modifier = Modifier.size(22.dp))
                }
            }
            IconButton(onClick = { }, modifier = Modifier.statusBarsPadding().padding(end = 16.dp, top = 12.dp)
                .align(Alignment.TopEnd).shadow(6.dp, CircleShape).background(BarWhite, CircleShape).size(44.dp)) {
                SuggestionIcon(tint = SteelBlueDark, modifier = Modifier.size(24.dp))
            }
        }

        // ── Bottom nav bar ───────────────────────────────────────────────
        Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
            .padding(horizontal = 20.dp).padding(bottom = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(28.dp))
                    .background(Brush.horizontalGradient(listOf(Cream, BarWhite, Cream)), RoundedCornerShape(28.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT — Calendar
                IconButton(onClick = { selectedTab = 0 }, modifier = Modifier.size(44.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CalendarIcon(tint = if (selectedTab == 0) SteelBlue else SteelBlueLight.copy(alpha = 0.6f),
                            modifier = Modifier.size(22.dp))
                        if (selectedTab == 0) SelectedDot()
                    }
                }

                // MIDDLE — Location arrow (iPhone style)
                IconButton(onClick = { selectedTab = 1 }, modifier = Modifier.size(44.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LocationArrowIcon(tint = if (selectedTab == 1) SteelBlue else SteelBlueLight.copy(alpha = 0.6f),
                            modifier = Modifier.size(22.dp))
                        if (selectedTab == 1) SelectedDot()
                    }
                }

                // RIGHT — Profile
                IconButton(onClick = { selectedTab = 2 }, modifier = Modifier.size(44.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ProfileIcon(tint = if (selectedTab == 2) SteelBlue else SteelBlueLight.copy(alpha = 0.6f),
                            modifier = Modifier.size(22.dp))
                        if (selectedTab == 2) SelectedDot()
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedDot() {
    Box(modifier = Modifier.padding(top = 4.dp).size(5.dp).clip(CircleShape)
        .background(Brush.horizontalGradient(listOf(SteelBlue, Teal))))
}
