package com.tupausa.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Configuración de sensibilidad
    private val SHAKE_THRESHOLD_GRAVITY = 2.7F // Qué tan fuerte hay que agitar
    private val SHAKE_SLOP_TIME_MS = 500 // Tiempo mínimo entre sacudidas
    private val SHAKE_COUNT_RESET_TIME_MS = 3000 // Si deja de agitar, se reinicia

    private var mListener: OnShakeListener? = null
    private var mShakeTimestamp: Long = 0 // Timestamp de la última sacudida
    private var mShakeCount = 0 // Contador de sacudidas

    // Interfaz para avisar a la Activity
    interface OnShakeListener {
        fun onShake(count: Int)
    }
     // Asignar Listener
    fun setOnShakeListener(listener: OnShakeListener) {
        this.mListener = listener
    }

    fun start() {
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    // Eventos de sensores
    override fun onSensorChanged(event: SensorEvent) {
        if (mListener == null) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val gX = x / SensorManager.GRAVITY_EARTH
        val gY = y / SensorManager.GRAVITY_EARTH
        val gZ = z / SensorManager.GRAVITY_EARTH

        // Calcular fuerza g neta
        val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

        if (gForce > SHAKE_THRESHOLD_GRAVITY) {
            val now = System.currentTimeMillis()

            // Ignorar si las sacudidas son muy seguidas
            if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                return
            }

            // Reiniciar contador
            if (mShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                mShakeCount = 0
            }

            mShakeTimestamp = now
            mShakeCount++

            mListener?.onShake(mShakeCount)
        }
    }
}