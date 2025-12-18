package com.tupausa.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.tupausa.ui.theme.ArenaOnPrimaryContainer
import com.tupausa.ui.theme.ArenaPrimary
import com.tupausa.viewModel.RegisterViewModel

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = viewModel(),
    onNavigateToLogin: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var correoElectronico by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }

    // Observar el estado del ViewModel
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState("")
    val registerSuccess by viewModel.registerSuccess.observeAsState(false)

    // Estados para las alertas de validación
    var nombreError by remember { mutableStateOf("") }
    var correoError by remember { mutableStateOf("") }
    var contrasenaError by remember { mutableStateOf("") }
    var confirmarContrasenaError by remember { mutableStateOf("") }

    // Manejar el botón de retroceso
    BackHandler {
        onNavigateToLogin() // Navegar a la pantalla de inicio de sesión
    }

    // Función para validar el nombre
    fun isValidNombre(nombre: String): Boolean {
        val nombreRegex = "^[a-zA-Z\\s]{5,}\$".toRegex()
        return nombre.matches(nombreRegex)
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

    // Función para filtrar caracteres no permitidos en el nombre
    fun filterNombre(input: String): String {
        return input.replace("[^a-zA-Z\\s]".toRegex(), "")
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
        Text(
            text = "Registro",
            style = MaterialTheme.typography.headlineMedium,
            color = ArenaOnPrimaryContainer
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de nombre
        OutlinedTextField(
            value = nombre,
            onValueChange = { newValue ->
                nombre = filterNombre(newValue) // Filtrar caracteres no permitidos
                nombreError = if (!isValidNombre(nombre)) {
                    "El nombre debe tener al menos 5 caracteres y no puede contener caracteres especiales."
                } else {
                    ""
                }
            },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                // Bordes: Bronce (para delimitar bien)
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                // Texto: Café Oscuro (para leer bien)
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                // Label (Etiqueta): Café
                focusedLabelColor = ArenaOnPrimaryContainer,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = MaterialTheme.shapes.medium
        )

        // Mostrar alerta de nombre
        if (nombreError.isNotEmpty()) {
            Text(
                text = nombreError,
                color = MaterialTheme.colorScheme.onError,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

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
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                // Bordes: Bronce (para delimitar bien)
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                // Texto: Café Oscuro (para leer bien)
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                // Label (Etiqueta): Café
                focusedLabelColor = ArenaOnPrimaryContainer,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = MaterialTheme.shapes.medium
        )

        // Mostrar alerta de correo electrónico
        if (correoError.isNotEmpty()) {
            Text(
                text = correoError,
                color = MaterialTheme.colorScheme.onError,
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
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                // Bordes: Bronce (para delimitar bien)
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                // Texto: Café Oscuro (para leer bien)
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                // Label (Etiqueta): Café
                focusedLabelColor = ArenaOnPrimaryContainer,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = MaterialTheme.shapes.medium
        )

        // Mostrar alerta de contraseña
        if (contrasenaError.isNotEmpty()) {
            Text(
                text = contrasenaError,
                color = MaterialTheme.colorScheme.onError,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de confirmar contraseña
        OutlinedTextField(
            value = confirmarContrasena,
            onValueChange = { newValue ->
                confirmarContrasena = newValue
                confirmarContrasenaError = if (confirmarContrasena != contrasena) {
                    "Las contraseñas no coinciden."
                } else {
                    ""
                }
            },
            label = { Text("Confirmar Contraseña") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                // Bordes: Bronce (para delimitar bien)
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                // Texto: Café Oscuro (para leer bien)
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                // Label (Etiqueta): Café
                focusedLabelColor = ArenaOnPrimaryContainer,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = MaterialTheme.shapes.medium
        )

        // Mostrar alerta de confirmar contraseña
        if (confirmarContrasenaError.isNotEmpty()) {
            Text(
                text = confirmarContrasenaError,
                color = MaterialTheme.colorScheme.onError,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón de registro
        Button(
            onClick = {
                // Validar el nombre
                if (!isValidNombre(nombre)) {
                    nombreError = "El nombre debe tener al menos 5 caracteres y no puede contener caracteres especiales."
                    return@Button
                }

                // Validar el correo electrónico
                if (!isValidEmail(correoElectronico)) {
                    correoError = "El correo electrónico debe tener un formato válido (ejemplo@dominio.com)."
                    return@Button
                }

                // Validar la contraseña
                if (!isValidPassword(contrasena)) {
                    contrasenaError = "La contraseña debe tener al menos 8 carácteres y no puede contener carácteres especiales."
                    return@Button
                }

                // Validar que las contraseñas coincidan
                if (confirmarContrasena != contrasena) {
                    confirmarContrasenaError = "Las contraseñas no coinciden."
                    return@Button
                }

                // Llamar al metodo register si las validaciones son exitosas
                viewModel.register(nombre, correoElectronico, contrasena, confirmarContrasena)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Registrarse")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botón para navegar al inicio de sesión
        TextButton(
            onClick = onNavigateToLogin,
            colors = ButtonDefaults.textButtonColors(
                contentColor = ArenaPrimary
            )
            ) {
            Text("¿Ya tienes una cuenta? Inicia sesión")
        }

        // Mostrar error general
        if (error.isNotEmpty()) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Mostrar alerta de registro exitoso
        if (registerSuccess) {
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                title = { Text("Registro Exitoso") },
                text = { Text("El usuario se ha registrado correctamente.") },
                confirmButton = {
                    Button(onClick = {
                        viewModel.clearError()
                        onNavigateToLogin()
                    }) {
                        Text("Aceptar")
                    }
                }
            )
        }
    }
}

