package com.AidenLiriano.newyou;

import android.content.Intent;
import android.util.Log;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PhoneListenerService extends WearableListenerService {

    private static final String TAG = "PhoneListenerService";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static LiveWorkoutActivity liveWorkoutActivity = null;

    // Custom workout type IDs start at 100
    public static final int CUSTOM_WORKOUT_TYPE_BASE = 100;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path        = messageEvent.getPath();
        String sourceNodeId = messageEvent.getSourceNodeId();
        Log.d(TAG, "Message received: " + path);

        // --- Workout STARTED ---
        if (path.startsWith("/workout/")) {
            try {
                int typeId = Integer.parseInt(path.replace("/workout/", ""));
                long timestamp = System.currentTimeMillis();
                final String workoutName = new String(messageEvent.getData());

                databaseExecutor().execute(() -> {
                    AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                    AppDao dao     = db.appDao();

                    User user = dao.getFirstUser();
                    if (user == null) {
                        Log.e(TAG, "No user found");
                        return;
                    }
                    int userId = user.userId;

                    // For custom workouts typeId is 100 + customWorkoutId
                    // For built-in workouts typeId is 1-8
                    int activityTypeForDb = typeId >= CUSTOM_WORKOUT_TYPE_BASE
                            ? CUSTOM_WORKOUT_TYPE_BASE : typeId;

                    Activity activity = new Activity(userId,
                            activityTypeForDb, timestamp, 0);
                    long activityId = dao.insertActivity(activity);
                    Log.d(TAG, "Activity inserted: " + activityId);

                    if (typeId >= CUSTOM_WORKOUT_TYPE_BASE) {
                        // Insert custom workout data placeholder
                        int customWorkoutId = typeId - CUSTOM_WORKOUT_TYPE_BASE;
                        CustomWorkoutData cwd = new CustomWorkoutData(
                                (int) activityId, customWorkoutId);
                        dao.insertCustomWorkoutData(cwd);
                    } else {
                        // Insert built-in workout data placeholder
                        switch (typeId) {
                            case 1: dao.insertRunningData(new RunningData((int) activityId)); break;
                            case 2: dao.insertSwimmingData(new SwimmingData((int) activityId)); break;
                            case 3: dao.insertBikingData(new BikingData((int) activityId)); break;
                            case 4: dao.insertWalkingData(new WalkingData((int) activityId)); break;
                            case 5: dao.insertHikingData(new HikingData((int) activityId)); break;
                            case 6: dao.insertMeditationData(new MeditationData((int) activityId)); break;
                            case 7: dao.insertStrengthTrainingData(new StrengthTrainingData((int) activityId)); break;
                            case 8: dao.insertYogaData(new YogaData((int) activityId)); break;
                        }
                    }

                    Wearable.getMessageClient(getApplicationContext())
                            .sendMessage(sourceNodeId,
                                    "/activity_id_response/" + activityId,
                                    new byte[0]);

                    Intent intent = new Intent(getApplicationContext(),
                            LiveWorkoutActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(LiveWorkoutActivity.EXTRA_WORKOUT_NAME, workoutName);
                    intent.putExtra(LiveWorkoutActivity.EXTRA_WORKOUT_TYPE, typeId);
                    getApplicationContext().startActivity(intent);
                });

            } catch (NumberFormatException e) {
                Log.e(TAG, "Failed to parse type: " + path);
            }
        }

        // --- Live Update ---
        else if (path.startsWith("/workout_live/")) {
            try {
                String[] p = path.replace("/workout_live/", "").split("/");
                long elapsedSeconds = Long.parseLong(p[2]);
                int heartRate       = Integer.parseInt(p[3]);
                int calories        = Integer.parseInt(p[4]);
                int steps           = Integer.parseInt(p[5]);
                float distanceKm    = Float.parseFloat(p[6]);
                float pace          = Float.parseFloat(p[7]);
                float speed         = Float.parseFloat(p[8]);
                float elevGain      = Float.parseFloat(p[9]);
                int laps            = Integer.parseInt(p[10]);
                int gpsActive       = p.length > 11 ? Integer.parseInt(p[11]) : 0;

                if (liveWorkoutActivity != null) {
                    liveWorkoutActivity.updateLiveStats(
                            elapsedSeconds, heartRate, calories,
                            steps, distanceKm, pace, speed, elevGain, laps);
                    final boolean usingGps = gpsActive == 1;
                    liveWorkoutActivity.runOnUiThread(() -> {
                        if (liveWorkoutActivity.gpsStatusView != null) {
                            liveWorkoutActivity.gpsStatusView.setText(
                                    usingGps ? "📍 GPS Active" : "");
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse live update: " + path, e);
            }
        }

        // --- Workout STOPPED ---
        else if (path.startsWith("/workout_stop/")) {
            try {
                String[] p = path.replace("/workout_stop/", "").split("/");
                int typeId          = Integer.parseInt(p[0]);
                int activityId      = Integer.parseInt(p[1]);
                long duration       = Long.parseLong(p[2]);
                int heartRate       = Integer.parseInt(p[3]);
                int calories        = Integer.parseInt(p[4]);
                int steps           = Integer.parseInt(p[5]);
                float distanceKm    = Float.parseFloat(p[6]);
                float pace          = Float.parseFloat(p[7]);
                float speed         = Float.parseFloat(p[8]);
                float elevGain      = Float.parseFloat(p[9]);
                float elevLoss      = Float.parseFloat(p[10]);
                int hrStart         = Integer.parseInt(p[11]);
                int hrEnd           = Integer.parseInt(p[12]);
                int laps            = Integer.parseInt(p[13]);
                long endTime        = System.currentTimeMillis();

                float poolLengthKm = 0.025f;
                float swimDistance = laps * poolLengthKm;
                float swimPace     = duration > 0 && swimDistance > 0
                        ? (duration / 60f) / swimDistance : 0f;

                final String workoutName = new String(messageEvent.getData());

                long h = duration / 3600;
                long m = (duration % 3600) / 60;
                long s = duration % 60;
                String formattedDuration = h > 0
                        ? h + "h " + m + "m " + s + "s"
                        : m > 0 ? m + "m " + s + "s"
                        : s + "s";
                final String summary = workoutName + " complete!\n"
                        + "Duration: " + formattedDuration + "  |  "
                        + calories + " kcal"
                        + (heartRate > 0 ? "  |  " + heartRate + " bpm avg" : "");

                databaseExecutor().execute(() -> {
                    AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                    AppDao dao     = db.appDao();

                    float[] metValues = {0, 9.8f, 6.0f, 7.5f, 3.5f,
                            6.0f, 2.5f, 5.0f, 3.0f};
                    float met = typeId >= 1 && typeId <= 8
                            ? metValues[typeId] : 5.0f;
                    User user = dao.getFirstUser();
                    float weightLbs = user != null && user.weightLbs > 0
                            ? user.weightLbs : 154f;
                    int finalCalories = recalculateCalories(met, weightLbs, duration);

                    dao.updateActivityEndTime(activityId, endTime);

                    if (typeId >= CUSTOM_WORKOUT_TYPE_BASE) {
                        // Save custom workout data
                        dao.updateCustomWorkoutData(activityId, duration, heartRate,
                                finalCalories, steps, distanceKm, pace, speed,
                                elevGain, 0f, laps);
                    } else {
                        switch (typeId) {
                            case 1: dao.updateRunningData(activityId, duration, distanceKm,
                                    pace, heartRate, finalCalories, steps); break;
                            case 2: dao.updateSwimmingData(activityId, duration, laps,
                                    swimDistance, swimPace, finalCalories, heartRate); break;
                            case 3: dao.updateBikingData(activityId, duration, distanceKm,
                                    speed, heartRate, finalCalories); break;
                            case 4: dao.updateWalkingData(activityId, duration, distanceKm,
                                    steps, pace, heartRate, finalCalories); break;
                            case 5: dao.updateHikingData(activityId, duration, distanceKm,
                                    elevGain, 0f, heartRate, finalCalories, pace); break;
                            case 6: dao.updateMeditationData(activityId, duration,
                                    heartRate, hrStart, hrEnd); break;
                            case 7: dao.updateStrengthTrainingData(activityId, duration,
                                    heartRate, finalCalories); break;
                            case 8: dao.updateYogaData(activityId, duration,
                                    heartRate, finalCalories); break;
                        }
                    }

                    if (liveWorkoutActivity != null) {
                        liveWorkoutActivity.runOnUiThread(() -> {
                            android.widget.Toast.makeText(
                                    getApplicationContext(), summary,
                                    android.widget.Toast.LENGTH_LONG).show();
                            liveWorkoutActivity.finishWorkout();
                            liveWorkoutActivity = null;
                        });
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Failed to parse workout stop: " + path, e);
            }
        }
    }

    private ExecutorService databaseExecutor() {
        return executor;
    }

    private int recalculateCalories(float metValue, float weightLbs,
                                    long durationSeconds) {
        float weightKg      = weightLbs * 0.453592f;
        float durationHours = durationSeconds / 3600f;
        return (int) (metValue * weightKg * durationHours);
    }
}