package com.tupausa.viewModel.appNavigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tupausa.utils.Constants
import com.tupausa.view.*
import com.tupausa.view.admin.*
import com.tupausa.view.user.*
import com.tupausa.viewModel.EjercicioViewModel
import com.tupausa.viewModel.LoginViewModel
import com.tupausa.viewModel.UsuarioViewModel

@Composable
fun AppNavigation(
    tupausaViewModel: UsuarioViewModel,
    loginViewModel: LoginViewModel = viewModel(),
    ejercicioViewModel: EjercicioViewModel = viewModel()
) {
    val navController = rememberNavController()

    // Verificar si hay sesión activa
    LaunchedEffect(Unit) {
        val usuario = loginViewModel.checkSession()
        if (usuario != null) {
            // Ya hay sesión, navegar según tipo de usuario
            val destination = if (usuario.idTipoUsuario == Constants.USER_TYPE_ADMIN) {
                AppRoutes.ADMIN_DASHBOARD
            } else {
                AppRoutes.USER_DASHBOARD
            }
            navController.navigate(destination) {
                popUpTo(AppRoutes.WELCOME) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppRoutes.WELCOME
    ) {
        // AUTENTICACIÓN
        composable(AppRoutes.WELCOME) {
            ScreenWelcome(
                onNavigateToLogin = { navController.navigate(AppRoutes.LOGIN) }
            )
        }

        composable(AppRoutes.LOGIN) {
            val loginSuccess by loginViewModel.loginSuccess.observeAsState()

            // Pasar el loginViewModel explícitamente
            LoginScreen(
                onNavigateToRegister = { navController.navigate(AppRoutes.REGISTER) },
                onLoginSuccess = { },
                loginViewModel = loginViewModel  // ← Agregar esto
            )

            // Observar login exitoso
            LaunchedEffect(loginSuccess) {
                loginSuccess?.let { usuario ->
                    // Navegar según tipo de usuario
                    val destination = if (usuario.idTipoUsuario == Constants.USER_TYPE_ADMIN) {
                        AppRoutes.ADMIN_DASHBOARD
                    } else {
                        AppRoutes.USER_DASHBOARD
                    }

                    navController.navigate(destination) {
                        popUpTo(AppRoutes.WELCOME) { inclusive = true }
                    }
                }
            }
        }

        composable(AppRoutes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate(AppRoutes.LOGIN) }
            )
        }

        // ==========================================
        // ADMIN
        // ==========================================

        composable(AppRoutes.ADMIN_DASHBOARD) {
            AdminDashboardScreen(
                onNavigateToUsersList = { navController.navigate(AppRoutes.ADMIN_USERS_LIST) },
                onNavigateToEjercicios = { navController.navigate(AppRoutes.ADMIN_EJERCICIOS) },
                onLogout = {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoutes.ADMIN_USERS_LIST) {
            val usuarios by tupausaViewModel.usuarios.observeAsState(emptyList())
            val isLoading by tupausaViewModel.isLoading.observeAsState(false)

            LaunchedEffect(Unit) {
                tupausaViewModel.fetchUsuariosFromApi()
            }

            AdminUsersListScreen(
                usuarios = usuarios,
                isLoading = isLoading,
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.ADMIN_EJERCICIOS) {
            val ejercicios by ejercicioViewModel.ejercicios.observeAsState(emptyList())
            val isLoading by ejercicioViewModel.isLoading.observeAsState(false)

            LaunchedEffect(Unit) {
                ejercicioViewModel.loadEjercicios()
            }

            AdminEjerciciosScreen(
                ejercicios = ejercicios,
                isLoading = isLoading,
                onBack = { navController.popBackStack() }
            )
        }

        // ==========================================
        // USUARIO
        // ==========================================

        composable(AppRoutes.USER_DASHBOARD) {
            UserDashboardScreen(
                onNavigateToEjercicios = { navController.navigate(AppRoutes.USER_EJERCICIOS) },
                onNavigateToAlarmas = { navController.navigate(AppRoutes.USER_ALARMAS) },
                onNavigateToHistorial = { navController.navigate(AppRoutes.USER_HISTORIAL) },
                onLogout = {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoutes.USER_EJERCICIOS) {
            val ejercicios by ejercicioViewModel.ejercicios.observeAsState(emptyList())
            val isLoading by ejercicioViewModel.isLoading.observeAsState(false)

            LaunchedEffect(Unit) {
                ejercicioViewModel.loadEjercicios()
            }

            UserEjerciciosListScreen(
                ejercicios = ejercicios,
                isLoading = isLoading,
                onEjercicioClick = { ejercicio ->
                    navController.navigate(AppRoutes.userEjercicioDetalle(ejercicio.idEjercicio))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppRoutes.USER_EJERCICIO_DETALLE,
            arguments = listOf(navArgument("ejercicioId") { type = NavType.IntType })
        ) { backStackEntry ->
            val ejercicioId = backStackEntry.arguments?.getInt("ejercicioId") ?: 0
            val ejercicio by ejercicioViewModel.ejercicioSeleccionado.observeAsState()

            LaunchedEffect(ejercicioId) {
                ejercicioViewModel.loadEjercicioById(ejercicioId)
            }

            ejercicio?.let {
                UserEjercicioDetalleScreen(
                    ejercicio = it,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(AppRoutes.USER_ALARMAS) {
            UserAlarmasScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.USER_HISTORIAL) {
            UserHistorialScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
