package com.AidenLiriano.newyou;

import android.util.Log;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PhoneListenerService extends WearableListenerService {

    private static final String TAG = "PhoneListenerService";
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        Log.d(TAG, "Message received on path: " + path);

        // Parse the activity type from the path e.g. "/workout/1"
        if (path.startsWith("/workout/")) {
            try {
                int activityType = Integer.parseInt(path.replace("/workout/", ""));
                long timestamp = System.currentTimeMillis();

                databaseExecutor.execute(() -> {
                    AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                    AppDao dao = db.appDao();

                    // Insert into activities table with a default user_id of 1
                    // (user system not yet implemented)
                    Activity activity = new Activity(1, activityType, timestamp, 0);
                    long activityId = dao.insertActivity(activity);
                    Log.d(TAG, "Inserted activity ID: " + activityId + " type: " + activityType);

                    // Insert a blank row into the matching activity data table
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

                    Log.d(TAG, "Successfully saved workout type " + activityType + " to database.");
                });

            } catch (NumberFormatException e) {
                Log.e(TAG, "Failed to parse activity type from path: " + path);
            }
        }
    }
}