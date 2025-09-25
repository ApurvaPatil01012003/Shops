package com.exam.shops;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DailyTableYOY extends AppCompatActivity {
    Spinner spinnerMonth;
    LinearLayout tableContainer;
    String Holiday;
    int achievedValue;
    int quantities;
    int nobValue;
    String highPerDay;
    int TurnOver;
    int achieved;
    Button btnCPDF;
    ImageView backArrow;
    float monthlyAchievedTotal = 0f;
    float monthlyAchievedPercent = 0f;
    int HighPerGrowthPer = 10;
    float monthlyTarget;
    int existing;
    int delta;
    SharedPreferences prefs;
    float lossAmount;
    Map<String, String> publicHolidayMap;
    List<String> publicHolidayList;

    private List<Float> originalWeights;
    private List<Float> finalTargetsGlobal;
    private List<String> dateListGlobal;
    private List<String> dayTypesGlobal;


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

        prefs = getSharedPreferences("Shop Data", MODE_PRIVATE);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        tableContainer = findViewById(R.id.tableContainer);
        btnCPDF = findViewById(R.id.btnCPDF);

        backArrow=findViewById(R.id.backArrow);
        Holiday = getIntent().getStringExtra("ShopHoliday");
        achievedValue = getIntent().getIntExtra("Achived_Value", 0);
        quantities = getIntent().getIntExtra("Quantity", 0);
        nobValue = getIntent().getIntExtra("NOB", 0);
        highPerDay = getIntent().getStringExtra("HighPerDay");
        TurnOver = getIntent().getIntExtra("Growth", 0);

        publicHolidayList = loadHolidayDates();


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getFincialYearMonths());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        String currentMonthLabel = getCurrentMonthLabel();
        int defaultPosition = adapter.getPosition(currentMonthLabel);
        spinnerMonth.setSelection(defaultPosition);

        backArrow.setOnClickListener(v->
                onBackPressed());
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
                    // clearOldExpectedValues(selectedMonth);


                    generateDateRows(selectedMonth);


                    getExpectedSumForPastDays(15);
                    getExpectedSumForPastDays(7);


                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });


        btnCPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generatePDF();

            }

        });




        SharedPreferences sharedPreferences = getSharedPreferences("shop_data", MODE_PRIVATE);
        Holiday = sharedPreferences.getString("Shop_Holiday", "");
        highPerDay = sharedPreferences.getString("selected_days", "");


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

    private float getSafeFloat(SharedPreferences prefs, String key, float defaultValue) {
        try {
            return prefs.getFloat(key, defaultValue);  // Try reading as float directly
        } catch (ClassCastException e) {
            try {
                Object value = prefs.getAll().get(key);  // Fallback: get raw value
                if (value instanceof Integer) {
                    return ((Integer) value).floatValue();  // Convert Integer to float
                } else if (value instanceof String) {
                    return Float.parseFloat((String) value);  // Parse String to float
                } else if (value instanceof Float) {
                    return (Float) value;
                } else {
                    return defaultValue;  // Unknown type
                }
            } catch (Exception ex) {
                return defaultValue;  // On any other failure
            }
        }
    }

    private void generateDateRows(Calendar startDate) {
        tableContainer.removeAllViews();
        monthlyAchievedTotal = 0f;


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
        Log.d("DailyTableYOY", "Working days : " + workingDays);
        Log.d("DailyTableYOY", "High per day : " + highPerfDays);
        SharedPreferences yoyPrefs = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);

// Get the short month name
        SimpleDateFormat sdf = new SimpleDateFormat("MMM", Locale.ENGLISH);
        String shortMonth = sdf.format(startDate.getTime());

// Get the year for that month
        String[] parts = getFinancialYear().split("_");
        int fyStartYear = Integer.parseInt(parts[0]);
        int dataYear = (shortMonth.equals("Jan") || shortMonth.equals("Feb") || shortMonth.equals("Mar")) ? fyStartYear + 1 : fyStartYear;

