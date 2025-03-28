package com.tupausa.viewModel.appNavigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tupausa.view.LoginScreen
import com.tupausa.view.MainScreen
import com.tupausa.view.ScreenWelcome
import com.tupausa.view.UsuarioList
import com.tupausa.view.UsuarioListContent
import com.tupausa.viewModel.RegisterScreen
import com.tupausa.viewModel.TuPausaViewModel


@Composable
fun AppNavigation(viewModel: TuPausaViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.WELCOME // La pantalla de Login será la primera
    ) {
        // Pantalla de Bienvenida
        composable(AppRoutes.WELCOME) {
            ScreenWelcome(
                onNavigateToLogin = { navController.navigate(AppRoutes.LOGIN) }
            )
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

        // Pantalla Principal
        composable(AppRoutes.MAIN) {
            MainScreen (
                onNavigateToUsuarioList = { navController.navigate(AppRoutes.USUARIOS_LIST) }
            )
        }

        // Pantalla de Lista de Usuarios
        composable(AppRoutes.USUARIOS_LIST) {
            val usuarios by viewModel.usuarios.observeAsState(emptyList())
            val isLoading by viewModel.isLoading.observeAsState(false)
            val error by viewModel.error.observeAsState()

            // Cargar usuarios desde la API al entrar en esta pantalla
            LaunchedEffect(Unit) {
                viewModel.fetchUsuariosFromApi()
            }

            UsuarioListContent(
                usuarios = usuarios
            )
        }
    }
}
