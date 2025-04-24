package com.exam.shops;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class InitializerRecentDays {

    public static void cacheExpectedSums(Context context) {
        ensureExpectedValuesExist(context);

        SharedPreferences prefs = context.getSharedPreferences("Shop Data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        float expected7 = getExpectedSumForPastDays(context, 7);
        float expected15 = getExpectedSumForPastDays(context, 15);

        editor.putFloat("Expected_Target_7_Days", expected7);
        editor.putFloat("Expected_Target_15_Days", expected15);
        editor.apply();

        Log.d("InitCache", "Expected 7 days: " + expected7 + " | 15 days: " + expected15);
    }

    private static float getExpectedSumForPastDays(Context context, int days) {
        SharedPreferences prefs = context.getSharedPreferences("Shop Data", Context.MODE_PRIVATE);
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        float total = 0f;
        for (int i = 1; i <= days; i++) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
            String key = "Expected_" + sdf.format(cal.getTime());
            float value = prefs.getFloat(key, -1f);
            if (value >= 0f) total += value;
        }
        return total;
    }

    // Generate default expected values for today and earlier if not present
    private static void ensureExpectedValuesExist(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("Shop Data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        float dailyTarget = prefs.getFloat("DefaultDailyTarget", 1000f); // fallback default

        for (int i = 1; i <= 15; i++) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
            String dateKey = "Expected_" + sdf.format(cal.getTime());
            if (!prefs.contains(dateKey)) {
                editor.putFloat(dateKey, dailyTarget);
                Log.d("InitExpected", "Auto-added expected for: " + dateKey + " = ₹" + dailyTarget);
            }
        }
        editor.apply();
    }
}
