package com.example.financetracker.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.LineBackgroundSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.HashMap;
import java.util.Map;

public class DailyTransactionDecorator implements DayViewDecorator {
    private final Map<CalendarDay, DayTransactionInfo> transactionInfo;

    public DailyTransactionDecorator(Map<CalendarDay, DayTransactionInfo> transactionInfo) {
        this.transactionInfo = transactionInfo;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return transactionInfo.containsKey(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        // Add custom span to show transaction info
    }

    public static class DayTransactionInfo {
        public long income;
        public long expense;

        public DayTransactionInfo(long income, long expense) {
            this.income = income;
            this.expense = expense;
        }
    }
}
