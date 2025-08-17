package tech.kaustubhdeshpande.dropnest.ui.screen.categoryfilter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import tech.kaustubhdeshpande.dropnest.domain.model.Drop
import tech.kaustubhdeshpande.dropnest.domain.model.DropType
import tech.kaustubhdeshpande.dropnest.ui.component.EmptyState
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components.DocumentDropItem
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components.DropItem

enum class DropTabType(val label: String) {
    Media("Media"),
    Docs("Docs"),
    Notes("Notes"),
    Links("Links")
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun CategoryFilterScreen(
    categoryName: String,
    drops: List<Drop>,
    initialTab: DropTabType = DropTabType.Media,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onMediaClick: (Drop) -> Unit = {},
) {
    val tabList = listOf(
        DropTabType.Media,
        DropTabType.Docs,
        DropTabType.Notes,
        DropTabType.Links
    )
    val initialPage = tabList.indexOf(initialTab).coerceAtLeast(0)
    var requestedTabIndex by remember { mutableStateOf(initialPage) }
    val pagerState = rememberPagerState(initialPage = initialPage)
    var searchMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(requestedTabIndex) {
        if (pagerState.currentPage != requestedTabIndex) {
            pagerState.animateScrollToPage(requestedTabIndex)
        }
    }
    Scaffold(
        topBar = {
            if (searchMode && tabList[pagerState.currentPage] != DropTabType.Media) {
                SearchTopBar(
                    searchQuery = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onBack = { searchMode = false; searchQuery = "" },
                )
            } else {
                CategoryFilterTopBar(
                    title = categoryName,
                    selectedTabIndex = pagerState.currentPage,
                    tabList = tabList,
                    onTabSelected = { tabIndex ->
                        requestedTabIndex = tabIndex
                        searchMode = false
                        searchQuery = ""
                    },
                    showSearch = tabList[pagerState.currentPage] != DropTabType.Media,
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
            HorizontalPager(
                count = tabList.size,
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val selectedTab = tabList[page]
                val dropsOfType = when (selectedTab) {
                    DropTabType.Media -> drops.filter { it.type == DropType.IMAGE || it.type == DropType.VIDEO }
                    DropTabType.Docs -> drops.filter { it.type == DropType.DOCUMENT }
                    DropTabType.Notes -> drops.filter { it.type == DropType.NOTE }
                    DropTabType.Links -> drops.filter { it.type == DropType.LINK }
                }
                val filteredDrops =
                    if (selectedTab == DropTabType.Media) {
                        dropsOfType
                    } else if (searchMode && searchQuery.isNotBlank()) {
                        dropsOfType.filter { it.matchesSearch(searchQuery) }
                    } else {
                        dropsOfType
                    }

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

    // Sync tab selection with pager
    LaunchedEffect(pagerState.currentPage) {
        // No-op, just ensures recomposition when swiped
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterTopBar(
    title: String,
    selectedTabIndex: Int,
    tabList: List<DropTabType>,
    onTabSelected: (Int) -> Unit,
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
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 0.dp
        ) {
            tabList.forEachIndexed { idx, tab ->
                Tab(
                    selected = selectedTabIndex == idx,
                    onClick = { onTabSelected(idx) },
                    text = { Text(tab.label) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        tonalElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.padding(end = 4.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                placeholder = { Text("Search...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
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