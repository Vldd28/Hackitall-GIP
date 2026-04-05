package org.example.project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.data.model.Event

@Composable
fun SelfiePromptOverlay(
    event: Event,
    isUploading: Boolean,
    onTakeSelfie: () -> Unit,
    onDismiss: () -> Unit
) {
    val cs = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(enabled = !isUploading) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .padding(24.dp)
                .clickable(enabled = false) { }, // block clicks through card
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(16.dp),
            colors = CardDefaults.cardColors(containerColor = cs.surface)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Camera emoji
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(cs.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📸", fontSize = 36.sp)
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    "Selfie Time!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = cs.onSurface
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "You're at \"${event.title}\" — capture the moment!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    event.locationName,
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.primary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(24.dp))

                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = cs.primary,
                        strokeWidth = 3.dp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Uploading...",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                } else {
                    Button(
                        onClick = onTakeSelfie,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = cs.primary,
                            contentColor = cs.onPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text(
                            "Take Selfie",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    TextButton(onClick = onDismiss) {
                        Text(
                            "Maybe later",
                            color = cs.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
