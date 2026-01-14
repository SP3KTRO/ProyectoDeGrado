package com.tupausa.view.user

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tupausa.model.data.Alarma
import com.tupausa.model.Ejercicio
import com.tupausa.ui.theme.ArenaOnPrimaryContainer
import com.tupausa.ui.theme.ArenaPrimary
import com.tupausa.ui.theme.ArenaPrimaryContainer
import com.tupausa.utils.rememberDrawableId

private enum class DialogStep {
    FORMULARIO,
    SELECCION_EJERCICIO
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmaFormDialog(
    alarmaAEditar: Alarma? = null,
    listaEjercicios: List<Ejercicio>,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, List<Int>, String, String, String, Int) -> Unit
) {
    // ESTADOS DEL FORMULARIO
    val timeState = rememberTimePickerState(
        initialHour = alarmaAEditar?.hora ?: 8,
        initialMinute = alarmaAEditar?.minuto ?: 0
    )
    var etiqueta by remember { mutableStateOf(alarmaAEditar?.etiqueta ?: "") }
    var tonoSeleccionado by remember { mutableStateOf("Predeterminado") }

    // Días
    val daysOptions = listOf(2, 3, 4, 5, 6, 7, 1)
    val daysLabels = listOf("L", "M", "M", "J", "V", "S", "D")
    val selectedDays = remember { mutableStateListOf<Int>().apply {
        if (alarmaAEditar != null) addAll(alarmaAEditar.diasRepeticion)
    }}

    // Ejercicio Seleccionado
    var ejercicioSeleccionado: Ejercicio? by remember { mutableStateOf(
        if (alarmaAEditar != null) listaEjercicios.find { it.idEjercicio == alarmaAEditar.idEjercicio } else null
    )}

    // ESTADO PARA CONTROLAR LA VISTA INTERNA
    var currentStep by remember { mutableStateOf(DialogStep.FORMULARIO) }

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f).heightIn(max = 600.dp),
        confirmButton = {
            // Solo mostramos el botón de Guardar si estamos en el Formulario
            if (currentStep == DialogStep.FORMULARIO) {
                Button(
                    enabled = ejercicioSeleccionado != null,
                    onClick = {
                        onConfirm(
                            timeState.hour,
                            timeState.minute,
                            selectedDays.toList(),
                            etiqueta.ifEmpty { ejercicioSeleccionado?.nombreEjercicio ?: "Pausa Activa" },
                            ejercicioSeleccionado?.tipoEjercicio ?: "ALEATORIO",
                            tonoSeleccionado,
                            ejercicioSeleccionado?.idEjercicio ?: -1
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArenaPrimary
                    )
                ) { Text(if (alarmaAEditar == null) "Crear" else "Guardar",
                        color = ArenaPrimaryContainer
                ) }
            }
        },
        dismissButton = {
            // Botón de Cancelar o Volver
            TextButton(onClick = {
                if (currentStep == DialogStep.SELECCION_EJERCICIO) {
                    currentStep = DialogStep.FORMULARIO // Volver atrás
                } else {
                    onDismiss() // Cerrar dialog
                }
            }) {
                Text(if (currentStep == DialogStep.SELECCION_EJERCICIO) "Volver" else "Cancelar",
                        color = ArenaPrimaryContainer)
            }
        },
        title = {
            Text(
                if (currentStep == DialogStep.FORMULARIO)
                    (if (alarmaAEditar == null) "Nueva Alarma" else "Editar Alarma")
                else "Elige un ejercicio"
            )
        },
        text = {
            Crossfade(targetState = currentStep, label = "dialog_transition") { step ->
                when (step) {
                    DialogStep.FORMULARIO -> {
                        // --- VISTA A: FORMULARIO ---
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            TimeInput(state = timeState)

                            // Tarjeta Clickable -> Cambia el paso a SELECCION
                            OutlinedCard(
                                onClick = { currentStep = DialogStep.SELECCION_EJERCICIO },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.outlinedCardColors(containerColor = Color.Transparent)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (ejercicioSeleccionado != null) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Ejercicio Seleccionado:", fontSize = 12.sp, color = Color.Gray)
                                            Text(text = ejercicioSeleccionado!!.nombreEjercicio, fontWeight = FontWeight.Bold)
                                        }
                                        Icon(Icons.Default.Edit, "Cambiar", tint = MaterialTheme.colorScheme.primary)
                                    } else {
                                        Icon(Icons.Default.FitnessCenter, null, tint = Color.Gray)
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text("Seleccionar Ejercicio", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.error)
                                        Icon(Icons.Default.ChevronRight, null)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = etiqueta,
                                onValueChange = { etiqueta = it },
                                label = { Text("Nombre") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Días
                            Text("Repetir:", style = MaterialTheme.typography.bodySmall)
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                daysOptions.forEachIndexed { index, dayValue ->
                                    val isSelected = selectedDays.contains(dayValue)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { if (isSelected) selectedDays.remove(dayValue) else selectedDays.add(dayValue) },
                                        label = { Text(daysLabels[index], fontSize = 10.sp) },
                                        modifier = Modifier.size(38.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        border = null
                                    )
                                }
                            }
                        }
                    }

                    DialogStep.SELECCION_EJERCICIO -> {
                        // --- VISTA B: LISTA DE EJERCICIOS ---
                        // Una lista simple dentro del mismo espacio del texto
                        Column {
                            // Usamos BoxWithConstraints para asegurar altura si la lista es larga
                            Box(modifier = Modifier.heightIn(max = 400.dp)) {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(listaEjercicios) { ejercicio ->
                                        SelectableEjercicioCard(
                                            ejercicio = ejercicio,
                                            isSelected = ejercicio.idEjercicio == ejercicioSeleccionado?.idEjercicio,
                                            onClick = {
                                                ejercicioSeleccionado = ejercicio
                                                currentStep = DialogStep.FORMULARIO // Seleccionar y volver automáticamente
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

// --- CARD DE EJERCICIO SELECCIONABLE (Estilo AdminEjercicios) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectableEjercicioCard(
    ejercicio: Ejercicio,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val drawableId = rememberDrawableId(ejercicio.urlImagenGuia)

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            // Si está seleccionado, le damos un tinte diferente
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen
            Surface(
                modifier = Modifier.size(56.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            ) {
                if (drawableId != 0) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(drawableId).crossfade(true).build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.FitnessCenter, null, modifier = Modifier.padding(12.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Textos
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ejercicio.nombreEjercicio,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${ejercicio.duracionSegundos}s • ${ejercicio.tipoEjercicio}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Radio Button visual (para que se note que es selección)
            RadioButton(selected = isSelected, onClick = null)
        }
    }
}

// --- SELECTOR DE TONO ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorTono(tonoActual: String, onTonoChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    // Puedes agregar nombres de tus archivos en res/raw aquí
    val opciones = listOf("Predeterminado", "Suave", "Enérgico", "Naturaleza")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = tonoActual,
            onValueChange = {},
            readOnly = true,
            label = { Text("Tono de Alarma") },
            leadingIcon = { Icon(Icons.Default.Notifications, null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onTonoChange(opcion)
                        expanded = false
                    },
                    leadingIcon = {
                        // Icono de play pequeñito para simular (funcionalidad futura)
                        Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                    }
                )
            }
        }
    }
}