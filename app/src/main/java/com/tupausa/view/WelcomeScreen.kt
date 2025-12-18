package com.tupausa.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tupausa.R
import kotlinx.coroutines.delay

@Composable
fun ScreenWelcome(onNavigateToLogin: () -> Unit) {
    val imagePainter: Painter = painterResource(id = R.drawable.welcome)

    // Ejecutar la navegación automáticamente
    LaunchedEffect(Unit) {
        delay(3700)
        onNavigateToLogin()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Fondo de la pantalla
        Image(
            painter = imagePainter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenWelcomePreview() {
    ScreenWelcome(onNavigateToLogin = {})
}
