package com.tupausa.utils

import androidx.compose.ui.geometry.Offset
import kotlin.math.abs

class CircleDetector {

    private var points = mutableListOf<Offset>() // Lista de puntos

    // Agregar un punto

    fun addPoint(offset: Offset) {
        points.add(offset)
    }

    fun isCircleDetected(): Boolean {
        if (points.size < 10) return false

        // Buscar los extremos para hallar el centro
        val minX = points.minOf { it.x }
        val maxX = points.maxOf { it.x }
        val minY = points.minOf { it.y }
        val maxY = points.maxOf { it.y }

        val width = maxX - minX
        val height = maxY - minY

        // Verificar que no sea solo una línea
        if (width < 100f || height < 100f) return false

        // Verificar que el inicio y el fin estén relativamente cerca y que se cierre el círculo
        val start = points.first()
        val end = points.last()
        val distanceStartEnd = Math.sqrt(
            Math.pow((end.x - start.x).toDouble(), 2.0) +
                    Math.pow((end.y - start.y).toDouble(), 2.0)
        )

        val isClosed = distanceStartEnd < (width / 2)

        if (isClosed) {
            points.clear()
            return true
        }

        return false
    }

    fun reset() {
        points.clear()
    }
}