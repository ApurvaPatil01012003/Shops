package com.exam.shops;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YOYActivity extends AppCompatActivity {

    TextView txtTurnOver;
    TableLayout tableLayout;
   // int Turnover;
    int Result;
    String financialYear;

    List<String> months = Arrays.asList(
            "April", "May", "June", "July", "August", "September",
            "October", "November", "December", "January", "February", "March"
    );
    Map<String, Float> monthTargetMap = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoyactivity);

        txtTurnOver = findViewById(R.id.txtTurnOver);
        tableLayout = findViewById(R.id.tableLayout);
        Result = getIntent().getIntExtra("ResultTurnYear", 0);
        txtTurnOver.setText("Yearly Target : " + String.valueOf(Result));

        // Turnover = getIntent().getIntExtra("TurnYear", 0);
        financialYear = getCurrentFinancialYear();

        // txtTurnOver.setText("Yearly Target : "+String.valueOf(Turnover));


        SharedPreferences sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        // sharedPref.edit().putString("TurnOver", String.valueOf(Turnover)).apply();
        sharedPref.edit().putString("ResultTurnOver", String.valueOf(Result)).apply();



        addMonthRows();

        int total = getTotalAchievedInYear();
        float percentOfYear = (Result != 0) ? (total * 100.0f / Result) : 0;

        Log.d("TotalAchieved", "Total Achieved is: " + total);
        Log.d("TotalPercent", "Total Achieved %: " + percentOfYear);


        SharedPreferences sharedPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        sharedPrefs.edit()
                .putInt("TotalAchievedValue", total)
                .putFloat("TotalAchievedPercentage", percentOfYear)
                .apply();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                int total = getTotalAchievedInYear();
                float percentOfYear = (Result != 0) ? (total * 100.0f / Result) : 0;

                Intent intent = new Intent(YOYActivity.this, GoToMAndD.class);
                intent.putExtra("TotalAchived", total);
                intent.putExtra("TotalAchPer", percentOfYear);
                startActivity(intent);
                finishAffinity();
            }
        });





    }



    private void addMonthRows() {

        SharedPreferences prefs = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);
        String[] parts = financialYear.split("_");
        int fyStartYear = Integer.parseInt(parts[0]);

        for (String month : months) {
            String shortMonth = convertToShortMonth(month);
            int dataYear = (shortMonth.equals("Jan") || shortMonth.equals("Feb") || shortMonth.equals("Mar")) ? fyStartYear + 1 : fyStartYear;

            float expected = prefs.getFloat("expected_" + shortMonth + "_" + dataYear, Result / 12.0f);
            int achieved = prefs.getInt("data_" + shortMonth + "_" + dataYear + "_Achieved", 0);
            float percent = (expected != 0) ? (achieved / expected) * 100 : 0;

            // CardView
            CardView cardView = new CardView(this);
            CardView.LayoutParams cardParams = new CardView.LayoutParams(
                    CardView.LayoutParams.MATCH_PARENT,
                    CardView.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 32);
            cardView.setLayoutParams(cardParams);
            cardView.setCardElevation(8f);
            cardView.setRadius(24f);
            cardView.setUseCompatPadding(true);
            cardView.setPreventCornerOverlap(true);
            cardView.setCardBackgroundColor(Color.WHITE);

            // Inner layout
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(32, 32, 32, 32);

            // Month
            TextView tvMonth = createText("Month: " + month, 16, Typeface.BOLD);
            layout.addView(tvMonth);

            // Target row
            LinearLayout targetRow = createLabeledRow("Target: ₹" + String.format("%.0f", expected));
            TextView tvTarget = (TextView) targetRow.getChildAt(0);
            ImageView ivEditTarget = (ImageView) targetRow.getChildAt(1);
            ivEditTarget.setOnClickListener(v -> showEditDialog(tvTarget, shortMonth, true));
            layout.addView(targetRow);

            // Achieved row
            LinearLayout achievedRow = createLabeledRow("Achieved: ₹" + achieved);
            TextView tvAchieved = (TextView) achievedRow.getChildAt(0);
            ImageView ivEditAchieved = (ImageView) achievedRow.getChildAt(1);
            ivEditAchieved.setOnClickListener(v -> showEditDialog(tvAchieved, shortMonth, false));
            layout.addView(achievedRow);

            // Percentage
            TextView tvPercentage = createText("Achieved Percentage: " + String.format("%.0f", percent) + "%", 14, Typeface.NORMAL);
            layout.addView(tvPercentage);

            updatePercentage(tvTarget, tvAchieved, tvPercentage, shortMonth, prefs);


            cardView.addView(layout);
            tableLayout.addView(cardView);

            ivEditTarget.setOnClickListener(v -> {
                showSingleEditDialog(tvTarget, tvAchieved, tvPercentage, shortMonth, true);
            });

            ivEditAchieved.setOnClickListener(v -> {
                showSingleEditDialog(tvTarget, tvAchieved, tvPercentage, shortMonth, false);
            });

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
            totalAchieved += prefs.getInt(key, 0); // default to 0 if not found
        }
        Log.d("TotalAchieved","Total Achived is :"+totalAchieved);
        return totalAchieved;


    }



    private void showEditDialog(TextView targetView, String shortMonth, boolean isExpected) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isExpected ? "Edit Expected Turnover" : "Edit Achieved Value");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(targetView.getText().toString());
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newValue = input.getText().toString().trim();
            targetView.setText(newValue);

            TableRow row = (TableRow) targetView.getParent();
            TextView tvExpected = (TextView) row.getChildAt(1);
            TextView tvAchieved = (TextView) row.getChildAt(2);
            TextView tvPercentage = (TextView) row.getChildAt(3);

            updatePercentage(tvExpected, tvAchieved, tvPercentage, shortMonth,
                    getSharedPreferences("YOY_PREFS", MODE_PRIVATE));


            SharedPreferences prefs = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            // Get financial year start from the `financialYear` string
            String[] parts = financialYear.split("_");
            int fyStartYear = Integer.parseInt(parts[0]);
            int dataYear = (shortMonth.equals("Jan") || shortMonth.equals("Feb") || shortMonth.equals("Mar"))
                    ? fyStartYear + 1 : fyStartYear;

            if (isExpected) {
                editor.putFloat("expected_" + shortMonth + "_" + dataYear, Float.parseFloat(newValue));
            } else {
                editor.putInt("data_" + shortMonth + "_" + dataYear + "_Achieved", Integer.parseInt(newValue));
            }

            editor.apply();

        });


        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }


    private void showSingleEditDialog(TextView tvTarget, TextView tvAchieved, TextView tvPercentage,
                                      String shortMonth, boolean isTargetEdit) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isTargetEdit ? "Edit Target" : "Edit Achieved");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        String current = isTargetEdit ? tvTarget.getText().toString() : tvAchieved.getText().toString();
        input.setText(parseNumericOnly(current));

        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newValue = input.getText().toString().trim();
            float newValueFloat = parseFloat(newValue);

            SharedPreferences prefs = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            String[] parts = financialYear.split("_");
            int fyStartYear = Integer.parseInt(parts[0]);
            int dataYear = (shortMonth.equals("Jan") || shortMonth.equals("Feb") || shortMonth.equals("Mar"))
                    ? fyStartYear + 1 : fyStartYear;

            if (isTargetEdit) {
                float oldValue = parseFloat(tvTarget.getText().toString());
                float diff = newValueFloat - oldValue;

                Log.d("TARGET_CHANGE", "Old: " + oldValue + " New: " + newValueFloat + " Diff: " + diff);


                editor.putFloat("expected_" + shortMonth + "_" + dataYear, newValueFloat);
                editor.apply();

                String monthKey = shortMonth + "_" + dataYear;
                monthTargetMap.put(monthKey, newValueFloat);


                if (diff != 0) {
                    redistributeDifference(shortMonth, dataYear, diff);
                }

                // Refresh whole UI after all updates (including edited month)
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    monthTargetMap.clear();
                    tableLayout.removeAllViews();
                    addMonthRows();
                }, 100); // small delay ensures sharedPrefs saved
            }
            else {
                tvAchieved.setText("Achieved: ₹" + newValue);
                editor.putInt("data_" + shortMonth + "_" + dataYear + "_Achieved", (int) newValueFloat);
            }

           // editor.apply();

            editor.putFloat("expected_" + shortMonth + "_" + dataYear, newValueFloat);
            editor.apply();

            updatePercentage(tvTarget, tvAchieved, tvPercentage, shortMonth, prefs);
            addTextWatcher(tvTarget, tvAchieved, tvPercentage, shortMonth);
        });


        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }


    private void updatePercentage(TextView expectedView, TextView achievedView, TextView percentView,
                                  String shortMonth, SharedPreferences prefs) {
        new Handler(Looper.getMainLooper()).post(() -> {
            float expected = parseFloat(expectedView.getText().toString());
            float achieved = parseFloat(achievedView.getText().toString());
            String percentText = "--";
            int colorResId = R.color.Black;

            if (expected != 0) {
                float percent = (achieved / expected) * 100;
                percentText = String.format("%.2f%%", percent);

                if (percent>=0 && percent < 70) {
                    colorResId = R.color.Green;
                } else if (percent >=70 && percent < 90) {
                    colorResId = R.color.Black;
                } else {
                    colorResId = R.color.Red;
                }
            }

            percentView.setText(percentText);
            percentView.setTextColor(ContextCompat.getColor(this, colorResId));

            prefs.edit()
                    .putString("data_" + shortMonth + "_" + financialYear, expectedView.getText().toString())
                    .putString("data_" + shortMonth + "_" + financialYear + "_Achieved", achievedView.getText().toString())
                    .putString("data_" + shortMonth + "_" + financialYear + "_AchievedPecentage", percentText)
                    .apply();
        });
    }

    private void addTextWatcher(TextView expectedView, TextView achievedView, TextView percentView, String shortMonth) {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                float expected = parseFloat(expectedView.getText().toString());
                float achieved = parseFloat(achievedView.getText().toString());

                String percentText = "--";
                int colorResId = R.color.Black;

                if (expected != 0) {
                    float percent = (achieved / expected) * 100;
                    percentText = String.format("%.2f%%", percent);
                    if (percent>=0 && percent < 70) {
                        colorResId = R.color.Green;
                    } else if (percent >=70 && percent < 90) {
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

    private LinearLayout createLabeledRow(String text) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        row.setPadding(0, 16, 0, 0);

        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(14);
        tv.setTextColor(Color.parseColor("#333333"));
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        ImageView iv = new ImageView(this);
        iv.setImageResource(R.drawable.ic_edit);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(60, 60);
        iv.setLayoutParams(iconParams);
        iv.setPadding(8, 8, 8, 8);

        row.addView(tv);
        row.addView(iv);

        return row;
    }

    private void redistributeDifference(String editedMonth, int editedYear, float diff) {
        List<String> remainingMonths = getRemainingMonths(editedMonth);

        if (remainingMonths.isEmpty()) return;

        float splitAmount = diff / remainingMonths.size();

        SharedPreferences.Editor editor = getSharedPreferences("YOY_PREFS", MODE_PRIVATE).edit();

        for (String month : remainingMonths) {

            int targetYear = (month.equals("Jan") || month.equals("Feb") || month.equals("Mar"))
                    ? Integer.parseInt(financialYear.split("_")[0]) + 1
                    : Integer.parseInt(financialYear.split("_")[0]);


            String key = "expected_" + month + "_" + targetYear;
            String mapKey = month + "_" + targetYear;

            float oldValue = monthTargetMap.getOrDefault(mapKey, Result / 12f);
            float newValue = oldValue - splitAmount;
            if (newValue < 0) newValue = 0;

            editor.putFloat(key, newValue);
            monthTargetMap.put(mapKey, newValue);

            Log.d("REDIST_TARGET", key + " updated to " + newValue);
        }

        editor.apply();

        // Refresh UI
        monthTargetMap.clear();
        tableLayout.removeAllViews();
        addMonthRows();
    }

    private List<String> getRemainingMonths(String currentShortMonth) {
        List<String> orderedMonths = Arrays.asList(
                "Apr", "May", "Jun", "Jul", "Aug", "Sep",
                "Oct", "Nov", "Dec", "Jan", "Feb", "Mar"
        );

        List<String> result = new ArrayList<>();
        boolean startCollecting = false;

        for (String month : orderedMonths) {
            if (startCollecting) result.add(month);
            if (month.equals(currentShortMonth)) startCollecting = true;
        }

        return result;
    }


}
