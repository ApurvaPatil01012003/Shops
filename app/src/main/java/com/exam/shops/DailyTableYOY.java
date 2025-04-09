package com.exam.shops;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DailyTableYOY extends AppCompatActivity {
    Spinner spinnerMonth;
    LinearLayout tableContainer;
    String Holiday;
    TextView txtTurnOver;
    int SecondTurnOverValue;


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

        Holiday = getIntent().getStringExtra("ShopHoliday");
        Log.d("ShopHoliday","Shop holiday is : "+Holiday);

        SecondTurnOverValue = getIntent().getIntExtra("TurnYear", 0);

        SharedPreferences sharedPrefs=getSharedPreferences("Shop Data",MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPrefs.edit();
        editor.putString("Shop_Holiday",Holiday);
        editor.putFloat("TURNOVER",SecondTurnOverValue);




        spinnerMonth = findViewById(R.id.spinnerMonth);
        tableContainer = findViewById(R.id.tableContainer);
        txtTurnOver=findViewById(R.id.txtTurnOver);
        txtTurnOver.setText("Target = "+SecondTurnOverValue);

        List<String> monthsList = new ArrayList<>();
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, Calendar.APRIL, 1); // Start from April 2025

        for (int i = 0; i < 12; i++) {
            monthsList.add(displayFormat.format(calendar.getTime()));
            calendar.add(Calendar.MONTH, 1); // Move to next month
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, monthsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Calendar selectedCal = Calendar.getInstance();
                selectedCal.set(2025, Calendar.APRIL + position, 1); // Set to selected month

                generateDateRows(selectedCal);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }







    private void generateDateRows(Calendar startDate) {

        tableContainer.removeAllViews();

        // --- Header Row ---
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setPadding(8, 8, 8, 8);
        headerRow.setBackgroundColor(Color.LTGRAY);

        TextView headerDate = new TextView(this);
        headerDate.setText("Date");
        headerDate.setTextColor(Color.BLACK);
        headerDate.setTextSize(16);
        headerDate.setTypeface(null, android.graphics.Typeface.BOLD);
        headerDate.setLayoutParams(new LinearLayout.LayoutParams(220, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView headerDay = new TextView(this);
        headerDay.setText("Day");
        headerDay.setTextColor(Color.BLACK);
        headerDay.setTextSize(16);
        headerDay.setTypeface(null, android.graphics.Typeface.BOLD);
        headerDay.setLayoutParams(new LinearLayout.LayoutParams(220, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView headerType = new TextView(this);
        headerType.setText("Type");
        headerType.setTextColor(Color.BLACK);
        headerType.setTextSize(16);
        headerType.setTypeface(null, android.graphics.Typeface.BOLD);
        headerType.setLayoutParams(new LinearLayout.LayoutParams(220, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView headerExpect = new TextView(this);
        headerExpect.setText("Expect");
        headerExpect.setTextColor(Color.BLACK);
        headerExpect.setTextSize(16);
        headerExpect.setTypeface(null, android.graphics.Typeface.BOLD);
        headerExpect.setLayoutParams(new LinearLayout.LayoutParams(220, LinearLayout.LayoutParams.WRAP_CONTENT));


        TextView headerAchieved = new TextView(this);
        headerAchieved.setText("Achieved");
        headerAchieved.setTextColor(Color.BLACK);
        headerAchieved.setTextSize(16);
        headerAchieved.setTypeface(null, android.graphics.Typeface.BOLD);
        headerAchieved.setLayoutParams(new LinearLayout.LayoutParams(220, LinearLayout.LayoutParams.WRAP_CONTENT));

        headerRow.addView(headerDate);
        headerRow.addView(headerDay);
        headerRow.addView(headerType);
        headerRow.addView(headerExpect);
        headerRow.addView(headerAchieved);

        tableContainer.addView(headerRow);





        int year = startDate.get(Calendar.YEAR);
        int month = startDate.get(Calendar.MONTH);

        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);

        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
      int TotalDaysCount=0;
        int cnt=0;


        int workingDays = 0;
        for (int i = 1; i <= daysInMonth; i++) {
            cal.set(Calendar.DAY_OF_MONTH, i);
            String dayStr = dayFormat.format(cal.getTime());

            if (!Holiday.equals(dayStr)) {
                workingDays++;
            }
        }
        float monthlyTarget = (float) SecondTurnOverValue / 12f;
        float dailyTarget = monthlyTarget / workingDays;
        String formattedDailyTarget = String.format("%.2f", dailyTarget);


        for (int i = 1; i <= daysInMonth; i++) {
            cal.set(Calendar.DAY_OF_MONTH, i);

            String dateStr = dateFormat.format(cal.getTime());
            String dayStr = dayFormat.format(cal.getTime());
            String type;

            TotalDaysCount++;
            if (Holiday.equals(dayStr)) {
                type="Holiday";
                cnt++;

            }
            else {
                type="Working day";
            }


            // Create row
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(8, 8, 8, 8);

            TextView txtDate = new TextView(this);
            txtDate.setText(dateStr);
            txtDate.setTextColor(Color.BLACK);
            txtDate.setLayoutParams(new LinearLayout.LayoutParams(220, LinearLayout.LayoutParams.WRAP_CONTENT));

            TextView txtDay = new TextView(this);
            txtDay.setText(dayStr);
            txtDay.setTextColor(Color.BLACK);
            txtDay.setLayoutParams(new LinearLayout.LayoutParams(220, LinearLayout.LayoutParams.WRAP_CONTENT));

            TextView txtType = new TextView(this);
            txtType.setText(type);
            txtType.setTextColor(Color.BLACK);
            txtType.setLayoutParams(new LinearLayout.LayoutParams(220, LinearLayout.LayoutParams.WRAP_CONTENT));


            TextView txtExpect = new TextView(this);
            if (type.equals("Working day")) {
                txtExpect.setText(formattedDailyTarget);
            } else {
                txtExpect.setText("0");
            }
            txtExpect.setTextColor(Color.BLACK);
            txtExpect.setLayoutParams(new LinearLayout.LayoutParams(220, LinearLayout.LayoutParams.WRAP_CONTENT));




            row.addView(txtDate);
            row.addView(txtDay);
            row.addView(txtType);
            row.addView(txtExpect);




            tableContainer.addView(row);

        }

        Log.d("Cont","count is ; "+cnt);
        Log.d("TotalCount","Total count is : "+TotalDaysCount);





    }
}