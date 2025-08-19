package tech.kaustubhdeshpande.dropnest.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalLibrary
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.SportsBasketball
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class CategoryIcon(
    val icon: ImageVector,
    val contentDescription: String
)

val availableCategoryIcons = listOf(
    CategoryIcon(Icons.Outlined.Folder, "General"),
    CategoryIcon(Icons.Outlined.Work, "Work"),
    CategoryIcon(Icons.Outlined.School, "Education"),
    CategoryIcon(Icons.Outlined.Home, "Home"),
    CategoryIcon(Icons.Outlined.ShoppingBag, "Shopping"),
    CategoryIcon(Icons.Outlined.Flight, "Travel"),
    CategoryIcon(Icons.Outlined.LocalLibrary, "Books"),
    CategoryIcon(Icons.Outlined.CollectionsBookmark, "Collections"),
    CategoryIcon(Icons.Outlined.Science, "Science"),
    CategoryIcon(Icons.Outlined.Pets, "Pets"),
    CategoryIcon(Icons.Outlined.SportsBasketball, "Sports"),
    CategoryIcon(Icons.Outlined.MusicNote, "Music"),
    CategoryIcon(Icons.Outlined.Brush, "Design"),
    CategoryIcon(Icons.Outlined.Code, "Development"),
    CategoryIcon(Icons.Outlined.Business, "Business")
)

@Composable
fun IconSelector(
    selectedIcon: ImageVector?,
    onIconSelected: (ImageVector) -> Unit,
    tintColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Icon",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            for (categoryIcon in availableCategoryIcons.take(5)) {
                IconOption(
                    icon = categoryIcon.icon,
                    contentDescription = categoryIcon.contentDescription,
                    selected = selectedIcon == categoryIcon.icon,
                    onClick = { onIconSelected(categoryIcon.icon) },
                    tintColor = tintColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            for (categoryIcon in availableCategoryIcons.subList(5, 10)) {
                IconOption(
                    icon = categoryIcon.icon,
                    contentDescription = categoryIcon.contentDescription,
                    selected = selectedIcon == categoryIcon.icon,
                    onClick = { onIconSelected(categoryIcon.icon) },
                    tintColor = tintColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            for (categoryIcon in availableCategoryIcons.subList(10, availableCategoryIcons.size)) {
                IconOption(
                    icon = categoryIcon.icon,
                    contentDescription = categoryIcon.contentDescription,
                    selected = selectedIcon == categoryIcon.icon,
                    onClick = { onIconSelected(categoryIcon.icon) },
                    tintColor = tintColor,
                    modifier = Modifier.weight(1f)
                )
            }

            // Add empty boxes to fill the row if needed
            val emptySlots = 5 - (availableCategoryIcons.size - 10)
            repeat(emptySlots) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun IconOption(
    icon: ImageVector,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit,
    tintColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected)
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        else
            MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .height(48.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tintColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}