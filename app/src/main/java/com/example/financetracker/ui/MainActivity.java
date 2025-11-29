package com.example.financetracker.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financetracker.R;
import com.example.financetracker.model.Transaction;
import com.example.financetracker.service.TossNotificationListenerService;
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

    private AlertDialog permissionDialog;
    private ActivityResultLauncher<String> notificationPermissionLauncher;

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

        // Initialize permission launcher
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    checkAndShowPermissionDialog();
                }
        );

        initViews();
        setupToolbar();
        setupViewModel();
        setupCalendar();
        setupFab();
        updateMonthlySummary();

        // Check permissions on start
        checkAndShowPermissionDialog();
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

        // Setup click listener for editing transaction
        recentTransactionsAdapter.setOnTransactionClickListener(transaction -> {
            Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
            intent.putExtra(AddTransactionActivity.EXTRA_EDIT_MODE, true);
            intent.putExtra(AddTransactionActivity.EXTRA_TRANSACTION_ID, transaction.getId());
            startActivity(intent);
        });

        // Setup long click listener for deleting transaction
        recentTransactionsAdapter.setOnTransactionLongClickListener(transaction -> {
            showDeleteConfirmationDialog(transaction);
        });

        // Initialize current year-month
        CalendarDay today = CalendarDay.today();
        currentYearMonth = String.format(Locale.US, "%04d-%02d", today.getYear(), today.getMonth());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        // Re-check permissions when returning to the app
        checkAndShowPermissionDialog();
    }

    // ==================== Permission Handling ====================

    private void checkAndShowPermissionDialog() {
        boolean hasNotificationPermission = hasNotificationPermission();
        boolean hasNotificationListenerPermission = isNotificationListenerEnabled();

        if (!hasNotificationPermission || !hasNotificationListenerPermission) {
            showPermissionDialog(hasNotificationPermission, hasNotificationListenerPermission);
        } else {
            dismissPermissionDialog();
        }
    }

    private boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Pre-Android 13 doesn't need this permission
    }

    private boolean isNotificationListenerEnabled() {
        ComponentName componentName = new ComponentName(this, TossNotificationListenerService.class);
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        return flat != null && flat.contains(componentName.flattenToString());
    }

    private void showPermissionDialog(boolean hasNotificationPermission, boolean hasNotificationListenerPermission) {
        // Dismiss any existing dialog
        dismissPermissionDialog();

        StringBuilder message = new StringBuilder();
        message.append("앱을 사용하려면 다음 권한이 필요합니다:\n\n");

        if (!hasNotificationPermission) {
            message.append("• 알림 권한: 거래 등록 알림을 보내기 위해 필요합니다.\n\n");
        }

        if (!hasNotificationListenerPermission) {
            message.append("• 알림 접근 권한: 토스 결제 알림을 읽어오기 위해 필요합니다.\n\n");
        }

        message.append("모든 권한을 허용해주세요.");

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("권한 필요")
                .setMessage(message.toString())
                .setCancelable(false);

        // Add button for notification permission (Android 13+)
        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            builder.setPositiveButton("알림 권한 허용", (dialog, which) -> {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            });
        }

        // Add button for notification listener permission
        if (!hasNotificationListenerPermission) {
            String buttonText = hasNotificationPermission ? "알림 접근 설정" : "알림 접근 설정";
            if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                builder.setNegativeButton(buttonText, (dialog, which) -> {
                    openNotificationListenerSettings();
                });
            } else {
                builder.setPositiveButton(buttonText, (dialog, which) -> {
                    openNotificationListenerSettings();
                });
            }
        }

        // Add neutral button to check again
        builder.setNeutralButton("다시 확인", (dialog, which) -> {
            checkAndShowPermissionDialog();
        });

        permissionDialog = builder.create();
        permissionDialog.show();
    }

    private void dismissPermissionDialog() {
        if (permissionDialog != null && permissionDialog.isShowing()) {
            permissionDialog.dismiss();
            permissionDialog = null;
        }
    }

    private void openNotificationListenerSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
            Toast.makeText(this, "'" + getString(R.string.app_name) + "'을(를) 찾아 활성화해주세요", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "설정을 열 수 없습니다. 직접 설정에서 알림 접근 권한을 허용해주세요.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissPermissionDialog();
    }

    private void showDeleteConfirmationDialog(Transaction transaction) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.KOREA);
        String amount = formatter.format(transaction.getAmount()) + "원";
        String type = "INCOME".equals(transaction.getType()) ? "수입" : "지출";

        String message = String.format("다음 거래를 삭제하시겠습니까?\n\n[%s] %s\n%s - %s",
                type, amount, transaction.getCategory(), transaction.getDescription());

        new AlertDialog.Builder(this)
                .setTitle("거래 삭제")
                .setMessage(message)
                .setPositiveButton("삭제", (dialog, which) -> {
                    viewModel.delete(transaction);
                    Toast.makeText(this, "거래가 삭제되었습니다", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("취소", null)
                .show();
    }
}
