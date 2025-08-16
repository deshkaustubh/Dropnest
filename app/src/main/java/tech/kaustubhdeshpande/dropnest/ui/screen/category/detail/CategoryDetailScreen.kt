package tech.kaustubhdeshpande.dropnest.ui.screen.category.detail

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components.AttachMediaBottomSheet
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components.DropInputField
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components.DropItem
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components.SystemMessage

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

    // Auto-scroll to bottom when new items are added
    LaunchedEffect(uiState.drops.size) {
        if (uiState.drops.isNotEmpty()) {
            listState.animateScrollToItem(uiState.drops.size - 1)
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
        topBar = {
            TopAppBar(
                title = { Text(uiState.category?.name ?: "Category") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121C17),
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF0B1410))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Subtitle showing current category
                Text(
                    text = "You're inside: ${uiState.category?.name ?: ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Chat messages list
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // System welcome messages (only show if no drops yet)
                    if (uiState.drops.isEmpty()) {
                        item {
                            SystemMessage(
                                text = "Welcome to DropNest! This thread is your personal vault."
                            )
                        }

                        item {
                            SystemMessage(
                                text = "Here's a quick guide to get you started: \n" +
                                        "1. **Drop Items:** Tap the '+' or 📎 to add links, notes, images, or PDFs.\n" +
                                        "2. **Thread = Vault:** Everything you drop stays organized in this thread.\n" +
                                        "3. **Search:** Use the search bar to find items instantly.\n" +
                                        "4. **Settings:** Customize your DropNest experience anytime."
                            )
                        }

                        item {
                            Text(
                                text = "Everything you drop here stays organized in your ${uiState.category?.name ?: ""} vault.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .padding(vertical = 16.dp)
                                    .fillMaxWidth()
                            )

                            Text(
                                text = "Start by saving your first item. This space is yours.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .padding(vertical = 16.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }

                    // User-added drops
                    items(uiState.drops) { drop ->
                        DropItem(drop = drop)
                    }
                }

                // Input field
                DropInputField(
                    text = uiState.inputText,
                    onTextChange = { viewModel.updateInputText(it) },
                    onSendClick = { viewModel.sendDrop() },
                    onAttachClick = { showAttachSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
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