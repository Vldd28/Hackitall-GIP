package org.example.project.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage

@Composable
actual fun EventPhotoImage(
    storagePath: String,
    contentDescription: String?,
    contentScale: ContentScale,
    modifier: Modifier
) {
    if (storagePath.startsWith("data:image")) {
        // Decode base64 data URI
        val base64 = storagePath.substringAfter("base64,")
        val bitmap = remember(base64) {
            val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        }
    } else if (storagePath.startsWith("asset://")) {
        // Load from Android assets
        val assetName = storagePath.removePrefix("asset://")
        val context = LocalContext.current
        val bitmap = remember(assetName) {
            try {
                context.assets.open(assetName).use { BitmapFactory.decodeStream(it) }
            } catch (e: Exception) { null }
        }
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        }
    } else {
        AsyncImage(
            model = storagePath,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}
