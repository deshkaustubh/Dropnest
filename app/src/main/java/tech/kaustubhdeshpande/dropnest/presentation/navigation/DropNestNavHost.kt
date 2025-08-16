package tech.kaustubhdeshpande.dropnest.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import tech.kaustubhdeshpande.dropnest.domain.model.Drop
import tech.kaustubhdeshpande.dropnest.ui.screen.category.CreateCategoryScreen
import tech.kaustubhdeshpande.dropnest.ui.screen.home.HomeScreen
import tech.kaustubhdeshpande.dropnest.ui.screen.home.HomeViewModelImpl
import tech.kaustubhdeshpande.dropnest.ui.screen.vault.VaultScreen
import tech.kaustubhdeshpande.dropnest.ui.screen.welcome.WelcomeScreen

@Composable
fun DropNestNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = DropNestDestination.Welcome.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Welcome screen
        composable(route = DropNestDestination.Welcome.route) {
            WelcomeScreen(
                onTimeout = {
                    navController.navigate(DropNestDestination.Home.route) {
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
                    navController.navigate(DropNestDestination.CreateDrop.route)
                },
                onDropClick = { dropId ->
                    navController.navigate(DropNestDestination.DropDetail.createRoute(dropId))
                },
                onSettingsClick = {
                    navController.navigate(DropNestDestination.Settings.route)
                }
            )
        }

        // Home Screen - new main screen of the app
        composable(route = DropNestDestination.Home.route) {
            val viewModel: HomeViewModelImpl = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onCreateCategoryClick = {
                    navController.navigate(DropNestDestination.CreateCategory.route)
                },
                onCategoryClick = { categoryId ->
                    navController.navigate(DropNestDestination.EditCategory.createRoute(categoryId))
                }
            )
        }

        // Create Category screen
        composable(route = DropNestDestination.CreateCategory.route) {
            CreateCategoryScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onCategorySaved = {
                    navController.popBackStack()
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
                    navController.popBackStack()
                },
                onCategorySaved = {
                    navController.popBackStack()
                }
            )
        }

        // Other routes will be implemented as we build those screens
    }
}