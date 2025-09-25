package com.exam.shops;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("ShopData", MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean("isFirstTime", true);
        String savedPin = prefs.getString("mpin", "");

       new Handler().postDelayed(() -> {
        Intent intent;
        if (isFirstTime || savedPin.isEmpty()) {
            // Start the first-time setup flow
            intent = new Intent(SplashScreen.this, SetSalesGoal.class);
        } else {
            // User has already set up MPIN → go to MPIN login
            intent = new Intent(SplashScreen.this, MpinLogin.class);
        }

           intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
       }, 2000);



    }
}
