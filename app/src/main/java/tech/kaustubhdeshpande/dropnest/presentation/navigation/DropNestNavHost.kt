package tech.kaustubhdeshpande.dropnest.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
                onGetStarted = {
                    navController.navigate(DropNestDestination.Vault.route) {
                        popUpTo(DropNestDestination.Welcome.route) { inclusive = true }
                    }
                }
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

        // Other routes will be implemented as we build those screens

        // For now, we've included route definitions that will be used later
    }
}