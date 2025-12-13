package com.example.financetracker.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financetracker.R;
import com.example.financetracker.database.AppDatabase;
import com.example.financetracker.database.TransactionDao;
import com.example.financetracker.model.CategoryStatistics;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.tabs.TabLayout;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class StatisticsActivity extends AppCompatActivity {

    private TextView periodText;
    private TextView totalTypeText;
    private TextView totalAmountText;
    private PieChart pieChart;
    private BarChart barChart;
    private RecyclerView statisticsRecyclerView;
    private CategoryStatisticsAdapter adapter;
    private LinearLayout categoryStatsLayout;
    private LinearLayout trendStatsLayout;

    private Button weeklyButton;
    private Button monthlyButton;
    private Button yearlyButton;
    private Button customButton;
    private ImageButton previousPeriodButton;
    private ImageButton nextPeriodButton;
    private TabLayout typeTabLayout;
    private TabLayout chartTypeTabLayout;

    private TransactionDao transactionDao;

    private String periodType = "monthly"; // weekly, monthly, yearly, custom
    private String transactionType = "EXPENSE"; // INCOME or EXPENSE
    private String chartType = "category"; // category or trend
    private Calendar currentCalendar;
    private String customStartDate;
    private String customEndDate;

    private static final int[] CHART_COLORS = {
            Color.rgb(244, 67, 54),     // Red (Material)
            Color.rgb(255, 87, 34),     // Deep Orange
            Color.rgb(121, 85, 72),     // Brown (노란색 대체)
            Color.rgb(76, 175, 80),     // Green
            Color.rgb(0, 150, 136),     // Teal
            Color.rgb(33, 150, 243),    // Blue
            Color.rgb(63, 81, 181),     // Indigo
            Color.rgb(156, 39, 176),    // Purple
            Color.rgb(233, 30, 99),     // Pink
            Color.rgb(96, 125, 139),    // Blue Grey
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
        setupBarChart();
        setupRecyclerView();
        setupListeners();
        updateTypeText();
        updateStatistics();
    }

    private void initViews() {
        periodText = findViewById(R.id.periodText);
        totalTypeText = findViewById(R.id.totalTypeText);
        totalAmountText = findViewById(R.id.totalAmountText);
        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);
        statisticsRecyclerView = findViewById(R.id.statisticsRecyclerView);
        categoryStatsLayout = findViewById(R.id.categoryStatsLayout);
        trendStatsLayout = findViewById(R.id.trendStatsLayout);

        weeklyButton = findViewById(R.id.weeklyButton);
        monthlyButton = findViewById(R.id.monthlyButton);
        yearlyButton = findViewById(R.id.yearlyButton);
        customButton = findViewById(R.id.customButton);
        previousPeriodButton = findViewById(R.id.previousPeriodButton);
        nextPeriodButton = findViewById(R.id.nextPeriodButton);
        typeTabLayout = findViewById(R.id.typeTabLayout);
        chartTypeTabLayout = findViewById(R.id.chartTypeTabLayout);
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

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setDoubleTapToZoomEnabled(false);

        Legend legend = barChart.getLegend();
        legend.setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
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

        chartTypeTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                chartType = tab.getPosition() == 0 ? "category" : "trend";
                updateChartVisibility();
                updateStatistics();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        typeTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                transactionType = tab.getPosition() == 0 ? "EXPENSE" : "INCOME";
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

    private void updateChartVisibility() {
        if (chartType.equals("category")) {
            categoryStatsLayout.setVisibility(View.VISIBLE);
            trendStatsLayout.setVisibility(View.GONE);
        } else {
            categoryStatsLayout.setVisibility(View.GONE);
            trendStatsLayout.setVisibility(View.VISIBLE);
        }
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
        MaterialDatePicker<Pair<Long, Long>> dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("기간 선택")
                .setSelection(
                        new Pair<>(
                                MaterialDatePicker.todayInUtcMilliseconds(),
                                MaterialDatePicker.todayInUtcMilliseconds()
                        )
                )
                .build();

        dateRangePicker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null && selection.first != null && selection.second != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                customStartDate = sdf.format(selection.first);
                customEndDate = sdf.format(selection.second);

                periodType = "custom";
                updateButtonStates();
                updateStatistics();
            }
        });

        dateRangePicker.show(getSupportFragmentManager(), "DATE_RANGE_PICKER");
    }

    private void updateStatistics() {
        if (chartType.equals("category")) {
            updateCategoryStatistics();
        } else {
            updateTrendStatistics();
        }
    }

    private void updateCategoryStatistics() {
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

    private void updateTrendStatistics() {
        String[] dateRange = getDateRange();
        String startDate = dateRange[0];
        String endDate = dateRange[1];

        updatePeriodText(startDate, endDate);

        new Thread(() -> {
            List<TransactionDao.CategoryAmount> dailyAmounts = transactionType.equals("EXPENSE") ?
                    transactionDao.getExpenseCategoryStatistics(startDate, endDate) :
                    transactionDao.getIncomeCategoryStatistics(startDate, endDate);

            long total = 0;
            for (TransactionDao.CategoryAmount ca : dailyAmounts) {
                total += ca.totalAmount;
            }

            final long finalTotal = total;
            runOnUiThread(() -> {
                updateBarChart(dailyAmounts);

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
                if (customStartDate != null && customEndDate != null) {
                    return new String[]{customStartDate, customEndDate};
                }
                start.set(Calendar.DAY_OF_MONTH, 1);
                end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH));
                break;
        }

        return new String[]{sdf.format(start.getTime()), sdf.format(end.getTime())};
    }

    private void updatePeriodText(String startDate, String endDate) {
        switch (periodType) {
            case "weekly":
                String formattedStart = startDate.replace("-", ".");
                String formattedEnd = endDate.replace("-", ".");
                periodText.setText(String.format("%s ~ %s", formattedStart, formattedEnd));
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
                if (startDate != null && endDate != null) {
                    periodText.setText(String.format("%s ~ %s", startDate, endDate));
                } else {
                    periodText.setText("기타");
                }
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

    private void updateBarChart(List<TransactionDao.CategoryAmount> categoryAmounts) {
        if (categoryAmounts.isEmpty()) {
            barChart.clear();
            barChart.setNoDataText("데이터가 없습니다");
            barChart.invalidate();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < categoryAmounts.size(); i++) {
            TransactionDao.CategoryAmount ca = categoryAmounts.get(i);
            entries.add(new BarEntry(i, ca.totalAmount));
            labels.add(ca.category);
        }

        BarDataSet dataSet = new BarDataSet(entries, "");

        // 분류별로 다른 색상 적용
        List<Integer> colors = new ArrayList<>();
        for (int i = 0; i < categoryAmounts.size(); i++) {
            colors.add(CHART_COLORS[i % CHART_COLORS.length]);
        }
        dataSet.setColors(colors);

        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return NumberFormat.getNumberInstance(Locale.KOREA).format((long) value);
            }
        });

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.8f);

        barChart.setData(data);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setLabelCount(labels.size());
        barChart.invalidate();
    }
}
