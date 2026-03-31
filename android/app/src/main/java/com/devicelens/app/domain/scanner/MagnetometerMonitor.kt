package com.devicelens.app.domain.scanner

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.math.pow
import kotlin.math.sqrt

class MagnetometerMonitor @Inject constructor(
    private val sensorManager: SensorManager
) {
    data class MagnetometerReading(
        val baselineMagnitude: Float,
        val peakMagnitude: Float,
        val anomalyDetected: Boolean
    )

    suspend fun sample(durationMs: Long = 3000): MagnetometerReading {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            ?: return MagnetometerReading(0f, 0f, false)

        val readings = mutableListOf<Float>()

        return suspendCancellableCoroutine { continuation ->
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val magnitude = sqrt(
                        event.values[0].pow(2) +
                        event.values[1].pow(2) +
                        event.values[2].pow(2)
                    )
                    readings.add(magnitude)
                }
                override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
            }

            sensorManager.registerListener(
                listener, sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )

            CoroutineScope(Dispatchers.IO).launch {
                delay(durationMs)
                sensorManager.unregisterListener(listener)
                val baseline = if (readings.isNotEmpty()) readings.average().toFloat() else 0f
                val peak = readings.maxOrNull() ?: 0f
                if (continuation.isActive) {
                    continuation.resume(
                        MagnetometerReading(
                            baselineMagnitude = baseline,
                            peakMagnitude = peak,
                            anomalyDetected = peak > 80f
                        )
                    )
                }
            }

            continuation.invokeOnCancellation {
                sensorManager.unregisterListener(listener)
            }
        }
    }
}
