package tech.kaustubhdeshpande.dropnest.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController

// Create a composition local to provide SafeNavigation throughout the app
val LocalSafeNavigation = compositionLocalOf<SafeNavigation> {
    error("SafeNavigation not provided")
}

/**
 * Provides SafeNavigation to all composables in the composition
 */
@Composable
fun SafeNavigationProvider(
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    val safeNavigation = remember(navController) {
        SafeNavigation(navController)
    }

    CompositionLocalProvider(LocalSafeNavigation provides safeNavigation) {
        content()
    }
}