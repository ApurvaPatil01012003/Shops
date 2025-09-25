package com.exam.shops;


public class DailyEntry {
    private String date;
    private int achieved;
    private int quantity;
    private int nob;

    public DailyEntry(String date, int achieved, int quantity, int nob) {
        this.date = date;
        this.achieved = achieved;
        this.quantity = quantity;
        this.nob = nob;
    }

    public String getDate() {
        return date;
    }

    public int getAchieved() {
        return achieved;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getNob() {
        return nob;
    }
}
