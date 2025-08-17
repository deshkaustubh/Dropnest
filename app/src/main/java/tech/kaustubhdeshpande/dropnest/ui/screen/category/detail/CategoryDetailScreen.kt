package tech.kaustubhdeshpande.dropnest.ui.screen.category.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import tech.kaustubhdeshpande.dropnest.domain.model.Drop
import tech.kaustubhdeshpande.dropnest.domain.model.DropType
import tech.kaustubhdeshpande.dropnest.presentation.navigation.DropNestDestination
import tech.kaustubhdeshpande.dropnest.presentation.navigation.LocalSafeNavigation
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    categoryId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoryDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val safeNavigation = LocalSafeNavigation.current
    val listState = rememberLazyListState()
    val context = LocalContext.current
    var showAttachSheet by remember { mutableStateOf(false) }
    var selectedImageDrop by remember { mutableStateOf<Drop?>(null) }

    // SEARCH STATE
    var searchMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf(listOf<Int>()) }
    var currentSearchIndex by remember { mutableStateOf(0) }

    // SELECTION STATE
    var selectedDropId by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // MENU STATE
    var showMenu by remember { mutableStateOf(false) }
    var menuCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val density = LocalDensity.current

    // Back: exit search or clear selection
    BackHandler(enabled = selectedDropId != null || searchMode) {
        if (searchMode) {
            searchMode = false
            searchQuery = ""
            searchResults = emptyList()
            currentSearchIndex = 0
        } else {
            selectedDropId = null
        }
    }

    val documentMimeTypes = arrayOf(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "text/plain",
        "application/rtf",
        "application/vnd.oasis.opendocument.text",
        "application/vnd.oasis.opendocument.spreadsheet"
    )
    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            viewModel.saveDocumentDrop(it.toString())
        }
    }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.saveImageDrop(it.toString()) } }

    // Load category/drops
    LaunchedEffect(categoryId) {
        viewModel.loadCategory(categoryId)
        viewModel.loadDrops(categoryId)
    }

    val sortedDrops = remember(uiState.drops) { uiState.drops.sortedBy { it.timestamp } }

    // SEARCH LOGIC - FIXED: Only match doc file names, not URI, for DropType.DOCUMENT
    LaunchedEffect(searchQuery, sortedDrops, searchMode) {
        if (searchMode && searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            val indexes = sortedDrops.mapIndexedNotNull { idx, drop ->
                when (drop.type) {
                    DropType.DOCUMENT -> {
                        val fileName = drop.title ?: drop.uri?.substringAfterLast('/') ?: ""
                        if (fileName.contains(q, ignoreCase = true)) idx else null
                    }
                    else -> {
                        if ((drop.text?.contains(q, ignoreCase = true) == true)
                            || (drop.title?.contains(q, ignoreCase = true) == true)
                        ) idx else null
                    }
                }
            }
            searchResults = indexes
            currentSearchIndex = 0
        } else {
            searchResults = emptyList()
            currentSearchIndex = 0
        }
    }

    LaunchedEffect(currentSearchIndex, searchResults, searchMode) {
        if (searchMode && searchResults.isNotEmpty() && currentSearchIndex in searchResults.indices) {
            listState.animateScrollToItem(searchResults[currentSearchIndex])
        }
    }

    val clipboardManager = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current

    val selectedDrop = sortedDrops.find { it.id == selectedDropId }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            when {
                searchMode -> {
                    SearchAppBar(
                        searchQuery = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onBack = {
                            searchMode = false
                            searchQuery = ""
                            searchResults = emptyList()
                            currentSearchIndex = 0
                        },
                        resultCount = searchResults.size,
                        currentIndex = if (searchResults.isNotEmpty()) currentSearchIndex + 1 else 0,
                        onPrev = {
                            if (searchResults.isNotEmpty()) {
                                currentSearchIndex =
                                    (currentSearchIndex - 1 + searchResults.size) % searchResults.size
                            }
                        },
                        onNext = {
                            if (searchResults.isNotEmpty()) {
                                currentSearchIndex = (currentSearchIndex + 1) % searchResults.size
                            }
                        }
                    )
                }
                selectedDropId != null -> {
                    Crossfade(targetState = selectedDrop?.type, label = "selectionBar") { type ->
                        when (type) {
                            DropType.NOTE, DropType.LINK -> {
                                ActionBarTextOrLink(
                                    onClose = { selectedDropId = null },
                                    onCopy = {
                                        val textToCopy =
                                            selectedDrop?.text ?: selectedDrop?.uri.orEmpty()
                                        if (textToCopy.isNotBlank()) {
                                            clipboardManager.setText(
                                                androidx.compose.ui.text.AnnotatedString(textToCopy)
                                            )
                                        }
                                        selectedDropId = null
                                    },
                                    onDelete = { showDeleteDialog = true },
                                    onMore = { }
                                )
                            }
                            DropType.IMAGE, DropType.DOCUMENT -> {
                                ActionBarMedia(
                                    onClose = { selectedDropId = null },
                                    onShare = { selectedDrop?.let { shareDrop(context, it) } },
                                    onDelete = { showDeleteDialog = true },
                                    onMore = { }
                                )
                            }
                            else -> {
                                ActionBarTextOrLink(
                                    onClose = { selectedDropId = null },
                                    onCopy = { selectedDropId = null },
                                    onDelete = { showDeleteDialog = true },
                                    onMore = { }
                                )
                            }
                        }
                    }
                }
                else -> {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    uiState.category?.name ?: "Category",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                if (!uiState.category?.name.isNullOrBlank()) {
                                    Text(
                                        text = "You're inside: ${uiState.category?.name}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            Box {
                                IconButton(
                                    onClick = { showMenu = true },
                                    modifier = Modifier.onGloballyPositioned { coordinates ->
                                        menuCoordinates = coordinates
                                    }
                                ) {
                                    Icon(Icons.Filled.MoreVert, contentDescription = "More menu")
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                    offset = DpOffset(0.dp, 0.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Search") },
                                        onClick = {
                                            showMenu = false
                                            searchMode = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Media, Links and Docs") },
                                        onClick = {
                                            showMenu = false
                                            safeNavigation.navigateTo(
                                                DropNestDestination.CategoryFilter.createRoute(
                                                    categoryId = categoryId
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                    end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                    top = paddingValues.calculateTopPadding(),
                    bottom = 0.dp
                )
                .background(MaterialTheme.colorScheme.background)
                .clickable(
                    enabled = selectedDropId != null,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }) {
                    selectedDropId = null
                }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (sortedDrops.isEmpty()) {
                        item { DropNestMessage(text = "Welcome to your ${uiState.category?.name ?: ""} vault!") }
                        item { DropNestMessage(text = "Add anything you like. Long press a drop to select it.") }
                    } else {
                        itemsIndexed(sortedDrops, key = { _, d -> d.id }) { idx, drop ->
                            val isSelected = drop.id == selectedDropId
                            val isMatching = searchMode && searchResults.contains(idx)
                            val isCurrentSearchResult =
                                searchMode && searchResults.isNotEmpty() && searchResults[currentSearchIndex] == idx
                            DropItem(
                                drop = drop,
                                isFromCurrentUser = true,
                                highlightText = if (isMatching) searchQuery else null,
                                modifier = Modifier
                                    .background(
                                        if (isSelected || isCurrentSearchResult)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                        else Color.Transparent
                                    )
                                    .then(
                                        Modifier.combinedClickable(
                                            onClick = {
                                                if (selectedDropId == drop.id) {
                                                    selectedDropId = null
                                                } else if (selectedDropId != null) {
                                                    selectedDropId = drop.id
                                                }
                                            },
                                            onLongClick = {
                                                haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                                selectedDropId = drop.id
                                            }
                                        )
                                    ),
                                onMediaClick = { clickedDrop ->
                                    if (selectedDropId == null && clickedDrop.type == DropType.IMAGE) {
                                        selectedImageDrop = clickedDrop
                                    }
                                }
                            )
                        }
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }

                DropInputField(
                    text = uiState.inputText,
                    onTextChange = { viewModel.updateInputText(it) },
                    onSendClick = { viewModel.sendDrop() },
                    onAttachClick = { showAttachSheet = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (showAttachSheet) {
                AttachMediaBottomSheet(
                    categoryName = uiState.category?.name ?: "",
                    onDismiss = { showAttachSheet = false },
                    onImageClick = {
                        imagePicker.launch("image/*")
                        showAttachSheet = false
                    },
                    onDocumentClick = {
                        documentPicker.launch(documentMimeTypes)
                        showAttachSheet = false
                    }
                )
            }

            // IMAGE PREVIEW DIALOG
            selectedImageDrop?.let { drop ->
                drop.uri?.let { imageUri ->
                    FullScreenImageDialog(
                        imageUri = imageUri,
                        onDismiss = { selectedImageDrop = null }
                    )
                }
            }

            if (showDeleteDialog && selectedDrop != null) {
                DeleteConfirmationDialog(
                    onDismiss = { showDeleteDialog = false },
                    onDelete = {
                        viewModel.deleteDropById(selectedDrop)
                        showDeleteDialog = false
                        selectedDropId = null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAppBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    resultCount: Int,
    currentIndex: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    TopAppBar(
        title = {
            TextField(
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
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            Text(
                text = if (resultCount > 0) "$currentIndex/$resultCount" else "",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.alignByBaseline()
            )
            IconButton(onClick = onPrev, enabled = resultCount > 0) {
                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Previous")
            }
            IconButton(onClick = onNext, enabled = resultCount > 0) {
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Next")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionBarTextOrLink(
    onClose: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onMore: () -> Unit
) {
    TopAppBar(
        title = { Text("1") },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Close selection")
            }
        },
        actions = {
            IconButton(onClick = onCopy) {
                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
            IconButton(onClick = onMore) {
                Icon(Icons.Filled.MoreVert, contentDescription = "More actions")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionBarMedia(
    onClose: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onMore: () -> Unit
) {
    TopAppBar(
        title = { Text("1") },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Close selection")
            }
        },
        actions = {
            IconButton(onClick = onShare) {
                Icon(Icons.Filled.Share, contentDescription = "Share")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete")
            }
            IconButton(onClick = onMore) {
                Icon(Icons.Filled.MoreVert, contentDescription = "More actions")
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
private fun DeleteConfirmationDialog(
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
        title = { Text("Delete this drop?") },
        text = { Text("Are you sure you want to delete this item? This action cannot be undone.") }
    )
}

fun shareDrop(context: Context, drop: Drop) {
    when (drop.type) {
        DropType.IMAGE, DropType.DOCUMENT -> {
            val fileUri = try {
                val parsedUri = Uri.parse(drop.uri)
                when {
                    parsedUri.scheme == "file" || parsedUri.scheme.isNullOrEmpty() -> {
                        val file =
                            if (parsedUri.scheme == "file") File(parsedUri.path!!) else File(drop.uri)
                        FileProvider.getUriForFile(
                            context,
                            context.packageName + ".provider",
                            file
                        )
                    }
                    parsedUri.scheme == "content" -> parsedUri
                    else -> null
                }
            } catch (e: Exception) {
                null
            }
            fileUri?.let {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = drop.mimeType ?: "*/*"
                    putExtra(Intent.EXTRA_STREAM, it)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(
                    Intent.createChooser(intent, "Share via")
                )
            }
        }
        DropType.LINK -> {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, drop.uri ?: drop.text ?: "")
            }
            context.startActivity(Intent.createChooser(intent, "Share via"))
        }
        DropType.NOTE -> {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, drop.text ?: "")
            }
            context.startActivity(Intent.createChooser(intent, "Share via"))
        }
        else -> {
            Toast.makeText(context, "Sharing this type is not supported.", Toast.LENGTH_SHORT)
                .show()
        }
    }
}