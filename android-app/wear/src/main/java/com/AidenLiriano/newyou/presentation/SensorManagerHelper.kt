package com.AidenLiriano.newyou.presentation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class SensorManagerHelper(private val context: Context) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Heart rate
    var heartRateReadings = mutableListOf<Float>()

    // Step count
    private var initialStepCount = -1f
    var totalSteps = 0

    // Elevation
    private var initialPressure = -1f
    var elevationGainMeters = 0f
    var elevationLossMeters = 0f
    private var lastPressure = -1f

    // GPS distance tracking
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var lastLocation: Location? = null
    var gpsDistanceMeters = 0f
    private var usingGps = false

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            lastLocation?.let { prev ->
                val dist = prev.distanceTo(location)
                // Only count if accuracy is reasonable and distance is plausible
                if (location.accuracy < 30f && dist < 500f) {
                    gpsDistanceMeters += dist
                }
            }
            lastLocation = location
        }
    }

    // Sensors
    private val heartRateSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
    private val stepCountSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val pressureSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

    fun startTracking(
        trackHeartRate: Boolean,
        trackSteps: Boolean,
        trackElevation: Boolean,
        trackGps: Boolean = false
    ) {
        if (trackHeartRate) {
            heartRateSensor?.let {
                sensorManager.registerListener(this, it,
                    SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
        if (trackSteps) {
            stepCountSensor?.let {
                sensorManager.registerListener(this, it,
                    SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
        if (trackElevation) {
            pressureSensor?.let {
                sensorManager.registerListener(this, it,
                    SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
        if (trackGps) {
            startGps()
        }
    }

    private fun startGps() {
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 5000L)
                .setMinUpdateDistanceMeters(5f)
                .build()

            fusedLocationClient?.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
            usingGps = true
        } catch (e: SecurityException) {
            usingGps = false
        }
    }

    fun stopTracking() {
        sensorManager.unregisterListener(this)
        fusedLocationClient?.removeLocationUpdates(locationCallback)
        usingGps = false
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_HEART_RATE -> {
                val hr = event.values[0]
                if (hr > 0) heartRateReadings.add(hr)
            }
            Sensor.TYPE_STEP_COUNTER -> {
                val steps = event.values[0]
                if (initialStepCount < 0) initialStepCount = steps
                totalSteps = (steps - initialStepCount).toInt()
            }
            Sensor.TYPE_PRESSURE -> {
                val pressure = event.values[0]
                if (initialPressure < 0) {
                    initialPressure = pressure
                    lastPressure = pressure
                } else {
                    val currentAlt = SensorManager.getAltitude(
                        SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure)
                    val lastAlt = SensorManager.getAltitude(
                        SensorManager.PRESSURE_STANDARD_ATMOSPHERE, lastPressure)
                    val diff = currentAlt - lastAlt
                    if (diff > 0) elevationGainMeters += diff
                    else elevationLossMeters += Math.abs(diff)
                    lastPressure = pressure
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    fun getAverageHeartRate(): Int {
        return if (heartRateReadings.isEmpty()) 0
        else heartRateReadings.average().toInt()
    }

    fun getStartHeartRate(): Int = heartRateReadings.firstOrNull()?.toInt() ?: 0
    fun getEndHeartRate(): Int   = heartRateReadings.lastOrNull()?.toInt() ?: 0

    // Returns GPS distance if available, otherwise falls back to step estimate
    fun getDistanceKm(strideLengthMeters: Float = 0.762f): Float {
        return if (usingGps && gpsDistanceMeters > 0) {
            gpsDistanceMeters / 1000f
        } else {
            (totalSteps * strideLengthMeters) / 1000f
        }
    }

    fun getPaceMinPerKm(durationSeconds: Long): Float {
        val distanceKm = getDistanceKm()
        return if (distanceKm <= 0) 0f
        else (durationSeconds / 60f) / distanceKm
    }

    fun getSpeedKmh(durationSeconds: Long): Float {
        val distanceKm = getDistanceKm()
        return if (durationSeconds <= 0) 0f
        else distanceKm / (durationSeconds / 3600f)
    }

    fun getCalories(metValue: Float, durationSeconds: Long): Int {
        val durationHours = durationSeconds / 3600f
        return (metValue * 70f * durationHours).toInt()
    }

    fun isUsingGps() = usingGps
}