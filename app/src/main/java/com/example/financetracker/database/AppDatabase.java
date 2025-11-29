package com.example.financetracker.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.financetracker.data.NotificationHistoryDao;
import com.example.financetracker.model.NotificationHistory;
import com.example.financetracker.model.Transaction;

@Database(entities = {Transaction.class, NotificationHistory.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract TransactionDao transactionDao();
    public abstract NotificationHistoryDao notificationHistoryDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "finance_tracker_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
