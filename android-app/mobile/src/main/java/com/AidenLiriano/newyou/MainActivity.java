package com.AidenLiriano.newyou;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements MessageClient.OnMessageReceivedListener {

    private TextView statusTextView;
    private TextView durationTextView;
    private TextView readyTextView;

    // Summary stats
    private TextView statTotalWorkouts;
    private TextView statWeeksTracked;
    private TextView statAvgWorkoutsPerWeek;

    // Heart rate stats
    private TextView statAvgHROverall;
    private TextView statHRByActivity;

    // Calorie stats
    private TextView statAvgCaloriesOverall;
    private TextView statCaloriesByActivity;

    // Time stats
    private TextView statAvgTimePerSession;
    private TextView statAvgTimePerWeek;
    private TextView statTimeByActivity;

    // Per-activity stats
    private TextView statRunning;
    private TextView statSwimming;
    private TextView statBiking;
    private TextView statWalking;
    private TextView statHiking;
    private TextView statMeditation;
    private TextView statStrength;
    private TextView statYoga;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Status views
        statusTextView  = findViewById(R.id.statusText);
        durationTextView = findViewById(R.id.durationText);
        readyTextView   = findViewById(R.id.readyText);

        // Summary
        statTotalWorkouts      = findViewById(R.id.statTotalWorkouts);
        statWeeksTracked       = findViewById(R.id.statWeeksTracked);
        statAvgWorkoutsPerWeek = findViewById(R.id.statAvgWorkoutsPerWeek);

        // Heart rate
        statAvgHROverall   = findViewById(R.id.statAvgHROverall);
        statHRByActivity   = findViewById(R.id.statHRByActivity);

        // Calories
        statAvgCaloriesOverall  = findViewById(R.id.statAvgCaloriesOverall);
        statCaloriesByActivity  = findViewById(R.id.statCaloriesByActivity);

        // Time
        statAvgTimePerSession = findViewById(R.id.statAvgTimePerSession);
        statAvgTimePerWeek    = findViewById(R.id.statAvgTimePerWeek);
        statTimeByActivity    = findViewById(R.id.statTimeByActivity);

        // Per-activity
        statRunning    = findViewById(R.id.statRunning);
        statSwimming   = findViewById(R.id.statSwimming);
        statBiking     = findViewById(R.id.statBiking);
        statWalking    = findViewById(R.id.statWalking);
        statHiking     = findViewById(R.id.statHiking);
        statMeditation = findViewById(R.id.statMeditation);
        statStrength   = findViewById(R.id.statStrength);
        statYoga       = findViewById(R.id.statYoga);

        if (statusTextView != null) statusTextView.setText("Waiting for Watch...");
        if (durationTextView != null) durationTextView.setVisibility(View.GONE);
        if (readyTextView != null) readyTextView.setVisibility(View.GONE);

        NavHelper.setup(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Wearable.getMessageClient(this).addListener(this);
        loadStats();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.getMessageClient(this).removeListener(this);
    }

    private void loadStats() {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            AppDao dao = db.appDao();

            // --- Summary ---
            int totalWorkouts = dao.getTotalActivityCount(1);
            long earliestTime = dao.getEarliestActivityTime(1);
            long now = System.currentTimeMillis();
            long msPerWeek = 7L * 24 * 60 * 60 * 1000;
            int weeksTracked = totalWorkouts == 0 ? 0
                    : Math.max(1, (int) ((now - earliestTime) / msPerWeek));
            float avgPerWeek = weeksTracked == 0 ? 0
                    : (float) totalWorkouts / weeksTracked;

            // --- Heart rate averages ---
            float hrRun   = dao.getAvgHeartRateRunning(1);
            float hrSwim  = dao.getAvgHeartRateSwimming(1);
            float hrBike  = dao.getAvgHeartRateBiking(1);
            float hrWalk  = dao.getAvgHeartRateWalking(1);
            float hrHike  = dao.getAvgHeartRateHiking(1);
            float hrMed   = dao.getAvgHeartRateMeditation(1);
            float hrStr   = dao.getAvgHeartRateStrength(1);
            float hrYoga  = dao.getAvgHeartRateYoga(1);
            float overallHR = average(hrRun, hrSwim, hrBike, hrWalk,
                    hrHike, hrMed, hrStr, hrYoga);

            // --- Calorie averages ---
            float calRun  = dao.getAvgCaloriesRunning(1);
            float calSwim = dao.getAvgCaloriesSwimming(1);
            float calBike = dao.getAvgCaloriesBiking(1);
            float calWalk = dao.getAvgCaloriesWalking(1);
            float calHike = dao.getAvgCaloriesHiking(1);
            float calStr  = dao.getAvgCaloriesStrength(1);
            float calYoga = dao.getAvgCaloriesYoga(1);
            float overallCal = average(calRun, calSwim, calBike, calWalk,
                    calHike, calStr, calYoga);

            // --- Duration averages ---
            float durRun  = dao.getAvgDurationRunning(1);
            float durSwim = dao.getAvgDurationSwimming(1);
            float durBike = dao.getAvgDurationBiking(1);
            float durWalk = dao.getAvgDurationWalking(1);
            float durHike = dao.getAvgDurationHiking(1);
            float durMed  = dao.getAvgDurationMeditation(1);
            float durStr  = dao.getAvgDurationStrength(1);
            float durYoga = dao.getAvgDurationYoga(1);
            float overallAvgDur = average(durRun, durSwim, durBike, durWalk,
                    durHike, durMed, durStr, durYoga);

            // Total duration for weekly average
            long totalDurSec = dao.getTotalDurationRunning(1)
                    + dao.getTotalDurationSwimming(1)
                    + dao.getTotalDurationBiking(1)
                    + dao.getTotalDurationWalking(1)
                    + dao.getTotalDurationHiking(1)
                    + dao.getTotalDurationMeditation(1)
                    + dao.getTotalDurationStrength(1)
                    + dao.getTotalDurationYoga(1);
            long weeklyAvgDurSec = weeksTracked == 0 ? 0 : totalDurSec / weeksTracked;

            // --- Per activity totals ---
            int runCount   = dao.getRunningSessionCount(1);
            float runDist  = dao.getRunningTotalDistance(1);
            int runCal     = dao.getRunningTotalCalories(1);
            int runSteps   = dao.getRunningTotalSteps(1);

            int swimCount  = dao.getSwimmingSessionCount(1);
            int swimLaps   = dao.getSwimmingTotalLaps(1);
            int swimCal    = dao.getSwimmingTotalCalories(1);

            int bikeCount  = dao.getBikingSessionCount(1);
            float bikeDist = dao.getBikingTotalDistance(1);
            int bikeCal    = dao.getBikingTotalCalories(1);

            int walkCount  = dao.getWalkingSessionCount(1);
            float walkDist = dao.getWalkingTotalDistance(1);
            int walkSteps  = dao.getWalkingTotalSteps(1);
            int walkCal    = dao.getWalkingTotalCalories(1);

            int hikeCount  = dao.getHikingSessionCount(1);
            float hikeDist = dao.getHikingTotalDistance(1);
            float hikeElev = dao.getHikingTotalElevationGain(1);
            int hikeCal    = dao.getHikingTotalCalories(1);

            int medCount   = dao.getMeditationSessionCount(1);
            int strCount   = dao.getStrengthSessionCount(1);
            int strCal     = dao.getStrengthTotalCalories(1);
            int yogaCount  = dao.getYogaSessionCount(1);
            int yogaCal    = dao.getYogaTotalCalories(1);

            // Build final strings and post to UI
            runOnUiThread(() -> {
                // Summary
                statTotalWorkouts.setText("Total Workouts: " + totalWorkouts);
                statWeeksTracked.setText("Weeks Tracked: " + weeksTracked);
                statAvgWorkoutsPerWeek.setText(String.format(
                        "Avg Workouts/Week: %.1f", avgPerWeek));

                // Heart rate
                statAvgHROverall.setText(overallHR > 0
                        ? "Overall Avg: " + (int) overallHR + " bpm"
                        : "Overall Avg: No data yet");
                statHRByActivity.setText(buildHRByActivity(
                        hrRun, hrSwim, hrBike, hrWalk, hrHike, hrMed, hrStr, hrYoga));

                // Calories
                statAvgCaloriesOverall.setText(overallCal > 0
                        ? "Avg Per Session: " + (int) overallCal + " kcal"
                        : "Avg Per Session: No data yet");
                statCaloriesByActivity.setText(buildCalByActivity(
                        calRun, calSwim, calBike, calWalk, calHike, calStr, calYoga));

                // Time
                statAvgTimePerSession.setText("Avg Session: "
                        + (overallAvgDur > 0
                        ? formatDuration((long) overallAvgDur)
                        : "No data yet"));
                statAvgTimePerWeek.setText("Avg Per Week: "
                        + (weeklyAvgDurSec > 0
                        ? formatDuration(weeklyAvgDurSec)
                        : "No data yet"));
                statTimeByActivity.setText(buildTimeByActivity(
                        durRun, durSwim, durBike, durWalk, durHike, durMed, durStr, durYoga));

                // Per activity
                statRunning.setText(runCount == 0 ? "No sessions yet"
                        : runCount + " sessions  |  " + String.format("%.2f km", runDist)
                        + "  |  " + runCal + " kcal  |  " + runSteps + " steps");

                statSwimming.setText(swimCount == 0 ? "No sessions yet"
                        : swimCount + " sessions  |  " + swimLaps + " total laps"
                        + "  |  " + swimCal + " kcal");

                statBiking.setText(bikeCount == 0 ? "No sessions yet"
                        : bikeCount + " sessions  |  " + String.format("%.2f km", bikeDist)
                        + "  |  " + bikeCal + " kcal");

                statWalking.setText(walkCount == 0 ? "No sessions yet"
                        : walkCount + " sessions  |  " + String.format("%.2f km", walkDist)
                        + "  |  " + walkSteps + " steps  |  " + walkCal + " kcal");

                statHiking.setText(hikeCount == 0 ? "No sessions yet"
                        : hikeCount + " sessions  |  " + String.format("%.2f km", hikeDist)
                        + "  |  " + String.format("%.0f m gain", hikeElev)
                        + "  |  " + hikeCal + " kcal");

                statMeditation.setText(medCount == 0 ? "No sessions yet"
                        : medCount + " sessions");

                statStrength.setText(strCount == 0 ? "No sessions yet"
                        : strCount + " sessions  |  " + strCal + " kcal");

                statYoga.setText(yogaCount == 0 ? "No sessions yet"
                        : yogaCount + " sessions  |  " + yogaCal + " kcal");
            });
        });
    }

    // Helper to average only non-zero values
    private float average(float... values) {
        float sum = 0;
        int count = 0;
        for (float v : values) {
            if (v > 0) { sum += v; count++; }
        }
        return count == 0 ? 0 : sum / count;
    }

    private String buildHRByActivity(float run, float swim, float bike,
                                     float walk, float hike, float med,
                                     float str, float yoga) {
        StringBuilder sb = new StringBuilder();
        if (run  > 0) sb.append("Running: ").append((int) run).append(" bpm\n");
        if (swim > 0) sb.append("Swimming: ").append((int) swim).append(" bpm\n");
        if (bike > 0) sb.append("Biking: ").append((int) bike).append(" bpm\n");
        if (walk > 0) sb.append("Walking: ").append((int) walk).append(" bpm\n");
        if (hike > 0) sb.append("Hiking: ").append((int) hike).append(" bpm\n");
        if (med  > 0) sb.append("Meditation: ").append((int) med).append(" bpm\n");
        if (str  > 0) sb.append("Strength: ").append((int) str).append(" bpm\n");
        if (yoga > 0) sb.append("Yoga: ").append((int) yoga).append(" bpm");
        return sb.length() == 0 ? "No data yet" : sb.toString().trim();
    }

    private String buildCalByActivity(float run, float swim, float bike,
                                      float walk, float hike,
                                      float str, float yoga) {
        StringBuilder sb = new StringBuilder();
        if (run  > 0) sb.append("Running: ").append((int) run).append(" kcal\n");
        if (swim > 0) sb.append("Swimming: ").append((int) swim).append(" kcal\n");
        if (bike > 0) sb.append("Biking: ").append((int) bike).append(" kcal\n");
        if (walk > 0) sb.append("Walking: ").append((int) walk).append(" kcal\n");
        if (hike > 0) sb.append("Hiking: ").append((int) hike).append(" kcal\n");
        if (str  > 0) sb.append("Strength: ").append((int) str).append(" kcal\n");
        if (yoga > 0) sb.append("Yoga: ").append((int) yoga).append(" kcal");
        return sb.length() == 0 ? "No data yet" : sb.toString().trim();
    }

    private String buildTimeByActivity(float run, float swim, float bike,
                                       float walk, float hike, float med,
                                       float str, float yoga) {
        StringBuilder sb = new StringBuilder();
        if (run  > 0) sb.append("Running: ").append(formatDuration((long) run)).append("\n");
        if (swim > 0) sb.append("Swimming: ").append(formatDuration((long) swim)).append("\n");
        if (bike > 0) sb.append("Biking: ").append(formatDuration((long) bike)).append("\n");
        if (walk > 0) sb.append("Walking: ").append(formatDuration((long) walk)).append("\n");
        if (hike > 0) sb.append("Hiking: ").append(formatDuration((long) hike)).append("\n");
        if (med  > 0) sb.append("Meditation: ").append(formatDuration((long) med)).append("\n");
        if (str  > 0) sb.append("Strength: ").append(formatDuration((long) str)).append("\n");
        if (yoga > 0) sb.append("Yoga: ").append(formatDuration((long) yoga));
        return sb.length() == 0 ? "No data yet" : sb.toString().trim();
    }

    private String formatDuration(long totalSeconds) {
        long hours   = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        if (hours > 0)   return hours + "h " + minutes + "m " + seconds + "s";
        if (minutes > 0) return minutes + "m " + seconds + "s";
        return seconds + "s";
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        String path = messageEvent.getPath();

        if (path.startsWith("/workout/")) {
            final String workoutName = new String(messageEvent.getData());
            runOnUiThread(() -> {
                statusTextView.setText("Workout started: " + workoutName);
                if (durationTextView != null) durationTextView.setVisibility(View.GONE);
                if (readyTextView != null) readyTextView.setVisibility(View.GONE);
                Toast.makeText(this, "Workout Started: " + workoutName,
                        Toast.LENGTH_SHORT).show();
            });
        } else if (path.startsWith("/workout_stop/")) {
            try {
                String[] parts = path.replace("/workout_stop/", "").split("/");
                long durationSeconds = Long.parseLong(parts[2]);
                long hours   = durationSeconds / 3600;
                long minutes = (durationSeconds % 3600) / 60;
                long seconds = durationSeconds % 60;
                String formattedDuration = hours > 0
                        ? hours + "h " + minutes + "m " + seconds + "s"
                        : minutes > 0 ? minutes + "m " + seconds + "s"
                        : seconds + "s";
                final String workoutName = new String(messageEvent.getData());

                runOnUiThread(() -> {
                    statusTextView.setText("Workout ended: " + workoutName);
                    if (durationTextView != null) {
                        durationTextView.setText("Duration: " + formattedDuration);
                        durationTextView.setVisibility(View.VISIBLE);
                    }
                    if (readyTextView != null) {
                        readyTextView.setText("Ready to start next workout");
                        readyTextView.setVisibility(View.VISIBLE);
                    }
                    Toast.makeText(this, "Workout Complete!", Toast.LENGTH_SHORT).show();
                    // Reload stats after workout ends
                    loadStats();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}