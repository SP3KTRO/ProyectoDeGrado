package com.tupausa.view.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tupausa.ui.theme.OnPrimaryContainer
import com.tupausa.ui.theme.OnSurface
import com.tupausa.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHistorialScreen(
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Mi Historial") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
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
                    text = "📊",
                    fontSize = 64.sp
                )
                Text(
                    text = "Próximamente",
                    fontSize = 24.sp,
                    color = OnPrimaryContainer
                )
                Text(
                    text = "Aquí verás el historial de pausas\ny estadísticas de progreso",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = OnSurface
                )
            }
        }
    }
}