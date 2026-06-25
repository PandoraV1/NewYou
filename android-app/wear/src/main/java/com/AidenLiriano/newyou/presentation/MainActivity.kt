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

val AppBackground = ComposeColor(0xFFE5E0AA)
val AppGreen      = ComposeColor(0xFF98CD00)
val AppCard       = ComposeColor(0xFFFFFADC)
val AppText       = ComposeColor(0xFF1C1C1E)
val AppRed        = ComposeColor(0xFFEF5350)
val AppOrange     = ComposeColor(0xFFFFA726)

// Custom workout type base ID
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

// Represents any workout, built-in or custom
data class WorkoutType(
    val name: String,
    val typeId: Int,  // 1-8 for built-in, 100+ for custom
    val iconRes: Int,
    val isCustom: Boolean = false,
    val trackSteps: Boolean = false,
    val trackDistance: Boolean = false,
    val trackElevation: Boolean = false,
    val trackLaps: Boolean = false,
    val trackSpeed: Boolean = false
)

// Built-in workouts
val builtInWorkouts = listOf(
    WorkoutType("Running",    1, R.drawable.ic_running,   trackSteps = true),
    WorkoutType("Swimming",   2, R.drawable.ic_swimming,  trackLaps = true),
    WorkoutType("Biking",     3, R.drawable.ic_biking,    trackDistance = true, trackSpeed = true),
    WorkoutType("Walking",    4, R.drawable.ic_walking,   trackSteps = true),
    WorkoutType("Hiking",     5, R.drawable.ic_hiking,    trackSteps = true, trackElevation = true),
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
    data class ActiveWorkout(val workout: WorkoutType, val activityId: Int) : Screen()
}

class MainActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {

    var pendingActivityId      by mutableStateOf(-1)
    var pendingGuidedWorkout   by mutableStateOf<GuidedWorkoutPlan?>(null)
    var customWorkouts         by mutableStateOf<List<WorkoutType>>(emptyList())

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        WorkoutTrackingService.sensorHelper = SensorManagerHelper(this)

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
        Wearable.getMessageClient(this).addListener(this)
        requestCustomWorkoutsFromPhone()
    }

    override fun onPause() {
        super.onPause()
        Wearable.getMessageClient(this).removeListener(this)
    }

    private fun requestCustomWorkoutsFromPhone() {
        kotlinx.coroutines.MainScope().launch {
            try {
                val nodes = Wearable.getNodeClient(this@MainActivity)
                    .connectedNodes.await()
                for (node in nodes) {
                    Wearable.getMessageClient(this@MainActivity)
                        .sendMessage(node.id, "/request_custom_workouts",
                            ByteArray(0)).await()
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
                val realId = path.replace("/activity_id_response/", "").toIntOrNull()
                if (realId != null) pendingActivityId = realId
            }
            path == "/guided_workout" -> {
                val data  = String(messageEvent.data)
                val parts = data.split("|")
                if (parts.size >= 3) {
                    val typeId   = parts[0].toIntOrNull() ?: return
                    val tierName = parts[1]
                    val goals    = parts[2].split("~")
                    pendingGuidedWorkout = GuidedWorkoutPlan(typeId, tierName, goals)
                }
            }
            path == "/custom_workouts_data" -> {
                // Receive custom workout definitions from phone
                // Format: name|iconIndex|trackSteps|trackDist|trackElev|trackLaps|trackSpeed|id
                // Multiple workouts separated by ;
                val data = String(messageEvent.data)
                if (data.isNotEmpty() && data != "NONE") {
                    val parsed = mutableListOf<WorkoutType>()
                    data.split(";").forEach { entry ->
                        val f = entry.split("|")
                        if (f.size >= 8) {
                            val iconIdx = f[1].toIntOrNull() ?: 0
                            val cwId    = f[7].toIntOrNull() ?: 0
                            parsed.add(WorkoutType(
                                name          = f[0],
                                typeId        = CUSTOM_WORKOUT_TYPE_BASE + cwId,
                                iconRes       = iconResources.getOrElse(iconIdx) { R.drawable.ic_running },
                                isCustom      = true,
                                trackSteps    = f[2] == "1",
                                trackDistance = f[3] == "1",
                                trackElevation = f[4] == "1",
                                trackLaps     = f[5] == "1",
                                trackSpeed    = f[6] == "1"
                            ))
                        }
                    }
                    customWorkouts = parsed
                } else {
                    customWorkouts = emptyList()
                }
            }
        }
    }

    fun startTrackingService() {
        val intent = Intent(this, WorkoutTrackingService::class.java)
        intent.action = WorkoutTrackingService.ACTION_START
        startForegroundService(intent)
    }

    fun stopTrackingService() {
        val intent = Intent(this, WorkoutTrackingService::class.java)
        intent.action = WorkoutTrackingService.ACTION_STOP
        startService(intent)
    }

    fun vibrateShort() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createOneShot(200,
            VibrationEffect.DEFAULT_AMPLITUDE))
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
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Selection) }

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
                    onWorkoutStarted = { workout, activityId ->
                        currentScreen = Screen.ActiveWorkout(workout, activityId)
                    }
                )
            }
            is Screen.ActiveWorkout -> {
                ActiveWorkoutScreen(
                    activity       = activity,
                    workout        = screen.workout,
                    activityId     = screen.activityId,
                    onWorkoutStopped = {
                        currentScreen = Screen.Selection
                    }
                )
            }
        }
    }
}

