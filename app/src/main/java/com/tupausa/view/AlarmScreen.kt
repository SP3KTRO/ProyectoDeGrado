package com.tupausa.view

import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.tupausa.R
import com.tupausa.model.Ejercicio
import com.tupausa.ui.theme.OnPrimary
import com.tupausa.ui.theme.OnPrimaryContainer
import com.tupausa.ui.theme.OnSecondary
import com.tupausa.ui.theme.OnSurface
import com.tupausa.ui.theme.OnSurfaceVariant
import com.tupausa.ui.theme.Primary
import com.tupausa.ui.theme.Tertiary
import com.tupausa.utils.rememberDrawableId
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    ejercicio: Ejercicio?,
    infoReto: String,
    isManual: Boolean,
    onDismiss: () -> Unit,
    onPosponer: () -> Unit,
    numActual: Int,
    total: Int
) {
    val context = LocalContext.current
    if (ejercicio == null) return

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(
                    GifDecoder.Factory()
                )
            }
            .build()
    }

    var timeLeft by remember(ejercicio.idEjercicio) { mutableFloatStateOf(ejercicio.duracionSegundos.toFloat()) }

    LaunchedEffect(ejercicio.idEjercicio) {
        while (timeLeft > 0) {
            delay(100L)
            timeLeft -= 0.1f
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column {
                    LinearProgressIndicator(
                        progress = { transformProgress(timeLeft / ejercicio.duracionSegundos) },
                        modifier = Modifier.fillMaxWidth().height(13.dp),
                        color = Primary,
                        trackColor = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Ejercicio $numActual de $total",
                        modifier = Modifier
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Tertiary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            },
            bottomBar = {
                Button(
                    onClick = if (isManual) onDismiss else onPosponer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isManual) Tertiary else OnPrimaryContainer)
                ) {
                    Icon(
                        if (isManual) Icons.Default.Stop else Icons.Default.Snooze,
                        contentDescription = null,
                        tint = if (isManual) OnPrimaryContainer else OnPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isManual) "TERMINAR EJERCICIO" else "POSPONER 5 MIN",
                        color = if (isManual) OnPrimaryContainer else OnPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(top = 2.dp, bottom = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = ejercicio.nombreEjercicio,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 22.sp,
                    color = OnSurface,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${timeLeft.toInt()} s",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Spacer(modifier = Modifier.height(3.dp))
                if (infoReto.isNotEmpty()) {
                    Text(
                        text = infoReto,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface,
                        modifier = Modifier
                            .background(
                                color = OnSecondary,
                                RoundedCornerShape(8.dp)
                            ).padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }

                val drawableId = rememberDrawableId(ejercicio.urlImagenGuia)
                Card(
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(280.dp)
                        .wrapContentHeight(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (drawableId != 0) {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(drawableId)
                                    .crossfade(true).build(),
                                imageLoader = imageLoader,
                                contentDescription = "Guía",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .wrapContentHeight()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = OnSecondary
                    )
                ) {
                    Column(modifier = Modifier
                        .padding(12.dp)) {
                        SectionTitle("Cómo realizarlo")
                        ejercicio.getInstruccionesList()
                            .forEachIndexed { index, instruccion ->
                                InstruccionItem(
                                    numero = index + 1,
                                    texto = instruccion
                                )
                            }
                    }
                }
            }
        }
    }
}
@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(bottom = 3.dp),
        color = OnSurfaceVariant
    )
}
@Composable
fun InstruccionItem(numero: Int, texto: String) {
    Row(
        modifier = Modifier
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$numero.",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 16.sp,
            modifier = Modifier
                .width(20.dp),
            color = OnSurfaceVariant
        )
        Text(
            text = texto,
            fontSize = 18.sp,
            lineHeight = 16.sp,
            color = OnSurface
        )
    }
}
fun transformProgress(value: Float): Float { return value.coerceIn(0f, 1f) }