package com.exam.shops;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Calendar;

public class SecondActivity extends AppCompatActivity {

    EditText FirstTurnOver, SecondTurnOver, edtGrowth;

    TextView ShopName, txtGrowth, txtFirstTurnOver, txtSecondTurnOver, txtGrowthShow;
    Button btnSave;

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
        txtFirstTurnOver.setText(getCurrentFinancialYear() + " TurnOver");
        txtSecondTurnOver = findViewById(R.id.txtSecondTurnOver);
        txtSecondTurnOver.setText(getCurrentFinancialYears() + " TurnOver");
        txtGrowthShow = findViewById(R.id.txtGrowthShow);

        Intent receivedIntent = getIntent();
        String shopName = receivedIntent.getStringExtra("shop_name");
        ShopName.setText(shopName);
        String holiday = getIntent().getStringExtra("shopsHoliday");
        String highPerformDay = getIntent().getStringExtra("HighPerformace");


        Log.d("INTENT_DEBUG", "Sending holiday: " + holiday);
        Log.d("INTENT_DEBUG", "Sending highPerform: " + highPerformDay);

        btnSave.setOnClickListener(v -> {
            String name = ShopName.getText().toString().trim();

            String fturnover = FirstTurnOver.getText().toString().trim();
            String sturnover = SecondTurnOver.getText().toString().trim();
            String EditGrowth = edtGrowth.getText().toString().trim();
            String GrowthShow = txtGrowthShow.getText().toString().trim();

            if (fturnover.isEmpty() || sturnover.isEmpty()) {
                Toast.makeText(this, "Enter both turnover values", Toast.LENGTH_SHORT).show();
                return;
            }

            int firstTurnOver = Integer.parseInt(fturnover);
            int secondTurnOver = Integer.parseInt(sturnover);
            int EDTGrowth = Integer.parseInt(EditGrowth);
            int Result = Integer.parseInt(GrowthShow);


            String growth_per = txtGrowth.getText().toString().trim();
            int growths;
            try {
                String cleanGrowth = growth_per.replace("%", "").trim();
                float growthFloat = Float.parseFloat(cleanGrowth);
                growths = Math.round(growthFloat);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                Toast.makeText(this, "Invalid growth percentage", Toast.LENGTH_SHORT).show();
                return;
            }


            if (!name.isEmpty() && !growth_per.isEmpty()) {
                saveDataToSharedPref(name, holiday, highPerformDay, firstTurnOver, secondTurnOver, growths, EDTGrowth, Result);
                Toast.makeText(this, "Data Saved!", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(SecondActivity.this,Holi_High_Day.class);
                Intent intent = new Intent(SecondActivity.this, GoToMAndD.class);
                intent.putExtra("ResultTurnover", Result);
                intent.putExtra("TurnOver", secondTurnOver);
                intent.putExtra("ShopName", name);
                intent.putExtra("shopsHoliday", holiday);
                intent.putExtra("HighPerformace", highPerformDay);
                intent.putExtra("EdtGrowth", EDTGrowth);
                startActivity(intent);
                finish();

                Log.d("HighPerformace", "HighPerformDays is : " + EDTGrowth);
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
            getCurrentFinancialYear();

            String editGrowthStr = edtGrowth.getText().toString().trim();
            String growthShowStr = txtGrowthShow.getText().toString().trim();

            if (TextUtils.isEmpty(editGrowthStr) || TextUtils.isEmpty(growthShowStr)) {
                Toast.makeText(this, "Please enter growth values", Toast.LENGTH_SHORT).show();
                return;
            }

            EDTGrowth = Integer.parseInt(editGrowthStr);
            Result = Integer.parseInt(growthShowStr);

            Log.d("Shows", "Shows growth s : " + growthShowStr);


        });


        FirstTurnOver.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateGrowth();
            }
        });

        SecondTurnOver.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateGrowth();

            }
        });


        edtGrowth.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                calculateGrowthResult();
            }
        });

    }

    public abstract class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    private void saveDataToSharedPref(String name, String holiday, String highPerformDay, int fturnover, int sturnover, int growth_per, int EditGrowth, int Result) {
        SharedPreferences sharedPref = getSharedPreferences("shop_data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("shop_name", name);
        editor.putString("Shop_Holiday", holiday);
        editor.putString("selected_days", highPerformDay);
        editor.putInt("Fturnover", fturnover);
        editor.putInt("Sturnover", sturnover);
        editor.putInt("Growth_Per", growth_per);
        editor.putInt("editGrowth", EditGrowth);
        editor.putInt("result", Result);
        editor.apply();


//        Log.d("selected_days","selected_days is : "+name);
//
//        Log.d("selected_days","selected_days is : "+fturnover);
//
//        Log.d("selected_days","selected_days is : "+sturnover);
//
//        Log.d("selected_days","selected_days is : "+growth_per);
//        Log.d("selected_days","selected_days is : "+EditGrowth);
//        Log.d("selected_days","selected_days is : "+shopHolidays);
//        Log.d("selected_days","selected_days is : "+HighPerformDays);


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


    private String getCurrentFinancialYear() {
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


    private String getCurrentFinancialYears() {
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


    public void calculateGrowthResult() {
        String growthText = edtGrowth.getText().toString().trim();
        String secondTurnoverText = SecondTurnOver.getText().toString().trim();

        if (!growthText.isEmpty() && !secondTurnoverText.isEmpty()) {
            try {
                float growthPercent = Float.parseFloat(growthText);
                int secondTurnover = Integer.parseInt(secondTurnoverText);

                Float result = ((growthPercent / 100f) * secondTurnover) + secondTurnover;

                txtGrowthShow.setText(String.valueOf(Math.round(result)));
            } catch (NumberFormatException e) {
                txtGrowthShow.setText("0");
            }
        } else {
            txtGrowthShow.setText("0");
        }
    }


}