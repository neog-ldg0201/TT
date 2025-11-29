package com.example.financetracker.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;

import com.example.financetracker.model.NotificationHistory;

import java.util.List;

@Dao
public interface NotificationHistoryDao {
    @Insert
    void insert(NotificationHistory notification);

    @Query("SELECT * FROM notification_history ORDER BY timestamp DESC")
    LiveData<List<NotificationHistory>> getAllNotifications();

    @Delete
    void delete(NotificationHistory notification);

    @Query("DELETE FROM notification_history")
    void deleteAll();
}
