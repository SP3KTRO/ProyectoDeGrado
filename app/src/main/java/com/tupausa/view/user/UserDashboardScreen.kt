package com.tupausa.view.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tupausa.R
import com.tupausa.TuPausaApplication
import com.tupausa.ui.theme.Secondary
import com.tupausa.ui.theme.OnPrimaryContainer
import com.tupausa.ui.theme.OnSurface
import com.tupausa.ui.theme.OnPrimary
import com.tupausa.ui.theme.Surface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboardScreen(
    onNavigateToEjercicios: () -> Unit,
    onNavigateToAlarmas: () -> Unit,
    onNavigateToHistorial: () -> Unit,
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
                ) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, "Cerrar Sesión", tint = OnPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OnPrimaryContainer
                )
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
            // Saludo personalizado
            Text(
                text = "¡Hola ${userName}!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )

            // Bienvenida
            Text(
                text = "Cuídate mientras estudias 💻",
                fontSize = 19.sp,
                fontWeight = FontWeight.Light,
                color = OnSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tarjeta: Ver Ejercicios
            UserMenuCard(
                title = "Ejercicios",
                description = "Explora ejercicios de pausas activas",
                icon = painterResource(id = R.drawable.exercise),
                iconTint = Color.Unspecified,
                onClick = onNavigateToEjercicios
            )

            // Tarjeta: Mis Alarmas
            UserMenuCard(
                title = "Mis Alarmas",
                description = "Programa tus pausas activas",
                icon = painterResource(id = R.drawable.reloj),
                iconTint = Color.Unspecified,
                onClick = onNavigateToAlarmas
            )

            // Tarjeta: Mi Historial
            UserMenuCard(
                title = "Mi Historial",
                description = "Revisa tu progreso y estadísticas",
                icon = painterResource(id = R.drawable.historial),
                iconTint = Color.Unspecified,
                onClick = onNavigateToHistorial
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMenuCard(
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
            containerColor = Secondary
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
                    color = OnPrimary
                )
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Surface
                )
            }
        }
    }
}