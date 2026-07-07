package com.AidenLiriano.newyou.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.AidenLiriano.newyou.R
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// App color constants
val AppBackground = ComposeColor(0xFFE5E0AA)
val AppGreen      = ComposeColor(0xFF98CD00)
val AppCard       = ComposeColor(0xFFFFFADC)
val AppText       = ComposeColor(0xFF1C1C1E)
val AppRed        = ComposeColor(0xFFEF5350)
val AppOrange     = ComposeColor(0xFFFFA726)
val AppDarkGreen  = ComposeColor(0xFF7DB800)

const val CUSTOM_WORKOUT_TYPE_BASE = 100

data class GuidedWorkoutPlan(
    val workoutTypeId: Int,
    val tierName: String,
    val goals: List<String>
)

data class WorkoutGoals(
    val targetDurationSeconds: Long,
    val targetHeartRate: Int,
    val targetSteps: Int,
    val targetCalories: Int,
    val targetDistance: Float
)

data class WorkoutType(
    val name: String,
    val typeId: Int,
    val iconRes: Int,
    val isCustom: Boolean = false,
    val trackSteps: Boolean = false,
    val trackDistance: Boolean = false,
    val trackElevation: Boolean = false,
    val trackLaps: Boolean = false,
    val trackSpeed: Boolean = false
)

val builtInWorkouts = listOf(
    WorkoutType("Running",    1, R.drawable.ic_running,    trackSteps = true),
    WorkoutType("Swimming",   2, R.drawable.ic_swimming,   trackLaps = true),
    WorkoutType("Biking",     3, R.drawable.ic_biking,     trackDistance = true, trackSpeed = true),
    WorkoutType("Walking",    4, R.drawable.ic_walking,    trackSteps = true),
    WorkoutType("Hiking",     5, R.drawable.ic_hiking,     trackSteps = true, trackElevation = true),
    WorkoutType("Meditation", 6, R.drawable.ic_meditation),
    WorkoutType("Strength",   7, R.drawable.ic_strength),
    WorkoutType("Yoga",       8, R.drawable.ic_yoga)
)

val iconResources = listOf(
    R.drawable.ic_running, R.drawable.ic_swimming, R.drawable.ic_biking,
    R.drawable.ic_walking, R.drawable.ic_hiking,   R.drawable.ic_meditation,
    R.drawable.ic_strength, R.drawable.ic_yoga
)

val metValues = mapOf(
    1 to 9.8f, 2 to 6.0f, 3 to 7.5f, 4 to 3.5f,
    5 to 6.0f, 6 to 2.5f, 7 to 5.0f, 8 to 3.0f
)

sealed class Screen {
    object Selection : Screen()
    data class Countdown(val workout: WorkoutType) : Screen()
    data class ActiveWorkout(val workout: WorkoutType, val activityId: Int) : Screen()
}

class MainActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {

    companion object {
        var instance: MainActivity? = null
    }

    var pendingActivityId    by mutableStateOf(-1)
    var pendingGuidedWorkout by mutableStateOf<GuidedWorkoutPlan?>(null)
    var pendingAutoStart     by mutableStateOf(false)
    var customWorkouts       by mutableStateOf<List<WorkoutType>>(emptyList())

    var currentScreen        by mutableStateOf<Screen>(Screen.Selection)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        instance = this
        WorkoutTrackingService.sensorHelper = SensorManagerHelper(this)

