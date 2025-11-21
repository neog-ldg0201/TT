package com.example.financetracker.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.financetracker.R;
import com.example.financetracker.model.Transaction;
import com.example.financetracker.utils.DateUtils;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class AddTransactionActivity extends AppCompatActivity {

    private RadioGroup typeRadioGroup;
    private TextInputEditText amountEditText;
    private Spinner categorySpinner;
    private TextInputEditText descriptionEditText;
    private TextInputEditText dateEditText;
    private TextInputEditText timeEditText;
    private Button saveButton;
    private Button cancelButton;

    private AddTransactionViewModel viewModel;
    private String selectedDate;
    private String selectedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        initViews();
        setupViewModel();
        setupListeners();
        setupSpinner();
        initializeDateTime();

        // Check if coming from notification
        String notificationAmount = getIntent().getStringExtra("amount");
        String notificationDescription = getIntent().getStringExtra("description");
        String notificationDate = getIntent().getStringExtra("date");
        String notificationTime = getIntent().getStringExtra("time");

        if (notificationAmount != null) {
            amountEditText.setText(notificationAmount);
        }
        if (notificationDescription != null) {
            descriptionEditText.setText(notificationDescription);
        }
        if (notificationDate != null) {
            selectedDate = notificationDate;
            dateEditText.setText(DateUtils.formatDateKorean(notificationDate));
        }
        if (notificationTime != null) {
            selectedTime = notificationTime;
            timeEditText.setText(notificationTime);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("거래 추가");
        }
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
        cancelButton.setOnClickListener(v -> finish());
    }

    private void setupSpinner() {
        String[] categories = {
            getString(R.string.category_food),
            getString(R.string.category_transport),
            getString(R.string.category_shopping),
            getString(R.string.category_entertainment),
            getString(R.string.category_bills),
            getString(R.string.category_salary),
            getString(R.string.category_etc)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
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

        long amount = Long.parseLong(amountStr);
        String type = typeRadioGroup.getCheckedRadioButtonId() == R.id.incomeRadio ? "INCOME" : "EXPENSE";
        String category = categorySpinner.getSelectedItem().toString();
        String description = descriptionEditText.getText().toString().trim();
        boolean isFromNotification = getIntent().getBooleanExtra("isFromNotification", false);

        Transaction transaction = new Transaction(
                type,
                amount,
                category,
                description,
                selectedDate,
                selectedTime,
                isFromNotification
        );

        viewModel.insert(transaction);
        Toast.makeText(this, R.string.transaction_added, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
