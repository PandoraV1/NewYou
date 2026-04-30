package com.AidenLiriano.newyou.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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

class MainActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {

    var pendingActivityId by mutableStateOf(-1)
    lateinit var sensorHelper: SensorManagerHelper

    // Permission launcher for body sensors
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* permissions handled at runtime */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        sensorHelper = SensorManagerHelper(this)

        // Request permissions on launch
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.BODY_SENSORS,
                Manifest.permission.ACTIVITY_RECOGNITION
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
        if (path.startsWith("/activity_id_response/")) {
            val realId = path.replace("/activity_id_response/", "").toIntOrNull()
            if (realId != null) {
                pendingActivityId = realId
            }
        }
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

// MET values for each workout type
val metValues = mapOf(
    1 to 9.8f,  // Running
    2 to 6.0f,  // Swimming
    3 to 7.5f,  // Biking
    4 to 3.5f,  // Walking
    5 to 6.0f,  // Hiking
    6 to 2.5f,  // Meditation
    7 to 5.0f,  // Strength Training
    8 to 3.0f   // Yoga
)

sealed class Screen {
    object Selection : Screen()
    data class ActiveWorkout(val workout: WorkoutType, val activityId: Int) : Screen()
}

@Composable
fun WearApp(activity: MainActivity) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Selection) }

    NewYouTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
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
}

@Composable
fun WorkoutSelector(
    activity: MainActivity,
    onWorkoutStarted: (WorkoutType, Int) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Select Workout",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(workouts) { workout ->
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val nodeClient = Wearable.getNodeClient(context)
                                val messageClient = Wearable.getMessageClient(context)
                                val nodes = nodeClient.connectedNodes.await()

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

                                // Start sensor tracking for this workout
                                activity.sensorHelper = SensorManagerHelper(context)
                                activity.sensorHelper.startTracking(
                                    trackHeartRate = true,
                                    trackSteps = workout.typeId in listOf(1, 4, 5), // Running, Walking, Hiking
                                    trackElevation = workout.typeId == 5 // Hiking only
                                )

                                onWorkoutStarted(workout, realActivityId)

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    modifier = Modifier
                        .width(70.dp)
                        .height(70.dp)
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
    var isRunning by remember { mutableStateOf(true) }
    var lapCount by remember { mutableStateOf(0) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000L)
            elapsedSeconds++
        }
    }

    val hours = elapsedSeconds / 3600
    val minutes = (elapsedSeconds % 3600) / 60
    val seconds = elapsedSeconds % 60
    val timerText = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Workout image in circle bubble
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(
                    color = MaterialTheme.colors.primary,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = workout.iconRes),
                contentDescription = workout.name,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = workout.name,
            fontSize = 13.sp,
            color = MaterialTheme.colors.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = timerText,
            fontSize = 18.sp,
            color = MaterialTheme.colors.onBackground,
            textAlign = TextAlign.Center
        )

        // Lap counter button for swimming only
        if (workout.typeId == 2) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Laps: $lapCount",
                fontSize = 13.sp,
                color = MaterialTheme.colors.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = { lapCount++ },
                modifier = Modifier
                    .width(80.dp)
                    .height(30.dp)
            ) {
                Text(text = "+ Lap", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Stop button
        Button(
            onClick = {
                isRunning = false
                activity.sensorHelper.stopTracking()

                val sensorHelper = activity.sensorHelper
                val met = metValues[workout.typeId] ?: 5.0f
                val avgHeartRate = sensorHelper.getAverageHeartRate()
                val calories = sensorHelper.getCalories(met, elapsedSeconds)
                val steps = sensorHelper.totalSteps
                val distanceKm = sensorHelper.getDistanceKm()
                val pace = sensorHelper.getPaceMinPerKm(elapsedSeconds)
                val speed = sensorHelper.getSpeedKmh(elapsedSeconds)
                val elevGain = sensorHelper.elevationGainMeters
                val elevLoss = sensorHelper.elevationLossMeters
                val hrStart = sensorHelper.getStartHeartRate()
                val hrEnd = sensorHelper.getEndHeartRate()

                // Build the stop message path with all sensor data
                // Format: /workout_stop/type/activityId/duration/heartRate/calories
                //         /steps/distanceKm/pace/speed/elevGain/elevLoss/hrStart/hrEnd/laps
                val stopPath = "/workout_stop/" +
                        "${workout.typeId}/$activityId/$elapsedSeconds/" +
                        "$avgHeartRate/$calories/$steps/" +
                        "$distanceKm/$pace/$speed/" +
                        "$elevGain/$elevLoss/$hrStart/$hrEnd/$lapCount"

                coroutineScope.launch {
                    try {
                        val nodeClient = Wearable.getNodeClient(context)
                        val messageClient = Wearable.getMessageClient(context)
                        val nodes = nodeClient.connectedNodes.await()

                        for (node in nodes) {
                            messageClient.sendMessage(
                                node.id,
                                stopPath,
                                workout.name.toByteArray()
                            ).await()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        onWorkoutStopped()
                    }
                }
            },
            modifier = Modifier
                .width(90.dp)
                .height(36.dp)
        ) {
            Text(text = "Stop", fontSize = 12.sp)
        }
    }
}