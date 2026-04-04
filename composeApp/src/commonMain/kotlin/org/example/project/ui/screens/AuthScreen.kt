package org.example.project.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.verticalScroll

// ── Palette ──────────────────────────────────────────────────────────────────
private val SteelBlue     = Color(0xFF80A1BA)
private val SteelBlueDark = Color(0xFF4A6E85)
private val Teal          = Color(0xFF91C4C3)
private val Mint          = Color(0xFFB4DEBD)
private val Cream         = Color(0xFFFFF7DD)

// ── Side-view plane at end of dotted route ───────────────────────────────────

private fun DrawScope.drawSidePlane(center: Offset, s: Float, angle: Float, color: Color) {
    rotate(angle, pivot = center) {
        val p = Path().apply {
            // Fuselage
            moveTo(center.x, center.y - s * 0.5f)
            lineTo(center.x + s * 0.08f, center.y + s * 0.5f)
            lineTo(center.x - s * 0.08f, center.y + s * 0.5f)
            close()
            // Left wing
            moveTo(center.x - s * 0.06f, center.y + s * 0.02f)
            lineTo(center.x - s * 0.55f, center.y + s * 0.24f)
            lineTo(center.x - s * 0.06f, center.y + s * 0.22f)
            close()
            // Right wing
            moveTo(center.x + s * 0.06f, center.y + s * 0.02f)
            lineTo(center.x + s * 0.55f, center.y + s * 0.24f)
            lineTo(center.x + s * 0.06f, center.y + s * 0.22f)
            close()
            // Left tail
            moveTo(center.x - s * 0.05f, center.y + s * 0.36f)
            lineTo(center.x - s * 0.28f, center.y + s * 0.50f)
            lineTo(center.x - s * 0.05f, center.y + s * 0.46f)
            close()
            // Right tail
            moveTo(center.x + s * 0.05f, center.y + s * 0.36f)
            lineTo(center.x + s * 0.28f, center.y + s * 0.50f)
            lineTo(center.x + s * 0.05f, center.y + s * 0.46f)
            close()
        }
        drawPath(p, color = color, style = Fill)
    }
}

// ── Side-view locomotive ─────────────────────────────────────────────────────

