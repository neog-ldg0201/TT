package com.example.financetracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.financetracker.R;
import com.example.financetracker.utils.DateUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private TextView monthYearText;
    private TextView totalIncomeText;
    private TextView totalExpenseText;
    private TextView balanceText;
    private FloatingActionButton fabAddTransaction;

    private MainViewModel viewModel;

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
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        viewModel.getAllTransactions().observe(this, transactions -> {
            updateMonthlySummary();
            // Update calendar decorators here if needed
        });
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
            updateMonthYearText(date);
            updateMonthlySummary();
        });

        // Set initial month/year text
        updateMonthYearText(calendarView.getCurrentDate());
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
