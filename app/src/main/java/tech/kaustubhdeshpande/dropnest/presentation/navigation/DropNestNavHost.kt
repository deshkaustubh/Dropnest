package tech.kaustubhdeshpande.dropnest.presentation.navigation

import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import tech.kaustubhdeshpande.dropnest.ui.screen.category.CreateCategoryScreen
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.CategoryDetailScreen
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.CategoryDetailViewModel
import tech.kaustubhdeshpande.dropnest.ui.screen.categoryfilter.CategoryFilterScreen
import tech.kaustubhdeshpande.dropnest.ui.screen.categoryfilter.DropTabType
import tech.kaustubhdeshpande.dropnest.ui.screen.home.HomeScreen
import tech.kaustubhdeshpande.dropnest.ui.screen.home.HomeViewModelImpl
import tech.kaustubhdeshpande.dropnest.ui.screen.vault.VaultScreen
import tech.kaustubhdeshpande.dropnest.ui.screen.welcome.WelcomeScreen

private const val TAG = "DropNestNavHost"

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DropNestNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = DropNestDestination.Welcome.route
) {
    SafeNavigationProvider(navController) {
        val safeNavigation = LocalSafeNavigation.current

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier
        ) {
            composable(
                route = DropNestDestination.Welcome.route,
                enterTransition = {
                    slideInHorizontally(
                        animationSpec = tween(350),
                        initialOffsetX = { it }
                    )
                },
                exitTransition = {
                    slideOutHorizontally(
                        animationSpec = tween(350),
                        targetOffsetX = { -it }
                    )
                },
                popEnterTransition = {
                    slideInHorizontally(
                        animationSpec = tween(350),
                        initialOffsetX = { -it }
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        animationSpec = tween(350),
                        targetOffsetX = { it }
                    )
                }
            ) {
                WelcomeScreen(
                    onTimeout = {
                        safeNavigation.navigateTo(DropNestDestination.Home.route) {
                            popUpTo(DropNestDestination.Welcome.route) { inclusive = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    delayMillis = 1500L,
                )
            }

            composable(
                route = DropNestDestination.Vault.route,
                enterTransition = { defaultWhatsAppEnter() },
                exitTransition = { defaultWhatsAppExit() },
                popEnterTransition = { defaultWhatsAppPopEnter() },
                popExitTransition = { defaultWhatsAppPopExit() }
            ) {
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

            composable(
                route = DropNestDestination.Home.route,
                enterTransition = { defaultWhatsAppEnter() },
                exitTransition = { defaultWhatsAppExit() },
                popEnterTransition = { defaultWhatsAppPopEnter() },
                popExitTransition = { defaultWhatsAppPopExit() }
            ) {
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
                        safeNavigation.navigateTo(
                            DropNestDestination.CategoryDetail.createRoute(
                                categoryId
                            )
                        )
                    }
                )
            }

            composable(
                route = DropNestDestination.CreateCategory.route,
                enterTransition = { defaultWhatsAppEnter() },
                exitTransition = { defaultWhatsAppExit() },
                popEnterTransition = { defaultWhatsAppPopEnter() },
                popExitTransition = { defaultWhatsAppPopExit() }
            ) {
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

            composable(
                route = DropNestDestination.EditCategory.route,
                arguments = DropNestDestination.EditCategory.arguments,
                enterTransition = { defaultWhatsAppEnter() },
                exitTransition = { defaultWhatsAppExit() },
                popEnterTransition = { defaultWhatsAppPopEnter() },
                popExitTransition = { defaultWhatsAppPopExit() }
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

            composable(
                route = DropNestDestination.CategoryDetail.route,
                arguments = DropNestDestination.CategoryDetail.arguments,
                enterTransition = { defaultWhatsAppEnter() },
                exitTransition = { defaultWhatsAppExit() },
                popEnterTransition = { defaultWhatsAppPopEnter() },
                popExitTransition = { defaultWhatsAppPopExit() }
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
                Log.d(TAG, "Navigating to Category Detail screen for category: $categoryId")
                CategoryDetailScreen(
                    categoryId = categoryId,
                    onBackClick = {
                        Log.d(TAG, "Navigating back from Category Detail")
                        safeNavigation.popBackStack()
                    }
                )
            }

            composable(
                route = DropNestDestination.CategoryFilter.route,
                arguments = DropNestDestination.CategoryFilter.arguments,
                enterTransition = { defaultWhatsAppEnter() },
                exitTransition = { defaultWhatsAppExit() },
                popEnterTransition = { defaultWhatsAppPopEnter() },
                popExitTransition = { defaultWhatsAppPopExit() }
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getString("categoryId") ?: return@composable
                val viewModel: CategoryDetailViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()

                LaunchedEffect(categoryId) {
                    viewModel.loadCategory(categoryId)
                    viewModel.loadDrops(categoryId)
                }

                CategoryFilterScreen(
                    categoryName = uiState.category?.name ?: "",
                    drops = uiState.drops,
                    initialTab = DropTabType.Media,
                    onBackClick = { safeNavigation.popBackStack() },
                )
            }
        }
    }
}

// Helper functions for consistent WhatsApp-style transitions
@OptIn(ExperimentalAnimationApi::class)
private fun AnimatedContentTransitionScope<*>.defaultWhatsAppEnter() =
    slideInHorizontally(animationSpec = tween(100)) { it }

@OptIn(ExperimentalAnimationApi::class)
private fun AnimatedContentTransitionScope<*>.defaultWhatsAppExit() =
    slideOutHorizontally(animationSpec = tween(100)) { -it }

@OptIn(ExperimentalAnimationApi::class)
private fun AnimatedContentTransitionScope<*>.defaultWhatsAppPopEnter() =
    slideInHorizontally(animationSpec = tween(100)) { -it }

@OptIn(ExperimentalAnimationApi::class)
private fun AnimatedContentTransitionScope<*>.defaultWhatsAppPopExit() =
    slideOutHorizontally(animationSpec = tween(100)) { it }