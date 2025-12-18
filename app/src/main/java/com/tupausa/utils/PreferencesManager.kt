package com.tupausa.utils

import android.content.Context
import android.content.SharedPreferences
import com.tupausa.model.Usuario

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.PREFS_NAME,
        Context.MODE_PRIVATE
    )

    // Guardar sesion
    fun saveUserSession(usuario: Usuario) {
        prefs.edit().apply {
            putInt(Constants.PREF_USER_ID, usuario.idUsuario)
            putString(Constants.PREF_USER_NAME, usuario.nombre)
            putString(Constants.PREF_USER_EMAIL, usuario.correoElectronico)
            putInt(Constants.PREF_USER_TYPE, usuario.idTipoUsuario)
            putBoolean(Constants.PREF_IS_LOGGED_IN, true)
            apply()
        }
    }

    // Logout
    fun clearUserSession() {
        prefs.edit().clear().apply()
    }

    // Obtener datos y sesion
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(Constants.PREF_IS_LOGGED_IN, false)
    }

    fun getUserId(): Int {
        return prefs.getInt(Constants.PREF_USER_ID, -1)
    }

    fun getUserName(): String {
        return prefs.getString(Constants.PREF_USER_NAME, "") ?: ""
    }

    fun getUserEmail(): String {
        return prefs.getString(Constants.PREF_USER_EMAIL, "") ?: ""
    }

    fun getUserType(): Int {
        return prefs.getInt(Constants.PREF_USER_TYPE, Constants.USER_TYPE_REGULAR)
    }

    fun isAdmin(): Boolean {
        return getUserType() == Constants.USER_TYPE_ADMIN
    }
}