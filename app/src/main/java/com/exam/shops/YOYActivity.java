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
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class YOYActivity extends AppCompatActivity {

    TextView txtTurnOver;
    LinearLayout tableLayout;
    int Turnover;
    //int Result;
    String financialYear;

    List<String> months = Arrays.asList(
            "April", "May", "June", "July", "August", "September",
            "October", "November", "December", "January", "February", "March"
    );
    Map<String, Float> monthTargetMap = new HashMap<>();

    Button btnExportPdf;
    ImageView backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoyactivity);

        txtTurnOver = findViewById(R.id.txtTurnOver);
        tableLayout = findViewById(R.id.tableLayout);
        btnExportPdf = findViewById(R.id.btnExportPdf);
        backArrow = findViewById(R.id.backArrow);

        String mobileNumber = getIntent().getStringExtra("Mobile_no");
        Log.d("YOYActivity", "Mobile number received: " + mobileNumber);

        Turnover = getIntent().getIntExtra("EdtGrowth", 0);
        txtTurnOver.setText("Yearly Target : " + String.valueOf(Turnover));

        financialYear = getCurrentFinancialYear();


        SharedPreferences sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        Turnover = getIntent().getIntExtra("EdtGrowth", sharedPref.getInt("EdtGrowth", 0));


        addMonthRows();

        int total = getTotalAchievedInYear();
        float percentOfYear = (Turnover != 0) ? (total * 100.0f / Turnover) : 0;

        Log.d("TotalAchieved", "Total Achieved is: " + total);
        Log.d("TotalPercent", "Total Achieved %: " + percentOfYear);


        SharedPreferences sharedPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        sharedPrefs.edit()
                .putInt("TotalAchievedValue", total)
                .putFloat("TotalAchievedPercentage", percentOfYear)
                .apply();
        saveMonthTargetMap((HashMap<String, Float>) monthTargetMap);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                int total = getTotalAchievedInYear();
                float percentOfYear = (Turnover != 0) ? (total * 100.0f / Turnover) : 0;

                Intent intent = new Intent(YOYActivity.this, GoToMAndD.class);
                intent.putExtra("TotalAchived", total);
                intent.putExtra("TotalAchPer", percentOfYear);
                intent.putExtra("Mobile_no", mobileNumber);
                HashMap<String, Float> monthTargetMapToPass = new HashMap<>(monthTargetMap);
                intent.putExtra("month_target_map", monthTargetMapToPass);
                Log.d("monthTargetMApToPass", "Monthis :" + monthTargetMapToPass);
                startActivity(intent);
                finishAffinity();
            }
        });

        getAllMonthlyExpectedValues();

        btnExportPdf.setOnClickListener(v -> generatePdfWithTable());
        backArrow.setOnClickListener(v ->
                onBackPressed()
                );
    }

    private void saveMonthTargetMap(HashMap<String, Float> monthTargetMap) {
        SharedPreferences sharedPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        JSONObject jsonObject = new JSONObject();
        try {
            for (String key : monthTargetMap.keySet()) {
                jsonObject.put(key, monthTargetMap.get(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        editor.putString("month_target_map", jsonObject.toString());
        editor.apply();

        Log.d("SharedPrefSave", "Month target map saved: " + jsonObject.toString());
    }


    @Override
    protected void onResume() {
        super.onResume();

        Turnover = getSharedPreferences("shop_data", MODE_PRIVATE).getInt("editGrowth", 0);
        txtTurnOver.setText("Yearly Target : " + Turnover);
        tableLayout.removeAllViews();
        addMonthRows();
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



    private void addMonthRows() {
        SharedPreferences prefs = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);
        String[] parts = financialYear.split("_");
        int fyStartYear = Integer.parseInt(parts[0]);

        for (String month : months) {
            String shortMonth = convertToShortMonth(month);
            int dataYear = (shortMonth.equals("Jan") || shortMonth.equals("Feb") || shortMonth.equals("Mar")) ? fyStartYear + 1 : fyStartYear;

            float expected = getSafeFloat(prefs, "expected_" + shortMonth + "_" + dataYear, Turnover / 12.0f);
            int achieved = prefs.getInt("data_" + shortMonth + "_" + dataYear + "_Achieved", 0);

            // ✅ CardView
            CardView cardView = new CardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 24); // spacing between cards
            cardView.setLayoutParams(cardParams);
            cardView.setRadius(24f);
            cardView.setCardElevation(4f);
            cardView.setUseCompatPadding(true);
            cardView.setPreventCornerOverlap(false);
            cardView.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_cardview_border));

            // Parent vertical layout inside card
            LinearLayout parentLayout = new LinearLayout(this);
            parentLayout.setOrientation(LinearLayout.VERTICAL);
            parentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            parentLayout.setPadding(32, 32, 32, 32);

            // Month TextView
            TextView tvMonth = createText(month + " " + dataYear, 22, Typeface.NORMAL);
            LinearLayout.LayoutParams monthParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            monthParams.setMargins(0, 0, 0, 28);
            tvMonth.setLayoutParams(monthParams);
            parentLayout.addView(tvMonth);

            // Target & Achieved Layout
            LinearLayout targetAchievedLayout = new LinearLayout(this);
            targetAchievedLayout.setOrientation(LinearLayout.VERTICAL);

            // Row 1: Labels
            LinearLayout labelRow = new LinearLayout(this);
            labelRow.setOrientation(LinearLayout.HORIZONTAL);
            labelRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            TextView tvTargetLabel = createText("Target", 18, Typeface.NORMAL);
            tvTargetLabel.setTextColor(Color.GRAY);
            TextView tvAchievedLabel = createText("Achieved", 18, Typeface.NORMAL);
            tvAchievedLabel.setTextColor(Color.GRAY);

            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvTargetLabel.setLayoutParams(labelParams);
            tvAchievedLabel.setLayoutParams(labelParams);

            labelRow.addView(tvTargetLabel);
            labelRow.addView(tvAchievedLabel);

            // Row 2: Values
            LinearLayout valueRow = new LinearLayout(this);
            valueRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams valueRowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            valueRowParams.topMargin = (int) (2 * getResources().getDisplayMetrics().density);
            valueRow.setLayoutParams(valueRowParams);

            LinearLayout leftValueLayout = new LinearLayout(this);
            leftValueLayout.setOrientation(LinearLayout.HORIZONTAL);
            leftValueLayout.setLayoutParams(labelParams);

            TextView tvTarget = createText("₹" + String.format("%.0f", expected), 16, Typeface.NORMAL);
            tvTarget.setTag("target_" + shortMonth + "_" + dataYear);
            leftValueLayout.addView(tvTarget);

            LinearLayout rightValueLayout = new LinearLayout(this);
            rightValueLayout.setOrientation(LinearLayout.HORIZONTAL);
            rightValueLayout.setLayoutParams(labelParams);

            TextView tvAchieved = createText("₹" + String.valueOf(achieved), 16, Typeface.NORMAL);
            rightValueLayout.addView(tvAchieved);

            valueRow.addView(leftValueLayout);
            valueRow.addView(rightValueLayout);

            targetAchievedLayout.addView(labelRow);
            targetAchievedLayout.addView(valueRow);

            parentLayout.addView(targetAchievedLayout);

            // Profit/Loss row
            float result = achieved - expected;
            if (Math.abs(result) > 0.001f) {
                float displayAmount = Math.abs(result);

                LinearLayout horizontalLayout = new LinearLayout(this);
                horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams horizontalParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                horizontalParams.setMargins(0, 28, 0, 0);
                horizontalLayout.setLayoutParams(horizontalParams);

                LinearLayout verticalLayout = new LinearLayout(this);
                verticalLayout.setOrientation(LinearLayout.VERTICAL);
                verticalLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                TextView tvLabel = createText(result > 0 ? "Profit this month" : "Loss this month", 18, Typeface.NORMAL);
                tvLabel.setTextColor(Color.GRAY);
                verticalLayout.addView(tvLabel);

                TextView tvAmount = createText("₹" + String.format("%.0f", displayAmount), 16, Typeface.BOLD);
                verticalLayout.addView(tvAmount);

                horizontalLayout.addView(verticalLayout);

                if (result < 0) {
                    Button btnDistribute = new Button(this);
                    btnDistribute.setText("Distribute loss →");
                    btnDistribute.setTextColor(Color.BLACK);
                    btnDistribute.setTextSize(12);
                    btnDistribute.setAllCaps(false);
                    btnDistribute.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_distribute_button));
                    btnDistribute.setPadding(32, 24, 32, 24);
                    btnDistribute.setMinHeight(0);
                    btnDistribute.setMinimumHeight(0);
                    btnDistribute.setOnClickListener(v -> showDistributeDialog(displayAmount, shortMonth, dataYear, tvAmount));
                    horizontalLayout.addView(btnDistribute);
                }

                parentLayout.addView(horizontalLayout);
            }

            // Divider + Edit amount
            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 4));
            divider.setBackgroundColor(Color.parseColor("#E0E0E0"));

            TextView editAmount = new TextView(this);
            editAmount.setText("Edit amount  →");
            editAmount.setTextColor(Color.parseColor("#FF5722"));
            editAmount.setTextSize(16);
            editAmount.setGravity(Gravity.END);
            editAmount.setPadding(32, 32, 32, 32);
            editAmount.setBackgroundColor(Color.parseColor("#FAFAFA"));
            editAmount.setOnClickListener(v -> showSingleEditDialog(tvTarget, tvAchieved,  shortMonth, true));

            parentLayout.addView(divider);
            parentLayout.addView(editAmount);


            // Container (so we can overlay badge + parentLayout)
            RelativeLayout container = new RelativeLayout(this);
            container.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            ));

