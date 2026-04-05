package org.example.project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.project.ui.theme.getInterestColor

@Composable
fun InterestChip(
    interestName: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onToggle: (() -> Unit)? = null
) {
    val backgroundColor = if (selected) {
        getInterestColor(interestName)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = if (selected) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .then(
                if (onToggle != null) {
                    Modifier.clickable { onToggle() }
                } else Modifier
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = interestName,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}
