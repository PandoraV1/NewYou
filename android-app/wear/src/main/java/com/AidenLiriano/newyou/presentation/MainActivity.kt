package com.AidenLiriano.newyou.presentation

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import com.AidenLiriano.newyou.R
import com.AidenLiriano.newyou.presentation.theme.NewYouTheme
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

val AppBackground = androidx.compose.ui.graphics.Color(0xFFE5E0AA)
val AppGreen      = androidx.compose.ui.graphics.Color(0xFF98CD00)
val AppCard       = androidx.compose.ui.graphics.Color(0xFFFFFADC)
val AppText       = androidx.compose.ui.graphics.Color(0xFF1C1C1E)
val AppRed        = androidx.compose.ui.graphics.Color(0xFFEF5350)
val AppOrange     = androidx.compose.ui.graphics.Color(0xFFFFA726)

// Holds a received guided workout plan from the phone
data class GuidedWorkoutPlan(
    val workoutTypeId: Int,
    val tierName: String,
    val goals: List<String>
)

// Parses goals from the string format and extracts numeric targets
data class WorkoutGoals(
    val targetDurationSeconds: Long,   // from "Duration: X minutes"
    val targetHeartRate: Int,          // from "Target heart rate: X-Y% max HR" — uses lower bound
    val targetSteps: Int,              // from "Target steps: X-Y"
    val targetCalories: Int,           // from "Target calories: X"
    val targetDistance: Float          // from "Target distance: X km"
)

class MainActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {

    var pendingActivityId by mutableStateOf(-1)
    var pendingGuidedWorkout by mutableStateOf<GuidedWorkoutPlan?>(null)

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
    }

    override fun onPause() {
        super.onPause()
        Wearable.getMessageClient(this).removeListener(this)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val path = messageEvent.path

        when {
            path.startsWith("/activity_id_response/") -> {
                val realId = path.replace("/activity_id_response/", "").toIntOrNull()
                if (realId != null) pendingActivityId = realId
            }
            path == "/guided_workout" -> {
                // Parse the guided workout sent from the phone
                // Format: workoutTypeId|tierName|goal1~goal2~goal3
                val data = String(messageEvent.data)
                val parts = data.split("|")
                if (parts.size >= 3) {
                    val typeId = parts[0].toIntOrNull() ?: return
                    val tierName = parts[1]
                    val goals = parts[2].split("~")
                    pendingGuidedWorkout = GuidedWorkoutPlan(typeId, tierName, goals)
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

    // Haptic feedback, short buzz for milestone, long buzz for completion
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

data class WorkoutType(val name: String, val typeId: Int, val iconRes: Int)

val workouts = listOf(
    WorkoutType("Running",    1, R.drawable.ic_running),
    WorkoutType("Swimming",   2, R.drawable.ic_swimming),
    WorkoutType("Biking",     3, R.drawable.ic_biking),
    WorkoutType("Walking",    4, R.drawable.ic_walking),
    WorkoutType("Hiking",     5, R.drawable.ic_hiking),
    WorkoutType("Meditation", 6, R.drawable.ic_meditation),
    WorkoutType("Strength",   7, R.drawable.ic_strength),
    WorkoutType("Yoga",       8, R.drawable.ic_yoga)
)

val metValues = mapOf(
    1 to 9.8f, 2 to 6.0f, 3 to 7.5f, 4 to 3.5f,
    5 to 6.0f, 6 to 2.5f, 7 to 5.0f, 8 to 3.0f
)

sealed class Screen {
    object Selection : Screen()
    data class ActiveWorkout(val workout: WorkoutType, val activityId: Int) : Screen()
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
                    activity = activity,
                    workout = screen.workout,
                    activityId = screen.activityId,
                    onWorkoutStopped = {
                        currentScreen = Screen.Selection
                    }
                )
            }
        }
    }
}

