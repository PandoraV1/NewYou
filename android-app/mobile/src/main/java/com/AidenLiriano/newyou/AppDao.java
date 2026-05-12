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

    @Query("UPDATE user SET name=:name, age=:age, height_inches=:heightInches, " +
            "weight_lbs=:weightLbs, gender=:gender WHERE user_id=:userId")
    void updateUser(int userId, String name, int age, int heightInches,
                    float weightLbs, String gender);

    @Query("SELECT * FROM user LIMIT 1")
    User getFirstUser();

    // --- Activities ---
    @Insert
    long insertActivity(Activity activity);

    @Query("UPDATE activities SET end_time = :endTime WHERE activity_id = :activityId")
    void updateActivityEndTime(int activityId, long endTime);

    @Query("SELECT * FROM activities WHERE user_id = :userId ORDER BY start_time DESC")
    List<Activity> getActivitiesForUser(int userId);

    @Query("SELECT COUNT(*) FROM activities WHERE user_id = :userId")
    int getTotalActivityCount(int userId);

    @Query("DELETE FROM activities")
    void clearAllActivities();

    // --- Global averages across all workouts ---

    // Average heart rate across all workout types
    @Query("SELECT AVG(heart_rate) FROM running_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    float getAvgHeartRateRunning(int userId);

    @Query("SELECT AVG(heart_rate) FROM swimming_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    float getAvgHeartRateSwimming(int userId);

    @Query("SELECT AVG(heart_rate) FROM biking_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    float getAvgHeartRateBiking(int userId);

    @Query("SELECT AVG(heart_rate) FROM walking_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    float getAvgHeartRateWalking(int userId);

    @Query("SELECT AVG(heart_rate) FROM hiking_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    float getAvgHeartRateHiking(int userId);

    @Query("SELECT AVG(heart_rate) FROM meditation_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    float getAvgHeartRateMeditation(int userId);

    @Query("SELECT AVG(heart_rate) FROM strength_training_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    float getAvgHeartRateStrength(int userId);

    @Query("SELECT AVG(heart_rate) FROM yoga_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    float getAvgHeartRateYoga(int userId);

    // Average calories across all workout types
    @Query("SELECT AVG(calories) FROM running_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    float getAvgCaloriesRunning(int userId);

    @Query("SELECT AVG(calories) FROM swimming_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    float getAvgCaloriesSwimming(int userId);

    @Query("SELECT AVG(calories) FROM biking_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    float getAvgCaloriesBiking(int userId);

    @Query("SELECT AVG(calories) FROM walking_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    float getAvgCaloriesWalking(int userId);

    @Query("SELECT AVG(calories) FROM hiking_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    float getAvgCaloriesHiking(int userId);

    @Query("SELECT AVG(calories) FROM strength_training_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    float getAvgCaloriesStrength(int userId);

    @Query("SELECT AVG(calories) FROM yoga_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    float getAvgCaloriesYoga(int userId);

    // Average duration across all workout types
    @Query("SELECT AVG(duration) FROM running_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    float getAvgDurationRunning(int userId);

    @Query("SELECT AVG(duration) FROM swimming_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    float getAvgDurationSwimming(int userId);

    @Query("SELECT AVG(duration) FROM biking_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    float getAvgDurationBiking(int userId);

    @Query("SELECT AVG(duration) FROM walking_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    float getAvgDurationWalking(int userId);

    @Query("SELECT AVG(duration) FROM hiking_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    float getAvgDurationHiking(int userId);

    @Query("SELECT AVG(duration) FROM meditation_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    float getAvgDurationMeditation(int userId);

    @Query("SELECT AVG(duration) FROM strength_training_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    float getAvgDurationStrength(int userId);

    @Query("SELECT AVG(duration) FROM yoga_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    float getAvgDurationYoga(int userId);

    // Total duration across all workout types (for weekly average)
    @Query("SELECT SUM(duration) FROM running_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    long getTotalDurationRunning(int userId);

    @Query("SELECT SUM(duration) FROM swimming_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    long getTotalDurationSwimming(int userId);

    @Query("SELECT SUM(duration) FROM biking_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    long getTotalDurationBiking(int userId);

    @Query("SELECT SUM(duration) FROM walking_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    long getTotalDurationWalking(int userId);

    @Query("SELECT SUM(duration) FROM hiking_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    long getTotalDurationHiking(int userId);

    @Query("SELECT SUM(duration) FROM meditation_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    long getTotalDurationMeditation(int userId);

    @Query("SELECT SUM(duration) FROM strength_training_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    long getTotalDurationStrength(int userId);

    @Query("SELECT SUM(duration) FROM yoga_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    long getTotalDurationYoga(int userId);

    // All time totals per activity type
    @Query("SELECT COUNT(*) FROM running_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId) AND duration > 0")
    int getRunningSessionCount(int userId);

    @Query("SELECT SUM(distance) FROM running_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    float getRunningTotalDistance(int userId);

    @Query("SELECT SUM(calories) FROM running_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    int getRunningTotalCalories(int userId);

    @Query("SELECT SUM(step_count) FROM running_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    int getRunningTotalSteps(int userId);

    @Query("SELECT COUNT(*) FROM swimming_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId) AND duration > 0")
    int getSwimmingSessionCount(int userId);

    @Query("SELECT SUM(laps) FROM swimming_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    int getSwimmingTotalLaps(int userId);

    @Query("SELECT SUM(calories) FROM swimming_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    int getSwimmingTotalCalories(int userId);

    @Query("SELECT COUNT(*) FROM biking_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId) AND duration > 0")
    int getBikingSessionCount(int userId);

    @Query("SELECT SUM(distance) FROM biking_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    float getBikingTotalDistance(int userId);

    @Query("SELECT SUM(calories) FROM biking_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    int getBikingTotalCalories(int userId);

    @Query("SELECT COUNT(*) FROM walking_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId) AND duration > 0")
    int getWalkingSessionCount(int userId);

    @Query("SELECT SUM(distance) FROM walking_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    float getWalkingTotalDistance(int userId);

    @Query("SELECT SUM(step_count) FROM walking_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    int getWalkingTotalSteps(int userId);

    @Query("SELECT SUM(calories) FROM walking_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    int getWalkingTotalCalories(int userId);

    @Query("SELECT COUNT(*) FROM hiking_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId) AND duration > 0")
    int getHikingSessionCount(int userId);

    @Query("SELECT SUM(distance) FROM hiking_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    float getHikingTotalDistance(int userId);

    @Query("SELECT SUM(elevation_gain) FROM hiking_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    float getHikingTotalElevationGain(int userId);

    @Query("SELECT SUM(calories) FROM hiking_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    int getHikingTotalCalories(int userId);

    @Query("SELECT COUNT(*) FROM meditation_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId) AND duration > 0")
    int getMeditationSessionCount(int userId);

    @Query("SELECT COUNT(*) FROM strength_training_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId) AND duration > 0")
    int getStrengthSessionCount(int userId);

    @Query("SELECT SUM(calories) FROM strength_training_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    int getStrengthTotalCalories(int userId);

    @Query("SELECT COUNT(*) FROM yoga_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId) AND duration > 0")
    int getYogaSessionCount(int userId);

    @Query("SELECT SUM(calories) FROM yoga_data WHERE activity_id IN " +
            "(SELECT activity_id FROM activities WHERE user_id = :userId)")
    int getYogaTotalCalories(int userId);

    // Earliest activity start time (to calculate weeks tracked)
    @Query("SELECT MIN(start_time) FROM activities WHERE user_id = :userId")
    long getEarliestActivityTime(int userId);

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