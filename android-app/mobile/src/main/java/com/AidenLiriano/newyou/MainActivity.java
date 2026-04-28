package com.AidenLiriano.newyou;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends AppCompatActivity implements MessageClient.OnMessageReceivedListener {

    private TextView statusTextView;
    private TextView durationTextView;
    private TextView readyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusTextView = findViewById(R.id.statusText);
        durationTextView = findViewById(R.id.durationText);
        readyTextView = findViewById(R.id.readyText);

        // Hide duration and ready text until a workout ends
        if (durationTextView != null) durationTextView.setVisibility(android.view.View.GONE);
        if (readyTextView != null) readyTextView.setVisibility(android.view.View.GONE);

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

        // Workout started
        if (path.startsWith("/workout/")) {
            final String workoutName = new String(messageEvent.getData());
            runOnUiThread(() -> {
                statusTextView.setText("Workout started: " + workoutName);
                if (durationTextView != null) durationTextView.setVisibility(android.view.View.GONE);
                if (readyTextView != null) readyTextView.setVisibility(android.view.View.GONE);
                Toast.makeText(MainActivity.this, "Workout Started: " + workoutName, Toast.LENGTH_SHORT).show();
            });
        }

        // Workout stopped
        else if (path.startsWith("/workout_stop/")) {
            try {
                String[] parts = path.replace("/workout_stop/", "").split("/");
                long durationSeconds = Long.parseLong(parts[2]);

                // Format duration as HH:MM:SS
                long hours = durationSeconds / 3600;
                long minutes = (durationSeconds % 3600) / 60;
                long seconds = durationSeconds % 60;
                String formattedDuration = String.format("%02d:%02d:%02d", hours, minutes, seconds);

                final String workoutName = new String(messageEvent.getData());

                runOnUiThread(() -> {
                    statusTextView.setText("Workout ended: " + workoutName);

                    if (durationTextView != null) {
                        durationTextView.setText("Duration: " + formattedDuration);
                        durationTextView.setVisibility(android.view.View.VISIBLE);
                    }

                    if (readyTextView != null) {
                        readyTextView.setText("Ready to start next workout");
                        readyTextView.setVisibility(android.view.View.VISIBLE);
                    }

                    Toast.makeText(MainActivity.this, "Workout Complete!", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}