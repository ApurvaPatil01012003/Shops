package com.exam.shops;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DailyTableYOY extends AppCompatActivity {
    Spinner spinnerMonth;
    LinearLayout tableContainer;
    String Holiday;
    TextView txtTurnOver;
    int SecondTurnOverValue;

    int achievedValue;
    int quantities;
    int nobValue;
    String highPerDay;
    int growthPer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_daily_table_yoy);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        spinnerMonth = findViewById(R.id.spinnerMonth);
        tableContainer = findViewById(R.id.tableContainer);

        Holiday = getIntent().getStringExtra("ShopHoliday");
        SecondTurnOverValue = getIntent().getIntExtra("TurnYear", 0);
        achievedValue = getIntent().getIntExtra("Achived_Value", 0);
        quantities = getIntent().getIntExtra("Quantity", 0);
        nobValue = getIntent().getIntExtra("NOB", 0);
        highPerDay = getIntent().getStringExtra("HighPerDay");
        growthPer = getIntent().getIntExtra("Growth", 0);



        Log.d("High", "High Performance Day : " + highPerDay);
        Log.d("High", "High Performance Day : " + growthPer);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getFincialYearMonths());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        String currentMonthLabel = getCurrentMonthLabel();
        int defaultPosition = adapter.getPosition(currentMonthLabel);
        spinnerMonth.setSelection(defaultPosition);

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                String[] parts = selected.split(" ");
                if (parts.length == 2) {
                    String monthStr = parts[0];
                    int year = Integer.parseInt(parts[1]);

                    int monthIndex = getMonthIndex(monthStr);
                    Calendar selectedMonth = Calendar.getInstance();
                    selectedMonth.set(Calendar.YEAR, year);
                    selectedMonth.set(Calendar.MONTH, monthIndex);
                    selectedMonth.set(Calendar.DAY_OF_MONTH, 1);
                    generateDateRows(selectedMonth);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }



    public List<String> getFincialYearMonths() {
        List<String> monthList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        int startYear;
        if (month >= calendar.APRIL) {
            startYear = year;
        } else {
            startYear = year - 1;
        }

        String MonthNames[] = {"April", "May", "June", "July", "August", "September", "October", "November", "December",
                "January", "February", "March"};

        for (int i = 0; i < MonthNames.length; i++) {
            String label;
            if (i < 9) {
                label = MonthNames[i] + " " + startYear;
            } else {
                label = MonthNames[i] + " " + (startYear + 1);
            }

            monthList.add(label);
        }
        return monthList;

    }

    private int getMonthIndex(String monthName) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM", Locale.ENGLISH);
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(monthName));
            return cal.get(Calendar.MONTH);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private String getCurrentMonthLabel() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        if (month < Calendar.APRIL) {
            year = year - 1;
        }

        String[] MonthNames = {"April", "May", "June", "July", "August", "September", "October", "November", "December",
                "January", "February", "March"};

        String currentMonthName;
        if (month >= Calendar.APRIL && month <= Calendar.DECEMBER) {
            currentMonthName = MonthNames[month - Calendar.APRIL];
            return currentMonthName + " " + year;
        } else {
            currentMonthName = MonthNames[month + 9];
            return currentMonthName + " " + (year + 1);
        }
    }

