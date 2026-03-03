package com.tupausa.view.user

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tupausa.model.data.Alarma
import com.tupausa.model.Ejercicio
import com.tupausa.model.data.TonosDisponibles
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
import com.tupausa.utils.PreferencesManager
import com.tupausa.utils.rememberDrawableId

private enum class DialogStep { FORMULARIO, TIPO_RUTINA, SELECCION_CATEGORIAS, SELECCION_EJERCICIOS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmaFormDialog(
    context: Context,
    alarmaAEditar: Alarma? = null,
    listaEjercicios: List<Ejercicio>,
    listaArlarmas: List<Alarma>,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, List<Int>, String, String, String, List<Int>) -> Unit
) {
    // Preparación de estado del tono
    val preferencesManager = remember { PreferencesManager(context) }

    val tieneTunelCarpo = remember {
        preferencesManager.getLimitaciones().contains("TIENE_TUNEL_CARPO")
    }

    val idTonoInicial = remember {
        if (alarmaAEditar != null) {
            TonosDisponibles.lista.find { it.nombre == alarmaAEditar.tonoAlarma }?.recurso
                ?: TonosDisponibles.lista.first().recurso
        } else {
            preferencesManager.getSelectedTone(TonosDisponibles.lista.first().recurso)
        }
    }

    // Estados del formulario
    val timeState = rememberTimePickerState(
        initialHour = alarmaAEditar?.hora ?: 8,
        initialMinute = alarmaAEditar?.minuto ?: 0
    )
    var etiqueta by remember { mutableStateOf(alarmaAEditar?.etiqueta ?: "") }

    // Tonos
    var selectedToneId by remember { mutableIntStateOf(idTonoInicial) }
    var showTonoDialog by remember { mutableStateOf(false) }

    // Info
    var showIntervaloInfo by remember { mutableStateOf(false) }

    // Helper para obtener el nombre actual basado en el ID seleccionado
    val nombreTonoActual = TonosDisponibles.lista.find { it.recurso == selectedToneId }?.nombre ?: "Predeterminado"

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

    var currentStep by remember { mutableStateOf(DialogStep.FORMULARIO) }

    // Categorías disponibles
    val categoriasDisponibles = remember(listaEjercicios) { listaEjercicios.map { it.tipoEjercicio }.distinct() }
    var esRutinaPersonalizada by remember { mutableStateOf(false) }
    val categoriasSeleccionadas = remember { mutableStateListOf<String>() }
    var zonaMostrandoMensaje by remember { mutableStateOf<String?>(null) }

    // Precargar datos si estamos editando
    LaunchedEffect(alarmaAEditar, listaEjercicios) {
        if (alarmaAEditar != null && listaEjercicios.isNotEmpty()) {
            val ejerciciosDeAlarma = listaEjercicios.filter { it.idEjercicio in alarmaAEditar.idsEjercicios }
            val cats = ejerciciosDeAlarma.map { it.tipoEjercicio }.distinct()
            categoriasSeleccionadas.clear()
            categoriasSeleccionadas.addAll(cats)
            // Si tiene todas las categorías, asumimos que es personalizada
            esRutinaPersonalizada = cats.size == categoriasDisponibles.size
        }
    }

    // Validación de ejercicios
    val mensajeValidacionEjercicios by remember {
        derivedStateOf {
            if (selectedExerciseIds.size < 4) "Mínimo 4 ejercicios en total (${selectedExerciseIds.size}/4)."
            else
                "" // Vacío significa que es correcto
        }
    }
    // Validación de alarmas - Tiempo y días
    val mensajeValidacionTiempo by remember {
        derivedStateOf {
            val minutosNuevos = timeState.hour * 60 + timeState.minute
            val alarmasAComparar = listaArlarmas.filter { it.id != (alarmaAEditar?.id ?: -1) }

            for (alarma in alarmasAComparar) {
                val diasAlarmaExistente = alarma.diasRepeticion

                val diasCoinciden = when {
                    selectedDays.isEmpty() || diasAlarmaExistente.isEmpty() -> true
                    else -> selectedDays.intersect(diasAlarmaExistente.toSet()).isNotEmpty()
                }

                if (diasCoinciden) {
                    val minutosExistente = alarma.hora * 60 + alarma.minuto
                    val diffAbs = kotlin.math.abs(minutosNuevos - minutosExistente)
                    val diffReal = kotlin.math.min(diffAbs, 1440 - diffAbs)

                    if (diffReal == 0) {
                        return@derivedStateOf "Ya tienes una rutina exactamente a esta hora en esos días."
                    } else if (diffReal < 90) {
                        val horaConflicto = String.format("%02d:%02d", alarma.hora, alarma.minuto)
                        return@derivedStateOf "Debes dejar 90 min de descanso. Choca con rutina de $horaConflicto."
                    }
                }
            }
            "" // Vacío significa que el horario es válido
        }
    }

    // Diálogo informativo sobre las alarmas
    if (showIntervaloInfo) {
        AlertDialog(
            properties = DialogProperties(dismissOnClickOutside = false),
            onDismissRequest = { showIntervaloInfo = false },
            containerColor = Secondary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Tertiary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Tiempo de Descanso", color = Surface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "Para que tus pausas activas sean realmente efectivas y cuides tu salud, te recomendamos dejar un intervalo mínimo de 90 minutos entre cada rutina.\n\n¡Evita la fatiga y mantén tu energía al máximo durante tu jornada!",
                    color = OnPrimary,
                    fontSize = 16.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showIntervaloInfo = false },
                    colors = ButtonDefaults.buttonColors(containerColor = OnPrimaryContainer)
                ) {
                    Text("Entendido", color = OnPrimary)
                }
            }
        )
    }

    // Mensaje Categoria
    if (zonaMostrandoMensaje != null) {
        AlertDialog(
            properties = DialogProperties(dismissOnClickOutside = false),
            onDismissRequest = { zonaMostrandoMensaje = null },
            containerColor = Secondary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.padding(end = 8.dp))
                    Text("Recomendación", color = Surface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = { Text(obtenerMensajeZona(zonaMostrandoMensaje!!), color = OnPrimary, fontSize = 16.sp) },
            confirmButton = {
                Button(
                    onClick = { zonaMostrandoMensaje = null },
                    colors = ButtonDefaults.buttonColors(containerColor = OnPrimaryContainer)
                ) {
                    Text("OK", color = OnPrimary)
                }
            }
        )
    }

    AlertDialog(
        properties = DialogProperties(dismissOnClickOutside = false),
        containerColor = Secondary,
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f).heightIn(max = 750.dp),
        confirmButton = {
            when (currentStep) {
                DialogStep.FORMULARIO -> {
                    // Validaciones para crear y guardar la alarma
                    val puedeGuardar = mensajeValidacionEjercicios.isEmpty() && mensajeValidacionTiempo.isEmpty()
                        Button(
                            enabled = puedeGuardar,
                            onClick = {
                                preferencesManager.setSelectedTone(selectedToneId)
                                onConfirm(
                                    timeState.hour, timeState.minute, selectedDays.toList(),
                                    etiqueta.ifEmpty { "Mi Rutina" }, "RUTINA", nombreTonoActual, selectedExerciseIds.toList()
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OnPrimaryContainer)
                        ) {
                            Text(if (alarmaAEditar == null) "Crear" else "Guardar", color = OnPrimary)
                        }
                }
                DialogStep.TIPO_RUTINA -> {
                    Button(
                        onClick = {
                            if (esRutinaPersonalizada) currentStep = DialogStep.SELECCION_EJERCICIOS
                            else currentStep = DialogStep.SELECCION_CATEGORIAS
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OnPrimaryContainer)
                    ) {
                        Text("Siguiente", color = OnPrimary)
                    }
                }
                DialogStep.SELECCION_CATEGORIAS -> {
                    Button(
                        enabled = categoriasSeleccionadas.size >= 2,
                        onClick = { currentStep = DialogStep.SELECCION_EJERCICIOS },
                        colors = ButtonDefaults.buttonColors(containerColor = OnPrimaryContainer)
                    ) {
                        Text("Siguiente", color = OnPrimary)
                    }
                }
                DialogStep.SELECCION_EJERCICIOS -> {
                    Column(horizontalAlignment = Alignment.End) {
                        if (mensajeValidacionEjercicios.isNotEmpty()) {
                            Text(mensajeValidacionEjercicios, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
                        }
                        Button(
                            enabled = mensajeValidacionEjercicios.isEmpty(),
                            onClick = { currentStep = DialogStep.FORMULARIO },
                            colors = ButtonDefaults.buttonColors(containerColor = OnPrimaryContainer)
                        ) {
                            Text("Listo (${selectedExerciseIds.size})", color = OnPrimary)
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                when (currentStep) {
                    DialogStep.SELECCION_EJERCICIOS -> {
                        if (esRutinaPersonalizada) currentStep = DialogStep.TIPO_RUTINA
                        else currentStep = DialogStep.SELECCION_CATEGORIAS
                    }
                    DialogStep.SELECCION_CATEGORIAS -> currentStep = DialogStep.TIPO_RUTINA
                    DialogStep.TIPO_RUTINA -> currentStep = DialogStep.FORMULARIO
                    DialogStep.FORMULARIO -> onDismiss()
                }
            }) {
                Text(if (currentStep == DialogStep.FORMULARIO) "Cancelar" else "Atrás", color = OnPrimary)
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (currentStep) {
                        DialogStep.FORMULARIO -> if (alarmaAEditar == null) "Nueva Alarma" else "Editar Alarma"
                        DialogStep.TIPO_RUTINA -> "Tipo de Rutina"
                        DialogStep.SELECCION_CATEGORIAS -> "Selecciona las categorías"
                        DialogStep.SELECCION_EJERCICIOS -> "Arma tu rutina"
                    },
                    color = OnPrimary,
                    modifier = Modifier.weight(1f)
                )

                // Botón de información en la esquina superior derecha
                IconButton(
                    onClick = { showIntervaloInfo = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Información de intervalos",
                        tint = Tertiary
                    )
                }
            }
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
                                    // AM/PM
                                    periodSelectorSelectedContainerColor = OnPrimaryContainer,
                                    periodSelectorUnselectedContainerColor = Gris,
                                    // Hora y minutos
                                    timeSelectorSelectedContainerColor = OnPrimaryContainer,
                                    timeSelectorUnselectedContainerColor = Gris,
                                )
                            )
                            // Seleeción de rutina
                            OutlinedCard(
                                onClick = { currentStep = DialogStep.TIPO_RUTINA },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.outlinedCardColors(containerColor = Color.Transparent)
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Rutina seleccionada:", fontSize = 12.sp, color = OnPrimary)
                                        Text(
                                            text = if (selectedExerciseIds.isEmpty()) "Ningún ejercicio"
                                            else "${selectedExerciseIds.size} ejercicios seleccionados",
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedExerciseIds.size < 4) Gris else Tertiary
                                        )
                                    }
                                    Icon(Icons.Default.Edit, "Cambiar", tint = OnPrimary)
                                }
                            }

                            // Selección de tono
                            OutlinedCard(
                                onClick = { showTonoDialog = true }, // Abre el popup
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.outlinedCardColors(containerColor = Color.Transparent)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Sonido de alarma:", fontSize = 12.sp, color = OnPrimary)
                                        Text(
                                            text = nombreTonoActual,
                                            fontWeight = FontWeight.Bold,
                                            color = Tertiary
                                        )
                                    }
                                    Icon(Icons.Default.MusicNote, "Cambiar Tono", tint = OnPrimary)
                                }
                            }

                            // Etiqueta
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
                            if (mensajeValidacionTiempo.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = mensajeValidacionTiempo,
                                    color = Color.Red,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                                )
                            }
                        }
                    }
                    DialogStep.TIPO_RUTINA -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("¿Cómo quieres armar tu rutina?",
                                style = MaterialTheme.typography.bodyLarge,
                                fontSize = 20.sp,
                                color = OnPrimary,
                                modifier = Modifier.padding(bottom = 16.dp))

                            if (tieneTunelCarpo) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Surface),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = OnSurface)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("💡 Recomendación:\nHaz ejercicios de muñecas.",
                                            color = OnSurface, fontSize = 13.sp, lineHeight = 18.sp)
                                    }
                                }
                            }

                            Card(
                                onClick = { esRutinaPersonalizada = false },
                                colors = CardDefaults.cardColors(containerColor = if (!esRutinaPersonalizada) OnPrimaryContainer.copy(alpha = 0.2f) else Color.Transparent),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                                    RadioButton(selected = !esRutinaPersonalizada, onClick = { esRutinaPersonalizada = false })
                                    Column {
                                        Text("Por zonas específicas",
                                            color = OnPrimary,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold)
                                        Text("Crea tu rutina de acuerdo a la zonas del cuerpo que desees trabajar.",
                                            color = Surface,
                                            fontSize = 14.sp)
                                    }
                                }
                            }

                            Card(
                                onClick = { esRutinaPersonalizada = true },
                                colors = CardDefaults.cardColors(containerColor = if (esRutinaPersonalizada) OnPrimaryContainer.copy(alpha = 0.2f) else Color.Transparent),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                                    RadioButton(selected = esRutinaPersonalizada, onClick = { esRutinaPersonalizada = true })
                                    Column {
                                        Text("Personalizada",
                                            color = OnPrimary,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold)
                                        Text("Verás la lista de todos los ejercicios.",
                                            color = Surface, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }

                    DialogStep.SELECCION_CATEGORIAS -> {
                        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                            Text("Selecciona mínimo 2 zonas del cuerpo a trabajar:",
                                fontSize = 16.sp, color = OnPrimary, modifier = Modifier.padding(bottom = 16.dp))

                            categoriasDisponibles.forEach { cat ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Secondary)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = categoriasSeleccionadas.contains(cat),
                                            onCheckedChange = { isChecked ->
                                                if (isChecked) {
                                                    categoriasSeleccionadas.add(cat)
                                                } else {
                                                    categoriasSeleccionadas.remove(cat)
                                                }
                                            }
                                        )
                                        Text(
                                            text = getNombreAmigable(cat),
                                            color = OnPrimary,
                                            fontWeight = if (categoriasSeleccionadas.contains(cat)) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 16.sp
                                        )

                                        Spacer(modifier = Modifier.weight(1f))

                                        IconButton(onClick = { zonaMostrandoMensaje = cat }) {
                                            Icon(
                                                imageVector = Icons.Default.HelpOutline,
                                                contentDescription = "Información de la zona",
                                                tint = Tertiary
                                            )
                                        }
                                    }
                                }
                            }
                            if (categoriasSeleccionadas.size < 2) {
                                Text("Aún necesitas seleccionar ${2 - categoriasSeleccionadas.size} zona(s) más.", color = Tertiary, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }

                    DialogStep.SELECCION_EJERCICIOS -> {
                        val ejerciciosAMostrar = if (esRutinaPersonalizada) listaEjercicios else listaEjercicios.filter { it.tipoEjercicio in categoriasSeleccionadas }
                        val ejerciciosAgrupados = ejerciciosAMostrar.groupBy { it.tipoEjercicio }

                        Column {
                            Box(modifier = Modifier.heightIn(max = 400.dp)) {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ejerciciosAgrupados.forEach { (tipo, lista) ->
                                        item {
                                            Text(getNombreAmigable(tipo), fontWeight = FontWeight.Bold, color = Tertiary, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                                        }
                                        items(lista) { ejercicio ->
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
        }
    )
    if (showTonoDialog) {
        SelectorTonoDialog(
            tonoIdActual = selectedToneId,
            onDismiss = { showTonoDialog = false },
            onTonoSeleccionado = { nuevoId ->
                selectedToneId = nuevoId
                showTonoDialog = false
            }
        )
    }
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

fun obtenerMensajeZona(zona: String): String {
    return when (zona.uppercase()) {
        "CUELLO" -> "Estos ejercicios son ideales si sientes tensión en el cuello por mirar la pantalla durante largos periodos."
        "HOMBROS" -> "Ejercicios recomendados para aliviar la rigidez en los hombros causada por mantener una postura encorvada."
        "MUÑECAS" -> "Estos ejercicios ayudan a reducir la sobrecarga en las muñecas provocada por el uso frecuente del teclado o el mouse."
        "ESPALDA" -> "Ejercicios ideales para disminuir la tensión lumbar asociada a permanecer sentado por mucho tiempo."
        "PIERNAS" -> "Estos ejercicios favorecen la circulación y previenen la rigidez muscular tras largos periodos de inactividad."
        "OJOS" -> "Estos ejercicios contribuyen a reducir la fatiga visual generada por la exposición prolongada a pantallas."
        "RESPIRACIÓN" -> "Estos ejercicios ayudan a disminuir la tensión acumulada y mejorar la oxigenación durante la jornada."
        "CARDIO SUAVE" -> "Ejercicios recomendados para activar el cuerpo y contrarrestar el sedentarismo prolongado."
        else -> "Ejercicios clave para esta zona del cuerpo."
    }
}
@Composable
fun SelectorTonoDialog(
    tonoIdActual: Int,
    onDismiss: () -> Unit,
    onTonoSeleccionado: (Int) -> Unit
) {
    val context = LocalContext.current

    var seleccionTemporal by remember {
        mutableStateOf(
            TonosDisponibles.lista.find { it.recurso == tonoIdActual }
                ?: TonosDisponibles.lista.first()
        )
    }

    // MediaPlayer para la previsualización
    val mediaPlayer = remember { MediaPlayer() }

    fun reproducirPreview(resId: Int) {
        try {
            if (mediaPlayer.isPlaying) mediaPlayer.stop()
            mediaPlayer.reset()

            // Abrimos el recurso raw usando el ID
            val afd = context.resources.openRawResourceFd(resId)
            if (afd != null) {
                mediaPlayer.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                mediaPlayer.prepare()
                mediaPlayer.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Limpieza al cerrar el dialog
    DisposableEffect(Unit) {
        onDispose {
            if (mediaPlayer.isPlaying) mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    AlertDialog(
        properties = DialogProperties(dismissOnClickOutside = false),
        onDismissRequest = onDismiss,
        title = { Text("Elige un tono", color = OnPrimary) },
        containerColor = Secondary,
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
            ) {
                items(TonosDisponibles.lista) { tono ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                seleccionTemporal = tono
                                reproducirPreview(tono.recurso)
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (tono == seleccionTemporal),
                            onClick = {
                                seleccionTemporal = tono
                                reproducirPreview(tono.recurso)
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = tono.nombre,
                            style = MaterialTheme.typography.bodyLarge,
                            color = OnPrimary
                        )

                        if (tono == seleccionTemporal) {
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Sonando",
                                tint = OnPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onTonoSeleccionado(seleccionTemporal.recurso)
                onDismiss()
            }) { Text("Aceptar", color = Tertiary) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = OnPrimary) }
        }
    )
}

