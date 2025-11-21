package com.example.financetracker.ui;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import com.example.financetracker.database.TransactionRepository;
import com.example.financetracker.model.Transaction;

public class AddTransactionViewModel extends AndroidViewModel {
    private TransactionRepository repository;

    public AddTransactionViewModel(Application application) {
        super(application);
        repository = new TransactionRepository(application);
    }

    public void insert(Transaction transaction) {
        repository.insert(transaction);
    }
}
