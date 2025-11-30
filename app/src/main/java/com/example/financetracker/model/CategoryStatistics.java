package com.example.financetracker.model;

public class CategoryStatistics {
    private String category;
    private long totalAmount;
    private float percentage;

    public CategoryStatistics(String category, long totalAmount) {
        this.category = category;
        this.totalAmount = totalAmount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }
}
