package com.AidenLiriano.newyou

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable

// 1. You must add "MessageClient.OnMessageReceivedListener" here
class MainActivity : AppCompatActivity(), MessageClient.OnMessageReceivedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Ensure you have an activity_main layout or use your Compose setContent

        // 2. Register the listener so this activity waits for watch messages
        Wearable.getMessageClient(this).addListener(this)
    }

    // 3. This function MUST have the "override" keyword
    override fun onMessageReceived(messageEvent: MessageEvent) {
        // We check if the path matches the one sent by the watch
        if (messageEvent.path == "/button_clicked") {
            val message = String(messageEvent.data)

            // This runs on a background thread, so we use runOnUiThread to show the Toast
            runOnUiThread {
                Toast.makeText(this, "Watch says: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 4. Always remove the listener to save battery and prevent crashes
        Wearable.getMessageClient(this).removeListener(this)
    }
}