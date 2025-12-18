package com.tupausa.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberDrawableId(imageName: String?): Int {
    val context = LocalContext.current
    return remember(imageName) {
        if (imageName.isNullOrEmpty()) return@remember 0 // O un icono por defecto

        val resourceId = context.resources.getIdentifier(
            imageName,
            "drawable",
            context.packageName
        )

        // Si no encuentra la imagen
        resourceId
    }
}