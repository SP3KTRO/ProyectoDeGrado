package com.tupausa.view.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tupausa.model.data.Alarma

@Composable
fun AlarmaCard(
    alarma: Alarma,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Hora Grande
                Text(
                    text = String.format("%02d:%02d", alarma.hora, alarma.minuto),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (alarma.activa) MaterialTheme.colorScheme.onSurface else Color.Gray
                )

                // Nombre y tipo
                Text(
                    text = alarma.etiqueta,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Días
                Text(
                    text = if (alarma.diasRepeticion.isEmpty()) "Una vez" else "Días programados",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                // Información de la Rutina (PUNTO 4 IMPLEMENTADO)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (alarma.idsEjercicios.size < 4) Color.Red else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Rutina: ${alarma.idsEjercicios.size} ejercicios",
                        fontSize = 12.sp,
                        color = if (alarma.idsEjercicios.size < 4) Color.Red else Color.Gray
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Switch Activar/Desactivar
                Switch(
                    checked = alarma.activa,
                    onCheckedChange = onToggle
                )
                // Botón eliminar
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Gray)
                }
            }
        }
    }
}