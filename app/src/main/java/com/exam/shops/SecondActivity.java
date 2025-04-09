package com.exam.shops;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
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

import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.ArrayList;

public class SecondActivity extends AppCompatActivity {

    EditText FirstTurnOver, SecondTurnOver, edtHighPerDays,edtGrowth ;
    Spinner  spinnerShopHoli;
    TextView ShopName,txtGrowth;
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
        edtGrowth=findViewById(R.id.edtGrowth);
        txtGrowth = findViewById(R.id.txtGrowth);
        spinnerShopHoli = findViewById(R.id.spinnerShopHoli);
        btnSave = findViewById(R.id.btnSave);


        String shopName = getIntent().getStringExtra("shop_name");
        ShopName.setText(shopName);

        ArrayAdapter<String> shopholiday = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Select Day", "Sunday", "Monday", "Tuesday", "Wensday", "Thursday", "Friday", "Saturday"}
        );
        shopholiday.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerShopHoli.setAdapter(shopholiday);


        edtHighPerDays = findViewById(R.id.edtHighPerDays);

        String[] daysArray = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        boolean[] checkedDays = new boolean[daysArray.length];
        ArrayList<String> selectedDays = new ArrayList<>();

        edtHighPerDays.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
            builder.setTitle("Select Days");

            builder.setMultiChoiceItems(daysArray, checkedDays, (dialog, index, isChecked) -> {
                if (isChecked) {
                    if (!selectedDays.contains(daysArray[index])) {
                        selectedDays.add(daysArray[index]);
                    }
                } else {
                    selectedDays.remove(daysArray[index]);
                }
            });

            builder.setPositiveButton("OK", (dialog, which) -> {
                edtHighPerDays.setText(TextUtils.join(", ", selectedDays));
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();

        });



        btnSave.setOnClickListener(v -> {
            String name = ShopName.getText().toString().trim();
            String fturnover = FirstTurnOver.getText().toString().trim();
            String sturnover = SecondTurnOver.getText().toString().trim();
            String EditGrowth= edtGrowth.getText().toString().trim();

            if (fturnover.isEmpty() || sturnover.isEmpty()) {
                Toast.makeText(this, "Enter both turnover values", Toast.LENGTH_SHORT).show();
                return;
            }

            int firstTurnOver = Integer.parseInt(fturnover);
            int secondTurnOver = Integer.parseInt(sturnover);
            int EDTGrowth =Integer.parseInt(EditGrowth);

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

            String shopHolidays = spinnerShopHoli.getSelectedItem().toString();
            String HighPerformDays = edtHighPerDays.getText().toString().trim();

            if (!name.isEmpty() && !growth_per.isEmpty()) {
                saveDataToSharedPref(name, firstTurnOver, secondTurnOver, growths,EDTGrowth, shopHolidays, HighPerformDays);
                Toast.makeText(this, "Data Saved!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SecondActivity.this,GoToMAndD.class);
                intent.putExtra("TurnOver",secondTurnOver);
                intent.putExtra("shopsHoliday",shopHolidays);
                startActivity(intent);
                finish();
                Log.d("SecondTurnOver","TurnOver is : "+secondTurnOver);
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
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


    }
        public abstract class SimpleTextWatcher implements TextWatcher {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        }



    private void saveDataToSharedPref(String name, int fturnover, int sturnover,int growth_per,int EditGrowth ,String shopHolidays,String HighPerformDays) {
        SharedPreferences sharedPref = getSharedPreferences("ShopData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("shop_name", name);
        editor.putInt("Fturnover", fturnover);
        editor.putInt("Sturnover", sturnover);
        editor.putInt("Growth_Per", growth_per);
        editor.putInt("editGrowth",EditGrowth);
        editor.putString("Shop_Holiday",shopHolidays);
        editor.putString("selected_days", HighPerformDays);
        editor.apply();



        Log.d("selected_days","selected_days is : "+name);

        Log.d("selected_days","selected_days is : "+fturnover);

        Log.d("selected_days","selected_days is : "+sturnover);

        Log.d("selected_days","selected_days is : "+growth_per);
        Log.d("selected_days","selected_days is : "+EditGrowth);
        Log.d("selected_days","selected_days is : "+shopHolidays);
        Log.d("selected_days","selected_days is : "+HighPerformDays);


    }

    private void calculateGrowth() {
        String first = FirstTurnOver.getText().toString().trim();
        String second = SecondTurnOver.getText().toString().trim();

        if (!first.isEmpty() && !second.isEmpty()) {
            try {
                int firstValue = Integer.parseInt(first);
                int secondValue = Integer.parseInt(second);
                int growth = secondValue - firstValue;

                Log.d("Growth", "Growth is : " + growth);

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




}