// Parse numeric targets from goal strings
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
                    // "Duration: 35-40 minutes" or "Duration: 20 minutes"
                    val nums = Regex("(\\d+)").findAll(goal).map { it.value.toLong() }.toList()
                    if (nums.isNotEmpty()) durationSec = nums[0] * 60
                }
                lower.contains("target steps:") || lower.startsWith("target steps") -> {
                    val nums = Regex("(\\d+)").findAll(goal).map { it.value.toInt() }.toList()
                    if (nums.isNotEmpty()) steps = nums[0]
                }
                lower.contains("target calories:") || lower.startsWith("target calories") -> {
                    val nums = Regex("(\\d+)").findAll(goal).map { it.value.toInt() }.toList()
                    if (nums.isNotEmpty()) calories = nums[0]
                }
                lower.contains("target distance:") -> {
                    val nums = Regex("([\\d.]+)").findAll(goal).map { it.value.toFloat() }.toList()
                    if (nums.isNotEmpty()) distance = nums[0]
                }
                lower.contains("target heart rate:") -> {
                    val nums = Regex("(\\d+)").findAll(goal).map { it.value.toInt() }.toList()
                    if (nums.isNotEmpty()) heartRate = nums[0]
                }
            }
        } catch (e: Exception) { /* skip unparseable goals */ }
    }

    return WorkoutGoals(durationSec, heartRate, steps, calories, distance)
}

