package com.tupausa.view.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tupausa.R
import com.tupausa.model.Usuario
import com.tupausa.ui.theme.OnPrimary
import com.tupausa.ui.theme.OnPrimaryContainer
import com.tupausa.ui.theme.OnSurface
import com.tupausa.ui.theme.OnSurfaceVariant
import com.tupausa.ui.theme.Outline
import com.tupausa.ui.theme.Primary
import com.tupausa.ui.theme.PrimaryContainer
import com.tupausa.ui.theme.Secondary
import com.tupausa.ui.theme.Surface
import com.tupausa.ui.theme.Tertiary
import com.tupausa.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersListScreen(
    usuarios: List<Usuario>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onDeleteUsuario: (Usuario) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var usuarioToDelete by remember { mutableStateOf<Usuario?>(null) }

    var searchQuery by remember { mutableStateOf("") }

    val filteredUsuarios = remember(searchQuery, usuarios) {
        if (searchQuery.isBlank()) {
            usuarios
        } else {
            usuarios.filter {
                it.nombre.contains(searchQuery, ignoreCase = true) ||
                        it.correoElectronico.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Lista de Usuarios") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")

                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = OnSurface,
                    navigationIconContentColor = OnSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Barra de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar por nombre o correo...", color = OnSurface) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = OnPrimary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = OnPrimary)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Tertiary,
                    unfocusedBorderColor = OnPrimaryContainer,
                    focusedContainerColor = Secondary,
                    unfocusedContainerColor = Secondary
                ),
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )

            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = OnPrimaryContainer)
                    }
                    filteredUsuarios.isEmpty() -> {
                        Text(
                            text = if (searchQuery.isEmpty()) "No hay usuarios registrados" else "No se encontraron coincidencias",
                            color = OnSurface,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredUsuarios) { usuario ->
                                UsuarioCardWithActions(
                                    usuario = usuario,
                                    icon = painterResource(id = R.drawable.user),
                                    iconTint = Color.Unspecified,
                                    onDelete = {
                                        usuarioToDelete = usuario
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dialog de confirmación para eliminar
        if (showDeleteDialog && usuarioToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                containerColor = Secondary,
                icon = {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = OnSurfaceVariant
                    )
                },
                title = {
                    Text("¿Eliminar usuario?", color = OnPrimary)
                },
                text = {
                    Text("¿Estás seguro de que deseas eliminar a ${usuarioToDelete?.nombre}? Esta acción no se puede deshacer.", color = OnPrimary)
                },
                confirmButton = {
                    Button(
                        onClick = {
                            usuarioToDelete?.let { onDeleteUsuario(it) }
                            showDeleteDialog = false
                            usuarioToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OnPrimaryContainer
                        )
                    ) {
                        Text("Eliminar", color = OnPrimary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar", color = OnPrimary)
                    }
                }
            )
        }
    }
}

@Composable
fun UsuarioCardWithActions(
    usuario: Usuario,
    icon: Painter,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Secondary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painter = icon, contentDescription = null, modifier = Modifier.size(40.dp), tint = iconTint)

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = usuario.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OnPrimary)
                Text(text = usuario.correoElectronico, fontSize = 14.sp, color = OnPrimary)
                Text(
                    text = if (usuario.idTipoUsuario == Constants.USER_TYPE_ADMIN) "Admin" else "Usuario",
                    fontSize = 12.sp,
                    color = if (usuario.idTipoUsuario == Constants.USER_TYPE_ADMIN) Tertiary else PrimaryContainer
                )
            }

            // Botón eliminar - solo si no es admin
            if (usuario.idTipoUsuario != Constants.USER_TYPE_ADMIN) {
                IconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = OnSurfaceVariant)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}