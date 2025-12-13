package com.example.financetracker.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financetracker.R;
import com.example.financetracker.utils.CategoryManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private CategoryManager categoryManager;

    private TabLayout tabLayout;
    private RecyclerView categoryRecyclerView;
    private TextInputLayout newCategoryInputLayout;
    private TextInputEditText newCategoryEditText;
    private Button addCategoryButton;

    private CategoryAdapter categoryAdapter;
    private boolean isIncomeTab = true; // 현재 수입 탭인지 여부
    private ItemTouchHelper itemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        categoryManager = new CategoryManager(this);

        initViews();
        setupToolbar();
        setupTabs();
        setupRecyclerView();
        setupButtons();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        newCategoryInputLayout = findViewById(R.id.newCategoryInputLayout);
        newCategoryEditText = findViewById(R.id.newCategoryEditText);
        addCategoryButton = findViewById(R.id.addCategoryButton);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isIncomeTab = tab.getPosition() == 0;
                updateCategoryList();
                updateButtonColor();
                updateHint();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // 초기 버튼 색상 및 힌트 설정
        updateButtonColor();
        updateHint();
    }

    private void setupRecyclerView() {
        List<String> categories = new ArrayList<>(categoryManager.getIncomeCategories());
        categoryAdapter = new CategoryAdapter(categories, (category, position) -> {
            showDeleteConfirmDialog(category, position);
        });

        // Set edit listener
        categoryAdapter.setEditListener((category, position) -> {
            showEditCategoryDialog(category, position);
        });

        // Set move listener to save order when categories are reordered
        categoryAdapter.setMoveListener((fromPosition, toPosition) -> {
            saveCategoryOrder();
        });

        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        categoryRecyclerView.setAdapter(categoryAdapter);

        // Setup drag and drop
        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                categoryAdapter.moveItem(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Not used
            }
        };

        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(categoryRecyclerView);
    }

    private void setupButtons() {
        addCategoryButton.setOnClickListener(v -> {
            String newCategory = newCategoryEditText.getText().toString().trim();
            if (!newCategory.isEmpty()) {
                if (isIncomeTab) {
                    categoryManager.addIncomeCategory(newCategory);
                    Toast.makeText(this, "수입 분류가 추가되었습니다", Toast.LENGTH_SHORT).show();
                } else {
                    categoryManager.addExpenseCategory(newCategory);
                    Toast.makeText(this, "지출 분류가 추가되었습니다", Toast.LENGTH_SHORT).show();
                }
                updateCategoryList();
                newCategoryEditText.setText("");
            }
        });
    }

    private void updateCategoryList() {
        List<String> categories;
        if (isIncomeTab) {
            categories = new ArrayList<>(categoryManager.getIncomeCategories());
        } else {
            categories = new ArrayList<>(categoryManager.getExpenseCategories());
        }
        categoryAdapter.setCategories(categories);
    }

    private void updateButtonColor() {
        if (isIncomeTab) {
            addCategoryButton.setBackgroundTintList(getResources().getColorStateList(R.color.income_color, getTheme()));
        } else {
            addCategoryButton.setBackgroundTintList(getResources().getColorStateList(R.color.expense_color, getTheme()));
        }
    }

    private void updateHint() {
        if (isIncomeTab) {
            newCategoryInputLayout.setHint("새 수입 분류 추가");
        } else {
            newCategoryInputLayout.setHint("새 지출 분류 추가");
        }
    }

    private void showEditCategoryDialog(String category, int position) {
        final EditText editText = new EditText(this);
        editText.setText(category);
        editText.setSelection(category.length());
        editText.setPadding(50, 40, 50, 40);

        new AlertDialog.Builder(this)
                .setTitle("분류 수정")
                .setMessage("새로운 분류 이름을 입력하세요")
                .setView(editText)
                .setPositiveButton("수정", (dialog, which) -> {
                    String newCategory = editText.getText().toString().trim();
                    if (!newCategory.isEmpty() && !newCategory.equals(category)) {
                        if (isIncomeTab) {
                            categoryManager.updateIncomeCategory(category, newCategory, () -> {
                                runOnUiThread(() -> {
                                    updateCategoryList();
                                    Toast.makeText(this, "분류가 수정되었습니다\n관련된 거래 내역도 모두 업데이트되었습니다", Toast.LENGTH_LONG).show();
                                });
                            });
                        } else {
                            categoryManager.updateExpenseCategory(category, newCategory, () -> {
                                runOnUiThread(() -> {
                                    updateCategoryList();
                                    Toast.makeText(this, "분류가 수정되었습니다\n관련된 거래 내역도 모두 업데이트되었습니다", Toast.LENGTH_LONG).show();
                                });
                            });
                        }
                    } else if (newCategory.isEmpty()) {
                        Toast.makeText(this, "분류 이름을 입력해주세요", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void showDeleteConfirmDialog(String category, int position) {
        new AlertDialog.Builder(this)
                .setTitle("분류 삭제")
                .setMessage("'" + category + "' 분류를 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> {
                    if (isIncomeTab) {
                        categoryManager.removeIncomeCategory(category);
                    } else {
                        categoryManager.removeExpenseCategory(category);
                    }
                    updateCategoryList();
                    Toast.makeText(this, "분류가 삭제되었습니다", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void saveCategoryOrder() {
        List<String> currentCategories = categoryAdapter.getCategories();
        if (isIncomeTab) {
            categoryManager.setIncomeCategories(currentCategories);
        } else {
            categoryManager.setExpenseCategories(currentCategories);
        }
    }
}