@Composable
fun WorkoutSelector(
    activity: MainActivity,
    onWorkoutStarted: (WorkoutType, Int) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val guidedWorkout = activity.pendingGuidedWorkout

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

        // Show guided workout indicator if one is queued from phone
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
            items(workouts) { workout ->
                // Highlight the workout type matching the guided plan
                val isGuided = guidedWorkout?.workoutTypeId == workout.typeId
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

                                WorkoutTrackingService.sensorHelper = SensorManagerHelper(context)
                                WorkoutTrackingService.sensorHelper?.startTracking(
                                    trackHeartRate = true,
                                    trackSteps     = workout.typeId in listOf(1, 4, 5),
                                    trackElevation = workout.typeId == 5,
                                    trackGps       = workout.typeId in listOf(1, 3, 4, 5) // Running, Biking, Walking, Hiking
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
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (isGuided) AppGreen else AppCard
                    )
                ) {
                    Image(
                        painter = painterResource(id = workout.iconRes),
                        contentDescription = workout.name,
                        modifier = Modifier.size(40.dp)
                    )
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var elapsedSeconds by remember { mutableStateOf(0L) }
    var isRunning      by remember { mutableStateOf(true) }
    var lapCount       by remember { mutableStateOf(0) }

    var liveHeartRate  by remember { mutableStateOf(0) }
    var liveCalories   by remember { mutableStateOf(0) }
    var liveSteps      by remember { mutableStateOf(0) }
    var liveDistance   by remember { mutableStateOf(0f) }

    // Parse goals from guided workout if one is active
    val guidedWorkout = activity.pendingGuidedWorkout
    val isGuided = guidedWorkout?.workoutTypeId == workout.typeId
    val goals = if (isGuided && guidedWorkout != null)
        parseGoals(guidedWorkout.goals) else null

    // Milestone tracking
    var durationMilestoneFired by remember { mutableStateOf(false) }
    var stepsMilestoneFired    by remember { mutableStateOf(false) }
    var calMilestoneFired      by remember { mutableStateOf(false) }
    var distMilestoneFired     by remember { mutableStateOf(false) }
    var workoutCompleteFired   by remember { mutableStateOf(false) }

    // Alert message shown on screen
    var alertMessage by remember { mutableStateOf("") }

    // Timer
    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000L)
            elapsedSeconds++
        }
    }

    // Sensor updates every 3 seconds + milestone checks
    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(3000L)
            val sh  = WorkoutTrackingService.sensorHelper ?: continue
            val met = metValues[workout.typeId] ?: 5.0f
            liveHeartRate = sh.getAverageHeartRate()
            liveCalories  = sh.getCalories(met, elapsedSeconds)
            liveSteps     = sh.totalSteps
            liveDistance  = sh.getDistanceKm()

            // Check milestones if guided workout is active
            if (goals != null) {
                // Duration milestone — 50% of target
                if (!durationMilestoneFired
                    && goals.targetDurationSeconds > 0
                    && elapsedSeconds >= goals.targetDurationSeconds / 2) {
                    durationMilestoneFired = true
                    activity.vibrateShort()
                    alertMessage = "⏱ Halfway through your target duration!"
                    delay(3000L)
                    alertMessage = ""
                }

                // Steps milestone
                if (!stepsMilestoneFired
                    && goals.targetSteps > 0
                    && liveSteps >= goals.targetSteps) {
                    stepsMilestoneFired = true
                    activity.vibrateShort()
                    alertMessage = "👟 Step goal reached! ${liveSteps} steps"
                    delay(3000L)
                    alertMessage = ""
                }

                // Calories milestone
                if (!calMilestoneFired
                    && goals.targetCalories > 0
                    && liveCalories >= goals.targetCalories) {
                    calMilestoneFired = true
                    activity.vibrateShort()
                    alertMessage = "🔥 Calorie goal reached! ${liveCalories} kcal"
                    delay(3000L)
                    alertMessage = ""
                }

                // Distance milestone
                if (!distMilestoneFired
                    && goals.targetDistance > 0
                    && liveDistance >= goals.targetDistance) {
                    distMilestoneFired = true
                    activity.vibrateShort()
                    alertMessage = "📍 Distance goal reached! ${String.format("%.2f", liveDistance)} km"
                    delay(3000L)
                    alertMessage = ""
                }

                // Workout complete — all applicable goals met
                if (!workoutCompleteFired) {
                    val durDone  = goals.targetDurationSeconds <= 0 || elapsedSeconds >= goals.targetDurationSeconds
                    val stepDone = goals.targetSteps <= 0 || liveSteps >= goals.targetSteps
                    val calDone  = goals.targetCalories <= 0 || liveCalories >= goals.targetCalories
                    val distDone = goals.targetDistance <= 0f || liveDistance >= goals.targetDistance

                    if (durDone && stepDone && calDone && distDone) {
                        workoutCompleteFired = true
                        activity.vibrateLong()
                        alertMessage = "🎉 Workout Complete! Great job!"
                        delay(4000L)
                        alertMessage = ""
                    }
                }
            }
        }
    }

    // Live phone updates every 5 seconds
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

                val isGps = WorkoutTrackingService.sensorHelper?.isUsingGps() ?: false
                val livePath = "/workout_live/" +
                        "${workout.typeId}/$activityId/$elapsedSeconds/" +
                        "$avgHR/$calories/$steps/" +
                        "$distanceKm/$pace/$speed/$elevGain/$lapCount/${if (isGps) 1 else 0}"

                val nodes = Wearable.getNodeClient(context).connectedNodes.await()
                for (node in nodes) {
                    Wearable.getMessageClient(context)
                        .sendMessage(node.id, livePath, workout.name.toByteArray()).await()
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

    val showSteps    = workout.typeId in listOf(1, 4, 5)
    val showDistance = workout.typeId in listOf(1, 2, 3, 4, 5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Workout icon
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

        // Guided workout indicator
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

        // Timer
        Text(
            text = timerText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = AppText,
            textAlign = TextAlign.Center
        )

        // Alert message shown when milestone is hit
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

        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Heart rate with goal indicator
            val hrGoal = goals?.targetHeartRate ?: 0
            StatBox(
                label = "❤️ HR",
                value = if (liveHeartRate > 0) "$liveHeartRate bpm" else "-- bpm",
                valueColor = AppRed,
                goalMet = hrGoal > 0 && liveHeartRate >= hrGoal
            )
            // Calories with goal indicator
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

        if (workout.typeId == 2) {
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
                        // Clear guided workout after completion
                        activity.pendingGuidedWorkout = null
                        onWorkoutStopped()
                    }
                }
            },
            modifier = Modifier.width(90.dp).height(32.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = androidx.compose.ui.graphics.Color(0xFFCC2A22)
            )
        ) {
            Text(text = "Stop", fontSize = 12.sp,
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatBox(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
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