package com.example.financetracker.utils;

import android.content.Context;
import android.net.Uri;

import com.example.financetracker.database.AppDatabase;
import com.example.financetracker.model.BackupData;
import com.example.financetracker.model.Transaction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

public class BackupManager {
    private static final int BACKUP_VERSION = 1;
    private final Context context;
    private final Gson gson;

    public BackupManager(Context context) {
        this.context = context;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void exportData(Uri uri, ExportCallback callback) {
        new Thread(() -> {
            try {
                // Get all transactions from database
                AppDatabase db = AppDatabase.getInstance(context);
                List<Transaction> transactions = db.transactionDao().getAllTransactionsSync();

                // Get categories
                CategoryManager categoryManager = new CategoryManager(context);
                BackupData.CategoryData categoryData = new BackupData.CategoryData(
                        categoryManager.getIncomeCategories(),
                        categoryManager.getExpenseCategories()
                );

                // Create backup data
                BackupData backupData = new BackupData(
                        BACKUP_VERSION,
                        DateUtils.getCurrentDate(),
                        transactions,
                        categoryData
                );

                // Convert to JSON
                String json = gson.toJson(backupData);

                // Write to file
                OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    outputStream.write(json.getBytes());
                    outputStream.close();
                    callback.onSuccess(transactions.size());
                } else {
                    callback.onError("파일을 열 수 없습니다");
                }
            } catch (Exception e) {
                callback.onError("백업 중 오류 발생: " + e.getMessage());
            }
        }).start();
    }

    public void importData(Uri uri, ImportCallback callback) {
        new Thread(() -> {
            try {
                // Read from file
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                if (inputStream == null) {
                    callback.onError("파일을 열 수 없습니다");
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();
                inputStream.close();

                String json = stringBuilder.toString();

                // Parse JSON
                BackupData backupData = gson.fromJson(json, BackupData.class);

                if (backupData == null || backupData.getTransactions() == null) {
                    callback.onError("잘못된 백업 파일입니다");
                    return;
                }

                // Import transactions
                AppDatabase db = AppDatabase.getInstance(context);
                List<Transaction> transactions = backupData.getTransactions();

                // Clear existing data and insert new data
                db.transactionDao().deleteAll();
                for (Transaction transaction : transactions) {
                    // Reset ID to let Room auto-generate new IDs
                    transaction.setId(0);
                    db.transactionDao().insertSync(transaction);
                }

                // Import categories if available
                if (backupData.getCategories() != null) {
                    CategoryManager categoryManager = new CategoryManager(context);
                    BackupData.CategoryData categoryData = backupData.getCategories();

                    if (categoryData.getIncome() != null) {
                        categoryManager.setIncomeCategories(categoryData.getIncome());
                    }
                    if (categoryData.getExpense() != null) {
                        categoryManager.setExpenseCategories(categoryData.getExpense());
                    }
                }

                callback.onSuccess(transactions.size());
            } catch (Exception e) {
                callback.onError("복원 중 오류 발생: " + e.getMessage());
            }
        }).start();
    }

    public interface ExportCallback {
        void onSuccess(int count);
        void onError(String error);
    }

    public interface ImportCallback {
        void onSuccess(int count);
        void onError(String error);
    }
}
