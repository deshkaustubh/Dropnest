package tech.kaustubhdeshpande.dropnest.presentation.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class DropNestDestination(
    val route: String,
    val arguments: List<NamedNavArgument> = emptyList()
) {
    // Welcome/Onboarding
    object Welcome : DropNestDestination("welcome")

    // Main vault screen
    object Vault : DropNestDestination("vault")

    object Home : DropNestDestination("home")

    // Category screens
    object Categories : DropNestDestination("categories")
    object CreateCategory : DropNestDestination("create_category")

    // Edit Category with ID parameter
    object EditCategory : DropNestDestination(
        route = "edit_category/{categoryId}",
        arguments = listOf(
            navArgument("categoryId") {
                type = NavType.StringType
            }
        )
    ) {
        fun createRoute(categoryId: String) = "edit_category/$categoryId"
    }

    // Drop screens
    object CreateDrop : DropNestDestination("create_drop")

    // Drop detail with ID parameter
    object DropDetail : DropNestDestination(
        route = "drop_detail/{dropId}",
        arguments = listOf(
            navArgument("dropId") {
                type = NavType.StringType
            }
        )
    ) {
        fun createRoute(dropId: String) = "drop_detail/$dropId"
    }

    // Settings screen
    object Settings : DropNestDestination("settings")
}