// Get updated monthly target
        //  monthlyTarget = yoyPrefs.getFloat("expected_" + shortMonth + "_" + dataYear, TurnOver / 12f);
        monthlyTarget = getSafeFloat(yoyPrefs, "expected_" + shortMonth + "_" + dataYear, TurnOver / 12f);

        Log.d("MonthlyTargetFromPrefs", "Month: " + shortMonth + " " + dataYear + " | Target: " + monthlyTarget);

        float baseTargetDays = workingDays;
        float growthMultiplier = 1 + (HighPerGrowthPer / 100f);
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


        List<String> prePublicHighPerfDates = new ArrayList<>();
        // List<String> publicHolidayDates = new ArrayList<>(publicHolidayMap.values());
        List<String> publicHolidayDates = new ArrayList<>(publicHolidayList);


        for (String holidayDateStr : publicHolidayDates) {
            try {
                Calendar holidayCal = Calendar.getInstance();
                holidayCal.setTime(dateFormat.parse(holidayDateStr));

                for (int i = 1; i <= 7; i++) {
                    Calendar preCal = (Calendar) holidayCal.clone();
                    preCal.add(Calendar.DAY_OF_MONTH, -i);

                    String preDateStr = dateFormat.format(preCal.getTime());
                    String preDayStr = dayFormat.format(preCal.getTime());


                    if (!publicHolidayDates.contains(preDateStr) && !Holiday.equals(preDayStr)) {
                        prePublicHighPerfDates.add(preDateStr);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<Float> rawTargets = new ArrayList<>();
        List<String> dayTypes = new ArrayList<>();
        List<String> dateList = new ArrayList<>();

        float rawTargetSum = 0f;

        for (int i = 1; i <= daysInMonth; i++) {
            cal.set(Calendar.DAY_OF_MONTH, i);
            String dateStr = dateFormat.format(cal.getTime());
            String dayStr = dayFormat.format(cal.getTime());
            dateList.add(dateStr);


            String type;
            float rawTarget;

            if (publicHolidayDates.contains(dateStr)) {
                type = "Public Holiday";
                rawTarget = 0f;
            } else if (Holiday.equals(dayStr)) {
                type = "Holiday";
                rawTarget = 0f;
            } else if (highPerfDaysList.contains(dayStr) || prePublicHighPerfDates.contains(dateStr)) {
                type = "High Performance Day";
                rawTarget = 1f * growthMultiplier;
            } else {
                type = "Working Day";
                rawTarget = 1f;
            }

            dayTypes.add(type);
            rawTargets.add(rawTarget);

            if (rawTarget > 0f) {
                rawTargetSum += rawTarget;
            }
        }


        float scalingFactor = (rawTargetSum != 0) ? (monthlyTarget / rawTargetSum) : 1f;

        List<Float> finalTargets = new ArrayList<>();
        finalTargets.clear();
        SharedPreferences prefs = getSharedPreferences("Shop Data", MODE_PRIVATE);

        float totalEditedTarget = 0f;
        float totalAutoTargetWeight = 0f;

        List<Boolean> isUserEdited = new ArrayList<>();


        SharedPreferences.Editor editor = prefs.edit();
        for (int i = 0; i < rawTargets.size(); i++) {
            String dateKey = dateList.get(i);
            boolean isEdited = prefs.getBoolean("edited_" + dateKey, false);

            // Reset if not edited
            if (!isEdited) {
                editor.remove("Expected_" + dateKey);
            }
        }
        editor.apply();

// Modify the finalTargets assignment loop:
        for (int i = 0; i < rawTargets.size(); i++) {
            String dateKey = dateList.get(i);
            float saved = prefs.getFloat("Expected_" + dateKey, -1f);
            boolean isEdited = prefs.getBoolean("edited_" + dateKey, false);

            if (isEdited && saved >= 0) {
                finalTargets.add(saved);
                totalEditedTarget += saved;
                isUserEdited.add(true);
            } else {
                finalTargets.add(0f);
                totalAutoTargetWeight += rawTargets.get(i);
                isUserEdited.add(false);
            }
        }


        float remainingTarget = monthlyTarget - totalEditedTarget;
        float autoScaling = (totalAutoTargetWeight > 0) ? (remainingTarget / totalAutoTargetWeight) : 0f;

        for (int i = 0; i < finalTargets.size(); i++) {
            if (!isUserEdited.get(i)) {
                float adjusted = rawTargets.get(i) * autoScaling;
                finalTargets.set(i, adjusted);
            }
        }


        // Debug: Sum check
        float sum = 0f;
        for (float f : finalTargets) sum += f;
        Log.d("TARGET_CHECK", "Expected: " + monthlyTarget + " | Calculated sum: " + sum);

        // Proceed to build UI
        for (int i = 0; i < daysInMonth; i++) {
            String dateStr = dateList.get(i);
            String type = dayTypes.get(i);
            float targetForDay = finalTargets.get(i);

            cal.set(Calendar.DAY_OF_MONTH, i + 1);
            String dayStr = dayFormat.format(cal.getTime());


            float savedExpected = prefs.getFloat("Expected_" + dateStr, -1f);

            if (savedExpected >= 0) {
                targetForDay = savedExpected;
            }
            String formattedDailyTarget = String.format("%.2f", targetForDay);



// Save the updated value to SharedPreferences after loading
            editor.putFloat("Expected_" + dateStr, targetForDay);
            editor.apply();


            //  SharedPreferences prefs = getSharedPreferences("Shop Data", MODE_PRIVATE);
            achieved = prefs.getInt("Achieved_" + dateStr, 0);
            monthlyAchievedTotal += achieved;

            //  SharedPreferences prefs = getSharedPreferences("Shop Data", MODE_PRIVATE);
            // SharedPreferences.Editor editor = prefs.edit();


            if (!prefs.contains("Expected_" + dateStr)) {
                editor.putFloat("Expected_" + dateStr, targetForDay);
                editor.apply();
                Log.d("DayTarget", "DAILY TARGET : " + dateStr + "  " + targetForDay);
            }

            int quantity = prefs.getInt("Quantity_" + dateStr, 0);
            int NOB = prefs.getInt("NOB_" + dateStr, 0);
            float ABS = (NOB != 0) ? (float) quantity / NOB : 0;
            float ATV = (NOB != 0) ? (float) achieved / NOB : 0;
            float ASP = (quantity != 0) ? (float) achieved / quantity : 0;
            float percentage = (targetForDay != 0) ? (achieved / targetForDay) * 100f : 0f;

            View cardView = getLayoutInflater().inflate(R.layout.card_daily_info, null);

            ((TextView) cardView.findViewById(R.id.txtDate)).setText("Date: " + dateStr);
            ((TextView) cardView.findViewById(R.id.txtDay)).setText("Day: " + dayStr);
            ((TextView) cardView.findViewById(R.id.txtAchieved)).setText("Achieved: " + achieved);
            ((TextView) cardView.findViewById(R.id.txtType)).setText("Type: " + type);
            ((TextView) cardView.findViewById(R.id.txtExpect)).setText("Expected: " + formattedDailyTarget);
            //  ((TextView) cardView.findViewById(R.id.txtAchievedPer)).setText("Achieved %: " + String.format("%.2f%%", percentage));
            ((TextView) cardView.findViewById(R.id.txtQuantity)).setText("Quantity: " + quantity);
            ((TextView) cardView.findViewById(R.id.txtNOB)).setText("NOB: " + NOB);
            ((TextView) cardView.findViewById(R.id.txtABS)).setText("ABS: " + String.format(Locale.US, "%.2f", ABS));
            ((TextView) cardView.findViewById(R.id.txtATV)).setText("ATV: " + String.format(Locale.US, "%.2f", ATV));
            ((TextView) cardView.findViewById(R.id.txtASP)).setText("ASP: " + String.format(Locale.US, "%.2f", ASP));
//            float Loss = Float.parseFloat(formattedDailyTarget) - achieved;
//            ((TextView) cardView.findViewById(R.id.txtLoss)).setText("Loss: " + String.format(Locale.US, "%.2f", Loss));


            LinearLayout detailsLayout = cardView.findViewById(R.id.detailsLayout);

            LinearLayout headerLayout = cardView.findViewById(R.id.headerLayout);
            headerLayout.setOnClickListener(v -> {
                detailsLayout.setVisibility(detailsLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            });


            tableContainer.addView(cardView);

            float[] expectedValue = {targetForDay};
            int[] achievedValue = {achieved};
            int[] quantityValue = {quantity};
            int[] nobValue = {NOB};


            TextView txtExpect = cardView.findViewById(R.id.txtExpect);
            TextView txtAchieved = cardView.findViewById(R.id.txtAchieved);
            TextView txtQuantity = cardView.findViewById(R.id.txtQuantity);
            TextView txtNOB = cardView.findViewById(R.id.txtNOB);
            //TextView txtAchievedPer = cardView.findViewById(R.id.txtAchievedPer);
            TextView txtABS = cardView.findViewById(R.id.txtABS);
            TextView txtATV = cardView.findViewById(R.id.txtATV);
            TextView txtASP = cardView.findViewById(R.id.txtASP);
            TextView txtLoss = cardView.findViewById(R.id.txtLoss);

            Runnable updateMetrics = () -> {

                int achieved = achievedValue[0];
                float expected = expectedValue[0];
                int qty = quantityValue[0];
                int nob = nobValue[0];

                float percent = (expected != 0) ? (achieved / expected) * 100f : 0f;
                float atv = (nob != 0) ? (float) achieved / nob : 0f;
                float asp = (qty != 0) ? (float) achieved / qty : 0f;
                float abs = (nob != 0) ? (float) qty / nob : 0;
                float diff = achieved - expected; // Profit (positive), Loss (negative)

                MaterialButton btnLoss = cardView.findViewById(R.id.btnLoss);

                if (diff > 0) {
                    // PROFIT
                    txtLoss.setVisibility(View.VISIBLE);
                    txtLoss.setText("Profit: ₹" + String.format(Locale.US, "%.2f", diff));
                    btnLoss.setVisibility(View.GONE); // Hide button if profit
                }
                else if (diff < 0) {
                    // LOSS
                    txtLoss.setVisibility(View.VISIBLE);
                    txtLoss.setText("Loss: ₹" + String.format(Locale.US, "%.2f", -diff));
                    btnLoss.setVisibility(View.VISIBLE);
                    btnLoss.setText("Distribute Loss →");
                    btnLoss.setOnClickListener(v -> showDistributeLossDialog(cardView));
                }
                else {
                    // BREAK-EVEN
                    txtLoss.setVisibility(View.GONE); // Hide completely
                    btnLoss.setVisibility(View.GONE);
                }



                if (diff < 0) {  // only show for loss
                    btnLoss.setVisibility(View.VISIBLE);
                    btnLoss.setText("Distribute Loss →");
                    btnLoss.setOnClickListener(v -> showDistributeLossDialog(cardView));
                } else {
                    btnLoss.setVisibility(View.GONE); // hide for profit or break-even
                }


                // txtAchievedPer.setText(String.format("Achieved %%: %.2f%%", percent));
                txtABS.setText("ABS: " + String.format(Locale.US, "%.2f", abs));
                txtATV.setText("ATV: " + String.format(Locale.US, "%.2f", atv));
                txtASP.setText("ASP: " + String.format(Locale.US, "%.2f", asp));


            };
            updateMetrics.run();


            TextView edtAmount = cardView.findViewById(R.id.edtAmount);
            String finalShortMonth = shortMonth;
            String finalShortMonth1 = shortMonth;
            edtAmount.setOnClickListener(v -> {
                // Inflate your custom dialog layout
                View dialogView = getLayoutInflater().inflate(R.layout.daily_alert, null);

                EditText edtTarget = dialogView.findViewById(R.id.edtTarget);
                EditText edtAchieved = dialogView.findViewById(R.id.edtAchived);
                EditText edtQty = dialogView.findViewById(R.id.edtQty);
                EditText edtNob = dialogView.findViewById(R.id.edtNob);
                MaterialButton btnSave = dialogView.findViewById(R.id.btnSave);

                float latestExpected = prefs.getFloat("Expected_" + dateStr, expectedValue[0]);
                int latestAchieved = prefs.getInt("Achieved_" + dateStr, achievedValue[0]);
                int latestQty = prefs.getInt("Quantity_" + dateStr, quantityValue[0]);
                int latestNob = prefs.getInt("NOB_" + dateStr, nobValue[0]);

                edtTarget.setText(String.valueOf(latestExpected));
                edtAchieved.setText(String.valueOf(latestAchieved));
                edtQty.setText(String.valueOf(latestQty));
                edtNob.setText(String.valueOf(latestNob));


                // Create AlertDialog
                androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setView(dialogView)
                        .setCancelable(true)
                        .create();


                btnSave.setOnClickListener(view -> {
                    String targetStr = edtTarget.getText().toString().trim();
                    String achievedStr = edtAchieved.getText().toString().trim();
                    String qtyStr = edtQty.getText().toString().trim();
                    String nobStr = edtNob.getText().toString().trim();

                    if (targetStr.isEmpty() || achievedStr.isEmpty() || qtyStr.isEmpty() || nobStr.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Parse new values
                    int newAchieved = Integer.parseInt(achievedStr);

                    expectedValue[0] = Float.parseFloat(targetStr);
                    achievedValue[0] = newAchieved;
                    quantityValue[0] = Integer.parseInt(qtyStr);
                    nobValue[0] = Integer.parseInt(nobStr);

                    // Save daily values
                    editor.putFloat("Expected_" + dateStr, expectedValue[0]);
                    editor.putInt("Achieved_" + dateStr, achievedValue[0]);
                    editor.putInt("Quantity_" + dateStr, quantityValue[0]);
                    editor.putInt("NOB_" + dateStr, nobValue[0]);
                    editor.putBoolean("edited_" + dateStr, true);
                    editor.apply();

                    // 🔥 Recalculate monthly achieved fresh
                    try {
                        Date date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(dateStr);
                        //Calendar cal = Calendar.getInstance();
                        cal.setTime(date);

                        // String shortMonth = new SimpleDateFormat("MMM", Locale.getDefault()).format(date);
                        // int year = cal.get(Calendar.YEAR);
                        int monthIndex = cal.get(Calendar.MONTH);

                        int totalAchieved = 0;
                        //int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

                        // SharedPreferences prefs = getSharedPreferences("DailyData", MODE_PRIVATE);
                        for (int day = 1; day <= daysInMonth; day++) {
                            String key = String.format(Locale.getDefault(), "%02d-%02d-%04d", day, monthIndex + 1, year);
                            totalAchieved += prefs.getInt("Achieved_" + key, 0);
                        }

                        yoyPrefs.edit()
                                .putInt("data_" + finalShortMonth1 + "_" + year + "_Achieved", totalAchieved)
                                .apply();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Update UI
                    txtExpect.setText("Expected: ₹" + targetStr);
                    txtAchieved.setText("Achieved: ₹" + achievedStr);
                    txtQuantity.setText("Quantity: " + qtyStr);
                    txtNOB.setText("NOB: " + nobStr);

                    // Rebalance targets
                    rebalanceTargets(dateStr, Float.parseFloat(targetStr) - expectedValue[0]);

                    // Update metrics
                    updateMetrics.run();

                    dialog.dismiss();
                });




//                btnSave.setOnClickListener(view -> {
//                    String targetStr = edtTarget.getText().toString().trim();
//                    String achievedStr = edtAchieved.getText().toString().trim();
//                    String qtyStr = edtQty.getText().toString().trim();
//                    String nobStr = edtNob.getText().toString().trim();
//
//                    if(targetStr.isEmpty() || achievedStr.isEmpty() || qtyStr.isEmpty() || nobStr.isEmpty()){
//                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//                    // Calculate deltas for rebalance
//                    float deltaExpected = Float.parseFloat(targetStr) - expectedValue[0];
//                    int deltaAchieved = Integer.parseInt(achievedStr) - achievedValue[0];
//
//                    // Update card values
//                    expectedValue[0] = Float.parseFloat(targetStr);
//                    achievedValue[0] = Integer.parseInt(achievedStr);
//                    quantityValue[0] = Integer.parseInt(qtyStr);
//                    nobValue[0] = Integer.parseInt(nobStr);
//
//                    // Update TextViews on card (not edtAmount)
//                    txtExpect.setText("Expected: ₹" + targetStr);
//                    txtAchieved.setText("Achieved: ₹" + achievedStr);
//                    txtQuantity.setText("Quantity: " + qtyStr);
//                    txtNOB.setText("NOB: " + nobStr);
//
//                    // Save to SharedPreferences
//                    editor.putFloat("Expected_" + dateStr, expectedValue[0]);
//                    editor.putInt("Achieved_" + dateStr, achievedValue[0]);
//                    editor.putInt("Quantity_" + dateStr, quantityValue[0]);
//                    editor.putInt("NOB_" + dateStr, nobValue[0]);
//                    editor.putBoolean("edited_" + dateStr, true);
//                    editor.apply();
//
//                    // Rebalance targets
//                    rebalanceTargets(dateStr, deltaExpected);
//
//                    // Update metrics on card
//                    updateMetrics.run();
//
//                    dialog.dismiss();
//                });

                dialog.show();
            });
            cardView.findViewById(R.id.btnLoss).setOnClickListener(v -> {
                showDistributeLossDialog(cardView);
            });
            String todayStr = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());

            if (dateStr.equals(todayStr)) {

                editor.putString("today_expected", formattedDailyTarget);
                editor.putInt("today_achieved", achieved);
                editor.putString("today_type", type);
                editor.putFloat("today_percent", percentage);

                editor.putInt("today_abs", (int) ABS);
                editor.putFloat("today_atv", ATV);
                editor.putFloat("today_asp", ASP);

                editor.apply();


            }


        }

        shortMonth = sdf.format(startDate.getTime());

        parts = getFinancialYear().split("_");
        fyStartYear = Integer.parseInt(parts[0]);
        dataYear = (shortMonth.equals("Jan") || shortMonth.equals("Feb") || shortMonth.equals("Mar")) ? fyStartYear + 1 : fyStartYear;

        // monthlyTarget = yoyPrefs.getFloat("expected_" + shortMonth + "_" + dataYear, TurnOver / 12f);
        getSafeFloat(prefs, "Expected_" + shortMonth + "_" + dataYear, TurnOver / 12f);

        Log.d("MonthlyTargetFromPrefs", "Month: " + shortMonth + " " + dataYear + " | Target: " + monthlyTarget);
        monthlyAchievedPercent = (monthlyTarget != 0) ? (monthlyAchievedTotal * 100f / monthlyTarget) : 0f;

        Log.d("MonthlySummary", "Achieved: " + monthlyAchievedTotal + " | Percent: " + monthlyAchievedPercent);


        List<Float> originalWeights = new ArrayList<>(rawTargets);


        this.originalWeights = originalWeights;
        this.dateListGlobal = new ArrayList<>(dateList);
        this.finalTargetsGlobal = new ArrayList<>(finalTargets);
        this.dayTypesGlobal = new ArrayList<>(dayTypes);


    }

    private interface OnFieldUpdated {
        void onUpdated(String newValue);
    }

    private float calculateLatestMonthlyAchievedTotal() {
        SharedPreferences prefs = getSharedPreferences("Shop Data", MODE_PRIVATE);
        float sum = 0f;
        for (String date : dateListGlobal) {
            sum += prefs.getInt("Achieved_" + date, 0);
        }
        return sum;
    }

    private void generatePDF() {
        PdfDocument document = new PdfDocument();
        int pageWidth = 595;
        int pageHeight = 842;
        int startX = 20;
        int startY = 50;
        int tableTop = startY + 40;
        int rowHeight = 35;


        //int[] columnWidths = {75, 75, 140, 60, 60, 70, 40, 40};
        int tableWidth = pageWidth - 2 * startX;
        int[] columnWidths = {
                (int) (tableWidth * 0.14),
                (int) (tableWidth * 0.12),
                (int) (tableWidth * 0.12),
                (int) (tableWidth * 0.12),
                (int) (tableWidth * 0.10),
                (int) (tableWidth * 0.10),
                (int) (tableWidth * 0.10),
                (int) (tableWidth * 0.10)
                , (int) (tableWidth * 0.10)
        };

        String[] headers = {"Date", "Expect", "Ach", "Ach%", "Qty", "NOB", "ABS", "ATV", "ASP"};
        int[] columnPositions = getColumnPositions(startX, columnWidths);

        Paint paint = new Paint();
        paint.setTextSize(12f);
        paint.setColor(Color.BLACK);

        Paint boldPaint = new Paint(paint);
        boldPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        Paint titlePaint = new Paint(boldPaint);
        titlePaint.setTextSize(20f);

        Paint linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(1f);

        Paint headerBgPaint = new Paint();
        headerBgPaint.setColor(Color.LTGRAY);

        Paint TApaint = new Paint(boldPaint);
        TApaint.setTextSize(14f);
        TApaint.setColor(Color.BLACK);


        int pageNum = 1;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();


        canvas.drawText("Daily Report", startX, startY, titlePaint);

        float latestAchieved = calculateLatestMonthlyAchievedTotal();

        DecimalFormat df = new DecimalFormat("#,##0");
        String targetText = "Monthly Target: ₹" + df.format(monthlyTarget);
        String achievedText = "Monthly Achieved: ₹" + df.format(latestAchieved);
        float targetTextWidth = paint.measureText(targetText);
        canvas.drawText(targetText, startX + 180, startY, TApaint);
        canvas.drawText(achievedText, (startX + 180) + targetTextWidth + 40f, startY, TApaint);


        int x, y = tableTop;

        // Draw top border above header
        canvas.drawLine(startX, y, pageWidth - startX, y, linePaint);

        // Header background
        canvas.drawRect(startX, y, pageWidth - startX, y + rowHeight, headerBgPaint);

        // Draw headers
        x = startX;
        for (int i = 0; i < headers.length; i++) {
            canvas.drawText(headers[i], x + 5, y + 22, boldPaint);
            x += columnWidths[i];
        }

        // Horizontal line under header
        canvas.drawLine(startX, y + rowHeight, pageWidth - startX, y + rowHeight, linePaint);
        y += rowHeight;

        // Vertical lines for header
        for (int pos : columnPositions) {
            canvas.drawLine(pos, tableTop, pos, y, linePaint);
        }


        for (int i = 0; i < tableContainer.getChildCount(); i++) {
            View card = tableContainer.getChildAt(i);
//            if (i % 2 == 0) {
//                Paint rowBg = new Paint();
//                rowBg.setColor(Color.parseColor("#f0f0f0"));
//                canvas.drawRect(startX, y, pageWidth - startX, y + rowHeight, rowBg);
//            }

            String date = ((TextView) card.findViewById(R.id.txtDate)).getText().toString().replace("Date: ", "");
//            String day = ((TextView) card.findViewById(R.id.txtDay)).getText().toString().replace("Day: ", "");
//            String type = ((TextView) card.findViewById(R.id.txtType)).getText().toString().replace("Type: ", "");
            String expected = ((TextView) card.findViewById(R.id.txtExpect)).getText().toString().replace("Expected: ", "");
            String achieved = ((TextView) card.findViewById(R.id.txtAchieved)).getText().toString().replace("Achieved: ", "");
            //  String percent = ((TextView) card.findViewById(R.id.txtAchievedPer)).getText().toString().replace("Achieved %: ", "").replace("%", "");
            String qty = ((TextView) card.findViewById(R.id.txtQuantity)).getText().toString().replace("Quantity: ", "");
            String nob = ((TextView) card.findViewById(R.id.txtNOB)).getText().toString().replace("NOB: ", "");
            String abs = ((TextView) card.findViewById(R.id.txtABS)).getText().toString().replace("ABS: ", "");
            String atv = ((TextView) card.findViewById(R.id.txtATV)).getText().toString().replace("ATV: ", "");
            String asp = ((TextView) card.findViewById(R.id.txtASP)).getText().toString().replace("ASP: ", "");

            String[] rowData = {date, expected, achieved, null, qty, nob, abs, atv, asp};

            // Draw row data
            x = startX;
            for (int j = 0; j < rowData.length; j++) {
                String display = rowData[j];
                if (display.length() > 15) {
                    display = display.substring(0, 13) + "...";
                }

                canvas.drawText(display, x + 5, y + 25, paint);
                x += columnWidths[j];
            }


            y += rowHeight;

            // Horizontal line after row
            canvas.drawLine(startX, y, pageWidth - startX, y, linePaint);

            // Vertical lines for row
            for (int pos : columnPositions) {
                canvas.drawLine(pos, y - rowHeight, pos, y, linePaint);
            }

            // Page break logic
            if (y > pageHeight - 50) {
                document.finishPage(page);
                pageNum++;
                pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = tableTop;

                // Redraw top line
                canvas.drawLine(startX, y, pageWidth - startX, y, linePaint);

                // Redraw header background
                canvas.drawRect(startX, y, pageWidth - startX, y + rowHeight, headerBgPaint);

                // Redraw headers
                x = startX;
                for (int h = 0; h < headers.length; h++) {
                    canvas.drawText(headers[h], x + 5, y + 22, boldPaint);
                    x += columnWidths[h];
                }

                canvas.drawLine(startX, y + rowHeight, pageWidth - startX, y + rowHeight, linePaint);
                y += rowHeight;

                // Redraw vertical lines
                for (int pos : columnPositions) {
                    canvas.drawLine(pos, tableTop, pos, y, linePaint);
                }
            }
        }

        document.finishPage(page);

        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "DailyReport_" + System.currentTimeMillis() + ".pdf");

            FileOutputStream out = new FileOutputStream(file);
            document.writeTo(out);
            document.close();
            out.close();

            Toast.makeText(this, "PDF saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

            // Open the generated PDF
            Uri pdfUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider", // This must match the authority in your manifest
                    file
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NO_HISTORY);

            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "No PDF viewer installed", Toast.LENGTH_SHORT).show();
            }


        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error writing PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    // Helper to get X positions of all columns
    private int[] getColumnPositions(int startX, int[] columnWidths) {
        int[] positions = new int[columnWidths.length + 1];
        positions[0] = startX;
        for (int i = 1; i <= columnWidths.length; i++) {
            positions[i] = positions[i - 1] + columnWidths[i - 1];
        }
        return positions;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        intent.putExtra("MonthlyAchieved", monthlyAchievedTotal);
        intent.putExtra("MonthlyAchievedPercent", monthlyAchievedPercent);
        intent.putExtra("data_updated", true);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void rebalanceTargets(String editedDate, float delta) {
        SharedPreferences prefs = getSharedPreferences("Shop Data", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        float monthlyTarget;
        {
            SharedPreferences yoyPrefs = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM", Locale.ENGLISH);
            Calendar cal = Calendar.getInstance();
            String shortMonth = sdf.format(cal.getTime());
            String[] parts = getFinancialYear().split("_");
            int fyStartYear = Integer.parseInt(parts[0]);
            int dataYear = (shortMonth.equals("Jan") || shortMonth.equals("Feb") || shortMonth.equals("Mar")) ? fyStartYear + 1 : fyStartYear;
            monthlyTarget = yoyPrefs.getFloat("expected_" + shortMonth + "_" + dataYear, TurnOver / 12f);
        }

        float totalEdited = 0f;
        float totalEditableWeight = 0f;

        for (int i = 0; i < dateListGlobal.size(); i++) {
            String dateKey = dateListGlobal.get(i);
            String type = dayTypesGlobal.get(i);
            boolean isEdited = prefs.getBoolean("edited_" + dateKey, false);

            if (isEdited) {
                float editedVal = prefs.getFloat("Expected_" + dateKey, 0f);
                totalEdited += editedVal;
            } else if (!type.equals("Holiday") && !type.equals("Public Holiday")) {
                totalEditableWeight += originalWeights.get(i);
            }
        }

        float remainingTarget = monthlyTarget - totalEdited;

        if (remainingTarget < 0f || totalEditableWeight == 0f) {
            Log.e("RebalanceError", "Negative remaining target or no editable days left");
            return;
        }


        for (int i = 0; i < dateListGlobal.size(); i++) {
            String dateKey = dateListGlobal.get(i);
            String type = dayTypesGlobal.get(i);
            boolean isEdited = prefs.getBoolean("edited_" + dateKey, false);

            if (isEdited || type.equals("Holiday") || type.equals("Public Holiday")) continue;

            float weight = originalWeights.get(i);
            float newExpected = (weight / totalEditableWeight) * remainingTarget;
            newExpected = Math.max(0f, newExpected);

            // editor.putFloat("Expected_" + dateKey, newExpected);
            editor.putFloat("Expected_" + dateKey, newExpected);
            editor.putBoolean("edited_" + dateKey, false);


            View card = findCardByDate(dateKey);
            if (card != null) {
                TextView txtExpect = card.findViewById(R.id.txtExpect);
                txtExpect.setText("Expected: ₹" + String.format("%.2f", newExpected));

                String achievedStr = ((TextView) card.findViewById(R.id.txtAchieved)).getText().toString().replace("Achieved: ₹", "").trim();
                float achievedVal = 0f;
                try {
                    achievedVal = Float.parseFloat(achievedStr);
                } catch (NumberFormatException e) {
                    Log.e("ParseError", "Invalid achieved value: " + achievedStr);
                }

            }
        }

        editor.apply();
    }


    private View findCardByDate(String targetDate) {
        for (int i = 0; i < tableContainer.getChildCount(); i++) {
            View card = tableContainer.getChildAt(i);
            TextView txtDate = card.findViewById(R.id.txtDate);
            if (txtDate != null) {
                String text = txtDate.getText().toString().trim();
                String[] parts = text.split(":");
                if (parts.length == 2) {
                    String dateInCard = parts[1].trim();
                    if (dateInCard.equals(targetDate)) {
                        return card;
                    }
                }
            }
        }
        return null;
    }


    private float getExpectedSumForPastDays(int numDays) {
        SharedPreferences prefs = getSharedPreferences("Shop Data", MODE_PRIVATE);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        float totalExpected = 0f;

        for (int i = 1; i <= numDays; i++) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
            String dateKey = sdf.format(cal.getTime());
            //float expected = prefs.getFloat("Expected_" + dateKey, -1f);

            float expected = -1f;
            try {
                expected = prefs.getFloat("Expected_" + dateKey, -1f);
            } catch (ClassCastException e) {
                // If it was saved as a String, convert it to float
                try {
                    String val = prefs.getString("Expected_" + dateKey, "-1");
                    expected = Float.parseFloat(val);
                } catch (Exception ex) {
                    expected = -1f; // fallback
                }
            }

            if (expected >= 0f) {
                totalExpected += expected;
            }
        }

        Log.d("ExpectedSum", numDays + " days total: ₹" + totalExpected);
        return totalExpected;
    }

    private String getFinancialYear() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        int startYear, endYear;
        if (month >= Calendar.APRIL) {
            startYear = year;
            endYear = year + 1;
        } else {
            startYear = year - 1;
            endYear = year;
        }
        return startYear + "_" + String.valueOf(endYear).substring(2);
    }

//    private Map<String, String> loadPublicHolidaysFromJson() {
//        Map<String, String> holidayMap = new HashMap<>();
//        try {
//            InputStream is = getAssets().open("public_holidays.json");
//            int size = is.available();
//            byte[] buffer = new byte[size];
//            is.read(buffer);
//            is.close();
//
//            String jsonString = new String(buffer, "UTF-8");
//            JSONObject jsonObject = new JSONObject(jsonString);
//
//            Iterator<String> keys = jsonObject.keys();
//            while (keys.hasNext()) {
//                String key = keys.next();
//                String value = jsonObject.getString(key);
//                holidayMap.put(key, value);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return holidayMap;
//    }


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


    private void showDistributeLossDialog(View cardView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Distribute Loss");
        builder.setMessage("Enter the number of working days to distribute the loss:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Distribute", (dialog, which) -> {
            int days = Integer.parseInt(input.getText().toString().trim());
            if (days <= 0) {
                Toast.makeText(this, "Number of days must be greater than zero", Toast.LENGTH_SHORT).show();
                return;
            }
            distributeLossAcrossDays(days, cardView);


        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    private void distributeLossAcrossDays(int days, View cardView) {
        TextView txtLoss = cardView.findViewById(R.id.txtLoss);
        String rawLossText = txtLoss.getText().toString();

        // Debugging
        Log.d("LossDistribution", "Raw loss text: " + rawLossText);

        // Clean the string to extract just the numeric value
        String cleanLoss = rawLossText
                .replace("Loss:", "")
                .replace("Loss", "")
                .replace("=", "")
                .replace("₹", "")
                .replace(",", "")
                .trim();

        float lossAmount;
        try {
            lossAmount = Float.parseFloat(cleanLoss);
        } catch (NumberFormatException e) {
            Log.e("LossDistribution", "Invalid loss value: " + cleanLoss, e);
            Toast.makeText(this, "Invalid loss value: " + cleanLoss, Toast.LENGTH_SHORT).show();
            return;
        }

        if (lossAmount <= 0) {
            Toast.makeText(this, "No loss to distribute", Toast.LENGTH_SHORT).show();
            return;
        }

        float lossPerDay = lossAmount / days;
        int count = 0;

        // Get date from card
        TextView txtDate = cardView.findViewById(R.id.txtDate);
        String lossDateText = txtDate.getText().toString().replace("Date: ", "").trim();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        try {
            Date lossDate = dateFormat.parse(lossDateText);
            cal.setTime(lossDate);

            // Adjust expected value of the loss day itself
            float currentExpected = prefs.getFloat("Expected_" + lossDateText, 0f);
            float adjustedExpected = Math.max(0, currentExpected - lossAmount);
            prefs.edit().putFloat("Expected_" + lossDateText, adjustedExpected).apply();

            View lossDayCard = findCardByDate(lossDateText);
            if (lossDayCard != null) {
                TextView txtExpect = lossDayCard.findViewById(R.id.txtExpect);
                txtExpect.setText("Expected: ₹" + String.format(Locale.US, "%.2f", adjustedExpected));
            }

            Log.d("LossDistribution", "Adjusted expected for loss day: " + lossDateText + " | New Expected: " + adjustedExpected);

            cal.add(Calendar.DAY_OF_MONTH, 1);
        } catch (Exception e) {
            Log.e("LossDistribution", "Invalid date format for: " + lossDateText, e);
            Toast.makeText(this, "Invalid loss date format", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> publicHolidays = loadHolidayDates();
        SharedPreferences.Editor editor = prefs.edit();

        Log.d("LossDistribution", "Total Loss: " + lossAmount + " | Per Day Loss: " + lossPerDay);

        while (count < days) {
            String dateStr = String.format(Locale.getDefault(), "%02d-%02d-%d",
                    cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
            String dayStr = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());

            if (isWorkingDay(dayStr, dateStr, publicHolidays, Holiday)) {
                float currentTarget = prefs.getFloat("Expected_" + dateStr, 0f);
                float newTarget = currentTarget + lossPerDay;

                editor.putFloat("Expected_" + dateStr, newTarget);
                editor.putBoolean("edited_" + dateStr, true);
                editor.apply();

                View targetCard = findCardByDate(dateStr);
                if (targetCard != null) {
                    TextView txtExpect = targetCard.findViewById(R.id.txtExpect);
                    txtExpect.setText("Expected: ₹" + String.format(Locale.US, "%.2f", newTarget));
                }

                Log.d("LossDistribution", "Distributed to: " + dateStr + " | New Target: " + newTarget);
                count++;
            } else {
                Log.d("LossDistribution", "Skipping holiday: " + dateStr + " (" + dayStr + ")");
            }

            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        txtLoss.setText("Loss: ₹0.00");
        prefs.edit().putFloat("last_loss", 0f).apply();

        Toast.makeText(this, "Loss distributed over " + days + " working days", Toast.LENGTH_SHORT).show();
    }



    private boolean isWorkingDay(String day, String date, List<String> publicHolidays, String shopHoliday) {
        if (prefs == null) {
            prefs = getSharedPreferences("Shop Data", MODE_PRIVATE);
        }

        // Check if the current day is a holiday or public holiday
        if (day.equalsIgnoreCase(shopHoliday) || publicHolidays.contains(date)) {
            Log.d("LossDistribution", "Skipping non-working day: " + date + " (" + day + ")");
            return false;
        }

        return true;
    }
    private void recalcMonthlyAchieved(String shortMonth, int year) {
        SharedPreferences prefs = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);

        int total = 0;
        Calendar cal = Calendar.getInstance();

        // Convert "Sep" → month index
        try {
            Date monthDate = new SimpleDateFormat("MMM", Locale.getDefault()).parse(shortMonth);
            cal.setTime(monthDate);
        } catch (Exception e) { e.printStackTrace(); }

        int monthIndex = cal.get(Calendar.MONTH);

        // Loop through all days of this month
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, monthIndex);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int day = 1; day <= daysInMonth; day++) {
            String dateKey = String.format(Locale.getDefault(), "%02d-%02d-%04d", day, monthIndex + 1, year);
            int dailyValue = prefs.getInt("Achieved_" + dateKey, 0);
            total += dailyValue;
        }

        // Save monthly total
        prefs.edit().putInt("data_" + shortMonth + "_" + year + "_Achieved", total).apply();
    }



}