package com.AidenLiriano.newyou;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

/**
 * Stores the recorded data for a completed custom workout session.
 * Only the fields the user chose to track will have real values.
 */
@Entity(
        tableName = "custom_workout_data",
        foreignKeys = @ForeignKey(
                entity = Activity.class,
                parentColumns = "activity_id",
                childColumns = "activityId",
                onDelete = ForeignKey.CASCADE
        )
)
public class CustomWorkoutData {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int activityId;
    public int customWorkoutId; // links to the CustomWorkout definition

    public long duration;       // seconds
    public int  heartRate;      // bpm avg
    public int  calories;       // kcal
    public int  stepCount;
    public float distance;      // km
    public float pace;          // min/km
    public float speed;         // km/h
    public float elevationGain; // metres
    public float elevationLoss; // metres
    public int  laps;

    public CustomWorkoutData() {}

    public CustomWorkoutData(int activityId, int customWorkoutId) {
        this.activityId       = activityId;
        this.customWorkoutId  = customWorkoutId;
    }
}