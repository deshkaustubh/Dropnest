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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import tech.kaustubhdeshpande.dropnest.domain.model.Drop
import tech.kaustubhdeshpande.dropnest.domain.model.DropType
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components.AttachMediaBottomSheet
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components.DropInputField
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components.DropItem
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components.DropNestMessage
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components.FullScreenImageDialog
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    categoryId: String,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoryDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    var showAttachSheet by remember { mutableStateOf(false) }
    var selectedImageDrop by remember { mutableStateOf<Drop?>(null) }

    // SINGLE selection state (ID or null)
    var selectedDropId by remember { mutableStateOf<String?>(null) }
    // Delete dialog visibility
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Back press disables selection mode
    BackHandler(enabled = selectedDropId != null) {
        selectedDropId = null
    }

    // Document/image pickers (unchanged)
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

    // Chronological sort + auto-scroll
    val sortedDrops = remember(uiState.drops) { uiState.drops.sortedBy { it.timestamp } }
    LaunchedEffect(sortedDrops.size) {
        if (sortedDrops.isNotEmpty()) listState.scrollToItem(sortedDrops.size - 1)
    }

    val clipboardManager = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current

    val selectedDrop = sortedDrops.find { it.id == selectedDropId }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Crossfade(targetState = selectedDropId != null, label = "selectionBar") { selecting ->
                if (selecting) {
                    when (selectedDrop?.type) {
                        DropType.NOTE, DropType.LINK -> {
                            ActionBarTextOrLink(
                                onClose = { selectedDropId = null },
                                onCopy = {
                                    val textToCopy = selectedDrop.text ?: selectedDrop.uri.orEmpty()
                                    if (textToCopy.isNotBlank()) {
                                        clipboardManager.setText(
                                            androidx.compose.ui.text.AnnotatedString(
                                                textToCopy
                                            )
                                        )
                                    }
                                    selectedDropId = null
                                },
                                onDelete = {
                                    showDeleteDialog = true // Only open dialog!
                                },
                                onMore = { /* TODO: More options */ }
                            )
                        }

                        DropType.IMAGE, DropType.DOCUMENT -> {
                            ActionBarMedia(
                                onClose = { selectedDropId = null },
                                onShare = { shareDrop(context, selectedDrop) },
                                onDelete = {
                                    showDeleteDialog = true // Only open dialog!
                                },
                                onMore = { /* TODO: More options */ }
                            )
                        }

                        else -> {
                            ActionBarTextOrLink(
                                onClose = { selectedDropId = null },
                                onCopy = { selectedDropId = null },
                                onDelete = { showDeleteDialog = true }, // Only open dialog!
                                onMore = { /* TODO: More options */ }
                            )
                        }
                    }
                } else {
                    NormalCategoryTopBarUI(
                        title = uiState.category?.name ?: "Category",
                        subtitle = uiState.category?.name?.let { "You're inside: $it" } ?: "",
                        onBack = onBackClick,
                        onSettings = onSettingsClick
                    )
                }
            }
        }
    ) { paddingValues ->
        // Click outside (on background) always clears selection
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
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            enabled = selectedDropId != null,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }) {
                            selectedDropId = null
                        }
                ) {
                    if (sortedDrops.isEmpty()) {
                        item { DropNestMessage(text = "Welcome to your ${uiState.category?.name ?: ""} vault!") }
                        item { DropNestMessage(text = "Add anything you like. Long press a drop to select it.") }
                    } else {
                        itemsIndexed(sortedDrops, key = { _, d -> d.id }) { _, drop ->
                            val isSelected = drop.id == selectedDropId
                            DropItem(
                                drop = drop,
                                isFromCurrentUser = true,
                                modifier = Modifier
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(
                                            alpha = 0.18f
                                        ) else Color.Transparent
                                    )
                                    .then(
                                        Modifier
                                            .combinedClickable(
                                                onClick = {
                                                    if (selectedDropId == drop.id) {
                                                        selectedDropId = null
                                                    } else if (selectedDropId != null) {
                                                        selectedDropId = drop.id
                                                    }
                                                    // else: let normal click do nothing unless in selection mode
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

            selectedImageDrop?.let { drop ->
                drop.uri?.let { imageUri ->
                    FullScreenImageDialog(
                        imageUri = imageUri,
                        onDismiss = { selectedImageDrop = null }
                    )
                }
            }

            // Delete confirmation dialog
            if (showDeleteDialog && selectedDrop != null) {
                DeleteConfirmationDialog(
                    onDismiss = { showDeleteDialog = false },
                    onDelete = {
                        viewModel.deleteDropById(selectedDrop) // Actually delete here!
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
private fun NormalCategoryTopBarUI(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    onSettings: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(title, style = MaterialTheme.typography.titleLarge)
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = onSettings) {
                Icon(Icons.Filled.Settings, contentDescription = "Edit Category")
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
            // drop.uri might be a file://, content://, or just a path string
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