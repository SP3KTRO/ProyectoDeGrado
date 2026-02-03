package com.tupausa.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class FlipDetector(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var listener: (() -> Unit)? = null  // Interfaz para avisar a la Activity
    private var isFaceDown = false

    fun start(onFlip: () -> Unit) {
        this.listener = onFlip
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val z = event.values[2] // Eje z - positivo es pantalla arriba, negativo es pantalla abajo

        // Si z es menor a -8, está boca abajo
        if (z < -8 && !isFaceDown) {
            isFaceDown = true
            listener?.invoke()
        } else if (z > -8) {
            isFaceDown = false
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}