package com.AidenLiriano.newyou;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;

@Entity(
        tableName = "meditation_data",
        foreignKeys = @ForeignKey(
                entity = Activity.class,
                parentColumns = "activity_id",
                childColumns = "activity_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class MeditationData {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "meditation_id")
    public int meditationId;

    @ColumnInfo(name = "activity_id")
    public int activityId;

    @ColumnInfo(name = "duration")
    public long duration;

    @ColumnInfo(name = "heart_rate")
    public int heartRate;

    @ColumnInfo(name = "heart_rate_start")
    public int heartRateStart;

    @ColumnInfo(name = "heart_rate_end")
    public int heartRateEnd;

    public MeditationData(int activityId) {
        this.activityId = activityId;
        this.duration = 0;
        this.heartRate = 0;
        this.heartRateStart = 0;
        this.heartRateEnd = 0;
    }
}