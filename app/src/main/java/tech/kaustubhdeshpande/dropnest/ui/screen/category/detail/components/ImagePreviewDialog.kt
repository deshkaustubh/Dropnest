package tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import java.io.File

/**
 * Full screen image preview dialog that completely takes over the screen
 */
@Composable
fun FullScreenImageDialog(
    imageUri: String,
    onDismiss: () -> Unit
) {
    // Use a Dialog with fullscreen properties
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false // Makes dialog full width
        )
    ) {
        ImagePreviewContent(
            imageUri = imageUri,
            onDismiss = onDismiss,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun ImagePreviewContent(
    imageUri: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val window = context.getActivity()?.window

    // Handle immersive mode
    DisposableEffect(Unit) {
        window?.let {
            // Go edge-to-edge
            WindowCompat.setDecorFitsSystemWindows(it, false)

            // Hide system bars in immersive mode
            val controller = WindowInsetsControllerCompat(it, view)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        onDispose {
            // Restore normal mode when dismissed
            window?.let {
                WindowCompat.setDecorFitsSystemWindows(it, true)
                val controller = WindowInsetsControllerCompat(it, view)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    // Convert file path to content URI for safe access
    val contentUri = remember {
        try {
            val file = File(imageUri)
            if (file.exists()) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
            } else {
                Uri.parse(imageUri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Uri.parse(imageUri)
        }
    }

    // Control UI visibility
    var showControls by remember { mutableStateOf(true) }

    // Auto-hide controls after a few seconds
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(3000)
            showControls = false
        }
    }

    // Completely full screen experience
    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Image with zoom and pan
        var scale by remember { mutableFloatStateOf(1f) }
        var offsetX by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(0f) }

        // Track double tap for quick zoom in/out
        var lastTapTime by remember { mutableLongStateOf(0L) }
        val doubleTapDelay = 300L

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(contentUri)
                .crossfade(true)
                .error(android.R.drawable.ic_menu_report_image)
                .build(),
            contentDescription = "Image",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 5f)

                        // Only pan when zoomed in
                        if (scale > 1f) {
                            val maxX = (size.width * (scale - 1)) / 2
                            val maxY = (size.height * (scale - 1)) / 2

                            offsetX = (offsetX + pan.x).coerceIn(-maxX, maxX)
                            offsetY = (offsetY + pan.y).coerceIn(-maxY, maxY)
                        } else {
                            // Reset position when zoomed out
                            offsetX = 0f
                            offsetY = 0f
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastTapTime < doubleTapDelay) {
                                // Double tap detected
                                if (scale > 1f) {
                                    // Reset zoom
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                } else {
                                    // Zoom to 2x
                                    scale = 2f
                                }
                            } else {
                                // Single tap - toggle controls
                                showControls = !showControls
                            }
                            lastTapTime = currentTime
                        }
                    )
                }
        )

        // Floating controls that appear/disappear on tap
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(8.dp)
            ) {
                // Back button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Share button
                IconButton(
                    onClick = {
                        try {
                            val file = File(imageUri)
                            if (file.exists()) {
                                val shareUri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    file
                                )

                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/*"
                                    putExtra(Intent.EXTRA_STREAM, shareUri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }

                                context.startActivity(Intent.createChooser(intent, "Share via"))
                            } else {
                                Toast.makeText(
                                    context,
                                    "Cannot share: Image file not found",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(
                                context,
                                "Failed to share image",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

// Helper extension function to get the Activity from a Context
fun Context.getActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}