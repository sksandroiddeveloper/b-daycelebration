package com.birthday.celebrate.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.birthday.celebrate.data.BirthdayViewModel
import com.birthday.celebrate.ui.screens.EditScreen
import com.birthday.celebrate.ui.screens.SlideshowScreen

sealed class Screen(val route: String) {
    object Slideshow : Screen("slideshow")
    object Edit      : Screen("edit")
}

@Composable
fun BirthdayNavGraph(viewModel: BirthdayViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Slideshow.route) {
        composable(Screen.Slideshow.route) {
            SlideshowScreen(
                viewModel = viewModel,
                onEditClick = { navController.navigate(Screen.Edit.route) }
            )
        }
        composable(Screen.Edit.route) {
            EditScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
