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

    @Query("SELECT * FROM activities WHERE user_id = :userId ORDER BY start_time DESC")
    List<Activity> getActivitiesForUser(int userId);

    // --- Running ---
    @Insert
    void insertRunningData(RunningData data);

    @Query("SELECT * FROM running_data WHERE activity_id = :activityId")
    RunningData getRunningData(int activityId);

    // --- Swimming ---
    @Insert
    void insertSwimmingData(SwimmingData data);

    @Query("SELECT * FROM swimming_data WHERE activity_id = :activityId")
    SwimmingData getSwimmingData(int activityId);

    // --- Biking ---
    @Insert
    void insertBikingData(BikingData data);

    @Query("SELECT * FROM biking_data WHERE activity_id = :activityId")
    BikingData getBikingData(int activityId);

    // --- Walking ---
    @Insert
    void insertWalkingData(WalkingData data);

    @Query("SELECT * FROM walking_data WHERE activity_id = :activityId")
    WalkingData getWalkingData(int activityId);

    // --- Hiking ---
    @Insert
    void insertHikingData(HikingData data);

    @Query("SELECT * FROM hiking_data WHERE activity_id = :activityId")
    HikingData getHikingData(int activityId);

    // --- Meditation ---
    @Insert
    void insertMeditationData(MeditationData data);

    @Query("SELECT * FROM meditation_data WHERE activity_id = :activityId")
    MeditationData getMeditationData(int activityId);

    // --- Strength Training ---
    @Insert
    void insertStrengthTrainingData(StrengthTrainingData data);

    @Query("SELECT * FROM strength_training_data WHERE activity_id = :activityId")
    StrengthTrainingData getStrengthTrainingData(int activityId);

    // --- Yoga ---
    @Insert
    void insertYogaData(YogaData data);

    @Query("SELECT * FROM yoga_data WHERE activity_id = :activityId")
    YogaData getYogaData(int activityId);
}