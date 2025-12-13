package com.example.financetracker.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.financetracker.R;
import com.example.financetracker.utils.ThemeManager;

public class ThemeSettingsActivity extends AppCompatActivity {

    private ThemeManager themeManager;
    private Button changeThemeButton;
    private TextView currentThemeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_settings);

        themeManager = new ThemeManager(this);

        setupToolbar();
        initViews();
        setupButtons();
        updateThemeDisplay();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        changeThemeButton = findViewById(R.id.changeThemeButton);
        currentThemeText = findViewById(R.id.currentThemeText);
    }

    private void setupButtons() {
        changeThemeButton.setOnClickListener(v -> showThemeSelectionDialog());
    }

    private void showThemeSelectionDialog() {
        String[] themes = {"시스템 설정", "라이트 모드", "다크 모드"};
        int currentTheme = themeManager.getThemeMode();

        new AlertDialog.Builder(this)
                .setTitle("테마 선택")
                .setSingleChoiceItems(themes, currentTheme, (dialog, which) -> {
                    themeManager.setThemeMode(which);
                    updateThemeDisplay();
                    dialog.dismiss();
                    recreate();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void updateThemeDisplay() {
        int currentTheme = themeManager.getThemeMode();
        String themeName = themeManager.getThemeModeName(currentTheme);
        currentThemeText.setText("현재 테마: " + themeName);
    }
}
