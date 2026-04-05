package org.example.project.ui.components

import android.Manifest
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import java.io.ByteArrayOutputStream

@Composable
actual fun CameraCapture(
    showCamera: Boolean,
    onPhotoCaptured: (ByteArray) -> Unit,
    onDismiss: () -> Unit
) {
    var pendingLaunch by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
            onPhotoCaptured(stream.toByteArray())
        } else {
            onDismiss()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(null)
        } else {
            onDismiss()
        }
    }

    LaunchedEffect(showCamera) {
        if (showCamera) {
            pendingLaunch = true
        }
    }

    // Launch permission request outside of LaunchedEffect to avoid lifecycle issues
    if (pendingLaunch) {
        pendingLaunch = false
        SideEffect {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}
