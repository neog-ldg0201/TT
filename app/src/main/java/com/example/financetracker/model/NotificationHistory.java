package com.example.financetracker.model;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "notification_history",
        indices = {@Index(value = {"timestamp"})})
public class NotificationHistory {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String amount;
    private String description;
    private String type; // "INCOME" or "EXPENSE"
    private String date;
    private String time;
    private long timestamp; // For sorting

    public NotificationHistory(String amount, String description, String type, String date, String time, long timestamp) {
        this.amount = amount;
        this.description = description;
        this.type = type;
        this.date = date;
        this.time = time;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