//    private void generateDateRows(Calendar startDate) {
//        tableContainer.removeAllViews();
//
//        int year = startDate.get(Calendar.YEAR);
//        int month = startDate.get(Calendar.MONTH);
//        Calendar cal = Calendar.getInstance();
//        cal.set(year, month, 1);
//
//        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
//        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
//        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
//
//        int workingDays = 0;
//        for (int i = 1; i <= daysInMonth; i++) {
//            cal.set(Calendar.DAY_OF_MONTH, i);
//            if (!Holiday.equals(dayFormat.format(cal.getTime()))) {
//                workingDays++;
//            }
//        }
//
//
//        for (int i = 1; i <= daysInMonth; i++) {
//            cal.set(Calendar.DAY_OF_MONTH, i);
//            String dateStr = dateFormat.format(cal.getTime());
//            String dayStr = dayFormat.format(cal.getTime());
//            String type;//= Holiday.equals(dayStr) ? "Holiday" : "Working day";
//            if (Holiday.equals(dayStr)) {
//                type = "Holiday";
//            } else if (highPerDay != null && highPerDay.equals(dayStr)) {
//                type = "High Performance Day";
//
//
//
//            } else {
//                type = "Working Day";
//
//
//            }
//            int Growth = growthPer / 100;
//
//            float monthlyTarget = (float) SecondTurnOverValue / 12f;
//            float dailyTarget = monthlyTarget / workingDays;
//            String formattedDailyTarget = String.format("%.2f", dailyTarget);
//
//            //try for high performance but not done.
//            if(highPerDay != null) {
//                if (highPerDay.equals(dayStr)) {
//                    dailyTarget = (monthlyTarget / workingDays) + Growth;
//                    formattedDailyTarget = String.format("%.2f", dailyTarget);
//                } else {
//                    dailyTarget = (monthlyTarget / workingDays) - Growth;
//                    formattedDailyTarget = String.format("%.2f", dailyTarget);
//                }
//            }else {
//                monthlyTarget = (float) SecondTurnOverValue / 12f;
//                dailyTarget = monthlyTarget / workingDays;
//                formattedDailyTarget = String.format("%.2f", dailyTarget);
//            }
//
//
//
//
//            int achievedForDate = getSharedPreferences("Shop Data", MODE_PRIVATE).getInt("Achieved_" + dateStr, 0);
//            int quantityForDate = getSharedPreferences("Shop Data", MODE_PRIVATE).getInt("Quantity_" + dateStr, 0);
//            int NOBForDate = getSharedPreferences("Shop Data", MODE_PRIVATE).getInt("NOB_" + dateStr, 0);
//            int ABS = (NOBForDate != 0) ? quantityForDate / NOBForDate : 0;
//            float ATV = (NOBForDate != 0) ? (float) achievedForDate / NOBForDate : 0;
//            float ASP = (quantityForDate != 0) ? (float) achievedForDate / quantityForDate : 0;
//            float percentage = (type.equals("Working Day") && dailyTarget != 0) ? (achievedForDate / dailyTarget) * 100f : 0;
//
//            // Inflate card view
//            View cardView = getLayoutInflater().inflate(R.layout.card_daily_info, null);
//
//            ((TextView) cardView.findViewById(R.id.txtDate)).setText("Date: " + dateStr);
//            ((TextView) cardView.findViewById(R.id.txtDay)).setText("Day: " + dayStr);
//            ((TextView) cardView.findViewById(R.id.txtAchieved)).setText("Achieved: " + achievedForDate);
//            ((TextView) cardView.findViewById(R.id.txtType)).setText("Type: " + type);
//            ((TextView) cardView.findViewById(R.id.txtExpect)).setText(
//                    "Expected: " + ((type.equals("Working Day") || type.equals("High Performance Day")) ? formattedDailyTarget : "0"));
//            ((TextView) cardView.findViewById(R.id.txtAchievedPer)).setText("Achieved %: " + String.format("%.2f%%", percentage));
//            ((TextView) cardView.findViewById(R.id.txtQuantity)).setText("Quantity: " + quantityForDate);
//            ((TextView) cardView.findViewById(R.id.txtNOB)).setText("NOB: " + NOBForDate);
//            ((TextView) cardView.findViewById(R.id.txtABS)).setText("ABS: " + ABS);
//            ((TextView) cardView.findViewById(R.id.txtATV)).setText("ATV: " + String.format(Locale.US, "%.2f", ATV));
//            ((TextView) cardView.findViewById(R.id.txtASP)).setText("ASP: " + String.format(Locale.US, "%.2f", ASP));
//
//            LinearLayout detailsLayout = cardView.findViewById(R.id.detailsLayout);
//            cardView.setOnClickListener(v -> {
//                detailsLayout.setVisibility(detailsLayout.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
//            });
//
//            tableContainer.addView(cardView);
//        }
//    }



    private void generateDateRows(Calendar startDate) {
        tableContainer.removeAllViews();

        int year = startDate.get(Calendar.YEAR);
        int month = startDate.get(Calendar.MONTH);
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);

        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());


        List<String> highPerfDaysList = new ArrayList<>();
        if (highPerDay != null && !highPerDay.isEmpty()) {
            for (String day : highPerDay.split(",")) {
                highPerfDaysList.add(day.trim());
            }
        }


        int workingDays = 0;
        int highPerfDays = 0;

        for (int i = 1; i <= daysInMonth; i++) {
            cal.set(Calendar.DAY_OF_MONTH, i);
            String dayStr = dayFormat.format(cal.getTime());
            if (!Holiday.equals(dayStr)) {
                workingDays++;
            }
            if (highPerfDaysList.contains(dayStr)) {
                highPerfDays++;
            }
        }
        Log.d("DailyTableYOY","Working days : "+workingDays);
        Log.d("DailyTableYOY","High per day : "+highPerfDays);

        float monthlyTarget = (float) SecondTurnOverValue / 12f;
        float baseTargetDays = workingDays;
        float growthMultiplier = 1 + (growthPer / 100f);
       float highPerfTarget = 0f;

        if (highPerfDays > 0) {
            float baseDailyTarget = monthlyTarget / (workingDays + highPerfDays * (growthMultiplier - 1));
            highPerfTarget = baseDailyTarget * growthMultiplier;
        }

        float dailyTarget = 0f;
        if (highPerfDays > 0) {
            dailyTarget = monthlyTarget / (workingDays + highPerfDays * (growthMultiplier - 1));
        } else {
            dailyTarget = monthlyTarget / workingDays;
        }

        for (int i = 1; i <= daysInMonth; i++) {
            cal.set(Calendar.DAY_OF_MONTH, i);
            String dateStr = dateFormat.format(cal.getTime());
            String dayStr = dayFormat.format(cal.getTime());

            String type;
            float targetForDay;

            if (Holiday.equals(dayStr)) {
                type = "Holiday";
                targetForDay = 0f;
            }  else if (highPerfDaysList.contains(dayStr)) {
                type = "High Performance Day";
                targetForDay = dailyTarget * growthMultiplier;
            } else {
                type = "Working Day";
                targetForDay = dailyTarget;
            }

            String formattedDailyTarget = String.format("%.2f", targetForDay);

            // Fetch performance data
            SharedPreferences prefs = getSharedPreferences("Shop Data", MODE_PRIVATE);
            int achieved = prefs.getInt("Achieved_" + dateStr, 0);
            int quantity = prefs.getInt("Quantity_" + dateStr, 0);
            int NOB = prefs.getInt("NOB_" + dateStr, 0);

            int ABS = (NOB != 0) ? quantity / NOB : 0;
            float ATV = (NOB != 0) ? (float) achieved / NOB : 0;
            float ASP = (quantity != 0) ? (float) achieved / quantity : 0;
            float percentage = (targetForDay != 0) ? (achieved / targetForDay) * 100f : 0f;

            // Inflate card view
            View cardView = getLayoutInflater().inflate(R.layout.card_daily_info, null);

            ((TextView) cardView.findViewById(R.id.txtDate)).setText("Date: " + dateStr);
            ((TextView) cardView.findViewById(R.id.txtDay)).setText("Day: " + dayStr);
            ((TextView) cardView.findViewById(R.id.txtAchieved)).setText("Achieved: " + achieved);
            ((TextView) cardView.findViewById(R.id.txtType)).setText("Type: " + type);
            ((TextView) cardView.findViewById(R.id.txtExpect)).setText("Expected: " + formattedDailyTarget);
            ((TextView) cardView.findViewById(R.id.txtAchievedPer)).setText("Achieved %: " + String.format("%.2f%%", percentage));
            ((TextView) cardView.findViewById(R.id.txtQuantity)).setText("Quantity: " + quantity);
            ((TextView) cardView.findViewById(R.id.txtNOB)).setText("NOB: " + NOB);
            ((TextView) cardView.findViewById(R.id.txtABS)).setText("ABS: " + ABS);
            ((TextView) cardView.findViewById(R.id.txtATV)).setText("ATV: " + String.format(Locale.US, "%.2f", ATV));
            ((TextView) cardView.findViewById(R.id.txtASP)).setText("ASP: " + String.format(Locale.US, "%.2f", ASP));

            LinearLayout detailsLayout = cardView.findViewById(R.id.detailsLayout);
            cardView.setOnClickListener(v -> {
                detailsLayout.setVisibility(detailsLayout.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
            });

            tableContainer.addView(cardView);
        }
    }


}