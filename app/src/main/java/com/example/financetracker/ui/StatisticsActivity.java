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
    private LinearLayout periodStatsLayout;

    private Button weeklyButton;
    private Button monthlyButton;
    private Button yearlyButton;
    private Button customButton;
    private ImageButton previousPeriodButton;
    private ImageButton nextPeriodButton;
    private TabLayout typeTabLayout;
    private TabLayout mainTabLayout;

    private TransactionDao transactionDao;

    private String periodType = "monthly"; // weekly, monthly, yearly, custom
    private String transactionType = "EXPENSE"; // INCOME or EXPENSE
    private String mainTab = "category"; // category or period
    private Calendar currentCalendar;
    private String customStartDate;
    private String customEndDate;

    private static final int[] CHART_COLORS = {
            Color.rgb(255, 182, 193),   // 연한 핑크
            Color.rgb(255, 200, 170),   // 연한 오렌지/피치
            Color.rgb(255, 240, 180),   // 연한 노란색
            Color.rgb(200, 240, 200),   // 연한 초록
            Color.rgb(180, 235, 220),   // 연한 민트
            Color.rgb(190, 220, 255),   // 연한 하늘색
            Color.rgb(210, 190, 255),   // 연한 보라
            Color.rgb(230, 200, 255),   // 연한 라벤더
            Color.rgb(255, 210, 200),   // 연한 복숭아
            Color.rgb(220, 220, 220),   // 연한 회색
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
        periodStatsLayout = findViewById(R.id.periodStatsLayout);

        weeklyButton = findViewById(R.id.weeklyButton);
        monthlyButton = findViewById(R.id.monthlyButton);
        yearlyButton = findViewById(R.id.yearlyButton);
        customButton = findViewById(R.id.customButton);
        previousPeriodButton = findViewById(R.id.previousPeriodButton);
        nextPeriodButton = findViewById(R.id.nextPeriodButton);
        typeTabLayout = findViewById(R.id.typeTabLayout);
        mainTabLayout = findViewById(R.id.mainTabLayout);
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

        mainTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mainTab = tab.getPosition() == 0 ? "category" : "period";
                updateTabVisibility();
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

    private void updateTabVisibility() {
        if (mainTab.equals("category")) {
            categoryStatsLayout.setVisibility(View.VISIBLE);
            periodStatsLayout.setVisibility(View.GONE);
        } else {
            categoryStatsLayout.setVisibility(View.GONE);
            periodStatsLayout.setVisibility(View.VISIBLE);
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
        if (mainTab.equals("category")) {
            updateCategoryStatistics();
        } else {
            updatePeriodStatistics();
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

    private void updatePeriodStatistics() {
        String[] dateRange = getDateRange();
        String startDate = dateRange[0];
        String endDate = dateRange[1];

        updatePeriodText(startDate, endDate);

        new Thread(() -> {
            List<String> categories = new ArrayList<>();
            List<String> periods = new ArrayList<>();
            java.util.Map<String, java.util.Map<String, Long>> data = new java.util.LinkedHashMap<>();
            long total = 0;

            if (periodType.equals("yearly")) {
                // 연간: 월별 데이터
                List<TransactionDao.MonthlyCategoryAmount> monthlyData =
                        transactionDao.getMonthlyCategoryStatistics(
                                transactionType,
                                String.valueOf(currentCalendar.get(Calendar.YEAR))
                        );

                // 1~12월 기간 생성
                for (int month = 1; month <= 12; month++) {
                    String monthStr = String.format("%04d-%02d",
                            currentCalendar.get(Calendar.YEAR), month);
                    periods.add(monthStr);
                    data.put(monthStr, new java.util.HashMap<>());
                }

                // 카테고리 추출 및 데이터 매핑
                for (TransactionDao.MonthlyCategoryAmount ma : monthlyData) {
                    if (!categories.contains(ma.category)) {
                        categories.add(ma.category);
                    }
                    data.get(ma.month).put(ma.category, ma.totalAmount);
                    total += ma.totalAmount;
                }
            } else {
                // 주간/월간: 일별 데이터
                List<TransactionDao.DailyCategoryAmount> dailyData =
                        transactionDao.getDailyCategoryStatistics(
                                transactionType, startDate, endDate
                        );

                // 날짜 범위 생성
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                try {
                    cal.setTime(sdf.parse(startDate));
                    Calendar endCal = Calendar.getInstance();
                    endCal.setTime(sdf.parse(endDate));

                    while (!cal.after(endCal)) {
                        String dateStr = sdf.format(cal.getTime());
                        periods.add(dateStr);
                        data.put(dateStr, new java.util.HashMap<>());
                        cal.add(Calendar.DAY_OF_MONTH, 1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 카테고리 추출 및 데이터 매핑
                for (TransactionDao.DailyCategoryAmount da : dailyData) {
                    if (!categories.contains(da.category)) {
                        categories.add(da.category);
                    }
                    data.get(da.date).put(da.category, da.totalAmount);
                    total += da.totalAmount;
                }
            }

            final long finalTotal = total;
            runOnUiThread(() -> {
                updateStackedBarChart(periods, categories, data);

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
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueFormatter(new PercentFormatter(pieChart));

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate();
    }

    private void updateStackedBarChart(List<String> periods, List<String> categories,
                                       java.util.Map<String, java.util.Map<String, Long>> data) {
        if (categories.isEmpty() || periods.isEmpty()) {
            barChart.clear();
            barChart.setNoDataText("데이터가 없습니다");
            barChart.invalidate();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        // 각 기간별로 BarEntry 생성 (stacked)
        for (int i = 0; i < periods.size(); i++) {
            String period = periods.get(i);
            java.util.Map<String, Long> periodData = data.get(period);

            // 카테고리별 값을 배열로 구성
            float[] values = new float[categories.size()];
            for (int j = 0; j < categories.size(); j++) {
                String category = categories.get(j);
                Long amount = periodData.get(category);
                values[j] = amount != null ? amount : 0;
            }

            entries.add(new BarEntry(i, values));

            // 라벨 생성 (날짜 또는 월)
            if (periodType.equals("yearly")) {
                // 월만 표시 (1월, 2월, ...)
                String month = period.substring(5, 7);
                labels.add(month + "월");
            } else {
                // 날짜 표시 (MM/DD)
                String monthDay = period.substring(5);
                labels.add(monthDay);
            }
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(CHART_COLORS);
        dataSet.setStackLabels(categories.toArray(new String[0]));

        dataSet.setValueTextSize(9f);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value == 0) return "";
                if (value < 1000) return "";
                return NumberFormat.getNumberInstance(Locale.KOREA).format((long) value);
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.85f);

        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setLabelCount(Math.min(labels.size(), 12));
        barChart.getXAxis().setLabelRotationAngle(-45f);
        barChart.invalidate();
    }
}
