package com.example.financetracker.ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

public class TodayDecorator implements DayViewDecorator {
    private final CalendarDay today;
    private final Drawable highlightDrawable;

    public TodayDecorator() {
        this.today = CalendarDay.today();
        // Light blue highlight color for today
        this.highlightDrawable = new ColorDrawable(Color.parseColor("#E3F2FD"));
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return day.equals(today);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(highlightDrawable);
    }
}
