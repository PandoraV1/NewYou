package com.AidenLiriano.newyou;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SplashActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            int userCount  = db.appDao().getUserCount();

            runOnUiThread(() -> {
                Intent intent;
                if (userCount > 0) {
                    // User already set up, go straight to main
                    intent = new Intent(this, MainActivity.class);
                } else {
                    // No user yet, go to login
                    intent = new Intent(this, LoginActivity.class);
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });
    }
}