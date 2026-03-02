package com.tupausa.view.user

import androidx.compose.foundation.background
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
import com.tupausa.ui.theme.OnPrimary
import com.tupausa.ui.theme.OnPrimaryContainer
import com.tupausa.ui.theme.OnSecondary
import com.tupausa.ui.theme.OnSurface
import com.tupausa.ui.theme.OnSurfaceVariant
import com.tupausa.ui.theme.Tertiary
import com.tupausa.ui.theme.Primary
import com.tupausa.ui.theme.Secondary
import com.tupausa.ui.theme.Surface
import com.tupausa.utils.PreferencesManager
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

    // Filtrar ejercicios según el filtro seleccionado
    val filteredEjercicios = when (selectedFilter) {
        "TODOS" -> ejercicios
        else -> ejercicios.filter { it.tipoEjercicio == selectedFilter }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Pausas Activas") },
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
            // Filtros - Scroll Horizontal
            FiltroDesplegable (
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
                            color = OnPrimaryContainer
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
                                tint = OnSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No se encontraron ejercicios",
                                color = OnSurface
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
fun FiltroDesplegable(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val context = LocalContext.current

    // Obtenemos las limitaciones para no mostrar esas opciones en el menú
    val preferencesManager = remember { PreferencesManager(context) }
    val limitacionesList = remember {
        val limStr = preferencesManager.getLimitaciones()
        if (limStr.isNotEmpty()) limStr.split(",") else emptyList()
    }

    // Lista original
    val todosLosFiltros = listOf(
        "TODOS", "CUELLO", "HOMBROS", "MUÑECAS", "ESPALDA",
        "PIERNAS", "OJOS", "RESPIRACIÓN", "CARDIO SUAVE"
    )

    // Filtramos la lista
    val filters = todosLosFiltros.filter { it == "TODOS" || !limitacionesList.contains(it) }

    // Estado para saber si el menú está abierto o cerrado
    var expanded by remember { mutableStateOf(false) }

    // Función de ayuda para los nombres amigables
    val obtenerNombreAmigable = { filtro: String ->
        when (filtro) {
            "TODOS" -> "Ver todo"
            "CUELLO" -> "Cuello"
            "ESPALDA" -> "Espalda"
            "HOMBROS" -> "Hombros"
            "MUÑECAS" -> "Manos / Muñecas"
            "OJOS" -> "Ojos"
            "PIERNAS" -> "Pies / Piernas"
            "RESPIRACIÓN" -> "Respiración"
            "CARDIO SUAVE" -> "Cardio"
            else -> filtro
        }
    }

    // Contenedor del Dropdown
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // El campo de texto visible que el usuario toca
        OutlinedTextField(
            value = obtenerNombreAmigable(selectedFilter),
            onValueChange = {},
            readOnly = true,
            label = { Text("Categoría de ejercicios", color = OnSurface) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedContainerColor = Secondary,
                unfocusedContainerColor = Secondary,
                focusedBorderColor = Primary,
                unfocusedBorderColor = OnSecondary,
            ),
            shape = MaterialTheme.shapes.medium
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Secondary)
        ) {
            filters.forEach { filter ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = obtenerNombreAmigable(filter),
                            color = OnPrimary
                        )
                    },
                    onClick = {
                        onFilterSelected(filter)
                        expanded = false // Cerramos el menú al seleccionar
                    }
                )
            }
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
            containerColor = Secondary
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
                            contentDescription = ejercicio.nombreEjercicio,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = getIconoPorTipo(ejercicio.tipoEjercicio),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Surface
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

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = ejercicio.descripcion,
                    fontSize = 14.sp,
                    color = Surface,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Badge tipo
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = OnSecondary
                    ) {
                        Text(
                            text = getNombreAmigable(ejercicio.tipoEjercicio),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            color = OnSurface
                        )
                    }

                    // Badge duración
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = OnSurfaceVariant
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = OnPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${ejercicio.duracionSegundos}s",
                                fontSize = 12.sp,
                                color = OnPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getIconoPorTipo(tipo: String): ImageVector {
    return when (tipo) {
        "OJOS" -> Icons.Default.Visibility
        "CUELLO", "HOMBROS" -> Icons.Default.Face
        "ESPALDA" -> Icons.Default.AccessibilityNew
        "MUNECAS" -> Icons.Default.PanTool
        "PIERNAS" -> Icons.AutoMirrored.Filled.DirectionsWalk
        "RESPIRACION" -> Icons.Default.Air
        "CARDIO_SUAVE" -> Icons.AutoMirrored.Filled.DirectionsRun
        else -> Icons.Default.FitnessCenter
    }
}

fun getNombreAmigable(tipo: String): String {
    return when (tipo) {
        "CARDIO_SUAVE" -> "Cardio"
        "MUNECAS" -> "Muñecas"
        else -> tipo.lowercase().replaceFirstChar { it.uppercase() }
    }
}