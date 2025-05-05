package com.exam.shops;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class SetHoliHighDay extends AppCompatActivity {
    Spinner ResetspinnerShopHoli;
    EditText edtResetHighPerDays;
    Button btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set_holi_high_day);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ResetspinnerShopHoli = findViewById(R.id.ResetspinnerShopHoli);
        edtResetHighPerDays = findViewById(R.id.edtResetHighPerDays);
        btnReset = findViewById(R.id.btnReset);

        ArrayAdapter<String> shopholiday = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Select Day", "Sunday", "Monday", "Tuesday", "wednesday", "Thursday", "Friday", "Saturday"}
        );
        shopholiday.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ResetspinnerShopHoli.setAdapter(shopholiday);


        String[] daysArray = {"Monday", "Tuesday", "wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        boolean[] checkedDays = new boolean[daysArray.length];
        ArrayList<String> selectedDays = new ArrayList<>();

        edtResetHighPerDays.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(SetHoliHighDay.this);
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
                edtResetHighPerDays.setText(TextUtils.join(", ", selectedDays));
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();

        });

        btnReset.setOnClickListener(v -> {

            String shopHolidays = ResetspinnerShopHoli.getSelectedItem().toString();
            String HighPerformDays = edtResetHighPerDays.getText().toString().trim();


            SharedPreferences sharedPreferences = getSharedPreferences("shop_data", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("Shop_Holiday", shopHolidays);
            editor.putString("selected_days", HighPerformDays);
            editor.apply();
            Log.d("SetHoliHighDay", "Saved selected_days: " + HighPerformDays);

            Toast.makeText(this, "Reset Successfully", Toast.LENGTH_SHORT).show();

        });

    }
}