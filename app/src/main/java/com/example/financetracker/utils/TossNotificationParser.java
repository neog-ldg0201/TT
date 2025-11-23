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
     * 토스 알림 형식 예시:
     *
     * 결제:
     *   - 제목: "6,600원 결제"
     *   - 내용: "동건이 밥그릇 카드 | 지에스25 거북섬더웰점..."
     *
     * 캐시백:
     *   - 제목: "11원 캐시백 🎉"
     *   - 내용: "3,700원 결제 | 우지커피 시흥MTV점\n잔액 62,730원(토스뱅크 체크카드)"
     *
     * 입금:
     *   - 제목: "100원 입금"
     *   - 내용: "백창관 → 내 토스뱅크 통장"
     *
     * @param title Notification title
     * @param text Notification text
     * @return Map containing parsed data (amount, description, type)
     */
    public static Map<String, String> parse(String title, String text) {
        Map<String, String> result = new HashMap<>();

        try {
            Log.d(TAG, "Parsing - Title: " + title + ", Text: " + text);

            String amount = null;
            String type = null;
            String description = "";
            String extraInfo = "";

            // 캐시백/포인트 알림인 경우 - 텍스트에서 실제 결제 금액 추출
            if (title.contains("캐시백") || title.contains("포인트") || title.contains("적립")) {
                // 캐시백 정보는 설명에 추가
                Pattern cashbackPattern = Pattern.compile("([\\d,]+)\\s*원\\s*(캐시백|포인트|적립)");
                Matcher cashbackMatcher = cashbackPattern.matcher(title);
                if (cashbackMatcher.find()) {
                    extraInfo = cashbackMatcher.group(1) + "원 " + cashbackMatcher.group(2);
                }

                // 텍스트에서 실제 결제 금액 추출 ("3,700원 결제")
                Pattern paymentPattern = Pattern.compile("([\\d,]+)\\s*원\\s*결제");
                Matcher paymentMatcher = paymentPattern.matcher(text);
                if (paymentMatcher.find()) {
                    amount = paymentMatcher.group(1).replace(",", "");
                    type = "EXPENSE";
                }
            }

            // 일반 결제/입금 알림 - 타이틀에서 금액 추출
            if (amount == null) {
                // 타이틀에서 금액 추출
                Pattern amountPattern = Pattern.compile("([\\d,]+)\\s*원");
                Matcher amountMatcher = amountPattern.matcher(title);

                if (amountMatcher.find()) {
                    amount = amountMatcher.group(1).replace(",", "");
                }

                // 타입 결정
                if (title.contains("입금") || title.contains("받")) {
                    type = "INCOME";
                } else if (title.contains("결제") || title.contains("출금") || title.contains("송금") || title.contains("이체")) {
                    type = "EXPENSE";
                } else {
                    type = "EXPENSE"; // Default
                }
            }

            if (amount == null) {
                Log.d(TAG, "No amount found");
                return null;
            }

            result.put("amount", amount);
            result.put("type", type);
            Log.d(TAG, "Extracted amount: " + amount + ", type: " + type);

            // 설명 추출
            description = extractDescription(text, type, extraInfo);
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
     * - 잔액 정보 제외
     * - 카드 정보와 캐시백은 설명에 포함
     * - 상호명 추출
     */
    private static String extractDescription(String text, String type, String extraInfo) {
        if (text == null || text.isEmpty()) {
            return type.equals("INCOME") ? "토스 입금" : "토스 결제";
        }

        String description = text.trim();
        StringBuilder descParts = new StringBuilder();

        // 잔액 정보 제거 ("잔액 62,730원(토스뱅크 체크카드)" 부분 제거)
        description = description.replaceAll("잔액\\s*[\\d,]+\\s*원[^\\n]*", "").trim();

        // 줄바꿈으로 분리된 경우 첫 번째 줄만 사용
        if (description.contains("\n")) {
            description = description.split("\n")[0].trim();
        }

        if (type.equals("INCOME")) {
            // 입금: "백창관 → 내 토스뱅크 통장" → "백창관" (보낸 사람)
            if (description.contains("→")) {
                String[] parts = description.split("→");
                if (parts.length > 0) {
                    descParts.append(parts[0].trim());
                }
            } else {
                descParts.append(description);
            }
        } else {
            // 결제: "3,700원 결제 | 우지커피 시흥MTV점" → 상호명과 카드 정보 분리
            String storeName = "";
            String cardInfo = "";

            if (description.contains("|")) {
                String[] parts = description.split("\\|");

                // "3,700원 결제 | 우지커피" 형식 처리
                if (parts.length > 1) {
                    String leftPart = parts[0].trim();
                    String rightPart = parts[1].trim();

                    // 왼쪽이 "~~원 결제" 형식이면 오른쪽이 상호명
                    if (leftPart.matches(".*\\d+.*원\\s*결제.*")) {
                        storeName = rightPart;
                    }
                    // 왼쪽이 카드명이면 오른쪽이 상호명
                    else if (leftPart.contains("카드") || leftPart.contains("통장") || leftPart.contains("계좌")) {
                        cardInfo = leftPart;
                        storeName = rightPart;
                    } else {
                        // 기타: 첫 번째가 카드, 두 번째가 상호명
                        cardInfo = leftPart;
                        storeName = rightPart;
                    }
                }
            } else {
                storeName = description;
            }

            // "..." 제거
            if (storeName.endsWith("...")) {
                storeName = storeName.substring(0, storeName.length() - 3).trim();
            }

            // 상호명이 비어있으면 기본값
            if (storeName.isEmpty()) {
                storeName = "토스 결제";
            }

            descParts.append(storeName);

            // 카드 정보 추가
            if (!cardInfo.isEmpty()) {
                descParts.append(" (").append(cardInfo).append(")");
            }
        }

        // 캐시백/포인트 정보 추가
        if (!extraInfo.isEmpty()) {
            if (descParts.length() > 0) {
                descParts.append(" +").append(extraInfo);
            } else {
                descParts.append(extraInfo);
            }
        }

        String result = descParts.toString().trim();
        if (result.isEmpty() || result.length() < 2) {
            result = type.equals("INCOME") ? "토스 입금" : "토스 결제";
        }

        return result;
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

        // 캐시백/포인트 알림도 결제 알림으로 처리
        if (title.contains("캐시백") || title.contains("포인트") || title.contains("적립")) {
            // 텍스트에 "결제" 정보가 있으면 거래 알림으로 처리
            if (text != null && text.contains("결제")) {
                return true;
            }
        }

        // Check if title contains transaction keywords
        return title.contains("입금") || title.contains("출금") ||
               title.contains("결제") || title.contains("송금") ||
               title.contains("이체") || title.contains("받");
    }
}