// Badge TextView
            TextView statusBadge = new TextView(this);
            RelativeLayout.LayoutParams badgeParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            badgeParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            badgeParams.setMargins(0, 16, 16, 0);
            statusBadge.setLayoutParams(badgeParams);
            statusBadge.setPadding(32, 12, 32, 12);
            statusBadge.setTextSize(12);
            statusBadge.setTextColor(Color.WHITE);
            statusBadge.setTypeface(Typeface.DEFAULT_BOLD);

// Decide text + background
            if (achieved > expected) {
                statusBadge.setText("• Profit");
                statusBadge.setTextColor(Color.parseColor("#2E7D32"));
                statusBadge.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_status_profit));
            } else if (achieved < expected) {
                statusBadge.setText("• Loss");
                statusBadge.setTextColor(Color.parseColor("#C62828"));
                statusBadge.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_status_loss));
            } else {
                statusBadge.setText("• Breakeven");
                statusBadge.setTextColor(Color.parseColor("#1565C0"));
                statusBadge.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_status_breakeven));
            }

// Add child layouts
            container.addView(parentLayout);
            container.addView(statusBadge);

// Finally add container to CardView
            cardView.addView(container);




           // cardView.addView(parentLayout);
            tableLayout.addView(cardView); // ✅ spacing works now

            monthTargetMap.put(shortMonth + "_" + dataYear, expected);
        }
    }


    private int getTotalAchievedInYear() {
        SharedPreferences prefs = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);
        String[] parts = financialYear.split("_");
        int fyStartYear = Integer.parseInt(parts[0]);
        int totalAchieved = 0;

        for (String month : months) {
            String shortMonth = convertToShortMonth(month);
            int dataYear = (shortMonth.equals("Jan") || shortMonth.equals("Feb") || shortMonth.equals("Mar"))
                    ? fyStartYear + 1 : fyStartYear;

            String key = "data_" + shortMonth + "_" + dataYear + "_Achieved";
            totalAchieved += prefs.getInt(key, 0);
        }
        Log.d("TotalAchieved", "Total Achived is :" + totalAchieved);
        return totalAchieved;


    }
    private void showSingleEditDialog(TextView tvTarget, TextView tvAchieved,
                                      String shortMonth, boolean isTargetEdit) {

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_target_acived_edit, null);

        EditText edtTarget = dialogView.findViewById(R.id.edtTarget);
        EditText edtAchieved = dialogView.findViewById(R.id.edtAchived);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSave);

        // Prefill existing values
        edtTarget.setText(parseNumericOnly(tvTarget.getText().toString()));
        edtAchieved.setText(parseNumericOnly(tvAchieved.getText().toString()));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnSave.setOnClickListener(v -> {
            String targetValue = edtTarget.getText().toString().trim();
            String achievedValue = edtAchieved.getText().toString().trim();

            float targetFloat = parseFloat(targetValue);
            float achievedFloat = parseFloat(achievedValue);

            SharedPreferences prefs = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            String[] parts = financialYear.split("_");
            int fyStartYear = Integer.parseInt(parts[0]);
            int dataYear = (shortMonth.equals("Jan") || shortMonth.equals("Feb") || shortMonth.equals("Mar"))
                    ? fyStartYear + 1 : fyStartYear;

            // ✅ Save edited month
            editor.putFloat("expected_" + shortMonth + "_" + dataYear, targetFloat);
            editor.putBoolean("edited_" + shortMonth + "_" + dataYear, true);
            editor.putInt("data_" + shortMonth + "_" + dataYear + "_Achieved", (int) achievedFloat);
            editor.apply();

            // ✅ Rebalance other months so that sum = yearly target
            redistributeToUneditedMonths(shortMonth, dataYear);

            // ✅ Update UI text
            tvTarget.setText("Target: ₹" + targetValue);
            tvAchieved.setText("Achieved: ₹" + achievedValue);

            addTextWatcher(tvTarget, tvAchieved, null, shortMonth);

            dialog.dismiss();
            showCustomToast("Updated Successfully!");
        });


        dialog.show();
    }


    private void addTextWatcher(TextView expectedView, TextView achievedView, TextView percentView, String shortMonth) {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                float expected = parseFloat(expectedView.getText().toString());
                float achieved = parseFloat(achievedView.getText().toString());

                String percentText = "--";
                int colorResId = R.color.Black;

                if (expected != 0) {
                    float percent = (achieved / expected) * 100;
                    percentText = String.format("%.2f%%", percent);
                    if (percent >= 0 && percent < 70) {
                        colorResId = R.color.Green;
                    } else if (percent >= 70 && percent < 90) {
                        colorResId = R.color.Black;
                    } else {
                        colorResId = R.color.Red;
                    }
                }

                percentView.setText(percentText);
                percentView.setTextColor(ContextCompat.getColor(getApplicationContext(), colorResId));

                SharedPreferences.Editor editor = getSharedPreferences("YOY_PREFS", MODE_PRIVATE).edit();
                editor.putString("data_" + shortMonth + "_" + financialYear, expectedView.getText().toString());
                editor.putString("data_" + shortMonth + "_" + financialYear + "_Achieved", achievedView.getText().toString());
                editor.putString("data_" + shortMonth + "_" + financialYear + "_AchievedPecentage", percentText);
                editor.apply();
            }
        };

        expectedView.addTextChangedListener(watcher);
        achievedView.addTextChangedListener(watcher);
    }


    private float parseFloat(String value) {
        try {
            return Float.parseFloat(value.replaceAll("[^\\d.]", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }


    private String parseNumericOnly(String value) {
        return value.replaceAll("[^\\d.]", "");
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

    private String getCurrentFinancialYear() {
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

    private TextView createText(String text, int sizeSp, int style) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(sizeSp);
        tv.setTypeface(null, style);
        tv.setTextColor(Color.parseColor("#333333"));
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        return tv;
    }
    private void redistributeToUneditedMonths(String justEditedMonth, int justEditedYear) {
        SharedPreferences prefs = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        float totalYearTarget = Turnover;

        List<String> orderedMonths = Arrays.asList(
                "Apr", "May", "Jun", "Jul", "Aug", "Sep",
                "Oct", "Nov", "Dec", "Jan", "Feb", "Mar"
        );

        float totalEdited = 0f;
        List<String> uneditedKeys = new ArrayList<>();

        for (String month : orderedMonths) {
            int year = (month.equals("Jan") || month.equals("Feb") || month.equals("Mar"))
                    ? Integer.parseInt(financialYear.split("_")[0]) + 1
                    : Integer.parseInt(financialYear.split("_")[0]);

            String fullKey = month + "_" + year;
            float currentValue = getSafeFloat(prefs, "expected_" + fullKey, Turnover / 12f);

            //float currentValue = prefs.getFloat("expected_" + fullKey, Turnover / 12f);
            boolean isEdited = prefs.getBoolean("edited_" + fullKey, false);

            if (isEdited) {
                totalEdited += currentValue;
            } else {
                uneditedKeys.add(fullKey);
            }
        }

        float remainingTarget = totalYearTarget - totalEdited;

        if (uneditedKeys.size() > 0) {
            float newPerMonth = remainingTarget / uneditedKeys.size();

            for (String key : uneditedKeys) {
                editor.putFloat("expected_" + key, newPerMonth);
                Log.d("REBALANCE", "Set " + key + " = " + newPerMonth);
            }

            editor.apply();
        }

        // Refresh UI
        monthTargetMap.clear();
        tableLayout.removeAllViews();
        addMonthRows();
    }


    private Map<String, Float> getAllMonthlyExpectedValues() {
        SharedPreferences prefs = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);
        Map<String, Float> monthData = new HashMap<>();

        String[] parts = financialYear.split("_");
        int fyStartYear = Integer.parseInt(parts[0]);

        for (String month : months) {
            String shortMonth = convertToShortMonth(month);
            int year = (shortMonth.equals("Jan") || shortMonth.equals("Feb") || shortMonth.equals("Mar")) ? fyStartYear + 1 : fyStartYear;
           // float expected = prefs.getFloat("expected_" + shortMonth + "_" + year, Turnover / 12.0f);
            float expected = getSafeFloat(prefs, "expected_" + shortMonth + "_" + year, Turnover / 12.0f);

            monthData.put(month + " " + year, expected);

            Log.d("MonthlyData", "Month data is : " + month + " " + year + " " + expected);
        }

        return monthData;

    }

    private float calculateLatestYearlyAchievedTotal() {
        SharedPreferences prefs = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);
        float total = 0f;

        String[] parts = financialYear.split("_");
        int fyStartYear = Integer.parseInt(parts[0]);

        for (String month : months) {
            String shortMonth = convertToShortMonth(month);
            int year = (shortMonth.equals("Jan") || shortMonth.equals("Feb") || shortMonth.equals("Mar"))
                    ? fyStartYear + 1 : fyStartYear;

            String key = "data_" + shortMonth + "_" + year + "_Achieved";
            total += prefs.getInt(key, 0);
        }

        return total;
    }


    private void generatePdfWithTable() {
        PdfDocument document = new PdfDocument();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();
        Paint linePaint = new Paint();
        Paint headerPaint = new Paint();
        Paint targetPaint = new Paint();

        int pageWidth = 595;
        int pageHeight = 842;
        int margin = 20;

        int tableStartX = margin;
        int tableStartY = 100;
        int rowHeight = 40;
        int tableWidth = pageWidth - 2 * margin;

        int[] columnWidths = {tableWidth / 4, tableWidth / 4, tableWidth / 4, tableWidth / 4};

        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));
        titlePaint.setTextSize(20f);
        titlePaint.setColor(Color.BLACK);

        paint.setColor(Color.BLACK);
        paint.setTextSize(12f);

        headerPaint.setColor(Color.LTGRAY);

        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(1);

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Draw title
        canvas.drawText("Monthly Report", margin, 60, titlePaint);


        targetPaint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));
        targetPaint.setColor(Color.BLACK);
        targetPaint.setTextSize(14f);
        canvas.drawText("Yearly Target : " + Turnover, margin + 180, 60, targetPaint);


        float latestAchieved = calculateLatestYearlyAchievedTotal();

        DecimalFormat df = new DecimalFormat("#,##0");
        String achievedText = "Monthly Achieved: ₹" + df.format(latestAchieved);
        canvas.drawText("Achieved Target : ₹" + df.format(latestAchieved), margin + 360, 60, targetPaint);





        // Draw Header Background
        canvas.drawRect(tableStartX, tableStartY, tableStartX + tableWidth, tableStartY + rowHeight, headerPaint);

        // Header titles
        String[] headers = {"Month", "Target (₹)", "Achieved (₹)", "Achieved %"};
        int x = tableStartX;
        for (int i = 0; i < headers.length; i++) {
            canvas.drawText(headers[i], x + 10, tableStartY + 25, paint);
            x += columnWidths[i];
        }

        // Draw horizontal line below header
        canvas.drawLine(tableStartX, tableStartY, tableStartX + tableWidth, tableStartY, linePaint);
        canvas.drawLine(tableStartX, tableStartY + rowHeight, tableStartX + tableWidth, tableStartY + rowHeight, linePaint);

        int currentY = tableStartY + rowHeight;

        SharedPreferences prefs = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);
        String[] parts = financialYear.split("_");
        int fyStartYear = Integer.parseInt(parts[0]);

        for (String month : months) {
            if (currentY + rowHeight > pageHeight - 50) {
                document.finishPage(page);
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                currentY = margin;
            }

            String shortMonth = convertToShortMonth(month);
            int year = (shortMonth.equals("Jan") || shortMonth.equals("Feb") || shortMonth.equals("Mar")) ? fyStartYear + 1 : fyStartYear;

            float expected = prefs.getFloat("expected_" + shortMonth + "_" + year, 0f);
            int achieved = prefs.getInt("data_" + shortMonth + "_" + year + "_Achieved", 0);
            float percent = expected != 0 ? (achieved * 100f / expected) : 0f;

            x = tableStartX;
            String[] rowData = {
                    month,
                    String.format(Locale.US, "%.0f", expected),
                    String.valueOf(achieved),
                    String.format(Locale.US, "%.2f%%", percent)
            };

            for (int i = 0; i < rowData.length; i++) {
                canvas.drawText(rowData[i], x + 10, currentY + 25, paint);
                x += columnWidths[i];
            }

            // Draw lines
            canvas.drawLine(tableStartX, currentY, tableStartX + tableWidth, currentY, linePaint);
            currentY += rowHeight;
            canvas.drawLine(tableStartX, currentY, tableStartX + tableWidth, currentY, linePaint);
        }

        // Draw vertical lines
        int verticalX = tableStartX;
        for (int w : columnWidths) {
            canvas.drawLine(verticalX, tableStartY, verticalX, currentY, linePaint);
            verticalX += w;
        }
        canvas.drawLine(tableStartX + tableWidth, tableStartY, tableStartX + tableWidth, currentY, linePaint);

        document.finishPage(page);

        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "Monthly_Report_" + System.currentTimeMillis() + ".pdf");

            FileOutputStream out = new FileOutputStream(file);
            document.writeTo(out);
            document.close();
            out.close();
            Uri pdfUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
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

            Toast.makeText(this, "PDF Saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showDistributeDialog(float loss, String month, int year, TextView tvLoss) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Distribute Loss");
        builder.setMessage("How many months you want to distribute loss");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Distribute", (dialog, which) -> {
            int monthsToDistribute;
            try {
                monthsToDistribute = Integer.parseInt(input.getText().toString().trim());
                if (monthsToDistribute <= 0) {
                    throw new NumberFormatException("Months must be positive");
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number of months", Toast.LENGTH_SHORT).show();
                return;
            }

            distributeLossAcrossMonths(loss, month, year, monthsToDistribute);
            tvLoss.setText("Loss: ₹0");
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    private void distributeLossAcrossMonths(float loss, String startMonth, int startYear, int months) {
        SharedPreferences prefs = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        List<String> orderedMonths = Arrays.asList(
                "Apr", "May", "Jun", "Jul", "Aug", "Sep",
                "Oct", "Nov", "Dec", "Jan", "Feb", "Mar"
        );

        int startIndex = orderedMonths.indexOf(startMonth);
        if (startIndex == -1) {
            Toast.makeText(this, "Invalid start month", Toast.LENGTH_SHORT).show();
            return;
        }

        float lossPerMonth = loss / months;

        // 1️⃣ Update current month (subtract total loss)
        String startKey = "expected_" + startMonth + "_" + startYear;
        float currentExpected = getSafeFloat(prefs, startKey, Turnover / 12.0f);
        float newExpected = Math.max(0, currentExpected - loss);
        editor.putFloat(startKey, newExpected);

        // 2️⃣ Distribute to future months (add lossPerMonth)
        for (int i = 1; i <= months; i++) {
            int nextIndex = (startIndex + i) % orderedMonths.size();
            String nextMonth = orderedMonths.get(nextIndex);
            int nextYear = (nextMonth.equals("Jan") || nextMonth.equals("Feb") || nextMonth.equals("Mar")) ? startYear + 1 : startYear;

            String nextKey = "expected_" + nextMonth + "_" + nextYear;
            float nextExpected = getSafeFloat(prefs, nextKey, Turnover / 12.0f);
            float updatedExpected = nextExpected + lossPerMonth;
            editor.putFloat(nextKey, updatedExpected);
        }

        // ✅ Apply all changes at once
        editor.apply();

        // 3️⃣ Update UI after all changes are applied
        updateMonthUI(startMonth, startYear, newExpected);

        for (int i = 1; i <= months; i++) {
            int nextIndex = (startIndex + i) % orderedMonths.size();
            String nextMonth = orderedMonths.get(nextIndex);
            int nextYear = (nextMonth.equals("Jan") || nextMonth.equals("Feb") || nextMonth.equals("Mar")) ? startYear + 1 : startYear;

            float updatedExpected = getSafeFloat(prefs, "expected_" + nextMonth + "_" + nextYear, Turnover / 12.0f);
            updateMonthUI(nextMonth, nextYear, updatedExpected);
        }

        Toast.makeText(this, "Loss distributed over " + months + " months", Toast.LENGTH_SHORT).show();

        // Refresh full table
        tableLayout.removeAllViews();
        addMonthRows();
    }


    private void updateMonthUI(String month, int year, float newExpected) {
        String targetTag = "target_" + month + "_" + year;

        for (int i = 0; i < tableLayout.getChildCount(); i++) {
            View card = tableLayout.getChildAt(i);
            if (card instanceof CardView) {
                CardView cardView = (CardView) card;

                // Recursive search inside CardView
                TextView tvTarget = findTextViewByTag(cardView, targetTag);
                if (tvTarget != null) {
                    tvTarget.setText("₹" + String.format("%.0f", newExpected));
                    Log.d("UpdateMonthUI", "Updated " + month + " " + year + " to ₹" + newExpected);
                    return;
                }
            }
        }
    }





    private void showCustomToast(String message) {
        // Create a TextView programmatically
        TextView toastText = new TextView(this);
        toastText.setText(message);
        toastText.setTextColor(Color.WHITE);
        toastText.setTextSize(16);
        toastText.setPadding(40, 24, 40, 24);
        toastText.setGravity(Gravity.CENTER);
        toastText.setBackgroundResource(R.drawable.bg_green_toast); // custom drawable

        Toast toast = new Toast(this);
        toast.setView(toastText);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 120); // show at top with margin
        toast.show();
    }
    private TextView findTextViewByTag(View root, String tag) {
        if (root instanceof TextView && tag.equals(root.getTag())) {
            return (TextView) root;
        } else if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) {
                TextView result = findTextViewByTag(group.getChildAt(i), tag);
                if (result != null) return result;
            }
        }
        return null;
    }


}
