package com.AidenLiriano.newyou;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;

@Entity(
        tableName = "biking_data",
        foreignKeys = @ForeignKey(
                entity = Activity.class,
                parentColumns = "activity_id",
                childColumns = "activity_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class BikingData {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "biking_id")
    public int bikingId;

    @ColumnInfo(name = "activity_id")
    public int activityId;

    @ColumnInfo(name = "duration")
    public long duration;

    @ColumnInfo(name = "distance")
    public float distance;

    @ColumnInfo(name = "speed")
    public float speed;

    @ColumnInfo(name = "heart_rate")
    public int heartRate;

    @ColumnInfo(name = "calories")
    public int calories;

    public BikingData(int activityId) {
        this.activityId = activityId;
        this.duration = 0;
        this.distance = 0;
        this.speed = 0;
        this.heartRate = 0;
        this.calories = 0;
    }
}