package com.tupausa.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class ProximityDetector(context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    private var sensorEventListener: SensorEventListener? = null

    // Función para verificar si el dispositivo tiene este sensor
    fun isAvailable(): Boolean = proximitySensor != null

    fun start(onTrigger: () -> Unit) {
        if (proximitySensor == null) return

        sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val distance = event.values[0]
                // Si la distancia es menor al rango máximo, detectamos "cerca"
                if (distance < proximitySensor.maximumRange) {
                    onTrigger()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(
            sensorEventListener,
            proximitySensor,
            SensorManager.SENSOR_DELAY_UI
        )
    }

    fun stop() {
        sensorEventListener?.let {
            sensorManager.unregisterListener(it)
            sensorEventListener = null
        }
    }
}