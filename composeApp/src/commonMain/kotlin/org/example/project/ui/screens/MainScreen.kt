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
import androidx.compose.material.icons.filled.Explore
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
private val Beige          = Color(0xFFF5ECD7)
private val BarWhite       = Color(0xFFFCFBF7)
private val TextDark       = Color(0xFF1E2D3A)

// Hobby chip colors — cycle through these for visual variety
private val chipColors = listOf(SteelBlue, Teal, Mint, CreamDark, TealDark, MintDark, SteelBlueDark, SteelBlueLight)

private fun chipColorForIndex(i: Int): Color = chipColors[i % chipColors.size]

// ══════════════════════════════════════════════════════════════════════════════
//  Custom drawn icons
// ══════════════════════════════════════════════════════════════════════════════

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
//  HOBBY PICKER — modal-style panel
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(16.dp, RoundedCornerShape(24.dp), ambientColor = SteelBlue.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Cream.copy(alpha = 0.97f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pick your hobbies", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SteelBlueDark)
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, "Close", tint = SteelBlue, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(4.dp))
            Text("Tap to select, then hit save", fontSize = 12.sp, color = Teal)

            Spacer(Modifier.height(16.dp))

            // Interests grid — wrapping flow
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Chunk into rows of 3
                allInterests.chunked(3).forEach { rowItems ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowItems.forEachIndexed { idx, interest ->
                            val isSelected = interest.id in selected
                            val bgColor = chipColorForIndex(allInterests.indexOf(interest))

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSelected) bgColor
                                        else bgColor.copy(alpha = 0.18f)
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) bgColor else bgColor.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        selected = selected.toMutableSet().apply {
                                            if (isSelected) remove(interest.id) else add(interest.id)
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check, null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = interest.name,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextDark,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        // Fill remaining space if row has < 3 items
                        repeat(3 - rowItems.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Save button
            Button(
                onClick = { onConfirm(selected.toList()) },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SteelBlue),
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) {
                Text("Save Hobbies (${selected.size})", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  PROFILE PAGE
// ══════════════════════════════════════════════════════════════════════════════

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
                0.0f to SteelBlue.copy(alpha = 0.35f),
                0.25f to Teal.copy(alpha = 0.15f),
                0.5f to Cream,
                0.8f to Mint.copy(alpha = 0.12f),
                1.0f to Cream
            )
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = SteelBlue, modifier = Modifier.align(Alignment.Center))
        } else if (profile != null) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(48.dp))

                // ── Avatar with colored ring ─────────────────────────────────
                Box(contentAlignment = Alignment.Center) {
                    // Gradient ring
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(listOf(SteelBlue, Teal, Mint, CreamDark, SteelBlue))
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .shadow(12.dp, CircleShape, ambientColor = SteelBlue.copy(alpha = 0.4f))
                            .clip(CircleShape)
                            .background(SteelBlueLight.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!profile.avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = profile.avatarUrl,
                                contentDescription = "Profile photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            ProfileIcon(tint = Cream, modifier = Modifier.size(64.dp))
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // ── Username ─────────────────────────────────────────────────
                Text(
                    text = profile.username,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SteelBlueDark
                )

                profile.fullName?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(it, fontSize = 15.sp, color = SteelBlue, fontWeight = FontWeight.Medium)
                }

                // ── Country pill ─────────────────────────────────────────────
                profile.country?.let {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Teal.copy(alpha = 0.2f))
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = Teal, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(it, fontSize = 13.sp, color = TealDark, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Bio card ─────────────────────────────────────────────────
                profile.bio?.let { bio ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Mint.copy(alpha = 0.22f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("About me", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                color = MintDark, letterSpacing = 1.5.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(bio, fontSize = 15.sp, lineHeight = 22.sp, color = TextDark)
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                }

                // ── Stats row ────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("Trips", "0", SteelBlue, Modifier.weight(1f))
                    StatCard("Friends", "0", Teal, Modifier.weight(1f))
                    StatCard("Groups", "0", Mint, Modifier.weight(1f))
                }

                Spacer(Modifier.height(24.dp))

                // ── My Hobbies section ───────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = SteelBlue.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("My Hobbies", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                                color = SteelBlueDark, letterSpacing = 1.sp)
                            // Add button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SteelBlue)
                                    .clickable {
                                        onLoadAllInterests()
                                        showHobbyPicker = true
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, null, tint = Cream, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Add", fontSize = 12.sp, color = Cream, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        if (userInterests.isNotEmpty()) {
                            Spacer(Modifier.height(14.dp))
                            // Hobby chips — scrollable row with colorful tags
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(userInterests) { interest ->
                                    val color = chipColorForIndex(interest.id)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(color.copy(alpha = 0.85f))
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            interest.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Cream
                                        )
                                    }
                                }
                            }
                        } else {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "No hobbies yet — tap Add to pick some!",
                                fontSize = 13.sp,
                                color = SteelBlue.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                // ── Sign out ─────────────────────────────────────────────────
                OutlinedButton(
                    onClick = onSignOut,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SteelBlueDark),
                    border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(SteelBlue, Teal))),
                    modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth()
                ) {
                    Text("Sign Out", fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            Text("Could not load profile", color = SteelBlueDark, modifier = Modifier.align(Alignment.Center))
        }

        // ── Hobby picker overlay ─────────────────────────────────────────
        AnimatedVisibility(
            visible = showHobbyPicker,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.CenterVertically),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.CenterVertically),
            modifier = Modifier.align(Alignment.Center)
        ) {
            // Dim background
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)).clickable { showHobbyPicker = false })
        }

        if (showHobbyPicker) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f))
                    .clickable { showHobbyPicker = false },
                contentAlignment = Alignment.Center
            ) {
                HobbyPickerSheet(
                    allInterests = allInterests,
                    currentInterestIds = userInterests.map { it.id }.toSet(),
                    onConfirm = { ids ->
                        onAddHobbies(ids)
                        showHobbyPicker = false
                    },
                    onDismiss = { showHobbyPicker = false }
                )
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.18f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Spacer(Modifier.height(2.dp))
            Text(label, fontSize = 11.sp, color = color.copy(alpha = 0.75f), fontWeight = FontWeight.Medium)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  EXPLORE PAGE (placeholder)
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ExplorePage() {
    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Teal.copy(alpha = 0.15f), Cream, Mint.copy(alpha = 0.1f)))
        ),
        contentAlignment = Alignment.Center
    ) {
        Text("Explore", fontSize = 22.sp, color = SteelBlueDark, fontWeight = FontWeight.SemiBold)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  MAP PAGE
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun MapPage(events: List<Event>, userId: String, onEventClick: (Event) -> Unit, onPlaceClick: (org.example.project.data.model.PlaceResult) -> Unit) {
    MapView(
        events = events,
        userId = userId,
        onEventClick = onEventClick,
        onPlaceClick = onPlaceClick,
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
            0 -> ExplorePage()
            1 -> MapPage(events = events, userId = userId, onEventClick = {}, onPlaceClick = {})
            2 -> ProfilePage(
                profile = profile,
                isLoading = isLoading,
                userInterests = userInterests,
                allInterests = allInterests,
                onAddHobbies = { ids -> profileViewModel.setUserInterests(userId, ids) },
                onLoadAllInterests = { profileViewModel.loadAllInterests() },
                onSignOut = onSignOut
            )
        }

        if (selectedTab == 1) {
            IconButton(
                onClick = { },
                modifier = Modifier
                    .statusBarsPadding().padding(start = 16.dp, top = 12.dp)
                    .align(Alignment.TopStart)
                    .shadow(6.dp, CircleShape).background(BarWhite, CircleShape).size(44.dp)
            ) {
                Icon(Icons.Default.Search, "Search place", tint = SteelBlueDark, modifier = Modifier.size(22.dp))
            }

            IconButton(
                onClick = { },
                modifier = Modifier
                    .statusBarsPadding().padding(end = 16.dp, top = 12.dp)
                    .align(Alignment.TopEnd)
                    .shadow(6.dp, CircleShape).background(BarWhite, CircleShape).size(44.dp)
            ) {
                QuestionMarkIcon(tint = SteelBlueDark, modifier = Modifier.size(24.dp))
            }
        }

        // ── Bottom nav bar ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter).fillMaxWidth()
                .navigationBarsPadding().padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(28.dp))
                    .background(
                        Brush.horizontalGradient(listOf(Cream, BarWhite, Cream)),
                        RoundedCornerShape(28.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedTab = 0 }, modifier = Modifier.size(52.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Explore, "Explore",
                            tint = if (selectedTab == 0) SteelBlue else SteelBlueLight.copy(alpha = 0.6f),
                            modifier = Modifier.size(26.dp))
                        if (selectedTab == 0) SelectedDot()
                    }
                }

                IconButton(onClick = { selectedTab = 1 }, modifier = Modifier.size(52.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LocationOn, "Map",
                            tint = if (selectedTab == 1) SteelBlue else SteelBlueLight.copy(alpha = 0.6f),
                            modifier = Modifier.size(26.dp))
                        if (selectedTab == 1) SelectedDot()
                    }
                }

                IconButton(onClick = { selectedTab = 2 }, modifier = Modifier.size(52.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ProfileIcon(
                            tint = if (selectedTab == 2) SteelBlue else SteelBlueLight.copy(alpha = 0.6f),
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
    Box(
        modifier = Modifier.padding(top = 4.dp).size(5.dp).clip(CircleShape)
            .background(Brush.horizontalGradient(listOf(SteelBlue, Teal)))
    )
}
