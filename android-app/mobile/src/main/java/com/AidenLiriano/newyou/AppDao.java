package com.AidenLiriano.newyou;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface AppDao {

    // --- User ---
    @Insert
    long insertUser(User user);

    @Query("SELECT * FROM user WHERE user_id = :userId")
    User getUser(int userId);

    // --- Activities ---
    @Insert
    long insertActivity(Activity activity);

    @Query("UPDATE activities SET end_time = :endTime WHERE activity_id = :activityId")
    void updateActivityEndTime(int activityId, long endTime);

    @Query("SELECT * FROM activities WHERE user_id = :userId ORDER BY start_time DESC")
    List<Activity> getActivitiesForUser(int userId);

    @Query("DELETE FROM activities")
    void clearAllActivities();

    // --- Running ---
    @Insert
    void insertRunningData(RunningData data);

    @Query("UPDATE running_data SET duration=:duration, distance=:distance, pace=:pace, " +
            "heart_rate=:heartRate, calories=:calories, step_count=:steps " +
            "WHERE activity_id=:activityId")
    void updateRunningData(int activityId, long duration, float distance,
                           float pace, int heartRate, int calories, int steps);

    @Query("SELECT * FROM running_data WHERE activity_id = :activityId")
    RunningData getRunningData(int activityId);

    // --- Swimming ---
    @Insert
    void insertSwimmingData(SwimmingData data);

    @Query("UPDATE swimming_data SET duration=:duration, laps=:laps, distance=:distance, " +
            "pace=:pace, calories=:calories, heart_rate=:heartRate " +
            "WHERE activity_id=:activityId")
    void updateSwimmingData(int activityId, long duration, int laps, float distance,
                            float pace, int calories, int heartRate);

    @Query("SELECT * FROM swimming_data WHERE activity_id = :activityId")
    SwimmingData getSwimmingData(int activityId);

    // --- Biking ---
    @Insert
    void insertBikingData(BikingData data);

    @Query("UPDATE biking_data SET duration=:duration, distance=:distance, speed=:speed, " +
            "heart_rate=:heartRate, calories=:calories WHERE activity_id=:activityId")
    void updateBikingData(int activityId, long duration, float distance,
                          float speed, int heartRate, int calories);

    @Query("SELECT * FROM biking_data WHERE activity_id = :activityId")
    BikingData getBikingData(int activityId);

    // --- Walking ---
    @Insert
    void insertWalkingData(WalkingData data);

    @Query("UPDATE walking_data SET duration=:duration, distance=:distance, step_count=:steps, " +
            "pace=:pace, heart_rate=:heartRate, calories=:calories WHERE activity_id=:activityId")
    void updateWalkingData(int activityId, long duration, float distance,
                           int steps, float pace, int heartRate, int calories);

    @Query("SELECT * FROM walking_data WHERE activity_id = :activityId")
    WalkingData getWalkingData(int activityId);

    // --- Hiking ---
    @Insert
    void insertHikingData(HikingData data);

    @Query("UPDATE hiking_data SET duration=:duration, distance=:distance, " +
            "elevation_gain=:elevGain, elevation_loss=:elevLoss, " +
            "heart_rate=:heartRate, calories=:calories, pace=:pace " +
            "WHERE activity_id=:activityId")
    void updateHikingData(int activityId, long duration, float distance,
                          float elevGain, float elevLoss, int heartRate,
                          int calories, float pace);

    @Query("SELECT * FROM hiking_data WHERE activity_id = :activityId")
    HikingData getHikingData(int activityId);

    // --- Meditation ---
    @Insert
    void insertMeditationData(MeditationData data);

    @Query("UPDATE meditation_data SET duration=:duration, heart_rate=:heartRate, " +
            "heart_rate_start=:hrStart, heart_rate_end=:hrEnd WHERE activity_id=:activityId")
    void updateMeditationData(int activityId, long duration, int heartRate,
                              int hrStart, int hrEnd);

    @Query("SELECT * FROM meditation_data WHERE activity_id = :activityId")
    MeditationData getMeditationData(int activityId);

    // --- Strength Training ---
    @Insert
    void insertStrengthTrainingData(StrengthTrainingData data);

    @Query("UPDATE strength_training_data SET duration=:duration, heart_rate=:heartRate, " +
            "calories=:calories WHERE activity_id=:activityId")
    void updateStrengthTrainingData(int activityId, long duration,
                                    int heartRate, int calories);

    @Query("SELECT * FROM strength_training_data WHERE activity_id = :activityId")
    StrengthTrainingData getStrengthTrainingData(int activityId);

    // --- Yoga ---
    @Insert
    void insertYogaData(YogaData data);

    @Query("UPDATE yoga_data SET duration=:duration, heart_rate=:heartRate, " +
            "calories=:calories WHERE activity_id=:activityId")
    void updateYogaData(int activityId, long duration, int heartRate, int calories);

    @Query("SELECT * FROM yoga_data WHERE activity_id = :activityId")
    YogaData getYogaData(int activityId);
}