package tech.kaustubhdeshpande.dropnest.ui.screen.categoryfilter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import tech.kaustubhdeshpande.dropnest.domain.model.Drop
import tech.kaustubhdeshpande.dropnest.domain.model.DropType
import tech.kaustubhdeshpande.dropnest.ui.component.EmptyState
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components.FullScreenImageDialog
import java.io.File

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
    onDeleteDrops: (List<Drop>) -> Unit
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
    var selectedDropIds by remember { mutableStateOf(setOf<String>()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // State for image preview dialog
    var selectedImageUri by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(requestedTabIndex) {
        if (pagerState.currentPage != requestedTabIndex) {
            pagerState.animateScrollToPage(requestedTabIndex)
        }
    }

    BackHandler(enabled = selectedDropIds.isNotEmpty() || searchMode || selectedImageUri != null) {
        when {
            selectedImageUri != null -> selectedImageUri = null
            selectedDropIds.isNotEmpty() -> selectedDropIds = emptySet()
            searchMode -> {
                searchMode = false
                searchQuery = ""
            }
        }
    }

    val selectedDrops = remember(selectedDropIds, drops) { drops.filter { it.id in selectedDropIds } }

    Scaffold(
        topBar = {
            Column {
                when {
                    selectedDropIds.isNotEmpty() -> {
                        MultiSelectActionBar(
                            selectedCount = selectedDropIds.size,
                            onClose = { selectedDropIds = emptySet() },
                            onShare = {
                                shareMultipleDrops(context, selectedDrops)
                                selectedDropIds = emptySet()
                            },
                            onDelete = { showDeleteDialog = true }
                        )
                        ScrollableTabRow(
                            selectedTabIndex = pagerState.currentPage,
                            edgePadding = 0.dp
                        ) {
                            tabList.forEachIndexed { idx, tab ->
                                Tab(
                                    selected = pagerState.currentPage == idx,
                                    onClick = {
                                        requestedTabIndex = idx
                                        searchMode = false
                                        searchQuery = ""
                                    },
                                    text = { Text(tab.label) }
                                )
                            }
                        }
                    }
                    searchMode && tabList[pagerState.currentPage] != DropTabType.Media -> {
                        SearchTopBar(
                            searchQuery = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onBack = { searchMode = false; searchQuery = "" },
                        )
                        ScrollableTabRow(
                            selectedTabIndex = pagerState.currentPage,
                            edgePadding = 0.dp
                        ) {
                            tabList.forEachIndexed { idx, tab ->
                                Tab(
                                    selected = pagerState.currentPage == idx,
                                    onClick = {
                                        requestedTabIndex = idx
                                        searchMode = false
                                        searchQuery = ""
                                    },
                                    text = { Text(tab.label) }
                                )
                            }
                        }
                    }
                    else -> {
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
                }
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
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top,
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
                    DropListByTabFancy(
                        drops = filteredDrops,
                        tab = selectedTab,
                        onMediaClick = { drop ->
                            when (drop.type) {
                                DropType.IMAGE -> selectedImageUri = drop.uri
                                DropType.DOCUMENT -> openDocumentExternally(context, drop)
                                else -> onMediaClick(drop)
                            }
                        },
                        selectedDropIds = selectedDropIds,
                        onDropClick = { drop ->
                            if (selectedDropIds.isNotEmpty()) {
                                selectedDropIds = if (selectedDropIds.contains(drop.id)) {
                                    selectedDropIds - drop.id
                                } else {
                                    selectedDropIds + drop.id
                                }
                            } else {
                                when (drop.type) {
                                    DropType.IMAGE -> selectedImageUri = drop.uri
                                    DropType.DOCUMENT -> openDocumentExternally(context, drop)
                                    else -> onMediaClick(drop)
                                }
                            }
                        },
                        onDropLongClick = { drop ->
                            selectedDropIds = selectedDropIds + drop.id
                        }
                    )
                }
            }
        }

        if (showDeleteDialog && selectedDrops.isNotEmpty()) {
            MultiDeleteConfirmationDialog(
                count = selectedDrops.size,
                onDismiss = { showDeleteDialog = false },
                onDelete = {
                    onDeleteDrops(selectedDrops)
                    showDeleteDialog = false
                    selectedDropIds = emptySet()
                }
            )
        }

        // Image preview dialog (fullscreen)
        selectedImageUri?.let { imageUri ->
            FullScreenImageDialog(
                imageUri = imageUri,
                onDismiss = { selectedImageUri = null }
            )
        }
    }
}

// Helper to get the correct mime type for a file based on uri and filename
fun getMimeTypeFromUriOrName(uri: String?, fileName: String?): String {
    val extensionFromUri = uri?.let { u ->
        Uri.parse(u).lastPathSegment?.substringAfterLast('.', "")?.lowercase()
    }
    val extensionFromName = fileName?.substringAfterLast('.', "")?.lowercase()
    val extension = when {
        !extensionFromUri.isNullOrEmpty() -> extensionFromUri
        !extensionFromName.isNullOrEmpty() -> extensionFromName
        else -> null
    }
    return if (!extension.isNullOrEmpty()) {
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
    } else {
        "*/*"
    }
}

fun openDocumentExternally(context: Context, drop: Drop) {
    val uri = getShareableUri(context, drop.uri)
    val mimeType = drop.mimeType
        ?: getMimeTypeFromUriOrName(drop.uri, drop.title)
        ?: "*/*"
    if (uri != null) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            ContextCompat.startActivity(context, intent, null)
        } catch (e: Exception) {
            Toast.makeText(context, "No app found to open this document", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(context, "Cannot open this document", Toast.LENGTH_SHORT).show()
    }
}

fun getDocumentIconAndColor(title: String?, mimeType: String?, uri: String?): Pair<ImageVector, Color> {
    val name = (title ?: uri?.substringAfterLast('/') ?: "").lowercase()
    return when {
        name.endsWith(".pdf") || (mimeType?.contains("pdf") == true) ->
            Pair(Icons.Filled.PictureAsPdf, Color(0xFFD32F2F)) // Red for PDF

        name.endsWith(".doc") || name.endsWith(".docx") ||
                (mimeType?.contains("msword") == true) ||
                (mimeType?.contains("wordprocessingml") == true) ->
            Pair(Icons.Filled.Description, Color(0xFF1976D2)) // Blue for DOC/DOCX

        name.endsWith(".xls") || name.endsWith(".xlsx") ||
                (mimeType?.contains("excel") == true) ->
            Pair(Icons.Filled.TableChart, Color(0xFF388E3C)) // Green for Excel

        name.endsWith(".ppt") || name.endsWith(".pptx") ||
                (mimeType?.contains("powerpoint") == true) ->
            Pair(Icons.Filled.Slideshow, Color(0xFFF57C00)) // Orange for PPT

        name.endsWith(".txt") || (mimeType?.contains("text/plain") == true) ->
            Pair(Icons.Filled.TextSnippet, Color(0xFF616161)) // Gray for TXT

        name.endsWith(".rtf") || (mimeType?.contains("rtf") == true) ->
            Pair(Icons.Filled.Description, Color(0xFF7B1FA2)) // Purple for RTF

        else -> Pair(Icons.Filled.InsertDriveFile, Color(0xFF455A64)) // Default: blue-gray
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MultiSelectActionBar(
    selectedCount: Int,
    onClose: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    TopAppBar(
        title = { Text("$selectedCount") },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close selection")
            }
        },
        actions = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
            IconButton(onClick = onShare) {
                Icon(Icons.Default.Share, contentDescription = "Share")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    )
}

@Composable
private fun MultiDeleteConfirmationDialog(
    count: Int,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDelete) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Delete $count item${if (count > 1) "s" else ""}?") },
        text = { Text("Are you sure you want to delete the selected item${if (count > 1) "s" else ""}? This action cannot be undone.") }
    )
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
    BackHandler(onBack = onBack)

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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                placeholder = { Text("Search...", style = MaterialTheme.typography.labelMedium) },
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

@Composable
fun DropListByTabFancy(
    drops: List<Drop>,
    tab: DropTabType,
    onMediaClick: (Drop) -> Unit,
    selectedDropIds: Set<String>,
    onDropClick: (Drop) -> Unit,
    onDropLongClick: (Drop) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(0.dp)
    ) {
        items(drops, key = { it.id }) { drop ->
            val isSelected = selectedDropIds.contains(drop.id)
            val background =
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                else Color.Transparent

            Box(
                modifier = Modifier
                    .background(background)
                    .padding(vertical = 4.dp, horizontal = 6.dp)
                    .combinedClickable(
                        onClick = { onDropClick(drop) },
                        onLongClick = { onDropLongClick(drop) }
                    )
            ) {
                when (tab) {
                    DropTabType.Media -> DropImageCard(drop = drop, onClick = { onMediaClick(drop) })
                    DropTabType.Docs -> DropDocumentCardWithCorrectIcon(drop = drop, onClick = { onMediaClick(drop) })
                    DropTabType.Notes -> DropNoteCard(drop)
                    DropTabType.Links -> DropLinkCard(drop)
                }
            }
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        }
    }
}

