package org.example.project.ui.components

import androidx.compose.runtime.Composable

/**
 * Platform-specific camera capture.
 * When triggered (showCamera = true), opens the front-facing camera.
 * Returns the captured photo as ByteArray via onPhotoCaptured.
 */
@Composable
expect fun CameraCapture(
    showCamera: Boolean,
    onPhotoCaptured: (ByteArray) -> Unit,
    onDismiss: () -> Unit
)
