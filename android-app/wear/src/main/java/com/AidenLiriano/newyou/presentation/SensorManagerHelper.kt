package com.AidenLiriano.newyou.presentation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class SensorManagerHelper(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Heart rate
    var heartRateReadings = mutableListOf<Float>()

    // Step count
    private var initialStepCount = -1f
    var totalSteps = 0

    // Elevation (via pressure sensor)
    private var initialPressure = -1f
    var elevationGainMeters = 0f
    var elevationLossMeters = 0f
    private var lastPressure = -1f

    // Sensors
    private val heartRateSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
    private val stepCountSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val pressureSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

    fun startTracking(trackHeartRate: Boolean, trackSteps: Boolean, trackElevation: Boolean) {
        if (trackHeartRate) {
            heartRateSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
        if (trackSteps) {
            stepCountSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
        if (trackElevation) {
            pressureSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
    }

    fun stopTracking() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_HEART_RATE -> {
                val hr = event.values[0]
                if (hr > 0) heartRateReadings.add(hr)
            }
            Sensor.TYPE_STEP_COUNTER -> {
                val steps = event.values[0]
                if (initialStepCount < 0) {
                    initialStepCount = steps
                }
                totalSteps = (steps - initialStepCount).toInt()
            }
            Sensor.TYPE_PRESSURE -> {
                val pressure = event.values[0]
                if (initialPressure < 0) {
                    initialPressure = pressure
                    lastPressure = pressure
                } else {
                    // Convert pressure difference to elevation in meters
                    val currentAltitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure)
                    val lastAltitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, lastPressure)
                    val diff = currentAltitude - lastAltitude
                    if (diff > 0) elevationGainMeters += diff
                    else elevationLossMeters += Math.abs(diff)
                    lastPressure = pressure
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    // --- Computed values ---

    fun getAverageHeartRate(): Int {
        return if (heartRateReadings.isEmpty()) 0
        else (heartRateReadings.average()).toInt()
    }

    fun getStartHeartRate(): Int {
        return heartRateReadings.firstOrNull()?.toInt() ?: 0
    }

    fun getEndHeartRate(): Int {
        return heartRateReadings.lastOrNull()?.toInt() ?: 0
    }

    // Distance in km using step count and average stride length (0.762m default)
    fun getDistanceKm(strideLengthMeters: Float = 0.762f): Float {
        return (totalSteps * strideLengthMeters) / 1000f
    }

    // Pace in min/km
    fun getPaceMinPerKm(durationSeconds: Long): Float {
        val distanceKm = getDistanceKm()
        return if (distanceKm <= 0) 0f
        else (durationSeconds / 60f) / distanceKm
    }

    // Speed in km/h (for biking)
    fun getSpeedKmh(durationSeconds: Long): Float {
        val distanceKm = getDistanceKm()
        return if (durationSeconds <= 0) 0f
        else distanceKm / (durationSeconds / 3600f)
    }

    // Calories using MET formula with placeholder 70kg weight
    // calories = MET * weight(kg) * duration(hours)
    fun getCalories(metValue: Float, durationSeconds: Long): Int {
        val durationHours = durationSeconds / 3600f
        return (metValue * 70f * durationHours).toInt()
    }
}