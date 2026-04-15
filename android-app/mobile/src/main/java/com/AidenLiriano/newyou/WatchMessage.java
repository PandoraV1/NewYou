package com.AidenLiriano.newyou;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "watch_messages")
public class WatchMessage {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "message_text")
    public String messageText;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    // Constructor
    public WatchMessage(String messageText, long timestamp) {
        this.messageText = messageText;
        this.timestamp = timestamp;
    }
}