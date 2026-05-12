package com.AidenLiriano.newyou;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PhoneListenerService extends WearableListenerService {

    private static final String TAG = "PhoneListenerService";
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    // Static reference to the live activity so we can push updates to it
    public static LiveWorkoutActivity liveWorkoutActivity = null;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        String sourceNodeId = messageEvent.getSourceNodeId();
        Log.d(TAG, "Message received on path: " + path);

        // --- Workout STARTED ---
        if (path.startsWith("/workout/")) {
            try {
                int activityType = Integer.parseInt(path.replace("/workout/", ""));
                long timestamp = System.currentTimeMillis();
                final String workoutName = new String(messageEvent.getData());

                databaseExecutor.execute(() -> {
                    AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                    AppDao dao = db.appDao();

                    Activity activity = new Activity(1, activityType, timestamp, 0);
                    long activityId = dao.insertActivity(activity);
                    Log.d(TAG, "Inserted activity ID: " + activityId + " type: " + activityType);

                    switch (activityType) {
                        case 1: dao.insertRunningData(new RunningData((int) activityId)); break;
                        case 2: dao.insertSwimmingData(new SwimmingData((int) activityId)); break;
                        case 3: dao.insertBikingData(new BikingData((int) activityId)); break;
                        case 4: dao.insertWalkingData(new WalkingData((int) activityId)); break;
                        case 5: dao.insertHikingData(new HikingData((int) activityId)); break;
                        case 6: dao.insertMeditationData(new MeditationData((int) activityId)); break;
                        case 7: dao.insertStrengthTrainingData(new StrengthTrainingData((int) activityId)); break;
                        case 8: dao.insertYogaData(new YogaData((int) activityId)); break;
                        default: Log.w(TAG, "Unknown activity type: " + activityType); break;
                    }

                    // Send real activity ID back to watch
                    Wearable.getMessageClient(getApplicationContext())
                            .sendMessage(sourceNodeId,
                                    "/activity_id_response/" + activityId,
                                    new byte[0]);

                    // Open live workout screen on phone
                    Intent intent = new Intent(getApplicationContext(),
                            LiveWorkoutActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(LiveWorkoutActivity.EXTRA_WORKOUT_NAME, workoutName);
                    intent.putExtra(LiveWorkoutActivity.EXTRA_WORKOUT_TYPE, activityType);
                    getApplicationContext().startActivity(intent);

                    Log.d(TAG, "Successfully saved and opened live screen for: " + workoutName);
                });

            } catch (NumberFormatException e) {
                Log.e(TAG, "Failed to parse activity type: " + path);
            }
        }

        // --- Live Update ---
        // Path: /workout_live/type/activityId/elapsed/hr/cal/steps/dist/pace/speed/elevGain/laps
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

                // Push update to live screen if it is open
                if (liveWorkoutActivity != null) {
                    liveWorkoutActivity.updateLiveStats(
                            elapsedSeconds, heartRate, calories,
                            steps, distanceKm, pace, speed, elevGain, laps
                    );
                }

            } catch (Exception e) {
                Log.e(TAG, "Failed to parse live update: " + path, e);
            }
        }

        // --- Workout STOPPED ---
        // Path: /workout_stop/type/activityId/duration/hr/cal/steps/dist/pace/speed/elevGain/elevLoss/hrStart/hrEnd/laps
        else if (path.startsWith("/workout_stop/")) {
            try {
                String[] p = path.replace("/workout_stop/", "").split("/");
                int activityType    = Integer.parseInt(p[0]);
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

                // Format summary for toast
                long h = duration / 3600;
                long m = (duration % 3600) / 60;
                long s = duration % 60;
                String formattedDuration = h > 0
                        ? h + "h " + m + "m " + s + "s"
                        : m > 0 ? m + "m " + s + "s"
                        : s + "s";
                final String summary = workoutName + " complete!\n"
                        + "Duration: " + formattedDuration + "  |  "
                        + calories + " kcal  |  "
                        + (heartRate > 0 ? heartRate + " bpm avg" : "");

                // Recalculate calories using real user weight
                float[] metValues = {0, 9.8f, 6.0f, 7.5f, 3.5f, 6.0f, 2.5f, 5.0f, 3.0f};
                float met = activityType >= 1 && activityType <= 8 ? metValues[activityType] : 5.0f;

                // Get user weight from database
                AppDatabase dbCheck = AppDatabase.getDatabase(getApplicationContext());
                User user = dbCheck.appDao().getFirstUser();
                float weightLbs = user != null && user.weightLbs > 0 ? user.weightLbs : 154f;
                int recalculatedCalories = recalculateCalories(met, weightLbs, duration);

                // Use recalculated value going forward
                final int finalCalories = recalculatedCalories;

                databaseExecutor.execute(() -> {
                    AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                    AppDao dao = db.appDao();

                    dao.updateActivityEndTime(activityId, endTime);

                    switch (activityType) {
                        case 1: dao.updateRunningData(activityId, duration, distanceKm,
                                pace, heartRate, finalCalories, steps); break;
                        case 2: dao.updateSwimmingData(activityId, duration, laps,
                                swimDistance, swimPace, finalCalories, heartRate); break;
                        case 3: dao.updateBikingData(activityId, duration, distanceKm,
                                speed, heartRate, finalCalories); break;
                        case 4: dao.updateWalkingData(activityId, duration, distanceKm,
                                steps, pace, heartRate, finalCalories); break;
                        case 5: dao.updateHikingData(activityId, duration, distanceKm,
                                elevGain, elevLoss, heartRate, finalCalories, pace); break;
                        case 6: dao.updateMeditationData(activityId, duration,
                                heartRate, hrStart, hrEnd); break;
                        case 7: dao.updateStrengthTrainingData(activityId, duration,
                                heartRate, finalCalories); break;
                        case 8: dao.updateYogaData(activityId, duration,
                                heartRate, finalCalories); break;
                        default: Log.w(TAG, "Unknown type: " + activityType); break;
                    }

                    Log.d(TAG, "Workout saved. ID: " + activityId
                            + " duration: " + duration + "s");

                    // Close live screen and show summary toast on main thread
                    if (liveWorkoutActivity != null) {
                        liveWorkoutActivity.runOnUiThread(() -> {
                            android.widget.Toast.makeText(
                                    getApplicationContext(),
                                    summary,
                                    android.widget.Toast.LENGTH_LONG
                            ).show();
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

    private int recalculateCalories(float metValue, float weightLbs, long durationSeconds) {
        float weightKg = weightLbs * 0.453592f;
        float durationHours = durationSeconds / 3600f;
        return (int) (metValue * weightKg * durationHours);
    }
}