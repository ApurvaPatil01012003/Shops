package com.exam.shops;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class YOYSecondActivity extends AppCompatActivity {
    EditText et_date,edtAchieved,edtQty,edtNob;
    Button btnSave,btnDailyTable;
    private String key;
    private int updatedValue;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_yoysecond);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        et_date = findViewById(R.id.et_date);
        edtAchieved = findViewById(R.id.edtAchieved);
        edtQty = findViewById(R.id.edtQty);
        edtNob = findViewById(R.id.edtNob);
        btnSave = findViewById(R.id.btnSave);
        btnDailyTable=findViewById(R.id.btnDailyTable);

        String Holiday = getIntent().getStringExtra("ShopHoliday");
        if (!Holiday.isEmpty())
        {
            Log.d("Holidqay","Holiday is : "+Holiday);
        }

        int SecondTurnOverValue = getIntent().getIntExtra("TurnYear", 0);
Log.d("TURNOVER","turn over is : ; "+SecondTurnOverValue);

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .build();

        et_date.setOnClickListener(v -> {
            if (!datePicker.isAdded()) {
                datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
            }
        });

        datePicker.addOnPositiveButtonClickListener(selection -> {
            et_date.setText(datePicker.getHeaderText());
        });


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = et_date.getText().toString();
                String Achievedstr = edtAchieved.getText().toString();
                String Quantitystr = edtQty.getText().toString();
                String nobstr = edtNob.getText().toString();
                int quantity = 0;
                int Achieved = 0;
                int Nob = 0;
                try {
                    quantity = Integer.parseInt(Quantitystr);
                    Achieved = Integer.parseInt(Achievedstr);
                    Nob = Integer.parseInt(nobstr);

                } catch (Exception e) {
                    edtQty.setError("Enter a valid number");
                    return;
                }


                if (!date.isEmpty() && !Achievedstr.isEmpty() && !Quantitystr.isEmpty() && !nobstr.isEmpty()) {
                    SaveDataToSharedPref(date, Integer.parseInt(Achievedstr), Integer.parseInt(Quantitystr), Integer.parseInt(nobstr));
                    Toast.makeText(YOYSecondActivity.this, "Save data", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(YOYSecondActivity.this, "Please fill the all feilds", Toast.LENGTH_SHORT).show();
                }

            }
        });


        btnDailyTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(YOYSecondActivity.this,DailyTableYOY.class);
               intent.putExtra("ShopHoliday",Holiday);
                intent.putExtra("TurnYear", SecondTurnOverValue);
                startActivity(intent);
            }
        });
        SharedPreferences sharedPreferences=getSharedPreferences("ShopHolidays",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("SHOPHOLIDAY",Holiday);
        editor.putInt("TURNOVER",SecondTurnOverValue);
        Log.d("TURNOVER","turn over is : ; "+SecondTurnOverValue);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(YOYSecondActivity.this, GoToMAndD.class);

                if (key != null && !key.isEmpty()) {
                    intent.putExtra("data_key", key);
                    intent.putExtra("data_value", updatedValue);
                }

                startActivity(intent);
                finish();
            }
        });

    }
    public void SaveDataToSharedPref(String date, int Achieved, int Quantity, int nob) {
        SharedPreferences sharedPreferences = getSharedPreferences("YOY_PREFS", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String[] parts = date.split(" ");
        String month = parts[1];
        String yearStr = parts[2];
        int year = Integer.parseInt(yearStr);

        String shortMonth = convertToShortMonth(month);

        if (shortMonth.equals("Jan") || shortMonth.equals("Feb") || shortMonth.equals("Mar")) {
            year -= 1;
        }

        int startFY = getIntent().getIntExtra("TurnYear", 0);
        int endFY = startFY + 1;
        String fySuffix = String.valueOf(endFY).substring(2);

        String key = "data_" + shortMonth + "_" + startFY + "_" + fySuffix + "_Achieved";

        int prev = 0;
        try {
            prev = Integer.parseInt(sharedPreferences.getString(key, "0"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        int updatedValue = prev + Achieved;

        editor.putInt(key, updatedValue);
        editor.putString("last_selected_date", date);
        editor.apply();

        Log.d("YOY_DATA", key + " = " + updatedValue);

        this.key = key;
        this.updatedValue = updatedValue;

        finish();
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


}

