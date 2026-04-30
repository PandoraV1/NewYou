package com.AidenLiriano.newyou;

import android.util.Log;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PhoneListenerService extends WearableListenerService {

    private static final String TAG = "PhoneListenerService";
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

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

                    Log.d(TAG, "Successfully saved workout type " + activityType);

                    // Send real activity ID back to watch
                    Wearable.getMessageClient(getApplicationContext())
                            .sendMessage(
                                    sourceNodeId,
                                    "/activity_id_response/" + activityId,
                                    new byte[0]
                            );
                });

            } catch (NumberFormatException e) {
                Log.e(TAG, "Failed to parse activity type: " + path);
            }
        }

        // --- Workout STOPPED ---
        // Path: /workout_stop/type/activityId/duration/heartRate/calories
        //       /steps/distanceKm/pace/speed/elevGain/elevLoss/hrStart/hrEnd/laps
        else if (path.startsWith("/workout_stop/")) {
            try {
                String[] p = path.replace("/workout_stop/", "").split("/");
                int activityType   = Integer.parseInt(p[0]);
                int activityId     = Integer.parseInt(p[1]);
                long duration      = Long.parseLong(p[2]);
                int heartRate      = Integer.parseInt(p[3]);
                int calories       = Integer.parseInt(p[4]);
                int steps          = Integer.parseInt(p[5]);
                float distanceKm   = Float.parseFloat(p[6]);
                float pace         = Float.parseFloat(p[7]);
                float speed        = Float.parseFloat(p[8]);
                float elevGain     = Float.parseFloat(p[9]);
                float elevLoss     = Float.parseFloat(p[10]);
                int hrStart        = Integer.parseInt(p[11]);
                int hrEnd          = Integer.parseInt(p[12]);
                int laps           = Integer.parseInt(p[13]);
                long endTime       = System.currentTimeMillis();

                // Swimming pool length in meters (25m standard)
                float poolLengthKm = 0.025f;
                float swimDistance = laps * poolLengthKm;
                float swimPace     = duration > 0 && swimDistance > 0
                        ? (duration / 60f) / swimDistance : 0f;

                databaseExecutor.execute(() -> {
                    AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                    AppDao dao = db.appDao();

                    dao.updateActivityEndTime(activityId, endTime);

                    switch (activityType) {
                        case 1: // Running
                            dao.updateRunningData(activityId, duration, distanceKm,
                                    pace, heartRate, calories, steps);
                            break;
                        case 2: // Swimming
                            dao.updateSwimmingData(activityId, duration, laps,
                                    swimDistance, swimPace, calories, heartRate);
                            break;
                        case 3: // Biking
                            dao.updateBikingData(activityId, duration, distanceKm,
                                    speed, heartRate, calories);
                            break;
                        case 4: // Walking
                            dao.updateWalkingData(activityId, duration, distanceKm,
                                    steps, pace, heartRate, calories);
                            break;
                        case 5: // Hiking
                            dao.updateHikingData(activityId, duration, distanceKm,
                                    elevGain, elevLoss, heartRate, calories, pace);
                            break;
                        case 6: // Meditation
                            dao.updateMeditationData(activityId, duration,
                                    heartRate, hrStart, hrEnd);
                            break;
                        case 7: // Strength Training
                            dao.updateStrengthTrainingData(activityId, duration,
                                    heartRate, calories);
                            break;
                        case 8: // Yoga
                            dao.updateYogaData(activityId, duration, heartRate, calories);
                            break;
                        default:
                            Log.w(TAG, "Unknown activity type: " + activityType);
                            break;
                    }

                    Log.d(TAG, "Updated activity ID: " + activityId
                            + " type: " + activityType
                            + " duration: " + duration
                            + " HR: " + heartRate
                            + " calories: " + calories);
                });

            } catch (Exception e) {
                Log.e(TAG, "Failed to parse workout stop message: " + path, e);
            }
        }
    }
}