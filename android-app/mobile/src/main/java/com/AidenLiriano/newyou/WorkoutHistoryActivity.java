package com.AidenLiriano.newyou;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkoutHistoryActivity extends AppCompatActivity {

    private LinearLayout historyContainer;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static final String[] WORKOUT_NAMES = {
            "", "Running", "Swimming", "Biking",
            "Walking", "Hiking", "Meditation", "Strength Training", "Yoga"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_history);

        historyContainer = findViewById(R.id.historyContainer);
        Button backButton = findViewById(R.id.backButton);
        Button clearButton = findViewById(R.id.clearButton);

        backButton.setOnClickListener(v -> finish());

        clearButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Clear History")
                    .setMessage("Are you sure you want to delete all workout history?")
                    .setPositiveButton("Delete", (dialog, which) -> clearHistory())
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        loadHistory();
    }

    private void loadHistory() {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            List<Activity> activities = db.appDao().getActivitiesForUser(1);

            runOnUiThread(() -> {
                historyContainer.removeAllViews();

                if (activities.isEmpty()) {
                    TextView empty = new TextView(this);
                    empty.setText("No workout history yet.");
                    empty.setTextSize(16f);
                    empty.setPadding(32, 32, 32, 32);
                    historyContainer.addView(empty);
                    return;
                }

                for (Activity activity : activities) {
                    addActivityCard(activity, db);
                }
            });
        });
    }

    private void addActivityCard(Activity activity, AppDatabase db) {
        // Outer card container
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

        // --- Compact summary row ---
        String workoutName = activity.activityType >= 1 && activity.activityType <= 8
                ? WORKOUT_NAMES[activity.activityType] : "Unknown";

        String date = new SimpleDateFormat("MMM dd, yyyy  hh:mm a", Locale.getDefault())
                .format(new Date(activity.startTime));

        TextView summaryText = new TextView(this);
        summaryText.setTextSize(16f);
        summaryText.setTypeface(null, android.graphics.Typeface.BOLD);
        summaryText.setText(workoutName + "  —  " + date);
        card.addView(summaryText);

        // --- Detail container (hidden by default) ---
        LinearLayout detailContainer = new LinearLayout(this);
        detailContainer.setOrientation(LinearLayout.VERTICAL);
        detailContainer.setVisibility(View.GONE);
        detailContainer.setPadding(0, 12, 0, 0);

        // Load detail data on background thread
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

        // Tap card to expand/collapse details
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
            case 1: { // Running
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
            case 2: { // Swimming
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
            case 3: { // Biking
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
            case 4: { // Walking
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
            case 5: { // Hiking
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
            case 6: { // Meditation
                MeditationData d = dao.getMeditationData(id);
                if (d != null) {
                    sb.append("Duration: ").append(formatDuration(d.duration)).append("\n");
                    sb.append("Avg Heart Rate: ").append(d.heartRate).append(" bpm\n");
                    sb.append("Starting Heart Rate: ").append(d.heartRateStart).append(" bpm\n");
                    sb.append("Ending Heart Rate: ").append(d.heartRateEnd).append(" bpm");
                }
                break;
            }
            case 7: { // Strength Training
                StrengthTrainingData d = dao.getStrengthTrainingData(id);
                if (d != null) {
                    sb.append("Duration: ").append(formatDuration(d.duration)).append("\n");
                    sb.append("Heart Rate: ").append(d.heartRate).append(" bpm\n");
                    sb.append("Calories: ").append(d.calories).append(" kcal");
                }
                break;
            }
            case 8: { // Yoga
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

    // Formats seconds into "Xh Xm Xs" human readable string
    private String formatDuration(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return hours + "h " + minutes + "m " + seconds + "s";
        } else if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }

    // Formats pace in min/km into "Xm Xs /km"
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
                loadHistory();
            });
        });
    }
}