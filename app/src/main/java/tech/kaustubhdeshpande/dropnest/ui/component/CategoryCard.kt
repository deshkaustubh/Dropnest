package tech.kaustubhdeshpande.dropnest.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tech.kaustubhdeshpande.dropnest.domain.model.Category

@Composable
fun CategoryCard(
    category: Category,
    isSelected: Boolean = false,
    dropCount: Int? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Parse the color from the hex string
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(category.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Emoji circle with category color
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(categoryColor.copy(alpha = 0.2f))
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) categoryColor else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category.emoji,
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Category name
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Optional count of drops
        dropCount?.let {
            Text(
                text = "$it drops",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}