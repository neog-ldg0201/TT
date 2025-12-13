package com.example.financetracker.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.financetracker.R;
import com.google.android.material.card.MaterialCardView;

public class SettingsMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_menu);

        setupToolbar();
        setupCards();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupCards() {
        MaterialCardView categorySettingsCard = findViewById(R.id.categorySettingsCard);
        MaterialCardView dataManagementCard = findViewById(R.id.dataManagementCard);
        MaterialCardView themeSettingsCard = findViewById(R.id.themeSettingsCard);

        categorySettingsCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        dataManagementCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, DataManagementActivity.class);
            startActivity(intent);
        });

        themeSettingsCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, ThemeSettingsActivity.class);
            startActivity(intent);
        });
    }
}
