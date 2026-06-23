package com.AidenLiriano.newyou;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity
        implements MessageClient.OnMessageReceivedListener {

    private LinearLayout chartsContainer;
    private ConfettiView confettiView;
    private HealthOverviewView healthOverviewRings;
    private LinearLayout healthOverviewLegend;
    private TextView healthOverviewMessage;

    private TextView statusTextView;
    private TextView durationTextView;
    private TextView readyTextView;

    private TextView statTotalWorkouts;
    private TextView statWeeksTracked;
    private TextView statAvgWorkoutsPerWeek;
    private TextView statAvgHROverall;
    private TextView statHRByActivity;
    private TextView statAvgCaloriesOverall;
    private TextView statCaloriesByActivity;
    private TextView statAvgTimePerSession;
    private TextView statAvgTimePerWeek;
    private TextView statTimeByActivity;
    private TextView statRunning;
    private TextView statSwimming;
    private TextView statBiking;
    private TextView statWalking;
    private TextView statHiking;
    private TextView statMeditation;
    private TextView statStrength;
    private TextView statYoga;

    private int currentUserId = -1;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Ring legend colors
    private static final int[] RING_COLORS = {
            Color.parseColor("#EF5350"),
            Color.parseColor("#98CD00"),
            Color.parseColor("#42A5F5"),
            Color.parseColor("#FFA726"),
            Color.parseColor("#AB47BC"),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusTextView   = findViewById(R.id.statusText);
        durationTextView = findViewById(R.id.durationText);
        readyTextView    = findViewById(R.id.readyText);

        statTotalWorkouts      = findViewById(R.id.statTotalWorkouts);
        statWeeksTracked       = findViewById(R.id.statWeeksTracked);
        statAvgWorkoutsPerWeek = findViewById(R.id.statAvgWorkoutsPerWeek);
        statAvgHROverall       = findViewById(R.id.statAvgHROverall);
        statHRByActivity       = findViewById(R.id.statHRByActivity);
        statAvgCaloriesOverall = findViewById(R.id.statAvgCaloriesOverall);
        statCaloriesByActivity = findViewById(R.id.statCaloriesByActivity);
        statAvgTimePerSession  = findViewById(R.id.statAvgTimePerSession);
        statAvgTimePerWeek     = findViewById(R.id.statAvgTimePerWeek);
        statTimeByActivity     = findViewById(R.id.statTimeByActivity);
        statRunning            = findViewById(R.id.statRunning);
        statSwimming           = findViewById(R.id.statSwimming);
        statBiking             = findViewById(R.id.statBiking);
        statWalking            = findViewById(R.id.statWalking);
        statHiking             = findViewById(R.id.statHiking);
        statMeditation         = findViewById(R.id.statMeditation);
        statStrength           = findViewById(R.id.statStrength);
        statYoga               = findViewById(R.id.statYoga);

        healthOverviewRings   = findViewById(R.id.healthOverviewRings);
        healthOverviewLegend  = findViewById(R.id.healthOverviewLegend);
        healthOverviewMessage = findViewById(R.id.healthOverviewMessage);
        confettiView          = findViewById(R.id.confettiView);
        chartsContainer       = findViewById(R.id.chartsContainer);

        if (statusTextView != null)  statusTextView.setText("Waiting for Watch...");
        if (durationTextView != null) durationTextView.setVisibility(View.GONE);
        if (readyTextView != null)   readyTextView.setVisibility(View.GONE);

        NavHelper.setup(this);

        LinearLayout recommendationsButton = findViewById(R.id.recommendationsButton);
        recommendationsButton.setOnClickListener(v ->
                startActivity(new Intent(this, RecommendationsActivity.class)));

        LinearLayout predictionsButton = findViewById(R.id.predictionsButton);
        predictionsButton.setOnClickListener(v ->
                startActivity(new Intent(this, HealthPredictionsActivity.class)));

        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            User user = db.appDao().getFirstUser();
            if (user != null) currentUserId = user.userId;

            runOnUiThread(() ->
                    TestDataSeeder.seedTestData(
                            getApplicationContext(),
                            currentUserId,
                            this::loadStats
                    )
            );
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Wearable.getMessageClient(this).addListener(this);
        if (currentUserId != -1) loadStats();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.getMessageClient(this).removeListener(this);
    }

    // Health overview rings builder
    private void updateHealthOverview(
            User user, float avgHeartRate,
            float weeklyActiveMinutes, float weeklySessionCount,
            int totalSessions, float avgCaloriesPerSession) {

        FitnessScoreEngine.FitnessScore score = FitnessScoreEngine.calculate(
                user, avgHeartRate, weeklyActiveMinutes,
                weeklySessionCount, totalSessions, avgCaloriesPerSession);

        healthOverviewRings.setFitnessScore(score);

        // Build legend
        healthOverviewLegend.removeAllViews();
        String[] labels  = {
                score.cardioLabel,
                score.activityLabel,
                score.consistencyLabel,
                score.weightLabel,
                score.recoveryLabel
        };
        int[] scores = {
                score.cardioScore,
                score.activityScore,
                score.consistencyScore,
                score.weightScore,
                score.recoveryScore
        };

        for (int i = 0; i < labels.length; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 4, 0, 4);
            row.setLayoutParams(rowParams);

            // Color dot
            View dot = new View(this);
            LinearLayout.LayoutParams dotParams =
                    new LinearLayout.LayoutParams(12, 12);
            dotParams.setMargins(0, 0, 8, 0);
            dotParams.gravity = android.view.Gravity.CENTER_VERTICAL;
            dot.setLayoutParams(dotParams);
            dot.setBackgroundColor(RING_COLORS[i]);
            row.addView(dot);

            // Label + score
            TextView label = new TextView(this);
            label.setText(labels[i] + "  " + scores[i] + "%");
            label.setTextSize(12f);
            label.setTextColor(Color.parseColor("#1C1C1E"));
            row.addView(label);

            healthOverviewLegend.addView(row);
        }

        healthOverviewMessage.setText(
                score.overallMessage + "\n\n" +
                        "❤️ " + score.cardioMessage + "\n" +
                        "🏃 " + score.activityMessage + "\n" +
                        "📅 " + score.consistencyMessage
        );
    }

    // Congratulation dialog
    private void showCongratulationsDialog(String workoutName,
                                           String formattedDuration) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_congratulations);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT));
        }

        TextView titleView = dialog.findViewById(R.id.congratsTitle);
        TextView bodyView  = dialog.findViewById(R.id.congratsBody);
        TextView btnView   = dialog.findViewById(R.id.congratsButton);

        if (titleView != null)
            titleView.setText("🎉 Workout Complete!");
        if (bodyView != null)
            bodyView.setText("Great job finishing your " + workoutName
                    + " session!\nDuration: " + formattedDuration);
        if (btnView != null)
            btnView.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        // Auto dismiss after 4 seconds
        mainHandler.postDelayed(dialog::dismiss, 4000);
    }

    // Confetti launcher
    private void launchConfetti() {
        if (confettiView == null) return;
        confettiView.setVisibility(View.VISIBLE);
        confettiView.startConfetti();
        // Stop after 3.5 seconds
        mainHandler.postDelayed(() -> {
            confettiView.stopConfetti();
            confettiView.setVisibility(View.GONE);
        }, 3500);
    }

    // Stats loader
    private void loadStats() {
        if (currentUserId == -1) return;

        executor.execute(() -> {
            AppDatabase db  = AppDatabase.getDatabase(getApplicationContext());
            AppDao dao      = db.appDao();
            int uid         = currentUserId;

            int totalWorkouts = dao.getTotalActivityCount(uid);
            long earliestTime = dao.getEarliestActivityTime(uid);
            long now          = System.currentTimeMillis();
            long msPerWeek    = 7L * 24 * 60 * 60 * 1000;
            int weeksTracked  = totalWorkouts == 0 ? 0
                    : Math.max(1, (int) ((now - earliestTime) / msPerWeek));
            float avgPerWeek  = weeksTracked == 0 ? 0
                    : (float) totalWorkouts / weeksTracked;

            float hrRun  = dao.getAvgHeartRateRunning(uid);
            float hrSwim = dao.getAvgHeartRateSwimming(uid);
            float hrBike = dao.getAvgHeartRateBiking(uid);
            float hrWalk = dao.getAvgHeartRateWalking(uid);
            float hrHike = dao.getAvgHeartRateHiking(uid);
            float hrMed  = dao.getAvgHeartRateMeditation(uid);
            float hrStr  = dao.getAvgHeartRateStrength(uid);
            float hrYoga = dao.getAvgHeartRateYoga(uid);
            float overallHR = average(hrRun, hrSwim, hrBike, hrWalk,
                    hrHike, hrMed, hrStr, hrYoga);

            float calRun  = dao.getAvgCaloriesRunning(uid);
            float calSwim = dao.getAvgCaloriesSwimming(uid);
            float calBike = dao.getAvgCaloriesBiking(uid);
            float calWalk = dao.getAvgCaloriesWalking(uid);
            float calHike = dao.getAvgCaloriesHiking(uid);
            float calStr  = dao.getAvgCaloriesStrength(uid);
            float calYoga = dao.getAvgCaloriesYoga(uid);
            float overallCal = average(calRun, calSwim, calBike, calWalk,
                    calHike, calStr, calYoga);

            float durRun  = dao.getAvgDurationRunning(uid);
            float durSwim = dao.getAvgDurationSwimming(uid);
            float durBike = dao.getAvgDurationBiking(uid);
            float durWalk = dao.getAvgDurationWalking(uid);
            float durHike = dao.getAvgDurationHiking(uid);
            float durMed  = dao.getAvgDurationMeditation(uid);
            float durStr  = dao.getAvgDurationStrength(uid);
            float durYoga = dao.getAvgDurationYoga(uid);
            float overallAvgDur = average(durRun, durSwim, durBike, durWalk,
                    durHike, durMed, durStr, durYoga);

            long totalDurSec = dao.getTotalDurationRunning(uid)
                    + dao.getTotalDurationSwimming(uid)
                    + dao.getTotalDurationBiking(uid)
                    + dao.getTotalDurationWalking(uid)
                    + dao.getTotalDurationHiking(uid)
                    + dao.getTotalDurationMeditation(uid)
                    + dao.getTotalDurationStrength(uid)
                    + dao.getTotalDurationYoga(uid);
            long weeklyAvgDurSec = weeksTracked == 0 ? 0 : totalDurSec / weeksTracked;
            float avgCalPerSession = totalWorkouts > 0
                    ? (dao.getRunningTotalCalories(uid)
                    + dao.getSwimmingTotalCalories(uid)
                    + dao.getBikingTotalCalories(uid)
                    + dao.getWalkingTotalCalories(uid)
                    + dao.getHikingTotalCalories(uid)
                    + dao.getStrengthTotalCalories(uid)
                    + dao.getYogaTotalCalories(uid)) / (float) totalWorkouts
                    : 0f;

            int   runCount  = dao.getRunningSessionCount(uid);
            float runDist   = dao.getRunningTotalDistance(uid);
            int   runCal    = dao.getRunningTotalCalories(uid);
            int   runSteps  = dao.getRunningTotalSteps(uid);
            int swimCount   = dao.getSwimmingSessionCount(uid);
            int swimLaps    = dao.getSwimmingTotalLaps(uid);
            int swimCal     = dao.getSwimmingTotalCalories(uid);
            int   bikeCount = dao.getBikingSessionCount(uid);
            float bikeDist  = dao.getBikingTotalDistance(uid);
            int   bikeCal   = dao.getBikingTotalCalories(uid);
            int   walkCount = dao.getWalkingSessionCount(uid);
            float walkDist  = dao.getWalkingTotalDistance(uid);
            int   walkSteps = dao.getWalkingTotalSteps(uid);
            int   walkCal   = dao.getWalkingTotalCalories(uid);
            int   hikeCount = dao.getHikingSessionCount(uid);
            float hikeDist  = dao.getHikingTotalDistance(uid);
            float hikeElev  = dao.getHikingTotalElevationGain(uid);
            int   hikeCal   = dao.getHikingTotalCalories(uid);
            int medCount    = dao.getMeditationSessionCount(uid);
            int strCount    = dao.getStrengthSessionCount(uid);
            int strCal      = dao.getStrengthTotalCalories(uid);
            int yogaCount   = dao.getYogaSessionCount(uid);
            int yogaCal     = dao.getYogaTotalCalories(uid);

            User user = dao.getFirstUser();

            final float finalWeeklyMins   = (totalDurSec / 60f) / Math.max(1, weeksTracked);
            final float finalWeeklySess   = avgPerWeek;
            final float finalAvgCalPerSess = avgCalPerSession;
            final float finalOverallHR    = overallHR;

            runOnUiThread(() -> {
                statTotalWorkouts.setText("Total Workouts: " + totalWorkouts);
                statWeeksTracked.setText("Weeks Tracked: " + weeksTracked);
                statAvgWorkoutsPerWeek.setText(String.format(
                        "Avg Workouts/Week: %.1f", avgPerWeek));

                statAvgHROverall.setText(overallHR > 0
                        ? "Overall Avg: " + (int) overallHR + " bpm"
                        : "Overall Avg: No data yet");
                statHRByActivity.setText(buildHRByActivity(
                        hrRun, hrSwim, hrBike, hrWalk, hrHike, hrMed, hrStr, hrYoga));

                statAvgCaloriesOverall.setText(overallCal > 0
                        ? "Avg Per Session: " + (int) overallCal + " kcal"
                        : "Avg Per Session: No data yet");
                statCaloriesByActivity.setText(buildCalByActivity(
                        calRun, calSwim, calBike, calWalk, calHike, calStr, calYoga));

                statAvgTimePerSession.setText("Avg Session: "
                        + (overallAvgDur > 0
                        ? formatDuration((long) overallAvgDur) : "No data yet"));
                statAvgTimePerWeek.setText("Avg Per Week: "
                        + (weeklyAvgDurSec > 0
                        ? formatDuration(weeklyAvgDurSec) : "No data yet"));
                statTimeByActivity.setText(buildTimeByActivity(
                        durRun, durSwim, durBike, durWalk, durHike, durMed, durStr, durYoga));

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

                // Update health overview rings
                updateHealthOverview(user, finalOverallHR, finalWeeklyMins,
                        finalWeeklySess, totalWorkouts, finalAvgCalPerSess);

                loadCharts();
            });
        });
    }

    private void loadCharts() {
        if (currentUserId == -1) return;

        executor.execute(() -> {
            AppDatabase db  = AppDatabase.getDatabase(getApplicationContext());
            AppDao dao      = db.appDao();
            int uid         = currentUserId;

            int range     = ChartPreferences.getRange(this);
            long fromTime = ChartPreferences.getFromTime(range);

            int[] caloriesByType = new int[9];
            caloriesByType[1] = dao.getRunningCaloriesFrom(uid, fromTime);
            caloriesByType[2] = dao.getSwimmingCaloriesFrom(uid, fromTime);
            caloriesByType[3] = dao.getBikingCaloriesFrom(uid, fromTime);
            caloriesByType[4] = dao.getWalkingCaloriesFrom(uid, fromTime);
            caloriesByType[5] = dao.getHikingCaloriesFrom(uid, fromTime);
            caloriesByType[6] = 0;
            caloriesByType[7] = dao.getStrengthCaloriesFrom(uid, fromTime);
            caloriesByType[8] = dao.getYogaCaloriesFrom(uid, fromTime);

            int[] sessionCounts = new int[9];
            for (int i = 1; i <= 8; i++)
                sessionCounts[i] = dao.getSessionCountByTypeFrom(uid, i, fromTime);

            long[] durationByType = new long[9];
            durationByType[1] = dao.getRunningDurationFrom(uid, fromTime);
            durationByType[2] = dao.getSwimmingDurationFrom(uid, fromTime);
            durationByType[3] = dao.getBikingDurationFrom(uid, fromTime);
            durationByType[4] = dao.getWalkingDurationFrom(uid, fromTime);
            durationByType[5] = dao.getHikingDurationFrom(uid, fromTime);
            durationByType[6] = dao.getMeditationDurationFrom(uid, fromTime);
            durationByType[7] = dao.getStrengthDurationFrom(uid, fromTime);
            durationByType[8] = dao.getYogaDurationFrom(uid, fromTime);

            List<long[]> hrOverTime = new ArrayList<>();
            List<Activity> activities = dao.getActivitiesFrom(uid, fromTime);
            for (Activity act : activities) {
                int hr = 0;
                switch (act.activityType) {
                    case 1: RunningData r = dao.getRunningData(act.activityId);
                        if (r != null && r.heartRate > 0) hr = r.heartRate; break;
                    case 2: SwimmingData sw = dao.getSwimmingData(act.activityId);
                        if (sw != null && sw.heartRate > 0) hr = sw.heartRate; break;
                    case 3: BikingData b = dao.getBikingData(act.activityId);
                        if (b != null && b.heartRate > 0) hr = b.heartRate; break;
                    case 4: WalkingData w = dao.getWalkingData(act.activityId);
                        if (w != null && w.heartRate > 0) hr = w.heartRate; break;
                    case 5: HikingData h = dao.getHikingData(act.activityId);
                        if (h != null && h.heartRate > 0) hr = h.heartRate; break;
                    case 6: MeditationData md = dao.getMeditationData(act.activityId);
                        if (md != null && md.heartRate > 0) hr = md.heartRate; break;
                    case 7: StrengthTrainingData st = dao.getStrengthTrainingData(act.activityId);
                        if (st != null && st.heartRate > 0) hr = st.heartRate; break;
                    case 8: YogaData y = dao.getYogaData(act.activityId);
                        if (y != null && y.heartRate > 0) hr = y.heartRate; break;
                }
                if (hr > 0) hrOverTime.add(new long[]{act.startTime, hr});
            }

            List<RunningData> runningData = new ArrayList<>();
            for (Activity act : activities) {
                if (act.activityType == 1) {
                    RunningData rd = dao.getRunningData(act.activityId);
                    if (rd != null) { rd.timestamp = act.startTime; runningData.add(rd); }
                }
            }

            List<WalkingData> walkingData = new ArrayList<>();
            for (Activity act : activities) {
                if (act.activityType == 4) {
                    WalkingData wd = dao.getWalkingData(act.activityId);
                    if (wd != null) { wd.timestamp = act.startTime; walkingData.add(wd); }
                }
            }

            List<float[]> hrCalPairs   = new ArrayList<>();
            List<Integer> scatterTypes = new ArrayList<>();
            for (Activity act : activities) {
                int hr = 0; int cal = 0;
                switch (act.activityType) {
                    case 1: RunningData rd = dao.getRunningData(act.activityId);
                        if (rd != null) { hr = rd.heartRate; cal = rd.calories; } break;
                    case 2: SwimmingData sw = dao.getSwimmingData(act.activityId);
                        if (sw != null) { hr = sw.heartRate; cal = sw.calories; } break;
                    case 3: BikingData bd = dao.getBikingData(act.activityId);
                        if (bd != null) { hr = bd.heartRate; cal = bd.calories; } break;
                    case 4: WalkingData wd = dao.getWalkingData(act.activityId);
                        if (wd != null) { hr = wd.heartRate; cal = wd.calories; } break;
                    case 5: HikingData hd = dao.getHikingData(act.activityId);
                        if (hd != null) { hr = hd.heartRate; cal = hd.calories; } break;
                    case 7: StrengthTrainingData st = dao.getStrengthTrainingData(act.activityId);
                        if (st != null) { hr = st.heartRate; cal = st.calories; } break;
                    case 8: YogaData yd = dao.getYogaData(act.activityId);
                        if (yd != null) { hr = yd.heartRate; cal = yd.calories; } break;
                }
                if (hr > 0 && cal > 0) {
                    hrCalPairs.add(new float[]{hr, cal});
                    scatterTypes.add(act.activityType);
                }
            }

            int[] scatterTypesArr = new int[scatterTypes.size()];
            for (int i = 0; i < scatterTypes.size(); i++)
                scatterTypesArr[i] = scatterTypes.get(i);

            final int[]             finalCalories     = caloriesByType;
            final int[]             finalSessions     = sessionCounts;
            final long[]            finalDurations    = durationByType;
            final List<long[]>      finalHR           = hrOverTime;
            final List<RunningData> finalRun          = runningData;
            final List<WalkingData> finalWalk         = walkingData;
            final List<float[]>     finalScatter      = hrCalPairs;
            final int[]             finalScatterTypes = scatterTypesArr;

            runOnUiThread(() -> {
                chartsContainer.removeAllViews();
                chartsContainer.addView(ChartCardHelper
                        .buildCaloriesByActivityChart(this, finalCalories));
                chartsContainer.addView(ChartCardHelper
                        .buildSessionCountChart(this, finalSessions));
                chartsContainer.addView(ChartCardHelper
                        .buildActiveMinutesChart(this, finalDurations));
                chartsContainer.addView(ChartCardHelper
                        .buildHeartRateTrendChart(this, finalHR));
                chartsContainer.addView(ChartCardHelper
                        .buildRunningDistanceChart(this, finalRun));
                chartsContainer.addView(ChartCardHelper
                        .buildWalkingStepsChart(this, finalWalk));
                chartsContainer.addView(ChartCardHelper
                        .buildHRvsCaloriesScatter(this, finalScatter, finalScatterTypes));
            });
        });
    }

    private float average(float... values) {
        float sum = 0; int count = 0;
        for (float v : values) { if (v > 0) { sum += v; count++; } }
        return count == 0 ? 0 : sum / count;
    }

    private String buildHRByActivity(float run, float swim, float bike,
                                     float walk, float hike, float med, float str, float yoga) {
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
                                      float walk, float hike, float str, float yoga) {
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
                                       float walk, float hike, float med, float str, float yoga) {
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
                if (readyTextView != null)    readyTextView.setVisibility(View.GONE);
                Toast.makeText(this, "Workout Started: " + workoutName,
                        Toast.LENGTH_SHORT).show();
            });
        } else if (path.startsWith("/workout_stop/")) {
            try {
                String[] parts       = path.replace("/workout_stop/", "").split("/");
                long durationSeconds = Long.parseLong(parts[2]);
                long hours           = durationSeconds / 3600;
                long minutes         = (durationSeconds % 3600) / 60;
                long seconds         = durationSeconds % 60;
                String formattedDuration = hours > 0
                        ? hours + "h " + minutes + "m " + seconds + "s"
                        : minutes > 0 ? minutes + "m " + seconds + "s"
                        : seconds + "s";
                final String workoutName     = new String(messageEvent.getData());
                final String finalDuration   = formattedDuration;

                runOnUiThread(() -> {
                    statusTextView.setText("Workout ended: " + workoutName);
                    if (durationTextView != null) {
                        durationTextView.setText("Duration: " + finalDuration);
                        durationTextView.setVisibility(View.VISIBLE);
                    }
                    if (readyTextView != null) {
                        readyTextView.setText("Ready to start next workout");
                        readyTextView.setVisibility(View.VISIBLE);
                    }

                    // Launch confetti and congratulations dialog
                    launchConfetti();
                    showCongratulationsDialog(workoutName, finalDuration);

                    loadStats();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}