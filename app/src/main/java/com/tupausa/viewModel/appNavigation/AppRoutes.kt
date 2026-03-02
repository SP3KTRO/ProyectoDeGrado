package com.tupausa.viewModel.appNavigation

object AppRoutes {
    // Autenticación
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"

    // Admin
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val ADMIN_USERS_LIST = "admin_users_list"
    const val ADMIN_EJERCICIOS = "admin_ejercicios"
    const val ADMIN_EJERCICIO_DETALLE = "admin_ejercicio_detalle/{ejercicioId}"
    const val ADMIN_HISTORIAL = "admin_historial"


    // Usuario
    const val USER_DASHBOARD = "user_dashboard"
    const val ONBOARDING = "onboarding"
    const val USER_EJERCICIOS = "user_ejercicios"
    const val USER_EJERCICIO_DETALLE = "user_ejercicio_detalle/{ejercicioId}"
    const val USER_ALARMAS = "user_alarmas"
    const val USER_HISTORIAL = "user_historial"

    // Helper para rutas con parámetros
    fun userEjercicioDetalle(ejercicioId: Int) = "user_ejercicio_detalle/$ejercicioId"
    fun adminEjercicioDetalle(ejercicioId: Int) = "admin_ejercicio_detalle/$ejercicioId"
}