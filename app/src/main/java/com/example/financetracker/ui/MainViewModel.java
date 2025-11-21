package com.example.financetracker.ui;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.financetracker.database.TransactionRepository;
import com.example.financetracker.model.Transaction;

import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private TransactionRepository repository;
    private LiveData<List<Transaction>> allTransactions;

    public MainViewModel(Application application) {
        super(application);
        repository = new TransactionRepository(application);
        allTransactions = repository.getAllTransactions();
    }

    public LiveData<List<Transaction>> getAllTransactions() {
        return allTransactions;
    }

    public void getMonthlyIncome(String yearMonth, TransactionRepository.OnResultCallback<Long> callback) {
        repository.getIncomeByDate(yearMonth, callback);
    }

    public void getMonthlyExpense(String yearMonth, TransactionRepository.OnResultCallback<Long> callback) {
        repository.getExpenseByDate(yearMonth, callback);
    }

    public void getMonthlyBalance(String yearMonth, TransactionRepository.OnResultCallback<Long> callback) {
        repository.getIncomeByDate(yearMonth, income -> {
            repository.getExpenseByDate(yearMonth, expense -> {
                long balance = income - expense;
                callback.onResult(balance);
            });
        });
    }
}
