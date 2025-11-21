package com.example.financetracker.ui;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.financetracker.database.TransactionRepository;
import com.example.financetracker.model.Transaction;

import java.util.List;

public class DayDetailViewModel extends AndroidViewModel {
    private TransactionRepository repository;

    public DayDetailViewModel(Application application) {
        super(application);
        repository = new TransactionRepository(application);
    }

    public LiveData<List<Transaction>> getTransactionsByDate(String date) {
        return repository.getTransactionsByDate(date);
    }
}
