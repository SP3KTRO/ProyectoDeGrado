package com.tupausa.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.tupausa.R
import com.tupausa.ui.theme.OnPrimary
import com.tupausa.ui.theme.OnPrimaryContainer
import com.tupausa.ui.theme.OnSecondary
import com.tupausa.ui.theme.OnSurface
import com.tupausa.ui.theme.Outline
import com.tupausa.ui.theme.Primary
import com.tupausa.ui.theme.PrimaryContainer
import com.tupausa.ui.theme.Secondary
import com.tupausa.viewModel.LoginViewModel

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    loginViewModel: LoginViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Observar estados del ViewModel
    val isLoading by loginViewModel.isLoading.observeAsState(false)
    val error by loginViewModel.error.observeAsState()
    val loginSuccess by loginViewModel.loginSuccess.observeAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar error si existe
    LaunchedEffect(error) {
        error?.let { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = SnackbarDuration.Short,
                )
                loginViewModel.clearError() // Limpia el error para que pueda volver a dispararse
            }
        }
    }

    // Navegar cuando login es exitoso
    LaunchedEffect(loginSuccess) {
        loginSuccess?.let {
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo de TuPausa",
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 32.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "Iniciar sesión",
                fontSize = 22.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
                color = OnSurface
            )

            // Campo de email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                enabled = !isLoading,
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

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                shape = MaterialTheme.shapes.medium,
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de login
            Button(
                onClick = {
                    loginViewModel.login(email, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty(),
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
                        "Iniciar sesión", fontSize = 16.sp,
                        color = OnPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón para ir a registro
            TextButton(
                onClick = onNavigateToRegister,
                enabled = !isLoading,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Primary
                )
            ) {
                Text("¿No tienes cuenta? Regístrate")
            }

                // Mostrar error si existe
                /*error?.let { errorMessage ->
                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Primary
                        )
                    ) {
                        Text(
                            text = errorMessage,
                            modifier = Modifier.padding(16.dp),
                            color = OnPrimary
                        )
                    }
                }
            }*/
        }
    }
}