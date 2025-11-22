package com.example.financetracker.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.financetracker.model.Transaction;

import java.util.List;

@Dao
public interface TransactionDao {
    @Insert
    long insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    @Query("SELECT * FROM transactions ORDER BY date DESC, time DESC")
    LiveData<List<Transaction>> getAllTransactions();

    @Query("SELECT * FROM transactions WHERE date = :date ORDER BY time DESC")
    LiveData<List<Transaction>> getTransactionsByDate(String date);

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC, time DESC")
    LiveData<List<Transaction>> getTransactionsBetweenDates(String startDate, String endDate);

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME' AND date = :date")
    long getIncomeByDate(String date);

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND date = :date")
    long getExpenseByDate(String date);

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME' AND date LIKE :yearMonth || '%'")
    long getMonthlyIncome(String yearMonth);

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND date LIKE :yearMonth || '%'")
    long getMonthlyExpense(String yearMonth);

    @Query("SELECT * FROM transactions WHERE id = :id")
    Transaction getTransactionById(int id);
}
