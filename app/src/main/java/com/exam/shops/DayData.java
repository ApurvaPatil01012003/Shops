package com.exam.shops;

public class DayData {
    String date, type;
    float target, abs, atv, asp;
    int achieved;
    float achievementPercentage;

    public DayData(String date, String type, float target, float abs, float atv, float asp, int achieved, float achievementPercentage) {
        this.date = date;
        this.type = type;
        this.target = target;
        this.abs = abs;
        this.atv = atv;
        this.asp = asp;
        this.achieved = achieved;
        this.achievementPercentage = achievementPercentage;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public float getTarget() {
        return target;
    }

    public void setTarget(float target) {
        this.target = target;
    }

    public float getAbs() {
        return abs;
    }

    public void setAbs(float abs) {
        this.abs = abs;
    }

    public float getAtv() {
        return atv;
    }

    public void setAtv(float atv) {
        this.atv = atv;
    }

    public float getAsp() {
        return asp;
    }

    public void setAsp(float asp) {
        this.asp = asp;
    }

    public int getAchieved() {
        return achieved;
    }

    public void setAchieved(int achieved) {
        this.achieved = achieved;
    }

    public float getAchievementPercentage() {
        return achievementPercentage;
    }

    public void setAchievementPercentage(float achievementPercentage) {
        this.achievementPercentage = achievementPercentage;
    }
}
