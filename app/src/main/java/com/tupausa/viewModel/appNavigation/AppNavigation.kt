package com.tupausa.viewModel.appNavigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tupausa.TuPausaApplication
import com.tupausa.model.Usuario
import com.tupausa.utils.Constants
import com.tupausa.view.*
import com.tupausa.view.admin.*
import com.tupausa.view.user.*
import com.tupausa.viewModel.AdminHistorialViewModel
import com.tupausa.viewModel.AdminHistorialViewModelFactory
import com.tupausa.viewModel.EjercicioViewModel
import com.tupausa.viewModel.LoginViewModel
import com.tupausa.viewModel.UsuarioViewModel

@Composable
fun AppNavigation(
    usuarioViewModel: UsuarioViewModel = viewModel(),
    loginViewModel: LoginViewModel = viewModel(),
    ejercicioViewModel: EjercicioViewModel = viewModel()
) {
    val navController = rememberNavController()

    // Verificar si hay sesión activa
    LaunchedEffect(Unit) {
        val usuario = loginViewModel.checkSession()
        if (usuario != null) {
            // Navegar según tipo de usuario
            val destination = if (usuario.idTipoUsuario == Constants.USER_TYPE_ADMIN) {
                AppRoutes.ADMIN_DASHBOARD
            } else {
                if (usuario.onboardingCompletado) {
                    AppRoutes.USER_DASHBOARD
                } else {
                    AppRoutes.ONBOARDING
                }
            }
            navController.navigate(destination) {
                popUpTo(0)
                { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppRoutes.WELCOME
    ) {
        // Bienvenida

        composable(AppRoutes.WELCOME) {
            ScreenWelcome(
                onNavigateToLogin = { navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(AppRoutes.WELCOME) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // Autenticación

        composable(AppRoutes.LOGIN) {
            val loginSuccess by loginViewModel.loginSuccess.observeAsState()

            // Pasar el loginViewModel a LoginScreen
            LoginScreen(
                onNavigateToRegister = { navController.navigate(AppRoutes.REGISTER) },
                onLoginSuccess = { },
                loginViewModel = loginViewModel
            )
            LaunchedEffect(loginSuccess) {
                loginSuccess?.let { usuario ->
                    // Navegar según tipo de usuario
                    val destination = if (usuario.idTipoUsuario == Constants.USER_TYPE_ADMIN) {
                        AppRoutes.ADMIN_DASHBOARD
                    } else {
                        if (usuario.onboardingCompletado) {
                            AppRoutes.USER_DASHBOARD
                        } else {
                            AppRoutes.ONBOARDING
                        }
                    }

                    navController.navigate(destination) {
                        popUpTo(0) { inclusive = true }
                        }
                    }
            }
        }

        composable(AppRoutes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // Admin

        composable(AppRoutes.ADMIN_DASHBOARD) {
            AdminDashboardScreen(
                usuarioViewModel = usuarioViewModel,
                onNavigateToUsersList = { navController.navigate(AppRoutes.ADMIN_USERS_LIST) },
                onNavigateToEjercicios = { navController.navigate(AppRoutes.ADMIN_EJERCICIOS) },
                onNavigateToHistorial = { navController.navigate(AppRoutes.ADMIN_HISTORIAL) },
                onLogout = {
                    loginViewModel.logout()
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoutes.ADMIN_USERS_LIST) {
            val usuarios by usuarioViewModel.usuarios.observeAsState(emptyList())
            val isLoading by usuarioViewModel.isLoading.observeAsState(false)
            val error by usuarioViewModel.error.observeAsState()
            val operationSuccess by usuarioViewModel.operationSuccess.observeAsState()

            var showEditDialog by remember { mutableStateOf(false) }
            var usuarioToEdit by remember { mutableStateOf<Usuario?>(null) }

            LaunchedEffect(Unit) {
                usuarioViewModel.fetchUsuariosFromApi()
            }

            LaunchedEffect(operationSuccess) {
                operationSuccess?.let {
                    if (it.isNotEmpty()) {
                        usuarioViewModel.clearOperationSuccess()
                    }
                }
            }

            // Mostrar error
            LaunchedEffect(error) {
                error?.let {
                    if (it.isNotEmpty()) {
                        usuarioViewModel.clearError()
                    }
                }
            }

            AdminUsersListScreen(
                usuarios = usuarios,
                isLoading = isLoading,
                onBack = { navController.popBackStack() },
                onDeleteUsuario = { usuario ->
                    usuarioViewModel.deleteUsuario(usuario.idUsuario)
                }
            )
        }

        composable(AppRoutes.ADMIN_EJERCICIOS) {
            val ejercicios by ejercicioViewModel.ejercicios.observeAsState(emptyList())
            val isLoading by ejercicioViewModel.isLoading.observeAsState(false)
            LaunchedEffect(Unit) { ejercicioViewModel.loadEjercicios() }

            AdminEjerciciosScreen(
                ejercicios = ejercicios,
                isLoading = isLoading,
                onBack = { navController.popBackStack() },
                onEjercicioClick = { ejercicio ->
                    navController.navigate(AppRoutes.adminEjercicioDetalle(ejercicio.idEjercicio))
                }
            )
        }

        composable(
            route = AppRoutes.ADMIN_EJERCICIO_DETALLE,
            arguments = listOf(navArgument("ejercicioId") { type = NavType.IntType })
        ) { backStackEntry ->
            val ejercicioId = backStackEntry.arguments?.getInt("ejercicioId") ?: 0
            val ejercicio by ejercicioViewModel.ejercicioSeleccionado.observeAsState()

            LaunchedEffect(ejercicioId) {
                ejercicioViewModel.loadEjercicioById(ejercicioId)
            }

            ejercicio?.let {
                AdminEjercicioDetalleScreen(
                    ejercicio = it,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(AppRoutes.ADMIN_HISTORIAL) {
            val adminVM: AdminHistorialViewModel = viewModel(
                factory = AdminHistorialViewModelFactory((
                        LocalContext.current.applicationContext as TuPausaApplication).historialRepository
                )
            )
            AdminHistorialScreen(
                onBack = { navController.popBackStack() },
                viewModel = adminVM
            )
        }

        // Usuario

        composable(AppRoutes.USER_DASHBOARD) {
            UserDashboardScreen(
                usuarioViewModel = usuarioViewModel,
                onNavigateToEjercicios = { navController.navigate(AppRoutes.USER_EJERCICIOS) },
                onNavigateToAlarmas = { navController.navigate(AppRoutes.USER_ALARMAS) },
                onNavigateToHistorial = { navController.navigate(AppRoutes.USER_HISTORIAL) },
                onLogout = {
                    loginViewModel.logout()
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onRehacerOnboarding = {
                    navController.navigate(AppRoutes.ONBOARDING)
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

        composable(AppRoutes.ONBOARDING){
            val usuarioActual = loginViewModel.loginSuccess.value ?: loginViewModel.checkSession()
            if (usuarioActual != null) {
                OnboardingScreen(
                    idUsuario = usuarioActual.idUsuario,
                    usuarioViewModel = usuarioViewModel,
                    onOnboardingComplete = {
                        navController.navigate(AppRoutes.USER_DASHBOARD) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
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
