package com.tupausa.utils

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

class LuminosityAnalyzer(private val onLuminosityChanged: (Double) -> Unit) : ImageAnalysis.Analyzer {

    override fun analyze(image: ImageProxy) {
        val buffer = image.planes[0].buffer
        val data = ByteArray(buffer.remaining())
        buffer.get(data)

        // Calculamos el promedio de brillo
        var total = 0L
        for (byte in data) {
            total += (byte.toInt() and 0xFF)
        }
        val luma = total.toDouble() / data.size

        // LOG PARA DEBUG: Verás esto en tu Logcat
        Log.d("LumaAnalyzer", "Brillo detectado: $luma")

        onLuminosityChanged(luma)

        image.close()
    }
}