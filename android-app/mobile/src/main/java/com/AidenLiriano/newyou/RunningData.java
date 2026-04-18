package com.AidenLiriano.newyou;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;

@Entity(
        tableName = "running_data",
        foreignKeys = @ForeignKey(
                entity = Activity.class,
                parentColumns = "activity_id",
                childColumns = "activity_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class RunningData {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "running_id")
    public int runningId;

    @ColumnInfo(name = "activity_id")
    public int activityId;

    @ColumnInfo(name = "duration")
    public long duration;

    @ColumnInfo(name = "distance")
    public float distance;

    @ColumnInfo(name = "pace")
    public float pace;

    @ColumnInfo(name = "heart_rate")
    public int heartRate;

    @ColumnInfo(name = "calories")
    public int calories;

    @ColumnInfo(name = "step_count")
    public int stepCount;

    public RunningData(int activityId) {
        this.activityId = activityId;
        this.duration = 0;
        this.distance = 0;
        this.pace = 0;
        this.heartRate = 0;
        this.calories = 0;
        this.stepCount = 0;
    }
}