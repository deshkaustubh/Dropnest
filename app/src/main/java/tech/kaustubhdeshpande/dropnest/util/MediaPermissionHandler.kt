package tech.kaustubhdeshpande.dropnest.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat

/**
 * Helper class to handle media permissions and picking based on Android version
 */
class MediaPermissionHandler(private val context: Context) {

    /**
     * Checks if the app has the necessary permissions to access media
     */
    fun hasMediaPermissions(): Boolean {
        return when {
            // For Android 13+ (API 33+), check for granular media permissions
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
            }
            // For Android 10+ (API 29+), only READ_EXTERNAL_STORAGE is needed
            else -> {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    /**
     * Returns the list of required permissions based on Android version
     */
    fun getRequiredPermissions(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
                )
            }
            else -> {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }
}

/**
 * Wrapper to unify media picking functionality across Android versions
 */
class MediaPicker(
    private val modernPicker: ActivityResultLauncher<PickVisualMediaRequest>? = null,
    private val legacyPicker: ActivityResultLauncher<String>? = null
) {
    fun pickMedia(mediaType: ActivityResultContracts.PickVisualMedia.VisualMediaType = ActivityResultContracts.PickVisualMedia.ImageOnly) {
        if (modernPicker != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Use modern photo picker on Android 13+
            modernPicker.launch(PickVisualMediaRequest(mediaType))
        } else if (legacyPicker != null) {
            // Use legacy content picker on older versions
            val mimeType = when (mediaType) {
                ActivityResultContracts.PickVisualMedia.ImageOnly -> "image/*"
                ActivityResultContracts.PickVisualMedia.VideoOnly -> "video/*"
                ActivityResultContracts.PickVisualMedia.ImageAndVideo -> "*/*"
                else -> "image/*"
            }
            legacyPicker.launch(mimeType)
        }
    }
}

/**
 * Composable function to create a unified media picker across Android versions
 */
@Composable
fun rememberMediaPicker(onMediaSelected: (Uri) -> Unit): MediaPicker {
    // Modern photo picker (Android 13+)
    val modernPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { onMediaSelected(it) }
    }

    // Legacy content picker (Android < 13)
    val legacyPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onMediaSelected(it) }
    }

    // Return a wrapper that handles the right picker based on Android version
    return MediaPicker(modernPicker, legacyPicker)
}