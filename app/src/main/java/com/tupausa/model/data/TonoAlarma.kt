package com.tupausa.model.data

import androidx.annotation.RawRes
import com.tupausa.R

data class TonoAlarma(
    val nombre: String,
    @RawRes val recurso: Int
)

object TonosDisponibles {
    val lista = listOf(
        TonoAlarma("Predeterminado", R.raw.jazz_suave),
        TonoAlarma("EDM", R.raw.edm),
        TonoAlarma("Funk", R.raw.funk),
        TonoAlarma("Hip Hop", R.raw.hip_hop),
        TonoAlarma("Jazz", R.raw.jazz_suave),
        TonoAlarma("Naturaleza", R.raw.naturaleza),
        TonoAlarma("Rock", R.raw.rock),
    )
}