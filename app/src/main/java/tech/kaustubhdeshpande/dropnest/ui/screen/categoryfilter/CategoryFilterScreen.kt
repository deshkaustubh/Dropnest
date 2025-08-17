package tech.kaustubhdeshpande.dropnest.ui.screen.categoryfilter

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import tech.kaustubhdeshpande.dropnest.domain.model.Drop
import tech.kaustubhdeshpande.dropnest.domain.model.DropType
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components.DocumentDropItem
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components.DropItem
import tech.kaustubhdeshpande.dropnest.ui.component.EmptyState

enum class DropTabType(val label: String) {
    Media("Media"),
    Docs("Docs"),
    Notes("Notes"),
    Links("Links")
}

@Composable
fun CategoryFilterScreen(
    categoryName: String,
    drops: List<Drop>,
    initialTab: DropTabType = DropTabType.Media,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onMediaClick: (Drop) -> Unit = {},
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    var searchMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredDrops = remember(drops, selectedTab, searchQuery, searchMode) {
        val dropsOfType = when (selectedTab) {
            DropTabType.Media -> drops.filter { it.type == DropType.IMAGE || it.type == DropType.VIDEO }
            DropTabType.Docs -> drops.filter { it.type == DropType.DOCUMENT }
            DropTabType.Notes -> drops.filter { it.type == DropType.NOTE }
            DropTabType.Links -> drops.filter { it.type == DropType.LINK }
        }
        if (selectedTab == DropTabType.Media) {
            dropsOfType
        } else if (searchMode && searchQuery.isNotBlank()) {
            dropsOfType.filter { it.matchesSearch(searchQuery) }
        } else {
            dropsOfType
        }
    }

    Scaffold(
        topBar = {
            if (searchMode && selectedTab != DropTabType.Media) {
                SearchTopBar(
                    searchQuery = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onBack = { searchMode = false; searchQuery = "" },
                )
            } else {
                CategoryFilterTopBar(
                    title = categoryName,
                    selectedTab = selectedTab,
                    onTabSelected = {
                        selectedTab = it
                        // Exit search mode and clear query when switching tabs
                        searchMode = false
                        searchQuery = ""
                    },
                    showSearch = selectedTab != DropTabType.Media,
                    onSearchClick = { searchMode = true },
                    onBackClick = onBackClick,
                )
            }
        },
        modifier = modifier
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (filteredDrops.isEmpty()) {
                EmptyState(
                    title = "No items",
                    message = "No items found for this filter.",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                DropListByTab(
                    drops = filteredDrops,
                    tab = selectedTab,
                    highlightText = if (searchMode && selectedTab != DropTabType.Media) searchQuery else null,
                    onMediaClick = onMediaClick
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterTopBar(
    title: String,
    selectedTab: DropTabType,
    onTabSelected: (DropTabType) -> Unit,
    showSearch: Boolean,
    onSearchClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    Column {
        TopAppBar(
            title = { Text(title) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                if (showSearch) {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            }
        )
        DropTabs(selectedTab = selectedTab, onTabSelected = onTabSelected)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
) {
    TopAppBar(
        title = {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                placeholder = { Text("Search") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {}
    )
}

@Composable
fun DropTabs(
    selectedTab: DropTabType,
    onTabSelected: (DropTabType) -> Unit
) {
    val tabs = listOf(
        DropTabType.Media,
        DropTabType.Docs,
        DropTabType.Notes,
        DropTabType.Links
    )
    ScrollableTabRow(
        selectedTabIndex = tabs.indexOf(selectedTab),
        edgePadding = 0.dp
    ) {
        tabs.forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = { Text(tab.label) }
            )
        }
    }
}

@Composable
fun DropListByTab(
    drops: List<Drop>,
    tab: DropTabType,
    highlightText: String?,
    onMediaClick: (Drop) -> Unit
) {
    Column {
        drops.forEach { drop ->
            when (tab) {
                DropTabType.Media -> DropItem(
                    drop = drop,
                    onMediaClick = onMediaClick
                )
                DropTabType.Docs -> DocumentDropItem(
                    drop = drop,
                    highlightText = highlightText
                )
                DropTabType.Notes, DropTabType.Links -> DropItem(
                    drop = drop,
                    highlightText = highlightText,
                    onMediaClick = onMediaClick
                )
            }
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        }
    }
}

// --- Helper extension functions ---

private fun Drop.matchesSearch(query: String): Boolean =
    (title?.contains(query, ignoreCase = true) == true) ||
            (text?.contains(query, ignoreCase = true) == true)