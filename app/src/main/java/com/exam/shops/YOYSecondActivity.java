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

import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class YOYSecondActivity extends AppCompatActivity {
    EditText et_date, edtAchieved, edtQty, edtNob;
    Button btnSave, btnDailyTable;
    private String key;
    private int updatedValue;
    private int updatedValueDaily;
    private float monthlyAchieved = 0f;
    private float monthlyPercent = 0f;
    private float monthlyTarget = 0f;




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
        btnDailyTable = findViewById(R.id.btnDailyTable);

        String Holiday = getIntent().getStringExtra("ShopHoliday");
        if (!Holiday.isEmpty()) {
            Log.d("Holidqay", "Holiday is : " + Holiday);
        }

//        int SecondTurnOverValue = getIntent().getIntExtra("TurnYear", 0);
//        Log.d("TURNOVER", "turn over is : ; " + SecondTurnOverValue);

        int Result = getIntent().getIntExtra("ResultTurnYear", 0);
        Log.d("TURNOVER", "Result turn over is : ; " + Result);


        String High_Per_Day = getIntent().getStringExtra("HighPerformance");
        int Growth_Per = getIntent().getIntExtra("Growth", 0);

        Log.d("HIGHPERFORM", "HIGH DAY" + High_Per_Day);
        Log.d("Growth", "groth is : " + Growth_Per);

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
                    //  edtQty.setError("Enter a valid number");
                    return;
                }


                if (!date.isEmpty() && !Achievedstr.isEmpty() && !Quantitystr.isEmpty() && !nobstr.isEmpty()) {
                    int ach = Integer.parseInt(Achievedstr);
                    int qty = Integer.parseInt(Quantitystr);
                    int nob = Integer.parseInt(nobstr);

                    SaveDataToSharedPref(date, ach, qty, nob);
                    SaveDailyDataToSharedPref(date, ach, qty, nob);

                    Toast.makeText(YOYSecondActivity.this, "Data saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(YOYSecondActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }


            }

        });

        btnDailyTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = et_date.getText().toString().trim();
                String Achievedstr = edtAchieved.getText().toString().trim();
                String Quantitystr = edtQty.getText().toString().trim();
                String nobstr = edtNob.getText().toString().trim();

                int achieved = 0, quantity = 0, nob = 0;

                if (!Achievedstr.isEmpty()) {
                    achieved = Integer.parseInt(Achievedstr);
                }

                if (!Quantitystr.isEmpty()) {
                    quantity = Integer.parseInt(Quantitystr);
                }

                if (!nobstr.isEmpty()) {
                    nob = Integer.parseInt(nobstr);
                }
//
//                if (!date.isEmpty()) {
//                    SaveDailyDataToSharedPref(date, achieved, quantity, nob);
//                }

                Intent intent = new Intent(YOYSecondActivity.this, DailyTableYOY.class);
                intent.putExtra("ShopHoliday", Holiday);
                intent.putExtra("ResultTurnYear", Result);
                //intent.putExtra("TurnYear", SecondTurnOverValue);
                intent.putExtra("Achived_Value", updatedValueDaily);
                intent.putExtra("Quantity", quantity);
                intent.putExtra("NOB", nob);
                intent.putExtra("HighPerDay", High_Per_Day);
                intent.putExtra("Growth", Growth_Per);

//                Log.d("Updated_Daily", "Updated for daily: " + updatedValueDaily);
//                Log.d("Updated_Daily", "quantity for daily: " + quantity);
//                Log.d("Updated_Daily", "nob for daily: " + nob);
                // startActivity(intent);
                startActivityForResult(intent, 101);
                ClearAllText();

            }
        });


