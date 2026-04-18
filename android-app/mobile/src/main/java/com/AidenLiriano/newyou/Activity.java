package com.AidenLiriano.newyou;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;

@Entity(
        tableName = "activities",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "user_id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class Activity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "activity_id")
    public int activityId;

    @ColumnInfo(name = "user_id")
    public int userId;

    // 1=Running, 2=Swimming, 3=Biking, 4=Walking,
    // 5=Hiking, 6=Meditation, 7=Strength Training, 8=Yoga
    @ColumnInfo(name = "activity_type")
    public int activityType;

    @ColumnInfo(name = "start_time")
    public long startTime;

    @ColumnInfo(name = "end_time")
    public long endTime;

    public Activity(int userId, int activityType, long startTime, long endTime) {
        this.userId = userId;
        this.activityType = activityType;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}