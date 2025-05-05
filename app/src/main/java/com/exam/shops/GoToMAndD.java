package com.exam.shops;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.navigation.NavigationView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import android.Manifest;


public class GoToMAndD extends AppCompatActivity {
    Button btnyes, btnDyes;
    TextView txtShopName, txtYearTarget, txtYearlyAch, YearlyAchPer, txtMonthTarget, MonthlyAch, MonthlyAchPer, txtTargetRecentdays, txtAchievedRecentdays, txtAchievedPerRecentdays;
    Spinner spinnerSR;
    LineChart lineChart, lineChartATV, lineChartASP;
    float monthlyTarget;
    int growth;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    private List<String> publicHolidays;
    private List<String> highPerfPreDays;
    private String shopHoliday;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_go_to_mand_d);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            int paddingDp = (int) (30 * getResources().getDisplayMetrics().density);

            v.setPadding(
                    systemBars.left + paddingDp,
                    systemBars.top + paddingDp,
                    systemBars.right + paddingDp,
                    systemBars.bottom + paddingDp
            );

            return insets;
        });

        btnyes = findViewById(R.id.btnyes);
        btnDyes = findViewById(R.id.btnDyes);
        txtShopName = findViewById(R.id.txtShopName);
        txtYearTarget = findViewById(R.id.txtYearTarget);
        txtMonthTarget = findViewById(R.id.txtMonthTarget);
        txtYearlyAch = findViewById(R.id.txtYearlyAch);
        YearlyAchPer = findViewById(R.id.YearlyAchPer);
        MonthlyAch = findViewById(R.id.MonthlyAch);
        MonthlyAchPer = findViewById(R.id.MonthlyAchPer);
        spinnerSR = findViewById(R.id.spinnerSR);
        txtTargetRecentdays = findViewById(R.id.txtTargetRecentdays);
        txtAchievedRecentdays = findViewById(R.id.txtAchievedRecentdays);
        txtAchievedPerRecentdays = findViewById(R.id.txtAchievedPerRecentdays);
        lineChart = findViewById(R.id.lineChart);
        drawLineChartABS(7);
        lineChartATV = findViewById(R.id.lineChartATV);
        drawLineChartATV(7);
        lineChartASP = findViewById(R.id.lineChartASP);
        drawLineChartASP(7);
        drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        ArrayAdapter<String> SR = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Select Days", "15 days", "7 days"});
        SR.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSR.setAdapter(SR);
        spinnerSR.setSelection(2);

        spinnerSR.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    updateRecentDaysData(15);
                    drawLineChartABS(15);
                    drawLineChartATV(15);
                    drawLineChartASP(15);
                } else if (position == 2) {
                    updateRecentDaysData(7);
                    drawLineChartABS(7);
                    drawLineChartATV(7);
                    drawLineChartASP(7);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        String shopname = getIntent().getStringExtra("ShopName");


        int yearlyAchieved = getIntent().getIntExtra("YearlyAchieved", -1);
        if (yearlyAchieved == -1) {
            int year = Calendar.getInstance().get(Calendar.YEAR);
            yearlyAchieved = getTotalAchievedForYear(year);
        }
        txtYearlyAch.setText(" " + yearlyAchieved);


        Intent intent = getIntent();
        int SecondTurnOverValue = intent.getIntExtra("TurnOver", -1);
        String Holiday = intent.getStringExtra("shopsHoliday");
        String HighPerDay = intent.getStringExtra("HighPerformace");
        growth = intent.getIntExtra("EdtGrowth", -1);


        updateYearlyAchievedDisplay();
        updateCurrentMonthAchieved();
        updateRecentDaysData(7);
        updateRecentDaysData(15);
        updateTodaysMetricsFromPrefs();


        SharedPreferences prefs = getSharedPreferences("ShopData", MODE_PRIVATE);

        if (SecondTurnOverValue == -1) {
            SecondTurnOverValue = prefs.getInt("TURNOVER", 0);
        }
        if (Holiday == null || Holiday.equals("Select Day")) {
            Holiday = prefs.getString("Shop_Holiday", "");
        }
        if (HighPerDay == null || HighPerDay.isEmpty()) {
            HighPerDay = prefs.getString("selected_days", "");
        }
        if (growth == -1) {
            growth = prefs.getInt("Growth", 0);
        }

        if (shopname == null || shopname.isEmpty()) {
            shopname = prefs.getString("ShopName", "");
        }




//        Log.d("GOTOACTIVITY", "ShopName is : " + shopname);
//
//        Log.d("GoToMAndD", "From SharedPref or Intent:");
//        Log.d("GoToMAndD", "TurnOver: " + SecondTurnOverValue);
//        Log.d("GoToMAndD", "TurnOver Result: " + Result);
//
//        Log.d("GoToMAndD", "Holiday: " + Holiday);
//        Log.d("GoToMAndD", "HighPerDay: " + HighPerDay);
//        Log.d("GoToMAndD", "Growth: " + growth);


        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("TURNOVER", SecondTurnOverValue);
        editor.putString("Shop_Holiday", Holiday);
        editor.putString("selected_days", HighPerDay);
        editor.putInt("Growth", growth);
        editor.putString("ShopName", shopname);
        editor.apply();


        SharedPreferences sharedPref = getSharedPreferences("shop_data", MODE_PRIVATE);
        String name = sharedPref.getString("shop_name", "");
        Holiday = sharedPref.getString("Shop_Holiday", "");
        HighPerDay = sharedPref.getString("selected_days", "");
        growth = sharedPref.getInt("editGrowth", 0);

        txtShopName.setText(name);
        txtYearTarget.setText(String.valueOf(growth));
        int MonthlyTarget = growth / 12;
        txtMonthTarget.setText(String.valueOf(MonthlyTarget));
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
        //int finalResult1 = Result;
        String finalHighPerDay = HighPerDay;
        int finalGrowth = growth;
        Map<String, Float> updatedMonthlyTargets = getUpdatedMonthlyTargetsFromPrefs(growth);

        HashMap<String, Float> monthTargetMap = new HashMap<>(updatedMonthlyTargets);
        btnDyes.setOnClickListener(v -> {
            Intent i = new Intent(GoToMAndD.this, YOYSecondActivity.class);
            i.putExtra("ShopHoliday", finalHoliday);
            i.putExtra("TurnYear", finalSecondTurnOverValue1);
            //i.putExtra("ResultTurnYear", finalResult1);
            i.putExtra("HighPerformance", finalHighPerDay);
            i.putExtra("Growth", finalGrowth);
            i.putExtra("MonthlyTarget", monthlyTarget);
            // Log.d("Monthly","Monthly target is : "+monthlyTarget);
            i.putExtra("MonthTargetMap", monthTargetMap);

            startActivityForResult(i, 101);



        });




        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            String finalShopname = shopname;
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {

                } else if (id == R.id.nav_tutorial) {
                    startActivity(new Intent(this, Tutorial.class));
                } else if (id == R.id.Set_Rev_Fig) {
                    Intent i = new Intent(GoToMAndD.this, SetRevenueFigures.class);
                    i.putExtra("ShopName", name);
                    startActivity(i);
                    Log.d("ShopName", "ShopName is : " + name);
                } else if (id == R.id.Holi_High_Day) {
                    Intent i = new Intent(GoToMAndD.this, SetHoliHighDay.class);
                    startActivity(i);
                } else if (id == R.id.Reset_Mpin) {
                    Intent i = new Intent(GoToMAndD.this, ResetMpin.class);
                    startActivity(i);
                } else if (id == R.id.Faq) {
                    Intent i = new Intent(GoToMAndD.this, FAQ.class);
                    startActivity(i);
                }
                else if(id == R.id.ExportFile)
                {
                    BackupHelper.exportBackup(GoToMAndD.this);
                }
                else if(id == R.id.ImportFile)
                {
                    Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("*/*");
                    startActivityForResult(i, 123);

                }else if(id == R.id.Calender)
                {
                    Intent i = new Intent(GoToMAndD.this,CalendarDays.class);
                    startActivity(i);
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });
        } else {
            Log.e("GoToMAndD", "NavigationView not found!");
        }

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






        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String todayKey = sdf.format(Calendar.getInstance().getTime());

        scheduleDailyNotification();
        //SharedPreferences sharedPref = getSharedPreferences("shop_data", MODE_PRIVATE);
        shopHoliday = sharedPref.getString("Shop_Holiday", "");
        publicHolidays = HolidayUtils.loadHolidayDates(this);
        highPerfPreDays = HolidayUtils.getPreHolidayHighPerformanceDates(publicHolidays, shopHoliday);
        Log.d("Publicdays","IS : "+publicHolidays);
        Log.d("Publicdays","IS : "+highPerfPreDays);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
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
        updateCurrentMonthAchieved();
        updateTodaysMetricsFromPrefs();

    }

    private int getTotalAchievedForYear(int year) {
        SharedPreferences prefs = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);
        int totalAchievedYearly = 0;


        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        for (String month : months) {
            String key = "data_" + month + "_" + year + "_Achieved";
            totalAchievedYearly += prefs.getInt(key, 0);

        }

        return totalAchievedYearly;
    }

    private void updateYearlyAchievedDisplay() {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int yearlyAchieved = getTotalAchievedForYear(currentYear);
        txtYearlyAch.setText("₹ " + yearlyAchieved);

        float percent = 0f;


        if (growth == 0) {
            SharedPreferences prefs = getSharedPreferences("ShopData", MODE_PRIVATE);
            growth = prefs.getInt("Result_TURNOVER", 0);
        }

        if (growth != 0) {
            percent = ((float) yearlyAchieved / growth) * 100f;
            YearlyAchPer.setText(String.format(Locale.US, "%.2f", percent) + "%");
        } else {
            YearlyAchPer.setText("N/A");
            Log.w("GoToMAndD", "Yearly Target (Result) is 0 — cannot calculate percentage");
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



    }

    private void updateCurrentMonthAchieved() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int monthIndex = Calendar.getInstance().get(Calendar.MONTH);
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        String currentMonth = monthNames[monthIndex];

        SharedPreferences prefs = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);
        int dataYear = (currentMonth.equals("Jan") || currentMonth.equals("Feb") || currentMonth.equals("Mar"))
                ? year + 1 : year;

        String key = "data_" + currentMonth + "_" + dataYear + "_Achieved";
        int achieved = prefs.getInt(key, 0);

        //monthlyTarget = prefs.getFloat("expected_" + currentMonth + "_" + dataYear, growth / 12f);
        monthlyTarget = getSafeFloat(prefs, "expected_" + currentMonth + "_" + dataYear, growth / 12f);

        float percent = (monthlyTarget > 0) ? (achieved * 100f / monthlyTarget) : 0f;

        MonthlyAch.setText("₹ " + achieved);
        txtMonthTarget.setText("₹ " + String.format("%.0f", monthlyTarget));
        MonthlyAchPer.setText(String.format(Locale.US, "%.2f ", percent) + "%");

        Log.d("MonthlyAch", "Month: " + currentMonth + " | Target: ₹" + monthlyTarget + " | Achieved: ₹" + achieved + " | Percent: " + percent);
    }

    private void updateRecentDaysData(int daysBack) {
        SharedPreferences prefs = getSharedPreferences("Shop Data", MODE_PRIVATE);

        float expectedTotal = getExpectedSumForPastDays(daysBack);
        int achievedTotal = 0;

        SimpleDateFormat keyFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        for (int i = 1; i <= daysBack; i++) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
            String dateStr = keyFormat.format(cal.getTime());

            int achieved = prefs.getInt("Achieved_" + dateStr, 0);
            achievedTotal += achieved;

            Log.d("DAYS_LOOP", dateStr + " | Achieved = ₹" + achieved);
        }

        float percent = (expectedTotal > 0) ? (achievedTotal * 100f / expectedTotal) : 0f;

        txtTargetRecentdays.setText("₹ " + String.format(Locale.US, "%.0f", expectedTotal));
        txtAchievedRecentdays.setText("₹ " + achievedTotal);
        txtAchievedPerRecentdays.setText(String.format(Locale.US, "%.2f", percent) + "%");

        Log.d("RecentStats", daysBack + " Days → Target: ₹" + expectedTotal +
                " | Achieved: ₹" + achievedTotal + " | %: " + percent);


    }


    private void drawLineChartABS(int daysBack) {
        LineChart lineChart = findViewById(R.id.lineChart);
        lineChart.clear();

        SharedPreferences prefs = getSharedPreferences("Shop Data", MODE_PRIVATE);

        List<Entry> absEntries = new ArrayList<>();
        List<String> xLabels = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM", Locale.US);

        for (int i = daysBack; i >= 1; i--) {
            calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -i);

            String dateKey = new SimpleDateFormat("dd-MM-yyyy", Locale.US).format(calendar.getTime());
            String label = dateFormat.format(calendar.getTime());
            xLabels.add(label);

            int achieved = prefs.getInt("Achieved_" + dateKey, 0);
            int qty = prefs.getInt("Quantity_" + dateKey, 0);
            int nob = prefs.getInt("NOB_" + dateKey, 0);

            int abs = (nob != 0) ? qty / nob : 0;

            absEntries.add(new Entry(daysBack - i, abs));

        }

        LineDataSet absSet = new LineDataSet(absEntries, "ABS");
        absSet.setColor(Color.RED);
        absSet.setCircleColor(Color.RED);


        LineData lineData = new LineData(absSet);
        lineChart.setData(lineData);

        // X axis labels
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.animateY(500);
        lineChart.invalidate();
    }


    private void drawLineChartATV(int daysBack) {
        LineChart lineChartATV = findViewById(R.id.lineChartATV);
        lineChartATV.clear();

        SharedPreferences prefs = getSharedPreferences("Shop Data", MODE_PRIVATE);

        List<Entry> atvEntries = new ArrayList<>();
        List<String> xLabels = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM", Locale.US);

        for (int i = daysBack; i >= 1; i--) {
            calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -i);

            String dateKey = new SimpleDateFormat("dd-MM-yyyy", Locale.US).format(calendar.getTime());
            String label = dateFormat.format(calendar.getTime());
            xLabels.add(label);

            int achieved = prefs.getInt("Achieved_" + dateKey, 0);
            int qty = prefs.getInt("Quantity_" + dateKey, 0);
            int nob = prefs.getInt("NOB_" + dateKey, 0);


            float atv = (nob != 0) ? (float) achieved / nob : 0;


            atvEntries.add(new Entry(daysBack - i, atv));

        }


        LineDataSet atvSet = new LineDataSet(atvEntries, "ATV");
        atvSet.setColor(Color.BLUE);
        atvSet.setCircleColor(Color.BLUE);
        atvSet.setLineWidth(2f);


        LineData lineData = new LineData(atvSet);
        lineChartATV.setData(lineData);

        // X axis labels
        XAxis xAxis = lineChartATV.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        lineChartATV.getAxisRight().setEnabled(false);
        lineChartATV.getDescription().setEnabled(false);
        lineChartATV.animateY(500);
        lineChartATV.invalidate();
    }


    private void drawLineChartASP(int daysBack) {
        LineChart lineChartASP = findViewById(R.id.lineChartASP);
        lineChartASP.clear();

        SharedPreferences prefs = getSharedPreferences("Shop Data", MODE_PRIVATE);

        List<Entry> aspEntries = new ArrayList<>();
        List<String> xLabels = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM", Locale.US);

        for (int i = daysBack; i >= 1; i--) {
            calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -i);

            String dateKey = new SimpleDateFormat("dd-MM-yyyy", Locale.US).format(calendar.getTime());
            String label = dateFormat.format(calendar.getTime());
            xLabels.add(label);

            int achieved = prefs.getInt("Achieved_" + dateKey, 0);
            int qty = prefs.getInt("Quantity_" + dateKey, 0);
            int nob = prefs.getInt("NOB_" + dateKey, 0);


            float asp = (qty != 0) ? (float) achieved / qty : 0;


            aspEntries.add(new Entry(daysBack - i, asp));
        }


        LineDataSet aspSet = new LineDataSet(aspEntries, "ASP");
        aspSet.setColor(Color.GREEN);
        aspSet.setCircleColor(Color.GREEN);
        LineData lineData = new LineData(aspSet);
        lineChartASP.setData(lineData);

        // X axis labels
        XAxis xAxis = lineChartASP.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        lineChartASP.getAxisRight().setEnabled(false);
        lineChartASP.getDescription().setEnabled(false);
        lineChartASP.animateY(500);
        lineChartASP.invalidate();
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

          //float expected = prefs.getFloat("Expected_" + dateStr, -1f);
            float expected = getSafeFloat(prefs, "Expected_" + dateStr, -1f);



            if (expected >= 0) {
                total += expected;
            }

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
           //float target = prefs.getFloat(key, -1f);
           float target = getSafeFloat(prefs, key, -1f);

            if (target == -1f) {
                target = defaultPerMonth;
            }

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
                this,
                0,
                dailyIntent,
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
                this,
                1,
                noonIntent,
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

}