package com.AidenLiriano.newyou;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements MessageClient.OnMessageReceivedListener {

    private TextView statusTextView;
    // Executor to run database tasks in the background (replaces Coroutines)
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ensure you have activity_main.xml in res/layout/
        setContentView(R.layout.activity_main);

        statusTextView = findViewById(R.id.statusText);
        if (statusTextView == null) {
            // Fallback if your XML doesn't have the ID yet
            statusTextView = new TextView(this);
            setContentView(statusTextView);
            statusTextView.setText("Waiting for Watch...");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the listener when the app is visible
        Wearable.getMessageClient(this).addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister to save battery when app is in background
        Wearable.getMessageClient(this).removeListener(this);
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        // Check if the message path matches what the watch sends
        if (messageEvent.getPath().equals("/button_clicked")) {
            final String message = new String(messageEvent.getData());

            // 1. Update UI (Must be on UI Thread)
            runOnUiThread(() -> {
                statusTextView.setText("Watch says: " + message);
                Toast.makeText(MainActivity.this, "Message Received & Saving...", Toast.LENGTH_SHORT).show();
            });

            // 2. Save to SQLite Database (Must be on Background Thread)
            databaseExecutor.execute(() -> {
                AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                WatchMessage newMessage = new WatchMessage(message, System.currentTimeMillis());
                db.messageDao().insertMessage(newMessage);

                System.out.println("DEBUG: Successfully saved to SQLite: " + message);
            });
        }
    }
}