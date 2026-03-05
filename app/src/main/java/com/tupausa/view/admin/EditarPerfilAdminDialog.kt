package com.tupausa.view.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.tupausa.model.Usuario
import com.tupausa.ui.theme.OnPrimary
import com.tupausa.ui.theme.OnPrimaryContainer
import com.tupausa.ui.theme.OnSurface
import com.tupausa.ui.theme.OnSurfaceVariant
import com.tupausa.ui.theme.Primary
import com.tupausa.ui.theme.PrimaryContainer
import com.tupausa.ui.theme.Secondary
import com.tupausa.ui.theme.Surface
import com.tupausa.ui.theme.Tertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPerfilAdminDialog(
    usuario: Usuario,
    onDismiss: () -> Unit,
    onConfirmDatos: (nombre: String, email: String, nuevaPass: String) -> Unit
) {
    var nombre by remember { mutableStateOf(usuario.nombre) }
    val email = usuario.correoElectronico
    var nuevaContrasena by remember { mutableStateOf("") }
    var showPasswordDialog by remember { mutableStateOf(false) }

    var nombreError by remember { mutableStateOf(false) }

    if (showPasswordDialog) {
        CambiarContrasenaDialog(
            onDismiss = { showPasswordDialog = false },
            onConfirm = { 
                nuevaContrasena = it
                showPasswordDialog = false
            }
        )
    }

    AlertDialog(
        properties = DialogProperties(dismissOnClickOutside = false),
        containerColor = Secondary,
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f),
        title = {
            Text("Editar Mi Perfil (Admin)", color = OnPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Datos de Acceso", color = Surface, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        nombreError = it.length < 3
                    },
                    label = { Text("Nombre", color = OnPrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Tertiary, unfocusedBorderColor = OnSurfaceVariant
                    ),
                    isError = nombreError
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Correo electrónico", color = OnPrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Secondary,
                        unfocusedBorderColor = Secondary,
                        focusedTextColor = PrimaryContainer,
                        unfocusedTextColor = PrimaryContainer
                    )
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = OnSurfaceVariant)

                Button(
                    onClick = { showPasswordDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Tertiary)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = OnSurface)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (nuevaContrasena.isEmpty()) "Cambiar Contraseña" else "Contraseña Lista ✓",
                        color = OnSurface
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!nombreError && nombre.isNotEmpty()) {
                        onConfirmDatos(nombre, email, nuevaContrasena)
                    }
                },
                enabled = !nombreError && nombre.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = OnPrimaryContainer)
            ) {
                Text("Guardar Cambios", color = OnPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = OnPrimary)
            }
        }
    )
}

@Composable
fun CambiarContrasenaDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var pass by remember { mutableStateOf("") }
    var repeatPass by remember { mutableStateOf("") }
    var passVisible by remember { mutableStateOf(false) }
    var repeatPassVisible by remember { mutableStateOf(false) }

    val passwordError = repeatPass.isNotEmpty() && pass != repeatPass
    val isEnabled = pass.isNotEmpty() && repeatPass.isNotEmpty() && !passwordError

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Secondary,
        title = { Text("Cambiar Contraseña", color = OnPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Introduce tu nueva contraseña y confírmala para continuar.", color = Surface, fontSize = 14.sp)
                
                OutlinedTextField(
                    value = pass,
                    onValueChange = { pass = it },
                    label = { Text("Nueva contraseña", color = OnPrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (passVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passVisible = !passVisible }) {
                            Icon(imageVector = image, contentDescription = null, tint = PrimaryContainer)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Tertiary, unfocusedBorderColor = OnSurfaceVariant
                    )
                )

                OutlinedTextField(
                    value = repeatPass,
                    onValueChange = { repeatPass = it },
                    label = { Text("Repetir contraseña", color = OnPrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (repeatPassVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = passwordError,
                    trailingIcon = {
                        val image = if (repeatPassVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { repeatPassVisible = !repeatPassVisible }) {
                            Icon(imageVector = image, contentDescription = null, tint = PrimaryContainer)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Tertiary, unfocusedBorderColor = OnSurfaceVariant
                    )
                )

                if (passwordError) {
                    Text(
                        text = "Las contraseñas no coinciden",
                        color = Primary,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(pass) },
                enabled = isEnabled,
                colors = ButtonDefaults.buttonColors(containerColor = OnPrimaryContainer)
            ) {
                Text("Confirmar", color = OnPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = OnPrimary)
            }
        }
    )
}
