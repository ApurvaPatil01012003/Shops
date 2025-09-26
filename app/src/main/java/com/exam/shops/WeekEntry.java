package com.exam.shops;
public class WeekEntry {
    private final String weekTitle;
    private final String dateRange;
    private final float amount;

    public WeekEntry(String weekTitle, String dateRange, float amount) {
        this.weekTitle = weekTitle;
        this.dateRange = dateRange;
        this.amount = amount;
    }

    public String getWeekTitle() { return weekTitle; }
    public String getDateRange() { return dateRange; }
    public float getAmount() { return amount; }
}

