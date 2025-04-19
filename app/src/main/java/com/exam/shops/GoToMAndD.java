package com.exam.shops;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class GoToMAndD extends AppCompatActivity {
    Button btnyes, btnDyes;

 //   CardView cardviewDaily;
    //txtDate, txtDay, txtExpect, txtAchived, txtAchievedPer, txtType,
    TextView  txtShopName,txtYearTarget,txtYearlyAch,YearlyAchPer,txtMonthTarget,MonthlyAch,MonthlyAchPer,txtTargetRecentdays,txtAchievedRecentdays,txtAchievedPerRecentdays;
    Spinner spinnerSR;
//    BarChart barChart;
//    PieChart pieChart;
int Result;
    LineChart lineChart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        drawLineChart(7);


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
                    drawLineChart(15);
                } else if (position == 2) {
                    updateRecentDaysData(7);
                    drawLineChart(7);
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
            yearlyAchieved = getTotalAchievedForYear(year); // fallback: recalculate
        }
        txtYearlyAch.setText(" " + yearlyAchieved);

        Log.d("GOTOACTIVITY", "ShopName is : " + shopname);


        Intent intent = getIntent();
        Result = intent.getIntExtra("ResultTurnover", -1);
        int SecondTurnOverValue = intent.getIntExtra("TurnOver", -1);
        String Holiday = intent.getStringExtra("shopsHoliday");
        String HighPerDay = intent.getStringExtra("HighPerformace");
        int growth = intent.getIntExtra("EdtGrowth", -1);


        if (Result <= 0) {
            SharedPreferences prefs = getSharedPreferences("ShopData", MODE_PRIVATE);
            Result = prefs.getInt("Result_TURNOVER", 0);
        }

        updateYearlyAchievedDisplay();
        updateCurrentMonthAchieved();
        updateRecentDaysData(7);
        updateRecentDaysData(15);
        updateTodaysMetricsFromPrefs();


        SharedPreferences prefs = getSharedPreferences("ShopData", MODE_PRIVATE);
        if (Result == -1) {
            Result = prefs.getInt("Result_TURNOVER", 0);
        }
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

        txtShopName.setText(shopname);
        txtYearTarget.setText(String.valueOf(Result));
        int MonthlyTarget = Result / 12;
        txtMonthTarget.setText(String.valueOf(MonthlyTarget));


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
        editor.putInt("Result_TURNOVER", Result);
        editor.putInt("TURNOVER", SecondTurnOverValue);
        editor.putString("Shop_Holiday", Holiday);
        editor.putString("selected_days", HighPerDay);
        editor.putInt("Growth", growth);
        editor.putString("ShopName", shopname);
        editor.apply();


        int finalSecondTurnOverValue = SecondTurnOverValue;
        int finalResult = Result;
        btnyes.setOnClickListener(v -> {
            Intent i = new Intent(GoToMAndD.this, YOYActivity.class);
            i.putExtra("TurnYear", finalSecondTurnOverValue);
            i.putExtra("ResultTurnYear", finalResult);
            startActivity(i);
        });

        String finalHoliday = Holiday;
        int finalSecondTurnOverValue1 = SecondTurnOverValue;
        int finalResult1 = Result;
        String finalHighPerDay = HighPerDay;
        int finalGrowth = growth;
        btnDyes.setOnClickListener(v -> {
            Intent i = new Intent(GoToMAndD.this, YOYSecondActivity.class);
            i.putExtra("ShopHoliday", finalHoliday);
            i.putExtra("TurnYear", finalSecondTurnOverValue1);
            i.putExtra("ResultTurnYear", finalResult1);
            i.putExtra("HighPerformance", finalHighPerDay);
            i.putExtra("Growth", finalGrowth);
            startActivityForResult(i, 101);
            //startActivity(i);
        });

    }
    @Override
    protected void onResume() {
        super.onResume();

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


        if (Result == 0) {
            SharedPreferences prefs = getSharedPreferences("ShopData", MODE_PRIVATE);
            Result = prefs.getInt("Result_TURNOVER", 0);
        }

        if (Result != 0) {
            percent = ((float) yearlyAchieved / Result) * 100f;
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
    }

    private void updateCurrentMonthAchieved() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int monthIndex = Calendar.getInstance().get(Calendar.MONTH);
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        String currentMonth = monthNames[monthIndex];

        SharedPreferences prefs = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);
        String key = "data_" + currentMonth + "_" + year + "_Achieved";
        int achieved = prefs.getInt(key, 0); // total achieved for current month

        // Calculate monthly target
        if (Result == 0) {
            SharedPreferences shopPrefs = getSharedPreferences("ShopData", MODE_PRIVATE);
            Result = shopPrefs.getInt("Result_TURNOVER", 0);
        }

        int monthlyTarget = Result / 12;
        float percent = (monthlyTarget > 0) ? (achieved * 100f / monthlyTarget) : 0f;



        MonthlyAch.setText("₹ " + achieved);
        txtMonthTarget.setText("₹ " + monthlyTarget);
        MonthlyAchPer.setText(String.format(Locale.US, "%.2f ", percent) + "%");

        Log.d("MonthlyAch", "Month: " + currentMonth + " | Achieved: ₹" + achieved + " | Percent: " + percent);
    }

    private void updateRecentDaysData(int daysBack) {
        SharedPreferences prefs = getSharedPreferences("Shop Data", MODE_PRIVATE); // this is where your daily data is
        SharedPreferences shopPrefs = getSharedPreferences("ShopData", MODE_PRIVATE); // config

        // Get configuration
        String holiday = shopPrefs.getString("Shop_Holiday", "Sunday");
        String highDays = shopPrefs.getString("selected_days", "");
        int growth = shopPrefs.getInt("Growth", 0);
        int turnover = shopPrefs.getInt("Result_TURNOVER", 0);

        List<String> highPerformDays = new ArrayList<>();
        if (!highDays.isEmpty()) {
            for (String d : highDays.split(",")) {
                highPerformDays.add(d.trim());
            }
        }

        int monthlyTarget = turnover / 12;
        float baseDailyTarget = monthlyTarget / 30f;
        float highDayTarget = baseDailyTarget * (1 + (growth / 100f));

        float expectedTotal = 0f;
        int achievedTotal = 0;

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        SimpleDateFormat keyFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        Calendar cal = Calendar.getInstance();

        for (int i = 1; i <= daysBack; i++) {
            cal.add(Calendar.DAY_OF_MONTH, -1); // go back one day at a time

            String dateStr = keyFormat.format(cal.getTime());
            String dayName = dayFormat.format(cal.getTime());

            // Decide the target
            float target;
            if (dayName.equalsIgnoreCase(holiday)) {
                target = 0;
            } else if (highPerformDays.contains(dayName)) {
                target = highDayTarget;
            } else {
                target = baseDailyTarget;
            }

            int achieved = prefs.getInt("Achieved_" + dateStr, 0);
            expectedTotal += target;
            achievedTotal += achieved;

            Log.d("DAYS_LOOP", dateStr + " | Target=" + target + " | Achieved=" + achieved);
        }

        float percent = (expectedTotal > 0) ? (achievedTotal * 100f / expectedTotal) : 0f;

        txtTargetRecentdays.setText("₹ " + String.format(Locale.US, "%.0f", expectedTotal));
        txtAchievedRecentdays.setText("₹ " + achievedTotal);
        txtAchievedPerRecentdays.setText(String.format(Locale.US, "%.2f", percent) + "%");

        Log.d("RecentStats", daysBack + " Days → Target: ₹" + expectedTotal +
                " | Achieved: ₹" + achievedTotal + " | %: " + percent);
    }


    private void drawLineChart(int daysBack) {
        LineChart lineChart = findViewById(R.id.lineChart);
        lineChart.clear();

        SharedPreferences prefs = getSharedPreferences("Shop Data", MODE_PRIVATE);

        List<Entry> absEntries = new ArrayList<>();
        List<Entry> atvEntries = new ArrayList<>();
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

            int abs = (nob != 0) ? qty / nob : 0;
            float atv = (nob != 0) ? (float) achieved / nob : 0;
            float asp = (qty != 0) ? (float) achieved / qty : 0;

            absEntries.add(new Entry(daysBack - i, abs));
            atvEntries.add(new Entry(daysBack - i, atv));
            aspEntries.add(new Entry(daysBack - i, asp));
        }

        LineDataSet absSet = new LineDataSet(absEntries, "ABS");
        absSet.setColor(Color.RED);
        absSet.setCircleColor(Color.RED);

        LineDataSet atvSet = new LineDataSet(atvEntries, "ATV");
        atvSet.setColor(Color.BLUE);
        atvSet.setCircleColor(Color.BLUE);
        atvSet.setLineWidth(2f);

        LineDataSet aspSet = new LineDataSet(aspEntries, "ASP");
        aspSet.setColor(Color.GREEN);
        aspSet.setCircleColor(Color.GREEN);

        LineData lineData = new LineData(absSet, atvSet, aspSet);
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


    private void updateTodaysMetricsFromPrefs() {
        SharedPreferences todayPrefs = getSharedPreferences("TodayData", MODE_PRIVATE);

        int abs = todayPrefs.getInt("today_abs", 0);
        float atv = todayPrefs.getFloat("today_atv", 0f);
        float asp = todayPrefs.getFloat("today_asp", 0f);

        Log.d("GoToActivity", "ABS: " + abs + " | ATV: " + atv + " | ASP: " + asp);

       ;
    }





}