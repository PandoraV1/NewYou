package com.AidenLiriano.newyou;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;

public class NavHelper {

    private static final int COLOR_ACTIVE   = Color.parseColor("#7DB800"); // darker green
    private static final int COLOR_INACTIVE = Color.parseColor("#98CD00"); // normal green

    public static void setup(Activity activity) {
        View navBar = activity.findViewById(R.id.navBar);
        if (navBar == null) return;

        LinearLayout navSettings = navBar.findViewById(R.id.navSettings);
        LinearLayout navHome     = navBar.findViewById(R.id.navHome);
        LinearLayout navHistory  = navBar.findViewById(R.id.navHistory);

        if (navSettings == null || navHome == null || navHistory == null) return;

        // Determine which screen is currently active
        String currentClass = activity.getClass().getSimpleName();

        // Reset all to inactive
        navSettings.setBackgroundColor(COLOR_INACTIVE);
        navHome.setBackgroundColor(COLOR_INACTIVE);
        navHistory.setBackgroundColor(COLOR_INACTIVE);

        // Highlight the current screen
        switch (currentClass) {
            case "SettingsActivity":
                navSettings.setBackgroundColor(COLOR_ACTIVE);
                break;
            case "MainActivity":
                navHome.setBackgroundColor(COLOR_ACTIVE);
                break;
            case "WorkoutHistoryActivity":
                navHistory.setBackgroundColor(COLOR_ACTIVE);
                break;
        }

        // Set click listeners
        navSettings.setOnClickListener(v -> {
            if (!(activity instanceof SettingsActivity)) {
                activity.startActivity(
                        new Intent(activity, SettingsActivity.class));
            }
        });

        navHome.setOnClickListener(v -> {
            if (!(activity instanceof MainActivity)) {
                activity.startActivity(
                        new Intent(activity, MainActivity.class));
            }
        });

        navHistory.setOnClickListener(v -> {
            if (!(activity instanceof WorkoutHistoryActivity)) {
                activity.startActivity(
                        new Intent(activity, WorkoutHistoryActivity.class));
            }
        });
    }
}