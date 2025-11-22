package com.example.financetracker.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financetracker.R;
import com.example.financetracker.utils.CategoryManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private CategoryManager categoryManager;

    private RecyclerView incomeCategoryRecyclerView;
    private RecyclerView expenseCategoryRecyclerView;
    private TextInputEditText newIncomeCategoryEditText;
    private TextInputEditText newExpenseCategoryEditText;
    private Button addIncomeCategoryButton;
    private Button addExpenseCategoryButton;

    private CategoryAdapter incomeAdapter;
    private CategoryAdapter expenseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        categoryManager = new CategoryManager(this);

        initViews();
        setupToolbar();
        setupRecyclerViews();
        setupButtons();
    }

    private void initViews() {
        incomeCategoryRecyclerView = findViewById(R.id.incomeCategoryRecyclerView);
        expenseCategoryRecyclerView = findViewById(R.id.expenseCategoryRecyclerView);
        newIncomeCategoryEditText = findViewById(R.id.newIncomeCategoryEditText);
        newExpenseCategoryEditText = findViewById(R.id.newExpenseCategoryEditText);
        addIncomeCategoryButton = findViewById(R.id.addIncomeCategoryButton);
        addExpenseCategoryButton = findViewById(R.id.addExpenseCategoryButton);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerViews() {
        // Income categories
        List<String> incomeCategories = new ArrayList<>(categoryManager.getIncomeCategories());
        incomeAdapter = new CategoryAdapter(incomeCategories, (category, position) -> {
            showDeleteConfirmDialog(category, position, true);
        });
        incomeCategoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        incomeCategoryRecyclerView.setAdapter(incomeAdapter);

        // Expense categories
        List<String> expenseCategories = new ArrayList<>(categoryManager.getExpenseCategories());
        expenseAdapter = new CategoryAdapter(expenseCategories, (category, position) -> {
            showDeleteConfirmDialog(category, position, false);
        });
        expenseCategoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        expenseCategoryRecyclerView.setAdapter(expenseAdapter);
    }

    private void setupButtons() {
        addIncomeCategoryButton.setOnClickListener(v -> {
            String newCategory = newIncomeCategoryEditText.getText().toString().trim();
            if (!newCategory.isEmpty()) {
                categoryManager.addIncomeCategory(newCategory);
                incomeAdapter.setCategories(new ArrayList<>(categoryManager.getIncomeCategories()));
                newIncomeCategoryEditText.setText("");
                Toast.makeText(this, "수입 분류가 추가되었습니다", Toast.LENGTH_SHORT).show();
            }
        });

        addExpenseCategoryButton.setOnClickListener(v -> {
            String newCategory = newExpenseCategoryEditText.getText().toString().trim();
            if (!newCategory.isEmpty()) {
                categoryManager.addExpenseCategory(newCategory);
                expenseAdapter.setCategories(new ArrayList<>(categoryManager.getExpenseCategories()));
                newExpenseCategoryEditText.setText("");
                Toast.makeText(this, "지출 분류가 추가되었습니다", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmDialog(String category, int position, boolean isIncome) {
        new AlertDialog.Builder(this)
                .setTitle("분류 삭제")
                .setMessage("'" + category + "' 분류를 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> {
                    if (isIncome) {
                        categoryManager.removeIncomeCategory(category);
                        incomeAdapter.setCategories(new ArrayList<>(categoryManager.getIncomeCategories()));
                    } else {
                        categoryManager.removeExpenseCategory(category);
                        expenseAdapter.setCategories(new ArrayList<>(categoryManager.getExpenseCategories()));
                    }
                    Toast.makeText(this, "분류가 삭제되었습니다", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("취소", null)
                .show();
    }
}
