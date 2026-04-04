package org.example.project.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.viewmodel.AuthUiState

// ── Palette ───────────────────────────────────────────────────────────────────
private val SteelBlue     = Color(0xFF80A1BA)
private val SteelBlueDark = Color(0xFF4A6E85)
private val Teal          = Color(0xFF91C4C3)
private val Mint          = Color(0xFFB4DEBD)
private val Cream         = Color(0xFFFFF7DD)

// ── Background: dotted winding roads with planes ─────────────────────────────

private fun DrawScope.drawPlane(center: Offset, sizePx: Float, angle: Float, color: Color) {
    rotate(angle, pivot = center) {
        val path = Path().apply {
            // Fuselage
            moveTo(center.x, center.y - sizePx * 0.5f)
            lineTo(center.x + sizePx * 0.06f, center.y + sizePx * 0.5f)
            lineTo(center.x - sizePx * 0.06f, center.y + sizePx * 0.5f)
            close()
            // Left wing
            moveTo(center.x - sizePx * 0.04f, center.y + sizePx * 0.05f)
            lineTo(center.x - sizePx * 0.45f, center.y + sizePx * 0.2f)
            lineTo(center.x - sizePx * 0.04f, center.y + sizePx * 0.18f)
            close()
            // Right wing
            moveTo(center.x + sizePx * 0.04f, center.y + sizePx * 0.05f)
            lineTo(center.x + sizePx * 0.45f, center.y + sizePx * 0.2f)
            lineTo(center.x + sizePx * 0.04f, center.y + sizePx * 0.18f)
            close()
            // Tail left
            moveTo(center.x - sizePx * 0.03f, center.y + sizePx * 0.38f)
            lineTo(center.x - sizePx * 0.18f, center.y + sizePx * 0.48f)
            lineTo(center.x - sizePx * 0.03f, center.y + sizePx * 0.44f)
            close()
            // Tail right
            moveTo(center.x + sizePx * 0.03f, center.y + sizePx * 0.38f)
            lineTo(center.x + sizePx * 0.18f, center.y + sizePx * 0.48f)
            lineTo(center.x + sizePx * 0.03f, center.y + sizePx * 0.44f)
            close()
        }
        drawPath(path, color = color, style = Fill)
    }
}

@Composable
private fun TravelRoutesBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val dash = PathEffect.dashPathEffect(floatArrayOf(14f, 12f), 0f)
        val routeStroke = Stroke(width = 3f, pathEffect = dash, cap = StrokeCap.Round)

        // ── Route 1: top-left → curves down through center → bottom-right ───
        val r1 = Path().apply {
            moveTo(0f, h * 0.12f)
            cubicTo(w * 0.15f, h * 0.08f, w * 0.25f, h * 0.22f, w * 0.38f, h * 0.28f)
            cubicTo(w * 0.50f, h * 0.34f, w * 0.55f, h * 0.52f, w * 0.62f, h * 0.60f)
            cubicTo(w * 0.70f, h * 0.70f, w * 0.85f, h * 0.72f, w * 1.05f, h * 0.65f)
        }
        drawPath(r1, color = Teal.copy(alpha = 0.55f), style = routeStroke)

        // ── Route 2: top-right → winding down → exits bottom-left ───────────
        val r2 = Path().apply {
            moveTo(w * 0.92f, -h * 0.02f)
            cubicTo(w * 0.88f, h * 0.12f, w * 0.72f, h * 0.18f, w * 0.60f, h * 0.32f)
            cubicTo(w * 0.48f, h * 0.46f, w * 0.35f, h * 0.55f, w * 0.25f, h * 0.68f)
            cubicTo(w * 0.15f, h * 0.80f, w * 0.08f, h * 0.88f, -w * 0.05f, h * 0.92f)
        }
        drawPath(r2, color = Teal.copy(alpha = 0.45f), style = routeStroke)

        // ── Route 3: left side → sweeps right → exits bottom ────────────────
        val r3 = Path().apply {
            moveTo(-w * 0.03f, h * 0.55f)
            cubicTo(w * 0.12f, h * 0.50f, w * 0.30f, h * 0.42f, w * 0.45f, h * 0.48f)
            cubicTo(w * 0.60f, h * 0.54f, w * 0.70f, h * 0.70f, w * 0.75f, h * 0.88f)
            cubicTo(w * 0.78f, h * 0.98f, w * 0.80f, h * 1.06f, w * 0.82f, h * 1.12f)
        }
        drawPath(r3, color = Teal.copy(alpha = 0.35f), style = routeStroke)

        // ── Route 4: top → gentle S-curve through center ────────────────────
        val r4 = Path().apply {
            moveTo(w * 0.35f, -h * 0.04f)
            cubicTo(w * 0.40f, h * 0.10f, w * 0.20f, h * 0.25f, w * 0.30f, h * 0.40f)
            cubicTo(w * 0.40f, h * 0.55f, w * 0.65f, h * 0.58f, w * 0.55f, h * 0.78f)
            cubicTo(w * 0.48f, h * 0.90f, w * 0.42f, h * 1.02f, w * 0.40f, h * 1.10f)
        }
        drawPath(r4, color = Teal.copy(alpha = 0.3f), style = routeStroke)

        // ── Planes at endpoints ─────────────────────────────────────────────

        // Plane 1: top-left corner area (visible near the card's top-left)
        drawPlane(
            center = Offset(w * 0.08f, h * 0.14f),
            sizePx = w * 0.09f,
            angle = -35f,
            color = Teal.copy(alpha = 0.7f)
        )

        // Plane 2: top-right, partially visible
        drawPlane(
            center = Offset(w * 0.90f, h * 0.02f),
            sizePx = w * 0.08f,
            angle = 200f,
            color = Teal.copy(alpha = 0.55f)
        )

        // Plane 3: bottom-right
        drawPlane(
            center = Offset(w * 1.02f, h * 0.64f),
            sizePx = w * 0.10f,
            angle = 120f,
            color = Teal.copy(alpha = 0.5f)
        )

        // Plane 4: bottom-left
        drawPlane(
            center = Offset(-w * 0.02f, h * 0.90f),
            sizePx = w * 0.08f,
            angle = -150f,
            color = Teal.copy(alpha = 0.45f)
        )

        // Plane 5: center-bottom (peeks from below card)
        drawPlane(
            center = Offset(w * 0.75f, h * 0.86f),
            sizePx = w * 0.07f,
            angle = 160f,
            color = Teal.copy(alpha = 0.4f)
        )
    }
}