        WearListenerService.pendingGuidedWorkout?.let {
            pendingGuidedWorkout = it
        }
        WearListenerService.pendingCustomWorkouts?.let {
            receiveCustomWorkouts(it)
        }

        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.BODY_SENSORS,
                Manifest.permission.ACTIVITY_RECOGNITION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )

        setContent {
            WearApp(activity = this)
        }
    }

    override fun onResume() {
        super.onResume()
        instance = this
        Wearable.getMessageClient(this).addListener(this)
        requestCustomWorkoutsFromPhone()

        if (WearListenerService.pendingAutoStart
            && pendingGuidedWorkout != null
            && currentScreen is Screen.Selection) {
            val workout = builtInWorkouts.find {
                it.typeId == pendingGuidedWorkout!!.workoutTypeId
            } ?: customWorkouts.find {
                it.typeId == pendingGuidedWorkout!!.workoutTypeId
            }
            if (workout != null) {
                currentScreen = Screen.Countdown(workout)
                WearListenerService.pendingAutoStart = false
                pendingAutoStart = false
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Wearable.getMessageClient(this).removeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance === this) instance = null
    }

    fun receiveCustomWorkouts(data: String) {
        if (data.isEmpty() || data == "NONE") {
            customWorkouts = emptyList()
            return
        }
        val parsed = mutableListOf<WorkoutType>()
        data.split(";").forEach { entry ->
            val f = entry.split("|")
            if (f.size >= 8) {
                val iconIdx = f[1].toIntOrNull() ?: 0
                val cwId    = f[7].toIntOrNull() ?: 0
                parsed.add(WorkoutType(
                    name           = f[0],
                    typeId         = CUSTOM_WORKOUT_TYPE_BASE + cwId,
                    iconRes        = iconResources.getOrElse(iconIdx) { R.drawable.ic_running },
                    isCustom       = true,
                    trackSteps     = f[2] == "1",
                    trackDistance  = f[3] == "1",
                    trackElevation = f[4] == "1",
                    trackLaps      = f[5] == "1",
                    trackSpeed     = f[6] == "1"
                ))
            }
        }
        customWorkouts = parsed
    }

    private fun requestCustomWorkoutsFromPhone() {
        kotlinx.coroutines.MainScope().launch {
            try {
                val nodes = Wearable.getNodeClient(this@MainActivity)
                    .connectedNodes.await()
                for (node in nodes) {
                    Wearable.getMessageClient(this@MainActivity)
                        .sendMessage(node.id,
                            "/request_custom_workouts", ByteArray(0)).await()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val path = messageEvent.path
        when {
            path.startsWith("/activity_id_response/") -> {
                val id = path.replace("/activity_id_response/", "").toIntOrNull()
                if (id != null) pendingActivityId = id
            }
            path == "/guided_workout" -> {
                val data  = String(messageEvent.data)
                val parts = data.split("|")
                if (parts.size >= 3) {
                    val typeId   = parts[0].toIntOrNull() ?: return
                    val tierName = parts[1]
                    val goals    = parts[2].split("~")
                    pendingGuidedWorkout = GuidedWorkoutPlan(typeId, tierName, goals)

                    // Auto-start: find the matching workout and go to countdown
                    val workout = builtInWorkouts.find { it.typeId == typeId }
                        ?: customWorkouts.find { it.typeId == typeId }
                    if (workout != null && currentScreen is Screen.Selection) {
                        currentScreen = Screen.Countdown(workout)
                    } else {
                        pendingAutoStart = true
                    }
                }
            }
            path == "/custom_workouts_data" -> {
                receiveCustomWorkouts(String(messageEvent.data))
            }
        }
    }

    fun startTrackingService(
        trackSteps: Boolean,
        trackElevation: Boolean,
        trackGps: Boolean
    ) {
        val intent = Intent(this, WorkoutTrackingService::class.java)
        intent.action = WorkoutTrackingService.ACTION_START
        intent.putExtra(WorkoutTrackingService.EXTRA_TRACK_STEPS,     trackSteps)
        intent.putExtra(WorkoutTrackingService.EXTRA_TRACK_ELEVATION, trackElevation)
        intent.putExtra(WorkoutTrackingService.EXTRA_TRACK_GPS,       trackGps)
        startForegroundService(intent)
    }

    fun stopTrackingService() {
        val intent = Intent(this, WorkoutTrackingService::class.java)
        intent.action = WorkoutTrackingService.ACTION_STOP
        startService(intent)
    }

    fun vibrateShort() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createOneShot(
            200, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun vibrateLong() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createWaveform(
            longArrayOf(0, 300, 150, 300), -1))
    }
}

fun parseGoals(goals: List<String>): WorkoutGoals {
    var durationSec = 0L
    var heartRate   = 0
    var steps       = 0
    var calories    = 0
    var distance    = 0f

    for (goal in goals) {
        val lower = goal.lowercase()
        try {
            when {
                lower.startsWith("duration:") -> {
                    val nums = Regex("(\\d+)").findAll(goal)
                        .map { it.value.toLong() }.toList()
                    if (nums.isNotEmpty()) durationSec = nums[0] * 60
                }
                lower.contains("target steps") -> {
                    val nums = Regex("(\\d+)").findAll(goal)
                        .map { it.value.toInt() }.toList()
                    if (nums.isNotEmpty()) steps = nums[0]
                }
                lower.contains("target calories") -> {
                    val nums = Regex("(\\d+)").findAll(goal)
                        .map { it.value.toInt() }.toList()
                    if (nums.isNotEmpty()) calories = nums[0]
                }
                lower.contains("target distance") -> {
                    val nums = Regex("([\\d.]+)").findAll(goal)
                        .map { it.value.toFloat() }.toList()
                    if (nums.isNotEmpty()) distance = nums[0]
                }
                lower.contains("target heart rate") -> {
                    val nums = Regex("(\\d+)").findAll(goal)
                        .map { it.value.toInt() }.toList()
                    if (nums.isNotEmpty()) heartRate = nums[0]
                }
            }
        } catch (e: Exception) { }
    }

    return WorkoutGoals(durationSec, heartRate, steps, calories, distance)
}

@Composable
fun WearApp(activity: MainActivity) {
    val currentScreen = activity.currentScreen

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentAlignment = Alignment.Center
    ) {
        when (val screen = currentScreen) {
            is Screen.Selection -> {
                TimeText()
                WorkoutSelector(
                    activity = activity,
                    onCountdownRequested = { workout ->
                        activity.currentScreen = Screen.Countdown(workout)
                    },
                    onWorkoutStarted = { workout, activityId ->
                        activity.currentScreen =
                            Screen.ActiveWorkout(workout, activityId)
                    }
                )
            }
            is Screen.Countdown -> {
                CountdownScreen(
                    activity = activity,
                    workout  = screen.workout,
                    onCountdownComplete = { workout, activityId ->
                        activity.currentScreen =
                            Screen.ActiveWorkout(workout, activityId)
                    },
                    onCancelled = {
                        activity.currentScreen = Screen.Selection
                        activity.pendingGuidedWorkout = null
                    }
                )
            }
            is Screen.ActiveWorkout -> {
                ActiveWorkoutScreen(
                    activity         = activity,
                    workout          = screen.workout,
                    activityId       = screen.activityId,
                    onWorkoutStopped = {
                        activity.currentScreen = Screen.Selection
                    }
                )
            }
        }
    }
}

// Workout selector screen
@Composable
fun WorkoutSelector(
    activity: MainActivity,
    onCountdownRequested: (WorkoutType) -> Unit,
    onWorkoutStarted: (WorkoutType, Int) -> Unit
) {
    val context        = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val guidedWorkout  = activity.pendingGuidedWorkout
    val customWorkouts = activity.customWorkouts
    val allWorkouts    = builtInWorkouts + customWorkouts

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App title
        Text(
            text = "New You",
            textAlign = TextAlign.Center,
            color = AppDarkGreen,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        if (guidedWorkout != null) {
            Box(
                modifier = Modifier
                    .background(AppDarkGreen, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "⭐ ${guidedWorkout.tierName}",
                    color = ComposeColor.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        Text(
            text = if (guidedWorkout != null)
                "Guided workout ready" else "Choose a workout",
            textAlign = TextAlign.Center,
            color = AppText.copy(alpha = 0.7f),
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(allWorkouts) { workout ->
                val isGuided   = guidedWorkout?.workoutTypeId == workout.typeId
                val bgColor    = when {
                    isGuided         -> AppDarkGreen
                    workout.isCustom -> ComposeColor(0xFFCCE4FF)
                    else             -> AppCard
                }
                val labelColor = when {
                    isGuided         -> ComposeColor.White
                    workout.isCustom -> ComposeColor(0xFF1565C0)
                    else             -> AppText
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(76.dp)
                ) {
                    Button(
                        onClick = {
                            if (isGuided) {
                                // Guided workout tapped — go straight to countdown
                                onCountdownRequested(workout)
                            } else {
                                // Regular workout — launch immediately
                                coroutineScope.launch {
                                    launchWorkout(context, activity, workout,
                                        onWorkoutStarted)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(72.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = bgColor)
                    ) {
                        Image(
                            painter = painterResource(id = workout.iconRes),
                            contentDescription = workout.name,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Workout name label
                    Text(
                        text = workout.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = labelColor,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        modifier = Modifier.width(76.dp)
                    )

                    if (workout.isCustom) {
                        Text(
                            text = "custom",
                            fontSize = 8.sp,
                            color = ComposeColor(0xFF1565C0),
                            textAlign = TextAlign.Center
                        )
                    }
                    if (isGuided) {
                        Text(
                            text = "tap to start",
                            fontSize = 8.sp,
                            color = AppDarkGreen,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// Countdown screen — shown before a guided workout starts
@Composable
fun CountdownScreen(
    activity: MainActivity,
    workout: WorkoutType,
    onCountdownComplete: (WorkoutType, Int) -> Unit,
    onCancelled: () -> Unit
) {
    val context        = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var countdownValue by remember { mutableStateOf(3) }
    var isCancelled    by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Vibrate to alert the user the countdown is starting
        activity.vibrateShort()

        while (countdownValue > 0 && !isCancelled) {
            delay(1000L)
            if (!isCancelled) {
                countdownValue--
                if (countdownValue > 0) activity.vibrateShort()
            }
        }

        if (!isCancelled) {
            // Countdown finished — launch the workout
            activity.vibrateLong()
            coroutineScope.launch {
                launchWorkout(context, activity, workout) { w, id ->
                    onCountdownComplete(w, id)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Workout icon
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(AppDarkGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = workout.iconRes),
                contentDescription = workout.name,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = workout.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AppText,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Starting in",
            fontSize = 12.sp,
            color = AppText.copy(alpha = 0.65f),
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
        )

        // Big countdown number
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(AppDarkGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = countdownValue.toString(),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = ComposeColor.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cancel button
        Button(
            onClick = {
                isCancelled = true
                onCancelled()
            },
            modifier = Modifier
                .width(110.dp)
                .height(36.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = ComposeColor(0xFFCC2A22))
        ) {
            Text(
                text = "Cancel",
                fontSize = 13.sp,
                color = ComposeColor.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Shared workout launch helper
private suspend fun launchWorkout(
    context: Context,
    activity: MainActivity,
    workout: WorkoutType,
    onStarted: (WorkoutType, Int) -> Unit
) {
    try {
        val nodeClient    = Wearable.getNodeClient(context)
        val messageClient = Wearable.getMessageClient(context)
        val nodes         = nodeClient.connectedNodes.await()

        activity.pendingActivityId = -1

        for (node in nodes) {
            messageClient.sendMessage(
                node.id,
                "/workout/${workout.typeId}",
                workout.name.toByteArray()
            ).await()
        }

        // Wait for the phone to send back the real activity ID
        var waited = 0
        while (activity.pendingActivityId == -1 && waited < 5000) {
            delay(100)
            waited += 100
        }

        val realActivityId = activity.pendingActivityId

        val needsGps = workout.trackDistance || workout.trackSpeed
        WorkoutTrackingService.sensorHelper = SensorManagerHelper(context)

        activity.startTrackingService(
            trackSteps     = workout.trackSteps,
            trackElevation = workout.trackElevation,
            trackGps       = needsGps
        )

        onStarted(workout, realActivityId)

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// Active workout screen
@Composable
fun ActiveWorkoutScreen(
    activity: MainActivity,
    workout: WorkoutType,
    activityId: Int,
    onWorkoutStopped: () -> Unit
) {
    val context        = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var elapsedSeconds by remember { mutableStateOf(0L) }
    var isRunning      by remember { mutableStateOf(true) }
    var lapCount       by remember { mutableStateOf(0) }

    var liveHeartRate by remember { mutableStateOf(0) }
    var liveCalories  by remember { mutableStateOf(0) }
    var liveSteps     by remember { mutableStateOf(0) }
    var liveDistance  by remember { mutableStateOf(0f) }

    val guidedWorkout = activity.pendingGuidedWorkout
    val isGuided      = guidedWorkout?.workoutTypeId == workout.typeId
    val goals         = if (isGuided && guidedWorkout != null)
        parseGoals(guidedWorkout.goals) else null

    var durationMilestoneFired by remember { mutableStateOf(false) }
    var stepsMilestoneFired    by remember { mutableStateOf(false) }
    var calMilestoneFired      by remember { mutableStateOf(false) }
    var distMilestoneFired     by remember { mutableStateOf(false) }
    var workoutCompleteFired   by remember { mutableStateOf(false) }
    var alertMessage           by remember { mutableStateOf("") }

    // Timer
    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000L)
            elapsedSeconds++
        }
    }

    // Sensor updates + milestone checks every 3 seconds
    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(3000L)
            val sh  = WorkoutTrackingService.sensorHelper ?: continue
            val met = metValues[workout.typeId] ?: 5.0f
            liveHeartRate = sh.getAverageHeartRate()
            liveCalories  = sh.getCalories(met, elapsedSeconds)
            liveSteps     = sh.totalSteps
            liveDistance  = sh.getDistanceKm()

            if (goals != null) {
                if (!durationMilestoneFired
                    && goals.targetDurationSeconds > 0
                    && elapsedSeconds >= goals.targetDurationSeconds / 2) {
                    durationMilestoneFired = true
                    activity.vibrateShort()
                    alertMessage = "⏱ Halfway there!"
                    delay(2500L); alertMessage = ""
                }
                if (!stepsMilestoneFired && goals.targetSteps > 0
                    && liveSteps >= goals.targetSteps) {
                    stepsMilestoneFired = true
                    activity.vibrateShort()
                    alertMessage = "👟 Steps goal reached!"
                    delay(2500L); alertMessage = ""
                }
                if (!calMilestoneFired && goals.targetCalories > 0
                    && liveCalories >= goals.targetCalories) {
                    calMilestoneFired = true
                    activity.vibrateShort()
                    alertMessage = "🔥 Calorie goal!"
                    delay(2500L); alertMessage = ""
                }
                if (!distMilestoneFired && goals.targetDistance > 0
                    && liveDistance >= goals.targetDistance) {
                    distMilestoneFired = true
                    activity.vibrateShort()
                    alertMessage = "📍 Distance goal!"
                    delay(2500L); alertMessage = ""
                }
                if (!workoutCompleteFired) {
                    val durDone  = goals.targetDurationSeconds <= 0
                            || elapsedSeconds >= goals.targetDurationSeconds
                    val stepDone = goals.targetSteps <= 0 || liveSteps >= goals.targetSteps
                    val calDone  = goals.targetCalories <= 0 || liveCalories >= goals.targetCalories
                    val distDone = goals.targetDistance <= 0f || liveDistance >= goals.targetDistance
                    if (durDone && stepDone && calDone && distDone) {
                        workoutCompleteFired = true
                        activity.vibrateLong()
                        alertMessage = "🎉 Workout Complete!"
                        delay(3500L); alertMessage = ""
                    }
                }
            }
        }
    }

    // Phone live updates every 5 seconds
    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(5000L)
            if (!isRunning) break
            try {
                val sh  = WorkoutTrackingService.sensorHelper ?: continue
                val met = metValues[workout.typeId] ?: 5.0f
                val avgHR      = sh.getAverageHeartRate()
                val calories   = sh.getCalories(met, elapsedSeconds)
                val steps      = sh.totalSteps
                val distanceKm = sh.getDistanceKm()
                val pace       = sh.getPaceMinPerKm(elapsedSeconds)
                val speed      = sh.getSpeedKmh(elapsedSeconds)
                val elevGain   = sh.elevationGainMeters
                val isGps      = sh.isUsingGps()

                val livePath = "/workout_live/" +
                        "${workout.typeId}/$activityId/$elapsedSeconds/" +
                        "$avgHR/$calories/$steps/" +
                        "$distanceKm/$pace/$speed/$elevGain/$lapCount/" +
                        "${if (isGps) 1 else 0}"

                val nodes = Wearable.getNodeClient(context).connectedNodes.await()
                for (node in nodes) {
                    Wearable.getMessageClient(context)
                        .sendMessage(node.id, livePath,
                            workout.name.toByteArray()).await()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val hours     = elapsedSeconds / 3600
    val minutes   = (elapsedSeconds % 3600) / 60
    val seconds   = elapsedSeconds % 60
    val timerText = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon + name row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(AppDarkGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = workout.iconRes),
                    contentDescription = workout.name,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = workout.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppText
            )
        }

        // Guided badge
        if (isGuided && guidedWorkout != null) {
            Box(
                modifier = Modifier
                    .background(AppDarkGreen, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                    .padding(bottom = 4.dp)
            ) {
                Text(
                    text = "⭐ ${guidedWorkout.tierName}",
                    fontSize = 9.sp,
                    color = ComposeColor.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Timer
        Text(
            text = timerText,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = AppDarkGreen,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        // Alert banner
        if (alertMessage.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppDarkGreen, RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                    .padding(bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = alertMessage,
                    fontSize = 11.sp,
                    color = ComposeColor.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val hrGoal  = goals?.targetHeartRate ?: 0
            val calGoal = goals?.targetCalories  ?: 0

            LargeStatBox(
                label    = "Heart Rate",
                value    = if (liveHeartRate > 0)
                    "$liveHeartRate bpm" else "-- bpm",
                valueColor = AppRed,
                goalMet  = hrGoal > 0 && liveHeartRate >= hrGoal
            )
            LargeStatBox(
                label    = "Calories",
                value    = "$liveCalories kcal",
                valueColor = AppOrange,
                goalMet  = calGoal > 0 && liveCalories >= calGoal
            )
        }

        // Steps and/or distance
        if (workout.trackSteps || workout.trackDistance) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (workout.trackSteps) {
                    val stepGoal = goals?.targetSteps ?: 0
                    LargeStatBox(
                        label    = "Steps",
                        value    = "$liveSteps",
                        valueColor = AppDarkGreen,
                        goalMet  = stepGoal > 0 && liveSteps >= stepGoal
                    )
                }
                if (workout.trackDistance) {
                    val distGoal = goals?.targetDistance ?: 0f
                    LargeStatBox(
                        label    = "Distance",
                        value    = String.format("%.2f km", liveDistance),
                        valueColor = AppDarkGreen,
                        goalMet  = distGoal > 0 && liveDistance >= distGoal
                    )
                }
            }
        }

        // Lap counter for swimming
        if (workout.trackLaps) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(
                    text = "Laps: $lapCount",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppText
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { lapCount++ },
                    modifier = Modifier
                        .width(60.dp)
                        .height(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = AppDarkGreen)
                ) {
                    Text(text = "+ Lap", fontSize = 10.sp,
                        color = ComposeColor.White,
                        fontWeight = FontWeight.Bold)
                }
            }
        }

        // Stop button
        Button(
            onClick = {
                isRunning = false
                activity.stopTrackingService()
                activity.vibrateLong()

                val sh = WorkoutTrackingService.sensorHelper
                sh?.stopTracking()

                val met        = metValues[workout.typeId] ?: 5.0f
                val avgHR      = sh?.getAverageHeartRate() ?: 0
                val calories   = sh?.getCalories(met, elapsedSeconds) ?: 0
                val steps      = sh?.totalSteps ?: 0
                val distanceKm = sh?.getDistanceKm() ?: 0f
                val pace       = sh?.getPaceMinPerKm(elapsedSeconds) ?: 0f
                val speed      = sh?.getSpeedKmh(elapsedSeconds) ?: 0f
                val elevGain   = sh?.elevationGainMeters ?: 0f
                val elevLoss   = sh?.elevationLossMeters ?: 0f
                val hrStart    = sh?.getStartHeartRate() ?: 0
                val hrEnd      = sh?.getEndHeartRate()   ?: 0

                val stopPath = "/workout_stop/" +
                        "${workout.typeId}/$activityId/$elapsedSeconds/" +
                        "$avgHR/$calories/$steps/" +
                        "$distanceKm/$pace/$speed/" +
                        "$elevGain/$elevLoss/$hrStart/$hrEnd/$lapCount"

                coroutineScope.launch {
                    try {
                        val nodes = Wearable.getNodeClient(context)
                            .connectedNodes.await()
                        for (node in nodes) {
                            Wearable.getMessageClient(context)
                                .sendMessage(node.id, stopPath,
                                    workout.name.toByteArray()).await()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        activity.pendingGuidedWorkout = null
                        onWorkoutStopped()
                    }
                }
            },
            modifier = Modifier
                .width(120.dp)
                .height(38.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = ComposeColor(0xFFCC2A22))
        ) {
            Text(
                text = "Stop Workout",
                fontSize = 13.sp,
                color = ComposeColor.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Enlarged stat box for active workout screen
@Composable
fun LargeStatBox(
    label: String,
    value: String,
    valueColor: ComposeColor,
    goalMet: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(
                if (goalMet) AppDarkGreen.copy(alpha = 0.25f) else AppCard,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label + if (goalMet) " ✓" else "",
            fontSize = 10.sp,
            color = if (goalMet) AppDarkGreen else AppText.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            fontWeight = if (goalMet) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            textAlign = TextAlign.Center
        )
    }
}