package com.AidenLiriano.newyou;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface MessageDao {

    @Insert
    void insertMessage(WatchMessage message);

    @Query("SELECT * FROM watch_messages ORDER BY timestamp DESC")
    List<WatchMessage> getAllMessages();
}