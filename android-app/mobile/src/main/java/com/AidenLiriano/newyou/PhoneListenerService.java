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

                    // Send the real activity ID back to the watch
                    Wearable.getMessageClient(getApplicationContext())
                            .sendMessage(
                                    sourceNodeId,
                                    "/activity_id_response/" + activityId,
                                    new byte[0]
                            );
                });

            } catch (NumberFormatException e) {
                Log.e(TAG, "Failed to parse activity type from path: " + path);
            }
        }

        // --- Workout STOPPED ---
        else if (path.startsWith("/workout_stop/")) {
            try {
                String[] parts = path.replace("/workout_stop/", "").split("/");
                int activityType = Integer.parseInt(parts[0]);
                int activityId = Integer.parseInt(parts[1]);
                long durationSeconds = Long.parseLong(parts[2]);
                long endTime = System.currentTimeMillis();

                databaseExecutor.execute(() -> {
                    AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                    AppDao dao = db.appDao();

                    dao.updateActivityEndTime(activityId, endTime);

                    switch (activityType) {
                        case 1: dao.updateRunningDuration(activityId, durationSeconds); break;
                        case 2: dao.updateSwimmingDuration(activityId, durationSeconds); break;
                        case 3: dao.updateBikingDuration(activityId, durationSeconds); break;
                        case 4: dao.updateWalkingDuration(activityId, durationSeconds); break;
                        case 5: dao.updateHikingDuration(activityId, durationSeconds); break;
                        case 6: dao.updateMeditationDuration(activityId, durationSeconds); break;
                        case 7: dao.updateStrengthTrainingDuration(activityId, durationSeconds); break;
                        case 8: dao.updateYogaDuration(activityId, durationSeconds); break;
                        default: Log.w(TAG, "Unknown activity type: " + activityType); break;
                    }

                    Log.d(TAG, "Updated activity ID: " + activityId + " duration: " + durationSeconds + "s");
                });

            } catch (Exception e) {
                Log.e(TAG, "Failed to parse workout stop message: " + path);
            }
        }
    }
}