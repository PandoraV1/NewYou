package com.AidenLiriano.newyou;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;

@Entity(
        tableName = "swimming_data",
        foreignKeys = @ForeignKey(
                entity = Activity.class,
                parentColumns = "activity_id",
                childColumns = "activity_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class SwimmingData {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "swimming_id")
    public int swimmingId;

    @ColumnInfo(name = "activity_id")
    public int activityId;

    @ColumnInfo(name = "duration")
    public long duration;

    @ColumnInfo(name = "laps")
    public int laps;

    @ColumnInfo(name = "distance")
    public float distance;

    @ColumnInfo(name = "pace")
    public float pace;

    @ColumnInfo(name = "calories")
    public int calories;

    @ColumnInfo(name = "heart_rate")
    public int heartRate;

    public SwimmingData(int activityId) {
        this.activityId = activityId;
        this.duration = 0;
        this.laps = 0;
        this.distance = 0;
        this.pace = 0;
        this.calories = 0;
        this.heartRate = 0;
    }
}