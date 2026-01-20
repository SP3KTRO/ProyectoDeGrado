package com.tupausa.view.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tupausa.model.Ejercicio
import com.tupausa.ui.theme.OnPrimary
import com.tupausa.ui.theme.OnPrimaryContainer
import com.tupausa.ui.theme.OnSurface
import com.tupausa.ui.theme.PrimaryContainer
import com.tupausa.ui.theme.Secondary
import com.tupausa.ui.theme.Surface
import com.tupausa.ui.theme.Tertiary
import com.tupausa.utils.rememberDrawableId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEjerciciosScreen(
    ejercicios: List<Ejercicio>,
    isLoading: Boolean,
    onEjercicioClick: (Ejercicio) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Ejercicios") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                // 2. Barra Transparente y textos color Café
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
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = OnPrimaryContainer
                    )
                }
                ejercicios.isEmpty() -> {
                    Text(
                        text = "No hay ejercicios disponibles",
                        modifier = Modifier.align(Alignment.Center),
                        color = OnSurface
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(ejercicios) { ejercicio ->
                            AdminEjercicioCard(
                                ejercicio = ejercicio,
                                onClick = { onEjercicioClick(ejercicio) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEjercicioCard(
    ejercicio: Ejercicio,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    // Obtener ID de la imagen
    val drawableId = rememberDrawableId(ejercicio.urlImagenGuia)

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Secondary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // IMAGEN
            Surface(
                modifier = Modifier.size(64.dp),
                shape = MaterialTheme.shapes.medium,
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (drawableId != 0) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(drawableId)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = getAdminIconoPorTipo(ejercicio.tipoEjercicio),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = OnPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ejercicio.nombreEjercicio,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnPrimary
                )
                // Categoría
                Text(
                    text = getAdminNombreAmigable(ejercicio.tipoEjercicio),
                    fontSize = 14.sp,
                    color = Surface,
                    fontWeight = FontWeight.Light
                )
            }
            // Datos Extra
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${ejercicio.duracionSegundos}s",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Tertiary
                )
                Text(
                    text = "Nivel: ${ejercicio.nivelIntensidad}",
                    fontSize = 12.sp,
                    color = PrimaryContainer
                )
            }
        }
    }
}

fun getAdminIconoPorTipo(tipo: String): ImageVector {
    return when (tipo) {
        "OJOS" -> Icons.Default.Visibility
        "CUELLO", "HOMBROS" -> Icons.Default.Face
        "ESPALDA" -> Icons.Default.AccessibilityNew
        "MUNECAS", "BRAZOS" -> Icons.Default.PanTool
        "PIERNAS", "PIES" -> Icons.AutoMirrored.Filled.DirectionsWalk
        "RESPIRACION" -> Icons.Default.Air
        "CARDIO_SUAVE" -> Icons.AutoMirrored.Filled.DirectionsRun
        "ESTIRAMIENTO_GENERAL" -> Icons.Default.SelfImprovement
        else -> Icons.Default.FitnessCenter
    }
}

fun getAdminNombreAmigable(tipo: String): String {
    return when (tipo) {
        "CARDIO_SUAVE" -> "Cardio"
        "ESTIRAMIENTO_GENERAL" -> "General"
        "MUNECAS" -> "Muñecas"
        else -> tipo.lowercase().replaceFirstChar { it.uppercase() }
    }
}