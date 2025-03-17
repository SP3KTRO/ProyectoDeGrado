package com.tupausa.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tupausa.model.Usuario

@Composable
fun UsuarioList(usuarios: List<Usuario>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(usuarios) { usuario ->
            Text(
                text = "Nombre: ${usuario.nombre}, Correo: ${usuario.correoElectronico}",
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}