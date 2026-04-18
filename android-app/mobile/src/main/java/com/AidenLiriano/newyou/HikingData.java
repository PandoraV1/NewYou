package com.AidenLiriano.newyou;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;

@Entity(
        tableName = "hiking_data",
        foreignKeys = @ForeignKey(
                entity = Activity.class,
                parentColumns = "activity_id",
                childColumns = "activity_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class HikingData {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "hiking_id")
    public int hikingId;

    @ColumnInfo(name = "activity_id")
    public int activityId;

    @ColumnInfo(name = "duration")
    public long duration;

    @ColumnInfo(name = "distance")
    public float distance;

    @ColumnInfo(name = "elevation_gain")
    public float elevationGain;

    @ColumnInfo(name = "elevation_loss")
    public float elevationLoss;

    @ColumnInfo(name = "heart_rate")
    public int heartRate;

    @ColumnInfo(name = "calories")
    public int calories;

    @ColumnInfo(name = "pace")
    public float pace;

    public HikingData(int activityId) {
        this.activityId = activityId;
        this.duration = 0;
        this.distance = 0;
        this.elevationGain = 0;
        this.elevationLoss = 0;
        this.heartRate = 0;
        this.calories = 0;
        this.pace = 0;
    }
}