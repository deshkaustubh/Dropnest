package tech.kaustubhdeshpande.dropnest.presentation.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import tech.kaustubhdeshpande.dropnest.ui.screen.category.CreateCategoryScreen
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.CategoryDetailScreen
import tech.kaustubhdeshpande.dropnest.ui.screen.home.HomeScreen
import tech.kaustubhdeshpande.dropnest.ui.screen.home.HomeViewModelImpl
import tech.kaustubhdeshpande.dropnest.ui.screen.vault.VaultScreen
import tech.kaustubhdeshpande.dropnest.ui.screen.welcome.WelcomeScreen

private const val TAG = "DropNestNavHost"

@Composable
fun DropNestNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = DropNestDestination.Welcome.route
) {
    // Wrap everything in our SafeNavigationProvider
    SafeNavigationProvider(navController) {
        // Get reference to our safe navigation
        val safeNavigation = LocalSafeNavigation.current

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier
        ) {
            // Welcome screen
            composable(route = DropNestDestination.Welcome.route) {
                WelcomeScreen(
                    onTimeout = {
                        // Use safe navigation instead of direct NavController
                        safeNavigation.navigateTo(DropNestDestination.Home.route) {
                            popUpTo(DropNestDestination.Welcome.route) { inclusive = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    delayMillis = 1500L,
                )
            }

            // Vault screen - main screen of the app
            composable(route = DropNestDestination.Vault.route) {
                VaultScreen(
                    onCreateDrop = {
                        safeNavigation.navigateTo(DropNestDestination.CreateDrop.route)
                    },
                    onDropClick = { dropId ->
                        safeNavigation.navigateTo(DropNestDestination.DropDetail.createRoute(dropId))
                    },
                    onSettingsClick = {
                        safeNavigation.navigateTo(DropNestDestination.Settings.route)
                    }
                )
            }

            // Home Screen - new main screen of the app
            composable(route = DropNestDestination.Home.route) {
                Log.d(TAG, "Navigating to Home screen")
                val viewModel: HomeViewModelImpl = hiltViewModel()
                HomeScreen(
                    viewModel = viewModel,
                    onCreateCategoryClick = {
                        Log.d(TAG, "Navigating to Create Category screen")
                        safeNavigation.navigateTo(DropNestDestination.CreateCategory.route)
                    },
                    onCategoryClick = { categoryId ->
                        Log.d(TAG, "Navigating to Category Detail screen for category: $categoryId")
                        safeNavigation.navigateTo(DropNestDestination.CategoryDetail.createRoute(categoryId))
                    }
                )
            }

            // Create Category screen
            composable(route = DropNestDestination.CreateCategory.route) {
                CreateCategoryScreen(
                    onBackClick = {
                        Log.d(TAG, "Navigating back from Create Category")
                        safeNavigation.popBackStack()
                    },
                    onCategorySaved = {
                        Log.d(TAG, "Category saved, navigating back")
                        safeNavigation.popBackStack()
                    }
                )
            }

            // Edit Category screen
            composable(
                route = DropNestDestination.EditCategory.route,
                arguments = DropNestDestination.EditCategory.arguments
            ) {
                CreateCategoryScreen(
                    onBackClick = {
                        Log.d(TAG, "Navigating back from Edit Category")
                        safeNavigation.popBackStack()
                    },
                    onCategorySaved = {
                        Log.d(TAG, "Category updated, navigating back")
                        safeNavigation.popBackStack()
                    }
                )
            }

            // Category Detail screen
            composable(
                route = DropNestDestination.CategoryDetail.route,
                arguments = DropNestDestination.CategoryDetail.arguments
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
                Log.d(TAG, "Navigating to Category Detail screen for category: $categoryId")
                CategoryDetailScreen(
                    categoryId = categoryId,
                    onBackClick = {
                        Log.d(TAG, "Navigating back from Category Detail")
                        safeNavigation.popBackStack()
                    },
                    onSettingsClick = {
                        // Navigate to EditCategory screen with current categoryId
                        Log.d(TAG, "Navigating to Edit Category screen from Category Detail for category: $categoryId")
                        safeNavigation.navigateTo(DropNestDestination.EditCategory.createRoute(categoryId))
                    }
                )
            }

            // Other routes will be implemented as we build those screens
        }
    }
}