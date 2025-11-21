package com.example.financetracker.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String type; // "INCOME" or "EXPENSE"
    private long amount;
    private String category;
    private String description;
    private String date; // Format: yyyy-MM-dd
    private String time; // Format: HH:mm
    private boolean isFromNotification;

    public Transaction(String type, long amount, String category, String description,
                      String date, String time, boolean isFromNotification) {
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.date = date;
        this.time = time;
        this.isFromNotification = isFromNotification;
    }

    // Getters
    public int getId() { return id; }
    public String getType() { return type; }
    public long getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public boolean isFromNotification() { return isFromNotification; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setType(String type) { this.type = type; }
    public void setAmount(long amount) { this.amount = amount; }
    public void setCategory(String category) { this.category = category; }
    public void setDescription(String description) { this.description = description; }
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
    public void setFromNotification(boolean fromNotification) { isFromNotification = fromNotification; }
}
