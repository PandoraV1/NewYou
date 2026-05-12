package com.AidenLiriano.newyou;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkoutHistoryActivity extends AppCompatActivity {

    private LinearLayout historyContainer;
    private EditText searchBox;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Full unfiltered list loaded from database
    private List<Activity> allActivities = new ArrayList<>();

    private static final String[] WORKOUT_NAMES = {
            "", "Running", "Swimming", "Biking",
            "Walking", "Hiking", "Meditation", "Strength Training", "Yoga"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_history);

        historyContainer = findViewById(R.id.historyContainer);
        searchBox        = findViewById(R.id.searchBox);
        Button clearButton = findViewById(R.id.clearButton);

        NavHelper.setup(this);

        clearButton.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Clear History")
                        .setMessage("Are you sure you want to delete all workout history?")
                        .setPositiveButton("Delete", (dialog, which) -> clearHistory())
                        .setNegativeButton("Cancel", null)
                        .show()
        );

        // Filter in real time as the user types
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAndDisplay(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadHistory();
    }

    private void loadHistory() {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            List<Activity> activities = db.appDao().getActivitiesForUser(1);

            runOnUiThread(() -> {
                allActivities = activities;
                // Show full list on load with whatever is currently in the search box
                filterAndDisplay(searchBox.getText().toString().trim());
            });
        });
    }

    // Filters allActivities by the search query and redraws the list
    private void filterAndDisplay(String query) {
        List<Activity> filtered = new ArrayList<>();

        if (query.isEmpty()) {
            // No filter — show everything
            filtered.addAll(allActivities);
        } else {
            String lowerQuery = query.toLowerCase();

            for (Activity activity : allActivities) {
                // Check activity type name match
                String workoutName = activity.activityType >= 1
                        && activity.activityType <= 8
                        ? WORKOUT_NAMES[activity.activityType].toLowerCase()
                        : "";

                boolean matchesType = workoutName.contains(lowerQuery);

                // Check date match — try both "Apr 30" and "4/30" formats
                boolean matchesDate = matchesDate(activity.startTime, lowerQuery);

                if (matchesType || matchesDate) {
                    filtered.add(activity);
                }
            }
        }

        drawList(filtered);
    }

    // Returns true if the timestamp matches the query in either date format
    private boolean matchesDate(long timestamp, String query) {
        Date date = new Date(timestamp);

        // Format 1: "Apr 30" style
        String monthDay = new SimpleDateFormat("MMM d", Locale.getDefault())
                .format(date).toLowerCase();

        // Format 2: "4/30" style
        String numericDate = new SimpleDateFormat("M/d", Locale.getDefault())
                .format(date);

        // Format 3: Full month name "april 30"
        String fullMonthDay = new SimpleDateFormat("MMMM d", Locale.getDefault())
                .format(date).toLowerCase();

        // Format 4: Year included "Apr 30 2025"
        String withYear = new SimpleDateFormat("MMM d yyyy", Locale.getDefault())
                .format(date).toLowerCase();

        return monthDay.contains(query)
                || numericDate.contains(query)
                || fullMonthDay.contains(query)
                || withYear.contains(query);
    }

    // Clears and redraws the history container with the given list
    private void drawList(List<Activity> activities) {
        historyContainer.removeAllViews();

        if (activities.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(allActivities.isEmpty()
                    ? "No workout history yet."
                    : "No workouts match your search.");
            empty.setTextSize(16f);
            empty.setPadding(32, 32, 32, 32);
            historyContainer.addView(empty);
            return;
        }

        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());

        for (Activity activity : activities) {
            addActivityCard(activity, db);
        }
    }

    private void addActivityCard(Activity activity, AppDatabase db) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(24, 24, 24, 24);
        card.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(16, 16, 16, 8);
        card.setLayoutParams(cardParams);

        String workoutName = activity.activityType >= 1 && activity.activityType <= 8
                ? WORKOUT_NAMES[activity.activityType] : "Unknown";

        String date = new SimpleDateFormat("MMM dd, yyyy  hh:mm a", Locale.getDefault())
                .format(new Date(activity.startTime));

        TextView summaryText = new TextView(this);
        summaryText.setTextSize(16f);
        summaryText.setTypeface(null, android.graphics.Typeface.BOLD);
        summaryText.setText(workoutName + "  —  " + date);
        card.addView(summaryText);

        LinearLayout detailContainer = new LinearLayout(this);
        detailContainer.setOrientation(LinearLayout.VERTICAL);
        detailContainer.setVisibility(View.GONE);
        detailContainer.setPadding(0, 12, 0, 0);

        executor.execute(() -> {
            String details = getDetailsForActivity(activity, db);
            runOnUiThread(() -> {
                TextView detailText = new TextView(this);
                detailText.setTextSize(14f);
                detailText.setLineSpacing(6f, 1f);
                detailText.setText(details);
                detailContainer.addView(detailText);
            });
        });

        card.addView(detailContainer);

        card.setOnClickListener(v -> {
            if (detailContainer.getVisibility() == View.GONE) {
                detailContainer.setVisibility(View.VISIBLE);
            } else {
                detailContainer.setVisibility(View.GONE);
            }
        });

        historyContainer.addView(card);
    }

    private String getDetailsForActivity(Activity activity, AppDatabase db) {
        AppDao dao = db.appDao();
        int id = activity.activityId;
        StringBuilder sb = new StringBuilder();

        switch (activity.activityType) {
            case 1: {
                RunningData d = dao.getRunningData(id);
                if (d != null) {
                    sb.append("Duration: ").append(formatDuration(d.duration)).append("\n");
                    sb.append("Distance: ").append(String.format("%.2f km", d.distance)).append("\n");
                    sb.append("Pace: ").append(formatPace(d.pace)).append("\n");
                    sb.append("Heart Rate: ").append(d.heartRate).append(" bpm\n");
                    sb.append("Calories: ").append(d.calories).append(" kcal\n");
                    sb.append("Steps: ").append(d.stepCount);
                }
                break;
            }
            case 2: {
                SwimmingData d = dao.getSwimmingData(id);
                if (d != null) {
                    sb.append("Duration: ").append(formatDuration(d.duration)).append("\n");
                    sb.append("Laps: ").append(d.laps).append("\n");
                    sb.append("Distance: ").append(String.format("%.2f km", d.distance)).append("\n");
                    sb.append("Pace: ").append(formatPace(d.pace)).append("\n");
                    sb.append("Heart Rate: ").append(d.heartRate).append(" bpm\n");
                    sb.append("Calories: ").append(d.calories).append(" kcal");
                }
                break;
            }
            case 3: {
                BikingData d = dao.getBikingData(id);
                if (d != null) {
                    sb.append("Duration: ").append(formatDuration(d.duration)).append("\n");
                    sb.append("Distance: ").append(String.format("%.2f km", d.distance)).append("\n");
                    sb.append("Speed: ").append(String.format("%.1f km/h", d.speed)).append("\n");
                    sb.append("Heart Rate: ").append(d.heartRate).append(" bpm\n");
                    sb.append("Calories: ").append(d.calories).append(" kcal");
                }
                break;
            }
            case 4: {
                WalkingData d = dao.getWalkingData(id);
                if (d != null) {
                    sb.append("Duration: ").append(formatDuration(d.duration)).append("\n");
                    sb.append("Distance: ").append(String.format("%.2f km", d.distance)).append("\n");
                    sb.append("Steps: ").append(d.stepCount).append("\n");
                    sb.append("Pace: ").append(formatPace(d.pace)).append("\n");
                    sb.append("Heart Rate: ").append(d.heartRate).append(" bpm\n");
                    sb.append("Calories: ").append(d.calories).append(" kcal");
                }
                break;
            }
            case 5: {
                HikingData d = dao.getHikingData(id);
                if (d != null) {
                    sb.append("Duration: ").append(formatDuration(d.duration)).append("\n");
                    sb.append("Distance: ").append(String.format("%.2f km", d.distance)).append("\n");
                    sb.append("Elevation Gain: ").append(String.format("%.1f m", d.elevationGain)).append("\n");
                    sb.append("Elevation Loss: ").append(String.format("%.1f m", d.elevationLoss)).append("\n");
                    sb.append("Pace: ").append(formatPace(d.pace)).append("\n");
                    sb.append("Heart Rate: ").append(d.heartRate).append(" bpm\n");
                    sb.append("Calories: ").append(d.calories).append(" kcal");
                }
                break;
            }
            case 6: {
                MeditationData d = dao.getMeditationData(id);
                if (d != null) {
                    sb.append("Duration: ").append(formatDuration(d.duration)).append("\n");
                    sb.append("Avg Heart Rate: ").append(d.heartRate).append(" bpm\n");
                    sb.append("Starting Heart Rate: ").append(d.heartRateStart).append(" bpm\n");
                    sb.append("Ending Heart Rate: ").append(d.heartRateEnd).append(" bpm");
                }
                break;
            }
            case 7: {
                StrengthTrainingData d = dao.getStrengthTrainingData(id);
                if (d != null) {
                    sb.append("Duration: ").append(formatDuration(d.duration)).append("\n");
                    sb.append("Heart Rate: ").append(d.heartRate).append(" bpm\n");
                    sb.append("Calories: ").append(d.calories).append(" kcal");
                }
                break;
            }
            case 8: {
                YogaData d = dao.getYogaData(id);
                if (d != null) {
                    sb.append("Duration: ").append(formatDuration(d.duration)).append("\n");
                    sb.append("Heart Rate: ").append(d.heartRate).append(" bpm\n");
                    sb.append("Calories: ").append(d.calories).append(" kcal");
                }
                break;
            }
            default:
                sb.append("No details available.");
        }

        return sb.toString();
    }

    private String formatDuration(long totalSeconds) {
        long hours   = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        if (hours > 0)   return hours + "h " + minutes + "m " + seconds + "s";
        if (minutes > 0) return minutes + "m " + seconds + "s";
        return seconds + "s";
    }

    private String formatPace(float paceMinPerKm) {
        if (paceMinPerKm <= 0) return "N/A";
        int minutes = (int) paceMinPerKm;
        int seconds = (int) ((paceMinPerKm - minutes) * 60);
        return minutes + "m " + seconds + "s /km";
    }

    private void clearHistory() {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            db.appDao().clearAllActivities();
            runOnUiThread(() -> {
                Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();
                allActivities.clear();
                searchBox.setText("");
                filterAndDisplay("");
            });
        });
    }
}