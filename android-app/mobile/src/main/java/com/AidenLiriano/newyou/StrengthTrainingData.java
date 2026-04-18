package com.AidenLiriano.newyou;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;

@Entity(
        tableName = "strength_training_data",
        foreignKeys = @ForeignKey(
                entity = Activity.class,
                parentColumns = "activity_id",
                childColumns = "activity_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class StrengthTrainingData {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "strength_training_id")
    public int strengthTrainingId;

    @ColumnInfo(name = "activity_id")
    public int activityId;

    @ColumnInfo(name = "duration")
    public long duration;

    @ColumnInfo(name = "heart_rate")
    public int heartRate;

    @ColumnInfo(name = "calories")
    public int calories;

    public StrengthTrainingData(int activityId) {
        this.activityId = activityId;
        this.duration = 0;
        this.heartRate = 0;
        this.calories = 0;
    }
}