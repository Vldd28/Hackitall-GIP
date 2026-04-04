package org.example.project.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
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

// Question mark icon
@Composable
private fun QuestionMarkIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(26.dp)) {
        val w = size.width; val h = size.height
        drawCircle(color = tint.copy(alpha = 0.15f), radius = w * 0.48f, center = Offset(w * 0.5f, h * 0.5f))
        val q = Path().apply {
            moveTo(w * 0.36f, h * 0.32f)
            cubicTo(w * 0.36f, h * 0.14f, w * 0.64f, h * 0.14f, w * 0.64f, h * 0.32f)
            cubicTo(w * 0.64f, h * 0.44f, w * 0.50f, h * 0.44f, w * 0.50f, h * 0.56f)
        }
        drawPath(q, color = tint, style = Stroke(width = w * 0.08f))
        drawCircle(color = tint, radius = w * 0.05f, center = Offset(w * 0.5f, h * 0.72f))
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
                            val bgColor = chipColorForIndex(allInterests.indexOf(interest))
                            Box(
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) bgColor else bgColor.copy(alpha = 0.18f))
                                    .border(if (isSelected) 2.dp else 1.dp, if (isSelected) bgColor else bgColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
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

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                0.0f to SteelBlue.copy(alpha = 0.30f),
                0.15f to Teal.copy(alpha = 0.12f),
                0.35f to Cream,
                0.65f to Mint.copy(alpha = 0.08f),
                0.85f to Cream,
                1.0f to SteelBlue.copy(alpha = 0.06f)
            )
        )
    ) {
        // Decorative background routes
        ProfileBackground()

        if (isLoading) {
            CircularProgressIndicator(color = SteelBlue, modifier = Modifier.align(Alignment.Center))
        } else if (profile != null) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .padding(top = 16.dp, bottom = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(28.dp))

                // ── Header card: avatar + name + country ─────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(containerColor = Cream.copy(alpha = 0.85f))
                ) {
                    // Teal accent bar at the top of the card
                    Box(modifier = Modifier.fillMaxWidth().height(4.dp)
                        .background(Brush.horizontalGradient(listOf(SteelBlue, Teal, Mint))))

                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar with gradient ring
                        Box(contentAlignment = Alignment.Center) {
                            Box(modifier = Modifier.size(120.dp).clip(CircleShape)
                                .background(Brush.sweepGradient(listOf(SteelBlue, Teal, Mint, CreamDark, SteelBlue))))
                            Box(
                                modifier = Modifier.size(110.dp)
                                    .shadow(10.dp, CircleShape, ambientColor = SteelBlue.copy(alpha = 0.4f))
                                    .clip(CircleShape).background(SteelBlueLight.copy(alpha = 0.35f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!profile.avatarUrl.isNullOrBlank()) {
                                    AsyncImage(model = profile.avatarUrl, contentDescription = "Profile photo",
                                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape))
                                } else {
                                    ProfileIcon(tint = Cream, modifier = Modifier.size(54.dp))
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Username
                        Text(profile.username, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = SteelBlueDark)

                        profile.fullName?.let {
                            Text(it, fontSize = 14.sp, color = SteelBlue, fontWeight = FontWeight.Medium)
                        }

                        // Country pill
                        profile.country?.let {
                            Spacer(Modifier.height(8.dp))
                            Box(modifier = Modifier.clip(RoundedCornerShape(20.dp))
                                .background(Brush.horizontalGradient(listOf(Teal.copy(alpha = 0.2f), Mint.copy(alpha = 0.15f))))
                                .padding(horizontal = 14.dp, vertical = 5.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, null, tint = Teal, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(it, fontSize = 13.sp, color = TealDark, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Bio — travel journal style ───────────────────────────────
                profile.bio?.let { bio ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(22.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(containerColor = Mint.copy(alpha = 0.2f))
                    ) {
                        Row(Modifier.padding(18.dp)) {
                            // Decorative travel quote bar
                            Box(modifier = Modifier.width(4.dp).height(60.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Brush.verticalGradient(listOf(Teal, Mint))))
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text("Travel Journal", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                    color = TealDark, letterSpacing = 1.5.sp)
                                Spacer(Modifier.height(6.dp))
                                Text(bio, fontSize = 14.sp, lineHeight = 21.sp, color = TextDark)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // ── Passport stamps (stats) ──────────────────────────────────
                Text("Passport Stamps", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    color = SteelBlueDark.copy(alpha = 0.5f), letterSpacing = 2.sp,
                    modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth())
                Spacer(Modifier.height(8.dp))

                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), Arrangement.spacedBy(10.dp)) {
                    PassportStamp(emoji = "✈", label = "Trips", value = "0", color = SteelBlue, modifier = Modifier.weight(1f))
                    PassportStamp(emoji = "🤝", label = "Friends", value = "0", color = Teal, modifier = Modifier.weight(1f))
                    PassportStamp(emoji = "🌍", label = "Groups", value = "0", color = Mint, modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(16.dp))

                // ── Hobbies — suitcase stickers style ────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(22.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = CreamDark.copy(alpha = 0.2f))
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🏷", fontSize = 16.sp)
                                Spacer(Modifier.width(6.dp))
                                Text("Travel Interests", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                                    color = SteelBlueDark, letterSpacing = 0.5.sp)
                            }
                            Box(modifier = Modifier.clip(RoundedCornerShape(12.dp))
                                .background(Brush.horizontalGradient(listOf(SteelBlue, Teal)))
                                .clickable { onLoadAllInterests(); showHobbyPicker = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, null, tint = Cream, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Add", fontSize = 12.sp, color = Cream, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        if (userInterests.isNotEmpty()) {
                            Spacer(Modifier.height(14.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(userInterests) { interest ->
                                    val chipColor = chipColorForIndex(interest.id)
                                    Box(modifier = Modifier.clip(RoundedCornerShape(14.dp))
                                        .background(chipColor.copy(alpha = 0.82f))
                                        .border(1.dp, chipColor, RoundedCornerShape(14.dp))
                                        .padding(horizontal = 14.dp, vertical = 8.dp)) {
                                        Text(interest.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Cream)
                                    }
                                }
                            }
                        } else {
                            Spacer(Modifier.height(14.dp))
                            Text("Your suitcase is empty — add some stickers!",
                                fontSize = 13.sp, color = SteelBlue.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Sign out ─────────────────────────────────────────────────
                OutlinedButton(onClick = onSignOut, shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SteelBlueDark),
                    border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(SteelBlue, Teal))),
                    modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()
                ) { Text("Sign Out", fontWeight = FontWeight.SemiBold) }
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
    }
}

// Passport stamp style stat card
@Composable
private fun PassportStamp(emoji: String, label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        modifier = modifier
    ) {
        Column(
            Modifier.padding(vertical = 14.dp, horizontal = 8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 20.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, fontSize = 10.sp, color = color.copy(alpha = 0.7f), fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
        }
    }
}

// Kept for backwards compat if needed elsewhere
@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    PassportStamp(emoji = "", label = label, value = value, color = color, modifier = modifier)
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
private fun MapPage(events: List<Event>, onEventClick: (Event) -> Unit, onPlaceClick: (org.example.project.data.model.PlaceResult) -> Unit) {
    MapView(events = events, onEventClick = onEventClick, onPlaceClick = onPlaceClick, modifier = Modifier.fillMaxSize())
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

    // 0 = profile, 1 = map, 2 = calendar
    var selectedTab by remember { mutableStateOf(1) }

    Box(
        modifier = modifier.fillMaxSize().pointerInput(selectedTab) {
            detectHorizontalDragGestures { _, dragAmount ->
                if (dragAmount < -40f && selectedTab < 2) selectedTab++
                else if (dragAmount > 40f && selectedTab > 0) selectedTab--
            }
        }
    ) {
        when (selectedTab) {
            0 -> ProfilePage(
                profile = profile, isLoading = isLoading, userInterests = userInterests,
                allInterests = allInterests,
                onAddHobbies = { ids -> profileViewModel.setUserInterests(userId, ids) },
                onLoadAllInterests = { profileViewModel.loadAllInterests() },
                onSignOut = onSignOut
            )
            1 -> MapPage(events = events, onEventClick = {}, onPlaceClick = {})
            2 -> CalendarPage()
        }

        // Top buttons (map only)
        if (selectedTab == 1) {
            IconButton(onClick = { }, modifier = Modifier.statusBarsPadding().padding(start = 16.dp, top = 12.dp)
                .align(Alignment.TopStart).shadow(6.dp, CircleShape).background(BarWhite, CircleShape).size(44.dp)) {
                Icon(Icons.Default.Search, "Search place", tint = SteelBlueDark, modifier = Modifier.size(22.dp))
            }
            IconButton(onClick = { }, modifier = Modifier.statusBarsPadding().padding(end = 16.dp, top = 12.dp)
                .align(Alignment.TopEnd).shadow(6.dp, CircleShape).background(BarWhite, CircleShape).size(44.dp)) {
                QuestionMarkIcon(tint = SteelBlueDark, modifier = Modifier.size(24.dp))
            }
        }

        // ── Bottom nav bar ───────────────────────────────────────────────
        Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
            .navigationBarsPadding().padding(horizontal = 24.dp, vertical = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(28.dp))
                    .background(Brush.horizontalGradient(listOf(Cream, BarWhite, Cream)), RoundedCornerShape(28.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT — Profile
                IconButton(onClick = { selectedTab = 0 }, modifier = Modifier.size(52.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ProfileIcon(tint = if (selectedTab == 0) SteelBlue else SteelBlueLight.copy(alpha = 0.6f),
                            modifier = Modifier.size(26.dp))
                        if (selectedTab == 0) SelectedDot()
                    }
                }

                // MIDDLE — Location arrow (iPhone style)
                IconButton(onClick = { selectedTab = 1 }, modifier = Modifier.size(52.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LocationArrowIcon(tint = if (selectedTab == 1) SteelBlue else SteelBlueLight.copy(alpha = 0.6f),
                            modifier = Modifier.size(26.dp))
                        if (selectedTab == 1) SelectedDot()
                    }
                }

                // RIGHT — Calendar / Events
                IconButton(onClick = { selectedTab = 2 }, modifier = Modifier.size(52.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CalendarIcon(tint = if (selectedTab == 2) SteelBlue else SteelBlueLight.copy(alpha = 0.6f),
                            modifier = Modifier.size(26.dp))
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
