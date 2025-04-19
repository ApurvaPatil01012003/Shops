package com.exam.shops;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TodayDataUtils {

    public static void updateTodayData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("ShopData", Context.MODE_PRIVATE);
        SharedPreferences dataPrefs = context.getSharedPreferences("Shop Data", Context.MODE_PRIVATE);





int Result =prefs.getInt("Result_TURNOVER",0);
       // int turnover = prefs.getInt("TURNOVER", 0);
        String holiday = prefs.getString("Shop_Holiday", "Sunday");
        String highPerDays = prefs.getString("selected_days", "");
        int growth = prefs.getInt("Growth", 0);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());

        String todayDate = dateFormat.format(cal.getTime());
        String todayDay = dayFormat.format(cal.getTime());

        int achieved = dataPrefs.getInt("Achieved_" + todayDate, 0);
        int quantity = dataPrefs.getInt("Quantity_" + todayDate, 0);
        int nob = dataPrefs.getInt("NOB_" + todayDate, 0);

        List<String> highPerfList = new ArrayList<>();
        if (highPerDays != null && !highPerDays.isEmpty()) {
            for (String s : highPerDays.split(",")) highPerfList.add(s.trim());
        }

        int workingDays = 0;
        int highPerfDays = 0;
        Calendar tempCal = Calendar.getInstance();
        tempCal.set(Calendar.DAY_OF_MONTH, 1);
        int daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 1; i <= daysInMonth; i++) {
            tempCal.set(Calendar.DAY_OF_MONTH, i);
            String dayStr = dayFormat.format(tempCal.getTime());
            if (!dayStr.equalsIgnoreCase(holiday)) {
                workingDays++;
                if (highPerfList.contains(dayStr)) {
                    highPerfDays++;
                }
            }
        }

        float monthlyTarget = Result / 12f;
        float growthMultiplier = 1 + (growth / 100f);
        float baseDailyTarget = monthlyTarget / (workingDays + highPerfDays * (growthMultiplier - 1));

        float expected = 0f;
        String type;

        if (todayDay.equalsIgnoreCase(holiday)) {
            expected = 0f;
            type = "Holiday";
        } else if (highPerfList.contains(todayDay)) {
            expected = baseDailyTarget * growthMultiplier;
            type = "High Performance Day";
        } else {
            expected = baseDailyTarget;
            type = "Working Day";
        }

        float percent = (expected > 0) ? (achieved / expected) * 100f : 0f;

        SharedPreferences.Editor editor = context.getSharedPreferences("TodayData", Context.MODE_PRIVATE).edit();
        editor.putString("today_expected", String.format(Locale.US, "%.2f", expected));
        editor.putInt("today_achieved", achieved);
        editor.putFloat("today_percent", percent);
        editor.putString("today_type", type);
        editor.apply();
    }
}
