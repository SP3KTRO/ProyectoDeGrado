package com.tupausa.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tupausa.ui.theme.OnPrimary
import com.tupausa.ui.theme.OnPrimaryContainer
import com.tupausa.ui.theme.OnSecondary
import com.tupausa.ui.theme.OnSurface
import com.tupausa.ui.theme.Outline
import com.tupausa.ui.theme.Primary
import com.tupausa.ui.theme.PrimaryContainer
import com.tupausa.ui.theme.Secondary
import com.tupausa.utils.Constants
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

    val snackbarHostState = remember { SnackbarHostState() }

    // Estados para las alertas de validación
    var nombreError by remember { mutableStateOf("") }
    var correoError by remember { mutableStateOf("") }
    var contrasenaError by remember { mutableStateOf("") }
    var confirmarContrasenaError by remember { mutableStateOf("") }

    val dominiosPermitidos = setOf(
        "gmail.com",
        "hotmail.com",
        "outlook.com",
        "yahoo.com",
        "yahoo.es",
        "live.com",
        "icloud.com",
        "msn.com",
        "udistrital.edu.co"
    )

    // Manejar el botón de retroceso
    BackHandler {
        onNavigateToLogin()
    }

    // Funciones para validación de campos
    fun isValidNombre(nombre: String): Boolean {
        val nombreRegex = "^[a-zA-Z\\s]{5,}\$".toRegex()
        return nombre.matches(nombreRegex)
    }

    fun isValidEmail(email: String): Boolean {
        // Verificamos que tenga el formato básico
        if (!email.matches(Constants.EMAIL_REGEX.toRegex())) {
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false
        }
        // Extraemos el dominio
        val partes = email.split("@")
        if (partes.size != 2) return false

        val dominio = partes[1].lowercase()

        val dominiosMalEscritos = setOf(
            "gamil.com", "gmail.con", "gmai.com", "gmal.com", "gmail.coom",
            "hotmial.com", "hotmai.com", "homail.com", "hotmail.con",
            "outlok.com", "outlook.con", "outloo.com",
            "yaho.com", "yahoo.con", "yahoo.com.coo"
        )
        if (dominiosMalEscritos.contains(dominio)) return false

        val correosBasura = setOf(
            "yopmail.com", "10minutemail.com", "mailinator.com",
            "guerrillamail.com", "tempmail.com", "temp-mail.org", "throwawaymail.com"
        )
        if (correosBasura.contains(dominio)) return false

        val esDominioInstitucional = dominio.endsWith(".edu.co") ||
                dominio.endsWith(".edu") ||
                dominio.endsWith(".gov.co") ||
                dominio.endsWith(".org.co") ||
                dominio.endsWith(".mil.co")

        // Verificamos si está en nuestra lista blanca o es institucional
        return dominiosPermitidos.contains(dominio) || esDominioInstitucional
    }

    fun isValidPassword(password: String): Boolean {
        val passwordRegex = "^[a-zA-Z0-9]{8,}\$".toRegex()
        return password.matches(passwordRegex)
    }

    fun filterNombre(input: String): String {
        return input.replace("[^a-zA-Z\\s]".toRegex(), "")
    }

    fun filterEmail(input: String): String {
        return input.replace("[^a-zA-Z0-9@._-]".toRegex(), "")
    }

    // Errores del server
    LaunchedEffect(error) {
        if (error.isNotEmpty()) {
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Regístrate",
                style = MaterialTheme.typography.headlineMedium,
                color = OnSurface
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
                    focusedContainerColor = Secondary,
                    unfocusedContainerColor = Secondary,
                    disabledContainerColor = Secondary,
                    // Bordes
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = OnSecondary,
                    // Texto
                    focusedTextColor = OnPrimary,
                    unfocusedTextColor = Outline,
                    // Label
                    focusedLabelColor = OnSurface,
                    unfocusedLabelColor = PrimaryContainer
                ),
                shape = MaterialTheme.shapes.medium
            )

            // Mostrar alerta de nombre
            if (nombreError.isNotEmpty()) {
                Text(
                    text = nombreError,
                    color = OnSurface,
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
                        "Usa un proveedor válido (ej. gmail.com, outlook.com) o un correo institucional (.edu.co)."
                    } else {
                        ""
                    }
                },
                label = { Text("Correo Electrónico") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Secondary,
                    unfocusedContainerColor = Secondary,
                    disabledContainerColor = Secondary,
                    // Bordes
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = OnSecondary,
                    // Texto
                    focusedTextColor = OnPrimary,
                    unfocusedTextColor = Outline,
                    // Label
                    focusedLabelColor = OnSurface,
                    unfocusedLabelColor = PrimaryContainer
                ),
                shape = MaterialTheme.shapes.medium
            )

            // Mostrar alerta de correo electrónico
            if (correoError.isNotEmpty()) {
                Text(
                    text = correoError,
                    color = OnSurface,
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
                    focusedContainerColor = Secondary,
                    unfocusedContainerColor = Secondary,
                    disabledContainerColor = Secondary,
                    // Bordes
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = OnSecondary,
                    // Texto
                    focusedTextColor = OnPrimary,
                    unfocusedTextColor = Outline,
                    // Label
                    focusedLabelColor = OnSurface,
                    unfocusedLabelColor = PrimaryContainer
                ),
                shape = MaterialTheme.shapes.medium
            )

            // Mostrar alerta de contraseña
            if (contrasenaError.isNotEmpty()) {
                Text(
                    text = contrasenaError,
                    color = OnSurface,
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
                    focusedContainerColor = Secondary,
                    unfocusedContainerColor = Secondary,
                    disabledContainerColor = Secondary,
                    // Bordes
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = OnSecondary,
                    // Texto
                    focusedTextColor = OnPrimary,
                    unfocusedTextColor = Outline,
                    // Label
                    focusedLabelColor = OnSurface,
                    unfocusedLabelColor = PrimaryContainer
                ),
                shape = MaterialTheme.shapes.medium
            )

            // Mostrar alerta de confirmar contraseña
            if (confirmarContrasenaError.isNotEmpty()) {
                Text(
                    text = confirmarContrasenaError,
                    color = OnSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de registro
            Button(
                onClick = {
                    // Condicionales para validaciones de campos
                    if (!isValidNombre(nombre)) {
                        nombreError =
                            "El nombre debe tener al menos 5 caracteres y no puede contener caracteres especiales."
                        return@Button
                    }

                    if (!isValidEmail(correoElectronico)) {
                        correoError =
                            "El correo electrónico debe tener un formato válido (ejemplo@dominio.com)."
                        return@Button
                    }

                    if (!isValidPassword(contrasena)) {
                        contrasenaError =
                            "La contraseña debe tener al menos 8 carácteres y no puede contener carácteres especiales."
                        return@Button
                    }

                    if (confirmarContrasena != contrasena) {
                        confirmarContrasenaError = "Las contraseñas no coinciden."
                        return@Button
                    }
                    // Registrar
                    viewModel.register(nombre, correoElectronico, contrasena, confirmarContrasena)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = OnPrimaryContainer
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp),
                        color = OnPrimaryContainer
                    )
                } else {
                    Text(
                        "Registrarse",
                        color = OnPrimary,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón para navegar al inicio de sesión
            TextButton(
                onClick = onNavigateToLogin,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Primary
                )
            ) {
                Text("¿Ya tienes una cuenta? Inicia sesión", fontSize = 16.sp)
            }

            // Mostrar alerta de registro exitoso
            if (registerSuccess) {
                AlertDialog(
                    onDismissRequest = { viewModel.clearError() },
                    containerColor = Secondary,
                    title = { Text("Registro Exitoso", color = OnPrimary) },
                    text = {
                        Text(
                            "El usuario se ha registrado correctamente.",
                            color = OnPrimary
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.clearError()
                                onNavigateToLogin()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OnPrimaryContainer)
                        ) {
                            Text("Aceptar", color = OnPrimary)
                        }
                    }
                )
            }
        }
    }
}

