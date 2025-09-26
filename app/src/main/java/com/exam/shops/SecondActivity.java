package com.exam.shops;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import java.util.Calendar;

public class SecondActivity extends AppCompatActivity {

    EditText FirstTurnOver, SecondTurnOver, edtGrowth;

    TextView ShopName, txtGrowth, txtFirstTurnOver, txtSecondTurnOver, txtGrowthShow,EGFY,txtexGrow;
    MaterialButton btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_second);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ShopName = findViewById(R.id.ShopName);
        FirstTurnOver = findViewById(R.id.FirstTurnOver);
        SecondTurnOver = findViewById(R.id.SecondTurnOver);
        edtGrowth = findViewById(R.id.edtGrowth);
        txtGrowth = findViewById(R.id.txtGrowth);
        btnSave = findViewById(R.id.btnSave);
        txtFirstTurnOver = findViewById(R.id.txtFirstTurnOver);
        txtFirstTurnOver.setText(getSecondLastFinancialYear() + " year sales");
        txtSecondTurnOver = findViewById(R.id.txtSecondTurnOver);
        txtSecondTurnOver.setText(getLastFinancialYear() + " year's target");
        EGFY = findViewById(R.id.EGFY);
        EGFY.setText(getCurrentFinancialYear() + " year's expected");
        txtGrowthShow = findViewById(R.id.txtGrowthShow);

        txtexGrow= findViewById(R.id.txtexGrow);

        Intent receivedIntent = getIntent();
        String shopName = receivedIntent.getStringExtra("shop_name");
        String MobileNumber = receivedIntent.getStringExtra("Mobile_no");
        ShopName.setText(shopName);
        String holiday = getIntent().getStringExtra("shopsHoliday");
        String highPerformDay = getIntent().getStringExtra("HighPerformace");

        Log.d("INTENT_DEBUG", "mobile " + MobileNumber);
        Log.d("INTENT_DEBUG", "Sending holiday: " + holiday);
        Log.d("INTENT_DEBUG", "Sending highPerform: " + highPerformDay);



        btnSave.setOnClickListener(v -> {
            String name = ShopName.getText().toString().trim();
            String fturnoverStr = FirstTurnOver.getText().toString().trim();
            String sturnoverStr = SecondTurnOver.getText().toString().trim();
            String editGrowthStr = edtGrowth.getText().toString().trim();
            String growthShowStr = txtGrowthShow.getText().toString().replace("%", "").trim();
            String growthPercentStr = txtGrowth.getText().toString().replace("%", "").trim();

            if (TextUtils.isEmpty(fturnoverStr) || TextUtils.isEmpty(sturnoverStr) ||
                    TextUtils.isEmpty(editGrowthStr) || TextUtils.isEmpty(growthShowStr)) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int firstTurnover = Integer.parseInt(fturnoverStr);
                int secondTurnover = Integer.parseInt(sturnoverStr);
                int edtGrowth = Integer.parseInt(editGrowthStr);
                int result = Math.round(Float.parseFloat(growthShowStr));  // Final "Result" value
                int growthPercent = Math.round(Float.parseFloat(growthPercentStr)); // txtGrowth cleaned

                saveDataToSharedPref(name,MobileNumber, holiday, highPerformDay, firstTurnover, secondTurnover, growthPercent, edtGrowth, result);

                Intent intent = new Intent(SecondActivity.this, YOYActivity.class);
                intent.putExtra("ResultTurnover", result);
                intent.putExtra("TurnOver", secondTurnover);
                intent.putExtra("ShopName", name);
                intent.putExtra("Mobile_no",MobileNumber);
                intent.putExtra("shopsHoliday", holiday);
                intent.putExtra("HighPerformace", highPerformDay);
                intent.putExtra("EdtGrowth", edtGrowth);
                startActivity(intent);
                finish();

                Log.d("DEBUG_LOG", "Saved and passed Result: " + result);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                Toast.makeText(this, "Invalid number format in one or more fields", Toast.LENGTH_SHORT).show();
            }
        });



        FirstTurnOver.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateGrowth();
                checkEnableButton();
            }
        });

        SecondTurnOver.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateGrowth();
                checkEnableButton();

            }
        });



        SecondTurnOver.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                calculateGrowthResult();
            }
        });
        edtGrowth.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                calculateGrowthResult();
            }
        });


    }

    public abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    private void saveDataToSharedPref(String name,String MobileNumber, String holiday, String highPerformDay, int fturnover, int sturnover, int growth_per, int EditGrowth, int Result) {
        SharedPreferences sharedPref = getSharedPreferences("shop_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("shop_name", name);
        editor.putString("Mobile_no",MobileNumber);
        editor.putString("Shop_Holiday", holiday);
        editor.putString("selected_days", highPerformDay);
        editor.putInt("Fturnover", fturnover);
        editor.putInt("Sturnover", sturnover);
        editor.putInt("Growth_Per", growth_per);
        editor.putInt("editGrowth", EditGrowth);
        editor.putInt("result", Result);
        editor.apply();


    }

    private void calculateGrowth() {
        String first = FirstTurnOver.getText().toString().trim();
        String second = SecondTurnOver.getText().toString().trim();

        if (!first.isEmpty() && !second.isEmpty()) {
            try {
                int firstValue = Integer.parseInt(first);
                int secondValue = Integer.parseInt(second);
                int growth = secondValue - firstValue;

                //  Log.d("Growth", "Growth is : " + growth);

                if (firstValue != 0) {
                    float percentage = ((float) growth / firstValue) * 100;
                    String formatted = String.format("%.2f", percentage);
                    txtGrowth.setText(formatted + " %");

                    if (percentage >= 0) {
                        txtGrowth.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    } else {
                        txtGrowth.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    }
                } else {
                    txtGrowth.setText("∞");
                    txtGrowth.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                }
            } catch (NumberFormatException e) {
                txtGrowth.setText("0 %");
                txtGrowth.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }
        } else {
            txtGrowth.setText("0 %");
            txtGrowth.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }


    private String getSecondLastFinancialYear() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        // Log.d("Year","Current6 year" +year + ""+month);

        year = year - 2;
        int startYear, endYear;
        if (month >= Calendar.APRIL) {
            startYear = year;
            endYear = year + 1;
            // Log.d("startyear","start year is in if : "+startYear+" "+endYear);
        } else {
            startYear = year - 1;
            endYear = year;
            // Log.d("startyear","start year is in else : "+startYear+" "+endYear);

        }
        return startYear + "_" + String.valueOf(endYear).substring(2);


    }


    private String getLastFinancialYear() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        // Log.d("Year","Current6 year" +year + ""+month);

        year = year - 1;
        int startYear, endYear;
        if (month >= Calendar.APRIL) {
            startYear = year;
            endYear = year + 1;
            //  Log.d("startyear","start year is in if : "+startYear+" "+endYear);
        } else {
            startYear = year - 1;
            endYear = year;
            // Log.d("startyear","start year is in else : "+startYear+" "+endYear);

        }
        return startYear + "_" + String.valueOf(endYear).substring(2);


    }

    private String getCurrentFinancialYear() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        // Log.d("Year","Current6 year" +year + ""+month);

       // year = year - 0;
        int startYear, endYear;
        if (month >= Calendar.APRIL) {
            startYear = year;
            endYear = year + 1;
            //  Log.d("startyear","start year is in if : "+startYear+" "+endYear);
        } else {
            startYear = year - 1;
            endYear = year;
            // Log.d("startyear","start year is in else : "+startYear+" "+endYear);

        }
        return startYear + "_" + String.valueOf(endYear).substring(2);


    }


    public void calculateGrowthResult() {


        String second = SecondTurnOver.getText().toString().trim();
        String  Ex_Growth_FY = edtGrowth.getText().toString().trim();

        if (!Ex_Growth_FY.isEmpty() && !Ex_Growth_FY.isEmpty()) {
            try {
                int firstValue = Integer.parseInt(second);
                int secondValue = Integer.parseInt(Ex_Growth_FY);
                int growth = secondValue - firstValue;

                txtGrowthShow.setVisibility(TextView.VISIBLE);
                txtexGrow.setVisibility(TextView.VISIBLE);
                if (firstValue != 0) {
                    float percentage = ((float) growth / firstValue) * 100;
                    String formatted = String.format("%.2f", percentage);
                    txtGrowthShow.setText(formatted + " %");

                    if (percentage >= 0) {
                        txtGrowthShow.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    } else {
                        txtGrowthShow.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    }
                } else {
                    txtGrowthShow.setText("∞");
                    txtGrowthShow.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                }
            } catch (NumberFormatException e) {
                txtGrowthShow.setVisibility(TextView.GONE);
                txtexGrow.setVisibility(TextView.GONE);
                txtGrowthShow.setText("0 %");
                txtGrowthShow.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }
        } else {
            txtGrowthShow.setVisibility(TextView.GONE);
            txtexGrow.setVisibility(TextView.GONE);
            txtGrowthShow.setText("0 %");
            txtGrowthShow.setTextColor(getResources().getColor(android.R.color.darker_gray));

        }
        checkEnableButton();

    }

    private void checkEnableButton() {
        String first = FirstTurnOver.getText().toString().trim();
        String second = SecondTurnOver.getText().toString().trim();
        String growth = edtGrowth.getText().toString().trim();
        String growthShow = txtGrowthShow.getText().toString().trim();

        boolean isValid = !first.isEmpty()
                && !second.isEmpty()
                && !growth.isEmpty()
                && !growthShow.isEmpty()
                && !growthShow.equals("0 %"); // avoid enabling for zero growth

        btnSave.setEnabled(isValid);
    }

}