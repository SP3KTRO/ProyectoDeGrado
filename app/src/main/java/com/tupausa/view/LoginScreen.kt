package com.tupausa.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tupausa.viewModel.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var correoElectronico by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState("")
    val loginSuccess by viewModel.loginSuccess.observeAsState(false)

    // Estados para las alertas de validación
    var correoError by remember { mutableStateOf("") }
    var contrasenaError by remember { mutableStateOf("") }

    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            onLoginSuccess()
        }
    }

    // Función para validar el correo electrónico
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.(com|co)\$".toRegex()
        return email.matches(emailRegex)
    }

    // Función para validar la contraseña
    fun isValidPassword(password: String): Boolean {
        val passwordRegex = "^[a-zA-Z0-9]{8,}\$".toRegex()
        return password.matches(passwordRegex)
    }

    // Función para filtrar caracteres no permitidos en el correo electrónico
    fun filterEmail(input: String): String {
        return input.replace("[^a-zA-Z0-9@._-]".toRegex(), "")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Iniciar Sesión", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de correo electrónico
        OutlinedTextField(
            value = correoElectronico,
            onValueChange = { newValue ->
                correoElectronico = filterEmail(newValue) // Filtrar caracteres no permitidos
                correoError = if (!isValidEmail(correoElectronico)) {
                    "El correo electrónico debe tener un formato válido (ejemplo@dominio.com o ejemplo@dominio.co)."
                } else {
                    ""
                }
            },
            label = { Text("Correo Electrónico") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        // Mostrar alerta de correo electrónico
        if (correoError.isNotEmpty()) {
            Text(
                text = correoError,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de contraseña
        OutlinedTextField(
            value = contrasena,
            onValueChange = { newValue ->
                contrasena = newValue
                contrasenaError = if (!isValidPassword(contrasena)) {
                    "La contraseña debe tener al menos 8 caracteres y no puede contener caracteres especiales."
                } else {
                    ""
                }
            },
            label = { Text("Contraseña") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        // Mostrar alerta de contraseña
        if (contrasenaError.isNotEmpty()) {
            Text(
                text = contrasenaError,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de inicio de sesión
        Button(
            onClick = {
                // Validar el correo electrónico
                if (!isValidEmail(correoElectronico)) {
                    viewModel.clearError()
                    correoError = "El correo electrónico debe tener un formato válido (ejemplo@dominio.com o ejemplo@dominio.co)."
                    return@Button
                }

                // Validar la contraseña
                if (!isValidPassword(contrasena)) {
                    viewModel.clearError()
                    contrasenaError = "La contraseña debe tener al menos 8 caracteres y no puede contener caracteres especiales."
                    return@Button
                }

                // Llamar al método login si las validaciones son exitosas
                viewModel.login(correoElectronico, contrasena)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Iniciar Sesión")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botón para navegar al registro
        TextButton(onClick = onNavigateToRegister) {
            Text("¿No tienes una cuenta? Regístrate")
        }

        // Mostrar error general
        if (error.isNotEmpty()) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}