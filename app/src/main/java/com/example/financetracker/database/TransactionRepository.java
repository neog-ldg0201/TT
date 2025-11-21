package com.example.financetracker.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.financetracker.model.Transaction;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionRepository {
    private TransactionDao transactionDao;
    private ExecutorService executorService;

    public TransactionRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        transactionDao = database.transactionDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(Transaction transaction) {
        executorService.execute(() -> transactionDao.insert(transaction));
    }

    public void update(Transaction transaction) {
        executorService.execute(() -> transactionDao.update(transaction));
    }

    public void delete(Transaction transaction) {
        executorService.execute(() -> transactionDao.delete(transaction));
    }

    public LiveData<List<Transaction>> getAllTransactions() {
        return transactionDao.getAllTransactions();
    }

    public LiveData<List<Transaction>> getTransactionsByDate(String date) {
        return transactionDao.getTransactionsByDate(date);
    }

    public void getIncomeByDate(String date, OnResultCallback<Long> callback) {
        executorService.execute(() -> {
            long result = transactionDao.getIncomeByDate(date);
            callback.onResult(result);
        });
    }

    public void getExpenseByDate(String date, OnResultCallback<Long> callback) {
        executorService.execute(() -> {
            long result = transactionDao.getExpenseByDate(date);
            callback.onResult(result);
        });
    }

    public interface OnResultCallback<T> {
        void onResult(T result);
    }
}
