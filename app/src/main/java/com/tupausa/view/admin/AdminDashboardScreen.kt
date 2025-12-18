package com.tupausa.view.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tupausa.R
import com.tupausa.TuPausaApplication
import com.tupausa.ui.theme.ArenaOnPrimaryContainer
import com.tupausa.ui.theme.ArenaSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateToUsersList: () -> Unit,
    onNavigateToEjercicios: () -> Unit,
    onLogout: () -> Unit
) {
    // Obtener nombre del usuario desde PreferencesManager
    val context = LocalContext.current
    val app = context.applicationContext as TuPausaApplication
    val userName = app.preferencesManager.getUserName()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Image(
                    painter = painterResource(id = R.drawable.logo_white),
                    contentDescription = "Logo de TuPausa",
                    modifier = Modifier
                        .size(130.dp)
                        .padding(bottom = 10.dp),
                    contentScale = ContentScale.Fit
                )},
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, "Cerrar Sesión")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "¡Hola ${userName}!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ArenaOnPrimaryContainer
            )

            // Badge de Administrador
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AdminPanelSettings,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Administrador",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = ArenaSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta: Ver Usuarios
            AdminMenuCard(
                title = "Ver Usuarios",
                description = "Lista de todos los usuarios registrados",
                icon = painterResource(id = R.drawable.user),
                iconTint = Color.Unspecified,
                onClick = onNavigateToUsersList
            )

            // Tarjeta: Gestionar Ejercicios
            AdminMenuCard(
                title = "Ejercicios",
                description = "Ver lista de ejercicios disponibles",
                icon = painterResource(id = R.drawable.exercise),
                iconTint = Color.Unspecified,
                onClick = onNavigateToEjercicios
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMenuCard(
    title: String,
    description: String,
    icon: Painter,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = iconTint
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}