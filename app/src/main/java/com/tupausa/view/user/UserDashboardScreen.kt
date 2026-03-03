package com.tupausa.view.user

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.tupausa.ui.theme.Tertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboardScreen(
    onNavigateToEjercicios: () -> Unit,
    onNavigateToAlarmas: () -> Unit,
    onNavigateToHistorial: () -> Unit,
    onLogout: () -> Unit,
) {
    // Obtener nombre del usuario desde PreferencesManager
    val context = LocalContext.current
    val app = context.applicationContext as TuPausaApplication
    val userName = app.preferencesManager.getUserName()

    // Estado para controlar el diálogo de Acerca de
    var showAboutDialog by remember { mutableStateOf(false) }

    // Función para enviar correo
    val sendEmail = {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("jdcallejasc@udistrital.edu.co", "bacostap@udistrital.edu.co"))
            putExtra(Intent.EXTRA_SUBJECT, "Soporte TuPausa - Usuario: $userName")
        }
        context.startActivity(Intent.createChooser(intent, "Enviar correo con..."))
    }

    // Dialog Acerca De
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = Secondary,
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Cerrar", color = Tertiary)
                }
            },
            title = { Text("TuPausa", color = Tertiary) },
            text = {
                Column {
                    Text("Versión: 1.0.0", color = OnPrimary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Desarrollado por:\nDaniel Callejas & Brayan Acosta", color = OnPrimary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Tecnología en Sistematización de datos.", color = OnPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Universidad Distrital Francisco José de Caldas.", color = OnPrimary)
                }
            }
        )
    }

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
        },
        bottomBar = {
            BottomAppBar(
                containerColor = OnPrimaryContainer,
                contentColor = OnPrimary,
                tonalElevation = 8.dp,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(60.dp)
                    ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Soporte
                    Surface(
                        onClick = { sendEmail() },
                        shape = RoundedCornerShape(8.dp),
                        color = OnPrimaryContainer,
                        modifier = Modifier.size(55.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Email,
                                tint = OnPrimary,
                                contentDescription = "Soporte",
                                modifier = Modifier.size(20.dp)
                            )
                            Text("Soporte", fontSize = 10.sp, color = OnPrimary)
                        }
                    }
                    // Acerca de / Versión
                    Surface(
                        onClick = { showAboutDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        color = OnPrimaryContainer,
                        modifier = Modifier.size(55.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Info,
                                tint = OnPrimary,
                                contentDescription = "Info",
                                modifier = Modifier.size(20.dp)
                            )
                            Text("v1.0", fontSize = 10.sp, color = OnPrimary)
                        }
                    }
                }
            }
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
                text = "Cuídate mientras estudias",
                fontSize = 19.sp,
                fontWeight = FontWeight.Light,
                color = OnSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Card Pausas
            UserMenuCard(
                title = "Pausas Activas",
                description = "Explora ejercicios de pausas activas",
                icon = painterResource(id = R.drawable.exercise),
                iconTint = Color.Unspecified,
                onClick = onNavigateToEjercicios
            )

            // Card Rutinas
            UserMenuCard(
                title = "Mis Rutinas",
                description = "Programa alarmas para tus rutinas de pausas activas",
                icon = painterResource(id = R.drawable.reloj),
                iconTint = Color.Unspecified,
                onClick = onNavigateToAlarmas
            )

            // Card Historial
            UserMenuCard(
                title = "Mi Historial",
                description = "Revisa tu historial, progreso y estadísticas",
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