package org.example.project.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas

// ── palette refs ──────────────────────────────────────────────────────────────
private val SteelBlue     = Color(0xFF80A1BA)
private val SteelBlueDark = Color(0xFF5A7E9A)
private val Teal          = Color(0xFF91C4C3)
private val Mint          = Color(0xFFB4DEBD)
private val Cream         = Color(0xFFFFF7DD)
private val Beige         = Color(0xFFF5ECD7)
private val BarWhite      = Color(0xFFFCFBF7)

// ── Custom icon: profile silhouette (head + shoulders, Instagram-style) ──────
@Composable
private fun ProfileIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(26.dp)) {
        val w = size.width
        val h = size.height

        // Head — circle at top-center
        drawCircle(
            color = tint,
            radius = w * 0.22f,
            center = Offset(w * 0.5f, h * 0.28f)
        )

        // Shoulders — a rounded arc from bottom-left to bottom-right
        val shoulderPath = Path().apply {
            moveTo(w * 0.08f, h * 0.95f)
            cubicTo(
                w * 0.08f, h * 0.58f,
                w * 0.28f, h * 0.48f,
                w * 0.5f, h * 0.48f
            )
            cubicTo(
                w * 0.72f, h * 0.48f,
                w * 0.92f, h * 0.58f,
                w * 0.92f, h * 0.95f
            )
        }
        drawPath(shoulderPath, color = tint, style = Fill)
    }
}

// ── Custom icon: question-mark ───────────────────────────────────────────────
@Composable
private fun QuestionMarkIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(26.dp)) {
        val w = size.width
        val h = size.height

        // Circle bg
        drawCircle(
            color = tint.copy(alpha = 0.15f),
            radius = w * 0.48f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        // The "?" curve
        val qPath = Path().apply {
            moveTo(w * 0.36f, h * 0.32f)
            cubicTo(
                w * 0.36f, h * 0.14f,
                w * 0.64f, h * 0.14f,
                w * 0.64f, h * 0.32f
            )
            cubicTo(
                w * 0.64f, h * 0.44f,
                w * 0.50f, h * 0.44f,
                w * 0.50f, h * 0.56f
            )
        }
        drawPath(qPath, color = tint, style = Stroke(width = w * 0.08f))

        // Dot
        drawCircle(
            color = tint,
            radius = w * 0.05f,
            center = Offset(w * 0.5f, h * 0.72f)
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  MAIN SCREEN
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun MainScreen(
    onSignOut: () -> Unit,
    userId: String,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(1) } // 0=explore, 1=map, 2=profile

    Box(modifier = modifier.fillMaxSize()) {

        // ── Map placeholder (beige) ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Beige)
        )

        // ── Top-left: Search button ──────────────────────────────────────────
        IconButton(
            onClick = { /* TODO: open search */ },
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 16.dp, top = 12.dp)
                .align(Alignment.TopStart)
                .shadow(6.dp, CircleShape)
                .background(BarWhite, CircleShape)
                .size(44.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search place",
                tint = SteelBlueDark,
                modifier = Modifier.size(22.dp)
            )
        }

        // ── Top-right: AI suggestions button ─────────────────────────────────
        IconButton(
            onClick = { /* TODO: open AI suggestions */ },
            modifier = Modifier
                .statusBarsPadding()
                .padding(end = 16.dp, top = 12.dp)
                .align(Alignment.TopEnd)
                .shadow(6.dp, CircleShape)
                .background(BarWhite, CircleShape)
                .size(44.dp)
        ) {
            QuestionMarkIcon(tint = SteelBlueDark, modifier = Modifier.size(24.dp))
        }

        // ── Bottom navigation bar ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
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
                // ── Explore (left) ───────────────────────────────────────────
                IconButton(
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.size(52.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = "Explore",
                            tint = if (selectedTab == 0) SteelBlue else Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(26.dp)
                        )
                        if (selectedTab == 0) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(SteelBlue)
                            )
                        }
                    }
                }

                // ── Map / Location (center) ──────────────────────────────────
                IconButton(
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.size(52.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Map",
                            tint = if (selectedTab == 1) SteelBlue else Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(26.dp)
                        )
                        if (selectedTab == 1) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(SteelBlue)
                            )
                        }
                    }
                }

                // ── Profile (right) ──────────────────────────────────────────
                IconButton(
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.size(52.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ProfileIcon(
                            tint = if (selectedTab == 2) SteelBlue else Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(26.dp)
                        )
                        if (selectedTab == 2) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(SteelBlue)
                            )
                        }
                    }
                }
            }
        }
    }
}
