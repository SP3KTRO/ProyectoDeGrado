package com.tupausa.viewModel.appNavigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tupausa.view.LoginScreen
import com.tupausa.view.UsuarioList
import com.tupausa.view.screenWelcome
import com.tupausa.viewModel.RegisterScreen
import com.tupausa.viewModel.TuPausaViewModel


@Composable
fun AppNavigation(viewModel: TuPausaViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.WELCOME
    ) {
        // Pantalla de Bienvenida
        composable(AppRoutes.WELCOME) {
            screenWelcome(navController = navController)
        }

        // Pantalla de Login
        composable(AppRoutes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(AppRoutes.REGISTER) },
                onLoginSuccess = { navController.navigate(AppRoutes.USUARIOS_LIST) }
            )
        }

        // Pantalla de Registro
        composable(AppRoutes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate(AppRoutes.LOGIN) }
            )
        }

        // Pantalla de Lista de Usuarios
        composable(AppRoutes.USUARIOS_LIST) {
            UsuarioList(viewModel = viewModel)
        }
    }
}
