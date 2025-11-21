package com.example.financetracker.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financetracker.R;
import com.example.financetracker.model.Transaction;
import com.example.financetracker.utils.DateUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private TextView monthYearText;
    private TextView totalIncomeText;
    private TextView totalExpenseText;
    private TextView balanceText;
    private FloatingActionButton fabAddTransaction;
    private RecyclerView recentTransactionsRecyclerView;
    private TransactionAdapter recentTransactionsAdapter;
    private TextView monthlyTransactionsHeader;

    private MainViewModel viewModel;
    private String currentYearMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupViewModel();
        setupCalendar();
        setupFab();
        updateMonthlySummary();
    }

    private void initViews() {
        calendarView = findViewById(R.id.calendarView);
        monthYearText = findViewById(R.id.monthYearText);
        totalIncomeText = findViewById(R.id.totalIncomeText);
        totalExpenseText = findViewById(R.id.totalExpenseText);
        balanceText = findViewById(R.id.balanceText);
        fabAddTransaction = findViewById(R.id.fabAddTransaction);
        recentTransactionsRecyclerView = findViewById(R.id.recentTransactionsRecyclerView);
        monthlyTransactionsHeader = findViewById(R.id.monthlyTransactionsHeader);

        // Setup RecyclerView
        recentTransactionsAdapter = new TransactionAdapter(new ArrayList<>());
        recentTransactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recentTransactionsRecyclerView.setAdapter(recentTransactionsAdapter);

        // Initialize current year-month
        CalendarDay today = CalendarDay.today();
        currentYearMonth = String.format(Locale.US, "%04d-%02d", today.getYear(), today.getMonth());
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        viewModel.getAllTransactions().observe(this, transactions -> {
            if (transactions != null) {
                // Filter transactions for current month only
                List<Transaction> monthlyTransactions = filterTransactionsByMonth(transactions, currentYearMonth);
                recentTransactionsAdapter.setTransactions(monthlyTransactions);

                // Update calendar decorators with all transactions
                updateCalendarDecorators(transactions);
            }
            updateMonthlySummary();
        });
    }

    private List<Transaction> filterTransactionsByMonth(List<Transaction> transactions, String yearMonth) {
        List<Transaction> filtered = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.getDate().startsWith(yearMonth)) {
                filtered.add(transaction);
            }
        }
        return filtered;
    }

    private void setupCalendar() {
        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(MaterialCalendarView widget, CalendarDay date, boolean selected) {
                String dateStr = DateUtils.calendarDayToString(date);
                openDayDetail(dateStr);
            }
        });

        calendarView.setOnMonthChangedListener((widget, date) -> {
            // Update current year-month
            currentYearMonth = String.format(Locale.US, "%04d-%02d", date.getYear(), date.getMonth());

            // Update header text
            updateMonthYearText(date);
            updateMonthlyTransactionsHeader(date);

            // Update summary and refresh transaction list
            updateMonthlySummary();
            refreshMonthlyTransactions();
        });

        // Set initial month/year text
        CalendarDay initialDate = calendarView.getCurrentDate();
        updateMonthYearText(initialDate);
        updateMonthlyTransactionsHeader(initialDate);
    }

    private void setupFab() {
        fabAddTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
            startActivity(intent);
        });
    }

    private void updateMonthYearText(CalendarDay date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(date.getYear(), date.getMonth() - 1, date.getDay());

        String monthYear = String.format(Locale.KOREA, "%d년 %d월",
            date.getYear(), date.getMonth());
        monthYearText.setText(monthYear);
    }

    private void updateMonthlySummary() {
        CalendarDay currentDate = calendarView.getCurrentDate();
        String yearMonth = String.format(Locale.US, "%04d-%02d",
            currentDate.getYear(), currentDate.getMonth());

        viewModel.getMonthlyIncome(yearMonth, income -> {
            runOnUiThread(() -> {
                totalIncomeText.setText(formatCurrency(income));
            });
        });

        viewModel.getMonthlyExpense(yearMonth, expense -> {
            runOnUiThread(() -> {
                totalExpenseText.setText(formatCurrency(expense));
            });
        });

        viewModel.getMonthlyBalance(yearMonth, balance -> {
            runOnUiThread(() -> {
                balanceText.setText(formatCurrency(balance));
            });
        });
    }

    private void openDayDetail(String date) {
        Intent intent = new Intent(MainActivity.this, DayDetailActivity.class);
        intent.putExtra("date", date);
        startActivity(intent);
    }

    private void updateCalendarDecorators(List<Transaction> transactions) {
        // Group transactions by date
        Map<String, Long> incomeByDate = new HashMap<>();
        Map<String, Long> expenseByDate = new HashMap<>();
        HashSet<CalendarDay> incomeDates = new HashSet<>();
        HashSet<CalendarDay> expenseDates = new HashSet<>();

        for (Transaction transaction : transactions) {
            String date = transaction.getDate();
            long amount = transaction.getAmount();

            if ("INCOME".equals(transaction.getType())) {
                incomeByDate.put(date, incomeByDate.getOrDefault(date, 0L) + amount);
                incomeDates.add(dateStringToCalendarDay(date));
            } else {
                expenseByDate.put(date, expenseByDate.getOrDefault(date, 0L) + amount);
                expenseDates.add(dateStringToCalendarDay(date));
            }
        }

        // Remove old decorators
        calendarView.removeDecorators();

        // Add dot decorators for income and expense
        if (!incomeDates.isEmpty()) {
            calendarView.addDecorator(new DotDecorator(
                getResources().getColor(R.color.income_color), incomeDates));
        }
        if (!expenseDates.isEmpty()) {
            calendarView.addDecorator(new DotDecorator(
                getResources().getColor(R.color.expense_color), expenseDates));
        }
    }

    private CalendarDay dateStringToCalendarDay(String dateStr) {
        try {
            String[] parts = dateStr.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);
            return CalendarDay.from(year, month, day);
        } catch (Exception e) {
            return CalendarDay.today();
        }
    }

    private void updateMonthlyTransactionsHeader(CalendarDay date) {
        String headerText = String.format(Locale.KOREA, "%d년 %d월 거래 내역",
            date.getYear(), date.getMonth());
        monthlyTransactionsHeader.setText(headerText);
    }

    private void refreshMonthlyTransactions() {
        // Trigger observer to refresh the transaction list
        viewModel.getAllTransactions().observe(this, transactions -> {
            if (transactions != null) {
                List<Transaction> monthlyTransactions = filterTransactionsByMonth(transactions, currentYearMonth);
                recentTransactionsAdapter.setTransactions(monthlyTransactions);
            }
        });
    }

    private String formatCurrency(long amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.KOREA);
        return formatter.format(amount) + "원";
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMonthlySummary();
    }
}
