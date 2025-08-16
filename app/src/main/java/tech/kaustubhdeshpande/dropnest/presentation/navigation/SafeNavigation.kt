package tech.kaustubhdeshpande.dropnest.presentation.navigation

import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A utility class that provides safe navigation operations
 * by preventing multiple navigation actions from occurring simultaneously.
 */
class SafeNavigation(private val navController: NavController) {

    private val tag = "SafeNavigation"
    private val isNavigating = AtomicBoolean(false)
    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState.Idle)
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()

    // Configurable debounce time
    var debounceTime = 300L // milliseconds
    private var lastNavigationTime = 0L

    /**
     * Safely navigate to a destination, preventing multiple rapid navigation calls
     */
    fun navigateTo(route: String, builder: (NavOptionsBuilder.() -> Unit)? = null) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastNavigationTime < debounceTime || !isNavigating.compareAndSet(false, true)) {
            Log.d(tag, "Navigation blocked to $route: too soon after previous navigation")
            return
        }

        try {
            _navigationState.value = NavigationState.Navigating(route)
            Log.d(tag, "Navigating to $route")
            val options = builder?.let { navOptions(it) }
            navController.navigate(route, options)
            lastNavigationTime = System.currentTimeMillis()
        } catch (e: Exception) {
            Log.e(tag, "Error navigating to $route", e)
            _navigationState.value = NavigationState.Error(e)
        } finally {
            isNavigating.set(false)
            _navigationState.value = NavigationState.Idle
        }
    }

    /**
     * Safely pop the back stack, preventing multiple rapid pops
     */
    fun popBackStack(): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastNavigationTime < debounceTime || !isNavigating.compareAndSet(false, true)) {
            Log.d(tag, "Back navigation blocked: too soon after previous navigation")
            return false
        }

        return try {
            _navigationState.value = NavigationState.Navigating("back")
            Log.d(tag, "Popping back stack")
            val result = navController.popBackStack()
            lastNavigationTime = System.currentTimeMillis()
            result
        } catch (e: Exception) {
            Log.e(tag, "Error popping back stack", e)
            _navigationState.value = NavigationState.Error(e)
            false
        } finally {
            isNavigating.set(false)
            _navigationState.value = NavigationState.Idle
        }
    }

    /**
     * Safely pop back to a specific destination
     */
    fun popBackTo(route: String, inclusive: Boolean = false): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastNavigationTime < debounceTime || !isNavigating.compareAndSet(false, true)) {
            Log.d(tag, "Pop back navigation blocked: too soon after previous navigation")
            return false
        }

        return try {
            _navigationState.value = NavigationState.Navigating("back to $route")
            Log.d(tag, "Popping back to $route (inclusive: $inclusive)")
            val result = navController.popBackStack(route, inclusive)
            lastNavigationTime = System.currentTimeMillis()
            result
        } catch (e: Exception) {
            Log.e(tag, "Error popping back to $route", e)
            _navigationState.value = NavigationState.Error(e)
            false
        } finally {
            isNavigating.set(false)
            _navigationState.value = NavigationState.Idle
        }
    }
}

/**
 * Represents the current state of navigation
 */
sealed class NavigationState {
    object Idle : NavigationState()
    data class Navigating(val destination: String) : NavigationState()
    data class Error(val error: Exception) : NavigationState()
}