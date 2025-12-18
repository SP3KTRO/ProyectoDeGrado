package com.tupausa.view.user

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tupausa.ui.theme.ArenaOnPrimaryContainer
import com.tupausa.ui.theme.ArenaPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAlarmasScreen(
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Mis Alarmas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = ArenaOnPrimaryContainer,
                    navigationIconContentColor = ArenaOnPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Crear alarma */ }
            ) {
                Icon(Icons.Default.Add, "Crear Alarma")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🔔",
                    fontSize = 64.sp
                )
                Text(
                    text = "Próximamente",
                    fontSize = 24.sp
                )
                Text(
                    text = "Aquí podrás programar tus alarmas\npara las pausas activas",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = ArenaPrimary
                )
            }
        }
    }
}