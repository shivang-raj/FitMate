package com.fitmate.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitmate.app.ui.screens.HealthScreen
import com.fitmate.app.ui.screens.HomeScreen
import com.fitmate.app.ui.screens.OnboardingScreen
import com.fitmate.app.ui.screens.ProfileScreen
import com.fitmate.app.ui.screens.RemindersScreen
import com.fitmate.app.ui.screens.WorkoutsScreen
import com.fitmate.app.viewmodel.FitnessViewModel

/** SEALED CLASS: the compiler knows every possible tab, so `when (tab)` needs no `else`. */
sealed class Tab(val label: String, val icon: ImageVector) {
    data object Home : Tab("Home", Icons.Filled.Home)
    data object Workouts : Tab("Workouts", Icons.Filled.PlayArrow)
    data object Health : Tab("Health", Icons.Filled.Favorite)
    data object Reminders : Tab("Reminders", Icons.Filled.Notifications)
    data object Profile : Tab("Profile", Icons.Filled.Person)
}

private val tabs = listOf(Tab.Home, Tab.Workouts, Tab.Health, Tab.Reminders, Tab.Profile)

@Composable
fun FitMateRoot(viewModel: FitnessViewModel = viewModel()) {
    val profile = viewModel.profile
    if (profile == null) {
        OnboardingScreen(onCreate = viewModel::saveProfile)
    } else {
        var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
        Scaffold(
            bottomBar = {
                NavigationBar {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = index == selectedIndex,
                            onClick = { selectedIndex = index },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (tabs[selectedIndex]) {
                    Tab.Home -> HomeScreen(
                        viewModel = viewModel,
                        profile = profile,
                        onOpenWorkouts = { selectedIndex = tabs.indexOf(Tab.Workouts) }
                    )
                    Tab.Workouts -> WorkoutsScreen(viewModel, profile)
                    Tab.Health -> HealthScreen(viewModel, profile)
                    Tab.Reminders -> RemindersScreen(viewModel)
                    Tab.Profile -> ProfileScreen(viewModel, profile)
                }
            }
        }
    }
}
