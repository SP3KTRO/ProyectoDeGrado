package com.tupausa.view.user

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.tupausa.ui.theme.*
import com.tupausa.viewModel.UsuarioViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    idUsuario: Int,
    usuarioViewModel: UsuarioViewModel,
    onOnboardingComplete: () -> Unit
) {
    val isLoading by usuarioViewModel.isLoading.observeAsState(false)

    // Estado para controlar en qué pregunta vamos (0 a 6)
    var pasoActual by remember { mutableIntStateOf(0) }

    var tieneCondicionCardiaca by remember { mutableStateOf(false) }
    var movilidadInferiorLimitada by remember { mutableStateOf(false) }
    var tieneTunelCarpo by remember { mutableStateOf<Boolean?>(null) }

    val categoriasSuperior = listOf("CUELLO", "HOMBROS", "ESPALDA")
    val seleccionesSuperior = remember { mutableStateMapOf<String, Boolean>().apply { categoriasSuperior.forEach { put(it, false) } } }

    Scaffold(
        containerColor = Color.Transparent)
    { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Crossfade(targetState = pasoActual, label = "onboarding_animation") { paso ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    when (paso) {
                        0 -> PasoBienvenida { pasoActual++ }
                        1 -> PasoOpciones("¿Cuál es tu rango de edad?",
                            listOf("15 a 24 años","25 a 34 años", "35 a 45", "Más de 45 años"))
                        { pasoActual++ }
                        2 -> PasoOpciones("¿Cuál es tu ocupación principal?",
                            listOf("Estudiante de Sistematización de Datos", "Estudiante de Ingeniería en Telemática", "Estudiante de otro programa", "Programador o trabajo en oficina\n(Computador)", "Otro"))
                        { pasoActual++ }
                        3 -> PasoOpciones("¿Cuántas horas pasas al día en el computador?\nYa sea estudiando, trabajando o jugando",
                            listOf("Menos de 4 horas", "De 4 a 6 horas", "De 6 a 8 horas", "Más de 8 horas"))
                        { pasoActual++ }
                        4 -> PasoCondicionCardiaca(
                            onSeleccion = { tieneCondicion ->
                                tieneCondicionCardiaca = tieneCondicion
                                pasoActual++
                            }
                        )
                        5 -> PasoMovilidadInferior(
                            onSeleccion = { tieneLimitacionPiernas ->
                                movilidadInferiorLimitada = tieneLimitacionPiernas
                                pasoActual++
                            }
                        )
                        6 -> PasoLimitacionesSuperior(
                            selecciones = seleccionesSuperior,
                            categorias = categoriasSuperior,
                            onSiguiente = { pasoActual++ }
                        )
                        7 -> PasoTunelCarpo(
                            seleccionActual = tieneTunelCarpo,
                            onSeleccion = { tieneTunelCarpo = it },
                            isLoading = isLoading,
                            onGuardar = {
                                // Juntamos las limitaciones seleccionadas
                                val limitacionesFinales = seleccionesSuperior.filter { it.value }.keys.toMutableList()

                                if (tieneCondicionCardiaca && !limitacionesFinales.contains("CARDIO SUAVE")) {
                                    limitacionesFinales.add("CARDIO SUAVE")
                                }

                                if (movilidadInferiorLimitada) {
                                    if (!limitacionesFinales.contains("PIERNAS")) limitacionesFinales.add("PIERNAS")
                                    if (!limitacionesFinales.contains("CARDIO SUAVE")) limitacionesFinales.add("CARDIO SUAVE")
                                    limitacionesFinales.add("NO_PIE")
                                }

                                if (tieneTunelCarpo == true) {
                                    limitacionesFinales.add("TIENE_TUNEL_CARPO")
                                }
                                val limitacionesString = limitacionesFinales.joinToString(",")

                                usuarioViewModel.guardarPreferenciasOnboarding(
                                    idUsuario = idUsuario,
                                    limitaciones = limitacionesString,
                                    onComplete = { onOnboardingComplete() }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PasoBienvenida(onSiguiente: () -> Unit) {
    Text("¡Bienvenido a TuPausa!",
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = OnPrimaryContainer,
        modifier = Modifier.padding(bottom = 16.dp))
    Text("Vamos a configurar tu perfil para darte las mejores pausas activas según tu rutina y condiciones físicas.😉",
        fontSize = 19.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = 32.dp))
    Button(onClick = onSiguiente,
        colors = ButtonDefaults.buttonColors(containerColor = OnPrimaryContainer),
        modifier = Modifier.fillMaxWidth().height(50.dp)) {
        Text("Comenzar", fontSize = 16.sp, color = OnPrimary)
    }
}
@Composable
fun PasoOpciones(pregunta: String, opciones: List<String>, onOpcionSeleccionada: (String) -> Unit) {
    Text(text = pregunta,
        fontSize = 21.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        color = OnPrimaryContainer,
        modifier = Modifier.padding(bottom = 32.dp))
    opciones.forEach { opcion ->
        OutlinedButton(
            onClick = { onOpcionSeleccionada(opcion) },
            colors = ButtonDefaults.outlinedButtonColors(containerColor = PrimaryContainer),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(55.dp)
        ) {
            Text(opcion, fontSize = 18.sp, color = OnSurface)
        }
    }
}
@Composable
fun PasoMovilidadInferior(onSeleccion: (Boolean) -> Unit) {
    Text("Movilidad Física",
        fontSize = 21.sp,
        fontWeight = FontWeight.Bold,
        color = OnPrimaryContainer,
        modifier = Modifier.padding(bottom = 16.dp))
    Text("¿Tienes alguna discapacidad o limitación grave que afecte la movilidad de tus extremidades inferiores o te impida realizar ejercicios de pie?",
        fontSize = 18.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = 32.dp))

    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly) {
        OutlinedButton(onClick = { onSeleccion(true) },
            modifier = Modifier.weight(1f).padding(end = 8.dp).height(55.dp)) {
            Text("Sí", fontSize = 16.sp, color = OnSurface)
        }
        Button(onClick = { onSeleccion(false) },
            colors = ButtonDefaults.buttonColors(containerColor = OnPrimaryContainer),
            modifier = Modifier.weight(1f).padding(start = 8.dp).height(55.dp)) {
            Text("No", fontSize = 16.sp, color = OnPrimary)
        }
    }
}
@Composable
fun PasoLimitacionesSuperior(
    selecciones: MutableMap<String, Boolean>,
    categorias: List<String>,
    onSiguiente: () -> Unit
) {
    Text("Limitaciones Físicas",
        fontSize = 21.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp),
        color = OnPrimaryContainer)
    Text("¿Tienes alguna condición médica o lesión grave que te impida realizar movimientos de estiramiento en alguna de estas zonas?\nSi no tienes ninguna, solo dale a continuar.",
        fontSize = 18.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = 16.dp))

    LazyColumn(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f)) {
        items(categorias) { categoria ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selecciones[categoria] = !(selecciones[categoria] ?: false) }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = selecciones[categoria] == true, onCheckedChange = null, colors = CheckboxDefaults.colors(OnSurface, OnSurface))
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = categoria.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 18.sp)
            }
            HorizontalDivider(color = OnSurface)
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = onSiguiente,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        colors = ButtonDefaults.buttonColors(OnPrimaryContainer)
    ) {
        Text("Continuar", fontSize = 16.sp, color = OnPrimary)
    }
}
@Composable
fun PasoCondicionCardiaca(onSeleccion: (Boolean) -> Unit) {
    Text("Condición Cardíaca",
        fontSize = 21.sp,
        fontWeight = FontWeight.Bold,
        color = OnPrimaryContainer,
        modifier = Modifier.padding(bottom = 16.dp))
    Text("¿Tienes alguna condición cardíaca diagnosticada que te impida realizar actividad física que acelere tu corazón?",
        fontSize = 18.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = 32.dp))

    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly) {
        OutlinedButton(onClick = { onSeleccion(true) },
            modifier = Modifier.weight(1f).padding(end = 8.dp).height(55.dp)) {
            Text("Sí", fontSize = 16.sp, color = OnSurface)
        }
        Button(onClick = { onSeleccion(false) },
            colors = ButtonDefaults.buttonColors(containerColor = OnPrimaryContainer),
            modifier = Modifier.weight(1f).padding(start = 8.dp).height(55.dp)) {
            Text("No", fontSize = 16.sp, color = OnPrimary)
        }
    }
}

