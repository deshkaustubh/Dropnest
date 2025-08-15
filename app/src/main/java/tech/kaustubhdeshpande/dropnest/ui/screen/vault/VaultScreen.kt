package tech.kaustubhdeshpande.dropnest.ui.screen.vault

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.kaustubhdeshpande.dropnest.domain.model.Category
import tech.kaustubhdeshpande.dropnest.domain.model.Drop
import tech.kaustubhdeshpande.dropnest.domain.model.DropType
import tech.kaustubhdeshpande.dropnest.presentation.event.VaultEvent
import tech.kaustubhdeshpande.dropnest.presentation.viewmodel.vault.VaultUiState
import tech.kaustubhdeshpande.dropnest.presentation.viewmodel.vault.VaultViewModel
import tech.kaustubhdeshpande.dropnest.ui.component.CategoryCard
import tech.kaustubhdeshpande.dropnest.ui.component.DropCard
import tech.kaustubhdeshpande.dropnest.ui.component.EmptyState
import tech.kaustubhdeshpande.dropnest.ui.component.LoadingState
import tech.kaustubhdeshpande.dropnest.ui.theme.DropnestTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    onCreateDrop: () -> Unit,
    onDropClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VaultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Your Vault",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    // Search icon
                    IconButton(onClick = {
                        // Toggle search mode
                    }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }

                    // Settings icon
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateDrop,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Drop"
                )
            }
        }
    ) { paddingValues ->
        VaultContent(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            onDropClick = onDropClick,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

@Composable
fun VaultContent(
    uiState: VaultUiState,
    onEvent: (VaultEvent) -> Unit,
    onDropClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        if (uiState.isLoading) {
            LoadingState()
        } else if (!uiState.hasCategories) {
            EmptyState(
                title = "No Categories Yet",
                message = "You need to create categories first before adding your drops.",
                actionLabel = "Create Category",
                onAction = { /* Navigate to create category */ }
            )
        } else if (!uiState.hasDrops) {
            EmptyState(
                title = "Your Vault is Empty",
                message = "Start adding your links, notes, and media to your vault.",
                actionLabel = "Add First Drop",
                onAction = { /* Navigate to create drop */ }
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Categories row
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.categories) { category ->
                        CategoryCard(
                            category = category,
                            isSelected = category.id == uiState.selectedCategoryId,
                            onClick = {
                                onEvent(VaultEvent.SelectCategory(category.id))
                            }
                        )
                    }
                }

                Divider()

                // Drops list
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.filteredDrops) { drop ->
                        DropCard(
                            drop = drop,
                            onClick = { onDropClick(drop.id) }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun VaultScreenPreview() {
    DropnestTheme(dynamicColor = false) {
        val mockCategories = listOf(
            Category(
                id = "1",
                name = "Work",
                emoji = "💼",
                colorHex = "#26E07F",
                isDefault = false
            ),
            Category(
                id = "2",
                name = "Personal",
                emoji = "🏠",
                colorHex = "#D6ABFF",
                isDefault = false
            )
        )

        val mockDrops = listOf(
            Drop(
                id = "1",
                type = DropType.NOTE,
                text = "Remember to finish the project proposal by Friday",
                title = "Project Deadline",
                categoryId = "1",
                isPinned = true
            ),
            Drop(
                id = "2",
                type = DropType.LINK,
                text = "https://example.com",
                title = "Example Website",
                categoryId = "2"
            )
        )

        VaultContent(
            uiState = VaultUiState(
                isLoading = false,
                categories = mockCategories,
                drops = mockDrops,
                selectedCategoryId = "1"
            ),
            onEvent = {},
            onDropClick = {}
        )
    }
}