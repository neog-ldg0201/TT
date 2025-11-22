package com.example.financetracker.ui;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import com.example.financetracker.database.TransactionRepository;
import com.example.financetracker.model.Transaction;

public class AddTransactionViewModel extends AndroidViewModel {
    private TransactionRepository repository;

    public interface TransactionCallback {
        void onResult(Transaction transaction);
    }

    public AddTransactionViewModel(Application application) {
        super(application);
        repository = new TransactionRepository(application);
    }

    public void insert(Transaction transaction) {
        repository.insert(transaction);
    }

    public void update(Transaction transaction) {
        repository.update(transaction);
    }

    public void delete(Transaction transaction) {
        repository.delete(transaction);
    }

    public void getTransactionById(int id, TransactionCallback callback) {
        repository.getTransactionById(id, callback);
    }
}
