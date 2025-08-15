package tech.kaustubhdeshpande.dropnest.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun DropNestBottomNavigation(
    navController: NavController
) {
    val items = listOf(
        BottomNavItem(
            route = DropNestDestination.Vault.route,
            title = "Vault",
            icon = Icons.Filled.Home
        ),
        BottomNavItem(
            route = DropNestDestination.CreateDrop.route,
            title = "Add",
            icon = Icons.Filled.Add
        ),
        BottomNavItem(
            route = DropNestDestination.Categories.route,
            title = "Categories",
            icon = Icons.Filled.Category
        ),
        BottomNavItem(
            route = DropNestDestination.Settings.route,
            title = "Settings",
            icon = Icons.Filled.Settings
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination
                        launchSingleTop = true
                        // Restore state when re-selecting previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)