package org.example.project.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.viewmodel.AuthUiState

private val SteelBlue = Color(0xFF80A1BA)
private val Teal      = Color(0xFF91C4C3)
private val Mint      = Color(0xFFB4DEBD)
private val Cream     = Color(0xFFFFF7DD)
private val SteelBlueDark = Color(0xFF5A7E9A)

private data class Tile(val emoji: String, val city: String, val rotation: Float, val bg: Color)

private val tiles = listOf(
    Tile("🗼", "Paris",      -7f,  SteelBlue),
    Tile("🌉", "London",      5f,  Teal),
    Tile("🏛️", "Rome",       -3f,  Mint),
    Tile("🕰️", "Big Ben",     8f,  Cream),
    Tile("⛪", "Barcelona",  -5f,  SteelBlue),
    Tile("⛲", "Trevi",       3f,  Teal),
    Tile("🚲", "Amsterdam",  -9f,  Mint),
    Tile("🏰", "Prague",      6f,  Cream),
    Tile("🌊", "Santorini",  -2f,  SteelBlue),
    Tile("🛶", "Venice",      7f,  Teal),
    Tile("🎭", "Vienna",     -6f,  Mint),
    Tile("🌅", "Dubrovnik",   2f,  Cream),
    Tile("🌃", "Budapest",   -4f,  SteelBlue),
    Tile("🍕", "Naples",      9f,  Teal),
    Tile("🥐", "Bruges",     -1f,  Mint),
    Tile("💃", "Madrid",      5f,  Cream),
    Tile("🎵", "Lisbon",     -6f,  SteelBlue),
    Tile("🏺", "Athens",      3f,  Teal),
    Tile("🎪", "Berlin",     -4f,  Mint),
    Tile("🏝️", "Mykonos",    7f,  Cream),
    Tile("🗿", "Valletta",   -3f,  SteelBlue),
    Tile("🌄", "Salzburg",    6f,  Teal),
    Tile("🏔️", "Innsbruck",  -8f,  Mint),
    Tile("🎠", "Bruges",      4f,  Cream),
)

@Composable
private fun TravelCollageBackground() {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val tileW = 110
        val tileH = 130
        val cols = (maxWidth.value / tileW).toInt() + 2
        val rows = (maxHeight.value / tileH).toInt() + 2

        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFD4B896)))

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val tile = tiles[(row * cols + col) % tiles.size]
                Box(
                    modifier = Modifier
                        .offset(x = (col * tileW - 5).dp, y = (row * tileH - 5).dp)
                        .rotate(tile.rotation)
                        .size((tileW - 12).dp, (tileH - 12).dp)
                        .shadow(4.dp, RoundedCornerShape(4.dp))
                        .background(Color.White, RoundedCornerShape(4.dp))
                        .padding(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(tile.bg.copy(alpha = 0.82f), RoundedCornerShape(2.dp))
                            .padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(tile.emoji, fontSize = 28.sp, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = tile.city, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            color = Color(0xFF2A3A45), textAlign = TextAlign.Center, maxLines = 1
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    0.0f to Color(0x00C8923A),
                    0.6f to Color(0x22C8923A),
                    1.0f to Color(0x99A0621A)
                )
            )
        )
        Box(modifier = Modifier.fillMaxSize().background(Color(0x44000000)))
    }
}

@Composable
fun AuthScreen(
    uiState: AuthUiState,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String, String) -> Unit,
    onResetError: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }

    val hoverSource = remember { MutableInteractionSource() }
    val isHovered by hoverSource.collectIsHoveredAsState()
    val cardElevation by animateDpAsState(
        targetValue = if (isHovered) 32.dp else 10.dp,
        animationSpec = tween(durationMillis = 250),
        label = "cardElevation"
    )

    Box(modifier = modifier.fillMaxSize()) {
        TravelCollageBackground()

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth(0.88f)
                    .hoverable(hoverSource)
                    .shadow(
                        elevation = cardElevation, shape = RoundedCornerShape(24.dp),
                        ambientColor = SteelBlue.copy(alpha = 0.5f),
                        spotColor = SteelBlueDark.copy(alpha = 0.6f)
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(listOf(SteelBlue.copy(alpha = 0.6f), Teal.copy(alpha = 0.4f), Mint.copy(alpha = 0.5f))),
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = Cream.copy(alpha = 0.97f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Wandr", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = SteelBlueDark)
                    Spacer(Modifier.height(4.dp))
                    Text("Find your adventure buddies", style = MaterialTheme.typography.bodyMedium, color = SteelBlueDark.copy(alpha = 0.7f))

                    Spacer(Modifier.height(28.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = !isSignUp, onClick = { isSignUp = false; onResetError() },
                            label = { Text("Sign In") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SteelBlue, selectedLabelColor = Color.White),
                            modifier = Modifier.weight(1f))
                        FilterChip(selected = isSignUp, onClick = { isSignUp = true; onResetError() },
                            label = { Text("Sign Up") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SteelBlue, selectedLabelColor = Color.White),
                            modifier = Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(20.dp))

                    val fieldColors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SteelBlue, unfocusedBorderColor = SteelBlue.copy(alpha = 0.4f),
                        focusedLabelColor = SteelBlue, cursorColor = SteelBlue
                    )

                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SteelBlue) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        singleLine = true, shape = RoundedCornerShape(12.dp), colors = fieldColors, modifier = Modifier.fillMaxWidth())

                    Spacer(Modifier.height(12.dp))

                    if (isSignUp) {
                        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SteelBlue) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            singleLine = true, shape = RoundedCornerShape(12.dp), colors = fieldColors, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(12.dp))
                    }

                    OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SteelBlue) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (isSignUp) onSignUp(email, password, username) else onSignIn(email, password)
                        }),
                        singleLine = true, shape = RoundedCornerShape(12.dp), colors = fieldColors, modifier = Modifier.fillMaxWidth())

                    Spacer(Modifier.height(28.dp))

                    when (uiState) {
                        is AuthUiState.Loading -> CircularProgressIndicator(color = SteelBlue)
                        is AuthUiState.Error -> {
                            Text(uiState.message, color = Color(0xFFD64E4E), style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = onResetError, shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SteelBlue),
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) { Text("Try Again", color = Color.White, fontWeight = FontWeight.SemiBold) }
                        }
                        else -> {
                            Button(
                                onClick = { if (isSignUp) onSignUp(email, password, username) else onSignIn(email, password) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SteelBlue, disabledContainerColor = SteelBlue.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                enabled = email.isNotBlank() && password.isNotBlank() && (!isSignUp || username.isNotBlank())
                            ) { Text(if (isSignUp) "Create Account" else "Sign In", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp) }
                        }
                    }
                }
            }
        }
    }
}
