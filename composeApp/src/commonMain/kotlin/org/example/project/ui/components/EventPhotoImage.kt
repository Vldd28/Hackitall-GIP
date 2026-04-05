package org.example.project.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

@Composable
expect fun EventPhotoImage(
    storagePath: String,
    contentDescription: String?,
    contentScale: ContentScale,
    modifier: Modifier
)
