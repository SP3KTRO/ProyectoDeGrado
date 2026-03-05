package com.tupausa.utils

import android.content.Context
import android.content.SharedPreferences
import com.tupausa.model.Usuario
import java.util.concurrent.TimeUnit

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.PREFS_NAME,
        Context.MODE_PRIVATE
    )

    // Guardar sesión
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

    // Guardar que el onboarding se completó y las limitaciones
    fun saveOnboardingPreferences(limitaciones: String) {
        prefs.edit().apply {
            putBoolean(Constants.PREF_ONBOARDING_COMPLETED, true)
            putString(Constants.PREF_LIMITACIONES, limitaciones)
            apply()
        }
    }

    // Comprobar si ya completó el onboarding
    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(Constants.PREF_ONBOARDING_COMPLETED, false)
    }

    // Obtener las limitaciones como String
    fun getLimitaciones(): String {
        return prefs.getString(Constants.PREF_LIMITACIONES, "") ?: ""
    }

    // Guardar la fecha exacta en la que terminó el Onboarding
    fun setUltimaActualizacionPreferencias(timeInMillis: Long) {
        prefs.edit().putLong("ultima_actualizacion_prefs", timeInMillis).apply()
    }

    // Calcular cuántos días faltan para poder volver a hacer el OnBoarding - 20 días
    fun diasRestantesParaActualizarPreferencias(): Int {
        val ultimaVez = prefs.getLong("ultima_actualizacion_prefs", 0L)

        // Si es 0, significa que nunca lo ha hecho o es su primera vez
        if (ultimaVez == 0L) return 0

        val diffMilisegundos = System.currentTimeMillis() - ultimaVez
        val diasPasados = TimeUnit.MILLISECONDS.toDays(diffMilisegundos).toInt()

        val diasRestantes = 20 - diasPasados
        return if (diasRestantes > 0) diasRestantes else 0
    }

    // Logout
    fun clearUserSession() {
        prefs.edit().clear().apply()
    }

    // Obtener datos y sesión
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
    // Guardar tono
    fun setSelectedTone(toneResourceId: Int) {
        prefs.edit().putInt(Constants.PREF_SELECTED_TONE, toneResourceId).apply()
    }
    // Obtener tono
    fun getSelectedTone(defaultToneId: Int): Int {
        return prefs.getInt(Constants.PREF_SELECTED_TONE, defaultToneId)
    }
}