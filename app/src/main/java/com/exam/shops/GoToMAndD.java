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
Button btnyes,btnNo,btnDyes;

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
        btnyes=findViewById(R.id.btnyes);
        btnDyes=findViewById(R.id.btnDyes);

        int SecondTurnOverValue = getIntent().getIntExtra("TurnOver", 0);

        if (SecondTurnOverValue != 0) {
            Log.d("ReceivedGrowth", "Hidden growth value: " + SecondTurnOverValue);
        };


        String Holiday = getIntent().getStringExtra("shopsHoliday");
        Log.d("Holiday", "Holiday is : " + Holiday);



        btnyes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent(GoToMAndD.this,YOYActivity.class);
              intent.putExtra("TurnYear", SecondTurnOverValue);


              Log.d("Give_next","Data :"+SecondTurnOverValue);
                startActivity(intent);
            }
        });
        SharedPreferences sharedPrefs = getSharedPreferences("Shop_data",MODE_PRIVATE);
        SharedPreferences.Editor editor =  sharedPrefs.edit();
        editor.putInt("Growth_Year",SecondTurnOverValue);
        editor.apply();




        btnDyes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GoToMAndD.this, YOYSecondActivity.class);
                intent.putExtra("ShopHoliday",Holiday);
                intent.putExtra("TurnYear", SecondTurnOverValue);
                startActivity(intent);
                Log.d("DailyTurnOver","Daily turn Over is : "+SecondTurnOverValue);
               Log.d("ShopHoliday","Holiday is : "+Holiday);
            }
        });
        SharedPreferences sharedPreferences=getSharedPreferences("HolidaysPrefs",MODE_PRIVATE);
        SharedPreferences.Editor editor1 =sharedPreferences.edit();
        editor1.putString("SHOP_HOLIDAY",Holiday);
        editor1.putInt("TURNOVER",SecondTurnOverValue);
        editor1.apply();



    }
}