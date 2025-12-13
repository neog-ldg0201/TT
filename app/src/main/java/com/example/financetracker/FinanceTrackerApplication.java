package com.example.financetracker;

import android.app.Application;

import com.example.financetracker.utils.ThemeManager;

public class FinanceTrackerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Apply saved theme on app start
        ThemeManager themeManager = new ThemeManager(this);
        themeManager.applyTheme();
    }
}
