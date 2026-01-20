package com.tupausa.view.admin

import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.tupausa.model.Ejercicio
import com.tupausa.utils.rememberDrawableId
import com.tupausa.view.user.InfoBadge
import com.tupausa.view.user.InstruccionItem
import com.tupausa.view.user.SectionTitle
import com.tupausa.alarm.AlarmActivity
import com.tupausa.ui.theme.OnPrimary
import com.tupausa.ui.theme.OnPrimaryContainer
import com.tupausa.ui.theme.OnSurface
import com.tupausa.ui.theme.Primary
import com.tupausa.ui.theme.PrimaryContainer
import com.tupausa.ui.theme.Secondary
import com.tupausa.ui.theme.Surface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEjercicioDetalleScreen(
    ejercicio: Ejercicio,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Configuración de Coil para el GIF
    val drawableId = rememberDrawableId(ejercicio.urlImagenGuia)
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Detalles del Ejercicio") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header con GIF
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
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
                            imageLoader = imageLoader,
                            contentDescription = ejercicio.nombreEjercicio,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = OnSurface
                            )
                            Text("Vista previa no disponible", color = OnSurface )
                        }
                    }
                }
            }

            // Información Detallada
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Secondary
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Titulo
                    Text(
                        text = ejercicio.nombreEjercicio,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnPrimary
                    )
                    // Badges
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        InfoBadge(icon = Icons.Default.Category, text = ejercicio.getTipoDisplayName())
                        InfoBadge(icon = Icons.Default.Timer, text = "${ejercicio.duracionSegundos}s")
                        InfoBadge(icon = Icons.AutoMirrored.Filled.TrendingUp, text = ejercicio.getNivelDisplayName())
                    }

                    HorizontalDivider()
                    // Descripción
                    SectionTitle("Descripción")
                    Text(
                        text = ejercicio.descripcion,
                        fontSize = 16.sp,
                        color = OnPrimary
                    )

                    HorizontalDivider()
                    // Instrucciones
                    SectionTitle("Instrucciones")
                    ejercicio.getInstruccionesList().forEachIndexed { index, instruccion ->
                        InstruccionItem(numero = index + 1, texto = instruccion)
                    }

                    if (!ejercicio.beneficios.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Surface
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = null,
                                    tint = OnSurface
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = ejercicio.beneficios,
                                    fontSize = 14.sp,
                                    color = OnSurface
                                )
                            }
                        }
                    }
                }
            }

            // BOTÓN DE COMENZAR EJERCICIO
            Button(
                onClick = {
                    val intent = Intent(context, AlarmActivity::class.java).apply {
                        putExtra("IS_MANUAL", true)
                        putIntegerArrayListExtra("ALARM_IDS_RUTINA", arrayListOf(ejercicio.idEjercicio))
                        putExtra("ALARM_NOMBRE", ejercicio.nombreEjercicio)
                        putExtra("ALARM_TIPO", ejercicio.tipoEjercicio)
                        putExtra("ALARM_DURACION", ejercicio.duracionSegundos)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = OnPrimaryContainer
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null,
                    tint = OnPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Comenzar Ejercicio", fontSize = 18.sp,
                    color = OnPrimary)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}