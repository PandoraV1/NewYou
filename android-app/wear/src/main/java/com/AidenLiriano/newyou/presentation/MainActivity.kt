package com.AidenLiriano.newyou.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import com.AidenLiriano.newyou.R
import com.AidenLiriano.newyou.presentation.theme.NewYouTheme
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)
        setContent {
            WearApp()
        }
    }
}

// Each workout has a name and a type number (1-8)
data class WorkoutType(val name: String, val typeId: Int)

val workouts = listOf(
    WorkoutType("Running", 1),
    WorkoutType("Swimming", 2),
    WorkoutType("Biking", 3),
    WorkoutType("Walking", 4),
    WorkoutType("Hiking", 5),
    WorkoutType("Meditation", 6),
    WorkoutType("Strength", 7),
    WorkoutType("Yoga", 8)
)

@Composable
fun WearApp() {
    NewYouTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            WorkoutSelector()
        }
    }
}

@Composable
fun WorkoutSelector() {
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

        // Horizontally scrollable row of workout buttons
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

                                for (node in nodes) {
                                    // Send path like "/workout/1", "/workout/2" etc.
                                    messageClient.sendMessage(
                                        node.id,
                                        "/workout/${workout.typeId}",
                                        workout.name.toByteArray()
                                    ).await()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    modifier = Modifier
                        .width(80.dp)
                        .height(60.dp)
                ) {
                    Text(
                        text = workout.name,
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}