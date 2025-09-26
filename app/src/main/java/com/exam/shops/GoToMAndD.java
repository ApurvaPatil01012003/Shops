package com.exam.shops;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.Manifest;

public class GoToMAndD extends AppCompatActivity {
    TextView btnyes, btnDyes, txtShopName, txtYearTarget, txtYearlyAch, txtMonthTarget, MonthlyAch, txtMonth;
    float monthlyTarget;
    int growth;
    ActionBarDrawerToggle toggle;
    private List<String> publicHolidays;
    private List<String> highPerfPreDays;
    private String shopHoliday;

    BottomNavigationView bottomNav;
    private int selectedYear;
    private int selectedMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_go_to_mand_d);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnyes = findViewById(R.id.btnyes);
        btnDyes = findViewById(R.id.btnDyes);
        txtShopName = findViewById(R.id.txtShopName);
        txtYearTarget = findViewById(R.id.txtYearTarget);
        txtMonthTarget = findViewById(R.id.txtMonthTarget);
        txtYearlyAch = findViewById(R.id.txtYearlyAch);
        MonthlyAch = findViewById(R.id.MonthlyAch);
        txtMonth = findViewById(R.id.txtMonth);

        String shopname = getIntent().getStringExtra("ShopName");
        String MobileNumber = getIntent().getStringExtra("Mobile_no");
        Log.d("Mob", "mobile :" + MobileNumber);
        Log.d("Mob", "shop :" + shopname);

        // Yearly achieved as float
        float yearlyAchieved = getIntent().getFloatExtra("YearlyAchieved", -1f);
        if (yearlyAchieved < 0f) {
            int year = Calendar.getInstance().get(Calendar.YEAR);
            yearlyAchieved = getTotalAchievedForYear(year);
        }
        txtYearlyAch.setText(String.format(Locale.getDefault(), " ₹ %.2f", yearlyAchieved));

        Intent intent = getIntent();
        int SecondTurnOverValue = intent.getIntExtra("TurnOver", -1);
        String Holiday = intent.getStringExtra("shopsHoliday");
        String HighPerDay = intent.getStringExtra("HighPerformace");
        growth = intent.getIntExtra("EdtGrowth", -1);

        updateYearlyAchievedDisplay();
        updateMonthAchieved(selectedYear, selectedMonth);
        updateRecentDaysData(7);
        updateRecentDaysData(15);
        updateTodaysMetricsFromPrefs();

        RecyclerView recyclerWeeks = findViewById(R.id.recyclerWeeks);
        recyclerWeeks.setLayoutManager(new LinearLayoutManager(this));

        List<WeekEntry> weekEntries = getWeeklyEntriesForMonth(selectedYear, selectedMonth);
        WeekEntryAdapter weekAdapter = new WeekEntryAdapter(weekEntries);
        recyclerWeeks.setAdapter(weekAdapter);

        Calendar now = Calendar.getInstance();
        selectedYear = now.get(Calendar.YEAR);
        selectedMonth = now.get(Calendar.MONTH);

        txtMonth.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(now.getTime()));

        loadMonthData(); // current month

        Button btnViewPrevious = findViewById(R.id.btnViewPrevious);
        btnViewPrevious.setOnClickListener(v -> {
            selectedMonth--;
            if (selectedMonth < 0) {
                selectedMonth = 11;
                selectedYear--;
            }
            Calendar cal = Calendar.getInstance();
            cal.set(selectedYear, selectedMonth, 1);
            txtMonth.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.getTime()));
            loadMonthData();
        });

        // Persist basic settings
        SharedPreferences prefs = getSharedPreferences("ShopData", MODE_PRIVATE);
        if (SecondTurnOverValue == -1) SecondTurnOverValue = prefs.getInt("TURNOVER", 0);
        if (Holiday == null || Holiday.equals("Select Day")) Holiday = prefs.getString("Shop_Holiday", "");
        if (HighPerDay == null || HighPerDay.isEmpty()) HighPerDay = prefs.getString("selected_days", "");
        if (growth == -1) growth = prefs.getInt("Growth", 0);
        if (shopname == null || shopname.isEmpty()) {
            shopname = prefs.getString("ShopName", "");
            MobileNumber = prefs.getString("Mobile_no", "");
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("TURNOVER", SecondTurnOverValue);
        editor.putString("Shop_Holiday", Holiday);
        editor.putString("selected_days", HighPerDay);
        editor.putInt("Growth", growth);
        editor.putString("ShopName", shopname);
        editor.putString("Mobile_no", MobileNumber);
        editor.apply();

        SharedPreferences sharedPref = getSharedPreferences("shop_data", MODE_PRIVATE);
        String name = sharedPref.getString("shop_name", "");
        String mob = sharedPref.getString("Mobile_no", "");
        Holiday = sharedPref.getString("Shop_Holiday", "");
        HighPerDay = sharedPref.getString("selected_days", "");
        growth = sharedPref.getInt("editGrowth", 0);

        txtShopName.setText(name);
        txtYearTarget.setText(String.valueOf(growth));
        float MonthlyTarget = growth / 12f;
        txtMonthTarget.setText(String.format(Locale.getDefault(), "%.0f", MonthlyTarget));
        Log.d("GOTOMonthlyTarget", "MonthlyTarget is : " + MonthlyTarget);

        int finalSecondTurnOverValue = SecondTurnOverValue;

        int finalGrowth1 = growth;
        btnyes.setOnClickListener(v -> {
            SharedPreferences sharedprefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            sharedprefs.edit().putInt("EdtGrowth", finalGrowth1).apply();
            Intent i = new Intent(GoToMAndD.this, YOYActivity.class);
            i.putExtra("EdtGrowth", finalGrowth1);
            startActivity(i);
        });

        String finalHoliday = Holiday;
        int finalSecondTurnOverValue1 = SecondTurnOverValue;

        String finalHighPerDay = HighPerDay;
        int finalGrowth = growth;
        Map<String, Float> updatedMonthlyTargets = getUpdatedMonthlyTargetsFromPrefs(growth);
        HashMap<String, Float> monthTargetMap = new HashMap<>(updatedMonthlyTargets);

        btnDyes.setOnClickListener(v -> {
            Intent i = new Intent(GoToMAndD.this, DailyTableYOY.class);
            i.putExtra("ShopHoliday", finalHoliday);
            i.putExtra("TurnYear", finalSecondTurnOverValue1);
            i.putExtra("HighPerformance", finalHighPerDay);
            i.putExtra("Growth", finalGrowth);
            i.putExtra("MonthlyTarget", monthlyTarget);
            i.putExtra("MonthTargetMap", monthTargetMap);
            startActivityForResult(i, 101);
        });

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.btmNav_home);
        String finalMobileNumber = MobileNumber;
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.btmNav_home) {
                return true;
            } else if (id == R.id.btmNav_data_erntry) {
                Intent iDataEntry = new Intent(GoToMAndD.this, YOYSecondActivity.class);
                iDataEntry.putExtra("ShopHoliday", finalHoliday);
                iDataEntry.putExtra("TurnYear", finalSecondTurnOverValue1);
                iDataEntry.putExtra("HighPerformance", finalHighPerDay);
                iDataEntry.putExtra("Growth", finalGrowth);
                iDataEntry.putExtra("MonthlyTarget", monthlyTarget);
                iDataEntry.putExtra("MonthTargetMap", monthTargetMap);
                startActivityForResult(iDataEntry, 101);
                return true;
            } else if (id == R.id.btmNav_data_profile) {
                Intent iProfile = new Intent(GoToMAndD.this, Profile.class);
                iProfile.putExtra("shop_name", name);
                iProfile.putExtra("Mobile_no", mob);
                startActivity(iProfile);
                return true;
            }
            return false;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "daily_notify",
                    "Daily Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setSound(null, null);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        scheduleDailyNotification();

        shopHoliday = sharedPref.getString("Shop_Holiday", "");
        publicHolidays = HolidayUtils.loadHolidayDates(this);
        highPerfPreDays = HolidayUtils.getPreHolidayHighPerformanceDates(publicHolidays, shopHoliday);
        Log.d("Publicdays", "IS : " + publicHolidays);
        Log.d("Publicdays", "IS : " + highPerfPreDays);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle != null && toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        growth = getSharedPreferences("shop_data", MODE_PRIVATE).getInt("editGrowth", 0);
        txtYearTarget.setText(String.valueOf(growth));
        updateYearlyAchievedDisplay();
        updateMonthAchieved(selectedYear, selectedMonth);
        updateTodaysMetricsFromPrefs();
        reloadWeekRecycler();
    }

    /** FLOAT VERSION: Yearly total achieved */
    private float getTotalAchievedForYear(int year) {
        SharedPreferences prefs = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);
        float totalAchievedYearly = 0f;

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        for (String month : months) {
            String key = "data_" + month + "_" + year + "_Achieved";
            totalAchievedYearly += getSafeFloat(prefs, key, 0f);
        }
        return totalAchievedYearly;
    }

    private void updateYearlyAchievedDisplay() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        float yearlyAchieved = getTotalAchievedForYear(currentYear);
        txtYearlyAch.setText(String.format(Locale.getDefault(), "₹ %.2f/", yearlyAchieved));

        if (growth == 0) {
            SharedPreferences prefs = getSharedPreferences("ShopData", MODE_PRIVATE);
            growth = prefs.getInt("Result_TURNOVER", 0);
        }

        float percent = 0f;
        if (growth != 0) {
            percent = (yearlyAchieved / (float) growth) * 100f;
        }
        Log.d("GoToMAndD", "Updated Yearly Achieved: ₹" + yearlyAchieved + " (" + percent + "%)");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK) {
            updateYearlyAchievedDisplay();
            Log.d("GoToMAndD", "Returned from YOYSecondActivity - Yearly Achieved Updated");
        }
        if (requestCode == 101 && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("data_updated", false)) {
                updateRecentDaysData(7);
                updateRecentDaysData(15);
                updateTodaysMetricsFromPrefs();
                Log.d("GoToMAndD", "Data updated from DailyTableYOY → via YOYSecondActivity");
            }
        }

        if (requestCode == 123 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                BackupHelper.importBackup(this, uri);
            } else {
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            }
        }
        reloadWeekRecycler();
    }

    /** FLOAT VERSION: 7/15 day summary uses float Achieved */
    private void updateRecentDaysData(int daysBack) {
        SharedPreferences prefs = getSharedPreferences("Shop Data", MODE_PRIVATE);

        float expectedTotal = getExpectedSumForPastDays(daysBack);
        float achievedTotal = 0f;

        SimpleDateFormat keyFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        for (int i = 1; i <= daysBack; i++) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
            String dateStr = keyFormat.format(cal.getTime());

            float achieved = getSafeFloat(prefs, "Achieved_" + dateStr, 0f);
            achievedTotal += achieved;

            Log.d("DAYS_LOOP", dateStr + " | Achieved = ₹" + achieved);
        }

        float percent = (expectedTotal > 0f) ? (achievedTotal * 100f / expectedTotal) : 0f;

        Log.d("RecentStats", daysBack + " Days → Target: ₹" + expectedTotal +
                " | Achieved: ₹" + achievedTotal + " | %: " + percent);
    }

    private void updateTodaysMetricsFromPrefs() {
        SharedPreferences todayPrefs = getSharedPreferences("TodayData", MODE_PRIVATE);
        int abs = todayPrefs.getInt("today_abs", 0);
        float atv = todayPrefs.getFloat("today_atv", 0f);
        float asp = todayPrefs.getFloat("today_asp", 0f);
        Log.d("GoToActivity", "ABS: " + abs + " | ATV: " + atv + " | ASP: " + asp);
    }

    private float getSafeFloat(SharedPreferences prefs, String key, float defaultValue) {
        try {
            return prefs.getFloat(key, defaultValue);
        } catch (ClassCastException e) {
            try {
                Object value = prefs.getAll().get(key);
                if (value instanceof Integer) {
                    return ((Integer) value).floatValue();
                } else if (value instanceof Long) {
                    return ((Long) value).floatValue();
                } else if (value instanceof String) {
                    return Float.parseFloat((String) value);
                } else if (value instanceof Float) {
                    return (Float) value;
                } else {
                    return defaultValue;
                }
            } catch (Exception ex) {
                return defaultValue;
            }
        }
    }

    private float getExpectedSumForPastDays(int daysBack) {
        SharedPreferences prefs = getSharedPreferences("Shop Data", MODE_PRIVATE);
        SimpleDateFormat keyFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        float total = 0f;
        for (int i = 1; i <= daysBack; i++) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
            String dateStr = keyFormat.format(cal.getTime());
            float expected = getSafeFloat(prefs, "Expected_" + dateStr, -1f);
            if (expected >= 0f) total += expected;
            Log.d("ExpectedSum", dateStr + " → ₹" + expected);
        }
        Log.d("ExpectedSum", daysBack + " days total: ₹" + total);
        return total;
    }

    private Map<String, Float> getUpdatedMonthlyTargetsFromPrefs(int yearlyGrowth) {
        SharedPreferences prefs = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);
        Map<String, Float> monthTargets = new HashMap<>();

        List<String> months = Arrays.asList(
                "Apr", "May", "Jun", "Jul", "Aug", "Sep",
                "Oct", "Nov", "Dec", "Jan", "Feb", "Mar"
        );

        int fyStartYear = getCurrentFinancialYearStart();
        float defaultPerMonth = yearlyGrowth / 12f;

        for (String month : months) {
            int year = (month.equals("Jan") || month.equals("Feb") || month.equals("Mar"))
                    ? fyStartYear + 1
                    : fyStartYear;

            String key = "expected_" + month + "_" + year;
            float target = getSafeFloat(prefs, key, -1f);
            if (target == -1f) target = defaultPerMonth;

            monthTargets.put(month + "_" + year, target);
            Log.d("MonthlyTargetFinal", "Month: " + month + " " + year + " → ₹" + target);
        }

        return monthTargets;
    }

    private int getCurrentFinancialYearStart() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        return (month >= Calendar.APRIL) ? year : (year - 1);
    }

    private void scheduleDailyNotification() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Calendar dailyCalendar = Calendar.getInstance();
        dailyCalendar.set(Calendar.HOUR_OF_DAY, 17);
        dailyCalendar.set(Calendar.MINUTE, 57);
        dailyCalendar.set(Calendar.SECOND, 0);
        dailyCalendar.set(Calendar.MILLISECOND, 0);
        if (dailyCalendar.getTimeInMillis() <= System.currentTimeMillis()) {
            dailyCalendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent dailyIntent = new Intent(this, NotificationReceiver.class);
        dailyIntent.setAction("DAILY_NOTIFICATION");
        PendingIntent dailyPendingIntent = PendingIntent.getBroadcast(
                this, 0, dailyIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    dailyCalendar.getTimeInMillis(),
                    dailyPendingIntent
            );
        }

        Calendar noonCalendar = Calendar.getInstance();
        noonCalendar.set(Calendar.HOUR_OF_DAY, 15);
        noonCalendar.set(Calendar.MINUTE, 57);
        noonCalendar.set(Calendar.SECOND, 0);
        noonCalendar.set(Calendar.MILLISECOND, 0);
        if (noonCalendar.getTimeInMillis() <= System.currentTimeMillis()) {
            noonCalendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent noonIntent = new Intent(this, NotificationReceiver.class);
        noonIntent.setAction("CHECK_DATA_FILLED");
        PendingIntent noonPendingIntent = PendingIntent.getBroadcast(
                this, 1, noonIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    noonCalendar.getTimeInMillis(),
                    noonPendingIntent
            );
        }
    }

    /** FLOAT VERSION: Weekly grouping sums float Achieved per week */
    private List<WeekEntry> getWeeklyEntriesForMonth(int year, int month) {
        List<WeekEntry> list = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences("Shop Data", MODE_PRIVATE);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        Calendar temp = Calendar.getInstance();
        temp.set(year, month, 1);
        int lastDay = temp.getActualMaximum(Calendar.DAY_OF_MONTH);

        int[][] ranges = {
                {1, 7},
                {8, 14},
                {15, 21},
                {22, lastDay}
        };

        for (int i = 0; i < ranges.length; i++) {
            int startDay = ranges[i][0];
            int endDay = ranges[i][1];

            float achievedSum = 0f; // float now
            for (int day = startDay; day <= endDay; day++) {
                Calendar d = Calendar.getInstance();
                d.set(year, month, day);
                String dateStr = dateFormat.format(d.getTime());
                achievedSum += getSafeFloat(prefs, "Achieved_" + dateStr, 0f);
            }

            String startDate = dateFormat.format(getDate(year, month, startDay));
            String endDate = dateFormat.format(getDate(year, month, endDay));
            String dateRange = startDate + " - " + endDate;

            // Ensure WeekEntry supports float achieved in its constructor
            list.add(new WeekEntry("Week " + (i + 1), dateRange, achievedSum));
        }

        return list;
    }

    private java.util.Date getDate(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day, 0, 0, 0);
        return c.getTime();
    }

    private void loadMonthData() {
        updateMonthAchieved(selectedYear, selectedMonth);

        RecyclerView recyclerWeeks = findViewById(R.id.recyclerWeeks);
        recyclerWeeks.setLayoutManager(new LinearLayoutManager(this));
        List<WeekEntry> weekEntries = getWeeklyEntriesForMonth(selectedYear, selectedMonth);
        WeekEntryAdapter weekAdapter = new WeekEntryAdapter(weekEntries);
        recyclerWeeks.setAdapter(weekAdapter);
    }
    private void updateMonthAchieved(int year, int monthIndex) {
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        String currentMonth = monthNames[monthIndex];

        SharedPreferences prefs = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);
        int dataYear = (currentMonth.equals("Jan") || currentMonth.equals("Feb") || currentMonth.equals("Mar"))
                ? year + 1 : year;

        String key = "data_" + currentMonth + "_" + dataYear + "_Achieved";
        float achieved = getSafeFloat(prefs, key, 0f);

        monthlyTarget = getSafeFloat(prefs, "expected_" + currentMonth + "_" + dataYear, growth / 12f);

        MonthlyAch.setText(String.format(Locale.getDefault(), "₹ %.2f/", achieved));
        txtMonthTarget.setText(String.format(Locale.getDefault(), "₹ %.2f", monthlyTarget));


    }

    private void reloadWeekRecycler() {
        RecyclerView recyclerWeeks = findViewById(R.id.recyclerWeeks);
        recyclerWeeks.setLayoutManager(new LinearLayoutManager(this));
        List<WeekEntry> weekEntries = getWeeklyEntriesForMonth(selectedYear, selectedMonth);
        WeekEntryAdapter weekAdapter = new WeekEntryAdapter(weekEntries);
        recyclerWeeks.setAdapter(weekAdapter);
    }
}
