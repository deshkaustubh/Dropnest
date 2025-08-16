package tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun AttachMediaBottomSheet(
    categoryName: String,
    onDismiss: () -> Unit,
    onImageClick: () -> Unit,
    onDocumentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Define colors to match the original design
    val backgroundColor = Color(0xFF131B17) // Very dark green/black
    val buttonColor = Color(0xFF8CE3B0)     // Light mint green
    val textColor = Color.White
    val buttonTextColor = Color(0xFF0A1F13) // Dark green/black for button text

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            color = backgroundColor
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .fillMaxWidth()
            ) {
                // Handle
                Surface(
                    modifier = Modifier
                        .height(4.dp)
                        .width(40.dp),
                    shape = RoundedCornerShape(2.dp),
                    color = Color.Gray.copy(alpha = 0.5f)
                ) {}

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Attach Media",
                    style = MaterialTheme.typography.headlineMedium,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Drop an image or document into your '$categoryName' vault.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Upload Image Button - with less rounded corners to match original design
                Button(
                    onClick = onImageClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = buttonTextColor
                    )
                ) {
                    Text(
                        text = "Upload Image",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Upload Document Button
                Button(
                    onClick = onDocumentClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = buttonTextColor
                    )
                ) {
                    Text(
                        text = "Upload Document",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}