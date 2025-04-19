package com.exam.shops;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    //int SecondTurnOverValue;
    int Result;

    int achievedValue;
    int quantities;
    int nobValue;
    String highPerDay;
    int growthPer;
    int achieved;
    Button btnCPDF;

    float monthlyAchievedTotal = 0f;
    float monthlyTarget = 0f;
    float monthlyAchievedPercent = 0f;


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
        btnCPDF = findViewById(R.id.btnCPDF);


        Holiday = getIntent().getStringExtra("ShopHoliday");
        Result = getIntent().getIntExtra("ResultTurnYear",0);
       // SecondTurnOverValue = getIntent().getIntExtra("TurnYear", 0);
        achievedValue = getIntent().getIntExtra("Achived_Value", 0);
        quantities = getIntent().getIntExtra("Quantity", 0);
        nobValue = getIntent().getIntExtra("NOB", 0);
        highPerDay = getIntent().getStringExtra("HighPerDay");
        growthPer = getIntent().getIntExtra("Growth", 0);

Log.d("YOYDAILY","Result turnover is : "+Result);
        Log.d("High", "High Performance Day : " + Holiday);
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

        btnCPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generatePDF();

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

    private void generateDateRows(Calendar startDate) {
        tableContainer.removeAllViews();
        monthlyAchievedTotal = 0f; // Reset when month changes


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

        float monthlyTarget = (float) Result / 12f;
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
            } else if (highPerfDaysList.contains(dayStr)) {
                type = "High Performance Day";
                targetForDay = dailyTarget * growthMultiplier;
            } else {
                type = "Working Day";
                targetForDay = dailyTarget;
            }

            String formattedDailyTarget = String.format("%.2f", targetForDay);

            // Fetch performance data
            SharedPreferences prefs = getSharedPreferences("Shop Data", MODE_PRIVATE);
            achieved = prefs.getInt("Achieved_" + dateStr, 0);
            monthlyAchievedTotal += achieved;

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
            TextView txtAchievedPer = cardView.findViewById(R.id.txtAchievedPer);
            TextView txtABS = cardView.findViewById(R.id.txtABS);
            TextView txtATV = cardView.findViewById(R.id.txtATV);
            TextView txtASP = cardView.findViewById(R.id.txtASP);

            Runnable updateMetrics = () -> {
                int achieved = achievedValue[0];
                float expected = expectedValue[0];
                int qty = quantityValue[0];
                int nob = nobValue[0];

                float percent = (expected != 0) ? (achieved / expected) * 100f : 0f;
                float atv = (nob != 0) ? (float) achieved / nob : 0f;
                float asp = (qty != 0) ? (float) achieved / qty : 0f;
                int abs = (nob != 0) ? qty / nob : 0;

                txtAchievedPer.setText(String.format("Achieved %%: %.2f%%", percent));
                txtABS.setText("ABS: " + abs);
                txtATV.setText("ATV: " + String.format(Locale.US, "%.2f", atv));
                txtASP.setText("ASP: " + String.format(Locale.US, "%.2f", asp));


                if (percent < 70) {
                    txtAchievedPer.setTextColor(Color.parseColor("#4CAF50"));
                } else if (percent < 90) {
                    txtAchievedPer.setTextColor(Color.BLACK);
                } else {
                    txtAchievedPer.setTextColor(Color.parseColor("#F44336"));
                }


                TodayDataUtils.updateTodayData(DailyTableYOY.this);


            };

            cardView.findViewById(R.id.btnEditExpected).setOnClickListener(v -> {
                showEditFieldDialog("Expected", String.valueOf(expectedValue[0]), newVal -> {
                    expectedValue[0] = Float.parseFloat(newVal);
                    txtExpect.setText("Expected: ₹" + newVal);
                    updateMetrics.run();
                });
            });


            cardView.findViewById(R.id.btnEditAchieved).setOnClickListener(v -> {
                showEditFieldDialog("Achieved", String.valueOf(achievedValue[0]), newVal -> {
                    achievedValue[0] = Integer.parseInt(newVal);
                    txtAchieved.setText("Achieved: ₹" + newVal);
                    prefs.edit().putInt("Achieved_" + dateStr, achievedValue[0]).apply();
                    updateMetrics.run();
                });
            });

            cardView.findViewById(R.id.btnEditQuantity).setOnClickListener(v -> {
                showEditFieldDialog("Quantity", String.valueOf(quantityValue[0]), newVal -> {
                    quantityValue[0] = Integer.parseInt(newVal);
                    txtQuantity.setText("Quantity: " + newVal);
                    prefs.edit().putInt("Quantity_" + dateStr, quantityValue[0]).apply();
                    updateMetrics.run();
                });
            });

            cardView.findViewById(R.id.btnEditNOB).setOnClickListener(v -> {
                showEditFieldDialog("NOB", String.valueOf(nobValue[0]), newVal -> {
                    nobValue[0] = Integer.parseInt(newVal);
                    txtNOB.setText("NOB: " + newVal);
                    prefs.edit().putInt("NOB_" + dateStr, nobValue[0]).apply();
                    updateMetrics.run();
                });
            });


            String todayStr = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());

            if (dateStr.equals(todayStr)) {
                SharedPreferences.Editor editor = getSharedPreferences("TodayData", MODE_PRIVATE).edit();
                editor.putString("today_expected", formattedDailyTarget);
                editor.putInt("today_achieved", achieved);
                editor.putString("today_type", type);
                editor.putFloat("today_percent", percentage);

                editor.putInt("today_abs",ABS);
                editor.putFloat("today_atv", ATV);
                editor.putFloat("today_asp", ASP);

                editor.apply();


        }


        }
        monthlyTarget = Result / 12f;
        monthlyAchievedPercent = (monthlyTarget != 0) ? (monthlyAchievedTotal * 100f / monthlyTarget) : 0f;

        Log.d("MonthlySummary", "Achieved: " + monthlyAchievedTotal + " | Percent: " + monthlyAchievedPercent);

    }

    private interface OnFieldUpdated {
        void onUpdated(String newValue);
    }

    private void showEditFieldDialog(String title, String currentValue, OnFieldUpdated callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit " + title);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        try {
            float value = Float.parseFloat(currentValue);
            input.setText(String.format(Locale.US, "%.2f", value));
        } catch (NumberFormatException e) {
            input.setText(currentValue);
        }

        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newValue = input.getText().toString().trim();
            callback.onUpdated(newValue);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }


    private void generatePDF() {
        PdfDocument document = new PdfDocument();

        int pageWidth = 595;
        int pageHeight = 842;

        int startX = 20;
        int startY = 50;
        int tableTop = startY + 40;
        int rowHeight = 35;

        int[] columnWidths = {75, 75, 140, 60, 60, 70, 40, 40}; // Fixed the last column
        String[] headers = {"Date", "Day", "Type", "Expect", "Ach", "Ach%", "Qty", "NOB"};
        int[] columnPositions = getColumnPositions(startX, columnWidths);

        Paint paint = new Paint();
        paint.setTextSize(12f);
        paint.setColor(Color.BLACK);

        Paint boldPaint = new Paint(paint);
        boldPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        Paint titlePaint = new Paint(boldPaint);
        titlePaint.setTextSize(16f);

        Paint linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(1f);

        Paint headerBgPaint = new Paint();
        headerBgPaint.setColor(Color.LTGRAY);

        int pageNum = 1;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Title
        canvas.drawText("Monthly Performance Report", startX, startY, titlePaint);

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

            String date = ((TextView) card.findViewById(R.id.txtDate)).getText().toString().replace("Date: ", "");
            String day = ((TextView) card.findViewById(R.id.txtDay)).getText().toString().replace("Day: ", "");
            String type = ((TextView) card.findViewById(R.id.txtType)).getText().toString().replace("Type: ", "");
            String expected = ((TextView) card.findViewById(R.id.txtExpect)).getText().toString().replace("Expected: ", "");
            String achieved = ((TextView) card.findViewById(R.id.txtAchieved)).getText().toString().replace("Achieved: ", "");
            String percent = ((TextView) card.findViewById(R.id.txtAchievedPer)).getText().toString().replace("Achieved %: ", "").replace("%", "");
            String qty = ((TextView) card.findViewById(R.id.txtQuantity)).getText().toString().replace("Quantity: ", "");
            String nob = ((TextView) card.findViewById(R.id.txtNOB)).getText().toString().replace("NOB: ", "");

            String[] rowData = {date, day, type, expected, achieved, percent + "%", qty, nob};

            // Draw row data
            x = startX;
            for (int j = 0; j < rowData.length; j++) {
                canvas.drawText(rowData[j], x + 5, y + 25, paint);
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
                    "MonthlyReport_" + System.currentTimeMillis() + ".pdf");

            FileOutputStream out = new FileOutputStream(file);
            document.writeTo(out);
            document.close();
            out.close();

            Toast.makeText(this, "PDF saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

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
        Intent intent = new Intent();
        intent.putExtra("MonthlyAchieved", monthlyAchievedTotal);
        intent.putExtra("MonthlyAchievedPercent", monthlyAchievedPercent);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }





}