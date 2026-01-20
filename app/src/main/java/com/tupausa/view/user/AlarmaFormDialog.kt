package com.tupausa.view.user

import android.R
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
import androidx.compose.ui.graphics.Color.Companion
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tupausa.model.data.Alarma
import com.tupausa.model.Ejercicio
import com.tupausa.ui.theme.Gris
import com.tupausa.ui.theme.OnPrimary
import com.tupausa.ui.theme.OnPrimaryContainer
import com.tupausa.ui.theme.OnSecondary
import com.tupausa.ui.theme.OnSurface
import com.tupausa.ui.theme.OnSurfaceVariant
import com.tupausa.ui.theme.Primary
import com.tupausa.ui.theme.PrimaryContainer
import com.tupausa.ui.theme.Secondary
import com.tupausa.ui.theme.Surface
import com.tupausa.ui.theme.Tertiary
import com.tupausa.utils.rememberDrawableId

private enum class DialogStep { FORMULARIO, SELECCION_EJERCICIO }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmaFormDialog(
    alarmaAEditar: Alarma? = null,
    listaEjercicios: List<Ejercicio>,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, List<Int>, String, String, String, List<Int>) -> Unit
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
    val selectedDays = remember {
        mutableStateListOf<Int>().apply {
            if (alarmaAEditar != null) addAll(alarmaAEditar.diasRepeticion)
        }
    }

    // Rutina de Ejercicios
    val selectedExerciseIds = remember {
        mutableStateListOf<Int>().apply {
            if (alarmaAEditar != null) addAll(alarmaAEditar.idsEjercicios)
        }
    }

    // ESTADO PARA CONTROLAR LA VISTA INTERNA
    var currentStep by remember { mutableStateOf(DialogStep.FORMULARIO) }

    AlertDialog(
        containerColor = Secondary,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .heightIn(max = 600.dp),
        confirmButton = {
            if (currentStep == DialogStep.FORMULARIO) {
                val puedeGuardar = selectedExerciseIds.size >= 4
                Column(horizontalAlignment = Alignment.End) {
                    if (!puedeGuardar && selectedExerciseIds.isNotEmpty()) {
                        Text(
                            "Mínimo 4 ejercicios",
                            color = OnSurfaceVariant,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Button(
                        enabled = puedeGuardar,
                        onClick = {
                            onConfirm(
                                timeState.hour,
                                timeState.minute,
                                selectedDays.toList(),
                                etiqueta.ifEmpty { "Mi Rutina de Pausa" },
                                "RUTINA",
                                tonoSeleccionado,
                                selectedExerciseIds.toList()
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OnPrimaryContainer)
                    ) {
                        Text(
                            if (alarmaAEditar == null) "Crear" else "Guardar",
                            color = OnPrimary
                        )
                    }
                }
            } else {
                // Botón "Listo" en la pantalla de selección
                Button(
                    onClick = { currentStep = DialogStep.FORMULARIO },
                    colors = ButtonDefaults.buttonColors(containerColor = OnPrimaryContainer)
                ) {
                    Text("Listo (${selectedExerciseIds.size})", color = OnPrimary)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (currentStep == DialogStep.SELECCION_EJERCICIO) {
                    currentStep = DialogStep.FORMULARIO
                } else {
                    onDismiss()
                }
            }) {
                Text(
                    if (currentStep == DialogStep.SELECCION_EJERCICIO) "Volver" else "Cancelar",
                    color = OnPrimary
                )
            }
        },
        title = {
            Text(
                text = if (currentStep == DialogStep.FORMULARIO)
                    (if (alarmaAEditar == null) "Nueva Alarma" else "Editar Alarma")
                else "Arma tu rutina (mín. 4)",
                color = OnPrimary
            )
        },
        text = {
            Crossfade(targetState = currentStep, label = "dialog_transition") { step ->
                when (step) {
                    DialogStep.FORMULARIO -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            TimeInput(
                                state = timeState,
                                colors = TimePickerDefaults.colors(
                                    // AM PM
                                    periodSelectorSelectedContainerColor = OnPrimaryContainer,
                                    periodSelectorUnselectedContainerColor = Gris,
                                    // Hora y Minutos
                                    timeSelectorSelectedContainerColor = OnPrimaryContainer,
                                    timeSelectorUnselectedContainerColor = Gris,
                                )
                            )
                            OutlinedCard(
                                onClick = { currentStep = DialogStep.SELECCION_EJERCICIO },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.outlinedCardColors(containerColor = Color.Transparent)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Rutina seleccionada:", fontSize = 12.sp, color = OnPrimary)
                                        Text(
                                            text = if (selectedExerciseIds.isEmpty()) "Ningún ejercicio"
                                            else "${selectedExerciseIds.size} ejercicios seleccionados",
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedExerciseIds.size < 4) OnSurfaceVariant else Tertiary
                                        )
                                    }
                                    Icon(Icons.Default.Edit, "Cambiar", tint = OnPrimary)
                                }
                            }

                            OutlinedTextField(
                                value = etiqueta,
                                onValueChange = { etiqueta = it },
                                label = {
                                    Text(  "Nombre de la rutina", color = OnPrimary) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    unfocusedBorderColor = OnPrimary
                                )
                            )

                            Text("Repetir:", style = MaterialTheme.typography.bodySmall, color = OnPrimary)
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
                        Column {
                            Text(
                                "Seleccionados: ${selectedExerciseIds.size}/4 mínimo",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selectedExerciseIds.size < 4) OnSurfaceVariant else Tertiary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Box(modifier = Modifier.heightIn(max = 400.dp)) {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(listaEjercicios) { ejercicio ->
                                        val isSelected = selectedExerciseIds.contains(ejercicio.idEjercicio)
                                        SelectableEjercicioCard(
                                            ejercicio = ejercicio,
                                            isSelected = isSelected,
                                            onClick = {
                                                if (isSelected) selectedExerciseIds.remove(ejercicio.idEjercicio)
                                                else selectedExerciseIds.add(ejercicio.idEjercicio)
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
            containerColor = if (isSelected)
                OnPrimaryContainer
            else
                Secondary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = MaterialTheme.shapes.small,
                color = Color.Transparent
            ) {
                if (drawableId != 0) {
                    AsyncImage(
                        model = ImageRequest.Builder(context).data(drawableId).crossfade(true).build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.FitnessCenter, null, modifier = Modifier.padding(12.dp), tint = OnPrimary)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ejercicio.nombreEjercicio,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnPrimary
                )
                Text(
                    text = "${ejercicio.duracionSegundos}s • ${ejercicio.tipoEjercicio}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Surface
                )
            }

            Checkbox(checked = isSelected, onCheckedChange = { onClick() })
        }
    }
}
