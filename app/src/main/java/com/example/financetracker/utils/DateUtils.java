package com.example.financetracker.utils;

import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(new Date());
    }

    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);
        return sdf.format(new Date());
    }

    public static String formatDate(int year, int month, int day) {
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, day);
    }

    public static String formatTime(int hour, int minute) {
        return String.format(Locale.US, "%02d:%02d", hour, minute);
    }

    public static String calendarDayToString(CalendarDay day) {
        return String.format(Locale.US, "%04d-%02d-%02d",
            day.getYear(), day.getMonth(), day.getDay());
    }

    public static String formatDateKorean(String dateStr) {
        try {
            String[] parts = dateStr.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);
            return String.format(Locale.KOREA, "%d년 %d월 %d일", year, month, day);
        } catch (Exception e) {
            return dateStr;
        }
    }

    public static Calendar stringToCalendar(String dateStr) {
        Calendar calendar = Calendar.getInstance();
        try {
            String[] parts = dateStr.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1; // Calendar month is 0-based
            int day = Integer.parseInt(parts[2]);
            calendar.set(year, month, day);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return calendar;
    }
}