private fun DrawScope.drawLocomotive(cx: Float, cy: Float, locoWidth: Float) {
    val lw = locoWidth
    val bodyH = lw * 0.22f
    val bodyY = cy - bodyH / 2f
    val bodyLeft = cx - lw * 0.45f
    val bodyRight = cx + lw * 0.45f

    // ── Boiler (main body — long rectangle with rounded front) ───────────
    val boilerPath = Path().apply {
        moveTo(bodyLeft + lw * 0.08f, bodyY)
        lineTo(bodyRight - lw * 0.05f, bodyY)
        // Rounded front (right side)
        cubicTo(
            bodyRight + lw * 0.02f, bodyY,
            bodyRight + lw * 0.04f, bodyY + bodyH * 0.5f,
            bodyRight - lw * 0.05f, bodyY + bodyH
        )
        lineTo(bodyLeft + lw * 0.08f, bodyY + bodyH)
        close()
    }
    drawPath(boilerPath, color = Teal, style = Fill)

    // ── Cab (back section — taller box) ──────────────────────────────────
    val cabW = lw * 0.18f
    val cabH = bodyH * 1.55f
    val cabLeft = bodyLeft
    val cabTop = bodyY + bodyH - cabH
    drawRoundRect(
        color = Teal.copy(alpha = 0.9f),
        topLeft = Offset(cabLeft, cabTop),
        size = androidx.compose.ui.geometry.Size(cabW, cabH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(lw * 0.02f)
    )
    // Cab window
    drawRoundRect(
        color = SteelBlueDark.copy(alpha = 0.7f),
        topLeft = Offset(cabLeft + cabW * 0.2f, cabTop + cabH * 0.15f),
        size = androidx.compose.ui.geometry.Size(cabW * 0.6f, cabH * 0.3f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(lw * 0.01f)
    )

    // ── Smokestack (chimney) ─────────────────────────────────────────────
    val stackX = bodyRight - lw * 0.14f
    val stackW = lw * 0.05f
    val stackH = bodyH * 0.5f
    drawRoundRect(
        color = SteelBlue,
        topLeft = Offset(stackX - stackW / 2, bodyY - stackH),
        size = androidx.compose.ui.geometry.Size(stackW, stackH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(lw * 0.01f)
    )
    // Stack top (wider cap)
    drawRoundRect(
        color = SteelBlue,
        topLeft = Offset(stackX - stackW * 0.8f, bodyY - stackH - lw * 0.015f),
        size = androidx.compose.ui.geometry.Size(stackW * 1.6f, lw * 0.025f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(lw * 0.005f)
    )

    // ── Dome on boiler ───────────────────────────────────────────────────
    val domeX = cx
    drawOval(
        color = Teal.copy(alpha = 0.8f),
        topLeft = Offset(domeX - lw * 0.04f, bodyY - bodyH * 0.22f),
        size = androidx.compose.ui.geometry.Size(lw * 0.08f, bodyH * 0.28f)
    )

    // ── Cowcatcher (front, right side) ───────────────────────────────────
    val cowPath = Path().apply {
        moveTo(bodyRight - lw * 0.05f, bodyY + bodyH)
        lineTo(bodyRight + lw * 0.06f, bodyY + bodyH + bodyH * 0.2f)
        lineTo(bodyRight - lw * 0.05f, bodyY + bodyH + bodyH * 0.2f)
        close()
    }
    drawPath(cowPath, color = SteelBlue, style = Fill)

    // ── Wheels ───────────────────────────────────────────────────────────
    val wheelY = bodyY + bodyH + bodyH * 0.05f
    val bigR = bodyH * 0.28f
    val smallR = bodyH * 0.18f

    // 3 big drive wheels
    for (i in 0..2) {
        val wx = bodyLeft + cabW + lw * 0.06f + i * (lw * 0.16f)
        drawCircle(color = SteelBlue, radius = bigR, center = Offset(wx, wheelY))
        drawCircle(color = SteelBlueDark, radius = bigR * 0.45f, center = Offset(wx, wheelY))
    }
    // 1 small front wheel
    drawCircle(color = SteelBlue, radius = smallR, center = Offset(bodyRight - lw * 0.04f, wheelY + bigR - smallR))
    drawCircle(color = SteelBlueDark, radius = smallR * 0.45f, center = Offset(bodyRight - lw * 0.04f, wheelY + bigR - smallR))

    // ── Connecting rod (line between big wheels) ─────────────────────────
    val rodY = wheelY
    drawLine(
        color = SteelBlueDark.copy(alpha = 0.6f),
        start = Offset(bodyLeft + cabW + lw * 0.06f, rodY),
        end = Offset(bodyLeft + cabW + lw * 0.06f + 2 * (lw * 0.16f), rodY),
        strokeWidth = lw * 0.012f
    )

    // ── Undercarriage strip ──────────────────────────────────────────────
    drawRect(
        color = SteelBlue.copy(alpha = 0.5f),
        topLeft = Offset(bodyLeft, bodyY + bodyH),
        size = androidx.compose.ui.geometry.Size(bodyRight - bodyLeft + lw * 0.06f, bodyH * 0.08f)
    )
}

// ── Background: dotted routes + big planes ───────────────────────────────────

@Composable
private fun TravelRoutesBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val dash = PathEffect.dashPathEffect(floatArrayOf(24f, 16f), 0f)
        val routeStroke = Stroke(width = 7f, pathEffect = dash, cap = StrokeCap.Round)
        val planeSize = w * 0.22f

        // Route 1: top-left plane → curves down through center → bottom-right
        val p1 = Offset(w * 0.12f, h * 0.08f)
        val r1 = Path().apply {
            moveTo(p1.x, p1.y)
            cubicTo(w * 0.22f, h * 0.20f, w * 0.35f, h * 0.32f, w * 0.48f, h * 0.42f)
            cubicTo(w * 0.62f, h * 0.52f, w * 0.78f, h * 0.58f, w * 0.92f, h * 0.70f)
        }
        drawPath(r1, color = Teal.copy(alpha = 0.6f), style = routeStroke)
        drawSidePlane(center = p1, s = planeSize, angle = -38f, color = Teal.copy(alpha = 0.75f))

        // Route 2: top-right plane → winding to bottom-left
        val p2 = Offset(w * 0.90f, h * 0.05f)
        val r2 = Path().apply {
            moveTo(p2.x, p2.y)
            cubicTo(w * 0.82f, h * 0.18f, w * 0.60f, h * 0.28f, w * 0.45f, h * 0.40f)
            cubicTo(w * 0.30f, h * 0.52f, w * 0.15f, h * 0.68f, w * 0.05f, h * 0.82f)
        }
        drawPath(r2, color = Teal.copy(alpha = 0.48f), style = routeStroke)
        drawSidePlane(center = p2, s = planeSize * 0.9f, angle = 195f, color = Teal.copy(alpha = 0.65f))

        // Route 3: bottom-right plane → curves left
        val p3 = Offset(w * 0.92f, h * 0.92f)
        val r3 = Path().apply {
            moveTo(w * 0.05f, h * 0.55f)
            cubicTo(w * 0.20f, h * 0.50f, w * 0.45f, h * 0.62f, w * 0.65f, h * 0.70f)
            cubicTo(w * 0.78f, h * 0.76f, w * 0.88f, h * 0.84f, p3.x, p3.y)
        }
        drawPath(r3, color = Teal.copy(alpha = 0.42f), style = routeStroke)
        drawSidePlane(center = p3, s = planeSize * 0.85f, angle = 130f, color = Teal.copy(alpha = 0.55f))

        // Route 4: bottom-left plane → curves up right
        val p4 = Offset(w * 0.10f, h * 0.94f)
        val r4 = Path().apply {
            moveTo(p4.x, p4.y)
            cubicTo(w * 0.25f, h * 0.82f, w * 0.42f, h * 0.72f, w * 0.55f, h * 0.58f)
        }
        drawPath(r4, color = Teal.copy(alpha = 0.38f), style = routeStroke)
        drawSidePlane(center = p4, s = planeSize * 0.8f, angle = -155f, color = Teal.copy(alpha = 0.5f))
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
        modifier = modifier.fillMaxSize().background(SteelBlue)
    ) {
        TravelRoutesBackground()

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

            // ── Locomotive with app name ─────────────────────────────────
            Box(
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawLocomotive(
                        cx = size.width * 0.5f,
                        cy = size.height * 0.55f,
                        locoWidth = size.width * 0.92f
                    )
                }
                // App name on top of the locomotive body
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Wandr",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Cream
                    )
                    Text(
                        "Find your adventure buddies",
                        fontSize = 12.sp,
                        color = Cream.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Login card ───────────────────────────────────────────────
            Card(
                modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Mint)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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

                    OutlinedTextField(
                        value = email, onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        singleLine = true, shape = RoundedCornerShape(14.dp),
                        colors = fieldColors, modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))

                    if (isSignUp) {
                        OutlinedTextField(
                            value = username, onValueChange = { username = it },
                            label = { Text("Username") },
                            leadingIcon = { Icon(Icons.Default.Person, null) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            singleLine = true, shape = RoundedCornerShape(14.dp),
                            colors = fieldColors, modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = password, onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (isSignUp) onSignUp(email, password, username) else onSignIn(email, password)
                        }),
                        singleLine = true, shape = RoundedCornerShape(14.dp),
                        colors = fieldColors, modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(24.dp))

                    when (uiState) {
                        is AuthUiState.Loading -> CircularProgressIndicator(color = SteelBlueDark)
                        is AuthUiState.Error -> {
                            Text(uiState.message, color = Color(0xFFBB3333), style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = onResetError, shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SteelBlueDark, contentColor = Cream),
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) { Text("Try Again", fontWeight = FontWeight.SemiBold) }
                        }
                        else -> {
                            Button(
                                onClick = { if (isSignUp) onSignUp(email, password, username) else onSignIn(email, password) },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SteelBlueDark, contentColor = Cream,
                                    disabledContainerColor = SteelBlueDark.copy(alpha = 0.35f),
                                    disabledContentColor = Cream.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                enabled = email.isNotBlank() && password.isNotBlank() && (!isSignUp || username.isNotBlank())
                            ) {
                                Text(
                                    if (isSignUp) "Create Account" else "Sign In",
                                    fontWeight = FontWeight.Bold, fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(0.6f))
        }
    }
}
