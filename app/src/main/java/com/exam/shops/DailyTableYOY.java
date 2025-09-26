package com.exam.shops;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyTableYOY extends AppCompatActivity {
    Spinner spinnerMonth;
    LinearLayout tableContainer;
    String Holiday;
    float achievedValue;
    float quantities;
    float nobValue;

    String highPerDay;
    int TurnOver;
    Button btnCPDF;
    ImageView backArrow;

    float monthlyAchievedTotal = 0f;
    float monthlyAchievedPercent = 0f;
    int HighPerGrowthPer = 10;
    float monthlyTarget;

    SharedPreferences prefs;
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
        backArrow = findViewById(R.id.backArrow);

        Holiday = getIntent().getStringExtra("ShopHoliday");
        // ↓↓↓ read as float from YOYSecondActivity ↓↓↓
        achievedValue = getIntent().getFloatExtra("Achived_Value", 0f);
        quantities = getIntent().getFloatExtra("Quantity", 0f);
        nobValue = getIntent().getFloatExtra("NOB", 0f);
        highPerDay = getIntent().getStringExtra("HighPerDay");
        TurnOver = getIntent().getIntExtra("Growth", 0);

        publicHolidayList = loadHolidayDates();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getFincialYearMonths());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        String currentMonthLabel = getCurrentMonthLabel();
        int defaultPosition = adapter.getPosition(currentMonthLabel);
        spinnerMonth.setSelection(defaultPosition);

        backArrow.setOnClickListener(v -> onBackPressed());

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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
                    getExpectedSumForPastDays(15);
                    getExpectedSumForPastDays(7);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
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

        int startYear = (month >= Calendar.APRIL) ? year : (year - 1);

        String MonthNames[] = {"April", "May", "June", "July", "August", "September", "October", "November", "December",
                "January", "February", "March"};

        for (int i = 0; i < MonthNames.length; i++) {
            String label = (i < 9) ? (MonthNames[i] + " " + startYear)
                    : (MonthNames[i] + " " + (startYear + 1));
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
        if (month < Calendar.APRIL) year = year - 1;

        String[] MonthNames = {"April", "May", "June", "July", "August", "September", "October", "November", "December",
                "January", "February", "March"};

        if (month >= Calendar.APRIL && month <= Calendar.DECEMBER) {
            String current = MonthNames[month - Calendar.APRIL];
            return current + " " + year;
        } else {
            String current = MonthNames[month + 9];
            return current + " " + (year + 1);
        }
    }

    private float getSafeFloat(SharedPreferences prefs, String key, float defaultValue) {
        try {
            return prefs.getFloat(key, defaultValue);
        } catch (ClassCastException e) {
            try {
                Object value = prefs.getAll().get(key);
                if (value instanceof Integer) return ((Integer) value).floatValue();
                if (value instanceof Long) return ((Long) value).floatValue();
                if (value instanceof String) return Float.parseFloat((String) value);
                if (value instanceof Float) return (Float) value;
                return defaultValue;
            } catch (Exception ex) {
                return defaultValue;
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
            for (String day : highPerDay.split(",")) highPerfDaysList.add(day.trim());
        }

        int workingDays = 0, highPerfDays = 0;
        for (int i = 1; i <= daysInMonth; i++) {
            cal.set(Calendar.DAY_OF_MONTH, i);
            String dayStr = dayFormat.format(cal.getTime());
            if (!Holiday.equals(dayStr)) workingDays++;
            if (highPerfDaysList.contains(dayStr)) highPerfDays++;
        }
        SharedPreferences yoyPrefs = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM", Locale.ENGLISH);
        String shortMonth = sdf.format(startDate.getTime());

        String[] partsFY = getFinancialYear().split("_");
        int fyStartYear = Integer.parseInt(partsFY[0]);
        int dataYear = (shortMonth.equals("Jan") || shortMonth.equals("Feb") || shortMonth.equals("Mar")) ? fyStartYear + 1 : fyStartYear;

        monthlyTarget = getSafeFloat(yoyPrefs, "expected_" + shortMonth + "_" + dataYear, TurnOver / 12f);
        float growthMultiplier = 1 + (HighPerGrowthPer / 100f);

        List<String> prePublicHighPerfDates = new ArrayList<>();
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
                type = "Public Holiday"; rawTarget = 0f;
            } else if (Holiday.equals(dayStr)) {
                type = "Holiday"; rawTarget = 0f;
            } else if (highPerfDaysList.contains(dayStr) || prePublicHighPerfDates.contains(dateStr)) {
                type = "High Performance Day"; rawTarget = 1f * growthMultiplier;
            } else {
                type = "Working Day"; rawTarget = 1f;
            }
            dayTypes.add(type);
            rawTargets.add(rawTarget);
            if (rawTarget > 0f) rawTargetSum += rawTarget;
        }

      //  float scalingFactor = (rawTargetSum != 0f) ? (monthlyTarget / rawTargetSum) : 1f;

        List<Float> finalTargets = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences("Shop Data", MODE_PRIVATE);
        float totalEditedTarget = 0f;
        float totalAutoTargetWeight = 0f;
        List<Boolean> isUserEdited = new ArrayList<>();
        SharedPreferences.Editor editor = prefs.edit();

        for (int i = 0; i < rawTargets.size(); i++) {
            String dateKey = dateList.get(i);
            boolean isEdited = prefs.getBoolean("edited_" + dateKey, false);
            if (!isEdited) editor.remove("Expected_" + dateKey);
        }
        editor.apply();

        for (int i = 0; i < rawTargets.size(); i++) {
            String dateKey = dateList.get(i);
            float saved = getSafeFloat(prefs, "Expected_" + dateKey, -1f);
            boolean isEdited = prefs.getBoolean("edited_" + dateKey, false);

            if (isEdited && saved >= 0f) {
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
        float autoScaling = (totalAutoTargetWeight > 0f) ? (remainingTarget / totalAutoTargetWeight) : 0f;

        for (int i = 0; i < finalTargets.size(); i++) {
            if (!isUserEdited.get(i)) {
                float adjusted = rawTargets.get(i) * autoScaling;
                finalTargets.set(i, adjusted);
            }
        }

        float sum = 0f; for (float f : finalTargets) sum += f;
        Log.d("TARGET_CHECK", "Expected: " + monthlyTarget + " | Calculated sum: " + sum);

        for (int i = 0; i < daysInMonth; i++) {
            String dateStr = dateList.get(i);
            String type = dayTypes.get(i);
            float targetForDay = finalTargets.get(i);

            cal.set(Calendar.DAY_OF_MONTH, i + 1);
            String dayStr = dayFormat.format(cal.getTime());

            float savedExpected = getSafeFloat(prefs, "Expected_" + dateStr, -1f);
            if (savedExpected >= 0f) targetForDay = savedExpected;
            String formattedDailyTarget = String.format(Locale.getDefault(), "%.2f", targetForDay);

            editor.putFloat("Expected_" + dateStr, targetForDay);
            editor.apply();
            float achieved = getSafeFloat(prefs, "Achieved_" + dateStr, 0f);
            monthlyAchievedTotal += achieved;

            float quantity = getSafeFloat(prefs, "Quantity_" + dateStr, 0f);
            float NOB = getSafeFloat(prefs, "NOB_" + dateStr, 0f);
            float ABS = (NOB != 0f) ? (quantity / NOB) : 0f;
            float ATV = (NOB != 0f) ? (achieved / NOB) : 0f;
            float ASP = (quantity != 0f) ? (achieved / quantity) : 0f;
            float percentage = (targetForDay != 0f) ? (achieved / targetForDay) * 100f : 0f;

            View cardView = getLayoutInflater().inflate(R.layout.card_daily_info, null);

            ((TextView) cardView.findViewById(R.id.txtDate)).setText("Date: " + dateStr);
            ((TextView) cardView.findViewById(R.id.txtDay)).setText("Day: " + dayStr);
            ((TextView) cardView.findViewById(R.id.txtAchieved)).setText("Achieved: ₹" + String.format(Locale.getDefault(), "%.2f", achieved));
            ((TextView) cardView.findViewById(R.id.txtType)).setText("Type: " + type);
            ((TextView) cardView.findViewById(R.id.txtExpect)).setText("Expected: ₹" + formattedDailyTarget);
            ((TextView) cardView.findViewById(R.id.txtQuantity)).setText("Quantity: " + String.format(Locale.getDefault(), "%.2f", quantity));
            ((TextView) cardView.findViewById(R.id.txtNOB)).setText("NOB: " + String.format(Locale.getDefault(), "%.2f", NOB));
            ((TextView) cardView.findViewById(R.id.txtABS)).setText("ABS: " + String.format(Locale.getDefault(), "%.2f", ABS));
            ((TextView) cardView.findViewById(R.id.txtATV)).setText("ATV: ₹" + String.format(Locale.getDefault(), "%.2f", ATV));
            ((TextView) cardView.findViewById(R.id.txtASP)).setText("ASP: ₹" + String.format(Locale.getDefault(), "%.2f", ASP));

            LinearLayout detailsLayout = cardView.findViewById(R.id.detailsLayout);
            LinearLayout headerLayout = cardView.findViewById(R.id.headerLayout);
            headerLayout.setOnClickListener(v -> {
                detailsLayout.setVisibility(detailsLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            });

            tableContainer.addView(cardView);

            final float[] expectedValue = {targetForDay};
            final float[] achievedValue = {achieved};
            final float[] quantityValue = {quantity};
            final float[] nobValue = {NOB};

            TextView txtExpect = cardView.findViewById(R.id.txtExpect);
            TextView txtAchieved = cardView.findViewById(R.id.txtAchieved);
            TextView txtQuantity = cardView.findViewById(R.id.txtQuantity);
            TextView txtNOB = cardView.findViewById(R.id.txtNOB);
            TextView txtABS = cardView.findViewById(R.id.txtABS);
            TextView txtATV = cardView.findViewById(R.id.txtATV);
            TextView txtASP = cardView.findViewById(R.id.txtASP);
            TextView txtLoss = cardView.findViewById(R.id.txtLoss);

            Runnable updateMetrics = () -> {
                float achievedF = round2(achievedValue[0]);
                float expectedF = round2(expectedValue[0]);
                float qtyF      = quantityValue[0];
                float nobF      = nobValue[0];

                float diff2 = round2(achievedF - expectedF);
                MaterialButton btnLoss = cardView.findViewById(R.id.btnLoss);

                if (Math.abs(diff2) < 0.005f) {
                    txtLoss.setVisibility(View.GONE);
                    btnLoss.setVisibility(View.GONE);
                } else if (diff2 > 0f) {
                    txtLoss.setVisibility(View.VISIBLE);
                    txtLoss.setText("Profit: ₹" + String.format(Locale.getDefault(), "%.2f", diff2));
                    btnLoss.setVisibility(View.GONE);
                } else {
                    txtLoss.setVisibility(View.VISIBLE);
                    txtLoss.setText("Loss: ₹" + String.format(Locale.getDefault(), "%.2f", -diff2));
                    btnLoss.setVisibility(View.VISIBLE);
                    btnLoss.setText("Distribute Loss →");
                    btnLoss.setOnClickListener(v -> showDistributeLossDialog(cardView));
                }

                float atvF = (nobF != 0f) ? (achievedF / nobF) : 0f;
                float aspF = (qtyF != 0f) ? (achievedF / qtyF) : 0f;
                float absF = (nobF != 0f) ? (qtyF / nobF) : 0f;

                txtABS.setText("ABS: " + String.format(Locale.getDefault(), "%.2f", absF));
                txtATV.setText("ATV: ₹" + String.format(Locale.getDefault(), "%.2f", atvF));
                txtASP.setText("ASP: ₹" + String.format(Locale.getDefault(), "%.2f", aspF));
            };

            updateMetrics.run();

            TextView edtAmount = cardView.findViewById(R.id.edtAmount);
            String finalShortMonth1 = shortMonth;
            edtAmount.setOnClickListener(v -> {
                View dialogView = getLayoutInflater().inflate(R.layout.daily_alert, null);

                EditText edtTarget = dialogView.findViewById(R.id.edtTarget);
                EditText edtAchieved = dialogView.findViewById(R.id.edtAchived);
                EditText edtQty = dialogView.findViewById(R.id.edtQty);
                EditText edtNob = dialogView.findViewById(R.id.edtNob);
                MaterialButton btnSave = dialogView.findViewById(R.id.btnSave);

                float latestExpected = getSafeFloat(prefs, "Expected_" + dateStr, expectedValue[0]);
                float latestAchieved = getSafeFloat(prefs, "Achieved_" + dateStr, achievedValue[0]);
                float latestQty = getSafeFloat(prefs, "Quantity_" + dateStr, quantityValue[0]);
                float latestNob = getSafeFloat(prefs, "NOB_" + dateStr, nobValue[0]);

                edtTarget.setText(String.format(Locale.getDefault(), "%.2f", latestExpected));
                edtAchieved.setText(String.valueOf(latestAchieved));
                edtQty.setText(String.valueOf(latestQty));
                edtNob.setText(String.valueOf(latestNob));

                edtTarget.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                edtAchieved.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                edtQty.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                edtNob.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                AlertDialog dialog = new AlertDialog.Builder(this)
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

                    expectedValue[0] = Float.parseFloat(targetStr);
                    achievedValue[0] = Float.parseFloat(achievedStr);
                    quantityValue[0] = Float.parseFloat(qtyStr);
                    nobValue[0] = Float.parseFloat(nobStr);

                    editor.putFloat("Expected_" + dateStr, expectedValue[0]);
                    editor.putFloat("Achieved_" + dateStr, achievedValue[0]);
                    editor.putFloat("Quantity_" + dateStr, quantityValue[0]);
                    editor.putFloat("NOB_" + dateStr, nobValue[0]);
                    editor.putBoolean("edited_" + dateStr, true);
                    editor.apply();

                    try {
                        Date date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(dateStr);
                        Calendar localCal = Calendar.getInstance();
                        localCal.setTime(date);
                        int mIndex = localCal.get(Calendar.MONTH);
                        int y = localCal.get(Calendar.YEAR);

                        float totalAchieved = 0f;
                        int days = localCal.getActualMaximum(Calendar.DAY_OF_MONTH);
                        for (int day = 1; day <= days; day++) {
                            String key = String.format(Locale.getDefault(), "%02d-%02d-%04d", day, mIndex + 1, y);
                            totalAchieved += getSafeFloat(prefs, "Achieved_" + key, 0f);
                        }

                        SharedPreferences yoy = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);
                        yoy.edit()
                                .putFloat("data_" + finalShortMonth1 + "_" + y + "_Achieved", totalAchieved)
                                .apply();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Update UI
                    txtExpect.setText("Expected: ₹" + String.format(Locale.getDefault(), "%.2f", expectedValue[0]));
                    txtAchieved.setText("Achieved: ₹" + String.format(Locale.getDefault(), "%.2f", achievedValue[0]));
                    txtQuantity.setText("Quantity: " + String.format(Locale.getDefault(), "%.2f", quantityValue[0]));
                    txtNOB.setText("NOB: " + String.format(Locale.getDefault(), "%.2f", nobValue[0]));

                    // Rebalance targets if needed (delta handled by comparing old/new in your flow)
                    rebalanceTargets(dateStr, 0f);

                    updateMetrics.run();
                    dialog.dismiss();
                });

                dialog.show();
            });

            cardView.findViewById(R.id.btnLoss).setOnClickListener(v -> showDistributeLossDialog(cardView));

            String todayStr = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
            if (dateStr.equals(todayStr)) {
                editor.putString("today_expected", formattedDailyTarget);
                editor.putFloat("today_achieved", achieved);     // float now
                editor.putString("today_type", type);
                editor.putFloat("today_percent", percentage);

                editor.putInt("today_abs", (int) ABS);           // ABS kept as int for your other screen
                editor.putFloat("today_atv", ATV);
                editor.putFloat("today_asp", ASP);
                editor.apply();
            }
        }

        // monthly summary
        shortMonth = sdf.format(startDate.getTime());
        partsFY = getFinancialYear().split("_");
        fyStartYear = Integer.parseInt(partsFY[0]);
        dataYear = (shortMonth.equals("Jan") || shortMonth.equals("Feb") || shortMonth.equals("Mar")) ? fyStartYear + 1 : fyStartYear;

        monthlyTarget = getSafeFloat(getSharedPreferences("YOY_PREFS", MODE_PRIVATE),
                "expected_" + shortMonth + "_" + dataYear, TurnOver / 12f);
        monthlyAchievedPercent = (monthlyTarget != 0f) ? (monthlyAchievedTotal * 100f / monthlyTarget) : 0f;

        this.originalWeights = new ArrayList<>(rawTargets);
        this.dateListGlobal = new ArrayList<>(dateList);
        this.finalTargetsGlobal = new ArrayList<>(finalTargets);
        this.dayTypesGlobal = new ArrayList<>(dayTypes);
    }

    private float calculateLatestMonthlyAchievedTotal() {
        SharedPreferences prefs = getSharedPreferences("Shop Data", MODE_PRIVATE);
        float sum = 0f;
        for (String date : dateListGlobal) {
            sum += getSafeFloat(prefs, "Achieved_" + date, 0f);
        }
        return sum;
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
            monthlyTarget = getSafeFloat(yoyPrefs, "expected_" + shortMonth + "_" + dataYear, TurnOver / 12f);
        }

        float totalEdited = 0f, totalEditableWeight = 0f;
        for (int i = 0; i < dateListGlobal.size(); i++) {
            String dateKey = dateListGlobal.get(i);
            String type = dayTypesGlobal.get(i);
            boolean isEdited = prefs.getBoolean("edited_" + dateKey, false);
            if (isEdited) {
                float editedVal = getSafeFloat(prefs, "Expected_" + dateKey, 0f);
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

            editor.putFloat("Expected_" + dateKey, newExpected);
            editor.putBoolean("edited_" + dateKey, false);

            View card = findCardByDate(dateKey);
            if (card != null) {
                TextView txtExpect = card.findViewById(R.id.txtExpect);
                txtExpect.setText("Expected: ₹" + String.format(Locale.getDefault(), "%.2f", newExpected));
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
                    if (dateInCard.equals(targetDate)) return card;
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
            float expected = getSafeFloat(prefs, "Expected_" + dateKey, -1f);
            if (expected >= 0f) totalExpected += expected;
        }
        return totalExpected;
    }

    private String getFinancialYear() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int startYear = (month >= Calendar.APRIL) ? year : (year - 1);
        int endYear = (month >= Calendar.APRIL) ? (year + 1) : year;
        return startYear + "_" + String.valueOf(endYear).substring(2);
    }

    private List<String> loadHolidayDates() {
        List<String> holidays = new ArrayList<>();
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        try {
            InputStream is = getAssets().open("public_holidays.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer); is.close();
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
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
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
        TextView txtDate = cardView.findViewById(R.id.txtDate);
        String lossDateText = txtDate.getText().toString().replace("Date: ", "").trim();

        float expOnLossDay = round2(getSafeFloat(prefs, "Expected_" + lossDateText, 0f));
        float achOnLossDay = round2(getSafeFloat(prefs, "Achieved_" + lossDateText, 0f));
        float lossAmount   = round2(expOnLossDay - achOnLossDay);

        if (Math.abs(lossAmount) < 0.005f) {
            Toast.makeText(this, "No loss to distribute", Toast.LENGTH_SHORT).show();
            TextView txtLoss = cardView.findViewById(R.id.txtLoss);
            MaterialButton btnLoss = cardView.findViewById(R.id.btnLoss);
            if (txtLoss != null) txtLoss.setVisibility(View.GONE);
            if (btnLoss != null) btnLoss.setVisibility(View.GONE);
            return;
        }


        if (lossAmount <= 0f) {
            Toast.makeText(this, "No loss to distribute", Toast.LENGTH_SHORT).show();
            return;
        }
        if (days <= 0) {
            Toast.makeText(this, "Number of days must be greater than zero", Toast.LENGTH_SHORT).show();
            return;
        }

        float lossPerDay = lossAmount / days;
        int count = 0;

        // 2) Parse date and set calendar
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        try {
            Date lossDate = dateFormat.parse(lossDateText);
            cal.setTime(lossDate);
        } catch (Exception e) {
            Log.e("LossDistribution", "Invalid date format for: " + lossDateText, e);
            Toast.makeText(this, "Invalid loss date format", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3) Apply full subtraction to the loss day (and mark as edited)
        float adjustedExpected = Math.max(0f, expOnLossDay - lossAmount);
        prefs.edit()
                .putFloat("Expected_" + lossDateText, adjustedExpected)
                .putBoolean("edited_" + lossDateText, true)   // keep on refresh
                .apply();

        // Update that card’s UI now
        View lossDayCard = findCardByDate(lossDateText);
        if (lossDayCard != null) {
            TextView txtExpect = lossDayCard.findViewById(R.id.txtExpect);
            txtExpect.setText("Expected: ₹" + String.format(Locale.getDefault(), "%.2f", adjustedExpected));
            recomputeCardMetrics(lossDayCard); // -> updates Profit/Loss label & button visibility
        }

        Log.d("LossDistribution", "Adjusted expected for loss day: " + lossDateText + " | New Expected: " + adjustedExpected);

        // Move to the next day
        cal.add(Calendar.DAY_OF_MONTH, 1);

        // 4) Distribute to future working days
        List<String> publicHolidays = loadHolidayDates();
        SharedPreferences.Editor editor = prefs.edit();
        Log.d("LossDistribution", "Total Loss: " + lossAmount + " | Per Day Loss: " + lossPerDay);

        while (count < days) {
            String dateStr = String.format(Locale.getDefault(), "%02d-%02d-%04d",
                    cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
            String dayStr = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());

            if (isWorkingDay(dayStr, dateStr, publicHolidays, Holiday)) {
                float currentTarget = getSafeFloat(prefs, "Expected_" + dateStr, 0f);
                float newTarget = currentTarget + lossPerDay;

                editor.putFloat("Expected_" + dateStr, newTarget);
                editor.putBoolean("edited_" + dateStr, true);
                editor.apply();

                View targetCard = findCardByDate(dateStr);
                if (targetCard != null) {
                    TextView txtExpect = targetCard.findViewById(R.id.txtExpect);
                    txtExpect.setText("Expected: ₹" + String.format(Locale.getDefault(), "%.2f", newTarget));
                    recomputeCardMetrics(targetCard); // update Profit/Loss immediately
                }

                Log.d("LossDistribution", "Distributed to: " + dateStr + " | New Target: " + newTarget);
                count++;
            } else {
                Log.d("LossDistribution", "Skipping holiday: " + dateStr + " (" + dayStr + ")");
            }
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        // 5) Clear the loss label on the original card now
        TextView txtLoss = cardView.findViewById(R.id.txtLoss);
        txtLoss.setText("Loss: ₹0.00");

        Toast.makeText(this, "Loss distributed over " + days + " working days", Toast.LENGTH_SHORT).show();

        // Optional: full refresh (safe to keep)
        refreshCurrentMonth();
    }

    private boolean isWorkingDay(String day, String date, List<String> publicHolidays, String shopHoliday) {
        if (prefs == null) prefs = getSharedPreferences("Shop Data", MODE_PRIVATE);
        return !(day.equalsIgnoreCase(shopHoliday) || publicHolidays.contains(date));
    }
    private void refreshCurrentMonth() {
        Object selected = spinnerMonth.getSelectedItem();
        if (selected == null) return;
        String[] parts = selected.toString().split(" ");
        if (parts.length != 2) return;

        int monthIndex = getMonthIndex(parts[0]);
        int year = Integer.parseInt(parts[1]);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, monthIndex);
        c.set(Calendar.DAY_OF_MONTH, 1);
        generateDateRows(c);
    }
    private float round2(float v) { return Math.round(v * 100f) / 100f; }
    private float parseMoney(String s) {
        try { return Float.parseFloat(s.replaceAll("[^\\d.]", "")); }
        catch (Exception e) { return 0f; }
    }
private void recomputeCardMetrics(View card) {
    if (card == null) return;
    TextView txtAch  = card.findViewById(R.id.txtAchieved);
    TextView txtExp  = card.findViewById(R.id.txtExpect);
    TextView txtLoss = card.findViewById(R.id.txtLoss);
    MaterialButton btnLoss = card.findViewById(R.id.btnLoss);

    float achievedF = round2(parseMoney(txtAch.getText().toString()));
    float expectedF = round2(parseMoney(txtExp.getText().toString()));
    float diff2 = round2(achievedF - expectedF);

    if (Math.abs(diff2) < 0.005f) {
        txtLoss.setVisibility(View.GONE);
        if (btnLoss != null) btnLoss.setVisibility(View.GONE);
        return;
    }
    if (diff2 > 0f) {
        txtLoss.setVisibility(View.VISIBLE);
        txtLoss.setText("Profit: ₹" + String.format(Locale.getDefault(), "%.2f", diff2));
        if (btnLoss != null) btnLoss.setVisibility(View.GONE);
    } else {
        txtLoss.setVisibility(View.VISIBLE);
        txtLoss.setText("Loss: ₹" + String.format(Locale.getDefault(), "%.2f", -diff2));
        if (btnLoss != null) {
            btnLoss.setVisibility(View.VISIBLE);
            btnLoss.setText("Distribute Loss →");
        }
    }
}


}
