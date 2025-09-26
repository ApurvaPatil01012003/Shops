package com.exam.shops;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class YOYSecondActivity extends AppCompatActivity {
    EditText et_date, edtAchieved, edtQty, edtNob;
    MaterialButton btnSave, btnDailyTable;

    private RecyclerView recyclerRecentEntries;
    TextView txtDailyTarget, btnViewAll, txtNoData;
    private String key;
    private float updatedValue;        // was int
    private float updatedValueDaily;   // was int
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
        btnViewAll = findViewById(R.id.btnViewAll);
        recyclerRecentEntries = findViewById(R.id.recyclerRecentEntries);
        txtNoData = findViewById(R.id.txtNoData);

        recyclerRecentEntries.setLayoutManager(new LinearLayoutManager(this));
        btnSave.setOnClickListener(v -> saveEntry());
        loadRecentEntries();

        Holiday = getIntent().getStringExtra("ShopHoliday");
        if (Holiday != null && !Holiday.isEmpty()) {
            Log.d("Holidqay", "Holiday is : " + Holiday);
        }

        int Result = getIntent().getIntExtra("ResultTurnYear", 0);
        Log.d("TURNOVER", "Result turn over is : ; " + Result);

        highPerDay = getIntent().getStringExtra("HighPerformance");
        Growth_Per = getIntent().getIntExtra("Growth", 0);

        monthTargetMap = (HashMap<String, Float>) getIntent().getSerializableExtra("MonthTargetMap");
        if (monthTargetMap == null) monthTargetMap = new HashMap<>();

        for (Map.Entry<String, Float> entry : monthTargetMap.entrySet()) {
            String monthYear = entry.getKey();
            float target = entry.getValue();
            Log.d("YOYSecondActivity", "Month: " + monthYear + ", Target: ₹" + target);
        }

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .build();

        et_date.setOnClickListener(v -> {
            if (!datePicker.isAdded()) datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
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
                txtDailyTarget.setText("Daily target : ₹" + String.format(Locale.getDefault(), "%.2f", expectedTarget));
            } else {
                txtDailyTarget.setText("Daily target : ₹0");
            }
        });

        btnDailyTable.setOnClickListener(v -> {
            String date = et_date.getText().toString().trim();
            String Achievedstr = edtAchieved.getText().toString().trim();
            String Quantitystr = edtQty.getText().toString().trim();
            String nobstr = edtNob.getText().toString().trim();

            float achieved = 0f, quantity = 0f, nob = 0f;
            if (!Achievedstr.isEmpty()) achieved = Float.parseFloat(Achievedstr);
            if (!Quantitystr.isEmpty()) quantity = Float.parseFloat(Quantitystr);
            if (!nobstr.isEmpty()) nob = Float.parseFloat(nobstr);

            if (!date.isEmpty()) {
                SaveDataToSharedPref(date, achieved, quantity, nob);
                SaveDailyDataToSharedPref(date, achieved, quantity, nob);
            }

            Intent i = new Intent(YOYSecondActivity.this, DailyTableYOY.class);
            i.putExtra("ShopHoliday", Holiday);
            i.putExtra("ResultTurnYear", Result);
            i.putExtra("Achived_Value", updatedValueDaily); // now float
            i.putExtra("Quantity", quantity);               // now float
            i.putExtra("NOB", nob);                         // now float
            i.putExtra("HighPerDay", highPerDay);
            i.putExtra("Growth", Growth_Per);
            i.putExtra("MonthlyTarget", MonthTarget);
            startActivityForResult(i, 101);

            ClearAllText();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(YOYSecondActivity.this, GoToMAndD.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                intent.putExtra("MonthlyTarget", Result / 12f);
                intent.putExtra("MonthlyAchieved", updatedValueDaily);
                SharedPreferences todayPrefs = getSharedPreferences("TodayData", MODE_PRIVATE);
                float percent = getSafeFloat(todayPrefs, "today_percent", 0f);

                intent.putExtra("MonthlyAchievedPercent", percent);

                if (key != null && !key.isEmpty()) {
                    intent.putExtra("data_key", key);
                    intent.putExtra("data_value", updatedValue);
                }

                setResult(RESULT_OK);
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

    // ===== FLOAT VERSION =====
    public void SaveDataToSharedPref(String date, float Achieved, float Quantity, float nob) {
        SharedPreferences sharedPreferences = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String[] parts = date.split(" ");
        String month = parts[1];
        String yearStr = parts[2];
        int year = Integer.parseInt(yearStr);

        String shortMonth = convertToShortMonth(month);
        if (shortMonth.equals("Jan") || shortMonth.equals("Feb") || shortMonth.equals("Mar")) {
            year = Integer.parseInt(yearStr);
        }

        String key = "data_" + shortMonth + "_" + year + "_Achieved";

        float prev = getSafeFloat(sharedPreferences, key, 0f);
        float updatedValue = prev + Achieved;

        editor.putFloat(key, updatedValue);
        editor.putString("last_selected_date", date);
        editor.apply();

        this.key = key;
        this.updatedValue = updatedValue;
        this.updatedValueDaily = Achieved; // keep last added daily value if you were using it
    }

    // ===== FLOAT VERSION =====
    private void SaveDailyDataToSharedPref(String dateInput, float achieved, float qty, float nob) {
        if (dateInput == null || dateInput.trim().isEmpty()) {
            Log.e("SaveDailyData", " Cannot save: date is empty");
            return;
        }

        // Normalize date to dd-MM-yyyy
        String normalizedDate;
        try {
            Date date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(dateInput);
            normalizedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date);
        } catch (Exception e) {
            Log.w("SaveDailyData", "⚠️ Date parsing failed, saving raw input: " + dateInput);
            normalizedDate = dateInput;
        }

        SharedPreferences prefs = getSharedPreferences("Shop Data", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Get old values as float (handles old int data too)
        float prevAchieved = getSafeFloat(prefs, "Achieved_" + normalizedDate, 0f);
        float prevQty = getSafeFloat(prefs, "Quantity_" + normalizedDate, 0f);
        float prevNob = getSafeFloat(prefs, "NOB_" + normalizedDate, 0f);

        // Add and save as float
        editor.putFloat("Achieved_" + normalizedDate, prevAchieved + achieved);
        editor.putFloat("Quantity_" + normalizedDate, prevQty + qty);
        editor.putFloat("NOB_" + normalizedDate, prevNob + nob);
        editor.putBoolean("from_yoy_" + normalizedDate, true);
        editor.putBoolean("expected_only_" + normalizedDate, false);


        editor.apply();
    }

    private String convertToShortMonth(String fullMonth) {
        switch (fullMonth) {
            case "January": return "Jan";
            case "February": return "Feb";
            case "March": return "Mar";
            case "April": return "Apr";
            case "May": return "May";
            case "June": return "Jun";
            case "July": return "Jul";
            case "August": return "Aug";
            case "September": return "Sep";
            case "October": return "Oct";
            case "November": return "Nov";
            case "December": return "Dec";
            default: return fullMonth;
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
                                "\nAchieved: " + String.format(Locale.getDefault(), "%.2f", monthlyPercent) + "%",
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
        for (float val : rawTargets) totalSum += val;

        float scalingFactor = (totalSum != 0) ? (monthlyTarget / totalSum) : 1f;

        for (int i = 0; i < rawTargets.size(); i++) {
            rawTargets.set(i, rawTargets.get(i) * scalingFactor);
        }

        for (int i = 0; i < rawTargets.size(); i++) {
            rawTargets.set(i, Math.round(rawTargets.get(i) * 100f) / 100f);
        }

        float finalSum = 0f;
        for (float val : rawTargets) finalSum += val;

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
            // If you also want to reset daily entries, uncomment:
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
            } else if (value instanceof Long) {
                return ((Long) value).floatValue();
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

    private void saveEntry() {
        String dateInput = et_date.getText().toString();
        String achievedStr = edtAchieved.getText().toString();
        String qtyStr = edtQty.getText().toString();
        String nobStr = edtNob.getText().toString();

        if (dateInput.isEmpty() || achievedStr.isEmpty() || qtyStr.isEmpty() || nobStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        float achieved = Float.parseFloat(achievedStr);
        float qty = Float.parseFloat(qtyStr);
        float nob = Float.parseFloat(nobStr);

        // Save to SharedPreferences (monthly + daily) as floats
        SaveDataToSharedPref(dateInput, achieved, qty, nob);
        SaveDailyDataToSharedPref(dateInput, achieved, qty, nob);

        // ✅ Pass current month calendar
        Calendar cal = Calendar.getInstance();
        refreshDailyTargetsForCurrentMonth(cal);

        loadRecentEntries(); // Refresh recycler view

        Toast.makeText(this, "Entry Saved", Toast.LENGTH_SHORT).show();
        ClearAllText();
    }

    private void loadRecentEntries() {
        SharedPreferences prefs = getSharedPreferences("Shop Data", MODE_PRIVATE);
        Map<String, ?> allData = prefs.getAll();

        List<DailyEntry> allEntries = new ArrayList<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        for (String key : allData.keySet()) {
            if (key.startsWith("Achieved_")) {
                String date = key.replace("Achieved_", "");

                float achieved = getSafeFloat(prefs, "Achieved_" + date, 0f);
                float qty      = getSafeFloat(prefs, "Quantity_" + date, 0f);
                float nob      = getSafeFloat(prefs, "NOB_" + date, 0f);

                // flags
                boolean expectedOnly = prefs.getBoolean("expected_only_" + date, false);
                boolean fromYOY      = prefs.getBoolean("from_yoy_" + date, false);

                // skip: edited only Expected (no sales) OR never saved via YOY (no sales)
                if ((expectedOnly && achieved == 0f && qty == 0f && nob == 0f)
                        || (!fromYOY && achieved == 0f && qty == 0f && nob == 0f)) {
                    continue;
                }

                allEntries.add(new DailyEntry(date, achieved, qty, nob));
            }
        }


        // ✅ Sort entries by date (latest first)
        allEntries.sort((e1, e2) -> {
            try {
                Date d1 = dateFormat.parse(e1.getDate());
                Date d2 = dateFormat.parse(e2.getDate());
                return d2.compareTo(d1); // descending order
            } catch (Exception e) {
                return 0;
            }
        });

        // ✅ Take last 4
        List<DailyEntry> recentEntries = new ArrayList<>();
        for (int i = 0; i < Math.min(4, allEntries.size()); i++) {
            recentEntries.add(allEntries.get(i));
        }

        if (recentEntries.isEmpty()) {
            recyclerRecentEntries.setVisibility(View.GONE);
            txtNoData.setVisibility(View.VISIBLE);
        } else {
            recyclerRecentEntries.setVisibility(View.VISIBLE);
            txtNoData.setVisibility(View.GONE);

            DailyEntryAdapter adapter = new DailyEntryAdapter(recentEntries);
            recyclerRecentEntries.setAdapter(adapter);
            recyclerRecentEntries.setAdapter(adapter);
        }

        btnViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(YOYSecondActivity.this, ViewAllDailyDataActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecentEntries(); // refresh recycler list
        recalcMonthlyTotals();
    }

    private void recalcMonthlyTotals() {
        SharedPreferences prefs = getSharedPreferences("Shop Data", MODE_PRIVATE);
        Map<String, ?> allData = prefs.getAll();

        float monthTotal = 0f;
        Calendar now = Calendar.getInstance();
        int currentMonth = now.get(Calendar.MONTH);
        int currentYear = now.get(Calendar.YEAR);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        for (String key : allData.keySet()) {
            if (key.startsWith("Achieved_")) {
                String dateStr = key.replace("Achieved_", "");
                try {
                    Date d = dateFormat.parse(dateStr);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(d);
                    if (cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear) {
                        monthTotal += getSafeFloat(prefs, key, 0f);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        monthlyAchieved = monthTotal;
        Log.d("YOYSecondActivity", "Monthly Achieved recalculated: ₹" + monthlyAchieved);
        // If you show monthly total in UI, update here
        // txtMonthlyAchieved.setText("Monthly Achieved: ₹" + String.format(Locale.getDefault(),"%.2f", monthTotal));
    }
}
