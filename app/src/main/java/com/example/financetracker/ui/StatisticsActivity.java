package com.example.financetracker.ui;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financetracker.R;
import com.example.financetracker.database.AppDatabase;
import com.example.financetracker.database.TransactionDao;
import com.example.financetracker.model.CategoryStatistics;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.tabs.TabLayout;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    private TextView periodText;
    private TextView totalTypeText;
    private TextView totalAmountText;
    private PieChart pieChart;
    private RecyclerView statisticsRecyclerView;
    private CategoryStatisticsAdapter adapter;

    private Button weeklyButton;
    private Button monthlyButton;
    private Button yearlyButton;
    private Button customButton;
    private ImageButton previousPeriodButton;
    private ImageButton nextPeriodButton;
    private TabLayout typeTabLayout;

    private TransactionDao transactionDao;

    private String periodType = "monthly"; // weekly, monthly, yearly, custom
    private String transactionType = "EXPENSE"; // INCOME or EXPENSE
    private Calendar currentCalendar;
    private String customStartDate;
    private String customEndDate;

    private static final int[] CHART_COLORS = {
            Color.rgb(255, 102, 102),   // Red
            Color.rgb(255, 178, 102),   // Orange
            Color.rgb(255, 255, 102),   // Yellow
            Color.rgb(178, 255, 102),   // Light Green
            Color.rgb(102, 255, 178),   // Mint
            Color.rgb(102, 178, 255),   // Light Blue
            Color.rgb(178, 102, 255),   // Purple
            Color.rgb(255, 102, 255),   // Pink
            Color.rgb(255, 102, 178),   // Light Pink
            Color.rgb(178, 178, 178),   // Gray
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        transactionDao = AppDatabase.getInstance(this).transactionDao();
        currentCalendar = Calendar.getInstance();

        initViews();
        setupToolbar();
        setupPieChart();
        setupRecyclerView();
        setupListeners();
        updateStatistics();
    }

    private void initViews() {
        periodText = findViewById(R.id.periodText);
        totalTypeText = findViewById(R.id.totalTypeText);
        totalAmountText = findViewById(R.id.totalAmountText);
        pieChart = findViewById(R.id.pieChart);
        statisticsRecyclerView = findViewById(R.id.statisticsRecyclerView);

        weeklyButton = findViewById(R.id.weeklyButton);
        monthlyButton = findViewById(R.id.monthlyButton);
        yearlyButton = findViewById(R.id.yearlyButton);
        customButton = findViewById(R.id.customButton);
        previousPeriodButton = findViewById(R.id.previousPeriodButton);
        nextPeriodButton = findViewById(R.id.nextPeriodButton);
        typeTabLayout = findViewById(R.id.typeTabLayout);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupPieChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setDrawCenterText(false);
        pieChart.setRotationEnabled(true);
        pieChart.setHighlightPerTapEnabled(true);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);
    }

    private void setupRecyclerView() {
        adapter = new CategoryStatisticsAdapter(new ArrayList<>(), CHART_COLORS);
        statisticsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        statisticsRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        weeklyButton.setOnClickListener(v -> {
            periodType = "weekly";
            updateButtonStates();
            updateStatistics();
        });

        monthlyButton.setOnClickListener(v -> {
            periodType = "monthly";
            updateButtonStates();
            updateStatistics();
        });

        yearlyButton.setOnClickListener(v -> {
            periodType = "yearly";
            updateButtonStates();
            updateStatistics();
        });

        customButton.setOnClickListener(v -> {
            showCustomDatePicker();
        });

        previousPeriodButton.setOnClickListener(v -> {
            movePeriod(-1);
        });

        nextPeriodButton.setOnClickListener(v -> {
            movePeriod(1);
        });

        typeTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                transactionType = tab.getPosition() == 0 ? "INCOME" : "EXPENSE";
                updateTypeText();
                updateStatistics();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Set monthly as default selected
        monthlyButton.setSelected(true);
        monthlyButton.setEnabled(false);
    }

    private void updateButtonStates() {
        weeklyButton.setSelected(periodType.equals("weekly"));
        monthlyButton.setSelected(periodType.equals("monthly"));
        yearlyButton.setSelected(periodType.equals("yearly"));
        customButton.setSelected(periodType.equals("custom"));

        weeklyButton.setEnabled(!periodType.equals("weekly"));
        monthlyButton.setEnabled(!periodType.equals("monthly"));
        yearlyButton.setEnabled(!periodType.equals("yearly"));
        customButton.setEnabled(!periodType.equals("custom"));
    }

    private void updateTypeText() {
        totalTypeText.setText(transactionType.equals("INCOME") ? "수입" : "지출");
        int color = transactionType.equals("INCOME") ?
                getResources().getColor(R.color.income_color, getTheme()) :
                getResources().getColor(R.color.expense_color, getTheme());
        totalAmountText.setTextColor(color);
    }

    private void movePeriod(int direction) {
        switch (periodType) {
            case "weekly":
                currentCalendar.add(Calendar.WEEK_OF_YEAR, direction);
                break;
            case "monthly":
                currentCalendar.add(Calendar.MONTH, direction);
                break;
            case "yearly":
                currentCalendar.add(Calendar.YEAR, direction);
                break;
        }
        updateStatistics();
    }

    private void showCustomDatePicker() {
        Calendar calendar = Calendar.getInstance();

        // Start date picker
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            customStartDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);

            // End date picker
            new DatePickerDialog(this, (view2, year2, month2, dayOfMonth2) -> {
                customEndDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year2, month2 + 1, dayOfMonth2);
                periodType = "custom";
                updateButtonStates();
                updateStatistics();
            }, year, month, dayOfMonth).show();

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateStatistics() {
        String[] dateRange = getDateRange();
        String startDate = dateRange[0];
        String endDate = dateRange[1];

        updatePeriodText(startDate, endDate);

        new Thread(() -> {
            List<TransactionDao.CategoryAmount> categoryAmounts = transactionType.equals("EXPENSE") ?
                    transactionDao.getExpenseCategoryStatistics(startDate, endDate) :
                    transactionDao.getIncomeCategoryStatistics(startDate, endDate);

            long total = 0;
            for (TransactionDao.CategoryAmount ca : categoryAmounts) {
                total += ca.totalAmount;
            }

            List<CategoryStatistics> statistics = new ArrayList<>();
            for (TransactionDao.CategoryAmount ca : categoryAmounts) {
                CategoryStatistics stat = new CategoryStatistics(ca.category, ca.totalAmount);
                stat.setPercentage(total > 0 ? (ca.totalAmount * 100f / total) : 0);
                statistics.add(stat);
            }

            final long finalTotal = total;
            runOnUiThread(() -> {
                updateChart(statistics);
                adapter.setStatistics(statistics);

                String formattedTotal = NumberFormat.getNumberInstance(Locale.KOREA).format(finalTotal);
                totalAmountText.setText(formattedTotal + "원");
            });
        }).start();
    }

    private String[] getDateRange() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar start = (Calendar) currentCalendar.clone();
        Calendar end = (Calendar) currentCalendar.clone();

        switch (periodType) {
            case "weekly":
                start.set(Calendar.DAY_OF_WEEK, start.getFirstDayOfWeek());
                end.set(Calendar.DAY_OF_WEEK, start.getFirstDayOfWeek());
                end.add(Calendar.DAY_OF_YEAR, 6);
                break;
            case "monthly":
                start.set(Calendar.DAY_OF_MONTH, 1);
                end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));
                break;
            case "yearly":
                start.set(Calendar.DAY_OF_YEAR, 1);
                end.set(Calendar.DAY_OF_YEAR, end.getActualMaximum(Calendar.DAY_OF_YEAR));
                break;
            case "custom":
                return new String[]{customStartDate, customEndDate};
        }

        return new String[]{sdf.format(start.getTime()), sdf.format(end.getTime())};
    }

    private void updatePeriodText(String startDate, String endDate) {
        switch (periodType) {
            case "weekly":
                periodText.setText(String.format("%d년 %d주차",
                        currentCalendar.get(Calendar.YEAR),
                        currentCalendar.get(Calendar.WEEK_OF_YEAR)));
                break;
            case "monthly":
                periodText.setText(String.format("%d년 %d월",
                        currentCalendar.get(Calendar.YEAR),
                        currentCalendar.get(Calendar.MONTH) + 1));
                break;
            case "yearly":
                periodText.setText(String.format("%d년", currentCalendar.get(Calendar.YEAR)));
                break;
            case "custom":
                periodText.setText(String.format("%s ~ %s", startDate, endDate));
                break;
        }
    }

    private void updateChart(List<CategoryStatistics> statistics) {
        if (statistics.isEmpty()) {
            pieChart.clear();
            pieChart.setNoDataText("데이터가 없습니다");
            pieChart.invalidate();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        for (CategoryStatistics stat : statistics) {
            entries.add(new PieEntry(stat.getTotalAmount(), stat.getCategory()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(CHART_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new PercentFormatter(pieChart));

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate();
    }
}
