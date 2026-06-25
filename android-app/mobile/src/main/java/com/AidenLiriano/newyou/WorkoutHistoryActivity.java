package com.AidenLiriano.newyou;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkoutHistoryActivity extends AppCompatActivity {

    private LinearLayout historyContainer;
    private EditText searchBox;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private List<Activity> allActivities = new ArrayList<>();
    private int currentUserId = -1;

    private static final int COLOR_CARD      = Color.parseColor("#FFFADC");
    private static final int COLOR_CARD_ALT  = Color.parseColor("#FFF5C0");
    private static final int COLOR_TEXT      = Color.parseColor("#1C1C1E");
    private static final int COLOR_SUBTEXT   = Color.parseColor("#555555");
    private static final int COLOR_DIVIDER   = Color.parseColor("#DDD8A0");
    private static final int COLOR_GREEN     = Color.parseColor("#7DB800");
    private static final int COLOR_ACCENT    = Color.parseColor("#4A7A00");

    private static final String[] WORKOUT_NAMES = {
            "", "Running", "Swimming", "Biking",
            "Walking", "Hiking", "Meditation", "Strength Training", "Yoga"
    };

    private static final String[] WORKOUT_EMOJIS = {
            "", "🏃", "🏊", "🚴", "🚶", "🥾", "🧘", "🏋️", "🧘"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_history);

        historyContainer = findViewById(R.id.historyContainer);
        searchBox        = findViewById(R.id.searchBox);
        Button clearButton = findViewById(R.id.clearButton);

        clearButton.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Clear History")
                        .setMessage("Are you sure you want to delete all workout history? This cannot be undone.")
                        .setPositiveButton("Delete Everything", (dialog, which) -> clearHistory())
                        .setNegativeButton("Cancel", null)
                        .show()
        );

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAndDisplay(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        NavHelper.setup(this);

        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            User user = db.appDao().getFirstUser();
            if (user != null) currentUserId = user.userId;
            runOnUiThread(this::loadHistory);
        });
    }

    private void loadHistory() {
        if (currentUserId == -1) return;
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            List<Activity> activities = db.appDao().getActivitiesForUser(currentUserId);
            runOnUiThread(() -> {
                allActivities = activities;
                filterAndDisplay(searchBox.getText().toString().trim());
            });
        });
    }

    private void filterAndDisplay(String query) {
        List<Activity> filtered = new ArrayList<>();
        if (query.isEmpty()) {
            filtered.addAll(allActivities);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Activity activity : allActivities) {
                String workoutName = activity.activityType >= 1
                        && activity.activityType <= 8
                        ? WORKOUT_NAMES[activity.activityType].toLowerCase() : "";
                boolean matchesType = workoutName.contains(lowerQuery);
                boolean matchesDate = matchesDate(activity.startTime, lowerQuery);
                if (matchesType || matchesDate) filtered.add(activity);
            }
        }
        drawList(filtered);
    }

    private boolean matchesDate(long timestamp, String query) {
        Date date = new Date(timestamp);
        String monthDay     = new SimpleDateFormat("MMM d", Locale.getDefault()).format(date).toLowerCase();
        String numericDate  = new SimpleDateFormat("M/d", Locale.getDefault()).format(date);
        String fullMonthDay = new SimpleDateFormat("MMMM d", Locale.getDefault()).format(date).toLowerCase();
        String withYear     = new SimpleDateFormat("MMM d yyyy", Locale.getDefault()).format(date).toLowerCase();
        return monthDay.contains(query) || numericDate.contains(query)
                || fullMonthDay.contains(query) || withYear.contains(query);
    }

    private void drawList(List<Activity> activities) {
        historyContainer.removeAllViews();

        if (activities.isEmpty()) {
            LinearLayout emptyLayout = new LinearLayout(this);
            emptyLayout.setOrientation(LinearLayout.VERTICAL);
            emptyLayout.setGravity(android.view.Gravity.CENTER);
            emptyLayout.setPadding(32, 64, 32, 64);

            TextView emptyIcon = new TextView(this);
            emptyIcon.setText("📋");
            emptyIcon.setTextSize(48f);
            emptyIcon.setGravity(android.view.Gravity.CENTER);
            emptyLayout.addView(emptyIcon);

            TextView emptyText = new TextView(this);
            emptyText.setText(allActivities.isEmpty()
                    ? "No workout history yet.\nStart a workout to see it here!"
                    : "No workouts match your search.");
            emptyText.setTextSize(16f);
            emptyText.setTextColor(COLOR_SUBTEXT);
            emptyText.setGravity(android.view.Gravity.CENTER);
            emptyText.setPadding(0, 16, 0, 0);
            emptyText.setLineSpacing(8f, 1f);
            emptyLayout.addView(emptyText);

            historyContainer.addView(emptyLayout);
            return;
        }

        // Section header
        TextView countLabel = new TextView(this);
        countLabel.setText(activities.size() + " workout" + (activities.size() != 1 ? "s" : "") + " found");
        countLabel.setTextSize(13f);
        countLabel.setTextColor(COLOR_SUBTEXT);
        countLabel.setTypeface(null, Typeface.ITALIC);
        countLabel.setPadding(8, 0, 8, 12);
        historyContainer.addView(countLabel);

        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        for (int i = 0; i < activities.size(); i++) {
            addActivityCard(activities.get(i), db, i);
        }
    }

    private void addActivityCard(Activity activity, AppDatabase db, int index) {

        // Alternating card colors
        int cardColor = index % 2 == 0 ? COLOR_CARD : COLOR_CARD_ALT;

        // Determine name and emoji
        LinearLayout outerCard = new LinearLayout(this);
        outerCard.setOrientation(LinearLayout.VERTICAL);
        outerCard.setBackgroundColor(cardColor);
        LinearLayout.LayoutParams outerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        outerParams.setMargins(4, 4, 4, 8);
        outerCard.setLayoutParams(outerParams);
        outerCard.setPadding(16, 16, 16, 16);

        LinearLayout cardWithAccent = new LinearLayout(this);
        cardWithAccent.setOrientation(LinearLayout.HORIZONTAL);
        cardWithAccent.setBackgroundColor(cardColor);

        View accentBar = new View(this);
        LinearLayout.LayoutParams accentParams = new LinearLayout.LayoutParams(6,
                LinearLayout.LayoutParams.MATCH_PARENT);
        accentParams.setMargins(0, 0, 16, 0);
        accentBar.setLayoutParams(accentParams);
        accentBar.setBackgroundColor(COLOR_GREEN);

        LinearLayout cardContent = new LinearLayout(this);
        cardContent.setOrientation(LinearLayout.VERTICAL);
        cardContent.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView nameText = new TextView(this);
        nameText.setTextSize(18f);
        nameText.setTypeface(null, Typeface.BOLD);
        nameText.setTextColor(COLOR_TEXT);
        nameText.setPadding(0, 0, 0, 4);
        cardContent.addView(nameText);

        String date = new SimpleDateFormat("EEE, MMM dd yyyy  •  hh:mm a",
                Locale.getDefault()).format(new Date(activity.startTime));
        TextView dateText = new TextView(this);
        dateText.setText(date);
        dateText.setTextSize(12f);
        dateText.setTextColor(COLOR_SUBTEXT);
        dateText.setPadding(0, 0, 0, 8);
        cardContent.addView(dateText);

        TextView expandHint = new TextView(this);
        expandHint.setText("Tap to see details  ▼");
        expandHint.setTextSize(12f);
        expandHint.setTextColor(COLOR_ACCENT);
        expandHint.setTypeface(null, Typeface.ITALIC);
        cardContent.addView(expandHint);

        LinearLayout detailContainer = new LinearLayout(this);
        detailContainer.setOrientation(LinearLayout.VERTICAL);
        detailContainer.setVisibility(View.GONE);
        cardContent.addView(detailContainer);

        cardWithAccent.addView(accentBar);
        cardWithAccent.addView(cardContent);
        outerCard.addView(cardWithAccent);

        outerCard.setOnClickListener(v -> {
            if (detailContainer.getVisibility() == View.GONE) {
                detailContainer.setVisibility(View.VISIBLE);
                expandHint.setText("Tap to collapse  ▲");
            } else {
                detailContainer.setVisibility(View.GONE);
                expandHint.setText("Tap to see details  ▼");
            }
        });

        historyContainer.addView(outerCard);

        executor.execute(() -> {
            String workoutName;
            String emoji;

            if (activity.activityType ==
                    PhoneListenerService.CUSTOM_WORKOUT_TYPE_BASE) {
                CustomWorkoutData cwd =
                        db.appDao().getCustomWorkoutData(activity.activityId);
                if (cwd != null) {
                    CustomWorkout cw =
                            db.appDao().getCustomWorkout(cwd.customWorkoutId);
                    workoutName = cw != null ? cw.name : "Custom Workout";
                } else {
                    workoutName = "Custom Workout";
                }
                emoji = "🛠️";
            } else {
                workoutName = activity.activityType >= 1
                        && activity.activityType <= 8
                        ? WORKOUT_NAMES[activity.activityType] : "Unknown";
                emoji = activity.activityType >= 1
                        && activity.activityType <= 8
                        ? WORKOUT_EMOJIS[activity.activityType] : "💪";
            }

            String details = getDetailsForActivity(activity, db);
            final String finalName  = workoutName;
            final String finalEmoji = emoji;

            runOnUiThread(() -> {
                nameText.setText(finalEmoji + "  " + finalName);

                View divider = new View(this);
                LinearLayout.LayoutParams divParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 1);
                divParams.setMargins(0, 12, 0, 12);
                divider.setLayoutParams(divParams);
                divider.setBackgroundColor(COLOR_DIVIDER);
                detailContainer.addView(divider);

                String[] lines = details.split("\n");
                for (String line : lines) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split(": ", 2);

                    LinearLayout row = new LinearLayout(this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    LinearLayout.LayoutParams rowParams =
                            new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                    rowParams.setMargins(0, 4, 0, 4);
                    row.setLayoutParams(rowParams);

                    if (parts.length == 2) {
                        TextView labelView = new TextView(this);
                        labelView.setText(parts[0]);
                        labelView.setTextSize(13f);
                        labelView.setTextColor(COLOR_SUBTEXT);
                        labelView.setLayoutParams(new LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                        row.addView(labelView);

                        TextView valueView = new TextView(this);
                        valueView.setText(parts[1]);
                        valueView.setTextSize(13f);
                        valueView.setTextColor(COLOR_TEXT);
                        valueView.setTypeface(null, Typeface.BOLD);
                        row.addView(valueView);
                    } else {
                        TextView fullLine = new TextView(this);
                        fullLine.setText(line);
                        fullLine.setTextSize(13f);
                        fullLine.setTextColor(COLOR_TEXT);
                        row.addView(fullLine);
                    }

                    detailContainer.addView(row);
                }
            });
        });
    }

    private String getDetailsForActivity(Activity activity, AppDatabase db) {
        AppDao dao = db.appDao();
        int id     = activity.activityId;
        StringBuilder sb = new StringBuilder();

        // Custom workout (type 100)
        if (activity.activityType == PhoneListenerService.CUSTOM_WORKOUT_TYPE_BASE) {
            CustomWorkoutData d = dao.getCustomWorkoutData(id);
            if (d != null) {
                if (d.duration > 0)
                    sb.append("Duration: ").append(formatDuration(d.duration)).append("\n");
                if (d.heartRate > 0)
                    sb.append("Heart Rate: ").append(d.heartRate).append(" bpm\n");
                if (d.calories > 0)
                    sb.append("Calories: ").append(d.calories).append(" kcal\n");
                if (d.stepCount > 0)
                    sb.append("Steps: ").append(d.stepCount).append("\n");
                if (d.distance > 0)
                    sb.append("Distance: ").append(String.format("%.2f km", d.distance)).append("\n");
                if (d.pace > 0)
                    sb.append("Pace: ").append(formatPace(d.pace)).append("\n");
                if (d.speed > 0)
                    sb.append("Speed: ").append(String.format("%.1f km/h", d.speed)).append("\n");
                if (d.elevationGain > 0)
                    sb.append("Elevation Gain: ").append(
                            String.format("%.1f m", d.elevationGain)).append("\n");
                if (d.laps > 0)
                    sb.append("Laps: ").append(d.laps);
            }
            return sb.length() == 0 ? "No detail data recorded." : sb.toString().trim();
        }

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
            default: sb.append("No details available.");
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
        if (currentUserId == -1) return;
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