// ── Auth Screen ──────────────────────────────────────────────────────────────

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SteelBlue)
    ) {
        // Dotted route lines + planes
        TravelRoutesBackground()

        // Scrollable content so keyboard doesn't hide fields
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.weight(1f))

            // ── Title above the card ─────────────────────────────────────
            Text(
                "Wandr",
                fontSize = 46.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Cream
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Find your adventure buddies",
                fontSize = 14.sp,
                color = Cream.copy(alpha = 0.75f)
            )

            Spacer(Modifier.height(28.dp))

            // ── Login card ───────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Mint)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !isSignUp,
                            onClick = { isSignUp = false; onResetError() },
                            label = { Text("Sign In", fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SteelBlueDark,
                                selectedLabelColor = Cream,
                                containerColor = Cream.copy(alpha = 0.5f),
                                labelColor = SteelBlueDark
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = isSignUp,
                            onClick = { isSignUp = true; onResetError() },
                            label = { Text("Sign Up", fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SteelBlueDark,
                                selectedLabelColor = Cream,
                                containerColor = Cream.copy(alpha = 0.5f),
                                labelColor = SteelBlueDark
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    val fieldColors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SteelBlueDark,
                        unfocusedBorderColor = SteelBlueDark.copy(alpha = 0.35f),
                        focusedLabelColor = SteelBlueDark,
                        unfocusedLabelColor = SteelBlueDark.copy(alpha = 0.6f),
                        cursorColor = SteelBlueDark,
                        focusedTextColor = SteelBlueDark,
                        unfocusedTextColor = SteelBlueDark,
                        focusedLeadingIconColor = SteelBlueDark,
                        unfocusedLeadingIconColor = SteelBlueDark.copy(alpha = 0.5f),
                        focusedContainerColor = Cream.copy(alpha = 0.35f),
                        unfocusedContainerColor = Cream.copy(alpha = 0.2f)
                    )

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    // Username (sign up only)
                    if (isSignUp) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (isSignUp) onSignUp(email, password, username)
                                else onSignIn(email, password)
                            }
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(24.dp))

                    // Button / loading / error
                    when (uiState) {
                        is AuthUiState.Loading -> {
                            CircularProgressIndicator(color = SteelBlueDark)
                        }
                        is AuthUiState.Error -> {
                            Text(
                                uiState.message,
                                color = Color(0xFFBB3333),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = onResetError,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SteelBlueDark,
                                    contentColor = Cream
                                ),
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                Text("Try Again", fontWeight = FontWeight.SemiBold)
                            }
                        }
                        else -> {
                            Button(
                                onClick = {
                                    if (isSignUp) onSignUp(email, password, username)
                                    else onSignIn(email, password)
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SteelBlueDark,
                                    contentColor = Cream,
                                    disabledContainerColor = SteelBlueDark.copy(alpha = 0.35f),
                                    disabledContentColor = Cream.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                enabled = email.isNotBlank() && password.isNotBlank() &&
                                        (!isSignUp || username.isNotBlank())
                            ) {
                                Text(
                                    if (isSignUp) "Create Account" else "Sign In",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))
        }
    }
}
