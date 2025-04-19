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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Mpin extends AppCompatActivity {
    EditText etPin, etConfirmPin;
    Button btnNextPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mpin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        etPin = findViewById(R.id.etPin);
        etConfirmPin = findViewById(R.id.etConfirmPin);
        btnNextPin = findViewById(R.id.btnNextPin);

        String Name = getIntent().getStringExtra("shop_name");

        btnNextPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pin = etPin.getText().toString().trim();
                String confirmPin = etConfirmPin.getText().toString().trim();




                if (pin.isEmpty() || confirmPin.isEmpty()) {
                    Toast.makeText(Mpin.this, "Please enter both fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (pin.length() != 4) {
                    Toast.makeText(Mpin.this, "PIN should be 4 digits", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!pin.equals(confirmPin)) {
                    Toast.makeText(Mpin.this, "PINs do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                    SharedPreferences sharedPreferences = getSharedPreferences("shop_data", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("mpin", pin);
                    editor.putString("confirmMpin",confirmPin);
                    editor.putString("name",Name);
                    editor.apply();

                Log.d("Mpin","Mpin is : "+pin);
                Log.d("Mpin","confirmPin is : "+confirmPin);
                    Intent intent = new Intent(Mpin.this, SecondActivity.class);
                    intent.putExtra("shop_name", Name);
                    startActivity(intent);

            }
        });

    }
}