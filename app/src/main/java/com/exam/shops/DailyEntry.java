package com.exam.shops;


public class DailyEntry {
    private String date;
    private float achieved;
    private float quantity;
    private float nob;

    public DailyEntry(String date, float achieved, float quantity, float nob) {
        this.date = date;
        this.achieved = achieved;
        this.quantity = quantity;
        this.nob = nob;
    }

    public String getDate() {
        return date;
    }

    public float getAchieved() {
        return achieved;
    }

    public float getQuantity() {
        return quantity;
    }

    public float getNob() {
        return nob;
    }
}