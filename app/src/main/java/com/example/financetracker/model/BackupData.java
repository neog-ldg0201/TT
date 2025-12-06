package com.example.financetracker.model;

import java.util.List;

public class BackupData {
    private int version;
    private String exportDate;
    private List<Transaction> transactions;
    private CategoryData categories;

    public BackupData() {
    }

    public BackupData(int version, String exportDate, List<Transaction> transactions, CategoryData categories) {
        this.version = version;
        this.exportDate = exportDate;
        this.transactions = transactions;
        this.categories = categories;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getExportDate() {
        return exportDate;
    }

    public void setExportDate(String exportDate) {
        this.exportDate = exportDate;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public CategoryData getCategories() {
        return categories;
    }

    public void setCategories(CategoryData categories) {
        this.categories = categories;
    }

    public static class CategoryData {
        private List<String> income;
        private List<String> expense;

        public CategoryData() {
        }

        public CategoryData(List<String> income, List<String> expense) {
            this.income = income;
            this.expense = expense;
        }

        public List<String> getIncome() {
            return income;
        }

        public void setIncome(List<String> income) {
            this.income = income;
        }

        public List<String> getExpense() {
            return expense;
        }

        public void setExpense(List<String> expense) {
            this.expense = expense;
        }
    }
}
