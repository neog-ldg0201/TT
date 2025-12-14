package com.example.financetracker.model;

public class CategoryStatistics {
    private String category;
    private long totalAmount;
    private float percentage;
    private int colorIndex;

    public CategoryStatistics(String category, long totalAmount) {
        this.category = category;
        this.totalAmount = totalAmount;
        this.colorIndex = -1;
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

    public int getColorIndex() {
        return colorIndex;
    }

    public void setColorIndex(int colorIndex) {
        this.colorIndex = colorIndex;
    }
}
