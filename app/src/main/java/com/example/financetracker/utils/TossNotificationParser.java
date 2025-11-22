package com.example.financetracker.utils;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TossNotificationParser {

    private static final String TAG = "TossNotificationParser";

    /**
     * Parses Toss notification content to extract transaction information
     *
     * 실제 토스 알림 형식:
     * 입금:
     *   - 제목: "100원 입금"
     *   - 내용: "백창관 → 내 토스뱅크 통장"
     *
     * 결제:
     *   - 제목: "6,600원 결제"
     *   - 내용: "동건이 밥그릇 카드 | 지에스25 거북섬더웰점..."
     *
     * @param title Notification title (예: "100원 입금", "6,600원 결제")
     * @param text Notification text (예: "백창관 → 내 토스뱅크 통장")
     * @return Map containing parsed data (amount, description, type)
     */
    public static Map<String, String> parse(String title, String text) {
        Map<String, String> result = new HashMap<>();

        try {
            Log.d(TAG, "Parsing - Title: " + title + ", Text: " + text);

            // Extract amount from title (예: "6,600원 결제" → 6600)
            Pattern amountPattern = Pattern.compile("([\\d,]+)\\s*원");
            Matcher amountMatcher = amountPattern.matcher(title);

            if (amountMatcher.find()) {
                String amountStr = amountMatcher.group(1).replace(",", "");
                result.put("amount", amountStr);
                Log.d(TAG, "Extracted amount: " + amountStr);
            } else {
                Log.d(TAG, "No amount found in title");
                return null;
            }

            // Determine transaction type from title
            String type;
            if (title.contains("입금") || title.contains("받")) {
                type = "INCOME";
            } else if (title.contains("결제") || title.contains("출금") || title.contains("송금") || title.contains("이체")) {
                type = "EXPENSE";
            } else {
                type = "EXPENSE"; // Default to expense
            }
            result.put("type", type);
            Log.d(TAG, "Transaction type: " + type);

            // Extract description from text
            String description = extractDescription(text, type);
            result.put("description", description);
            Log.d(TAG, "Extracted description: " + description);

            return result;

        } catch (Exception e) {
            Log.e(TAG, "Error parsing notification", e);
            return null;
        }
    }

    /**
     * Extract meaningful description from notification text
     *
     * 입금: "백창관 → 내 토스뱅크 통장" → "백창관"
     * 결제: "동건이 밥그릇 카드 | 지에스25 거북섬더웰점..." → "지에스25 거북섬더웰점"
     */
    private static String extractDescription(String text, String type) {
        if (text == null || text.isEmpty()) {
            return type.equals("INCOME") ? "토스 입금" : "토스 결제";
        }

        String description = text.trim();

        if (type.equals("INCOME")) {
            // 입금: "백창관 → 내 토스뱅크 통장" → "백창관" (보낸 사람)
            if (description.contains("→")) {
                String[] parts = description.split("→");
                if (parts.length > 0) {
                    description = parts[0].trim();
                }
            }
        } else {
            // 결제: "동건이 밥그릇 카드 | 지에스25 거북섬더웰점..." → "지에스25 거북섬더웰점"
            if (description.contains("|")) {
                String[] parts = description.split("\\|");
                if (parts.length > 1) {
                    description = parts[1].trim();
                }
            }
            // Remove trailing "..." if present
            if (description.endsWith("...")) {
                description = description.substring(0, description.length() - 3).trim();
            }
        }

        // If description is still empty or too short, use default
        if (description.isEmpty() || description.length() < 2) {
            description = type.equals("INCOME") ? "토스 입금" : "토스 결제";
        }

        return description;
    }

    /**
     * Validates if the notification is likely a transaction notification
     */
    public static boolean isTransactionNotification(String title, String text) {
        if (title == null || title.isEmpty()) {
            return false;
        }

        // Check if title contains amount pattern (숫자+원)
        Pattern amountPattern = Pattern.compile("[\\d,]+\\s*원");
        Matcher amountMatcher = amountPattern.matcher(title);

        if (!amountMatcher.find()) {
            return false;
        }

        // Check if title contains transaction keywords
        return title.contains("입금") || title.contains("출금") ||
               title.contains("결제") || title.contains("송금") ||
               title.contains("이체") || title.contains("받");
    }
}
