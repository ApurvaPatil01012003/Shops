package com.exam.shops;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    EditText edtName, edtMobile, edtActcode;
    Button btnActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        SharedPreferences sharedPref = getSharedPreferences("ShopData", MODE_PRIVATE);
        boolean isFirstTime = sharedPref.getBoolean("isFirstTime", true);

        if (!isFirstTime) {
            Intent intent = new Intent(MainActivity.this,MpinLogin.class);
            startActivity(intent);
            finish();
            return;
        }


        edtName = findViewById(R.id.edtName);
        edtMobile = findViewById(R.id.edtMobile);
        edtActcode = findViewById(R.id.edtActcode);
        btnActive = findViewById(R.id.btnActive);

        btnActive.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String mobile = edtMobile.getText().toString().trim();
            String actCode = edtActcode.getText().toString().trim();


            if (!name.isEmpty() && !mobile.isEmpty() && !actCode.isEmpty()) {
                if (mobile.length() == 10 && mobile.matches("\\d{10}")) {
                    saveDataToSharedPref(name, mobile, actCode);
                    Toast.makeText(this, "Data Saved!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, Mpin.class);
                    intent.putExtra("shop_name", name);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Please Enter 10 Digit Valid Mobile Number", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveDataToSharedPref(String name, String mobile, String actCode) {
        SharedPreferences sharedPref = getSharedPreferences("ShopData", MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("shop_name", name);
        editor.putString("mobile", mobile);
        editor.putString("activation_code", actCode);
        editor.putBoolean("isFirstTime", false);
        editor.apply();


    }
}