//        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
//            @Override
//            public void handleOnBackPressed() {
//                Intent intent = new Intent(YOYSecondActivity.this, GoToMAndD.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//
////                if (key != null && !key.isEmpty()) {
////                    intent.putExtra("data_key", key);
////                    intent.putExtra("data_value", updatedValue);
////
////                }
//                if (key != null && !key.isEmpty()) {
//                    intent.putExtra("data_key", key);
//                    intent.putExtra("data_value", updatedValue);
//                }
//
//// Always send the monthly data
//                intent.putExtra("MonthlyAchieved", monthlyAchieved);
//                intent.putExtra("MonthlyAchievedPercent", monthlyPercent);
//
//
//
//                startActivity(intent);
//                finish();
//            }
//        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(YOYSecondActivity.this, GoToMAndD.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                intent.putExtra("MonthlyTarget", Result / 12f);
                intent.putExtra("MonthlyAchieved", updatedValueDaily);
                SharedPreferences todayPrefs = getSharedPreferences("TodayData", MODE_PRIVATE);
                float percent = todayPrefs.getFloat("today_percent", 0f);
                intent.putExtra("MonthlyAchievedPercent", percent);

                if (key != null && !key.isEmpty()) {
                    intent.putExtra("data_key", key);
                    intent.putExtra("data_value", updatedValue);
                }


                setResult(RESULT_OK);

               // startActivity(intent);
                finish();
            }
        });
    }

        public void ClearAllText() {
        et_date.setText("");
        edtAchieved.setText("");
        edtQty.setText("");
        edtNob.setText("");
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
            year = Integer.parseInt(yearStr);
            ;
        }

        String key = "data_" + shortMonth + "_" + year + "_Achieved";

        int prev = sharedPreferences.getInt(key, 0);
        int updatedValue = prev + Achieved;

        editor.putInt(key, updatedValue);
        editor.putString("last_selected_date", date);
        editor.apply();

        //Log.d("YOY_DATA", key + " = " + updatedValue);

        this.key = key;
        this.updatedValue = updatedValue;


        //  finish();
    }

    public void SaveDailyDataToSharedPref(String dateInput, int Achieved, int Quantity, int nob) {
        SharedPreferences sharedPreferences = getSharedPreferences("Shop Data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
        SimpleDateFormat saveFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);

        String formattedDate = "";
        try {
            formattedDate = saveFormat.format(inputFormat.parse(dateInput));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String key = "Achieved_" + formattedDate;
        String keyQty = "Quantity_" + formattedDate;
        String keyNob = "NOB_" + formattedDate;


        int prev = sharedPreferences.getInt(key, 0);
        int prevQty = sharedPreferences.getInt(keyQty, 0);
        int prevNob = sharedPreferences.getInt(keyNob, 0);

        updatedValueDaily = prev + Achieved;
        int updatedQty = prevQty + Quantity;
        int updatedNob = prevNob + nob;

        editor.putInt(key, updatedValueDaily);
        editor.putInt(keyQty, updatedQty);
        editor.putInt(keyNob, updatedNob);

        editor.putString("last_selected_date", formattedDate);
        editor.apply();

        Log.d("YOY_DATA_ForDaily", key + " = " + updatedValueDaily);
        Log.d("YOY_DATA_ForDaily", keyQty + " = " + Quantity);
        Log.d("YOY_DATA_ForDaily", keyNob + " = " + nob);

        this.key = key;
        this.updatedValueDaily = updatedValueDaily;



    //chage the Achived and percentage in cardview of GotoActivity
        String todayStr = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(System.currentTimeMillis());
        if (formattedDate.equals(todayStr)) {
            SharedPreferences todayPref = getSharedPreferences("TodayData", MODE_PRIVATE);
            SharedPreferences.Editor todayEditor = todayPref.edit();

            String expectedStr = todayPref.getString("today_expected", "0");
            float expected = 0f;
            try {
                expected = Float.parseFloat(expectedStr);
            } catch (Exception e) {
                expected = 0f;
            }

            float percentage = (expected != 0f) ? ((float) updatedValueDaily / expected) * 100f : 0f;

            todayEditor.putInt("today_achieved", updatedValueDaily);
            todayEditor.putFloat("today_percent", percentage);
            todayEditor.apply();

            Log.d("TodayData", "Only updated Achieved=" + updatedValueDaily + ", Percentage=" + percentage);
        }


    }
//   public void SaveDailyDataToSharedPref(String dateInput, int Achieved, int Quantity, int nob) {
//       SharedPreferences sharedPreferences = getSharedPreferences("YOY_PREFS", MODE_PRIVATE); // <- Fixed
//       SharedPreferences.Editor editor = sharedPreferences.edit();
//
//       SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
//       SimpleDateFormat saveFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
//
//       String formattedDate = "";
//       try {
//           formattedDate = saveFormat.format(inputFormat.parse(dateInput));
//       } catch (Exception e) {
//           e.printStackTrace();
//       }
//
//       // Extract day, month, year
//       String[] dateParts = formattedDate.split("-");
//       String day = dateParts[0];
//       String month = dateParts[1];
//       String year = dateParts[2];
//
//       String key = "day_" + day + "_" + month + "_" + year + "_Achieved";
//       int prev = sharedPreferences.getInt(key, 0);
//       updatedValueDaily = prev + Achieved;
//
//       editor.putInt(key, updatedValueDaily);
//       editor.putString("last_selected_date", formattedDate);
//       editor.apply();
//
//       Log.d("YOY_DATA_FIXED", key + " = " + updatedValueDaily);
//   }


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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            monthlyAchieved = data.getFloatExtra("MonthlyAchieved", 0f);
            monthlyPercent = data.getFloatExtra("MonthlyAchievedPercent", 0f);

            // Assume 12 months in year
            monthlyTarget = getIntent().getIntExtra("ResultTurnYear", 0) / 12f;

            Toast.makeText(this,
                    "Month Total: ₹" + monthlyAchieved +
                            "\nAchieved: " + String.format("%.2f", monthlyPercent) + "%",
                    Toast.LENGTH_LONG
            ).show();
        }
    }



}

