package com.tupausa.view.admin

import android.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tupausa.model.Usuario
import com.tupausa.ui.theme.ArenaPrimary
import com.tupausa.ui.theme.ArenaPrimaryContainer
import com.tupausa.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarUsuarioDialog(
    usuario: Usuario,
    onDismiss: () -> Unit,
    onConfirm: (Usuario) -> Unit
) {
    var nombre by remember { mutableStateOf(usuario.nombre) }
    var email by remember { mutableStateOf(usuario.correoElectronico) }
    var tipoUsuario by remember { mutableStateOf(usuario.idTipoUsuario) }

    var nombreError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        onDismissRequest = onDismiss,
        title = {
            Text("Editar Usuario")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Campo Nombre
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        nombreError = it.length < Constants.MIN_NAME_LENGTH
                    },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nombreError,
                    supportingText = {
                        if (nombreError) {
                            Text("El nombre debe tener al menos ${Constants.MIN_NAME_LENGTH} caracteres")
                        }
                    }
                )

                // Campo Email
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = !it.matches(Constants.EMAIL_REGEX.toRegex())
                    },
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = emailError,
                    supportingText = {
                        if (emailError) {
                            Text("Formato de correo inválido")
                        }
                    }
                )

                // Selector de Tipo de Usuario
                Column {
                    Text("Tipo de usuario", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = tipoUsuario == Constants.USER_TYPE_REGULAR,
                            onClick = { tipoUsuario = Constants.USER_TYPE_REGULAR },
                            label = { Text("Usuario") }
                        )
                        FilterChip(
                            selected = tipoUsuario == Constants.USER_TYPE_ADMIN,
                            onClick = { tipoUsuario = Constants.USER_TYPE_ADMIN },
                            label = { Text("Administrador") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!nombreError && !emailError && nombre.isNotEmpty() && email.isNotEmpty()) {
                        val usuarioActualizado = usuario.copy(
                            nombre = nombre,
                            correoElectronico = email,
                            idTipoUsuario = tipoUsuario
                        )
                        onConfirm(usuarioActualizado)
                    }
                },
                enabled = !nombreError && !emailError && nombre.isNotEmpty() && email.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ArenaPrimary
                )
            ) {
                Text("Guardar",
                    color = ArenaPrimaryContainer)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar",
                    color = ArenaPrimaryContainer)
            }
        }
    )
}