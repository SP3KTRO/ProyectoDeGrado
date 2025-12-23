package com.tupausa.view.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tupausa.model.Ejercicio
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tupausa.ui.theme.ArenaOnPrimaryContainer
import com.tupausa.utils.rememberDrawableId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserEjerciciosListScreen(
    ejercicios: List<Ejercicio>,
    isLoading: Boolean,
    onEjercicioClick: (Ejercicio) -> Unit,
    onBack: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("TODOS") }

    // Lógica de filtrado
    val filteredEjercicios = when (selectedFilter) {
        "TODOS" -> ejercicios
        else -> ejercicios.filter { it.tipoEjercicio == selectedFilter }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Ejercicios") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                // 2. Barra transparente. El título tomará el color Café Oscuro (onSurface) definido en tu Theme
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = ArenaOnPrimaryContainer,
                    navigationIconContentColor = ArenaOnPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filtros (Scroll Horizontal)
            FilterChips(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )

            // Lista de ejercicios
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary // Color Bronce
                        )
                    }
                    filteredEjercicios.isEmpty() -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant // Café grisáceo
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No se encontraron ejercicios",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredEjercicios) { ejercicio ->
                                UserEjercicioCard(
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChips(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val filters = listOf(
        "TODOS",
        "CUELLO",
        "HOMBROS",
        "MUÑECAS",
        "ESPALDA",
        "PIERNAS",
        "PIES",
        "OJOS",
        "RESPIRACIÓN",
        "CARDIO SUAVE",
        "GENERAL"
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters.size) { index ->
            val filter = filters[index]
            val isSelected = selectedFilter == filter
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = when (filter) {
                            "TODOS" -> "Todos"
                            "CUELLO" -> "Cuello"
                            "ESPALDA" -> "Espalda"
                            "HOMBROS" -> "Hombros"
                            "MUÑECAS" -> "Muñecas"
                            "OJOS" -> "Ojos"
                            "PIERNAS" -> "Piernas"
                            "PIES" -> "Pies"
                            "RESPIRACIÓN" -> "Respiración"
                            "CARDIO SUAVE" -> "Cardio"
                            "GENERAL" -> "General"
                            else -> filter
                        }
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    selectedBorderColor = Color.Transparent,
                    borderWidth = 1.dp
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserEjercicioCard(
    ejercicio: Ejercicio,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    // Obtener ID del GIF
    val drawableId = rememberDrawableId(ejercicio.urlImagenGuia)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 3. IMAGEN
            Surface(
                modifier = Modifier.size(72.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
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
                            contentDescription = ejercicio.nombreEjercicio,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = getIconoPorTipo(ejercicio.tipoEjercicio),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary // Bronce
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
                    color = MaterialTheme.colorScheme.onSurface // Café Oscuro
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = ejercicio.descripcion,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // Café Gris
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Badge tipo
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.secondaryContainer // Bronce suave
                    ) {
                        Text(
                            text = getNombreAmigable(ejercicio.tipoEjercicio),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer // Café muy oscuro
                        )
                    }

                    // Badge duración
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        // Un color un poco distinto para la duración (usamos Tertiary)
                        color = MaterialTheme.colorScheme.tertiaryContainer // Puede ser un tono melocotón suave si lo definiste, o default
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${ejercicio.duracionSegundos}s",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

// Funciones auxiliares
fun getIconoPorTipo(tipo: String): ImageVector {
    return when (tipo) {
        "OJOS" -> Icons.Default.Visibility
        "CUELLO", "HOMBROS" -> Icons.Default.Face
        "ESPALDA" -> Icons.Default.AccessibilityNew
        "MUNECAS" -> Icons.Default.PanTool
        "PIERNAS", "PIES" -> Icons.AutoMirrored.Filled.DirectionsWalk
        "RESPIRACION" -> Icons.Default.Air
        "CARDIO_SUAVE" -> Icons.AutoMirrored.Filled.DirectionsRun
        "ESTIRAMIENTO_GENERAL" -> Icons.Default.SelfImprovement
        else -> Icons.Default.FitnessCenter
    }
}

fun getNombreAmigable(tipo: String): String {
    return when (tipo) {
        "CARDIO_SUAVE" -> "Cardio"
        "ESTIRAMIENTO_GENERAL" -> "General"
        "MUNECAS" -> "Muñecas"
        else -> tipo.lowercase().replaceFirstChar { it.uppercase() }
    }
}