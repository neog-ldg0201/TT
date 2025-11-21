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
     * Example Toss notifications:
     * - "10,000원 결제" (Payment)
     * - "100,000원 입금" (Deposit)
     * - "KB국민 3,500원 결제" (Card payment)
     *
     * @param title Notification title
     * @param text Notification text
     * @return Map containing parsed data (amount, description, type)
     */
    public static Map<String, String> parse(String title, String text) {
        Map<String, String> result = new HashMap<>();

        try {
            // Combine title and text for parsing
            String content = title + " " + text;

            // Extract amount (숫자 + 원)
            Pattern amountPattern = Pattern.compile("([\\d,]+)\\s*원");
            Matcher amountMatcher = amountPattern.matcher(content);

            if (amountMatcher.find()) {
                String amountStr = amountMatcher.group(1).replace(",", "");
                result.put("amount", amountStr);
                Log.d(TAG, "Extracted amount: " + amountStr);
            } else {
                return null; // No amount found
            }

            // Determine transaction type based on keywords
            String type = "EXPENSE"; // Default to expense
            if (content.contains("입금") || content.contains("수입") ||
                content.contains("받으셨") || content.contains("충전")) {
                type = "INCOME";
            } else if (content.contains("결제") || content.contains("지출") ||
                       content.contains("출금") || content.contains("송금")) {
                type = "EXPENSE";
            }
            result.put("type", type);

            // Extract description
            // Remove amount and common keywords to get description
            String description = content
                .replaceAll("[\\d,]+\\s*원", "")
                .replaceAll("입금|출금|결제|지출|수입|송금", "")
                .trim();

            // If description is empty, use a default based on type
            if (description.isEmpty()) {
                description = type.equals("INCOME") ? "토스 입금" : "토스 결제";
            }

            result.put("description", description);
            Log.d(TAG, "Extracted description: " + description);
            Log.d(TAG, "Transaction type: " + type);

            return result;

        } catch (Exception e) {
            Log.e(TAG, "Error parsing notification", e);
            return null;
        }
    }

    /**
     * Validates if the notification is likely a transaction notification
     */
    public static boolean isTransactionNotification(String title, String text) {
        String content = title + " " + text;

        // Check if contains amount pattern
        Pattern amountPattern = Pattern.compile("[\\d,]+\\s*원");
        Matcher amountMatcher = amountPattern.matcher(content);

        if (!amountMatcher.find()) {
            return false;
        }

        // Check if contains transaction keywords
        return content.contains("입금") || content.contains("출금") ||
               content.contains("결제") || content.contains("지출") ||
               content.contains("수입") || content.contains("송금") ||
               content.contains("충전");
    }
}