@Composable
fun DropImageCard(drop: Drop, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.6f)
            .heightIn(min = 120.dp, max = 220.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = null
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        if (!drop.uri.isNullOrBlank()) {
            AsyncImage(
                model = drop.uri,
                contentDescription = drop.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                    contentDescription = "Image",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@Composable
fun DropDocumentCardWithCorrectIcon(drop: Drop, onClick: () -> Unit = {}) {
    val (icon, iconColor) = remember(drop.title, drop.mimeType, drop.uri) {
        getDocumentIconAndColor(drop.title, drop.mimeType, drop.uri)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(85.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = null
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = drop.title ?: "File",
                tint = iconColor,
                modifier = Modifier.size(42.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    drop.title ?: drop.uri?.substringAfterLast('/') ?: "Document",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    drop.mimeType ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun DropNoteCard(drop: Drop) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 70.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            if (!drop.title.isNullOrBlank()) {
                Text(
                    drop.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                drop.text ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DropLinkCard(drop: Drop) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 58.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            if (!drop.title.isNullOrBlank()) {
                Text(
                    drop.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
            Text(
                drop.uri ?: drop.text ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun Drop.matchesSearch(query: String): Boolean =
    (title?.contains(query, ignoreCase = true) == true) ||
            (text?.contains(query, ignoreCase = true) == true)

private fun getShareableUri(context: Context, uriString: String?): Uri? {
    if (uriString.isNullOrBlank()) return null
    val uri = Uri.parse(uriString)
    return when (uri.scheme) {
        "content" -> uri
        "file", null -> {
            try {
                val file = File(uri.path ?: uriString)
                if (file.exists()) {
                    FileProvider.getUriForFile(
                        context,
                        context.packageName + ".provider",
                        file
                    )
                } else null
            } catch (e: Exception) { null }
        }
        else -> null
    }
}

fun shareMultipleDrops(context: Context, drops: List<Drop>) {
    if (drops.isEmpty()) return

    val allText = drops.all { it.type == DropType.NOTE || it.type == DropType.LINK }
    val allMediaOrDocs = drops.all { it.type == DropType.IMAGE || it.type == DropType.DOCUMENT }

    when {
        allText -> {
            val combined = drops.joinToString("\n\n") {
                when (it.type) {
                    DropType.NOTE -> it.text.orEmpty()
                    DropType.LINK -> it.uri ?: it.text.orEmpty()
                    else -> ""
                }
            }
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, combined)
            }
            context.startActivity(Intent.createChooser(intent, "Share via"))
        }
        allMediaOrDocs -> {
            val uris = drops.mapNotNull { getShareableUri(context, it.uri) }
            if (uris.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "*/*"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share via"))
            } else {
                Toast.makeText(context, "Could not share selected files.", Toast.LENGTH_SHORT).show()
            }
        }
        else -> {
            Toast.makeText(context, "Cannot share mixed types together.", Toast.LENGTH_SHORT).show()
        }
    }
}