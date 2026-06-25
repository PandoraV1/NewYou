package com.AidenLiriano.newyou;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Stores user-created custom workout definitions.
 * Each custom workout has a name, an icon choice, and a set of
 * feature flags indicating which metrics to track.
 */
@Entity(tableName = "custom_workouts")
public class CustomWorkout {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;
    public String name;
    public int iconIndex;   // 0-7 maps to one of 8 icons

    // metrics created workout tracks
    public boolean trackHeartRate;
    public boolean trackCalories;
    public boolean trackDuration;
    public boolean trackSteps;
    public boolean trackDistance;
    public boolean trackPace;
    public boolean trackSpeed;
    public boolean trackElevation;
    public boolean trackLaps;

    // Timestamps
    public long createdAt;

    public CustomWorkout() {}

    public CustomWorkout(int userId, String name, int iconIndex,
                         boolean trackHeartRate, boolean trackCalories,
                         boolean trackDuration, boolean trackSteps,
                         boolean trackDistance, boolean trackPace,
                         boolean trackSpeed, boolean trackElevation,
                         boolean trackLaps) {
        this.userId        = userId;
        this.name          = name;
        this.iconIndex     = iconIndex;
        this.trackHeartRate = trackHeartRate;
        this.trackCalories  = trackCalories;
        this.trackDuration  = trackDuration;
        this.trackSteps     = trackSteps;
        this.trackDistance  = trackDistance;
        this.trackPace      = trackPace;
        this.trackSpeed     = trackSpeed;
        this.trackElevation = trackElevation;
        this.trackLaps      = trackLaps;
        this.createdAt      = System.currentTimeMillis();
    }
}