@Composable
fun WorkoutSelector(
    activity: MainActivity,
    onWorkoutStarted: (WorkoutType, Int) -> Unit
) {
    val context        = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val guidedWorkout  = activity.pendingGuidedWorkout
    val customWorkouts = activity.customWorkouts

    // Combine built-in and custom workouts
    val allWorkouts = builtInWorkouts + customWorkouts

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "New You",
            textAlign = TextAlign.Center,
            color = AppGreen,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 2.dp)
        )

        if (guidedWorkout != null) {
            Text(
                text = "⭐ ${guidedWorkout.tierName}",
                textAlign = TextAlign.Center,
                color = AppText,
                fontSize = 10.sp,
                modifier = Modifier
                    .background(AppCard, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        Text(
            text = if (guidedWorkout != null) "Start guided workout" else "Select Workout",
            textAlign = TextAlign.Center,
            color = AppText,
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(allWorkouts) { workout ->
                val isGuided = guidedWorkout?.workoutTypeId == workout.typeId
                val bgColor  = when {
                    isGuided        -> AppGreen
                    workout.isCustom -> ComposeColor(0xFFDDEEFF) // light blue tint for custom
                    else            -> AppCard
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(80.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
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

                                    var waited = 0
                                    while (activity.pendingActivityId == -1 && waited < 5000) {
                                        delay(100)
                                        waited += 100
                                    }

                                    val realActivityId = activity.pendingActivityId

                                    val needsGps = workout.trackDistance || workout.trackSpeed
                                    WorkoutTrackingService.sensorHelper =
                                        SensorManagerHelper(context)
                                    WorkoutTrackingService.sensorHelper?.startTracking(
                                        trackHeartRate  = true,
                                        trackSteps      = workout.trackSteps,
                                        trackElevation  = workout.trackElevation,
                                        trackGps        = needsGps
                                    )

                                    activity.startTrackingService()
                                    onWorkoutStarted(workout, realActivityId)

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        },
                        modifier = Modifier
                            .width(70.dp)
                            .height(70.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = bgColor)
                    ) {
                        Image(
                            painter = painterResource(id = workout.iconRes),
                            contentDescription = workout.name,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // Label below button
                    Text(
                        text = if (workout.name.length > 8)
                            workout.name.take(7) + "…"
                        else workout.name,
                        fontSize = 8.sp,
                        color = AppText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    // Custom badge
                    if (workout.isCustom) {
                        Text(
                            text = "custom",
                            fontSize = 7.sp,
                            color = ComposeColor(0xFF1565C0),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

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
    val isGuided = guidedWorkout?.workoutTypeId == workout.typeId
    val goals    = if (isGuided && guidedWorkout != null)
        parseGoals(guidedWorkout.goals) else null

    var durationMilestoneFired by remember { mutableStateOf(false) }
    var stepsMilestoneFired    by remember { mutableStateOf(false) }
    var calMilestoneFired      by remember { mutableStateOf(false) }
    var distMilestoneFired     by remember { mutableStateOf(false) }
    var workoutCompleteFired   by remember { mutableStateOf(false) }

    var alertMessage by remember { mutableStateOf("") }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000L)
            elapsedSeconds++
        }
    }

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
                    alertMessage = "⏱ Halfway through target duration!"
                    delay(3000L); alertMessage = ""
                }
                if (!stepsMilestoneFired && goals.targetSteps > 0
                    && liveSteps >= goals.targetSteps) {
                    stepsMilestoneFired = true
                    activity.vibrateShort()
                    alertMessage = "👟 Step goal reached!"
                    delay(3000L); alertMessage = ""
                }
                if (!calMilestoneFired && goals.targetCalories > 0
                    && liveCalories >= goals.targetCalories) {
                    calMilestoneFired = true
                    activity.vibrateShort()
                    alertMessage = "🔥 Calorie goal reached!"
                    delay(3000L); alertMessage = ""
                }
                if (!distMilestoneFired && goals.targetDistance > 0
                    && liveDistance >= goals.targetDistance) {
                    distMilestoneFired = true
                    activity.vibrateShort()
                    alertMessage = "📍 Distance goal reached!"
                    delay(3000L); alertMessage = ""
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
                        delay(4000L); alertMessage = ""
                    }
                }
            }
        }
    }

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

    val hours   = elapsedSeconds / 3600
    val minutes = (elapsedSeconds % 3600) / 60
    val seconds = elapsedSeconds % 60
    val timerText = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    val showSteps    = workout.trackSteps
    val showDistance = workout.trackDistance

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(color = AppGreen, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = workout.iconRes),
                contentDescription = workout.name,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        if (workout.isCustom) {
            Text(
                text = "🛠️ Custom",
                fontSize = 9.sp,
                color = AppText,
                modifier = Modifier
                    .background(AppCard, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
        }

        if (isGuided && guidedWorkout != null) {
            Text(
                text = "⭐ ${guidedWorkout.tierName}",
                fontSize = 9.sp,
                color = AppText,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(AppCard, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
        }

        Text(
            text = timerText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppText,
            textAlign = TextAlign.Center
        )

        if (alertMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = alertMessage,
                fontSize = 10.sp,
                color = AppText,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(AppGreen, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val hrGoal = goals?.targetHeartRate ?: 0
            StatBox(
                label = "❤️ HR",
                value = if (liveHeartRate > 0) "$liveHeartRate bpm" else "-- bpm",
                valueColor = AppRed,
                goalMet = hrGoal > 0 && liveHeartRate >= hrGoal
            )
            val calGoal = goals?.targetCalories ?: 0
            StatBox(
                label = "🔥 Cal",
                value = "$liveCalories",
                valueColor = AppOrange,
                goalMet = calGoal > 0 && liveCalories >= calGoal
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (showSteps || showDistance) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (showSteps) {
                    val stepGoal = goals?.targetSteps ?: 0
                    StatBox(
                        label = "👟 Steps",
                        value = "$liveSteps",
                        valueColor = AppGreen,
                        goalMet = stepGoal > 0 && liveSteps >= stepGoal
                    )
                }
                if (showDistance) {
                    val distGoal = goals?.targetDistance ?: 0f
                    StatBox(
                        label = "📍 Dist",
                        value = String.format("%.2f km", liveDistance),
                        valueColor = AppGreen,
                        goalMet = distGoal > 0 && liveDistance >= distGoal
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (workout.trackLaps) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Laps: $lapCount", fontSize = 11.sp, color = AppText)
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { lapCount++ },
                    modifier = Modifier.width(52.dp).height(24.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = AppGreen)
                ) {
                    Text(text = "+", fontSize = 12.sp, color = AppText)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

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
                val hrEnd      = sh?.getEndHeartRate() ?: 0

                val stopPath = "/workout_stop/" +
                        "${workout.typeId}/$activityId/$elapsedSeconds/" +
                        "$avgHR/$calories/$steps/" +
                        "$distanceKm/$pace/$speed/" +
                        "$elevGain/$elevLoss/$hrStart/$hrEnd/$lapCount"

                coroutineScope.launch {
                    try {
                        val nodes = Wearable.getNodeClient(context).connectedNodes.await()
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
            modifier = Modifier.width(90.dp).height(32.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = ComposeColor(0xFFCC2A22)
            )
        ) {
            Text(
                text = "Stop",
                fontSize = 12.sp,
                color = ComposeColor.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatBox(
    label: String,
    value: String,
    valueColor: ComposeColor,
    goalMet: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(
                if (goalMet) AppGreen.copy(alpha = 0.3f) else AppCard,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label + if (goalMet) " ✓" else "",
            fontSize = 9.sp,
            color = if (goalMet) AppGreen else AppText.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            fontWeight = if (goalMet) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            textAlign = TextAlign.Center
        )
    }
}