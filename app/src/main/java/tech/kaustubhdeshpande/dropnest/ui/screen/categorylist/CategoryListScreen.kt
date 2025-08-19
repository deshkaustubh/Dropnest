package tech.kaustubhdeshpande.dropnest.ui.screen.categorylist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.kaustubhdeshpande.dropnest.domain.model.Category
import tech.kaustubhdeshpande.dropnest.ui.component.availableCategoryIcons
import tech.kaustubhdeshpande.dropnest.ui.theme.DropnestTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    onCategoryClick: (String) -> Unit,
    onAddCategoryClick: () -> Unit,
    viewModel: CategoryListViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsState()

    // 1. Only custom categories (not default), 2. Sorted by last updated (timestamp desc)
    val customCategories = categories
        .filter { !it.isDefault }
        .sortedByDescending { it.timestamp }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Your Categories",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (customCategories.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No categories yet.\nCreate your first category!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(customCategories) { category ->
                        CategoryListItem(
                            category = category,
                            onClick = { onCategoryClick(category.id) }
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = onAddCategoryClick,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        bottom = paddingValues.calculateBottomPadding() + 60.dp,
                        end = 16.dp
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Category",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

// Helper to get ImageVector from icon name in the emoji field (actually stores icon name)
fun getCategoryIconFromName(iconName: String): ImageVector {
    return availableCategoryIcons.find { it.contentDescription == iconName }?.icon
        ?: Icons.Outlined.Folder // fallback
}

@Composable
fun CategoryListItem(
    category: Category,
    onClick: () -> Unit
) {
    val parsedColor = try {
        Color(android.graphics.Color.parseColor(category.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(parsedColor, shape = MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getCategoryIconFromName(category.emoji),
                contentDescription = null,
                tint = Color.White, // Always white for maximum contrast
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Last updated:\n${formatCategoryTimestamp(category.timestamp)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = formatCategoryShortTimestamp(category.timestamp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
}

// Format as "Just now", "2 hours ago", "Yesterday", or a date
fun formatCategoryTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val oneMinute = 60 * 1000L
    val oneHour = 60 * oneMinute
    val oneDay = 24 * oneHour

    return when {
        diff < oneMinute -> "Just now"
        diff < oneHour -> "${diff / oneMinute} minutes ago"
        diff < oneDay -> "${diff / oneHour} hours ago"
        diff < 2 * oneDay -> "Yesterday"
        diff < 7 * oneDay -> "${diff / oneDay} days ago"
        else -> {
            val sdf = SimpleDateFormat("MM/dd/yy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

// For right aligned short info: time if today, "Yesterday", or date
fun formatCategoryShortTimestamp(timestamp: Long): String {
    val now = Calendar.getInstance()
    val then = Calendar.getInstance().apply { timeInMillis = timestamp }
    return when {
        now.get(Calendar.YEAR) == then.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR) -> {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
        }
        now.get(Calendar.YEAR) == then.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) - then.get(Calendar.DAY_OF_YEAR) == 1 -> {
            "Yesterday"
        }
        else -> SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(Date(timestamp))
    }
}