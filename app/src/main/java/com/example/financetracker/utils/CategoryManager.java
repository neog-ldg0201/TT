package com.example.financetracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CategoryManager {
    private static final String PREFS_NAME = "category_prefs";
    private static final String KEY_INCOME_CATEGORIES = "income_categories";
    private static final String KEY_EXPENSE_CATEGORIES = "expense_categories";

    private static final String[] DEFAULT_INCOME_CATEGORIES = {
            "급여", "용돈", "이자", "부수입", "기타"
    };

    private static final String[] DEFAULT_EXPENSE_CATEGORIES = {
            "식비", "교통", "쇼핑", "여가", "공과금", "의료", "교육", "기타"
    };

    private final SharedPreferences prefs;

    public CategoryManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        initializeDefaultCategories();
    }

    private void initializeDefaultCategories() {
        if (!prefs.contains(KEY_INCOME_CATEGORIES)) {
            setIncomeCategories(new ArrayList<>(Arrays.asList(DEFAULT_INCOME_CATEGORIES)));
        }
        if (!prefs.contains(KEY_EXPENSE_CATEGORIES)) {
            setExpenseCategories(new ArrayList<>(Arrays.asList(DEFAULT_EXPENSE_CATEGORIES)));
        }
    }

    public List<String> getIncomeCategories() {
        Set<String> set = prefs.getStringSet(KEY_INCOME_CATEGORIES, null);
        if (set == null) {
            return new ArrayList<>(Arrays.asList(DEFAULT_INCOME_CATEGORIES));
        }
        return new ArrayList<>(set);
    }

    public List<String> getExpenseCategories() {
        Set<String> set = prefs.getStringSet(KEY_EXPENSE_CATEGORIES, null);
        if (set == null) {
            return new ArrayList<>(Arrays.asList(DEFAULT_EXPENSE_CATEGORIES));
        }
        return new ArrayList<>(set);
    }

    public void setIncomeCategories(List<String> categories) {
        prefs.edit().putStringSet(KEY_INCOME_CATEGORIES, new HashSet<>(categories)).apply();
    }

    public void setExpenseCategories(List<String> categories) {
        prefs.edit().putStringSet(KEY_EXPENSE_CATEGORIES, new HashSet<>(categories)).apply();
    }

    public void addIncomeCategory(String category) {
        List<String> categories = getIncomeCategories();
        if (!categories.contains(category)) {
            categories.add(category);
            setIncomeCategories(categories);
        }
    }

    public void addExpenseCategory(String category) {
        List<String> categories = getExpenseCategories();
        if (!categories.contains(category)) {
            categories.add(category);
            setExpenseCategories(categories);
        }
    }

    public void removeIncomeCategory(String category) {
        List<String> categories = getIncomeCategories();
        categories.remove(category);
        setIncomeCategories(categories);
    }

    public void removeExpenseCategory(String category) {
        List<String> categories = getExpenseCategories();
        categories.remove(category);
        setExpenseCategories(categories);
    }

    public List<String> getCategoriesForType(String type) {
        if ("INCOME".equals(type)) {
            return getIncomeCategories();
        } else {
            return getExpenseCategories();
        }
    }
}
