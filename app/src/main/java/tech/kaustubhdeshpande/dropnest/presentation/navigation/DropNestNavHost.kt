package tech.kaustubhdeshpande.dropnest.presentation.navigation

import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import tech.kaustubhdeshpande.dropnest.presentation.navigation.DropNestBottomNavigation
import tech.kaustubhdeshpande.dropnest.ui.screen.category.CreateCategoryScreen
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.CategoryDetailScreen
import tech.kaustubhdeshpande.dropnest.ui.screen.category.detail.CategoryDetailViewModel
import tech.kaustubhdeshpande.dropnest.ui.screen.categoryfilter.CategoryFilterScreen
import tech.kaustubhdeshpande.dropnest.ui.screen.categoryfilter.DropTabType
import tech.kaustubhdeshpande.dropnest.ui.screen.home.HomeScreen
import tech.kaustubhdeshpande.dropnest.ui.screen.home.HomeViewModelImpl
import tech.kaustubhdeshpande.dropnest.ui.screen.welcome.WelcomeScreen
import tech.kaustubhdeshpande.dropnest.ui.screen.categorylist.CategoryListScreen
import tech.kaustubhdeshpande.dropnest.ui.screen.settings.SettingsScreen

private const val TAG = "DropNestNavHost"

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DropNestNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = DropNestDestination.Home.route
) {
    SafeNavigationProvider(navController) {
        val safeNavigation = LocalSafeNavigation.current
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Show bottom navigation only on these routes
        val bottomBarRoutes = setOf(
            DropNestDestination.Categories.route,
            DropNestDestination.Home.route,
            DropNestDestination.Settings.route
        )

        Scaffold(
            bottomBar = {
                if (currentRoute in bottomBarRoutes) {
                    DropNestBottomNavigation(navController = navController)
                }
            }
        ) { innerPadding ->
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

                // Left tab: Categories list
                composable(
                    route = DropNestDestination.Categories.route,
                    enterTransition = { defaultWhatsAppEnter() },
                    exitTransition = { defaultWhatsAppExit() },
                    popEnterTransition = { defaultWhatsAppPopEnter() },
                    popExitTransition = { defaultWhatsAppPopExit() }
                ) {
                    // Pass innerPadding ONLY to the main content of the tab screens
                    CategoryListScreen(
                        onCategoryClick = { categoryId ->
                            safeNavigation.navigateTo(DropNestDestination.CategoryDetail.createRoute(categoryId))
                        },
                        onAddCategoryClick = {
                            safeNavigation.navigateTo(DropNestDestination.CreateCategory.route)
                        },
//                        modifier = Modifier.padding(innerPadding)
                    )
                }

                // Center tab: Home
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
                            safeNavigation.navigateTo(DropNestDestination.CreateCategory.route)
                        },
                        onCategoryClick = { categoryId ->
                            safeNavigation.navigateTo(DropNestDestination.CategoryDetail.createRoute(categoryId))
                        },
                    )
                }

                // Right tab: Settings
                composable(
                    route = DropNestDestination.Settings.route,
                    enterTransition = { defaultWhatsAppEnter() },
                    exitTransition = { defaultWhatsAppExit() },
                    popEnterTransition = { defaultWhatsAppPopEnter() },
                    popExitTransition = { defaultWhatsAppPopExit() }
                ) {
                    SettingsScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                // ==== Non-tab destinations ====
                composable(
                    route = DropNestDestination.CreateCategory.route,
                    enterTransition = { defaultWhatsAppEnter() },
                    exitTransition = { defaultWhatsAppExit() },
                    popEnterTransition = { defaultWhatsAppPopEnter() },
                    popExitTransition = { defaultWhatsAppPopExit() }
                ) {
                    CreateCategoryScreen(
                        onBackClick = {
                            safeNavigation.popBackStack()
                        },
                        onCategorySaved = {
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
                        onBackClick = { safeNavigation.popBackStack() },
                        onCategorySaved = { safeNavigation.popBackStack() }
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
                    CategoryDetailScreen(
                        categoryId = categoryId,
                        onBackClick = { safeNavigation.popBackStack() }
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
                        onDeleteDrops = { dropsToDelete -> dropsToDelete.forEach { viewModel.deleteDropById(it) } }
                    )
                }
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