package com.exam.shops;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class YOYActivity extends AppCompatActivity {

    TextView txtTurnOver;
    TableLayout tableLayout;
    int Turnover;
    String financialYear;

    List<String> months = Arrays.asList(
            "April", "May", "June", "July", "August", "September",
            "October", "November", "December", "January", "February", "March"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yoyactivity);

        txtTurnOver = findViewById(R.id.txtTurnOver);
        tableLayout = findViewById(R.id.tableLayout);

        Turnover = getIntent().getIntExtra("TurnYear", 0);
        financialYear = getCurrentFinancialYear();

        txtTurnOver.setText(String.valueOf(Turnover));

        SharedPreferences sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        sharedPref.edit().putString("TurnOver", String.valueOf(Turnover)).apply();

        addHeaderRow();
        addMonthRows();
    }

    private void addHeaderRow() {
        TableRow header = new TableRow(this);
        header.setBackgroundColor(Color.parseColor("#196C92"));

        String[] headers = {"Month", financialYear + " (Exp)", financialYear + " (Ach)", "Achieved %"};
        for (String title : headers) {
            TextView tv = new TextView(this);
            tv.setText(title);
            tv.setPadding(20, 20, 20, 20);
            tv.setTextSize(16);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setTextColor(Color.BLACK);
            tv.setGravity(Gravity.CENTER);
            header.addView(tv);
        }
        tableLayout.addView(header);
    }


    private void addMonthRows() {
        SharedPreferences prefs = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);
        boolean alternate = false;

        String[] parts = financialYear.split("_");
        int fyStartYear = Integer.parseInt(parts[0]);

        for (String month : months) {
            TableRow row = new TableRow(this);
            row.setBackgroundColor(alternate ? Color.parseColor("#F7F7F7") : Color.WHITE);
            alternate = !alternate;

            TextView tvMonth = createTextView(month);
            row.addView(tvMonth);

            String shortMonth = convertToShortMonth(month);
            float monthlyExpected = Turnover / 12.0f;

            TextView tvExpected = createTextView(String.format("%.2f", monthlyExpected));
            tvExpected.setOnTouchListener(getDoubleTapListener(tvExpected, shortMonth, true));
            row.addView(tvExpected);

            int dataYear = fyStartYear;
            if (shortMonth.equals("Jan") || shortMonth.equals("Feb") || shortMonth.equals("Mar")) {
                dataYear += 1;
            }

            int achievedInt = prefs.getInt("data_" + shortMonth + "_" + dataYear + "_Achieved", 0);
            TextView tvAchieved = createTextView(String.valueOf(achievedInt));
            tvAchieved.setOnTouchListener(getDoubleTapListener(tvAchieved, shortMonth, false));
            row.addView(tvAchieved);

            TextView tvPercentage = createTextView("--");
            row.addView(tvPercentage);
            tableLayout.addView(row);
            updatePercentage(tvExpected, tvAchieved, tvPercentage, shortMonth, prefs);
            addTextWatcher(tvExpected, tvAchieved, tvPercentage, shortMonth);
        }
    }

    private TextView createTextView(String value) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setPadding(16, 16, 16, 16);
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(Color.BLACK);
        tv.setClickable(true);
        tv.setFocusable(true);
        tv.setFocusableInTouchMode(true);
        return tv;
    }

    private View.OnTouchListener getDoubleTapListener(TextView view, String shortMonth, boolean isExpected) {
        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                showEditDialog(view, shortMonth, isExpected);
                return true;
            }
        });
        return (v, event) -> gestureDetector.onTouchEvent(event);
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
            return Float.parseFloat(value.trim());
        } catch (Exception e) {
            return 0;
        }
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

}
