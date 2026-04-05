package org.example.project.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
actual fun EventPhotoImage(
    storagePath: String,
    contentDescription: String?,
    contentScale: ContentScale,
    modifier: Modifier
) {
    AsyncImage(
        model = storagePath,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier
    )
}
