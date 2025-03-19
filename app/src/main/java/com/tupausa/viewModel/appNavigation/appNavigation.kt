package com.tupausa.viewModel.appNavigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tupausa.view.UsuarioList
import com.tupausa.view.screenWelcome
import com.tupausa.viewModel.TuPausaViewModel

@Composable
fun AppNavigation(viewModel: TuPausaViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.WELCOME
    ) {
        composable(AppRoutes.WELCOME) {
            screenWelcome(navController = navController)
        }

        composable(AppRoutes.USUARIOS_LIST) {
            UsuarioList(viewModel = viewModel)
        }
    }
}
