package tech.kaustubhdeshpande.dropnest.ui.screen.category.detail

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
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
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
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components.AttachMediaBottomSheet
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components.DropInputField
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components.DropItem
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components.DropNestMessage

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

    // PDF picker
    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.savePdfDrop(it.toString())
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
                    IconButton(onClick = onBackClick) {
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
                                        "• Use the attachment icon to add images and PDFs\n" +
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
                            DropItem(drop = drop)
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
                        .windowInsetsPadding(WindowInsets.navigationBars)  // Apply navigation bar insets
                )
            }

            // Attach media bottom sheet
            if (showAttachSheet) {
                AttachMediaBottomSheet(
                    categoryName = uiState.category?.name ?: "",
                    onDismiss = { showAttachSheet = false },
                    onImageClick = {
                        imagePicker.launch("image/*")
                        showAttachSheet = false
                    },
                    onPdfClick = {
                        pdfPicker.launch("application/pdf")
                        showAttachSheet = false
                    }
                )
            }
        }
    }
}