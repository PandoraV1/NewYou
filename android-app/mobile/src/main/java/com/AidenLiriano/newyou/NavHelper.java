package com.AidenLiriano.newyou;

import android.app.Activity;
import android.content.Intent;
import android.widget.ImageButton;

public class NavHelper {

    public static void setup(Activity activity) {
        ImageButton btnSettings = activity.findViewById(R.id.navSettings);
        ImageButton btnHome     = activity.findViewById(R.id.navHome);
        ImageButton btnHistory  = activity.findViewById(R.id.navHistory);

        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                if (!(activity instanceof SettingsActivity)) {
                    activity.startActivity(
                            new Intent(activity, SettingsActivity.class));
                }
            });
        }

        if (btnHome != null) {
            btnHome.setOnClickListener(v -> {
                if (!(activity instanceof MainActivity)) {
                    Intent intent = new Intent(activity, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                }
            });
        }

        if (btnHistory != null) {
            btnHistory.setOnClickListener(v -> {
                if (!(activity instanceof WorkoutHistoryActivity)) {
                    activity.startActivity(
                            new Intent(activity, WorkoutHistoryActivity.class));
                }
            });
        }
    }
}