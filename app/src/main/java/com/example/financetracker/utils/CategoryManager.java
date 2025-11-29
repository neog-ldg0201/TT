package com.example.financetracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class CategoryManager {
    private static final String PREFS_NAME = "category_prefs";
    private static final String KEY_INCOME_CATEGORIES = "income_categories_ordered";
    private static final String KEY_EXPENSE_CATEGORIES = "expense_categories_ordered";
    private static final String KEY_CATEGORY_VERSION = "category_version";
    private static final int CURRENT_CATEGORY_VERSION = 4; // 카테고리 업데이트 버전 (split 정규식 수정)
    private static final String DELIMITER = "|||"; // 카테고리 구분자

    private static final String[] DEFAULT_INCOME_CATEGORIES = {
            "💰 월급", "💵 부수입", "🤑 용돈", "🏅 상여", "🏦 금융소득", "기타"
    };

    private static final String[] DEFAULT_EXPENSE_CATEGORIES = {
            "🍜 식비", "✈️ 여행", "🚖 교통/차량", "🖼 문화생활",
            "🛒 마트/편의점", "🧥 패션/미용", "📌 고정지출", "🏠 주거/통신",
            "📙 교육", "🎁 경조사/회비/선물", "🎫 복권", "💰 저축", "기타"
    };

    private final SharedPreferences prefs;

    public CategoryManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        initializeDefaultCategories();
    }

    private void initializeDefaultCategories() {
        int savedVersion = prefs.getInt(KEY_CATEGORY_VERSION, 0);

        // 버전이 다르면 카테고리 초기화
        if (savedVersion < CURRENT_CATEGORY_VERSION) {
            setIncomeCategories(new ArrayList<>(Arrays.asList(DEFAULT_INCOME_CATEGORIES)));
            setExpenseCategories(new ArrayList<>(Arrays.asList(DEFAULT_EXPENSE_CATEGORIES)));
            prefs.edit().putInt(KEY_CATEGORY_VERSION, CURRENT_CATEGORY_VERSION).apply();
        }
    }

    public List<String> getIncomeCategories() {
        String saved = prefs.getString(KEY_INCOME_CATEGORIES, null);
        if (saved == null || saved.isEmpty()) {
            return new ArrayList<>(Arrays.asList(DEFAULT_INCOME_CATEGORIES));
        }
        return stringToList(saved);
    }

    public List<String> getExpenseCategories() {
        String saved = prefs.getString(KEY_EXPENSE_CATEGORIES, null);
        if (saved == null || saved.isEmpty()) {
            return new ArrayList<>(Arrays.asList(DEFAULT_EXPENSE_CATEGORIES));
        }
        return stringToList(saved);
    }

    public void setIncomeCategories(List<String> categories) {
        prefs.edit().putString(KEY_INCOME_CATEGORIES, listToString(categories)).apply();
    }

    public void setExpenseCategories(List<String> categories) {
        prefs.edit().putString(KEY_EXPENSE_CATEGORIES, listToString(categories)).apply();
    }

    private String listToString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(DELIMITER);
            }
        }
        return sb.toString();
    }

    private List<String> stringToList(String str) {
        if (str == null || str.isEmpty()) {
            return new ArrayList<>();
        }
        // Use Pattern.quote to escape regex special characters in DELIMITER
        String[] items = str.split(Pattern.quote(DELIMITER));
        return new ArrayList<>(Arrays.asList(items));
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