@Composable
fun PasoTunelCarpo(
    seleccionActual: Boolean?,
    onSeleccion: (Boolean) -> Unit,
    isLoading: Boolean,
    onGuardar: () -> Unit
) {
    Text("Cuidado de las Manos",
        fontSize = 21.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp),
        color = OnPrimaryContainer)
    Text("Por último.\n¿Has sido diagnosticado con Síndrome del Túnel Carpiano o sufres de dolor crónico en las muñecas?",
        fontSize = 18.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = 24.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        OutlinedButton(
            onClick = { onSeleccion(true) },
            modifier = Modifier.weight(1f).padding(end = 8.dp).height(55.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (seleccionActual == true) OnPrimaryContainer else Color.Transparent
            )
        ) {
            Text("Sí", color = if (seleccionActual == true) OnPrimary else OnSurface)
        }
        Button(
            onClick = { onSeleccion(false) },
            modifier = Modifier.weight(1f).padding(start = 8.dp).height(55.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (seleccionActual == false) OnPrimaryContainer else PrimaryContainer
            )
        ) {
            Text("No", color = if (seleccionActual == false) OnPrimary else OnSurface)
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    if (seleccionActual == true) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Surface),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = "Info", tint = OnSurface)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Te sugeriremos ejercicios para las muñecas que ayudarán a aliviar la tensión y mejorar tus síntomas",
                    color = OnSurface,
                    fontSize = 16.sp
                )
            }
        }
    } else {

        Spacer(modifier = Modifier.height(100.dp))
    }

    Button(
        onClick = onGuardar,
        colors = ButtonDefaults.buttonColors(containerColor = OnPrimaryContainer),
        modifier = Modifier.fillMaxWidth().height(50.dp),
        enabled = !isLoading && seleccionActual != null
    ) {
        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
        else Text("Finalizar y Guardar", fontSize = 16.sp, color = OnPrimary)
    }
}