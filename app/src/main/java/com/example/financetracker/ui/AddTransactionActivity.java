package com.example.financetracker.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.financetracker.R;
import com.example.financetracker.model.Transaction;
import com.example.financetracker.utils.CategoryManager;
import com.example.financetracker.utils.DateUtils;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddTransactionActivity extends AppCompatActivity {

    public static final String EXTRA_TRANSACTION_ID = "transaction_id";
    public static final String EXTRA_EDIT_MODE = "edit_mode";

    private RadioGroup typeRadioGroup;
    private TextInputEditText amountEditText;
    private Spinner categorySpinner;
    private TextInputEditText descriptionEditText;
    private TextInputEditText dateEditText;
    private TextInputEditText timeEditText;
    private Button saveButton;
    private Button cancelButton;

    private AddTransactionViewModel viewModel;
    private CategoryManager categoryManager;
    private String selectedDate;
    private String selectedTime;
    private String selectedCategory = "";
    private boolean isFromNotification;
    private boolean isEditMode;
    private int editTransactionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        categoryManager = new CategoryManager(this);

        initViews();
        setupViewModel();
        setupListeners();
        setupCategorySpinner();
        initializeDateTime();

        // Check if edit mode
        isEditMode = getIntent().getBooleanExtra(EXTRA_EDIT_MODE, false);
        editTransactionId = getIntent().getIntExtra(EXTRA_TRANSACTION_ID, -1);

        // Check if coming from notification
        isFromNotification = getIntent().getBooleanExtra("isFromNotification", false);

        // Handle notification data
        handleNotificationData();

        // Handle edit mode
        if (isEditMode && editTransactionId != -1) {
            loadTransactionForEdit();
        }

        setupActionBar();
    }

    private void initViews() {
        typeRadioGroup = findViewById(R.id.typeRadioGroup);
        amountEditText = findViewById(R.id.amountEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        dateEditText = findViewById(R.id.dateEditText);
        timeEditText = findViewById(R.id.timeEditText);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(AddTransactionViewModel.class);
    }

    private void setupListeners() {
        dateEditText.setOnClickListener(v -> showDatePicker());
        timeEditText.setOnClickListener(v -> showTimePicker());
        saveButton.setOnClickListener(v -> saveTransaction());
        cancelButton.setOnClickListener(v -> navigateBack());

        // Listen to type changes to update category list
        typeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            setupCategorySpinner();
        });

        // Listen to category selection
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategory = "";
            }
        });

        // Add thousand separator to amount input
        amountEditText.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) {
                    return;
                }

                isFormatting = true;

                String input = s.toString();
                if (!input.equals(current)) {
                    // Remove all commas
                    String cleanString = input.replaceAll("[,]", "");

                    if (cleanString.isEmpty()) {
                        current = "";
                        isFormatting = false;
                        return;
                    }

                    try {
                        // Parse and format with commas
                        long parsed = Long.parseLong(cleanString);
                        String formatted = String.format("%,d", parsed);

                        current = formatted;
                        amountEditText.setText(formatted);
                        amountEditText.setSelection(formatted.length());
                    } catch (NumberFormatException e) {
                        // If parsing fails, keep the text as is
                    }
                }

                isFormatting = false;
            }
        });
    }

    private String getCurrentType() {
        return typeRadioGroup.getCheckedRadioButtonId() == R.id.incomeRadio ? "INCOME" : "EXPENSE";
    }

    private void setupCategorySpinner() {
        String type = getCurrentType();
        List<String> categories = categoryManager.getCategoriesForType(type);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                categories
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // Reset selection
        if (!categories.isEmpty()) {
            categorySpinner.setSelection(0);
            selectedCategory = categories.get(0);
        } else {
            selectedCategory = "";
        }
    }

    private void handleNotificationData() {
        String notificationAmount = getIntent().getStringExtra("amount");
        String notificationDescription = getIntent().getStringExtra("description");
        String notificationType = getIntent().getStringExtra("type");
        String notificationDate = getIntent().getStringExtra("date");
        String notificationTime = getIntent().getStringExtra("time");

        if (notificationAmount != null) {
            try {
                long amount = Long.parseLong(notificationAmount);
                amountEditText.setText(String.format("%,d", amount));
            } catch (NumberFormatException e) {
                amountEditText.setText(notificationAmount);
            }
        }
        if (notificationDescription != null) {
            descriptionEditText.setText(notificationDescription);
        }
        if (notificationType != null) {
            if ("INCOME".equals(notificationType)) {
                typeRadioGroup.check(R.id.incomeRadio);
            } else {
                typeRadioGroup.check(R.id.expenseRadio);
            }
        }
        if (notificationDate != null) {
            selectedDate = notificationDate;
            dateEditText.setText(DateUtils.formatDateKorean(notificationDate));
        }
        if (notificationTime != null) {
            selectedTime = notificationTime;
            timeEditText.setText(notificationTime);
        }
    }

    private void loadTransactionForEdit() {
        viewModel.getTransactionById(editTransactionId, transaction -> {
            if (transaction != null) {
                runOnUiThread(() -> {
                    amountEditText.setText(String.format("%,d", transaction.getAmount()));
                    descriptionEditText.setText(transaction.getDescription());

                    if ("INCOME".equals(transaction.getType())) {
                        typeRadioGroup.check(R.id.incomeRadio);
                    } else {
                        typeRadioGroup.check(R.id.expenseRadio);
                    }

                    // Edit 모드에서는 알림 플래그 해제
                    isFromNotification = false;

                    // Set category selection
                    selectedCategory = transaction.getCategory();
                    setupCategorySpinner();

                    // Find and select the category in spinner
                    String type = getCurrentType();
                    List<String> categories = categoryManager.getCategoriesForType(type);
                    int categoryIndex = categories.indexOf(selectedCategory);
                    if (categoryIndex >= 0) {
                        categorySpinner.setSelection(categoryIndex);
                    }

                    selectedDate = transaction.getDate();
                    selectedTime = transaction.getTime();
                    dateEditText.setText(DateUtils.formatDateKorean(selectedDate));
                    timeEditText.setText(selectedTime);
                });
            }
        });
    }

    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? "거래 수정" : "거래 추가");
        }
    }

    private void initializeDateTime() {
        selectedDate = DateUtils.getCurrentDate();
        selectedTime = DateUtils.getCurrentTime();
        dateEditText.setText(DateUtils.formatDateKorean(selectedDate));
        timeEditText.setText(selectedTime);
    }

    private void showDatePicker() {
        Calendar calendar = DateUtils.stringToCalendar(selectedDate);

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate = DateUtils.formatDate(year, month + 1, dayOfMonth);
                    dateEditText.setText(DateUtils.formatDateKorean(selectedDate));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showTimePicker() {
        String[] timeParts = selectedTime.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        TimePickerDialog dialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    selectedTime = DateUtils.formatTime(hourOfDay, minuteOfHour);
                    timeEditText.setText(selectedTime);
                },
                hour, minute, true);
        dialog.show();
    }

    private void saveTransaction() {
        String amountStr = amountEditText.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, R.string.invalid_amount, Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCategory.isEmpty()) {
            Toast.makeText(this, "분류를 선택해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        // Remove commas before parsing
        String cleanAmountStr = amountStr.replaceAll("[,]", "");
        long amount = Long.parseLong(cleanAmountStr);
        String type = getCurrentType();
        String description = descriptionEditText.getText().toString().trim();

        if (isEditMode && editTransactionId != -1) {
            // Update existing transaction
            Transaction transaction = new Transaction(
                    type, amount, selectedCategory, description,
                    selectedDate, selectedTime, false
            );
            transaction.setId(editTransactionId);
            viewModel.update(transaction);
            Toast.makeText(this, R.string.transaction_updated, Toast.LENGTH_SHORT).show();
        } else {
            // Insert new transaction
            Transaction transaction = new Transaction(
                    type, amount, selectedCategory, description,
                    selectedDate, selectedTime, isFromNotification
            );
            viewModel.insert(transaction);
            Toast.makeText(this, R.string.transaction_added, Toast.LENGTH_SHORT).show();
        }

        navigateBack();
    }

    private void navigateBack() {
        if (isFromNotification) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        navigateBack();
        return true;
    }

    @Override
    public void onBackPressed() {
        navigateBack();
    }
}
