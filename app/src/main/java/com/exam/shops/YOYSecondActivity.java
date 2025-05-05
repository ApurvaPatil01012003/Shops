package com.exam.shops;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.datepicker.MaterialDatePicker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class YOYSecondActivity extends AppCompatActivity {
    EditText et_date, edtAchieved, edtQty, edtNob;
    Button btnSave, btnDailyTable;
    TextView txtDailyTarget;
    private String key;
    private int updatedValue;
    private int updatedValueDaily;
    private float monthlyAchieved = 0f;
    private float monthlyPercent = 0f;
    private float monthlyTarget = 0f;
    float MonthTarget = 0f;

    String highPerDay;
    String Holiday;
    int HighPerGrowthPer = 10;
    int Growth_Per;

    List<String> publicHolidayList;
    HashMap<String, Float> monthTargetMap;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_yoysecond);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        et_date = findViewById(R.id.et_date);
        edtAchieved = findViewById(R.id.edtAchieved);
        edtQty = findViewById(R.id.edtQty);
        edtNob = findViewById(R.id.edtNob);
        btnSave = findViewById(R.id.btnSave);
        btnDailyTable = findViewById(R.id.btnDailyTable);
        txtDailyTarget = findViewById(R.id.txtDailyTarget);

        Holiday = getIntent().getStringExtra("ShopHoliday");
        if (!Holiday.isEmpty()) {
            Log.d("Holidqay", "Holiday is : " + Holiday);
        }

