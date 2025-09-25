package com.exam.shops;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class Holi_High_Day extends AppCompatActivity {
    EditText edtHighPerDays;
    Spinner spinnerShopHoli;
  MaterialButton btnNextSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_holi_high_day);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spinnerShopHoli = findViewById(R.id.spinnerShopHoli);
        edtHighPerDays = findViewById(R.id.edtHighPerDays);
        btnNextSchedule = findViewById(R.id.btnNextSchedule);


        Intent receivedIntent = getIntent();
        String Shopname = receivedIntent.getStringExtra("shop_name");
        String MobileNumber = receivedIntent.getStringExtra("Mobile_no");
//        int Result = receivedIntent.getIntExtra("ResultTurnover", -1);
//        int SecondTurnOverValue = receivedIntent.getIntExtra("TurnOver", -1);
//        int growth = receivedIntent.getIntExtra("EdtGrowth", -1);

Log.d("Holi_High_Day","Mobile :"+MobileNumber);
        ArrayAdapter<String> shopholiday = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Select Day", "Sunday", "Monday", "Tuesday", "wednesday", "Thursday", "Friday", "Saturday"}
        );
        shopholiday.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerShopHoli.setAdapter(shopholiday);

        String[] daysArray = {"Monday", "Tuesday", "wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        boolean[] checkedDays = new boolean[daysArray.length];
        ArrayList<String> selectedDays = new ArrayList<>();

        edtHighPerDays.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(Holi_High_Day.this);
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

        // Listen for changes in EditText (after selection)
        edtHighPerDays.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { checkInputs(); }
            @Override
            public void afterTextChanged(Editable s) {}
        });

// Listen for Spinner selection changes
        spinnerShopHoli.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                checkInputs();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                checkInputs();
            }
        });



        int Result = getIntent().getIntExtra("ResultTurnover", -1);
        int SecondTurnOverValue = getIntent().getIntExtra("TurnOver", -1);
        int growth = getIntent().getIntExtra("EdtGrowth", -1);

        btnNextSchedule.setEnabled(false);
        btnNextSchedule.setAlpha(0.5f); // visually dimmed

        btnNextSchedule.setOnClickListener(v -> {
            String shopHolidays = spinnerShopHoli.getSelectedItem().toString();
            String HighPerformDays = edtHighPerDays.getText().toString().trim();


            Intent intent = new Intent(Holi_High_Day.this, SecondActivity.class);

            intent.putExtra("shop_name", Shopname);
            intent.putExtra("Mobile_no",MobileNumber);
            intent.putExtra("TurnOver", SecondTurnOverValue);
            intent.putExtra("shopsHoliday", shopHolidays);
            intent.putExtra("HighPerformace", HighPerformDays);
            intent.putExtra("EdtGrowth", growth);
            intent.putExtra("ResultTurnover", Result);
            startActivity(intent);

            SharedPreferences sharedPreferences = getSharedPreferences("shop_data", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("TURNOVER", getIntent().getIntExtra("TurnOver", -1));
            editor.putInt("Growth", getIntent().getIntExtra("EdtGrowth", -1));
            editor.putString("Shop_Holiday", shopHolidays);
            editor.putString("selected_days", HighPerformDays);
            editor.putString("ShopName", Shopname);
            editor.putInt("ResultTurnover", Result);
            editor.apply();

            Log.d("ShopName", "shop name issss : " + Shopname);
            Log.d("Turnover", "Turnover is: " + getIntent().getIntExtra("TurnOver", -1));
            Log.d("Turnover", "Growth is: " + getIntent().getIntExtra("EdtGrowth", -1));
            Log.d("Turnover", "Holiday is: " + shopHolidays);
            Log.d("Turnover", "High per day is: " + HighPerformDays);
            Log.d("Turnover", "Result is: " + getIntent().getIntExtra("ResultTurnover", -1));
        });


    }
    private void checkInputs() {
        String highPerfDays = edtHighPerDays.getText().toString().trim();
        boolean spinnerSelected = spinnerShopHoli.getSelectedItemPosition() != 0; // assuming 0 = "Select Day"

        boolean enableButton = !highPerfDays.isEmpty() && spinnerSelected;

        btnNextSchedule.setEnabled(enableButton);
        btnNextSchedule.setAlpha(enableButton ? 1f : 0.5f);
    }

}





