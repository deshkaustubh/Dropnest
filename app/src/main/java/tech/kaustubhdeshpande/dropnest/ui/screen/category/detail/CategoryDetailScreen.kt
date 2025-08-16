package tech.kaustubhdeshpande.dropnest.ui.screen.category.detail

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.kaustubhdeshpande.dropnest.domain.model.Drop
import tech.kaustubhdeshpande.dropnest.domain.model.DropType
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components.AttachMediaBottomSheet
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components.DropInputField
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components.DropItem
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components.DropNestMessage
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components.FullScreenImageDialog

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

    // State for the selected image for preview
    var selectedImageDrop by remember { mutableStateOf<Drop?>(null) }

    // Define document MIME types as an array for OpenDocument
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

    // Document picker using OpenDocument which accepts an array of MIME types
    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            // Request persistent permissions for the URI
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Ignore if we can't get persistable permission
            }

            viewModel.saveDocumentDrop(it.toString())
        }
    }

    // Launch effect to load category and drops
    LaunchedEffect(categoryId) {
        viewModel.loadCategory(categoryId)
        viewModel.loadDrops(categoryId)
    }

    // Sort drops chronologically (oldest first)
    val sortedDrops = remember(uiState.drops) {
        uiState.drops.sortedBy { it.timestamp }
    }

    // Auto-scroll to bottom (newest messages) when data is loaded or new messages are added
    LaunchedEffect(sortedDrops.size) {
        if (sortedDrops.isNotEmpty()) {
            // Scroll to bottom (newest messages)
            listState.scrollToItem(sortedDrops.size - 1)
        }
    }

    // Image picker
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.saveImageDrop(it.toString())
        }
    }

    Scaffold(
        // Configure Scaffold to not apply window insets automatically
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                // Include both title and subtitle in the TopAppBar
                title = {
                    Column {
                        Text(
                            text = uiState.category?.name ?: "Category",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "You're inside: ${uiState.category?.name ?: ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    // Track last click time to implement debounce
                    var lastClickTime by remember { mutableStateOf(0L) }
                    val debounceTime = 300L // milliseconds

                    IconButton(
                        onClick = {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastClickTime > debounceTime) {
                                lastClickTime = currentTime
                                onBackClick()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Settings icon used to edit the category
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Edit Category"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                // Apply status bar insets to the top app bar
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Chat messages list - chronological order (oldest at top, newest at bottom)
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clipToBounds()  // Ensure content doesn't overflow
                ) {
                    // Welcome messages (only show if no drops yet)
                    if (sortedDrops.isEmpty()) {
                        item {
                            DropNestMessage(
                                text = "Welcome to your ${uiState.category?.name ?: ""} vault!"
                            )
                        }

                        item {
                            DropNestMessage(
                                text = "This is your personal space for collecting and organizing content.\n\n" +
                                        "• Use the attachment icon to add images and documents\n" +
                                        "• Type or paste links and notes directly\n" +
                                        "• Your content stays organized in this category"
                            )
                        }

                        item {
                            DropNestMessage(
                                text = "Start adding content to build your collection. Everything is searchable and easy to find later."
                            )
                        }
                    } else {
                        // Display drops in chronological order (oldest first)
                        items(sortedDrops) { drop ->
                            DropItem(
                                drop = drop,
                                isFromCurrentUser = true,
                                onMediaClick = { clickedDrop ->
                                    // Handle media click based on type
                                    if (clickedDrop.type == DropType.IMAGE) {
                                        // Set the selected image to show the full screen preview
                                        selectedImageDrop = clickedDrop
                                    }
                                }
                            )
                        }
                    }

                    // Add some space at the bottom for better UX
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Input field
                DropInputField(
                    text = uiState.inputText,
                    onTextChange = { viewModel.updateInputText(it) },
                    onSendClick = {
                        viewModel.sendDrop()
                    },
                    onAttachClick = { showAttachSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }

            // Update the AttachMediaBottomSheet call
            if (showAttachSheet) {
                AttachMediaBottomSheet(
                    categoryName = uiState.category?.name ?: "",
                    onDismiss = { showAttachSheet = false },
                    onImageClick = {
                        imagePicker.launch("image/*")
                        showAttachSheet = false
                    },
                    onDocumentClick = {
                        // Launch document picker with the array of MIME types
                        documentPicker.launch(documentMimeTypes)
                        showAttachSheet = false
                    }
                )
            }

            // Show full screen image dialog when an image is selected
            selectedImageDrop?.let { drop ->
                drop.uri?.let { imageUri ->
                    FullScreenImageDialog(
                        imageUri = imageUri,
                        onDismiss = { selectedImageDrop = null }
                    )
                }
            }
        }
    }
}