//        int SecondTurnOverValue = getIntent().getIntExtra("TurnYear", 0);
//        Log.d("TURNOVER", "turn over is : ; " + SecondTurnOverValue);

        int Result = getIntent().getIntExtra("ResultTurnYear", 0);
        Log.d("TURNOVER", "Result turn over is : ; " + Result);


        String High_Per_Day = getIntent().getStringExtra("HighPerformance");
        highPerDay = getIntent().getStringExtra("HighPerformance");

        Growth_Per = getIntent().getIntExtra("Growth", 0);

        Log.d("HIGHPERFORM", "HIGH DAY" + High_Per_Day);
        Log.d("Growth", "groth is : " + Growth_Per);


        Intent intent = getIntent();
        monthTargetMap = (HashMap<String, Float>) getIntent().getSerializableExtra("MonthTargetMap");
        if (monthTargetMap == null) {
            monthTargetMap = new HashMap<>();
        }

        for (Map.Entry<String, Float> entry : monthTargetMap.entrySet()) {
            String monthYear = entry.getKey();
            float target = entry.getValue();
            Log.d("YOYSecondActivity", "Month: " + monthYear + ", Target: ₹" + target);
        }

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .build();

        et_date.setOnClickListener(v -> {
            if (!datePicker.isAdded()) {
                datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
            }
        });


        datePicker.addOnPositiveButtonClickListener(selection -> {
            et_date.setText(datePicker.getHeaderText());

            SharedPreferences sharedPreferences = getSharedPreferences("Shop Data", MODE_PRIVATE);

            SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
            SimpleDateFormat saveFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.ENGLISH);

            String formattedDate = "";
            Calendar selectedCalendar = Calendar.getInstance();

            try {
                Date selectedDate = inputFormat.parse(datePicker.getHeaderText());
                formattedDate = saveFormat.format(selectedDate);
                selectedCalendar.setTime(selectedDate);


                String selectedDayName = dayFormat.format(selectedDate);
                List<String> publicHolidayList = loadHolidayDates();

                boolean isWeeklyHoliday = Holiday != null && Holiday.equalsIgnoreCase(selectedDayName);
                boolean isPublicHoliday = publicHolidayList.contains(formattedDate);

                if (isWeeklyHoliday && isPublicHoliday) {
                    Toast.makeText(this, "This date is both a Holiday and a Public Holiday", Toast.LENGTH_LONG).show();
                } else if (isWeeklyHoliday) {
                    Toast.makeText(this, "This is a Weekly Holiday: " + selectedDayName, Toast.LENGTH_SHORT).show();
                } else if (isPublicHoliday) {
                    Toast.makeText(this, "This is a Public Holiday", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            String expectedKey = "Expected_" + formattedDate;
            refreshDailyTargetsForCurrentMonth(selectedCalendar);

            float expectedTarget = sharedPreferences.getFloat(expectedKey, -1f);

            if (expectedTarget >= 0f) {
                txtDailyTarget.setText("Daily target : ₹" + String.format("%.2f", expectedTarget));
            } else {
                txtDailyTarget.setText("Daily target : ₹0");
            }
        });



        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = et_date.getText().toString();
                String Achievedstr = edtAchieved.getText().toString();
                String Quantitystr = edtQty.getText().toString();
                String nobstr = edtNob.getText().toString();
                int quantity = 0;
                int Achieved = 0;
                int Nob = 0;
                try {
                    quantity = Integer.parseInt(Quantitystr);
                    Achieved = Integer.parseInt(Achievedstr);
                    Nob = Integer.parseInt(nobstr);

                } catch (Exception e) {
                    //  edtQty.setError("Enter a valid number");
                    return;
                }


                if (!date.isEmpty() && !Achievedstr.isEmpty() && !Quantitystr.isEmpty() && !nobstr.isEmpty()) {
                    int ach = Integer.parseInt(Achievedstr);
                    int qty = Integer.parseInt(Quantitystr);
                    int nob = Integer.parseInt(nobstr);

                    SaveDataToSharedPref(date, ach, qty, nob);
                    SaveDailyDataToSharedPref(date, ach, qty, nob);


                    Toast.makeText(YOYSecondActivity.this, "Data saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(YOYSecondActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }


            }

        });

        btnDailyTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = et_date.getText().toString().trim();
                String Achievedstr = edtAchieved.getText().toString().trim();
                String Quantitystr = edtQty.getText().toString().trim();
                String nobstr = edtNob.getText().toString().trim();

                int achieved = 0, quantity = 0, nob = 0;

                if (!Achievedstr.isEmpty()) {
                    achieved = Integer.parseInt(Achievedstr);
                }

                if (!Quantitystr.isEmpty()) {
                    quantity = Integer.parseInt(Quantitystr);
                }

                if (!nobstr.isEmpty()) {
                    nob = Integer.parseInt(nobstr);
                }
//
//                if (!date.isEmpty()) {
//                    SaveDailyDataToSharedPref(date, achieved, quantity, nob);
//                }

                Intent intent = new Intent(YOYSecondActivity.this, DailyTableYOY.class);
                intent.putExtra("ShopHoliday", Holiday);
                intent.putExtra("ResultTurnYear", Result);
                //intent.putExtra("TurnYear", SecondTurnOverValue);
                intent.putExtra("Achived_Value", updatedValueDaily);
                intent.putExtra("Quantity", quantity);
                intent.putExtra("NOB", nob);
                intent.putExtra("HighPerDay", High_Per_Day);
                intent.putExtra("Growth", Growth_Per);
                intent.putExtra("MonthlyTarget", MonthTarget);
                Log.d("Monthly", "MonthTarget is : " + MonthTarget);
                //  startActivityForResult(intent, 102);
                startActivityForResult(intent, 101);
                ClearAllText();

//                Log.d("Updated_Daily", "Updated for daily: " + updatedValueDaily);
//                Log.d("Updated_Daily", "quantity for daily: " + quantity);
//                Log.d("Updated_Daily", "nob for daily: " + nob);
                // startActivity(intent);


            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(YOYSecondActivity.this, GoToMAndD.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                intent.putExtra("MonthlyTarget", Result / 12f);
                intent.putExtra("MonthlyAchieved", updatedValueDaily);
                SharedPreferences todayPrefs = getSharedPreferences("TodayData", MODE_PRIVATE);
               // float percent = todayPrefs.getFloat("today_percent", 0f);
                float percent = getSafeFloat(todayPrefs, "today_percent", 0f);

                intent.putExtra("MonthlyAchievedPercent", percent);

                if (key != null && !key.isEmpty()) {
                    intent.putExtra("data_key", key);
                    intent.putExtra("data_value", updatedValue);
                }


                setResult(RESULT_OK);

                // startActivity(intent);
                finish();
            }
        });
    }


    public void ClearAllText() {
        et_date.setText("");
        edtAchieved.setText("");
        edtQty.setText("");
        edtNob.setText("");
    }

    public void SaveDataToSharedPref(String date, int Achieved, int Quantity, int nob) {
        SharedPreferences sharedPreferences = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String[] parts = date.split(" ");
        String month = parts[1];
        String yearStr = parts[2];
        int year = Integer.parseInt(yearStr);

        String shortMonth = convertToShortMonth(month);

        if (shortMonth.equals("Jan") || shortMonth.equals("Feb") || shortMonth.equals("Mar")) {
            year = Integer.parseInt(yearStr);
            ;
        }

        String key = "data_" + shortMonth + "_" + year + "_Achieved";

        int prev = sharedPreferences.getInt(key, 0);
        int updatedValue = prev + Achieved;

        editor.putInt(key, updatedValue);
        editor.putString("last_selected_date", date);
        editor.apply();

        //Log.d("YOY_DATA", key + " = " + updatedValue);

        this.key = key;
        this.updatedValue = updatedValue;


        //  finish();
    }

    public void SaveDailyDataToSharedPref(String dateInput, int Achieved, int Quantity, int nob) {
        SharedPreferences sharedPreferences = getSharedPreferences("Shop Data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
        SimpleDateFormat saveFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);

        String formattedDate = "";
        try {
            formattedDate = saveFormat.format(inputFormat.parse(dateInput));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String key = "Achieved_" + formattedDate;
        String keyQty = "Quantity_" + formattedDate;
        String keyNob = "NOB_" + formattedDate;


        int prev = sharedPreferences.getInt(key, 0);
        int prevQty = sharedPreferences.getInt(keyQty, 0);
        int prevNob = sharedPreferences.getInt(keyNob, 0);

        updatedValueDaily = prev + Achieved;
        int updatedQty = prevQty + Quantity;
        int updatedNob = prevNob + nob;

        editor.putInt(key, updatedValueDaily);
        editor.putInt(keyQty, updatedQty);
        editor.putInt(keyNob, updatedNob);

        editor.putString("last_selected_date", formattedDate);
        editor.apply();

        Log.d("YOY_DATA_ForDaily", key + " = " + updatedValueDaily);
        Log.d("YOY_DATA_ForDaily", keyQty + " = " + Quantity);
        Log.d("YOY_DATA_ForDaily", keyNob + " = " + nob);

        this.key = key;
        this.updatedValueDaily = updatedValueDaily;


        //chage the Achived and percentage in cardview of GotoActivity
        String todayStr = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(System.currentTimeMillis());
        if (formattedDate.equals(todayStr)) {
            SharedPreferences todayPref = getSharedPreferences("TodayData", MODE_PRIVATE);
            SharedPreferences.Editor todayEditor = todayPref.edit();

            String expectedStr = todayPref.getString("today_expected", "0");
            float expected = 0f;
            try {
                expected = Float.parseFloat(expectedStr);
            } catch (Exception e) {
                expected = 0f;
            }

            float percentage = (expected != 0f) ? ((float) updatedValueDaily / expected) * 100f : 0f;

            todayEditor.putInt("today_achieved", updatedValueDaily);
            todayEditor.putFloat("today_percent", percentage);
            todayEditor.apply();

            Log.d("TodayData", "Only updated Achieved=" + updatedValueDaily + ", Percentage=" + percentage);
        }


    }

    private String convertToShortMonth(String fullMonth) {
        switch (fullMonth) {
            case "January":
                return "Jan";
            case "February":
                return "Feb";
            case "March":
                return "Mar";
            case "April":
                return "Apr";
            case "May":
                return "May";
            case "June":
                return "Jun";
            case "July":
                return "Jul";
            case "August":
                return "Aug";
            case "September":
                return "Sep";
            case "October":
                return "Oct";
            case "November":
                return "Nov";
            case "December":
                return "Dec";
            default:
                return fullMonth;
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == 101) {
                monthlyAchieved = data.getFloatExtra("MonthlyAchieved", 0f);
                monthlyPercent = data.getFloatExtra("MonthlyAchievedPercent", 0f);

                monthlyTarget = getIntent().getIntExtra("ResultTurnYear", 0) / 12f;

                Toast.makeText(this,
                        "Month Total: ₹" + monthlyAchieved +
                                "\nAchieved: " + String.format("%.2f", monthlyPercent) + "%",
                        Toast.LENGTH_LONG
                ).show();

            }

            data.putExtra("data_updated", true);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    private void generateDateRowsForYOYSecondActivity(Calendar startDate) {
        SharedPreferences prefs = getSharedPreferences("Shop Data", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        List<String> publicHolidayDates = loadHolidayDates();

        int year = startDate.get(Calendar.YEAR);
        int month = startDate.get(Calendar.MONTH);
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);


        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        String currentMonthName = monthNames[month];

        String monthYearKey = currentMonthName + "_" + year;
        float monthlyTarget = monthTargetMap.getOrDefault(monthYearKey, MonthTarget);


        Log.d("CorrectMonthTarget", "Using target for " + monthYearKey + " = ₹" + monthlyTarget);

        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());

        List<String> highPerfDates = new ArrayList<>();
        if (highPerDay != null && !highPerDay.isEmpty()) {
            for (String day : highPerDay.split(",")) {
                highPerfDates.add(day.trim());
            }
        }

        List<String> prePublicHighPerfDates = new ArrayList<>();
        for (String holidayDateStr : publicHolidayDates) {
            try {
                Calendar holidayCal = Calendar.getInstance();
                holidayCal.setTime(dateFormat.parse(holidayDateStr));

                for (int i = 1; i <= 7; i++) {
                    Calendar preCal = (Calendar) holidayCal.clone();
                    preCal.add(Calendar.DAY_OF_MONTH, -i);

                    String preDateStr = dateFormat.format(preCal.getTime());
                    String preDayStr = dayFormat.format(preCal.getTime());

                    if (!Holiday.equals(preDayStr)) {
                        prePublicHighPerfDates.add(preDateStr);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int workingDays = 0;
        int highPerfDays = 0;

        for (int i = 1; i <= daysInMonth; i++) {
            cal.set(Calendar.DAY_OF_MONTH, i);
            String dateStr = dateFormat.format(cal.getTime());
            String dayStr = dayFormat.format(cal.getTime());

            if (!Holiday.equals(dayStr) && !publicHolidayDates.contains(dateStr)) {
                workingDays++;
            }
            if (highPerfDates.contains(dayStr) || prePublicHighPerfDates.contains(dateStr)) {
                highPerfDays++;
            }
        }

        Log.d("YOYSecond", "Working days: " + workingDays);
        Log.d("YOYSecond", "High Perf days: " + highPerfDays);

        float growthMultiplier = 1 + (HighPerGrowthPer / 100f);

        float baseDailyTarget = monthlyTarget / (workingDays + highPerfDays * (growthMultiplier - 1));

        List<Float> rawTargets = new ArrayList<>();
        List<String> dateList = new ArrayList<>();

        cal.set(year, month, 1);

        for (int i = 1; i <= daysInMonth; i++) {
            cal.set(Calendar.DAY_OF_MONTH, i);
            String dateStr = dateFormat.format(cal.getTime());
            String dayStr = dayFormat.format(cal.getTime());


            float target;
            if (Holiday.equals(dayStr) || publicHolidayDates.contains(dateStr)) {
                target = 0f;

            } else if (highPerfDates.contains(dayStr) || prePublicHighPerfDates.contains(dateStr)) {
                target = baseDailyTarget * growthMultiplier;
            } else {
                target = baseDailyTarget;
            }

            rawTargets.add(target);
            dateList.add(dateStr);

        }

        float totalSum = 0f;
        for (float val : rawTargets) {
            totalSum += val;
        }

        float scalingFactor = (totalSum != 0) ? (monthlyTarget / totalSum) : 1f;

        for (int i = 0; i < rawTargets.size(); i++) {
            rawTargets.set(i, rawTargets.get(i) * scalingFactor);
        }

        for (int i = 0; i < rawTargets.size(); i++) {
            rawTargets.set(i, Math.round(rawTargets.get(i) * 100f) / 100f);
        }

        float finalSum = 0f;
        for (float val : rawTargets) {
            finalSum += val;
        }

        float diff = monthlyTarget - finalSum;

        for (int i = rawTargets.size() - 1; i >= 0; i--) {
            if (rawTargets.get(i) != 0f) {
                rawTargets.set(i, rawTargets.get(i) + diff);
                break;
            }
        }

        for (int i = 0; i < dateList.size(); i++) {
            String dateKey = dateList.get(i);
            editor.putFloat("Expected_" + dateKey, rawTargets.get(i));
            Log.d("DailyTargetGen", "Date: " + dateKey + " Target: " + rawTargets.get(i));
        }
        editor.apply();
        SharedPreferences yoyPrefs = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);
        SharedPreferences.Editor yoyEditor = yoyPrefs.edit();
        String monthKey = "last_month_target_" + startDate.get(Calendar.MONTH) + "_" + startDate.get(Calendar.YEAR);
        yoyEditor.putFloat(monthKey, monthlyTarget);
        yoyEditor.apply();


        Toast.makeText(this, "Daily Targets Generated!", Toast.LENGTH_SHORT).show();


    }
    private void refreshDailyTargetsForCurrentMonth(Calendar cal) {
        SharedPreferences prefs = getSharedPreferences("Shop Data", MODE_PRIVATE);
        SharedPreferences yoyPrefs = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        boolean hasManualEdits = false;

        Calendar tempCal = Calendar.getInstance();
        tempCal.set(year, month, 1);

        for (int i = 1; i <= daysInMonth; i++) {
            tempCal.set(Calendar.DAY_OF_MONTH, i);
            String dateStr = dateFormat.format(tempCal.getTime());

            boolean edited = prefs.getBoolean("edited_" + dateStr, false);
            if (edited) {
                hasManualEdits = true;
                break;
            }
        }


        String monthKey = "last_month_target_" + month + "_" + year;
       // float lastSavedTarget = yoyPrefs.getFloat(monthKey, -1f);
        float lastSavedTarget = getSafeFloat(yoyPrefs, monthKey, -1f);

        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        String monthName = monthNames[month];
        float currentMonthTarget = monthTargetMap.getOrDefault(monthName + "_" + year, MonthTarget);

        boolean targetChanged = Math.abs(currentMonthTarget - lastSavedTarget) > 1f;

        if (!hasManualEdits || targetChanged) {
            Log.d("RefreshDaily", "No manual edits found or target changed, refreshing data...");
            clearExpectedDataForMonth(cal);
            generateDateRowsForYOYSecondActivity(cal);
        } else {
            Log.d("RefreshDaily", "Manual edits found and no target change, skipping refresh.");
        }
    }



    private List<String> loadHolidayDates() {
        List<String> holidays = new ArrayList<>();
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        try {
            InputStream is = getAssets().open("public_holidays.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String jsonStr = new String(buffer, "UTF-8");
            JSONObject jsonObj = new JSONObject(jsonStr);
            JSONArray array = jsonObj.getJSONArray("public_holidays");

            for (int i = 0; i < array.length(); i++) {
                String rawDate = array.getString(i);
                try {
                    String formatted = outputFormat.format(inputFormat.parse(rawDate));
                    holidays.add(formatted);
                    Log.d("PUBLIC_HOLIDAY_PARSED", formatted);
                } catch (Exception e) {
                    Log.e("DATE_PARSE_ERROR", "Invalid date: " + rawDate);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return holidays;
    }
      private void clearExpectedDataForMonth(Calendar cal) {
        SharedPreferences prefs = getSharedPreferences("Shop Data", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        Calendar tempCal = Calendar.getInstance();
        tempCal.set(year, month, 1);

        for (int i = 1; i <= daysInMonth; i++) {
            tempCal.set(Calendar.DAY_OF_MONTH, i);
            String dateStr = dateFormat.format(tempCal.getTime());

            editor.remove("Expected_" + dateStr);
            // Optional: also clear Achieved, Quantity, NOB if you want to fully reset
            // editor.remove("Achieved_" + dateStr);
            // editor.remove("Quantity_" + dateStr);
            // editor.remove("NOB_" + dateStr);
        }

        editor.apply();
        Log.d("ClearData", "Cleared expected data for month: " + (month + 1) + "/" + year);
    }


    private float getSafeFloat(SharedPreferences prefs, String key, float defaultValue) {
        try {
            return prefs.getFloat(key, defaultValue);
        } catch (ClassCastException e) {
            Object value = prefs.getAll().get(key);
            if (value instanceof Integer) {
                return ((Integer) value).floatValue();
            } else if (value instanceof String) {
                try {
                    return Float.parseFloat((String) value);
                } catch (NumberFormatException ex) {
                    return defaultValue;
                }
            } else {
                return defaultValue;
            }
        }
    }





}