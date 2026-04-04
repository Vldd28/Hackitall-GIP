package org.example.project.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.example.project.data.model.Profile
import org.example.project.viewmodel.ProfileViewModel

private val SteelBlue     = Color(0xFF80A1BA)
private val SteelBlueDark = Color(0xFF5A7E9A)
private val Teal          = Color(0xFF91C4C3)
private val Mint          = Color(0xFFB4DEBD)
private val Cream         = Color(0xFFFFF7DD)
private val Beige         = Color(0xFFF5ECD7)
private val BarWhite      = Color(0xFFFCFBF7)

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

@Composable
private fun ProfilePage(
    profile: Profile?,
    isLoading: Boolean,
    onSignOut: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                0.0f to SteelBlue.copy(alpha = 0.25f),
                0.35f to Cream,
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
                Spacer(Modifier.height(60.dp))

                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .shadow(12.dp, CircleShape, ambientColor = SteelBlue.copy(alpha = 0.3f))
                        .clip(CircleShape)
                        .background(Color(0xFFD0D0D0)),
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
                        ProfileIcon(tint = Color.White, modifier = Modifier.size(64.dp))
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = profile.username,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = SteelBlueDark
                )

                profile.fullName?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(it, fontSize = 15.sp, color = SteelBlue)
                }

                profile.country?.let {
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Teal, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(it, fontSize = 14.sp, color = SteelBlue.copy(alpha = 0.8f))
                    }
                }

                Spacer(Modifier.height(24.dp))

                profile.bio?.let { bio ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("About me", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Teal, letterSpacing = 1.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(bio, fontSize = 15.sp, lineHeight = 22.sp, color = Color(0xFF3A4A55))
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatCard("Trips", "0", SteelBlue)
                    StatCard("Friends", "0", Teal)
                    StatCard("Groups", "0", Mint)
                }

                Spacer(Modifier.height(32.dp))

                OutlinedButton(
                    onClick = onSignOut,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SteelBlueDark),
                    modifier = Modifier.padding(horizontal = 28.dp).fillMaxWidth()
                ) {
                    Text("Sign Out", fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            Text("Could not load profile", color = SteelBlueDark, modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        modifier = Modifier.width(95.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(Modifier.height(2.dp))
            Text(label, fontSize = 12.sp, color = color.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun ExplorePage() {
    Box(modifier = Modifier.fillMaxSize().background(Cream), contentAlignment = Alignment.Center) {
        Text("Explore", fontSize = 22.sp, color = SteelBlueDark, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MapPage() {
    Box(modifier = Modifier.fillMaxSize().background(Beige))
}

@Composable
fun MainScreen(
    onSignOut: () -> Unit,
    userId: String,
    profileViewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(userId) { profileViewModel.loadProfile(userId) }

    val profile by profileViewModel.profile.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()

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
            1 -> MapPage()
            2 -> ProfilePage(profile = profile, isLoading = isLoading, onSignOut = onSignOut)
        }

        if (selectedTab == 1) {
            IconButton(
                onClick = { },
                modifier = Modifier
                    .statusBarsPadding().padding(start = 16.dp, top = 12.dp)
                    .align(Alignment.TopStart)
                    .shadow(6.dp, CircleShape).background(BarWhite, CircleShape).size(44.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search place", tint = SteelBlueDark, modifier = Modifier.size(22.dp))
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

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter).fillMaxWidth()
                .navigationBarsPadding().padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(28.dp))
                    .background(BarWhite, RoundedCornerShape(28.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedTab = 0 }, modifier = Modifier.size(52.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Explore, "Explore",
                            tint = if (selectedTab == 0) SteelBlue else Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(26.dp))
                        if (selectedTab == 0) SelectedDot()
                    }
                }

                IconButton(onClick = { selectedTab = 1 }, modifier = Modifier.size(52.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LocationOn, "Map",
                            tint = if (selectedTab == 1) SteelBlue else Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(26.dp))
                        if (selectedTab == 1) SelectedDot()
                    }
                }

                IconButton(onClick = { selectedTab = 2 }, modifier = Modifier.size(52.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ProfileIcon(
                            tint = if (selectedTab == 2) SteelBlue else Color.Gray.copy(alpha = 0.5f),
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
    Box(modifier = Modifier.padding(top = 4.dp).size(5.dp).clip(CircleShape).background(SteelBlue))
}
