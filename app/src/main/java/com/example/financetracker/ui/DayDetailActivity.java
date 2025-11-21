package com.example.financetracker.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financetracker.R;
import com.example.financetracker.model.Transaction;
import com.example.financetracker.utils.DateUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DayDetailActivity extends AppCompatActivity {

    private TextView dateText;
    private TextView dayIncomeText;
    private TextView dayExpenseText;
    private RecyclerView transactionsRecyclerView;
    private TextView emptyStateText;

    private TransactionAdapter adapter;
    private DayDetailViewModel viewModel;
    private String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_detail);

        date = getIntent().getStringExtra("date");
        if (date == null) {
            date = DateUtils.getCurrentDate();
        }

        initViews();
        setupViewModel();
        setupRecyclerView();
    }

    private void initViews() {
        dateText = findViewById(R.id.dateText);
        dayIncomeText = findViewById(R.id.dayIncomeText);
        dayExpenseText = findViewById(R.id.dayExpenseText);
        transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);

        dateText.setText(DateUtils.formatDateKorean(date));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("거래 내역");
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(DayDetailViewModel.class);

        viewModel.getTransactionsByDate(date).observe(this, transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                adapter.setTransactions(transactions);
                transactionsRecyclerView.setVisibility(View.VISIBLE);
                emptyStateText.setVisibility(View.GONE);
                updateDaySummary(transactions);
            } else {
                transactionsRecyclerView.setVisibility(View.GONE);
                emptyStateText.setVisibility(View.VISIBLE);
                dayIncomeText.setText("0원");
                dayExpenseText.setText("0원");
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(new ArrayList<>());
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionsRecyclerView.setAdapter(adapter);
    }

    private void updateDaySummary(List<Transaction> transactions) {
        long income = 0;
        long expense = 0;

        for (Transaction transaction : transactions) {
            if ("INCOME".equals(transaction.getType())) {
                income += transaction.getAmount();
            } else {
                expense += transaction.getAmount();
            }
        }

        dayIncomeText.setText(formatCurrency(income));
        dayExpenseText.setText(formatCurrency(expense));
    }

    private String formatCurrency(long amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.KOREA);
        return formatter.format(amount) + "원";
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
