package com.AidenLiriano.newyou.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)
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
    workout: WorkoutType,
    activityId: Int,
    onWorkoutStopped: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var elapsedSeconds by remember { mutableStateOf(0L) }
    var isRunning by remember { mutableStateOf(true) }

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
        // Workout image inside a circle bubble
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = MaterialTheme.colors.primary,
                    shape = CircleShape
                )
                .padding(bottom = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = workout.iconRes),
                contentDescription = workout.name,
                modifier = Modifier.size(50.dp)
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

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                isRunning = false
                coroutineScope.launch {
                    try {
                        val nodeClient = Wearable.getNodeClient(context)
                        val messageClient = Wearable.getMessageClient(context)
                        val nodes = nodeClient.connectedNodes.await()

                        for (node in nodes) {
                            messageClient.sendMessage(
                                node.id,
                                "/workout_stop/${workout.typeId}/$activityId/$elapsedSeconds",
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