package com.AidenLiriano.newyou;

/**
 * ============================================================
 * TEMPORARY TEST DATA — REMOVE BEFORE RELEASE
 * ============================================================
 * This class inserts fake workout data for UI testing.
 * Call seedTestData() once from MainActivity onCreate.
 * To remove: delete this file and remove the call in MainActivity.
 * ============================================================
 */

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestDataSeeder {

    private static final String TAG = "TestDataSeeder";

    // TEMPORARY: Called once to insert fake workout data
    public static void seedTestData(Context context, int userId, Runnable onComplete) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(context);
            AppDao dao     = db.appDao();

            int existing = dao.getTotalActivityCount(userId);
            if (existing >= 5) {
                Log.d(TAG, "TEMP: Skipping seed — data already exists for user " + userId);
                if (onComplete != null)
                    new Handler(Looper.getMainLooper()).post(onComplete);
                return;
            }

            Log.d(TAG, "TEMP: Seeding varied test workout data for user " + userId);

            Calendar cal = Calendar.getInstance();

            // ============================================================
            // TEMPORARY TEST DATA BELOW, all activities are fabricated
            // Varied session counts per activity to produce different tier recommendations across workout types:
            //   Running    — many sessions → higher tier (Tempo Run)
            //   Swimming   — few sessions  → lower tier  (Easy Swim)
            //   Biking     — moderate      → mid tier    (Moderate Ride)
            //   Walking    — many sessions → higher tier (Power Walk)
            //   Hiking     — very few      → lowest tier (Beginner Hike)
            //   Meditation — many sessions → highest tier (Advanced Session)
            //   Strength   — few sessions  → lower tier  (Easy Strength)
            //   Yoga       — moderate      → mid tier    (Moderate Yoga)
            // ============================================================

            // --- Running: 10 sessions, produces Tempo Run recommendation ---
            insertRunning(dao, userId, daysAgo(cal, 58), 1800, 4.2f, 7.1f, 142, 320, 5400);
            insertRunning(dao, userId, daysAgo(cal, 52), 2100, 5.1f, 6.8f, 148, 380, 6300);
            insertRunning(dao, userId, daysAgo(cal, 46), 2400, 5.6f, 6.5f, 152, 420, 7000);
            insertRunning(dao, userId, daysAgo(cal, 40), 2600, 6.0f, 6.2f, 155, 450, 7500);
            insertRunning(dao, userId, daysAgo(cal, 34), 2700, 6.4f, 6.0f, 158, 480, 7900);
            insertRunning(dao, userId, daysAgo(cal, 28), 2800, 6.8f, 5.8f, 160, 500, 8200);
            insertRunning(dao, userId, daysAgo(cal, 22), 2900, 7.0f, 5.6f, 162, 520, 8500);
            insertRunning(dao, userId, daysAgo(cal, 16), 3000, 7.3f, 5.4f, 164, 540, 8800);
            insertRunning(dao, userId, daysAgo(cal, 10), 3100, 7.6f, 5.2f, 166, 560, 9100);
            insertRunning(dao, userId, daysAgo(cal, 4),  3200, 7.9f, 5.0f, 168, 580, 9400);

            // --- Swimming: 2 sessions, produces Easy Swim recommendation ---
            insertSwimming(dao, userId, daysAgo(cal, 45), 1200, 8,  0.2f, 5.0f, 140, 110);
            insertSwimming(dao, userId, daysAgo(cal, 20), 1500, 12, 0.3f, 4.5f, 175, 118);

            // --- Biking: 5 sessions, produces Moderate Ride recommendation ---
            insertBiking(dao, userId, daysAgo(cal, 50), 2400, 12.5f, 18.8f, 135, 380);
            insertBiking(dao, userId, daysAgo(cal, 38), 2700, 14.8f, 19.7f, 140, 430);
            insertBiking(dao, userId, daysAgo(cal, 26), 3000, 17.2f, 20.6f, 145, 480);
            insertBiking(dao, userId, daysAgo(cal, 14), 3300, 19.5f, 21.3f, 150, 530);
            insertBiking(dao, userId, daysAgo(cal, 5),  3600, 22.1f, 22.1f, 155, 580);

            // --- Walking: 12 sessions, produces Power Walk recommendation ---
            insertWalking(dao, userId, daysAgo(cal, 56), 1800, 2.8f, 3500, 9.6f, 112, 180);
            insertWalking(dao, userId, daysAgo(cal, 50), 2000, 3.0f, 3750, 9.5f, 114, 195);
            insertWalking(dao, userId, daysAgo(cal, 44), 2100, 3.2f, 4000, 9.4f, 115, 210);
            insertWalking(dao, userId, daysAgo(cal, 38), 2200, 3.4f, 4250, 9.3f, 116, 220);
            insertWalking(dao, userId, daysAgo(cal, 32), 2400, 3.6f, 4500, 9.2f, 117, 235);
            insertWalking(dao, userId, daysAgo(cal, 26), 2500, 3.8f, 4750, 9.1f, 118, 245);
            insertWalking(dao, userId, daysAgo(cal, 20), 2600, 4.0f, 5000, 9.0f, 119, 255);
            insertWalking(dao, userId, daysAgo(cal, 16), 2700, 4.2f, 5200, 8.9f, 120, 265);
            insertWalking(dao, userId, daysAgo(cal, 12), 2800, 4.4f, 5500, 8.8f, 121, 275);
            insertWalking(dao, userId, daysAgo(cal, 8),  2900, 4.6f, 5750, 8.7f, 122, 285);
            insertWalking(dao, userId, daysAgo(cal, 4),  3000, 4.8f, 6000, 8.6f, 123, 295);
            insertWalking(dao, userId, daysAgo(cal, 1),  3100, 5.0f, 6200, 8.5f, 124, 305);

            // --- Hiking: 1 session, produces Beginner Hike recommendation ---
            insertHiking(dao, userId, daysAgo(cal, 30), 2400, 3.5f, 60f, 15f, 138, 340, 10.2f);

            // --- Meditation: 14 sessions, produces Advanced Session recommendation ---
            insertMeditation(dao, userId, daysAgo(cal, 55), 600,  72, 76, 68);
            insertMeditation(dao, userId, daysAgo(cal, 50), 900,  70, 74, 65);
            insertMeditation(dao, userId, daysAgo(cal, 45), 1200, 68, 73, 62);
            insertMeditation(dao, userId, daysAgo(cal, 40), 1500, 66, 71, 60);
            insertMeditation(dao, userId, daysAgo(cal, 35), 1800, 64, 70, 58);
            insertMeditation(dao, userId, daysAgo(cal, 30), 1800, 63, 69, 57);
            insertMeditation(dao, userId, daysAgo(cal, 25), 2100, 62, 68, 56);
            insertMeditation(dao, userId, daysAgo(cal, 22), 2100, 61, 67, 55);
            insertMeditation(dao, userId, daysAgo(cal, 19), 2400, 60, 66, 54);
            insertMeditation(dao, userId, daysAgo(cal, 16), 2400, 59, 65, 53);
            insertMeditation(dao, userId, daysAgo(cal, 13), 2700, 58, 64, 52);
            insertMeditation(dao, userId, daysAgo(cal, 10), 2700, 57, 63, 51);
            insertMeditation(dao, userId, daysAgo(cal, 6),  3000, 56, 62, 50);
            insertMeditation(dao, userId, daysAgo(cal, 2),  3600, 55, 61, 49);

            // --- Strength Training: 3 sessions, produces Easy Strength recommendation ---
            insertStrength(dao, userId, daysAgo(cal, 40), 1500, 128, 200);
            insertStrength(dao, userId, daysAgo(cal, 22), 1800, 132, 240);
            insertStrength(dao, userId, daysAgo(cal, 8),  2100, 136, 280);

            // --- Yoga: 6 sessions, produces Moderate Yoga recommendation ---
            insertYoga(dao, userId, daysAgo(cal, 48), 1800, 100, 160);
            insertYoga(dao, userId, daysAgo(cal, 38), 2100, 104, 185);
            insertYoga(dao, userId, daysAgo(cal, 28), 2400, 107, 210);
            insertYoga(dao, userId, daysAgo(cal, 18), 2700, 109, 235);
            insertYoga(dao, userId, daysAgo(cal, 8),  3000, 111, 255);
            insertYoga(dao, userId, daysAgo(cal, 2),  3300, 113, 275);

            Log.d(TAG, "TEMP: Varied test data seeding complete for user " + userId);

            if (onComplete != null)
                new Handler(Looper.getMainLooper()).post(onComplete);
        });
    }

    // TEMPORARY HELPER METHODS, remove with this file

    private static long daysAgo(Calendar cal, int days) {
        Calendar c = (Calendar) cal.clone();
        c.add(Calendar.DAY_OF_YEAR, -days);
        c.set(Calendar.HOUR_OF_DAY, 9);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return c.getTimeInMillis();
    }

    private static void insertRunning(AppDao dao, int userId, long startTime,
                                      long duration, float distance, float pace,
                                      int heartRate, int calories, int steps) {
        Activity act = new Activity(userId, 1, startTime,
                startTime + (duration * 1000));
        long actId = dao.insertActivity(act);
        RunningData d = new RunningData((int) actId);
        d.duration  = duration;
        d.distance  = distance;
        d.pace      = pace;
        d.heartRate = heartRate;
        d.calories  = calories;
        d.stepCount = steps;
        dao.insertRunningData(d);
    }

    private static void insertSwimming(AppDao dao, int userId, long startTime,
                                       long duration, int laps, float distance, float pace,
                                       int calories, int heartRate) {
        Activity act = new Activity(userId, 2, startTime,
                startTime + (duration * 1000));
        long actId = dao.insertActivity(act);
        SwimmingData d = new SwimmingData((int) actId);
        d.duration  = duration;
        d.laps      = laps;
        d.distance  = distance;
        d.pace      = pace;
        d.calories  = calories;
        d.heartRate = heartRate;
        dao.insertSwimmingData(d);
    }

    private static void insertBiking(AppDao dao, int userId, long startTime,
                                     long duration, float distance, float speed,
                                     int heartRate, int calories) {
        Activity act = new Activity(userId, 3, startTime,
                startTime + (duration * 1000));
        long actId = dao.insertActivity(act);
        BikingData d = new BikingData((int) actId);
        d.duration  = duration;
        d.distance  = distance;
        d.speed     = speed;
        d.heartRate = heartRate;
        d.calories  = calories;
        dao.insertBikingData(d);
    }

    private static void insertWalking(AppDao dao, int userId, long startTime,
                                      long duration, float distance, int steps, float pace,
                                      int heartRate, int calories) {
        Activity act = new Activity(userId, 4, startTime,
                startTime + (duration * 1000));
        long actId = dao.insertActivity(act);
        WalkingData d = new WalkingData((int) actId);
        d.duration  = duration;
        d.distance  = distance;
        d.stepCount = steps;
        d.pace      = pace;
        d.heartRate = heartRate;
        d.calories  = calories;
        dao.insertWalkingData(d);
    }

    private static void insertHiking(AppDao dao, int userId, long startTime,
                                     long duration, float distance, float elevGain, float elevLoss,
                                     int heartRate, int calories, float pace) {
        Activity act = new Activity(userId, 5, startTime,
                startTime + (duration * 1000));
        long actId = dao.insertActivity(act);
        HikingData d = new HikingData((int) actId);
        d.duration      = duration;
        d.distance      = distance;
        d.elevationGain = elevGain;
        d.elevationLoss = elevLoss;
        d.heartRate     = heartRate;
        d.calories      = calories;
        d.pace          = pace;
        dao.insertHikingData(d);
    }

    private static void insertMeditation(AppDao dao, int userId, long startTime,
                                         long duration, int heartRate, int hrStart, int hrEnd) {
        Activity act = new Activity(userId, 6, startTime,
                startTime + (duration * 1000));
        long actId = dao.insertActivity(act);
        MeditationData d = new MeditationData((int) actId);
        d.duration       = duration;
        d.heartRate      = heartRate;
        d.heartRateStart = hrStart;
        d.heartRateEnd   = hrEnd;
        dao.insertMeditationData(d);
    }

    private static void insertStrength(AppDao dao, int userId, long startTime,
                                       long duration, int heartRate, int calories) {
        Activity act = new Activity(userId, 7, startTime,
                startTime + (duration * 1000));
        long actId = dao.insertActivity(act);
        StrengthTrainingData d = new StrengthTrainingData((int) actId);
        d.duration  = duration;
        d.heartRate = heartRate;
        d.calories  = calories;
        dao.insertStrengthTrainingData(d);
    }

    private static void insertYoga(AppDao dao, int userId, long startTime,
                                   long duration, int heartRate, int calories) {
        Activity act = new Activity(userId, 8, startTime,
                startTime + (duration * 1000));
        long actId = dao.insertActivity(act);
        YogaData d = new YogaData((int) actId);
        d.duration  = duration;
        d.heartRate = heartRate;
        d.calories  = calories;
        dao.insertYogaData(d);
    }
}