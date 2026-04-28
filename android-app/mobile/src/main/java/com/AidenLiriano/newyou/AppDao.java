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

    // Update end_time and duration for an existing activity
    @Query("UPDATE activities SET end_time = :endTime WHERE activity_id = :activityId")
    void updateActivityEndTime(int activityId, long endTime);

    @Query("SELECT * FROM activities WHERE user_id = :userId ORDER BY start_time DESC")
    List<Activity> getActivitiesForUser(int userId);

    // --- Running ---
    @Insert
    void insertRunningData(RunningData data);

    @Query("UPDATE running_data SET duration = :duration WHERE activity_id = :activityId")
    void updateRunningDuration(int activityId, long duration);

    @Query("SELECT * FROM running_data WHERE activity_id = :activityId")
    RunningData getRunningData(int activityId);

    // --- Swimming ---
    @Insert
    void insertSwimmingData(SwimmingData data);

    @Query("UPDATE swimming_data SET duration = :duration WHERE activity_id = :activityId")
    void updateSwimmingDuration(int activityId, long duration);

    @Query("SELECT * FROM swimming_data WHERE activity_id = :activityId")
    SwimmingData getSwimmingData(int activityId);

    // --- Biking ---
    @Insert
    void insertBikingData(BikingData data);

    @Query("UPDATE biking_data SET duration = :duration WHERE activity_id = :activityId")
    void updateBikingDuration(int activityId, long duration);

    @Query("SELECT * FROM biking_data WHERE activity_id = :activityId")
    BikingData getBikingData(int activityId);

    // --- Walking ---
    @Insert
    void insertWalkingData(WalkingData data);

    @Query("UPDATE walking_data SET duration = :duration WHERE activity_id = :activityId")
    void updateWalkingDuration(int activityId, long duration);

    @Query("SELECT * FROM walking_data WHERE activity_id = :activityId")
    WalkingData getWalkingData(int activityId);

    // --- Hiking ---
    @Insert
    void insertHikingData(HikingData data);

    @Query("UPDATE hiking_data SET duration = :duration WHERE activity_id = :activityId")
    void updateHikingDuration(int activityId, long duration);

    @Query("SELECT * FROM hiking_data WHERE activity_id = :activityId")
    HikingData getHikingData(int activityId);

    // --- Meditation ---
    @Insert
    void insertMeditationData(MeditationData data);

    @Query("UPDATE meditation_data SET duration = :duration WHERE activity_id = :activityId")
    void updateMeditationDuration(int activityId, long duration);

    @Query("SELECT * FROM meditation_data WHERE activity_id = :activityId")
    MeditationData getMeditationData(int activityId);

    // --- Strength Training ---
    @Insert
    void insertStrengthTrainingData(StrengthTrainingData data);

    @Query("UPDATE strength_training_data SET duration = :duration WHERE activity_id = :activityId")
    void updateStrengthTrainingDuration(int activityId, long duration);

    @Query("SELECT * FROM strength_training_data WHERE activity_id = :activityId")
    StrengthTrainingData getStrengthTrainingData(int activityId);

    // --- Yoga ---
    @Insert
    void insertYogaData(YogaData data);

    @Query("UPDATE yoga_data SET duration = :duration WHERE activity_id = :activityId")
    void updateYogaDuration(int activityId, long duration);

    @Query("SELECT * FROM yoga_data WHERE activity_id = :activityId")
    YogaData getYogaData(int activityId);
}