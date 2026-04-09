package com.AidenLiriano.newyou.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.AidenLiriano.newyou.R
import com.AidenLiriano.newyou.presentation.theme.NewYouTheme
import androidx.compose.runtime.rememberCoroutineScope // To run the message sending in background
import androidx.compose.foundation.layout.Column // To stack text and button
import androidx.wear.compose.material.Button // The button component
import com.google.android.gms.wearable.Wearable // The Wearable API
import kotlinx.coroutines.launch // To launch the scope
import kotlinx.coroutines.tasks.await // To handle the API calls easily
import androidx.compose.ui.platform.LocalContext // To get the context for the API

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
            Greeting()
        }
    }
}

@Composable
fun Greeting() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary,
            text = "New You Watch"
        )

        // The Button
        Button(
            onClick = {
                // Launch a coroutine to send the message without freezing the UI
                coroutineScope.launch {
                    try {
                        val nodeClient = Wearable.getNodeClient(context)
                        val messageClient = Wearable.getMessageClient(context)

                        // Find connected nodes (the phone)
                        val nodes = nodeClient.connectedNodes.await()

                        for (node in nodes) {
                            // Send the message
                            messageClient.sendMessage(
                                node.id,
                                "/button_clicked",
                                "Watch button was pressed!".toByteArray()
                            ).await()
                        }
                    } catch (e: Exception) {
                        // Handle errors (e.g., phone not connected)
                        e.printStackTrace()
                    }
                }
            }
        ) {
            Text("Ping Phone")
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp()
}