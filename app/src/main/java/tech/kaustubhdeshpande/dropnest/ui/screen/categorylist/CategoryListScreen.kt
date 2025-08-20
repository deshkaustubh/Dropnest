package tech.kaustubhdeshpande.dropnest.ui.screen.categorylist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.kaustubhdeshpande.dropnest.domain.model.Category
import tech.kaustubhdeshpande.dropnest.ui.component.availableCategoryIcons
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    onCategoryClick: (String) -> Unit,
    onAddCategoryClick: () -> Unit,
    onEditCategoryClick: (String) -> Unit,
    viewModel: CategoryListViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsState()
    var searchMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Only custom categories (not default), sorted by last updated (timestamp desc)
    val customCategories = categories
        .filter { !it.isDefault }
        .sortedByDescending { it.timestamp }

    // Filter categories by search query (case-insensitive)
    val filteredCategories = if (searchQuery.isBlank()) {
        customCategories
    } else {
        customCategories.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    // Selection mode state
    var selectedCategoryIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Handle back button in search or selection mode
    BackHandler(enabled = searchMode || selectedCategoryIds.isNotEmpty()) {
        when {
            searchMode -> {
                searchMode = false
                searchQuery = ""
            }
            selectedCategoryIds.isNotEmpty() -> selectedCategoryIds = emptySet()
        }
    }

    Scaffold(
        topBar = {
            Column {
                when {
                    selectedCategoryIds.isNotEmpty() -> {
                        SelectionTopBar(
                            selectedCount = selectedCategoryIds.size,
                            onClose = { selectedCategoryIds = emptySet() },
                            onDelete = { showDeleteDialog = true },
                            onEdit = {
                                if (selectedCategoryIds.size == 1)
                                    onEditCategoryClick(selectedCategoryIds.first())
                            }
                        )
                    }
                    searchMode -> {
                        CategoryListSearchTopBar(
                            searchQuery = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onBack = {
                                searchMode = false
                                searchQuery = ""
                            }
                        )
                    }
                    else -> {
                        NormalTopBar(
                            onSearchClick = { searchMode = true }
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (filteredCategories.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (searchQuery.isNotBlank())
                            "No categories found for \"$searchQuery\""
                        else
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
                    items(filteredCategories) { category ->
                        val selected = selectedCategoryIds.contains(category.id)
                        CategoryListItem(
                            category = category,
                            onClick = {
                                if (selectedCategoryIds.isNotEmpty()) {
                                    selectedCategoryIds =
                                        if (selected) selectedCategoryIds - category.id
                                        else selectedCategoryIds + category.id
                                } else {
                                    onCategoryClick(category.id)
                                }
                            },
                            onLongClick = {
                                selectedCategoryIds = setOf(category.id)
                            },
                            selected = selected
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

        // Delete dialog
        if (showDeleteDialog && selectedCategoryIds.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        selectedCategoryIds.forEach { categoryId ->
                            viewModel.deleteCategoryAndDrops(categoryId)
                        }
                        selectedCategoryIds = emptySet()
                        showDeleteDialog = false
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Delete Category?") },
                text = { Text("Deleting this category will also delete all drops in it. This action cannot be undone. Are you sure?") }
            )
        }
    }
}

// --- TopAppBars ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NormalTopBar(
    onSearchClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                "Your Categories",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListSearchTopBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit
) {
    // This implementation matches the beautiful search bar from CategoryFilterScreen
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        tonalElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.padding(end = 4.dp)) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                placeholder = { Text("Search categories...", style = MaterialTheme.typography.labelMedium) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopBar(
    selectedCount: Int,
    onClose: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    TopAppBar(
        title = { Text("$selectedCount selected") },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close selection")
            }
        },
        actions = {
            IconButton(
                onClick = onEdit,
                enabled = selectedCount == 1 // Only enabled when one is selected
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
            IconButton(onClick = { /* Overflow menu, empty for now */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More")
            }
        }
    )
}

// Helper to get ImageVector from icon name in the emoji field (actually stores icon name)
fun getCategoryIconFromName(iconName: String): ImageVector {
    return availableCategoryIcons.find { it.contentDescription == iconName }?.icon
        ?: Icons.Outlined.Folder // fallback
}

@Composable
fun CategoryListItem(
    category: Category,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    selected: Boolean = false
) {
    val parsedColor = try {
        Color(android.graphics.Color.parseColor(category.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }
    val backgroundColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    else MaterialTheme.colorScheme.surface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .background(
                color = backgroundColor,
                shape = MaterialTheme.shapes.medium
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
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