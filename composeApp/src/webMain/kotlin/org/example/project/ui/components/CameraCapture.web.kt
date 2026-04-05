package org.example.project.ui.components

import androidx.compose.runtime.Composable

@Composable
actual fun CameraCapture(
    showCamera: Boolean,
    onPhotoCaptured: (ByteArray) -> Unit,
    onDismiss: () -> Unit
) {
    // Web camera capture not implemented yet — dismiss immediately
    if (showCamera) {
        onDismiss()
    }
}
