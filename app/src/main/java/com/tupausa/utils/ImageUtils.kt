package com.tupausa.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
// Función para obtener el ID de un drawable a partir de su nombre
fun rememberDrawableId(imageName: String?): Int {
    val context = LocalContext.current
    return remember(imageName) {
        if (imageName.isNullOrEmpty()) return@remember 0 // Icono por defecto

        val resourceId = context.resources.getIdentifier(
            imageName,
            "drawable",
            context.packageName
        )
        resourceId
    }
}