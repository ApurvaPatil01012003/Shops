package com.exam.shops;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.LocalDate;

public class GoToMAndD extends AppCompatActivity {
    Button btnyes, btnDyes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_go_to_mand_d);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnyes = findViewById(R.id.btnyes);
        btnDyes = findViewById(R.id.btnDyes);


        Intent intent = getIntent();
        int SecondTurnOverValue = intent.getIntExtra("TurnOver", -1);
        String Holiday = intent.getStringExtra("shopsHoliday");
        String HighPerDay = intent.getStringExtra("HighPerformace");
        int growth = intent.getIntExtra("EdtGrowth", -1);


        SharedPreferences prefs = getSharedPreferences("ShopData", MODE_PRIVATE);
        if (SecondTurnOverValue == -1) {
            SecondTurnOverValue = prefs.getInt("Sturnover", 0);
        }
        if (Holiday == null) {
            Holiday = prefs.getString("Shop_Holiday", "");
        }
        if (HighPerDay == null) {
            HighPerDay = prefs.getString("selected_days", "");
        }
        if (growth == -1) {
            growth = prefs.getInt("editGrowth", 0);
        }

        Log.d("GoToMAndD", "From SharedPref or Intent:");
        Log.d("GoToMAndD", "TurnOver: " + SecondTurnOverValue);
        Log.d("GoToMAndD", "Holiday: " + Holiday);
        Log.d("GoToMAndD", "HighPerDay: " + HighPerDay);
        Log.d("GoToMAndD", "Growth: " + growth);


        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("TURNOVER", SecondTurnOverValue);
        editor.putString("SHOP_HOLIDAY", Holiday);
        editor.putString("High_per_day", HighPerDay);
        editor.putInt("Growth", growth);
        editor.apply();

        int finalSecondTurnOverValue = SecondTurnOverValue;
        btnyes.setOnClickListener(v -> {
            Intent i = new Intent(GoToMAndD.this, YOYActivity.class);
            i.putExtra("TurnYear", finalSecondTurnOverValue);
            startActivity(i);
        });

        String finalHoliday = Holiday;
        int finalSecondTurnOverValue1 = SecondTurnOverValue;
        String finalHighPerDay = HighPerDay;
        int finalGrowth = growth;
        btnDyes.setOnClickListener(v -> {
            Intent i = new Intent(GoToMAndD.this, YOYSecondActivity.class);
            i.putExtra("ShopHoliday", finalHoliday);
            i.putExtra("TurnYear", finalSecondTurnOverValue1);
            i.putExtra("HighPerformance", finalHighPerDay);
            i.putExtra("Growth", finalGrowth);
            startActivity(i);
        });
    }

}