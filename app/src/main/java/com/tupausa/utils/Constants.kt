package com.tupausa.utils

object Constants {

    // API REST
    const val BASE_URL = "https://5az7zcnh4j.execute-api.sa-east-1.amazonaws.com/Inicio/"

    // SHARED PREFERENCES
    const val PREFS_NAME = "TuPausaPrefs"
    const val PREF_USER_ID = "user_id"
    const val PREF_USER_NAME = "user_name"
    const val PREF_USER_EMAIL = "user_email"
    const val PREF_IS_LOGGED_IN = "is_logged_in"
    const val PREF_USER_TYPE = "user_type"

    // BASE DE DATOS
    const val DATABASE_NAME = "tupausa_database.db"
    const val DATABASE_VERSION = 2

    // TIPOS DE USUARIO
    const val USER_TYPE_REGULAR = 1
    const val USER_TYPE_ADMIN = 2

    // VALIDACIONES
    const val MIN_PASSWORD_LENGTH = 8
    const val MIN_NAME_LENGTH = 5
    const val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    const val PASSWORD_REGEX = "^[a-zA-Z0-9]{8,}$"
    const val NAME_REGEX = "^[a-zA-Z\\s]{5,}$"

    // RUTAS DE NAVEGACIÓN
    object Routes {
        const val WELCOME= "welcome"
        const val LOGIN = "login"
        const val REGISTER = "register"

        // Admin
        const val ADMIN_DASHBOARD = "admin_dashboard"
        const val ADMIN_USERS_LIST = "admin_users_list"
        const val ADMIN_EJERCICIOS = "admin_ejercicios"

        // Usuario
        const val USER_DASHBOARD = "user_dashboard"
        const val USER_EJERCICIOS = "user_ejercicios"
        const val USER_EJERCICIO_DETALLE = "user_ejercicio_detalle/{ejercicioId}"
        const val USER_ALARMAS = "user_alarmas"
        const val USER_HISTORIAL = "user_historial"
    }

    // MENSAJES
    const val MSG_NETWORK_ERROR = "Error de conexión. Verifica tu internet."
    const val MSG_SERVER_ERROR = "Error del servidor. Intenta más tarde."
    const val MSG_LOADING = "Cargando..."
}