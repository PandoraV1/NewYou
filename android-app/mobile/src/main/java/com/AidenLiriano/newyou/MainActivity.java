package com.AidenLiriano.newyou;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends AppCompatActivity implements MessageClient.OnMessageReceivedListener {

    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusTextView = findViewById(R.id.statusText);
        if (statusTextView == null) {
            statusTextView = new TextView(this);
            setContentView(statusTextView);
        }
        statusTextView.setText("Waiting for Watch...");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Wearable.getMessageClient(this).addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.getMessageClient(this).removeListener(this);
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        String path = messageEvent.getPath();

        // Listen for any workout message
        if (path.startsWith("/workout/")) {
            final String workoutName = new String(messageEvent.getData());

            // Update UI only — database saving is handled by PhoneListenerService
            runOnUiThread(() -> {
                statusTextView.setText("Workout started: " + workoutName);
                Toast.makeText(MainActivity.this, "Workout Logged: " + workoutName, Toast.LENGTH_SHORT).show();
            });
        }
    }
}